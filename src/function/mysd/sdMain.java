package function.mysd;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import httpconnect.HttpURLConnectionUtil;
import interfaces.Processable;
import main.Main;

import java.io.*;
import java.util.Objects;
import java.util.Scanner;
import java.util.UnknownFormatConversionException;

import static main.Main.localPath;
import static main.Main.setNextLog;
import static utils.saveImg.saveBase64Img;

public class sdMain implements Processable {

    static private int state = 2;

    static private String hashCodex;

    private JSONArray groups;

    private final JSONArray pattern = JSONArray.parseArray(/*"""
            [
            "",
            50,
            "k_euler_a",
            [
            "Normalize Prompt Weights (ensure sum of weights add up to 1.0)",
            "Save individual images",
            "Save grid",
            "Sort samples by prompt",
            "Write sample info files"
            ],
            "RealESRGAN_x4plus",
            0,
            2,
            2,
            7.5,
            "",
            448,320,
            null,
            0,
            "",
            "",
            false,false,false,3
            ]
            """*/"");

    public sdMain() {
        try {
            File ff = new File("sdLevel.json");
            if (!ff.exists()) {
                if (!ff.createNewFile()) System.out.println("sdLevel创建失败");
                else {
                    FileWriter fw = new FileWriter(ff);
                    fw.write("{\"data\":[]}");
                    fw.close();
                }
            }
            FileReader f = new FileReader("sdLevel.json");
            Scanner S = new Scanner(f);
            StringBuilder sb = new StringBuilder();
            while (S.hasNext()) {
                sb.append(S.nextLine()).append(' ');
            }

            JSONObject J = JSONObject.parseObject(String.valueOf(sb));

            groups = J.getJSONArray("data");
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void process(String message_type, String message, long group_id, long user_id, int message_id) {
        message = message.replace("\r","");
        message = message.replace("\n","");
        if (message.equals("sd.help")) {
            Main.setNextSender(message_type, user_id, group_id,
                    /*"""
                            Stable Diffusion:
                                sd prompt:xxx H:xxx W:xxx CF:xxx STEP:xxx
                            prompt: 提示词
                            H: 图片高度，默认320 [50,512]
                            W: 图片宽度，默认512 [50,512]
                            CF: 多大依赖提示词，默认7.5 [2,10]
                            STEP: 生成步骤数，默认50 [0,150]
                            """*/"");
            return;
        }
        if(message.equals("sd.on")){
            state = 2;
            return;
        }
        if(message.equals("sd.off")){
            state = 0;
            return;
        }
        if(message.equals("sd.mid")){
            state = 1;
            return;
        }
        if(message.startsWith("sd.add")){
            groups.add(Long.parseLong(message.substring(6).trim()));
            try{
                save();
            } catch (IOException e){
                Main.setNextSender(message_type,user_id,group_id,"文件保存失败");
                Main.setNextLog("sd.add: 文件保存失败",1);
            }
            return;
        }
        if(state == 0 || (state == 1 && !groups.toJavaList(Long.class).contains(group_id))) return;
        String[] ps = message.substring(2).split(" ");
        StringBuilder prompt = new StringBuilder();
        boolean flg = false;
        int H = 320, W = 512, step = 50;
        double CF = 7.5;
        try {
            for (String s : ps) {
                if (s.startsWith("prompt:")) {
                    prompt.append(s.substring(7));
                    flg = true;
                } else if (s.startsWith("H:")) {
                    H = Integer.parseInt(s.substring(2));
                } else if (s.startsWith("W:")) {
                    W = Integer.parseInt(s.substring(2));
                } else if (s.startsWith("STEP:")) {
                    step = Integer.parseInt(s.substring(5));
                } else if (s.startsWith("CF:")) {
                    CF = Double.parseDouble(s.substring(3));
                } else if (s.length() > 0) prompt.append(' ').append(s);
            }
        } catch (NumberFormatException e) {
            Main.setNextSender(message_type, user_id, group_id, "参数错误：不是数字");
            return;
        }
        if(!flg){
            return;
        }
        if (H < 50 || W < 10) {
            Main.setNextSender(message_type, user_id, group_id, "参数错误：图片太小");
            return;
        }
        if (H > 512 || W > 512) {
            Main.setNextSender(message_type, user_id, group_id, "参数错误：图片太大");
            return;
        }
        if (CF < 2 || CF > 10) {
            Main.setNextSender(message_type, user_id, group_id, "参数错误：CF");
            return;
        }
        if (step < 1 || step > 150) {
            Main.setNextSender(message_type, user_id, group_id, "参数错误：STEP");
            return;
        }
        JSONArray ja = new JSONArray(pattern);
        ja.set(0, prompt);
        ja.set(1, step);
        ja.set(8, CF);
        ja.set(10, H);
        ja.set(11, W);
        if(hashCodex != null){
            Main.setNextSender(message_type,user_id,group_id,"有任务进行中，请稍后重试");
            return;
        }
        hashCodex = String.valueOf(Math.abs(ja.hashCode()));
        ja.set(15, hashCodex);

        Main.setNextSender(message_type,user_id,group_id,"正在生成中...");

        JSONArray hashCode = new JSONArray();
        hashCode.set(0, hashCodex);
        JSONObject J = new JSONObject();
        J.put("data", ja);
        J.put("fn_index", 14);
        J.put("session_hash", String.valueOf(J.hashCode()));
        String rs = String.valueOf(HttpURLConnectionUtil.doPost("http://localhost:7860/api/txt2img/", J));
        if(Objects.equals(rs, "null")){
            Main.setNextSender(message_type,user_id,group_id,"程序在重启或已关闭。");
            hashCodex = null;
            return;
        }
        J.put("data", hashCode);
        J.put("fn_index", 13);
        HttpURLConnectionUtil.doPost("http://localhost:7860/api/txt2img/", J);
        J.put("fn_index", 12);
        JSONObject mid = JSONObject.parseObject(Objects.requireNonNull(HttpURLConnectionUtil.doPost("http://localhost:7860/api/txt2img/", J)).toString());
        try{
            Main.setNextSender(message_type,user_id,group_id,(mid.getJSONArray("data").getString(2)+"%")/*.formatted()*/);
        } catch (UnknownFormatConversionException e){
            Main.setNextSender(message_type,user_id,group_id,(mid.getJSONArray("data").getString(2))/*.formatted()*/);
        }
        hashCodex = null;
        J.put("fn_index", 11);
        HttpURLConnectionUtil.doPost("http://localhost:7860/api/txt2img/", J);
        J.put("fn_index", 4);
        J = JSONObject.parseObject(
                Objects.requireNonNull(
                        HttpURLConnectionUtil.doPost("http://localhost:7860/api/txt2img", J)).toString());
        ja = J.getJSONArray("data");

        StringBuilder sb = new StringBuilder();

        for (JSONArray jaa : ja.toJavaList(JSONArray.class)) {
            for (String s : jaa.toJavaList(String.class)) {
                sb.append("[CQ:image,file=file:///").append(localPath).append(saveBase64Img(s)).append(",id=40000]\n");
            }
        }

        setNextLog("Stable Diffusion at group " + group_id + " by " + user_id + " input: " + message, 0);
        Main.setNextSender(message_type, user_id, group_id, sb.toString());
    }

    private void save() throws IOException {
        FileWriter fw = new FileWriter("sdLevel.json", false);
        BufferedWriter bw = new BufferedWriter(fw);
        JSONObject J = new JSONObject();
        J.put("data", groups);
        bw.write(J.toString());
        bw.close();
        fw.close();
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return message.startsWith("sd");
    }

    @Override
    public String help() {
        return "Stable Diffusion: sd.help";
    }
}
