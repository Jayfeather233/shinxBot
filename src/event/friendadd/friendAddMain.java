package event.friendadd;

import com.alibaba.fastjson.JSONObject;
import main.EventProcessable;
import main.Main;

public class friendAddMain implements EventProcessable {
    @Override
    public void process(JSONObject J) {
        JSONObject Js = new JSONObject();
        Js.put("flag", J.getString("flag"));
        Js.put("approve", true);
        Main.setNextSender("set_friend_add_request", Js);
        Main.getFriendSet().add(J.getLong("user_id"));
        Main.setNextLog("Add a friend:" + J.getLong("user_id"),0);
    }

    @Override
    public boolean check(JSONObject J) {
        return J.containsKey("request_type") && J.getString("request_type").equals("friend");
    }
}