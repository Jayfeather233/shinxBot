package utils;

import function.imageGenerator.ImageGeneratorMain;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

public class ImageDownloader {
    public static void download(String httpAddress, String filePath, String fileName) {
        try {
            URL url1 = new URL(httpAddress);
            URLConnection uc = url1.openConnection();
            InputStream inputStream = uc.getInputStream();
            File f = new File("./" + filePath + '/' + fileName);
            if (!f.exists()) {
                FileOutputStream out;
                try {
                    out = new FileOutputStream(f);
                } catch (FileNotFoundException e) {
                    new File("./" + filePath).mkdirs();
                    out = new FileOutputStream(f);
                }
                int j;
                while ((j = inputStream.read()) != -1) {
                    out.write(j);
                }
                out.flush();
                out.close();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addRandomNoise(String filePath, String ext) {
        BufferedImage buffImg;
        try {
            File f = new File("./" + filePath);
            buffImg = ImageIO.read(f);
            int w = buffImg.getWidth();
            int h = buffImg.getHeight();
            Color[][] imgColor = ImageGeneratorMain.getImagePixArray(buffImg);
            Random R = new Random();
            int a, r, g, b;
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    a = imgColor[i][j].getAlpha();
                    r = imgColor[i][j].getRed();
                    g = imgColor[i][j].getGreen();
                    b = imgColor[i][j].getBlue();
                    r += R.nextInt(4) - 2;
                    g += R.nextInt(4) - 2;
                    b += R.nextInt(4) - 2;
                    r = Math.max(r, 0);
                    g = Math.max(g, 0);
                    b = Math.max(b, 0);
                    r = Math.min(r, 255);
                    g = Math.min(g, 255);
                    b = Math.min(b, 255);
                    buffImg.setRGB(i,j, new Color(r,g,b,a).getRGB());
                }
            }
            ImageIO.write(buffImg, ext, f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean saveImg(BufferedImage buffImg, String ext, String path) throws IOException {
        File f = new File(path);
        if(!f.mkdirs()) return false;
        return ImageIO.write(buffImg,ext,f);
    }
}
