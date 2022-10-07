package function.fudu;

import interfaces.Processable;
import main.Main;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static main.Main.botQQ;

public class fuduMain implements Processable {
    Map<Long, String> msg = new HashMap<>();
    Map<Long, Integer> times = new HashMap<>();
    @Override
    public void process(String message_type, String message, long group_id, long user_id, int message_id) {
        if(Objects.equals(msg.get(group_id),message)){
            times.put(group_id,times.get(group_id)+1);
            if(times.get(group_id)==5){
                Main.setNextSender(message_type,user_id,group_id,msg.get(group_id));
                times.put(group_id,0);
            }
        } else {
            msg.put(group_id,message);
            times.put(group_id,1);
        }
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return message_type.equals("group") && user_id != botQQ;
    }

    @Override
    public String help() {
        return null;
    }
}
