package main;

import com.alibaba.fastjson.JSONObject;

import java.util.Objects;
import java.util.Scanner;

import static main.Main.setNextSender;

public class InputProcess implements Runnable {
    public void run() {
        Scanner S = new Scanner(System.in);
        String comma;
        StringBuilder mess;
        while (true) {
            comma = S.next();
            mess = new StringBuilder(S.next());
            switch (comma) {
                case "image": {
                    JSONObject J = new JSONObject();
                    J.put("file", mess);
                    System.out.println(setNextSender("get_image", J));
                }break;
                case "img": {
                    JSONObject J = new JSONObject();
                    J.put("file", mess);
                    System.out.println(setNextSender("get_image", J));
                }break;
                case "forward": {
                    JSONObject J = new JSONObject();
                    J.put("message_id", mess);
                    System.out.println(J);
                    System.out.println(setNextSender("get_forward_msg", J));
                }break;
                case "forw": {
                    JSONObject J = new JSONObject();
                    J.put("message_id", mess);
                    System.out.println(J);
                    System.out.println(setNextSender("get_forward_msg", J));
                }break;
                case "f": {
                    String s1, s2;
                    JSONObject J = new JSONObject();
                    do {
                        s1 = S.next();
                        s2 = S.next();
                        s2 = s2 + S.nextLine();
                        if (s1.equals("end")) break;
                        J.put(s1, s2);
                    } while (true);
                    System.out.println(Objects.requireNonNull(setNextSender(mess.toString(), J)));
                }break;
                default : System.out.println("unsupported");
            }
        }
    }
}
