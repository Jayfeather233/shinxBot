package function.cat;

import com.alibaba.fastjson.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class catColor {
    static ArrayList<Integer> bodyColor = new ArrayList<>();
    static ArrayList<Integer> footColor = new ArrayList<>();
    static ArrayList<Integer> tailTipColor = new ArrayList<>();
    static ArrayList<Integer> eyeColor = new ArrayList<>();
    static ArrayList<Color> colorList = new ArrayList<>();
    static ArrayList<String> colorName = new ArrayList<>();

    public int col;

    public catColor() {
        col = singleCat.R.nextInt(bodyColor.size());
    }
    public catColor(int col){
        this.col=col;
    }

    public catColor(int type, catColor co) {
        if (type == 0) {
            col = singleCat.R.nextInt(footColor.size() + 1);
            if (col == footColor.size()) col = co.col;
            else col = footColor.get(col);
        } else if (type == 1) {
            col = singleCat.R.nextInt(tailTipColor.size() + 1);
            if (col == tailTipColor.size()) col = co.col;
            else col = tailTipColor.get(col);
        } else if (type == 2) {
            col = singleCat.R.nextInt(eyeColor.size());
            col = eyeColor.get(col);
        }
    }

    static void initColor() {
        try {
            File ff = new File("catColor.json");
            if (!ff.exists()) {
                System.out.println("颜色文件不存在");
                return;
            }
            FileReader f = new FileReader("catColor.json");
            Scanner S = new Scanner(f);
            StringBuilder sb = new StringBuilder();
            while (S.hasNext()) {
                sb.append(S.nextLine()).append(' ');
            }

            JSONObject J = JSONObject.parseObject(String.valueOf(sb));
            for (JSONObject Jo : J.getJSONArray("color").toJavaList(JSONObject.class)) {
                colorList.add(new Color(Jo.getInteger("r"), Jo.getInteger("g"), Jo.getInteger("b")));
                colorName.add(Jo.getString("name"));
            }
            bodyColor.addAll(J.getJSONArray("body").toJavaList(Integer.class));
            footColor.addAll(J.getJSONArray("foot").toJavaList(Integer.class));
            tailTipColor.addAll(J.getJSONArray("tail").toJavaList(Integer.class));
            eyeColor.addAll(J.getJSONArray("eye").toJavaList(Integer.class));

            S.close();
            f.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return colorName.get(col);
    }

    public int toInteger(){
        return col;
    }
}
