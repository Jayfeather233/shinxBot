package function.cat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

enum positionName {
    livingRoom, diningRoom, toilet, bedroom, bathroom, balcony, kitchen
}

enum pattern {
    pure, stripe, tigerStripe, threeColor, tortoiseshell
}

class position {
    positionName name;

    //interactive
    public position() {
        nextPosition();
    }

    public void nextPosition() {
        name = positionName.values()[singleCat.R.nextInt(positionName.values().length)];
    }
}

class catAppearance {
    catColor eyeCol, furCol, patternCol, footCol, tailTipCol;
    pattern pa;

    @Override
    public String toString() {
        return  "eyeCol=" + eyeCol +
                ", furCol=" + furCol +
                ", patternCol=" + patternCol +
                ", footCol=" + footCol +
                ", tailTipCol=" + tailTipCol +
                ", pa=" + pa;
    }
}

class state {
    int hungry;
    int thirsty;
    position pos;
}

public class singleCat {
    static Random R = new Random();
    Date birthday;
    state stat;
    catAppearance ap;
    String name;

    public singleCat(String n) {
        name = n;
        birthday = new Date();
        stat = new state();
        stat.hungry = 100;
        stat.thirsty = 100;
        stat.pos = new position();
        ap = new catAppearance();
        ap.furCol = new catColor();
        ap.patternCol = new catColor();
        ap.footCol = new catColor(0, ap.furCol);
        ap.tailTipCol = new catColor(1, ap.furCol);
        ap.eyeCol = new catColor(2, ap.furCol);
        ap.pa = pattern.values()[R.nextInt(pattern.values().length)];
    }

    @Override
    public String toString() {
        return "Name: " + name + "\n" +
                "Birthday: " +  (new SimpleDateFormat("yyyy-MM-dd").format(birthday)) + "\n" +
                "State: " + (stat.hungry==0 ? "" : "Not ") + "hungry, " + (stat.thirsty==0 ? "" : "Not ") + "thirsty\n" +
                "Location: " + stat.pos + "\n" +
                "Appearance: " + ap.toString();
    }
}
