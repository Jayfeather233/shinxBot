package function.autoForwardGenerator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import main.Main;
import interfaces.Processable;

import java.util.Scanner;

public class AutoForwardGeneratorMain implements Processable {

    private JSONObject getData(String s1, Scanner S, long group_id) {
        JSONObject J2 = new JSONObject();
        StringBuilder s2 = new StringBuilder(S.nextLine().trim());
        if(s2.toString().equals("合并行")){
            String nxt = S.nextLine().trim();
            while(!nxt.equals("结束合并")){
                s2.append('\n').append(nxt);
                nxt=S.nextLine().trim();
            }
            s2= new StringBuilder(s2.substring(4));
        }
        String uin;
        if (s1.startsWith("[CQ:at,qq=")) {
            s1 = s1.substring(10);
            int i = 0;
            while (i != s1.length() && s1.charAt(i) != ']') i++;
            uin = s1.substring(0, i);
            s1 = Main.getUserName(group_id, Long.parseLong(uin));
        } else {
            uin = s1;
            s1 = Main.getUserName(group_id, Long.parseLong(s1));
        }
        J2.put("name", s1);
        J2.put("uin", uin);
        if (s2.toString().equals("转发")) {
            J2.put("content", getContent(S, group_id));
        } else {
            int pos = 0;
            do {
                pos = s2.indexOf("[CQ:at,qq=", pos);
                if (pos == -1) break;
                int po1 = pos + 10;
                while (s2.charAt(pos) != ']') pos++;
                s2 = new StringBuilder(s2.substring(0, pos) + ",name=" + Main.getUserName(group_id, Long.parseLong(s2.substring(po1, pos))) + s2.substring(pos));
            } while (true);
            J2.put("content", s2.toString());
        }
        return J2;
    }


    private JSONArray getContent(Scanner S, long group_id) {
        String s1;
        JSONArray JA = new JSONArray();
        while (S.hasNext()) {
            s1 = S.next();
            if (s1.equals("结束转发")) {
                break;
            }
            JSONObject J = new JSONObject();
            J.put("type", "node");
            J.put("data", getData(s1, S, group_id));
            JA.add(J);
        }
        return JA;
    }

    @Override
    public void process(String message_type, String message, long group_id, long user_id, int message_id) {
        message = message.substring(2);
        if (message.equals("帮助")) {
            Main.setNextSender(message_type, user_id, group_id, "格式为：\n转发\n[@某人或qq号] 消息（一整行）\n[@某人或qq号] 合并行\n（多行消息）\n结束合并\n...\n[@某人或qq号] 转发\n（此处为转发内套转发）\n结束转发\n...\n结束转发 ");
            return;
        }
        Scanner S = new Scanner(message);
        JSONArray JA = getContent(S, group_id);

        JSONObject J = new JSONObject();
        J.put("messages", JA);

        if (message_type.equals("group")) {
            J.put("group_id", group_id);
            Main.setNextSender("send_group_forward_msg", J);
            Main.setNextLog("AutoForward at group " + group_id + " by "+user_id,0);
        } else {
            J.put("user_id", user_id);
            Main.setNextSender("send_private_forward_msg", J);
            Main.setNextLog("AutoForward at private by "+user_id,0);
        }
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return message.startsWith("转发");
    }

    @Override
    public String help() {
        return "自动生成转发信息： 详细资料请输入 转发帮助";
    }

}
