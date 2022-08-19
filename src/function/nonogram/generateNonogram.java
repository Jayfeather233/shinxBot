package function.nonogram;

import function.imageGenerator.ImageGeneratorMain;

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

public class generateNonogram {

    public static void main(String[] args) {
        String file;
        Scanner S = new Scanner(System.in);
        file = S.nextLine();
        pairx gameID = new pairx(S.nextInt(), S.nextInt());
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

            //System.out.printf("dx:%d, dy:%d, sizeX=%d, sizeY=%d",dx,dy,blockSizeX,blockSizeY);

            for (int i = 0; i < gameID.diff * 5; i++) {
                for (int j = 0; j < gameID.diff * 5; j++) {
                    int tr = 0, cntx = 0;
                    //System.out.printf("%d %d\n",i,j);
                    //System.out.println("x: from " + ((i + gameID.diff) * blockSizeX + 5) + " to "+ ((i + gameID.diff + 1) * blockSizeX + 5));
                    //System.out.println("y: from " + ((j + gameID.diff) * blockSizeY + 5) + " to "+ ((j + gameID.diff + 1) * blockSizeY + 5));
                    for (int ii = (i + gameID.diff) * blockSizeX + dx + blockSizeX / 8; ii < (i + gameID.diff + 1) * blockSizeX + dx - blockSizeX / 8; ii++)
                        for (int jj = (j + gameID.diff) * blockSizeY + dy + blockSizeY / 8; jj < (j + gameID.diff + 1) * blockSizeY + dy - blockSizeY / 8; jj++) {
                            if (jj < imgColor.length && ii < imgColor[jj].length) {
                                tr += imgColor[jj][ii].getRed() + imgColor[jj][ii].getGreen() + imgColor[jj][ii].getBlue();
                                cntx++;
                            }

                        }
                    bri[i][j] = Math.min(255, tr / (3 * cntx));
                    //System.out.print(bri[i][j] + " ");
                    cnt[bri[i][j]]++;
                }
                //System.out.println("");
            }

            int minn = 255, maxn = 0;
            for (int i = 0; i < 256; i++) {
                if (cnt[i] != 0) {
                    minn = Math.min(minn, i);
                    maxn = Math.max(maxn, i);
                }
                //System.out.printf("%d %d %d\n", cnt[i], minn, maxn);
            }
            int low = maxn - 20;

            for (int i = 0; i < gameID.diff * 5; i++) {
                for (int j = 0; j < gameID.diff * 5; j++) {
                    if (bri[i][j] < low) bri[i][j] = 1;
                    else bri[i][j] = 0;
                    System.out.print(bri[i][j]);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
