package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Random;

public class saveImg {
    static Random R = new Random();

    public static byte[] base64ToByte(String b64) {
        byte[] b = null;
        Base64.Decoder decoder = Base64.getDecoder();
        try {
            if (b64.contains("data:image/png;base64,")) {
                b = decoder.decode(b64.replace("data:image/png;base64,", ""));
            } else if (b64.contains("data:image/jpeg;base64,")) {
                b = decoder.decode(b64.replace("data:image/jpeg;base64,", ""));
            } else return null;
            for (int i = 0; i < b.length; i++) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return b;
    }

    public static String saveBase64Img(String base64Code) {
        byte[] b64 = base64ToByte(base64Code);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String filename;
            if (base64Code.contains("jpeg")) {
                filename = sdf.format(new Date()) + R.nextInt(1000) + ".jpeg";
            } else if (base64Code.contains("png")) {
                filename = sdf.format(new Date()) + R.nextInt(1000) + ".png";
            } else return null;
            File dic = new File("./resource/sd/");
            if (!dic.exists()) {
                dic.mkdirs();
            }
            File file = new File(dic + "/" + filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            OutputStream out = new FileOutputStream(dic + "/" + filename);
            out.write(b64);
            out.flush();
            out.close();
            return dic + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
