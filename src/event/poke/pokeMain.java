package event.poke;

import com.alibaba.fastjson.JSONObject;
import main.EventProcessable;
import main.Main;

import java.util.Random;

public class pokeMain implements EventProcessable {
    Random R = new Random();

    @Override
    public void process(JSONObject J) {
        if (J.containsKey("group_id")) {
            Main.setNextSender("group", 0, J.getLong("group_id"), "[CQ:poke,qq=" + J.getLong("user_id") + "]");
            if (R.nextInt(3) == 1)
                Main.setNextSender("group", 0, J.getLong("group_id"), "别戳我TAT");
        } else {
            Main.setNextSender("private", J.getLong("sender_id"), 0, "[CQ:poke,qq=" + J.getLong("user_id") + "]");
            if (R.nextInt(3) == 1)
                Main.setNextSender("private", J.getLong("sender_id"), 0, "别戳我TAT");
        }
    }

    @Override
    public boolean check(JSONObject J) {
        if (!J.containsKey("sub_type") || !J.getString("sub_type").equals("poke")) return false;
        long target_id = J.getLong("target_id");
        long user_id = J.getLong("user_id");
        if (target_id != Main.botQQ || user_id == 1783241911 || user_id == 1318920100) return false;
        return true;
    }
}