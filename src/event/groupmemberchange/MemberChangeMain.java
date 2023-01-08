package event.groupmemberchange;

import com.alibaba.fastjson.JSONObject;
import interfaces.EventProcessable;
import main.Main;

import java.util.Objects;

public class MemberChangeMain implements EventProcessable {
    @Override
    public void process(JSONObject J) {
        String name = Main.getUserName(J.getLong("group_id"), J.getLong("user_id"));
        name = name + " (***" + String.format("%03d", J.getLong("user_id") % 1000) + ")";
        if (J.getString("notice_type").equals("group_decrease")) {
            if (Objects.equals(J.getLong("operator_id"), J.getLong("user_id"))) {
                Main.setNextSender("group", 0, J.getLong("group_id"), name + "离开了我们……");
                Main.setNextLog(J.getLong("user_id") + " leave group " + J.getLong("group_id"), 0);
            } else {
                Main.setNextSender("group", 0, J.getLong("group_id"), name + "被" + Main.getUserName(J.getLong("group_id"), J.getLong("operator_id")) + "送走了");
                Main.setNextLog(J.getLong("user_id") + " leave group " + J.getLong("group_id") + " op: " + J.getLong("operator_id"), 0);
            }
        }
        if (J.getString("notice_type").equals("group_increase")) {
            Main.setNextSender("group", 0, J.getLong("group_id"), "欢迎 " + name + " 的加入");
            Main.setNextLog(J.getLong("user_id") + " get in group " + J.getLong("group_id"), 0);
            Main.userName.update(J.getLong("group_id"));
        }
    }

    @Override
    public boolean check(JSONObject J) {
        if (!J.getString("post_type").equals("notice")) return false;
        return J.getString("notice_type").equals("group_decrease") || J.getString("notice_type").equals("group_increase");
    }
}
