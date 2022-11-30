package event.dragonKing;

import com.alibaba.fastjson.JSONObject;
import interfaces.EventProcessable;
import main.Main;

public class dragonMain implements EventProcessable {
    @Override
    public void process(JSONObject J) {
        Main.setNextSender("group",J.getLong("user_id"),J.getLong("group_id"),"[CQ:at,qq="+ J.getLong("user_id")+"] 获得了龙王标识！");
    }

    @Override
    public boolean check(JSONObject J) {
        return J.containsKey("honor_type") && J.getString("honor_type").equals("talkative");
    }
}
