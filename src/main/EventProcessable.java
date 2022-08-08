package main;

import com.alibaba.fastjson.JSONObject;

public interface EventProcessable {
    void process(JSONObject J);
    boolean check(JSONObject J);
}
