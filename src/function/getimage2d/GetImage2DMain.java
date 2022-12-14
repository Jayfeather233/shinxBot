package function.getimage2d;

import com.alibaba.fastjson.JSONObject;
import httpconnect.HttpURLConnectionUtil;
import main.Main;
import interfaces.Processable;

import java.net.SocketTimeoutException;

public class GetImage2DMain implements Processable {

    @Override
    public void process(String message_type, String message, long group_id, long user_id, int message_id) {
        try {
            JSONObject J = JSONObject.parseObject(HttpURLConnectionUtil.doGet("https://www.dmoe.cc/random.php?return=json"));
            Main.setNextSender(message_type, user_id, group_id, "[CQ:image,file=" + J.getString("imgurl") + ",id=40000]");
            Main.setNextLog("Auto2DAnimateImg at group " + group_id + " by "+user_id,0);
        } catch (SocketTimeoutException e) {
            Main.setNextSender(message_type, user_id, group_id, "网站链接超时");
            Main.setNextLog("Auto2DAnimateImg at group " + group_id + " by "+user_id + " Connected Time Out",2);
        }
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return message.equals("来点二次元");
    }

    @Override
    public String help() {
        return "二次元图，来源dmoe： 来点二次元";
    }
}
