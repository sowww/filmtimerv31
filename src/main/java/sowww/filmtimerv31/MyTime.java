package sowww.filmtimerv31;

import java.util.Calendar;
import java.util.Locale;

public class MyTime {

    private int h;
    private int m;
    private int s;

    private Boolean nextDay;

    MyTime() {
        h = 0;
        m = 0;
        s = 0;
        nextDay = false;
    }

    MyTime(MyTime inputTime) {
        this();
        h = inputTime.getH();
        m = inputTime.getM();
        s = inputTime.getS();
        nextDay = inputTime.isNextDay();
    }

    MyTime(int sec) {
        this();
        setFromSec(sec);
    }

    MyTime(int inH, int inM, int inS) {
        this();
        h = inH;
        m = inM;
        s = inS;
    }

    void set (MyTime inputTime) {
        h = inputTime.getH();
        m = inputTime.getM();
        s = inputTime.getS();
        nextDay = inputTime.isNextDay();
    }

    void setNow() {
        MyTime time = MyTime.now();
        h = time.getH();
        m = time.getM();
        s = time.getS();
    }

    void setH(int i) { h = i; }
    void setM(int i) { m = i; }
    void setS(int i) { s = i; }

    void set(int inH, int inM, int inS) {
        h = inH;
        m = inM;
        s = inS;
    }

    int getH() { return h; }
    int getM() { return m; }
    int getS() { return s; }

    Boolean isNextDay() { return nextDay; }

    int toSec() {
        int daySec = 0;
        if (nextDay) { daySec += 86400; }
        return h*3600 + m*60 + s + daySec;
    }

    void setFromSec(int sec) {
        s = sec;
        if ( s >= 86400 ) {
            s -= 86400;
            nextDay = true;
        } else { nextDay = false; }
        h = s / 3600;
        s = s % 3600;
        m = s / 60;
        s = s % 60;
    }

    public String toString() { return String.format(Locale.ENGLISH,"%01d:%02d:%02d", h, m, s); }

    void add(MyTime inputTime) {
        int secSum = toSec() + inputTime.toSec();
        setFromSec(secSum);
    }

    void add(int addH, int addM, int addS) {
        MyTime addTime = new MyTime(addH, addM, addS);
        add(addTime);
    }

    void add(int i) {
        int secSum = toSec() + i;
        setFromSec(secSum);
    }

    void substract(MyTime inputTime) {
        int secSum = Math.abs(this.toSec() - inputTime.toSec());
        this.setFromSec(secSum);
    }

    static MyTime now() {
        MyTime result = new MyTime();
        Calendar cal;
        cal = Calendar.getInstance();
        result.setH(cal.get(Calendar.HOUR_OF_DAY));
        result.setM(cal.get(Calendar.MINUTE));
        result.setS(cal.get(Calendar.SECOND));

        return result;
    }

}