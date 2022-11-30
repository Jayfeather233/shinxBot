package function.cat;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import interfaces.Processable;
import main.Main;

import java.io.*;
import java.util.*;

public class catMain implements Processable {

    public catMain(){
        catColor.initColor();
        positionText.initText();

        try {
            File ff = new File("catData.json");
            if (!ff.exists()) {
                if (!ff.createNewFile()) System.out.println("catData创建失败");
                else {
                    FileWriter fw = new FileWriter(ff);
                    fw.write("{\"data\":[]}");
                    fw.close();
                }
            }
            FileReader f = new FileReader("catData.json");
            Scanner S = new Scanner(f);
            StringBuilder sb = new StringBuilder();
            while (S.hasNext()) {
                sb.append(S.nextLine()).append(' ');
            }

            JSONArray ja = JSONObject.parseObject(sb.toString()).getJSONArray("data");
            for(JSONObject u : ja.toJavaList(JSONObject.class)){
                userCat.put(u.getLong("user"),new singleCat(u.getJSONObject("data")));
            }

            S.close();
            f.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Map<Long,singleCat> userCat = new HashMap<>();
    Set<Long> naming = new HashSet<>();

    public synchronized void catSave(){
        JSONArray ja = new JSONArray();
        for(long u : userCat.keySet()){
            JSONObject Jt = new JSONObject();
            Jt.put("data", userCat.get(u).toJSONObject());
            Jt.put("user",u);
            ja.add(Jt);
        }
        JSONObject J = new JSONObject();
        J.put("data",ja);

        try{
            FileWriter fw = new FileWriter("catData.json", false);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(J.toString());
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Cannot save cat data.");
        }
    }

    @Override
    public void process(String message_type, String message, long group_id, long user_id, int message_id) {
        if(message.equals("get")){
            if(userCat.containsKey(user_id)){
                Main.setNextSender(message_type,user_id,group_id,"Already has one.");
            }else{
                Main.setNextSender(message_type,user_id,group_id,"name?");
                naming.add(user_id);
            }
        }else if(message.startsWith("look")){
            if(!userCat.containsKey(user_id)){
                Main.setNextSender(message_type,user_id,group_id,"No cat.");
            }else{
                positionName u;
                try {
                    u = positionName.valueOf(message.substring(4).trim());
                } catch(IllegalArgumentException e){
                    u =positionName.values()[singleCat.R.nextInt(positionName.values().length)];
                }
                Main.setNextSender(message_type, user_id, group_id, userCat.get(user_id).visit(u));
            }
            catSave();
        }else if(naming.contains(user_id)){
            userCat.put(user_id,new singleCat(message));
            naming.remove(user_id);
            Main.setNextSender(message_type,user_id,group_id,"OK.");
            catSave();
        }
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return user_id==1826559889 && ( message.equals("get") || message.startsWith("look") || naming.contains(user_id));
    }

    @Override
    public String help() {
        return null;
    }
}
