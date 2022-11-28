package function.cat;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

enum positionName {
    livingRoom, diningRoom, toilet, bedroom, bathroom, balcony, kitchen, garden
}

public class positionText {
    private static JSONObject text;

    static void initText() {
        try {
            File ff = new File("catText.json");
            if (!ff.exists()) {
                if (!ff.createNewFile()) System.out.println("catText创建失败");
                else {
                    FileWriter fw = new FileWriter(ff);
                    JSONObject J = new JSONObject();
                    JSONArray emp = new JSONArray();
                    for (positionName u : positionName.values()) {
                        JSONObject jo = new JSONObject();
                        jo.put("hungry", emp);
                        jo.put("thirsty", emp);
                        jo.put("others", emp);
                        J.put(u.toString(), jo);
                    }
                    J.put("others", emp);
                    fw.write(J.toString());
                    fw.close();
                }
            }
            FileReader f = new FileReader("catText.json");
            Scanner S = new Scanner(f);
            StringBuilder sb = new StringBuilder();
            while (S.hasNext()) {
                sb.append(S.nextLine()).append(' ');
            }

            text = JSONObject.parseObject(String.valueOf(sb));

            S.close();
            f.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String getText(singleCat theCat, positionName vis) {
        JSONArray ja;
        if(vis != theCat.stat.pos.name){
            ja = text.getJSONArray("others");
        }else{
            if(theCat.stat.hungry<=0){
                 ja = text.getJSONObject(vis.toString()).getJSONArray("hungry");
            }else if(theCat.stat.thirsty<=0){
                ja = text.getJSONObject(vis.toString()).getJSONArray("thirsty");
            }else{
                ja = text.getJSONObject(vis.toString()).getJSONArray("others");
            }
        }
        String ans = ja.getString(singleCat.R.nextInt(ja.size()));
        ans = ans.replace("%gender%", theCat.gender ? "he" : "she");
        ans = ans.replace("%Gender%", theCat.gender ? "He" : "She");
        ans = ans.replace("%name%", theCat.name);
        return ans;
    }
}
