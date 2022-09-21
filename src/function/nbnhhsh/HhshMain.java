package function.nbnhhsh;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import httpconnect.HttpURLConnectionUtil;
import interfaces.Processable;
import main.Main;

public class HhshMain implements Processable {

    @Override
    public void process(String message_type, String message, long group_id, long user_id, int message_id) {
        message = message.substring(4).trim();
        JSONObject J = new JSONObject();
        J.put("text", message.replace(' ', ','));

        JSONArray JA = JSONArray.parseArray(String.valueOf(HttpURLConnectionUtil.doPost("https://lab.magiconch.com/api/nbnhhsh/guess", J)));

        StringBuilder sb = new StringBuilder();
        boolean flg = false;

        for (Object o : JA) {
            if (flg) sb.append('\n');
            J = (JSONObject) o;
            if (J.containsKey("inputting")) {
                sb.append(J.getString("name")).append("有可能是\n");
                int t = 0;
                for (String s : J.getJSONArray("inputting").toJavaList(String.class)) {
                    sb.append(s).append("  ");
                    t++;
                    if (t == 3) break;
                }
            } else if (J.containsKey("trans")) {
                if (J.getString("trans") == null) {
                    sb.append(J.getString("name")).append("未收录");
                } else {
                    sb.append(J.getString("name")).append("是\n");
                    int t = 0;
                    for (String s : J.getJSONArray("trans").toJavaList(String.class)) {
                        sb.append(s).append("  ");
                        t++;
                        if (t == 3) break;
                    }
                }
            } else {
                sb.append(J.getString("name")).append("未收录");
            }
            flg = true;
        }
        Main.setNextLog("hhsh at group " + group_id + " by " + user_id, 0);
        Main.setNextSender(message_type, user_id, group_id, sb.toString());
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return message.startsWith("hhsh");
    }

    @Override
    public String help() {
        return "能不能好好说话：hhsh +缩写";
    }
}
