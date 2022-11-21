package function.getImage621;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import httpconnect.HttpURLConnectionUtil;
import interfaces.Processable;
import main.Main;
import utils.ImageDownloader;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import static main.Main.localPath;

public class GetImage621Main implements Processable {

    String userName, authorKey;
    private JSONArray JGroup, JPrivate, JAdmin;
    private long lastMsg = 0;
    private int retry = 0;


    public GetImage621Main() {
        try {
            File ff = new File("621Level.json");
            if (!ff.exists()) {
                if (!ff.createNewFile()) System.out.println("621权限管理文件创建失败");
                else {
                    FileWriter fw = new FileWriter(ff);
                    fw.write("{}");
                    fw.close();
                }
            }
            FileReader f = new FileReader("621Level.json");
            Scanner S = new Scanner(f);
            StringBuilder sb = new StringBuilder();
            while (S.hasNext()) {
                sb.append(S.nextLine()).append(' ');
            }

            JSONObject J = JSONObject.parseObject(String.valueOf(sb));
            JGroup = J.getJSONArray("group");
            JPrivate = J.getJSONArray("private");
            JAdmin = J.getJSONArray("admin");
            userName = J.getString("username");
            authorKey = J.getString("authorKey");
            S.close();
            f.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    level:
        0: safe feral pokemon
        1: safe feral
        2: safe
        3: question
        4: explicit feral or s/q
        5: explicit
     */
    private StringBuilder dealInput(String input, int level, boolean poolFlag) {

        StringBuilder sb = new StringBuilder(input);

        if (level <= 0) sb.append(" score:>0");
        if (level <= 2) sb.append(" rating:s");
        else if (level <= 3) {
            sb.append(" -rating:e");
        }

        if (level == 0) sb.append(" feral pokemon");
        if (level == 1) sb.append(" feral");
        if (level == 4 && sb.indexOf("rating:q") == -1 && sb.indexOf("rating:s") == -1) sb.append(" feral");

        String[] qs = String.valueOf(sb).split(" ");
        StringBuilder quest = new StringBuilder();
        for (String q : qs) {
            if (q.startsWith("score:>") && level <= 2) continue;
            if (q.startsWith("rating:q") && level <= 2) continue;
            if (q.startsWith("rating:e") && level <= 3) continue;

            if (q.length() > 0) {
                quest.append('+').append(q);
            }
        }
        if (quest.length() == 0) quest.append("eeveelution");
        if (!input.contains("id:")) {
            if (!input.contains("favcount") && !input.contains("score") && level <= 2 && !poolFlag)
                quest.append("+favcount:>10").append("+score:>10");
            else if (!input.contains("favcount") && !input.contains("score") && !poolFlag)
                quest.append("+favcount:>400").append("+score:>200");
            if (!input.contains("order") && !poolFlag) quest.append("+order:random");
            if (!input.contains("gore") && level <= 4) quest.append("+-gore");
            if (!input.contains("human") && level <= 2) quest.append("+-human");
        }
        return quest;
    }

    @Override
    public void process(String message_type, String message, long group_id, long user_id, int message_id) {

        message = message.toLowerCase();
        if (message.equals("621.recall") && lastMsg != 0) {
            JSONObject J = new JSONObject();
            J.put("message_id", lastMsg);
            Main.setNextSender("delete_msg", J);
            return;
        }
        if (message.startsWith("621.set")) {
            adminProcessSet(message_type, message.substring(8), group_id, user_id);
            return;
        }
        if (message.startsWith("621.del")) {
            adminProcessDel(message_type, message.substring(8), group_id, user_id);
            return;
        }
        if (message.equals("621.default")) {
            Main.setNextSender(message_type, user_id, group_id, "如未指定tag，默认加上eeveelution\n如未指定favcount或score，默认加上favcount:>400 score:>200\n如未指定以下tags，默认不搜索gore,anthro,human");
            return;
        }
        if (message.equals("621.level")) {
            Main.setNextSender(message_type, user_id, group_id,
                    "level:\n  0: safe feral pokemon\n  1: safe feral\n  2: safe\n  3: question\n  4: explicit feral or safe/q\n  5: explicit");
            return;
        }
        if (message.startsWith("621.autocomplete")) {
            message = message.substring(16).trim();
            try {
                JSONArray JA = JSONArray.parseArray(HttpURLConnectionUtil.do621Get("https://e621.net/tags/autocomplete.json?search[name_matches]=" + message + "&expiry=7", userName, false, null));

                StringBuilder sb = new StringBuilder();
                for (Object J : JA) {
                    sb.append(((JSONObject) J).getString("name")).append("    ").append(numberTrans(((JSONObject) J).getInteger("post_count"))).append('\n');
                }
                Main.setNextSender(message_type, user_id, group_id, sb.toString());
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
            }
            return;
        }

        int level = -1;
        if (message_type.equals("group")) {
            for (int i = 0; i < JGroup.size(); i++) {
                JSONObject J = JGroup.getJSONObject(i);
                if (J.getLong("id") == group_id) level = J.getInteger("level");
            }
        } else {
            for (int i = 0; i < JPrivate.size(); i++) {
                JSONObject J = JPrivate.getJSONObject(i);
                if (J.getLong("id") == user_id) level = J.getInteger("level");
            }
        }
        if (level < 0) {
            Main.setNextLog("621 at group " + group_id + " by " + user_id + " but level < 0", 0);
            return;
        }
        boolean poolFlag = message.contains("pool:");
        if (message.startsWith("621.input")) {
            Main.setNextSender(message_type, user_id, group_id, String.valueOf(dealInput(message.substring(9), level, poolFlag)));
            return;
        }

        message = message.substring(3);

        boolean getTag = false;
        if (message.startsWith(".tag")) {
            if (message.startsWith(".tags")) message = message.substring(5).trim();
            else message = message.substring(4).trim();
            getTag = true;
        }
        StringBuilder quest = dealInput(message, level, poolFlag);
        StringBuilder quest2 = new StringBuilder(quest);

        quest.insert(0, "https://e621.net/posts.json?limit=1&tags=");
        quest2.insert(0, "https://e621.net/posts.json?limit=50&tags=");

        //System.out.println(quest2);

        JSONObject J, J2;
        try {
            J = JSONObject.parseObject(HttpURLConnectionUtil.do621Get(quest.toString(), userName, true, authorKey));
            //System.out.println(HttpURLConnectionUtil.doGet(quest2.toString()));
            J2 = JSONObject.parseObject(HttpURLConnectionUtil.do621Get(quest2.toString(), userName, true, authorKey));
            //System.out.println(J2);
        } catch (SocketTimeoutException e) {
            retry++;
            //System.out.println("621 network failed");
            if (retry >= 3) {
                Main.setNextSender(message_type, user_id, group_id, "网络不通畅发送图片失败");
                Main.setNextLog("621 at group " + group_id + " by " + user_id + " but SocketTimeOut and retry = " + retry, 2);
            } else {
                if(getTag) this.process(message_type, "621.tag " + message, group_id, user_id, message_id);
                else this.process(message_type, "621" + message, group_id, user_id, message_id);
            }
            return;
        }
        if (J == null || J2 == null) {
            retry++;
            //System.out.println("621 unknown reason");
            if (retry >= 3) {
                Main.setNextSender(message_type, user_id, group_id, "奇怪原因发送图片失败");
                Main.setNextLog("621 at group " + group_id + " by " + user_id + " but unknown reason and retry = " + retry, 2);
            } else {
                if(getTag) this.process(message_type, "621.tag " + message, group_id, user_id, message_id);
                else this.process(message_type, "621" + message, group_id, user_id, message_id);
            }
            return;
        }
        int count = J2.getJSONArray("posts").size();
        if (J.getJSONArray("posts").size() == 0) {
            Main.setNextSender(message_type, user_id, group_id, "不存在图片");
            Main.setNextLog("621 at group " + group_id + " by " + user_id + " but no img and retry = " + retry, 0);
            return;
        }

        if (!poolFlag) {
            J = J.getJSONArray("posts").getJSONObject(0);
            if(getTag){
                J = JSONObject.parseObject(String.valueOf(
                        Main.setNextSender(message_type, user_id, group_id, getImageTags(J))));
            } else {
                J = JSONObject.parseObject(String.valueOf(
                        Main.setNextSender(message_type, user_id, group_id, getImageInfo(J, count, poolFlag))));
            }
            if (J.getString("status").equals("failed")) {
                retry++;
                //System.out.println("621 tx failed");
                if (retry >= 3) {
                    Main.setNextSender(message_type, user_id, group_id, "tx原因发送图片失败");
                    Main.setNextLog("621 at group " + group_id + " by " + user_id + " but tencent catch it and retry = " + retry, 1);
                } else {
                    if(getTag) this.process(message_type, "621.tag " + message, group_id, user_id, message_id);
                    else this.process(message_type, "621" + message, group_id, user_id, message_id);
                }
            } else {
                lastMsg = J.getJSONObject("data").getLong("message_id");
                retry = 0;
                Main.setNextLog("621 at group " + group_id + " by " + user_id, 0);
            }
        } else {
            try {
                long poolID = getPoolID(J2.getJSONArray("posts").getJSONObject(0));
                String quest3 = "https://e621.net/pools.json?search[id]=" + poolID;
                JSONArray JA = JSONArray.parseArray(HttpURLConnectionUtil.do621Get(quest3, userName, true, authorKey));
                J = JA.getJSONObject(0);
                List<Integer> postIDs = J.getJSONArray("post_ids").toJavaList(Integer.class);
                StringBuilder msg = new StringBuilder("转发\n");
                msg.append(Main.botQQ).append(" ").append(J.getString("category")).append(": ").append(J.getString("name")).append("\n");
                msg.append(Main.botQQ).append(" 合并行\n简介：").append(J.getString("description")/*.formatted()*/).append("\n结束合并\n");
                msg.append(Main.botQQ).append(" 共有 ").append(J.getLong("post_count")).append(" 张\n");
                //System.out.println(msg);
                for (int i = 0; i < postIDs.size(); i++) {
                    for (int j = 0; j < J2.getJSONArray("posts").size(); j++) {
                        if (Objects.equals(postIDs.get(i), J2.getJSONArray("posts").getJSONObject(j).getInteger("id"))) {
                            msg.append(Main.botQQ).append(" 合并行\n");
                            msg.append(getImageInfo(J2.getJSONArray("posts").getJSONObject(j), count, poolFlag));
                            msg.append("\n结束合并\n");
                        }
                    }
                }
                J = new JSONObject();
                J.put("post_type", "message");
                J.put("message", msg.toString());
                J.put("message_type", message_type);
                J.put("message_id", message_id);
                J.put("user_id", user_id);
                J.put("group_id", group_id);
                Main.setNextOutput(J.toString());
                retry = 0;
                Main.setNextLog("621 at group " + group_id + " by " + user_id, 0);
            } catch (SocketTimeoutException e) {
                Main.setNextLog("621 at group " + group_id + " by " + user_id + " Runtime Error", 2);
                throw new RuntimeException(e);
            }
        }
    }

    private String getImageTags(JSONObject img) {
        StringBuilder sb = new StringBuilder();
        sb.append("artist:");
        for(String u : img.getJSONObject("tags").getJSONArray("artist").toJavaList(String.class)) sb.append(' ').append(u);
        sb.append('\n');
        sb.append("character:");
        for(String u : img.getJSONObject("tags").getJSONArray("character").toJavaList(String.class)) sb.append(' ').append(u);
        sb.append('\n');
        sb.append("species:");
        for(String u : img.getJSONObject("tags").getJSONArray("species").toJavaList(String.class)) sb.append(' ').append(u);
        sb.append('\n');

        return sb.toString();
    }

    private long getPoolID(JSONObject posts) {
        return posts.getJSONArray("pools").getInteger(0);
    }

    private String numberTrans(int u) {
        if (u > 1000000) return (u / 1000000) + "M";
        else if (u > 1000) return (u / 1000) + "k";
        else return String.valueOf(u);
    }

    private String getImageInfo(JSONObject J, int count, boolean poolFlag) {

        String imageUrl;
        if (retry >= 1) {
            if (retry == 1) imageUrl = J.getJSONObject("sample").getString("url");
            else imageUrl = J.getJSONObject("preview").getString("url");
        } else imageUrl = J.getJSONObject("tags").getJSONArray("meta").toJavaList(String.class).contains("animated") ?
                J.getJSONObject("sample").getString("url") :
                J.getJSONObject("file").getString("url");

        if (imageUrl == null) return "";
        long id = J.getLong("id");
        long fav_count = J.getLong("fav_count");
        long score = J.getJSONObject("score").getLong("total");
        StringBuilder quest = new StringBuilder();
        if (!poolFlag && count != 50) quest.append("只有").append(count).append("个图片\n");
        if (poolFlag && count == 50) quest.append("多于").append(count).append("个图片\n");

        int extPos = 0, tmpPos;
        while ((tmpPos = imageUrl.indexOf(".", extPos)) != -1) extPos = tmpPos + 1;
        String fileExt = imageUrl.substring(extPos);
        String imageLocalPath = String.valueOf(id) + '.' + fileExt;
        if (!new File("resource/download/e621/" + imageLocalPath).exists()) {
            ImageDownloader.download(imageUrl, "resource/download/e621", imageLocalPath);
        }
        ImageDownloader.addRandomNoise("resource/download/e621/" + imageLocalPath, fileExt);

        quest.append("[CQ:image,file=file:///").append(localPath).append("/resource/download/e621/").append(imageLocalPath).append(",id=40000]\n");

        quest.append("Fav_count: ").append(fav_count).append("  ");
        quest.append("Score: ").append(score).append("\n");

        List<Integer> poolList = J.getJSONArray("pools").toJavaList(Integer.class);
        if (poolList.size() > 0) {
            quest.append("pools:");
            for (int u : poolList) {
                quest.append(" ").append(u);
            }
            quest.append('\n');
        }

        quest.append("id: ").append(id);
        return quest.toString();
    }

    private void adminProcessSet(String message_type, String message, long group_id, long user_id) {

        int level = getLevel(user_id);
        if (level <= 0) return;

        String[] sp = message.split(" ");
        if (sp.length <= 1) {
            Main.setNextSender(message_type, user_id, group_id, "格式为：621.set (type id)/this level");
            return;
        }
        try {
            long id;
            int setLevel;
            if (sp[0].equals("this")) {
                if (sp.length != 2) {
                    Main.setNextSender(message_type, user_id, group_id, "格式为：621.set (type id)/this level");
                    return;
                }
                sp[0] = message_type;
                if (message_type.equals("group")) {
                    id = group_id;
                } else {
                    id = user_id;
                }
                setLevel = Integer.parseInt(sp[1]);
            } else {
                if (sp.length != 3) {
                    Main.setNextSender(message_type, user_id, group_id, "格式为：621.set (type id)/this level");
                    return;
                }
                id = Long.parseLong(sp[1]);
                setLevel = Integer.parseInt(sp[2]);
            }
            if (setLevel >= level) {
                Main.setNextSender(message_type, user_id, group_id, "权限不够");
                return;
            }
            switch (sp[0]) {
                case "group" : {
                    boolean flg = true;
                    for (int i = 0; i < JGroup.size(); i++) {
                        JSONObject J = JGroup.getJSONObject(i);
                        if (J.getLong("id") == id) {
                            J.put("level", setLevel);
                            JGroup.set(i, J);
                            Main.setNextSender(message_type, user_id, group_id, "修改成功");
                            flg = false;
                            break;
                        }
                    }
                    if (flg) {
                        JSONObject J = new JSONObject();
                        J.put("id", id);
                        J.put("level", setLevel);
                        JGroup.add(J);
                        Main.setNextSender(message_type, user_id, group_id, "新建成功");
                    }
                }break;
                case "private" : {
                    boolean flg = true;
                    for (int i = 0; i < JPrivate.size(); i++) {
                        JSONObject J = JPrivate.getJSONObject(i);
                        if (J.getLong("id") == id) {
                            JPrivate.set(i, JPrivate.getJSONObject(i).put("level", setLevel));
                            Main.setNextSender(message_type, user_id, group_id, "修改成功");
                            flg = false;
                            break;
                        }
                    }
                    if (flg) {
                        JSONObject J = new JSONObject();
                        J.put("id", id);
                        J.put("level", setLevel);
                        JPrivate.add(J);
                        Main.setNextSender(message_type, user_id, group_id, "新建成功");
                    }
                }break;
                case "admin" : {
                    boolean flg = true;
                    for (int i = 0; i < JAdmin.size(); i++) {
                        JSONObject J = JAdmin.getJSONObject(i);
                        if (J.getLong("id") == id) {
                            JAdmin.set(i, JAdmin.getJSONObject(i).put("level", setLevel));
                            Main.setNextSender(message_type, user_id, group_id, "修改成功");
                            flg = false;
                            break;
                        }
                    }
                    if (flg) {
                        JSONObject J = new JSONObject();
                        J.put("id", id);
                        J.put("level", setLevel);
                        JAdmin.add(J);
                        Main.setNextSender(message_type, user_id, group_id, "新建成功");
                    }
                }break;
                default : Main.setNextSender(message_type, user_id, group_id, "type: group/private/admin or this");
            }

            saveLevel();
        } catch (NumberFormatException | IOException e) {
            Main.setNextSender(message_type, user_id, group_id, e.getMessage());
        }
    }

    private void adminProcessDel(String message_type, String message, long group_id, long user_id) {
        int level = getLevel(user_id);
        if (level <= 0) return;

        String[] sp = message.split(" ");
        if (sp.length != 2 && sp.length != 1) {
            Main.setNextSender(message_type, user_id, group_id, "格式为：621.del (type id)/this");
            return;
        }
        try {
            long id;
            if (sp[0].equals("this")) {
                sp[0] = message_type;
                if (message_type.equals("group")) {
                    id = group_id;
                } else {
                    id = user_id;
                }
            } else {
                id = Long.parseLong(sp[1]);
            }

            switch (sp[0]) {
                case "group" : {
                    boolean flg = true;
                    for (int i = 0; i < JGroup.size(); i++) {
                        JSONObject J = JGroup.getJSONObject(i);
                        if (J.getLong("id") == id) {
                            if (J.getInteger("level") >= level) {
                                Main.setNextSender(message_type, user_id, group_id, "权限不够");
                            } else {
                                JGroup.remove(i);
                                Main.setNextSender(message_type, user_id, group_id, "删除成功");
                                flg = false;
                            }
                            break;
                        }
                    }
                    if (flg) {
                        Main.setNextSender(message_type, user_id, group_id, "未找到");
                    }
                }break;
                case "private" : {
                    boolean flg = true;
                    for (int i = 0; i < JPrivate.size(); i++) {
                        JSONObject J = JPrivate.getJSONObject(i);
                        if (J.getLong("id") == id) {
                            if (J.getInteger("level") >= level) {
                                Main.setNextSender(message_type, user_id, group_id, "权限不够");
                            } else {
                                JPrivate.remove(i);
                                Main.setNextSender(message_type, user_id, group_id, "删除成功");
                                flg = false;
                            }
                            break;
                        }
                    }
                    if (flg) {
                        Main.setNextSender(message_type, user_id, group_id, "未找到");
                    }
                }break;
                case "admin" : {
                    boolean flg = true;
                    for (int i = 0; i < JAdmin.size(); i++) {
                        JSONObject J = JAdmin.getJSONObject(i);
                        if (J.getLong("id") == id) {
                            if (J.getInteger("level") >= level) {
                                Main.setNextSender(message_type, user_id, group_id, "权限不够");
                            } else {
                                JAdmin.remove(i);
                                Main.setNextSender(message_type, user_id, group_id, "删除成功");
                                flg = false;
                            }
                            break;
                        }
                    }
                    if (flg) {
                        Main.setNextSender(message_type, user_id, group_id, "未找到");
                    }
                }break;
                default : Main.setNextSender(message_type, user_id, group_id, "type: group/private/admin or this");
            }

            saveLevel();
        } catch (NumberFormatException | IOException e) {
            Main.setNextSender(message_type, user_id, group_id, e.toString());
        }
    }

    private int getLevel(long user_id) {
        int level = 0;
        for (int i = 0; i < JAdmin.size(); i++) {
            JSONObject J = JAdmin.getJSONObject(i);
            if (J.getLong("id") == user_id) level = J.getInteger("level");
        }
        return level;
    }

    private void saveLevel() throws IOException {
        FileWriter fw = new FileWriter("621Level.json", false);
        BufferedWriter bw = new BufferedWriter(fw);
        JSONObject J = new JSONObject();
        J.put("group", JGroup);
        J.put("private", JPrivate);
        J.put("admin", JAdmin);
        J.put("username", userName);
        J.put("authorKey", authorKey);
        bw.write(J.toString());
        bw.close();
        fw.close();
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        if (!message.startsWith("621")) return false;
        if (message_type.equals("group")) {
            for (int i = 0; i < JGroup.size(); i++) {
                JSONObject J = JGroup.getJSONObject(i);
                if (J.getLong("id") == group_id) return true;
            }
        } else {
            for (int i = 0; i < JPrivate.size(); i++) {
                JSONObject J = JPrivate.getJSONObject(i);
                if (J.getLong("id") == user_id) return true;
            }
        }
        return false;
    }

    @Override
    public String help() {
        return null;
    }
}