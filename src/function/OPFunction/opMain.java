package function.OPFunction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import interfaces.Processable;
import main.Main;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static utils.userIDGetter.getID;

public class opMain implements Processable {

    private final Map< Long, Boolean> opEnable = new HashMap<>();
    private int duration = 60;

    public opMain(){
        try {
            File ff = new File("./config/op.json");
            if (!ff.exists()) {
                if (!ff.createNewFile()) System.out.println("op权限管理文件创建失败");
                else {
                    FileWriter fw = new FileWriter(ff);
                    fw.write("[60]");
                    fw.close();
                }
            }
            FileReader f = new FileReader("./config/op.json");
            Scanner S = new Scanner(f);
            StringBuilder sb = new StringBuilder();
            while (S.hasNext()) {
                sb.append(S.nextLine()).append(' ');
            }
            JSONArray JA = JSONArray.parseArray(sb.toString());
            duration = (int) JA.get(0);
            JA.remove(0);
            for(long u : JA.toJavaList(Long.class)){
                opEnable.put(u,true);
            }
            S.close();
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void saveOP() throws IOException {
        FileWriter fw = new FileWriter("./config/op.json", false);
        BufferedWriter bw = new BufferedWriter(fw);
        JSONArray JA = new JSONArray();
        JA.add(duration);
        JA.addAll(opEnable.keySet());
        bw.write(JA.toString());
        bw.close();
        fw.close();
    }

    @Override
    public void process(String message_type, String message, long group_id, long user_id, int message_id) {
        message = message.substring(3);
        if(message.startsWith("help")){
            Main.setNextSender(message_type,user_id,group_id,
                    "op.blacklist [QQid or @xxx] : kick him out and will not receive his request\n" +
                            "op.kick [QQid or @xxx] : kick him out\n" +
                            "op.shut [QQid or @xxx] : shut him for a period of time.\n" +
                            "op.unshut [QQid or @xxx] : unshut him.\n" +
                            "op.setshut [time(in seconds)] : set shut time\n" +
                            "op.switch/op.enable : switch or enable this feature.");
        }else if(message.startsWith("blacklist")){
            long id = getID(message.substring(9));
            JSONObject J = new JSONObject();
            J.put("group_id",group_id);
            J.put("user_id",id);
            J.put("reject_add_request",true);
            Main.setNextSender("set_group_kick",J);
            Main.setNextLog("blacklist " + id + " in group " + group_id + " by " + user_id, 0);
            Main.setNextSender(message_type,user_id,group_id,"kick off " + Main.getUserName(group_id,id) + "(" + id + ") and will not receive request.");
        } else if(message.startsWith("kick")){
            long id = getID(message.substring(4));
            JSONObject J = new JSONObject();
            J.put("group_id",group_id);
            J.put("user_id",id);
            Main.setNextSender("set_group_kick",J);
            Main.setNextLog("kick " + id + " in group " + group_id + " by " + user_id, 0);
            Main.setNextSender(message_type,user_id,group_id,"kick off " + Main.getUserName(group_id,id)+ "(" + id + ")");
        } else if(message.startsWith("shut")){
            long id = getID(message.substring(4));
            JSONObject J = new JSONObject();
            J.put("group_id",group_id);
            J.put("user_id",id);
            J.put("duration",duration);
            Main.setNextSender("set_group_ban",J);
            Main.setNextLog("ban " + id + " in group " + group_id + " by " + user_id, 0);
            Main.setNextSender(message_type,user_id,group_id,"shut " + Main.getUserName(group_id,id) + "(" + id + ") for " + duration + "s.");
        } else if(message.startsWith("unshut")){
            long id = getID(message.substring(6));
            JSONObject J = new JSONObject();
            J.put("group_id",group_id);
            J.put("user_id",id);
            J.put("duration",0);
            Main.setNextSender("set_group_ban",J);
            Main.setNextLog("unshut " + id + " in group " + group_id + " by " + user_id, 0);
            Main.setNextSender(message_type,user_id,group_id,"unshut " + Main.getUserName(group_id,id) + "(" + id + ").");
        } else if(message.startsWith("setshut")){
            duration = Integer.parseInt(message.substring(7).trim());
            try {
                saveOP();
            } catch (IOException e){
                e.printStackTrace();
            }
            Main.setNextSender(message_type,user_id,group_id,"shut time set to " + duration + ".");
        } else if(message.startsWith("switch") || message.startsWith("enable")){
            if(opEnable.containsKey(group_id) && message.startsWith("switch")){
                opEnable.remove(group_id);
                Main.setNextSender(message_type,user_id,group_id,"op disabled.");
            }else{
                if(!opEnable.containsKey(group_id)) opEnable.put(group_id,true);
                Main.setNextSender(message_type,user_id,group_id,"op enabled.");
            }
            try {
                saveOP();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return message_type.equals("group") && message.startsWith("op.") && (opEnable.containsKey(group_id) || message.equals("op.switch") || message.equals("op.enable") || message.equals("op.help"));
    }

    @Override
    public String help() {
        return null;
    }
}
