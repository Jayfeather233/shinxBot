package function.guess;

import main.Main;
import interfaces.Processable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class GuessGameInfo {
    public int state;
    public long stdAnswer;

    public GuessGameInfo(int state, long stdAnswer) {
        this.state = state;
        this.stdAnswer = stdAnswer;
    }
}

public class GuessGameMain implements Processable {

    final Map<Long, GuessGameInfo> playerMap = new HashMap<>();

    @Override
    public void process(String message_type, String message, long group_id, long user_id, int message_id) {
        if (message.equals("猜数")) {
            if (playerMap.containsKey(user_id)) {
                sendMsg(message_type, user_id, group_id, "请先完成现有游戏或用 结束 来结束");
            } else {
                sendMsg(message_type, user_id, group_id, "请选择：\n1. 猜数游戏\n2. 困难猜数游戏\n3. 规则介绍");
                playerMap.put(user_id, new GuessGameInfo(0, 0));
            }
        } else if (message.equals("结束")) {
            if (playerMap.containsKey(user_id)) {
                playerMap.remove(user_id);
                sendMsg(message_type, user_id, group_id, "结束");
            }
        } else {
            long type = toInt(message);
            if (type == -1) sendMsg(message_type, user_id, group_id, "数字太大");
            GuessGameInfo u = playerMap.get(user_id);
            switch (u.state) {
                case 0 -> {
                    switch ((int) type) {
                        case 1 -> {
                            u.state = 1;
                            sendMsg(message_type, user_id, group_id, "请设置上限：");
                        }
                        case 2 -> {
                            u.state = 10;
                            sendMsg(message_type, user_id, group_id, "请设置上限：");
                        }
                        case 3 ->
                                sendMsg(message_type, user_id, group_id, "猜数游戏：每猜一个数，机器人会告诉你比标准答案大还是小，在与标准答案相差10以内会说接近。\n困难猜数游戏：只会告诉你与标准答案相差的百分比区间。");
                    }
                }
                case 1, 10 -> {
                    ++u.state;
                    u.stdAnswer = new Random().nextInt((int) type) + 1;
                    sendMsg(message_type, user_id, group_id, "开始游戏");
                }
                case 2 -> {
                    if (type == u.stdAnswer) {
                        sendMsg(message_type, user_id, group_id, "猜对了！");
                        playerMap.remove(user_id);
                    } else if (Math.abs(type - u.stdAnswer) <= 10) {
                        sendMsg(message_type, user_id, group_id, "很接近");
                    } else if (type < u.stdAnswer) {
                        sendMsg(message_type, user_id, group_id, "较小");
                    } else sendMsg(message_type, user_id, group_id, "较大");
                }
                case 11 -> {
                    if (type == u.stdAnswer) {
                        sendMsg(message_type, user_id, group_id, "猜对了！");
                        playerMap.remove(user_id);
                    } else {
                        double percentage = Math.abs(type - u.stdAnswer) * 1.0 / u.stdAnswer;
                        if (percentage >= 1.0) sendMsg(message_type, user_id, group_id, "相对于答案相差大于100%");
                        else if (percentage >= 0.75) sendMsg(message_type, user_id, group_id, "相对于答案相差大于75%");
                        else if (percentage >= 0.50) sendMsg(message_type, user_id, group_id, "相对于答案相差大于50%");
                        else if (percentage >= 0.25) sendMsg(message_type, user_id, group_id, "相对于答案相差大于25%");
                        else if (percentage >= 0.10) sendMsg(message_type, user_id, group_id, "相对于答案相差大于10%");
                        else sendMsg(message_type, user_id, group_id, "相对于答案相差小于10%");
                    }
                }
            }
            playerMap.replace(user_id, u);
        }
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return message.equals("猜数") || message.equals("结束") || (playerMap.containsKey(user_id) && isNumber(message));
    }

    @Override
    public String help() {
        return "猜数游戏： （没啥意思别玩了）";
    }

    private boolean isNumber(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ' ') continue;
            if (s.charAt(i) < '0' || '9' < s.charAt(i)) return false;
        }
        return true;
    }

    private long toInt(String s) {
        if (s.length() > 9) return -1;
        long u = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ' ') continue;
            u = u * 10 + s.charAt(i) - '0';
        }
        return u;
    }

    private void sendMsg(String msg_type, long user_id, long group_id, String msg) {
        if (msg_type.equals("group")) {
            msg = "[CQ:at,qq=" + user_id + "] " + msg;
        }
        Main.setNextSender(msg_type, user_id, group_id, msg);
    }
}