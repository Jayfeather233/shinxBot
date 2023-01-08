package function.OCR;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import interfaces.Processable;
import main.Main;

import java.util.Objects;

public class OCRMain implements Processable {
    @Override
    public void process(String message_type, String message, long group_id, long user_id, int message_id) {
        int index = message.indexOf("[CQ:image,file=");
        index += 15;
        int index2 = index;
        while(message.charAt(index2)!=','){
            ++index2;
        }
        JSONObject J = new JSONObject();
        J.put("image",message.substring(index,index2));
        StringBuffer sb = Main.setNextSender("ocr_image",J);
        JSONArray Ja = JSONObject.parseObject(Objects.requireNonNull(sb).toString()).getJSONObject("data").getJSONArray("texts");
        sb = new StringBuffer();
        for(JSONObject j : Ja.toJavaList(JSONObject.class)){
            sb.append(j.getString("text")).append(' ');
        }
        Main.setNextSender(message_type,user_id,group_id,sb.toString());
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return message.startsWith(".ocr") || message.startsWith(".OCR");
    }

    @Override
    public String help() {
        return "图片OCR: .ocr + 图片";
    }
}
