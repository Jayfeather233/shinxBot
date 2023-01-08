package function.winmine;

import interfaces.Processable;
import main.Main;

import java.util.HashMap;
import java.util.Map;

public class MineMain implements Processable {
    private final Map<Long, Mine> group = new HashMap<>();
    private final Map<Long, Mine> user = new HashMap<>();

    @Override
    public void process(String message_type, String message, long group_id, long user_id, int message_id) {
        if (message.trim().startsWith("来局扫雷")) {
            int diff;
            try {
                diff = Integer.parseInt(message.substring(4).trim());
            } catch (NumberFormatException e) {
                diff = 0;
            }
            if (group.containsKey(group_id)) {
                Main.setNextSender(message_type, user_id, group_id, "有一局游戏正在进行。继续或使用mine.end");
            } else {
                Main.setNextSender(message_type, user_id, group_id, "输入mine [x] [y]来玩：");
                if (message_type.equals("group")) {
                    group.put(group_id, new Mine(diff));
                    Main.setNextSender(message_type, user_id, group_id, group.get(group_id).toString());
                } else {
                    user.put(user_id, new Mine(diff));
                    Main.setNextSender(message_type, user_id, group_id, user.get(user_id).toString());
                }
            }
        } else if (message.startsWith("mine")) {
            message = message.substring(4).trim();
            if (message.equals(".end")) {
                if (message_type.equals("group")) {
                    group.remove(group_id);
                } else {
                    user.remove(user_id);
                }
                Main.setNextSender(message_type, user_id, group_id, "已删除");
            } else {
                message = message.replace("  ", " ");
                String[] des = message.split(" ");
                int col = Integer.parseInt(des[0]);
                int row = Integer.parseInt(des[1]);
                Mine game;
                if (message_type.equals("group")) {
                    game = group.get(group_id);
                } else {
                    game = user.get(user_id);
                }
                state u = game.play(col, row);
                if (u == state.CONTINUE) {
                    Main.setNextSender(message_type, user_id, group_id, "[CQ:face,id=114]");
                } else if (u == state.WIN || u == state.LOSE) {
                    if (u == state.WIN) {
                        Main.setNextSender(message_type, user_id, group_id, "[CQ:face,id=4]");
                    } else {
                        Main.setNextSender(message_type, user_id, group_id, "[CQ:face,id=36]");
                    }
                    if (message_type.equals("group")) {
                        group.remove(group_id);
                    } else {
                        user.remove(user_id);
                    }
                } else if (u == state.OUT_OF_BOUNDARY) {
                    Main.setNextSender(message_type, user_id, group_id, "出界");
                }
                Main.setNextSender(message_type, user_id, group_id, game.toString());
            }
        }
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        message = message.toLowerCase();
        return message.startsWith("来局扫雷") || message.startsWith("mine");
    }

    @Override
    public String help() {
        return "扫雷：来局扫雷 （群内共享）";
    }
}
