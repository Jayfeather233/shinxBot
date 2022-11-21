package function.nonogram;

import com.alibaba.fastjson.JSONObject;
import function.imageGenerator.ImageGeneratorMain;
import utils.ImageDownloader;
import main.Main;
import interfaces.Processable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.*;

import static main.Main.localPath;

class pair {
    int diff, id;

    public pair(int a, int b) {
        this.diff = a;
        this.id = b;
    }
}

public class nonogram implements Processable {
    private final Map<Long, pair> idMap = new HashMap<>();
    private final Map<Long, Boolean> isAt = new HashMap<>();
    private final List<List<String>> gameList = new ArrayList<>();
    private final Random R = new Random();


    public nonogram() {
        try {
            File ff = new File("nonogramData.json");
            if (!ff.exists()) {
                FileWriter fw = new FileWriter(ff);
                fw.write("{}");
                fw.close();
            }
            FileReader f = new FileReader("nonogramData.json");
            Scanner S = new Scanner(f);
            StringBuilder sb = new StringBuilder();
            while (S.hasNext()) {
                sb.append(S.nextLine()).append(' ');
            }
            JSONObject J = JSONObject.parseObject(String.valueOf(sb));
            S.close();
            f.close();

            for (int i = 1; J.containsKey(String.valueOf(i)); i++) {
                gameList.add(new ArrayList<>());
                gameList.get(i - 1).addAll(J.getJSONArray(String.valueOf(i)).toJavaList(String.class));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(String message_type, String message, long group_id, long user_id, int message_id) {
        if (message.equals("数织帮助")) {
            Main.setNextSender(message_type, user_id, group_id,
                    "游戏棋盘是一张正方形网格. 棋盘每一行左边或每一列上方的数字表示该行或该列上每一组相邻的黑色方格的长度。 游戏目标是要找出所有的黑色方格。在原图上涂黑后发回机器人即可。\n"+
                    "开始新游戏：来局数织\n"+
                    "数织检测：数织检测 [1,2,3](难度) [图片]\n"+
                    "提交答案：@机器人并发送图片\n");
            return;
        }
        if (message.equals("来局数织")) {
            Main.setNextSender(message_type, user_id, group_id, "请选择难度：\n1: 5x5\n2: 10x10\n3: 15x15");
            idMap.put(user_id, new pair(-1, message_id));
            return;
        }
        if (idMap.get(user_id).diff == -1) {
            try {
                int id = idMap.get(user_id).id;
                pair u = getGameID(Integer.parseInt(message));
                idMap.put(user_id, u);
                Main.setNextSender(message_type, user_id, group_id, "[CQ:reply,id=" + id + "] [CQ:image,file=file:///" + localPath + generateNonogram(u) + "]");
            } catch (NumberFormatException e) {
                return;
            }
        }
        boolean flg1 = message.contains("[CQ:at,qq=" + Main.botQQ + "]");
        boolean flg2 = message.contains("[CQ:image,");
        String fileName = null, url;
        if ((flg1 || isAt.containsKey(user_id)) && flg2) {
            isAt.remove(user_id);
            int u;
            u = message.indexOf(",file=");
            u += 6;
            for (int i = u; i < message.length(); i++) {
                if (message.charAt(i) == '.') {
                    fileName = message.substring(u, i);
                    break;
                }
            }

            u = message.indexOf(",url=");
            url = message.substring(u + 5);
            for (int i = 0; i < url.length(); i++) {
                if (url.charAt(i) == ']') {
                    url = url.substring(0, i);
                    break;
                }
            }
            fileName = fileName + ".png";
            ImageDownloader.download(url, "resource/download", fileName);

            if (picCheck(idMap.get(user_id), "resource/download/" + fileName)) {
                Main.setNextSender(message_type, user_id, group_id, "恭喜你答对啦" + getColoredImg(idMap.get(user_id)));
                idMap.remove(user_id);
            } else {
                Main.setNextSender(message_type, user_id, group_id, "似乎做的不对呢");
            }
        } else if (flg1) {
            isAt.put(user_id, true);
            Main.setNextSender(message_type, user_id, group_id, "图来！");
        } else {
            isAt.remove(user_id);
        }
    }

    private String getColoredImg(pair u) {
        File file = new File("./resource/nonogram/" + u.diff + "/color" + u.id + ".png");
        if (!file.exists()) {
            return "";
        } else {
            return "\n[CQ:image,file=file:///" + localPath + "/resource/nonogram/" + u.diff + "/color" + u.id + ".png]";
        }
    }

    private String generateNonogram(pair u) {
        File outFile = new File("./resource/nonogram/" + u.diff + "/board" + u.id + ".png");
        if (!outFile.exists()) {
            BufferedImage buffImg = new BufferedImage(80 * u.diff * 6 + 10, 80 * u.diff * 6 + 10, BufferedImage.TYPE_INT_RGB);
            Graphics g = buffImg.getGraphics();
            g.setColor(Color.white);
            g.fillRect(0, 0, 80 * u.diff * 6 + 10, 80 * u.diff * 6 + 10);
            g.setColor(new Color(240, 243, 248));
            for (int i = 0; i < u.diff * 5; i++) {
                g.fillRoundRect((u.diff + i) * 80 + 5, 10, 70, u.diff * 80 - 20, 20, 20);
                g.fillRoundRect(10, (u.diff + i) * 80 + 5, u.diff * 80 - 20, 70, 20, 20);
            }
            g.setColor(new Color(221, 228, 236));
            for (int i = 0; i < u.diff * 5; i++) {
                g.drawRoundRect((u.diff + i) * 80 + 5, 10, 70, u.diff * 80 - 20, 20, 20);
                g.drawRoundRect(10, (u.diff + i) * 80 + 5, u.diff * 80 - 20, 70, 20, 20);
            }
            g.setColor(new Color(190, 197, 213));
            for (int i = 0; i < u.diff * 5; i++) {
                g.drawLine((u.diff + i) * 80, u.diff * 80, (u.diff + i) * 80, 80 * u.diff * 6);
                g.drawLine(u.diff * 80, (u.diff + i) * 80, 80 * u.diff * 6, (u.diff + i) * 80);
            }
            g.setColor(new Color(0, 0, 0));
            for (int i = 0; i <= u.diff; i++) {
                g.drawLine((u.diff + i * 5) * 80 - 1, u.diff * 80, (u.diff + i * 5) * 80 - 1, 80 * u.diff * 6);
                g.drawLine(u.diff * 80, (u.diff + i * 5) * 80 - 1, 80 * u.diff * 6, (u.diff + i * 5) * 80 - 1);

                g.drawLine((u.diff + i * 5) * 80, u.diff * 80, (u.diff + i * 5) * 80, 80 * u.diff * 6);
                g.drawLine(u.diff * 80, (u.diff + i * 5) * 80, 80 * u.diff * 6, (u.diff + i * 5) * 80);

                g.drawLine((u.diff + i * 5) * 80 + 1, u.diff * 80, (u.diff + i * 5) * 80 + 1, 80 * u.diff * 6);
                g.drawLine(u.diff * 80, (u.diff + i * 5) * 80 + 1, 80 * u.diff * 6, (u.diff + i * 5) * 80 + 1);
            }

            String data = gameList.get(u.diff - 1).get(u.id);
            boolean[][] mp = new boolean[u.diff * 5][u.diff * 5];
            for (int i = 0; i < u.diff * 5; i++)
                for (int j = 0; j < u.diff * 5; j++)
                    mp[i][j] = (data.charAt(i * u.diff * 5 + j) == '1');
            List<List<Integer>> nums1 = new ArrayList<>();
            List<String> nums2 = new ArrayList<>();
            int fontSize = 200, interval = 1000;
            for (int i = 0; i < u.diff * 5; i++) {
                List<Integer> qq;
                qq = getNumber(mp, 0, i, 1, 0, u.diff);
                for (int qqq : qq) {
                    fontSize = Math.min(fontSize, getProperFont("微软雅黑", String.valueOf(qqq), g, 50, (80 * u.diff - 10) / qq.size()));
                    interval = Math.min(interval, (80 * u.diff - 10) / qq.size());
                }
                nums1.add(qq);
                qq = getNumber(mp, i, 0, 0, 1, u.diff);
                StringBuilder ww = new StringBuilder();
                for (int qqq : qq) {
                    ww.append(' ').append(qqq);
                }
                fontSize = Math.min(fontSize, getProperFont("微软雅黑", ww.toString(), g, 80 * u.diff, 70));
                nums2.add(ww.toString());
            }
            g.setFont(new Font("微软雅黑", Font.PLAIN, fontSize));
            int dx, dy;
            dx = u.diff * 80 + 15;
            for (List<Integer> i : nums1) {
                dy = u.diff * 80 - 20;
                for (int j = i.size() - 1; j >= 0; j--) {
                    g.drawString(String.valueOf(i.get(j)), dx, dy);
                    dy -= interval;
                }
                dx += 80;
            }
            dy = u.diff * 80 + 55;
            for (String s : nums2) {
                dx = u.diff * 80 - 20 - getFontSize("微软雅黑", s, g, fontSize).width;
                g.drawString(s, dx, dy);
                dy += 80;
            }

            try {
                ImageIO.write(buffImg, "png", outFile);
            } catch (IOException e) {
                new File("./resource/nonogram/" + u.diff + "/").mkdirs();
                try {
                    ImageIO.write(buffImg, "png", outFile);
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
            }
        }
        return "/resource/nonogram/" + u.diff + "/board" + u.id + ".png";
    }

    private List<Integer> getNumber(boolean[][] mp, int x, int y, int dx, int dy, int diff) {
        List<Integer> re = new ArrayList<>();
        int u;
        while (0 <= x && x < diff * 5 && 0 <= y && y < diff * 5) {
            while (0 <= x && x < diff * 5 && 0 <= y && y < diff * 5 && !mp[x][y]) {
                x += dx;
                y += dy;
            }
            u = 0;
            while (0 <= x && x < diff * 5 && 0 <= y && y < diff * 5 && mp[x][y]) {
                x += dx;
                y += dy;
                u++;
            }
            if (u != 0) re.add(u);
        }
        return re;
    }

    private int getProperFont(String name, String msg, Graphics g, int wid, int hei) {
        int l = 5, r = 100, mid;
        while (l < r - 1) {
            mid = (l + r) >> 1;
            Rectangle rec = getFontSize(name, msg, g, mid);
            if (rec.width > wid || rec.height > hei) {
                r = mid;
            } else {
                l = mid;
            }
        }
        return l;
    }

    private Rectangle getFontSize(String name, String msg, Graphics g, int size) {
        Font f = new Font(name, Font.PLAIN, size);
        return f.getStringBounds(msg, g.getFontMetrics(f).getFontRenderContext()).getBounds();
    }

    private boolean picCheck(pair gameID, String file) {
        try {
            BufferedImage buffImg = ImageIO.read(new File(file));
            int w = buffImg.getWidth();
            int h = buffImg.getHeight();
            int blockSizeX = (int) (h * (gameID.diff * 6 * 80) * 1.0 / (gameID.diff * 6 * 80 + 10) / (gameID.diff * 6));
            int blockSizeY = (int) (w * (gameID.diff * 6 * 80) * 1.0 / (gameID.diff * 6 * 80 + 10) / (gameID.diff * 6));
            int dx = (h - blockSizeX * (gameID.diff) * 6) / 2;
            int dy = (w - blockSizeY * (gameID.diff) * 6) / 2;

            int[][] bri = new int[gameID.diff * 5][gameID.diff * 5];
            int[] cnt = new int[256];
            Color[][] imgColor = ImageGeneratorMain.getImagePixArray(buffImg);

            for (int i = 0; i < gameID.diff * 5; i++) {
                for (int j = 0; j < gameID.diff * 5; j++) {
                    int tr = 0, cntx = 0;
                    for (int ii = (i + gameID.diff) * blockSizeX + dx + blockSizeX / 8; ii < (i + gameID.diff + 1) * blockSizeX + dx - blockSizeX / 8; ii++)
                        for (int jj = (j + gameID.diff) * blockSizeY + dy + blockSizeY / 8; jj < (j + gameID.diff + 1) * blockSizeY + dy - blockSizeY / 8; jj++) {
                            if (jj < imgColor.length && ii < imgColor[jj].length) {
                                tr += imgColor[jj][ii].getRed() + imgColor[jj][ii].getGreen() + imgColor[jj][ii].getBlue();
                                cntx++;
                            }

                        }
                    bri[i][j] = Math.min(255, tr / (3 * cntx));
                    cnt[bri[i][j]]++;
                }
            }

            int minn = 255, maxn = 0;
            for (int i = 0; i < 256; i++) {
                if (cnt[i] != 0) {
                    minn = Math.min(minn, i);
                    maxn = Math.max(maxn, i);
                }
            }
            int low = Math.max(maxn - 8, maxn-(maxn-minn+4)/5);

            String ans = gameList.get(gameID.diff - 1).get(gameID.id);
            for (int i = 0; i < gameID.diff * 5; i++) {
                for (int j = 0; j < gameID.diff * 5; j++) {
                    if (bri[i][j] < low) bri[i][j] = 1;
                    else bri[i][j] = 0;
                    if (bri[i][j] != ans.charAt(i * gameID.diff * 5 + j) - '0') return false;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    private pair getGameID(int diff) {
        return new pair(diff, R.nextInt(gameList.get(diff - 1).size()));
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return message.equals("数织帮助") || message.equals("来局数织") || idMap.containsKey(user_id);
    }

    @Override
    public String help() {
        return "数织游戏：数织帮助";
    }
}
