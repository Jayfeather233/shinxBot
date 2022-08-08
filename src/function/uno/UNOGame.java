package function.uno;

import main.Main;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.List;

public class UNOGame implements Runnable {
    private static final int CARDS_LENGTH = 108;
    public final long gameID;
    final String[] color;
    final String[] name;
    boolean hasNextInput = false;
    boolean isEnd = false;
    boolean colorChoosing = false;
    boolean needOutput = true;
    boolean isDrawn = false;
    String nextInput;
    long nextID;
    private final List<Long> playerID;
    private int[] cards;
    private int top;
    private int order;
    private final List<List<Integer>> playerCards;
    private boolean isBegin = false;
    private int lastCard;
    private int nextColor = -1;
    private int needDraw;
    private int nowPlayer;

    public UNOGame(long gameID) {
        this.gameID = gameID;
        playerID = new ArrayList<>();
        playerCards = new ArrayList<>();
        color = new String[5];
        color[0] = "红";
        color[1] = "黄";
        color[2] = "蓝";
        color[3] = "绿";
        color[4] = "黑";
        name = new String[14];
        name[0] = "0  ";
        name[1] = "1  ";
        name[2] = "2  ";
        name[3] = "3  ";
        name[4] = "4  ";
        name[5] = "5  ";
        name[6] = "6  ";
        name[7] = "7  ";
        name[8] = "8  ";
        name[9] = "9  ";
        name[10] = "翻转";
        name[11] = "禁止";
        name[12] = "+2  ";
        name[13] = "改色";

        sendGroupMsg("UNO游戏创建成功");
    }

    private void shuffle() {
        Random r = new Random();
        top = 0;
        cards = new int[CARDS_LENGTH];
        int ls = 0;
        for (int i = 0; i < 4; i++) {
            cards[ls++] = i * 14;
            for (int j = 1; j <= 9; j++) {
                cards[ls++] = cards[ls++] = i * 14 + j;
            }
            cards[ls++] = cards[ls++] = i * 14 + 10;//res
            cards[ls++] = cards[ls++] = i * 14 + 11;//ban
            cards[ls++] = cards[ls++] = i * 14 + 12;//+2
        }
        for (int i = 0; i < 4; i++) cards[ls++] = 4 * 14;//change
        for (int i = 0; i < 4; i++) cards[ls++] = 4 * 14 + 1; //+4

        for (int i = 0; i < ls; i++) {
            int t = cards[i], u = r.nextInt(ls);
            cards[i] = cards[u];
            cards[u] = t;
        }
    }

    private void draw(int pos, int num) {
        if (pos == -1) return;
        while (num != 0) {
            --num;
            playerCards.get(pos).add(cards[top]);
            ++top;
            if (top == CARDS_LENGTH) {
                shuffle();
            }
        }
    }

    private boolean playable(int cardID) {
        if (lastCard == 4 * 14 + 1) {
            return (cardID == lastCard);
        }
        if (lastCard % 14 == 12 && needDraw > 0) {
            return (cardID % 14 == 12);
        }
        if (cardID / 14 == 4) return true;
        return (lastCard % 14 == cardID % 14 || lastCard / 14 == cardID / 14);
    }

    private void sendGroupMsg(String msg) {
        JSONObject J = new JSONObject();
        J.put("group_id", gameID);
        J.put("message", msg);
        Main.setNextSender("send_group_msg", J);
    }

    private void sendAllMsg(String msg) {
        for(long ID : playerID) {
            JSONObject J = new JSONObject();
            J.put("user_id", ID);
            J.put("message", msg);
            Main.setNextSender("send_private_msg", J);
        }
    }

    private void sendPrivateMsg(long ID, String msg) {
        JSONObject J = new JSONObject();
        J.put("user_id", ID);
        J.put("message", msg);
        Main.setNextSender("send_private_msg", J);
    }

    private int nextPlayer() {
        return (nowPlayer + order + playerID.size()) % playerID.size();
    }

    public boolean isBegin() {
        return isBegin;
    }

    public String getOrder() {
        StringBuilder sb = new StringBuilder("出牌顺序：\n");
        int bf = nowPlayer;
        do {
            sb.append(Main.getName(playerID.get(bf))).append("有").append(playerCards.get(bf).size()).append("张牌\n");
            bf = (bf + order + playerID.size()) % playerID.size();
        } while (bf != nowPlayer);
        return String.valueOf(sb);
    }

    public void join(long ID) {
        if (!UNOMain.UNOJoin(ID, gameID)) {
            sendPrivateMsg(ID, "您在其他群有游戏进行。");
        } else {
            playerID.add(ID);
            playerCards.add(new ArrayList<>());
            if (isBegin) draw(playerID.indexOf(ID), 7);
            sendPrivateMsg(ID, "加入成功");
            sendGroupMsg("人数：" + playerID.size());
        }
    }

    public int leave(long ID) {
        int u = playerID.indexOf(ID);
        if (u == -1) return -1;
        playerID.remove(u);
        if (isBegin) {
            for (int i = u; i < playerID.size() - 1; i++) {
                playerCards.set(i, playerCards.get(i + 1));
            }
        }
        playerCards.set(playerID.size() - 1, null);
        if (playerID.size() <= 1) {
            end("人数小于1人，游戏结束");
        }
        UNOMain.UNOLeave(ID);
        return 0;
    }

    public int play(long ID, int cardID) {
        int pos = playerID.indexOf(ID);
        if (pos == -1) return 1;//No such person
        if (pos != nowPlayer) return 2;//Not your turn
        int pos2 = playerCards.get(pos).indexOf(cardID);
        if (pos2 == -1) return 3;//No such card
        if (!playable(cardID)) return 4;//Cannot use that card

        playerCards.get(pos).remove(pos2);

        if (playerCards.get(pos).size() == 0) {
            win(pos);
        }

        lastCard = cardID;

        if (cardID / 14 == 4) {
            if (cardID % 14 == 1) needDraw += 4;
            return 5;//choose next color
        } else if (cardID % 14 == 12) {
            needDraw += 2;
        } else if (cardID % 14 == 10 && playerID.size() != 2) {
            order = -order;
        } else if (cardID % 14 == 10 || cardID % 14 == 11) {
            nowPlayer = nextPlayer();
        }
        nowPlayer = nextPlayer();
        return 0;
    }

    public void begin() {
        isBegin = true;
        order = 1;
        needDraw = 0;
        nowPlayer = 0;
        shuffle();
        for (int i = 0; i < playerID.size(); i++) draw(i, 7);
        while (cards[top] / 14 == 4) top++;
        lastCard = cards[top];
        top++;
    }

    private void end(String msg) {
        sendGroupMsg(msg);
        for (long u : playerID) {
            sendPrivateMsg(u, "游戏结束");
            UNOMain.UNOLeave(u);
        }
        UNOMain.getUnoGameMap().remove(gameID);
        isEnd = true;
    }

    private void win(int pos) {
        long ID = playerID.get(pos);
        end("[CQ:at,qq=" + ID + "] 赢了！");
    }

    public void setNextInput(long ID, String input) {
        nextID = ID;
        nextInput = input;
        hasNextInput = true;
    }

    @Override
    public void run() {
        begin();
        int TLE;
        String bufferInput;
        long bufferID;
        if (playerID.size() <= 1) {
            end("人数不足2人，开始失败");
            return;
        } else sendGroupMsg("UNO游戏开始");
        hasNextInput = false;
        while (!isEnd) {

            if (needOutput) {
                String cardName;
                if (lastCard / 14 == 4) {
                    if (lastCard % 14 == 0) cardName = "Err";
                    else cardName = "黑+4";
                } else {
                    cardName = color[lastCard / 14] + name[lastCard % 14];
                }
                if (lastCard % 14 == 12 && needDraw == 0) {
                    cardName += "(上一名玩家已摸牌)";
                }
                sendAllMsg("上一张牌是" + cardName + "\n现在是 " + Main.getName(playerID.get(nowPlayer)) + " 出牌，他还剩" + playerCards.get(nowPlayer).size() + "张牌");

                playerCards.get(nowPlayer).sort(Comparator.comparingInt(o -> o));

                StringBuilder nowCards = new StringBuilder("上一张牌是" + cardName);
                if (isDrawn) {
                    nowCards.append("\n[-1]:过");
                } else {
                    nowCards.append("\n[-1]:摸牌").append(needDraw == 0 ? 1 : needDraw).append("张");
                }
                int las = -1;
                for (int i = 0; i < playerCards.get(nowPlayer).size(); i++) {
                    int u = playerCards.get(nowPlayer).get(i);
                    if (u / 14 != las) {
                        nowCards.append('\n');
                    }
                    las = u / 14;
                    if (u / 14 == 4) {
                        if (u % 14 == 0) nowCards.append('[').append(i).append("]:改色 ").append(playable(u) ? "√" : " ");
                        else nowCards.append('[').append(i).append("]:黑+4 ").append(playable(u) ? "√" : " ");
                    } else {
                        nowCards.append('[').append(i).append("]:").append(color[u / 14]).append(name[u % 14]).append(playable(u) ? "√" : " ").append(' ');
                    }
                }
                sendPrivateMsg(playerID.get(nowPlayer), String.valueOf(nowCards));
            }

            needOutput = true;

            TLE = 0;
            while (!hasNextInput && !isEnd) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    end("In program error.");
                    return;
                }
                TLE++;
                if (TLE == 60 * 100) {
                    sendPrivateMsg(playerID.get(nowPlayer), "60秒未出牌。若再30秒不出则自动踢出");
                }
                if (TLE > 90 * 100) {
                    sendGroupMsg("[CQ:at,qq=" + playerID.get(nowPlayer) + "] 90秒未出牌，自动踢出");
                    leave(playerID.get(nowPlayer));
                    if (colorChoosing) {
                        colorChoosing = false;
                        nextColor = new Random().nextInt(4);
                    }
                    TLE = 0;
                }
            }
            if (isEnd) return;
            hasNextInput = false;
            bufferInput = nextInput;
            bufferID = nextID;
            if (!playerID.contains(bufferID)) {
                needOutput = false;
                continue;
            }
            if (bufferInput.equals(".resend")) {
                continue;
            } else if (bufferInput.equals(".order")) {
                sendGroupMsg(getOrder());
            } else if (bufferInput.equals(".leave")) {
                if (leave(bufferID) == -1) sendGroupMsg("[CQ:at,qq=" + bufferID + "] 您不在游戏中");
                else sendGroupMsg("[CQ:at,qq=" + bufferID + "] 退出成功");
            } else if (bufferInput.equals(".join")) {
                if (playerID.contains(bufferID)) {
                    sendGroupMsg("[CQ:at,qq=" + bufferID + "] 您已在游戏中");
                } else {
                    join(bufferID);
                }
            } else if (bufferInput.indexOf(".play") == 0) {
                if (playerID.get(nowPlayer) != bufferID) {
                    sendPrivateMsg(bufferID, "不是你的回合");
                    needOutput = false;
                }
                int playID;
                try {
                    playID = Integer.parseInt(bufferInput.substring(6));
                } catch (NumberFormatException e) {
                    sendPrivateMsg(bufferID, "不可识别的ID");
                    needOutput = false;
                    continue;
                }
                if (colorChoosing) {
                    if (playID >= 1 && playID <= 4) {
                        sendPrivateMsg(bufferID, "颜色设置成功");
                        nextColor = playID - 1;
                        if (needDraw == 0) {
                            lastCard = nextColor * 14 + 13;
                            nextColor = -1;
                        }
                        nowPlayer = nextPlayer();
                        colorChoosing = false;
                    } else {
                        sendPrivateMsg(bufferID, "颜色ID错误");
                        needOutput = false;
                    }
                    continue;
                } else if (playID == -1) {
                    if (!isDrawn) {
                        if (needDraw == 0) {
                            needDraw = 1;
                        }
                        draw(playerID.indexOf(bufferID), needDraw);
                        sendPrivateMsg(bufferID, "手牌增加" + needDraw + "张");
                        sendAllMsg(Main.getName(bufferID) + " 已摸牌" + needDraw + "张");
                        if (nextColor >= 0) {
                            lastCard = nextColor * 14 + 13;
                            nextColor = -1;
                        }
                        needDraw = 0;
                        isDrawn = true;
                    } else {
                        nowPlayer = nextPlayer();
                        isDrawn = false;
                    }
                    continue;
                } else {
                    try {
                        playID = playerCards.get(playerID.indexOf(bufferID)).get(playID);
                        int u = play(bufferID, playID);
                        if (u == 4) {
                            sendPrivateMsg(bufferID, "不能打出这张牌");
                            needOutput = false;
                        } else if (u == 5) {
                            sendPrivateMsg(bufferID, "请选择颜色：1.红 2.黄 3.蓝 4.绿");
                            colorChoosing = true;
                        } else if (u == 0) {
                            sendPrivateMsg(bufferID, "打出成功");
                        } else {
                            sendPrivateMsg(bufferID, "Unknown Error.");
                            needOutput = false;
                        }
                        isDrawn = false;
                    } catch (IndexOutOfBoundsException e) {
                        sendPrivateMsg(bufferID, "ID过大");
                        needOutput = false;
                    }
                    continue;
                }
            }
            needOutput = false;
        }
    }
}