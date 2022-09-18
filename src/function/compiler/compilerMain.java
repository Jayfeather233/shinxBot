package function.compiler;

import com.alibaba.fastjson.JSONObject;
import httpconnect.HttpURLConnectionUtil;
import main.Main;
import interfaces.Processable;

import java.io.*;
import java.util.Objects;
import java.util.Scanner;

public class compilerMain implements Processable {

    private String clientId;
    private String clientSecret;

    public compilerMain() {
        try {
            FileReader f = new FileReader("compilerKey.json");
            Scanner S = new Scanner(f);
            StringBuilder sb = new StringBuilder();
            while (S.hasNextLine()) sb.append(S.nextLine());
            JSONObject J = JSONObject.parseObject(sb.toString());
            clientId = J.getString("clientId");
            clientSecret = J.getString("clientSecret");
        } catch (FileNotFoundException e) {
            try {
                System.out.println("如果想使用在线编译功能，请在compilerKey.json里填写JDoodle的账户信息");
                FileWriter fw = new FileWriter("compilerKey.json", false);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write("""
                        {
                          "clientId": "",
                          "clientSecret": ""
                        }""");
                bw.close();
                fw.close();
            } catch (IOException ee) {
                System.out.println("文件读写出错");
            }
        }
    }

    @Override
    public void process(String message_type, String message, long group_id, long user_id, int message_id) {
        Scanner S = new Scanner(message.substring(4));
        String language = S.next();
        language = language.toLowerCase();
        String ttmp;
        StringBuilder code = new StringBuilder();
        while (S.hasNextLine()) {
            ttmp = S.nextLine();
            if (ttmp.equals("输入")) break;
            code.append(ttmp).append('\n');
        }
        StringBuilder inp = new StringBuilder();
        while (S.hasNextLine()) {
            inp.append(S.nextLine()).append('\n');
        }

        String script = code.toString();
        String versionIndex;

        switch (language) {
            case "c" -> versionIndex = "5";
            case "java" -> versionIndex = "4";
            case "c++" -> {
                language = "cpp";
                versionIndex = "5";
            }
            case "python" -> {
                language = "python3";
                versionIndex = "4";
            }
            default -> {
                Main.setNextSender(message_type, user_id, group_id, "不支持的语言类型");
                Main.setNextLog("Compiler at group " + group_id + " by "+user_id + " unsupported language " + language,0);
                return;
            }
        }

        JSONObject J = new JSONObject();
        J.put("clientId", clientId);
        J.put("clientSecret", clientSecret);
        J.put("script", script);
        J.put("language", language);
        J.put("versionIndex", versionIndex);
        if (inp.length() != 0) J.put("stdin", inp);

        J = JSONObject.parseObject(Objects.requireNonNull(HttpURLConnectionUtil.doPost("https://api.jdoodle.com/v1/execute", J)).toString());

        if (J.containsKey("error")) {
            Main.setNextSender(message_type, user_id, group_id, J.getString("error"));
            Main.setNextLog("Compiler at group " + group_id + " by "+user_id + " error",1);
        } else {
            Main.setNextSender(message_type, user_id, group_id,
                    "输出：" + J.getString("output") + '\n'
                            + "内存：" + J.getString("memory") + '\n'
                            + "CPU时间：" + J.getString("cpuTime"));
            Main.setNextLog("Compiler at group " + group_id + " by "+user_id,0);
        }
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return message.startsWith("编译代码");
    }

    @Override
    public String help() {
        return "在线编译代码： 编译代码+[language:{c++,java,python}]+换行+代码";
    }
}
