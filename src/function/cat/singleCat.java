package function.cat;

import com.alibaba.fastjson.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import static function.cat.singleCat.R;

enum pattern {
    pure, stripe, tigerStripe, threeColor, tortoiseshell
}

class position {
    positionName name;

    //interactive
    public position() {
        nextPosition();
    }
    public position(String s){
        name = positionName.valueOf(s);
    }

    public void nextPosition() {
        name = positionName.values()[R.nextInt(positionName.values().length)];
    }

    @Override
    public String toString() {
        return name.toString();
    }
}

class catAppearance {
    catColor eyeCol, furCol, patternCol, footCol, tailTipCol;
    pattern pa;

    public catAppearance() {
        this.furCol = new catColor();
        this.patternCol = new catColor();
        this.footCol = new catColor(0, this.furCol);
        this.tailTipCol = new catColor(1, this.furCol);
        this.eyeCol = new catColor(2, this.furCol);
        this.pa = pattern.values()[R.nextInt(pattern.values().length)];
    }

    public catAppearance(JSONObject J){
        this.eyeCol = new catColor(J.getInteger("eyeCol"));
        this.furCol = new catColor(J.getInteger("furCol"));
        this.patternCol = new catColor(J.getInteger("patternCol"));
        this.footCol = new catColor(J.getInteger("footCol"));
        this.tailTipCol = new catColor(J.getInteger("tailTipCol"));
        this.pa = pattern.valueOf(J.getString("pa"));
    }

    @Override
    public String toString() {
        return "eyeCol=" + eyeCol +
                ", furCol=" + furCol +
                ", patternCol=" + patternCol +
                ", footCol=" + footCol +
                ", tailTipCol=" + tailTipCol +
                ", pa=" + pa;
    }

    public JSONObject toJSONObject() {
        JSONObject J = new JSONObject();
        J.put("eyeCol", eyeCol.col);
        J.put("furCol", furCol.col);
        J.put("patternCol", patternCol.col);
        J.put("footCol", footCol.col);
        J.put("tailTipCol", tailTipCol.col);
        J.put("pa", pa);
        return J;
    }
}

class state {
    long hungry;
    long thirsty;
    position pos;

    public state() {
        hungry = 100;
        thirsty = 100;
        pos = new position();
    }
    public state(JSONObject J){
        this.hungry = J.getLong("hungry");
        this.thirsty = J.getLong("thirsty");
        this.pos = new position(J.getString("pos"));
    }

    public JSONObject toJSONObject() {
        JSONObject J = new JSONObject();
        J.put("hungry",this.hungry);
        J.put("thirsty",this.thirsty);
        J.put("pos",this.pos.name);
        return J;
    }
}

class feeding {
    positionName foodPos;
    positionName waterPos;

    long foodAmount;
    long waterAmount;

    public feeding() {
        foodAmount = waterAmount = 0;
        foodPos = waterPos = positionName.livingRoom;
    }
    public feeding(JSONObject J){
        this.foodAmount = J.getLong("foodAmount");
        this.waterAmount = J.getLong("waterAmount");
        this.foodPos = positionName.valueOf(J.getString("foodPos"));
        this.waterPos = positionName.valueOf(J.getString("waterPos"));
    }

    public void refill() {
        this.foodAmount = 700;
        this.waterAmount = 700;
    }

    public JSONObject toJSONObject() {
        JSONObject J = new JSONObject();
        J.put("foodPos", foodPos);
        J.put("waterPos", waterPos);
        J.put("foodAmount", foodAmount);
        J.put("waterAmount", waterAmount);
        return J;
    }
}

public class singleCat {
    static Random R = new Random();
    Date birthday;
    state stat;
    catAppearance ap;
    String name;
    feeding feed;
    Date lastVisitTime;
    boolean gender;
    int affection;//[0,255]

    public singleCat(String n) {
        name = n;
        birthday = new Date();
        lastVisitTime = new Date();
        stat = new state();
        feed = new feeding();
        ap = new catAppearance();
        gender = R.nextInt() % 2 != 0;
        affection = 50;
    }
    public singleCat(JSONObject J){
        try {
            this.birthday = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(J.getString("birthday"));
            this.stat = new state(J.getJSONObject("stat"));
            this.ap = new catAppearance(J.getJSONObject("ap"));
            this.name = J.getString("name");
            this.feed = new feeding(J.getJSONObject("feed"));
            this.lastVisitTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(J.getString("lastVisitTime"));
            this.gender = J.getBoolean("gender");
            this.affection = J.getInteger("affection");

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }

    public void setFeedingPos(int feedingPos) {
        this.feed.foodPos = positionName.values()[feedingPos];
    }

    public void setWaterPos(int waterPos) {
        this.feed.waterPos = positionName.values()[waterPos];
    }

    public String refill() {
        this.feed.refill();
        return "Food and water refilled.";
    }

    public String visit() {
        return visit(positionName.livingRoom);
    }

    public String visit(positionName pos) {
        Date nowTime = new Date();
        long timePass = (nowTime.getTime() - lastVisitTime.getTime()) / 1000;

        long foodNeed = timePass / 864;
        if (foodNeed <= this.feed.foodAmount) {
            this.feed.foodAmount -= foodNeed;
        } else {
            foodNeed -= this.feed.foodAmount;
            this.feed.foodAmount = 0;
            this.stat.hungry -= foodNeed;
        }
        this.stat.hungry += this.feed.foodAmount;
        if (this.stat.hungry > 100) {
            this.feed.foodAmount += this.stat.hungry - 100;
            this.stat.hungry = 100;
        }

        long waterNeed = timePass / 864;
        if (waterNeed <= this.feed.waterAmount) {
            this.feed.waterAmount -= waterNeed;
        } else {
            waterNeed -= this.feed.waterAmount;
            this.feed.waterAmount = 0;
            this.stat.thirsty -= waterNeed;
        }
        this.stat.thirsty += this.feed.waterAmount;
        if (this.stat.thirsty > 100) {
            this.feed.waterAmount += this.stat.thirsty - 100;
            this.stat.thirsty = 100;
        }
        if (timePass >= 60) this.stat.pos.nextPosition();
        lastVisitTime = nowTime;

        return positionText.getText(this, pos);
    }

    @Override
    public String toString() {
        return "Name: " + name + "\n" +
                "Birthday: " + (new SimpleDateFormat("yyyy-MM-dd").format(birthday)) + "\n" +
                "State: " + (stat.hungry <= 0 ? "" : "Not ") + "hungry, " + (stat.thirsty <= 0 ? "" : "Not ") + "thirsty\n" +
                "Location: " + stat.pos + "\n" +
                "Appearance: " + ap.toString();
    }

    public JSONObject toJSONObject() {
        JSONObject J = new JSONObject();
        J.put("birthday", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(birthday)));
        J.put("stat", this.stat.toJSONObject());
        J.put("ap", this.ap.toJSONObject());
        J.put("name", this.name);
        J.put("feed", this.feed.toJSONObject());
        J.put("lastVisitTime", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastVisitTime)));
        J.put("gender", gender);
        J.put("affection", affection);
        return J;
    }
}
