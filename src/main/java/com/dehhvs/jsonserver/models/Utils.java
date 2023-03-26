package com.dehhvs.jsonserver.models;

import java.text.Normalizer;

public class Utils {
    public static String stripAccents(String s) {
        String ss = Normalizer.normalize(s, Normalizer.Form.NFD);
        ss = ss.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return ss;
    }
}
