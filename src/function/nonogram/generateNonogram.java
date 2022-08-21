package function.nonogram;

import function.imageGenerator.ImageGeneratorMain;
import httpconnect.ImageDownloader;
import main.Main;
import main.Processable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

class pairx {
    int diff, id;

    public pairx(int a, int b) {
        this.diff = a;
        this.id = b;
    }
}

public class generateNonogram implements Processable {

    public static void main(String[] args) {
        String file;
        Scanner S = new Scanner(System.in);
        file = S.nextLine();
        pairx gameID = new pairx(S.nextInt(), S.nextInt());
        System.out.println(detect(file, gameID));
    }

    public static String detect(String file, pairx gameID) {
        StringBuilder sb = new StringBuilder();
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
                    sb.append(bri[i][j]).append(' ');
                    cnt[bri[i][j]]++;
                }
                sb.append('\n');
            }

            int minn = 255, maxn = 0;
            for (int i = 0; i < 256; i++) {
                if (cnt[i] != 0) {
                    minn = Math.min(minn, i);
                    maxn = Math.max(maxn, i);
                }
            }
            int low = Math.max(maxn - 8, maxn - (maxn - minn + 4) / 5);

            for (int i = 0; i < gameID.diff * 5; i++) {
                for (int j = 0; j < gameID.diff * 5; j++) {
                    if (bri[i][j] < low) bri[i][j] = 1;
                    else bri[i][j] = 0;
                    sb.append(bri[i][j]).append(' ');
                }
                sb.append('\n');
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    @Override
    public void process(String message_type, String message, long group_id, long user_id, int message_id) {
        message = message.substring(4);
        int diff = toInt(message);
        String fileName = "", url;
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
        Main.setNextSender(message_type, user_id, group_id, detect("resource/download/" + fileName, new pairx(diff, 0)));
    }

    @Override
    public boolean check(String message_type, String message, long group_id, long user_id) {
        return message.startsWith("数织检测");
    }

    @Override
    public String help() {
        return null;
    }

    private boolean isDigit(char s) {
        return '0' <= s && s <= '9';
    }

    private int toInt(String s) {
        int u = 0;
        boolean flg = false;
        for (int i = 0; i < s.length(); i++) {
            if (!isDigit(s.charAt(i))) {
                if (flg) return u;
                else {
                    flg = true;
                    while (!isDigit(s.charAt(i))) i++;
                    i--;
                }
            } else {
                u = u * 10 + s.charAt(i) - '0';
            }
        }
        return u;
    }
}
