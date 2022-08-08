package function.deliver;

import main.Processable;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import static main.Main.setNextSender;

class DeliverItemInfo {
    String name;
    int possibility;
    int color; //0:green 1:yellow 2:purple
}

public class DeliverMain implements Processable {
    private final DeliverItemInfo[] diiArray = new DeliverItemInfo[100];
    private final Random R = new Random();
    private final String[][] times = {{"零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"}, {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}};
    private int totalPoss = 0;

    public DeliverMain() {
        try {
            FileReader f = new FileReader("DeliverPossibility.txt");
            Scanner S = new Scanner(f);
            int n = S.nextInt();
            for (int i = 0; i < n; i++) {
                diiArray[i] = new DeliverItemInfo();
                diiArray[i].name = S.next();
                diiArray[i].possibility = S.nextInt();
                diiArray[i].color = S.nextInt();
                totalPoss += diiArray[i].possibility;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DeliverItemInfo getRandomDeliverItem() {
        int u = R.nextInt(totalPoss);
        for (DeliverItemInfo deliverU : diiArray) {
            if (u <= deliverU.possibility) {
                return deliverU;
            }
            u -= deliverU.possibility;
        }
        return null;
    }

    private ArrayList<DeliverItemInfo> getDeliverItem(int t) {
        ArrayList<DeliverItemInfo> re = new ArrayList<>();
        for (int i = 0; i < t; i++) re.add(getRandomDeliverItem());
        return re;
    }

    public void process(String message_type, String message, long group_id, long user_id) {
        int t;
        if (message.contains(times[0][10]) || message.contains(times[1][10])) t = 11;
        else {
            t = 0;
            for (int i = 1; i < 10; i++) {
                if (message.contains(times[0][i]) || message.contains(times[1][i])) {
                    t = i;
                    break;
                }
            }
        }
        if (t == 0) return;

        ArrayList<DeliverItemInfo> deliverItemArray = getDeliverItem(t);

        StringBuilder output = new StringBuilder("[CQ:at,qq=" + user_id + "] 大嘴鸥回来啦\n");
        for (DeliverItemInfo u : deliverItemArray) {
            switch (u.color) {
                case 0 -> output.append("绿色 ");
                case 1 -> output.append("黄色 ");
                case 2 -> output.append("紫色 ");
            }
            output.append(u.name).append('\n');
        }
        setNextSender(message_type, user_id, group_id, output.substring(0, output.length() - 1));
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return message_type.equals("group") && (message.startsWith("外送") || message.endsWith("外送"));
    }

    @Override
    public String help() {
        return null;
    }
}
