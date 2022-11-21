package utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import static main.Main.setNextSender;

public class userNameGetter {
    Map<Long, Map<Long, String>> userNameMap = new HashMap<>();

    public userNameGetter() {
        JSONObject J = JSONObject.parseObject(Objects.requireNonNull(setNextSender("get_group_list", null)).toString());
        JSONArray Ja = J.getJSONArray("data");
        J = new JSONObject();
        for (JSONObject u : Ja.toJavaList(JSONObject.class)) {
            update(u.getLong("group_id"));
        }
    }

    public String getName(long group_id, long user_id) throws NoSuchElementException {
        if(!userNameMap.containsKey(group_id)) throw new NoSuchElementException("No such group");
        if(!userNameMap.get(group_id).containsKey(user_id)) throw new NoSuchElementException("No such user");
        return userNameMap.get(group_id).get(user_id);
    }

    public void update(long group_id){
        userNameMap.put(group_id, new HashMap<>());
        JSONObject J = new JSONObject();
        J.put("group_id", group_id);
        JSONObject J2 = JSONObject.parseObject(Objects.requireNonNull(setNextSender("get_group_member_list", J)).toString());
        JSONArray Ja2 = J2.getJSONArray("data");
        for (JSONObject v : Ja2.toJavaList(JSONObject.class)) {
            userNameMap.get(group_id).put(v.getLong("user_id"),v.getString("nickname"));
        }
    }
}
