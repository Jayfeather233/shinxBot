package function.auto114514;

import main.Main;
import main.Processable;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

class info {
    int num;
    String ans;

    public info(int a, String b) {
        num = a;
        ans = b;
    }
}

public class Auto114514Main implements Processable {
    final String __1 = "11-4-5+1-4";
    ArrayList<info> ai = new ArrayList<>();

    public Auto114514Main() {
        try {
            FileReader f = new FileReader("homodata.txt");
            Scanner S = new Scanner(f);
            while (S.hasNext()) {
                ai.add(new info(S.nextInt(), S.next()));
            }
        } catch (FileNotFoundException e) {
            System.out.println("似乎缺失了文件 homodata.txt");
        }
    }

    @Override
    public void process(String message_type, String message, long group_id, long user_id, int message_id) {
        try {
            long num = Long.parseLong(message.substring(4).trim());
            if (num == 114514) {
                Main.setNextSender(message_type, user_id, group_id, "这么臭的数字有必要论证吗（恼）");
                return;
            }
            Main.setNextSender(message_type, user_id, group_id, "" + num + " = " + getAns(num));
            Main.setNextLog("Auto114514 at group " + group_id + " by "+user_id,0);
        } catch (NumberFormatException e) {
            Main.setNextSender(message_type, user_id, group_id, "需要一个数字，这事数字吗（恼）");
        }
    }

    private StringBuilder getAns(long num) {
        StringBuilder re = new StringBuilder();
        if (num < 0) {
            return re.append("(").append(__1).append(")").append(getAns((-1) * num));
        }
        info x = getMinNum(num);
        assert x != null;
        if (x.num == num) return new StringBuilder(x.ans);
        if (num / x.num == 1) re.append(x.ans);
        else re.append("(").append(x.ans).append(")*(").append(getAns(num / x.num)).append(")");

        if (num % x.num != 0) {
            re.append("+(").append(getAns(num % x.num)).append(")");
        }
        return re;
    }

    private info getMinNum(long u) {
        for (info s : ai) {
            if (s.num <= u) return s;
        }
        return null;
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return message.startsWith("恶臭数字");
    }

    @Override
    public String help() {
        return "恶臭数字生成器： 恶臭数字+[Number]";
    }
}
