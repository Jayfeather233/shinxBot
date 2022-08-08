package httpconnect;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class ImageDownloader {
    public static void download(String httpAddress, String filePath, String fileName) {
        try {
            URL url1 = new URL(httpAddress);
            URLConnection uc = url1.openConnection();
            InputStream inputStream = uc.getInputStream();
            FileOutputStream out;
            try {
                out = new FileOutputStream("./" + filePath + '/' + fileName);
            } catch (FileNotFoundException e) {
                new File("./" + filePath).mkdirs();
                out = new FileOutputStream("./" + filePath + '/' + fileName);
            }
            int j;
            while ((j = inputStream.read()) != -1) {
                out.write(j);
            }
            inputStream.close();
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
