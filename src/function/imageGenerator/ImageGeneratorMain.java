package function.imageGenerator;

import utils.ImageDownloader;
import main.Main;
import interfaces.Processable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static main.Main.localPath;

public class ImageGeneratorMain implements Processable {

    @Override
    public void process(String message_type, String message, long group_id, long user_id, int message_id) {
        String fileName = "";
        for (int i = 2; i < message.length(); i++) {
            if (message.charAt(i) != ' ') {
                message = message.substring(i);
                break;
            }
        }
        int u;
        long uin = 0;
        if ((u = message.indexOf("[CQ:at,qq=")) != -1) {
            message = message.substring(u + 10);
            for (int i = 0; i < message.length(); i++) {
                if (message.charAt(i) == ']') break;
                uin = uin * 10 + message.charAt(i) - '0';
            }
        } else if (message.contains("[CQ:image,")) {
            u = message.indexOf(",file=");
            u += 6;
            for (int i = u; i < message.length(); i++) {
                if (message.charAt(i) == '.') {
                    fileName = message.substring(u, i);
                    break;
                }
            }

            u = message.indexOf(",url=");
            message = message.substring(u + 5);
            for (int i = 0; i < message.length(); i++) {
                if (message.charAt(i) == ']') {
                    message = message.substring(0, i);
                    break;
                }
            }
        } else {
            uin = Long.parseLong(message);
        }

        if (uin != 0) {
            fileName = Long.toString(uin);
            fileName = fileName + ".png";
            message = "http://q1.qlogo.cn/g?b=qq&nk=" + uin + "&s=160";
        } else {
            fileName = fileName + ".png";
        }
        File file = new File("./resource/download/" + fileName);
        if (!file.exists())
            ImageDownloader.download(message, "resource/download", fileName);

        File outFile = new File("./resource/generate/" + fileName);
        if (!outFile.exists()) {
            try {
                BufferedImage buffImg = ImageIO.read(file);
                int w = buffImg.getWidth();
                int h = buffImg.getHeight();
                double k = 720.0 / w;
                if (370 < h * k) k = 370.0 / h;
                int i1 = Double.valueOf(w * k).intValue();
                int i2 = Double.valueOf(h * k).intValue();
                Image scaledInstance = buffImg.getScaledInstance(i1, i2, Image.SCALE_AREA_AVERAGING);
                BufferedImage scaledImg = new BufferedImage(i1, i2, BufferedImage.TYPE_INT_ARGB);
                scaledImg.getGraphics().drawImage(scaledInstance, 0, 0, null);

                int dw = 840 + (int) ((720 - w * k) / 2);
                int dh = 90 + (int) ((370 - h * k) / 2);
                file = new File("./resource/local/vaporeon_background.jpg");
                buffImg = ImageIO.read(file);
                file = new File("./resource/local/vaporeon_background_mask.jpg");

                generate(buffImg, ImageIO.read(file), scaledImg, dw, dh);

                try {
                    ImageIO.write(buffImg, "png", outFile);
                } catch (IOException e) {
                    new File("./resource/generate/").mkdirs();
                    ImageIO.write(buffImg, "png", outFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //840 90
            //1560 460
        }
        Main.setNextSender(message_type, user_id, group_id, "[CQ:image,file=file:///" + localPath + "/resource/generate/" + fileName + "]");
        Main.setNextLog("ImgGenerator at group " + group_id + " by " + user_id, 0);
    }

    private static void generate(BufferedImage original, BufferedImage mask, BufferedImage scaled, int dx, int dy) {
        Color[][] originalArr = getImagePixArray(original);
        Color[][] maskArr = getImagePixArray(mask);
        Color[][] scaledArr = getImagePixArray(scaled);

        for (int i = 0; i < scaledArr.length; i++) {
            for (int j = 0; j < scaledArr[i].length; j++) {
                Color u = scaledArr[i][j];
                Color v = maskArr[i + dx][j + dy];
                Color w = originalArr[i + dx][j + dy];

                Color s = new Color(
                        (int) (u.getRed() * (u.getAlpha() / 255.0) + w.getRed() * (1 - u.getAlpha() / 255.0)),
                        (int) (u.getGreen() * (u.getAlpha() / 255.0) + w.getGreen() * (1 - u.getAlpha() / 255.0)),
                        (int) (u.getBlue() * (u.getAlpha() / 255.0) + w.getBlue() * (1 - u.getAlpha() / 255.0))
                );

                s = new Color(
                        (int) (s.getRed() * (v.getRed() / 255.0) + w.getRed() * (1 - v.getRed() / 255.0)),
                        (int) (s.getGreen() * (v.getGreen() / 255.0) + w.getGreen() * (1 - v.getGreen() / 255.0)),
                        (int) (s.getBlue() * (v.getBlue() / 255.0) + w.getBlue() * (1 - v.getBlue() / 255.0))
                );
                original.setRGB(i + dx, j + dy, s.getRGB());
            }
        }
    }

    public static Color[][] getImagePixArray(BufferedImage buffImg) {
        // 获取图片尺寸
        int w = buffImg.getWidth();
        int h = buffImg.getHeight();

        Color[][] imgArr = new Color[w][h];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                imgArr[i][j] = new Color(buffImg.getRGB(i, j), true);
            }
        }

        return imgArr;
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return message.startsWith("想要");
    }

    @Override
    public String help() {
        return "生成水布想要的图片： 想要+[图片]";
    }
}
