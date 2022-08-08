package function.uno;

import main.Processable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static main.Main.getFriendSet;
import static main.Main.setNextSender;

public class UNOMain implements Processable {
    private static final Map<Long, UNOGame> unoGameMap = new HashMap<>();
    private static final Map<Long, Long> unoIDMap = new HashMap<>();

    public static Map<Long, UNOGame> getUnoGameMap() {
        return unoGameMap;
    }

    public static boolean UNOJoin(long userID, long gameID) {
        if (unoIDMap.containsKey(userID)) return false;
        unoIDMap.put(userID, gameID);
        return true;
    }

    public static void UNOLeave(long userID) {
        unoIDMap.remove(userID);
    }

    public void process(String message_type, String message, long group_id, long user_id) {
        message = message.toLowerCase();
        if (!message.startsWith("uno.") && unoIDMap.containsKey(user_id)) {
            message = "uno.play " + message;
        }
        if (message.startsWith("uno.")) message = message.substring(3);
        if (message.equals(".help")) {
            setNextSender(message_type, user_id, group_id, "uno.new 创建一个群UNO游戏\nuno.join 加入当前群游戏\nuno.leave 离开\nuno.start 开始\nuno.order 出牌顺序\nuno.resend 重新发送消息\nuno.help 帮助");
            return;
        }
        Set<Long> friendSet = getFriendSet();
        if (message_type.equals("group")) {
            switch (message) {
                case ".new":
                    if (!unoGameMap.containsKey(group_id)) {
                        unoGameMap.put(group_id, new UNOGame(group_id));
                    } else {
                        setNextSender(message_type, user_id, group_id, "本群已存在一个进行中的游戏，使用uno.join来加入");
                    }
                    break;
                case ".join":
                    if (!unoGameMap.containsKey(group_id)) {
                        setNextSender(message_type, user_id, group_id, "本群不存在一个进行中的游戏，使用uno.new来新建一个");
                    } else {
                        if (friendSet.contains(user_id)) unoGameMap.get(group_id).join(user_id);
                        else {
                            setNextSender(message_type, user_id, group_id, "[CQ:at,qq=" + user_id + "] 请先添加bot好友");
                        }
                    }
                    break;
                case ".start":
                    if (!unoGameMap.containsKey(group_id)) {
                        setNextSender(message_type, user_id, group_id, "本群不存在一个准备中的游戏，使用uno.new来新建一个");
                    } else {
                        if (!unoGameMap.get(group_id).isBegin()) new Thread(unoGameMap.get(group_id)).start();
                        else {
                            setNextSender(message_type, user_id, group_id, "本群已存在一个进行中的游戏，使用uno.join来加入");
                        }
                    }
                    break;
                default:
                    if (unoGameMap.containsKey(group_id))
                        unoGameMap.get(group_id).setNextInput(user_id, message);
                    break;
            }
        } else if (unoIDMap.containsKey(user_id)) {
            unoGameMap.get(unoIDMap.get(user_id)).setNextInput(user_id, message);
        }
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        message = message.toLowerCase();
        return message.startsWith("uno.") || (message_type.equals("private") && unoIDMap.containsKey(user_id));
    }

    @Override
    public String help() {
        return "群内UNO： 详情 uno.help";
    }
}
