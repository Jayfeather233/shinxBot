package function.randomColor;

import main.Main;
import interfaces.Processable;
import utils.ImageDownloader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class randomColorMain implements Processable {

    String int_to_hex = "0123456789ABCDEF";

    @Override
    public void process(String message_type, String message, long group_id, long user_id, int message_id) {
        int w = 256;
        int h = 256;
        BufferedImage buffImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Random R = new Random();
        int r = R.nextInt(256);
        int g = R.nextInt(256);
        int b = R.nextInt(256);

        int pos;
        message = message.trim();
        if ((pos = message.indexOf("#")) != -1) {
            r = getInt(message, pos + 1);
            g = getInt(message, pos + 3);
            b = getInt(message, pos + 5);
        }
        if (r == -1 || g == -1 || b == -1) {
            Main.setNextSender(message_type, user_id, group_id, "颜色代码错误");
            return;
        }
        String text = "" + int_to_hex.charAt(r / 16) + int_to_hex.charAt(r % 16)
                + int_to_hex.charAt(g / 16) + int_to_hex.charAt(g % 16)
                + int_to_hex.charAt(b / 16) + int_to_hex.charAt(b % 16);
        Graphics2D G = (Graphics2D) buffImg.getGraphics();
        G.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G.setColor(new Color(r, g, b));
        G.fillRect(0, 0, w, h);

        Font f = new Font("Courier New", Font.BOLD, 30);
        Rectangle rec = f.getStringBounds("#" + text, G.getFontMetrics(f).getFontRenderContext()).getBounds();

        if (r + g + b >= 3 * 128) {
            G.setColor(Color.BLACK);
        } else {
            G.setColor(Color.WHITE);
        }
        G.setFont(f);
        G.drawString("#" + text, (w - rec.width) / 2, (h + rec.height) / 2);
        try {
            ImageDownloader.saveImg(buffImg, "png", "./resource/temp/" + text + ".png");
            Main.setNextSender(message_type, user_id, group_id, "[CQ:image,file=file:///" + new File("").getCanonicalPath() + "/resource/temp/" + text + ".png]");
        } catch (IOException e) {
            Main.setNextSender(message_type, user_id, group_id, "图片生成错误，请重试");
        }
    }

    private int getInt(String message, int pos) {
        int u;
        char ch1 = message.charAt(pos);
        char ch2 = message.charAt(pos + 1);
        if ('0' <= ch1 && ch1 <= '9') {
            u = ch1 - '0';
        } else if ('A' <= ch1 && ch1 <= 'F') {
            u = ch1 - 'A' + 10;
        } else if ('a' <= ch1 && ch1 <= 'f') {
            u = ch1 - 'a' + 10;
        } else return -1;

        if ('0' <= ch2 && ch2 <= '9') {
            u = u * 16 + ch2 - '0';
        } else if ('A' <= ch2 && ch2 <= 'F') {
            u = u * 16 + ch2 - 'A' + 10;
        } else if ('a' <= ch2 && ch2 <= 'f') {
            u = u * 16 + ch2 - 'a' + 10;
        } else return -1;
        return u;
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return message.startsWith("来点色图");
    }

    @Override
    public String help() {
        return "随机颜色：来点色图 + [可选:] #12DF3A（颜色代码）";
    }
}
