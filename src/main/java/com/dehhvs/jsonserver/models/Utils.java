package com.dehhvs.jsonserver.models;

import java.text.Normalizer;
import java.util.Map;

import org.json.JSONObject;

public class Utils {
    public static String stripAccents(String s) {
        String ss = Normalizer.normalize(s, Normalizer.Form.NFD);
        ss = ss.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return ss;
    }

    public static JSONObject typing(Map<String, Object> reqParam) {
        JSONObject element = new JSONObject();
        for (String key : reqParam.keySet()) {
            Object value;
            try {
                value = Integer.parseInt((String) reqParam.get(key));
            } catch (Exception e) {
                try {
                    value = Float.parseFloat((String) reqParam.get(key));
                } catch (Exception er) {
                    if (reqParam.get(key).equals("true") || reqParam.get(key).equals("false")) {
                        value = Boolean.parseBoolean((String) reqParam.get(key));
                    } else {
                        value = String.valueOf(reqParam.get(key));
                    }
                }
            }
            element.put(key, value);
        }
        return element;
    }
}
