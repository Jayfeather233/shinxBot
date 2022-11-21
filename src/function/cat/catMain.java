package function.cat;

import interfaces.Processable;
import main.Main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class catMain implements Processable {

    public catMain(){
        catColor.initColor();
    }

    Map<Long,singleCat> userCat = new HashMap<>();
    Set<Long> naming = new HashSet<>();
    @Override
    public void process(String message_type, String message, long group_id, long user_id, int message_id) {
        if(message.equals("get")){
            if(userCat.containsKey(user_id)){
                Main.setNextSender(message_type,user_id,group_id,"Already has one.");
            }else{
                Main.setNextSender(message_type,user_id,group_id,"name?");
                naming.add(user_id);
            }
        }else if(message.equals("look")){
            if(!userCat.containsKey(user_id)){
                Main.setNextSender(message_type,user_id,group_id,"No cat.");
            }else{
                Main.setNextSender(message_type,user_id,group_id,userCat.get(user_id).toString());
            }
        }else{
            userCat.put(user_id,new singleCat(message));
        }
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return user_id==1826559889 && ( message.equals("get") || message.equals("look") || naming.contains(user_id));
    }

    @Override
    public String help() {
        return null;
    }
}
