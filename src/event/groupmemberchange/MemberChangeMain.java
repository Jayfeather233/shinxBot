package event.groupmemberchange;

import com.alibaba.fastjson.JSONObject;
import main.EventProcessable;
import main.Main;

public class MemberChangeMain implements EventProcessable {
    @Override
    public void process(JSONObject J) {
        String name = Main.getName(J.getLong("user_id"));
        if (name == null) name = Main.getUserName(J.getLong("group_id"), J.getLong("user_id"));
        name = name + " (" + J.getLong("user_id") + ")";
        if (J.getString("notice_type").equals("group_decrease"))
            Main.setNextSender("group", 0, J.getLong("group_id"), name + "离开了我们……");
        if (J.getString("notice_type").equals("group_increase"))
            Main.setNextSender("group", 0, J.getLong("group_id"), "欢迎 " + name + " 的加入");
    }

    @Override
    public boolean check(JSONObject J) {
        if (!J.getString("post_type").equals("notice")) return false;
        return J.getString("notice_type").equals("group_decrease") || J.getString("notice_type").equals("group_increase");
    }
}
