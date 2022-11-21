package main;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import event.friendadd.friendAddMain;
import event.groupmemberchange.MemberChangeMain;
import event.poke.pokeMain;
import function.auto114514.Auto114514Main;
import function.autoForwardGenerator.AutoForwardGeneratorMain;
import function.autoreply.AutoReplyMain;
import function.cat.catMain;
import function.compiler.compilerMain;
import function.deliver.DeliverMain;
import function.fudu.fuduMain;
import function.getImage621.GetImage621Main;
import function.getimage2d.GetImage2DMain;
import function.guess.GuessGameMain;
import function.imageGenerator.ImageGeneratorMain;
import function.mysd.sdMain;
import function.nbnhhsh.HhshMain;
import function.nonogram.generateNonogram;
import function.nonogram.nonogram;
import function.randomColor.randomColorMain;
import function.uno.UNOMain;
import httpconnect.HttpURLConnectionUtil;
import interfaces.EventProcessable;
import interfaces.Processable;
import utils.userNameGetter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


public class Main {
    private static final Set<Long> friendSet = new HashSet<>();
    private static final ArrayList<interfaces.Processable> features = new ArrayList<>();
    private static final ArrayList<interfaces.EventProcessable> events = new ArrayList<>();
    private static final SimpleDateFormat logFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String[] logLevel = {"INFO", "WARNING", "ERROR"};
    public static userNameGetter userName;
    public static int sendPort;
    public static int receivePort;
    public static long botQQ;
    public static String localPath;

    public static Set<Long> getFriendSet() {
        return friendSet;
    }


    public static void setNextOutput(String input) {//收到传来的EVENT的JSON数据处理
        JSONObject J_input = JSONObject.parseObject(input);
        String post_type = J_input.getString("post_type");
        String uName = null;

        if (post_type.equals("request") || post_type.equals("notice")) {
            for (EventProcessable eve : events) {
                if (eve.check(J_input)) {
                    eve.process(J_input);
                }
            }
        } else if (post_type.equals("message")) {
            String message = J_input.getString("message");
            int message_id = J_input.getInteger("message_id");
            String message_type = J_input.containsKey("message_type") ? J_input.getString("message_type") : null;
            if (message == null || message_type == null) return;
            if (!message_type.equals("private") && !message_type.equals("group")) return;
            long user_id = 0, group_id = -1;
            if (J_input.containsKey("user_id")) {
                user_id = J_input.getLong("user_id");
            }
            if (J_input.containsKey("group_id")) {
                group_id = J_input.getLong("group_id");
            }

            if (message.equals("bot.cntest")) {
                if (message_type.equals("group")) {
                    JSONObject J = new JSONObject();
                    J.put("group_id", group_id);
                    J.put("message", "中文测试");
                    setNextSender("send_group_msg", J);
                }
            } else if (message.equals("bot.help")) {
                StringBuilder sb = new StringBuilder();
                for (Processable game : features) {
                    if (game.help() != null)
                        sb.append(game.help()).append('\n');
                }
                sb.append("本Bot项目地址：https://github.com/Jayfeather233/shinxBot");
                Main.setNextSender(message_type, user_id, group_id, String.valueOf(sb));
            } else {
                for (Processable game : features) {
                    if (game.check(message_type, message, group_id, user_id)) {
                        game.process(message_type, message, group_id, user_id, message_id);
                    }
                }
            }
        }
    }

    public synchronized static StringBuffer setNextSender(String msg_type, JSONObject msg) {
        try {
            Thread.sleep(50);//延时。在电脑QQ消息间隔过快收不到
            return HttpURLConnectionUtil.doPost("http://127.0.0.1:" + sendPort + "/" + msg_type, msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized static StringBuffer setNextSender(String msg_type, long user_id, long group_id, String msg) {
        JSONObject J = new JSONObject();
        J.put("message", msg);
        J.put("message_type", msg_type);
        J.put("group_id", group_id);
        J.put("user_id", user_id);
        return setNextSender("send_msg", J);
    }

    /*
    level:
        0-info
        1-warning
        2-error
     */
    public synchronized static void setNextLog(String log, int level) {
        System.out.println("[" + logFormatter.format(new Date()) + "] [" + logLevel[level] + "]: " + log);
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        localPath = new File("").getCanonicalPath();

        System.setProperty("java.net.useSystemProxies", "true");
        features.add(new UNOMain());
        features.add(new AutoForwardGeneratorMain());
        features.add(new DeliverMain());
        features.add(new GuessGameMain());
        features.add(new ImageGeneratorMain());
        features.add(new GetImage621Main());
        features.add(new Auto114514Main());
        features.add(new GetImage2DMain());
        features.add(new AutoReplyMain());
        features.add(new compilerMain());
        features.add(new nonogram());
        features.add(new generateNonogram());
        features.add(new randomColorMain());
        features.add(new HhshMain());
        features.add(new sdMain());
        features.add(new fuduMain());
        features.add(new catMain());

        events.add(new friendAddMain());
        events.add(new MemberChangeMain());
        events.add(new pokeMain());

        File f = new File("./port.txt");
        if (!f.exists()) {
            Scanner S = new Scanner(System.in);
            System.out.println("Please input the send_port: ");
            sendPort = S.nextInt();
            System.out.println("Please input the receive_port: ");
            receivePort = S.nextInt();

            FileOutputStream fops = new FileOutputStream(f);
            fops.write(String.valueOf(sendPort).getBytes());
            fops.write(' ');
            fops.write(String.valueOf(receivePort).getBytes());
            fops.close();
            S.close();
            System.out.println("Now you should restart me. Quiting in 5 seconds.");
            Thread.sleep(5000);
            return;
        } else {
            Scanner S = new Scanner(f);
            sendPort = S.nextInt();
            receivePort = S.nextInt();
            S.close();
        }

        Go_Listener Listen = new Go_Listener();
        Thread R1 = new Thread(Listen);
        R1.start();
        JSONObject J_input;

        boolean flg = true;
        while (flg) {
            flg = false;
            try {
                J_input = JSONObject.parseObject(Objects.requireNonNull(setNextSender("get_login_info", null)).toString());
                botQQ = J_input.getJSONObject("data").getLong("user_id");
                break;
            } catch (NullPointerException e) {
                flg = true;
            }
            Thread.sleep(10000);
        }
        System.out.println("QQ:" + botQQ);
        J_input = JSONObject.parseObject(Objects.requireNonNull(setNextSender("get_friend_list", null)).toString());
        JSONArray JA = J_input.getJSONArray("data");
        for (Object o : JA) {
            friendSet.add(((JSONObject) o).getLong("user_id"));
        }
        userName = new userNameGetter();
        new Thread(new InputProcess()).start();
    }

    public static String getUserName(long group_id, long user_id) {
        try {
            return userName.getName(group_id, user_id);
        } catch (NoSuchElementException e) {
            JSONObject J = new JSONObject();
            J.put("user_id", user_id);
            J = JSONObject.parseObject(Objects.requireNonNull(setNextSender("get_stranger_info", J)).toString());
            J = J.getJSONObject("data");

            String uName;
            if (J.containsKey("card") && !J.getString("card").equals("")) {
                uName = J.getString("card");
            } else if (J.containsKey("nickname")) {
                uName = J.getString("nickname");
            } else uName = J.getString("user_id");
            return uName;
        }
    }
}
