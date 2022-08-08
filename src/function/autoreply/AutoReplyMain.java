package function.autoreply;

import com.alibaba.fastjson.JSONObject;
import main.Main;
import main.Processable;

import java.io.*;
import java.util.Scanner;

public class AutoReplyMain implements Processable {

    JSONObject replyData = new JSONObject();


    public AutoReplyMain() {
        try {
            File ff = new File("replydata.json");
            if (!ff.exists()) {
                if (!ff.createNewFile()) System.out.println("自动回复文件创建失败");
                else {
                    FileWriter fw = new FileWriter(ff);
                    fw.write("{}");
                    fw.close();
                }
            }
            FileReader f = new FileReader("replydata.json");
            Scanner S = new Scanner(f);
            StringBuilder sb = new StringBuilder();
            while (S.hasNext()) {
                sb.append(S.nextLine()).append(' ');
            }
            replyData = JSONObject.parseObject(String.valueOf(sb));
            S.close();
            f.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try {
            FileWriter fw = new FileWriter("replydata.json", false);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(replyData.toString());
            bw.close();
            fw.close();
        } catch (IOException e) {
            System.out.println("文件读写出错");
        }
    }

    @Override
    public void process(String message_type, String message, long group_id, long user_id) {
        message = message.substring(2).trim();
        if (message.startsWith("添加") && (user_id == ((long) 34258) * 100000 + 11925 || user_id == 1826559889)) {
            message = message.substring(2).trim();
            Scanner S = new Scanner(message);
            String key = S.next();
            String ans = S.next();
            if (S.hasNext()) ans = ans + S.nextLine();
            replyData.put(key, ans);
            save();
            Main.setNextSender(message_type, user_id, group_id, "添加成功");
        } else if (message.startsWith("删除") && (user_id == ((long) 34258) * 100000 + 11925 || user_id == 1826559889)) {
            message = message.substring(2).trim();
            if (replyData.containsKey(message)) {
                replyData.remove(message);
                save();
                Main.setNextSender(message_type, user_id, group_id, "删除成功");
            } else {
                Main.setNextSender(message_type, user_id, group_id, "未找到");
            }
        } else if (message.equals("帮助")) {
            StringBuilder sb = new StringBuilder("##帮助\n");
            for (String u : replyData.keySet()) {
                sb.append("##").append(u).append('\n');
            }
            Main.setNextSender(message_type, user_id, group_id, sb.toString());
        } else {
            if (replyData.containsKey(message)) {
                Main.setNextSender(message_type, user_id, group_id, replyData.getString(message));
            } else {
                Main.setNextSender(message_type, user_id, group_id, "未找到匹配内容");
            }
        }
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return message.startsWith("##");
    }

    @Override
    public String help() {
        return "自动关键词回复： 请输入 ##帮助";
    }
}
