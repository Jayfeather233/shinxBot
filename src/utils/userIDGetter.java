package utils;

public class userIDGetter {
    public static long getID(String u){
        u = u.trim();
        if (u.startsWith("[CQ:at,qq=")) {
            u = u.substring(10);
            int i = 0;
            while (i != u.length() && u.charAt(i) != ']') i++;
            u = u.substring(0, i);
        }
        return Long.parseLong(u.trim());
    }
}
