package sowww.filmtimerv31;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class TimerService extends Service {

    private class MyTimer {

        MyTime lastTime;

        private class MyTimerTask extends TimerTask {

            int lastNotifiedTime = 0;

            private Boolean shouldUpdateNotify() {
                String string = subTime.toString();
                String lastChar = String.valueOf(string.charAt(string.length() - 1));
                Boolean result = false;
                if ((lastChar.equals("0") || lastChar.equals("5")) && (lastNotifiedTime != subTime.toSec())) {
                    result = true;
                    lastNotifiedTime = subTime.toSec();
                }
                return result;
            }

            @Override
            public void run() {
                nowTime.setNow();

                if (endTime.isNextDay() && (lastTime.toSec() > nowTime.toSec())) {
                    endTime.add(-86400);
                }

                if ((timerIsRunning) && (endTime.toSec() > nowTime.toSec())) {
                    int sub = endTime.toSec() - nowTime.toSec();
                    subTime = new MyTime();
                    subTime.add(Math.abs(sub));
                    if (shouldUpdateNotify()) {
                        sendNotification(subTime.toString());
                    }
                    sendTimeToMainApp(subTime.toSec());
                } else {
                    sendNotification("Время вышло!");
                    sendTimeToMainApp(1);
                    ends();
                }
                lastTime.set(nowTime);
            }
        }

        MyTime startTime, nowTime, endTime, subTime;

        MyTimerTask myTimerTask;

        MyTimer() {
            startTime = new MyTime();
            nowTime = new MyTime();
            endTime = new MyTime();
            lastTime = new MyTime();
            subTime = new MyTime();

            myTimerTask = new MyTimerTask();
            MyTime mt = new MyTime();
        }

        void start(MyTime inEndTime) {
            if (!timerIsRunning) {

                vibro(70);

                timerIsRunning = true;
                myLog("Starting Timer");
                startTime = MyTime.now();
                lastTime = MyTime.now();
                nowTime = MyTime.now();

                endTime.set(inEndTime);
                int sub = endTime.toSec() - nowTime.toSec();
                subTime = new MyTime();
                subTime.add(Math.abs(sub));
                sendNotification(subTime.toString());
                sendTimeToMainApp(subTime.toSec());

                timer = new Timer();
                timer.schedule(myTimerTask, 0, 500);
            }
        }

        private void ends() {
            myLog("Timer ENDS!");

            if (timerIsRunning) {
                myLog("Timer stop");
                timer.cancel();
                timerIsRunning = false;
                cycleIsOver = true;
            }

            soundNotificationStart();
            Handler handler = new Handler(getMainLooper());
            handler.postDelayed(() -> soundNotificationStop(), 1000 * 10);

            vibro(500);

            handlerStopper.postDelayed(runnableStopper, 10 * 1000);

        }

        void stop() {

            if (timerIsRunning) {
                myLog("Timer stop");
                timer.cancel();
                timerIsRunning = false;
                vibro(70);
            }

            handlerStopper.removeCallbacks(runnableStopper);

            soundNotificationStop();
            stopForeground(true);
            stopSelf();
        }
    }

    void vibro(long millis) {
        vibrator.vibrate(millis);
    }

    private static final String LOGS_TAG = "MYLOGS_SERVICE";

    void myLog(String s) {
        Log.d(LOGS_TAG, s);
    }

    private Intent responseIntent;

    Vibrator vibrator;
    Ringtone ringtone;

    Boolean soundIsOn;
    private Boolean cycleIsOver;
    private Boolean timerIsRunning;

    private MyTime endTime, filmLength;


    private MyTimer myTimer;
    private Timer timer;

    Handler handlerStopper;
    Runnable runnableStopper;

    public static final String SERVICE_RESPONSE_ACTION = "sowww.filmtimerv31.timerservice.RESPONSE";
    public static final String EXTRA_ENDTIME = "EndTime";
    public static final String EXTRA_COMMAND = "Command";
    public static final String EXTRA_SUBTIME = "SubTime";
    public static final String EXTRA_FILMLENGTH = "FilmLength";

    public static final int NOTIFICATION_ID = 556;

    Notification notification;

    @Override
    public void onCreate() {
        super.onCreate();
        myLog("CREATED!");
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        timerIsRunning = false;
        handlerStopper = new Handler(getMainLooper());
        runnableStopper = () -> myTimer.stop();
        timer = new Timer();
        cycleIsOver = false;
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), alarmUri);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //if (myTimer.getIsRunning()) { myTimer.stop(); }
        sendStopsMessage();
        myLog("SERVICE DESTROYED");
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent inputIntent, int flags, int startId) {
        myTimer = new MyTimer();
        endTime = new MyTime();
        filmLength = new MyTime();

        soundIsOn = false;

        String command = getCommandFromIntent(inputIntent);

        if (command.equals("Start") && !cycleIsOver) {
            endTime = getEndTimeFromIntent(inputIntent);
            myLog("!!! " + endTime.isNextDay().toString());
            filmLength = getFilmLengthFromIntent(inputIntent);
            myTimer.start(endTime);
        } else if (command.equals("Stop")) {
            myTimer.stop();
            soundNotificationStop();
        }

        return Service.START_STICKY;
    }

    private MyTime getFilmLengthFromIntent(Intent inputIntent) {
        int filmLengthSec = inputIntent.getIntExtra(EXTRA_FILMLENGTH, 0);
        MyTime myTimeFromIntent;
        myTimeFromIntent = new MyTime();
        myTimeFromIntent.setFromSec(filmLengthSec);
        return myTimeFromIntent;
    }

    private void sendNotification(String string) {
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.my_timer_icon)
                .setWhen(System.currentTimeMillis())
                .setContentText("Продолжительность фильма: " + filmLength.toString())
                .setContentTitle(string)
                .setContentIntent(contentIntent)
                .build();
        startForeground(NOTIFICATION_ID, notification);
        myLog("Notification updated: " + string);
    }

    private MyTime getEndTimeFromIntent(Intent inputIntent) {
        int endTimeSec = inputIntent.getIntExtra(EXTRA_ENDTIME, 0);
        MyTime myTimeFromIntent;
        myTimeFromIntent = new MyTime();
        myTimeFromIntent.setFromSec(endTimeSec);

        myLog("endTimeSec: " + endTimeSec);
        myLog("getEndTime: " + myTimeFromIntent.toString());
        return myTimeFromIntent;
    }

    private String getCommandFromIntent(Intent inputIntent) {
        String string = inputIntent.getStringExtra(EXTRA_COMMAND);
        myLog("getCommand: " + string);
        return string;
    }

    private void sendTimeToMainApp(int i) {
        responseIntent = new Intent();
        responseIntent.setAction(SERVICE_RESPONSE_ACTION);
        responseIntent.addCategory(Intent.CATEGORY_DEFAULT);
        responseIntent.putExtra(EXTRA_COMMAND, "SetTime");
        responseIntent.putExtra(EXTRA_SUBTIME, i);
        responseIntent.putExtra(EXTRA_FILMLENGTH, filmLength.toSec());
        sendBroadcast(responseIntent);
    }

    private void sendStopsMessage() {
        responseIntent = new Intent();
        responseIntent.setAction(SERVICE_RESPONSE_ACTION);
        responseIntent.addCategory(Intent.CATEGORY_DEFAULT);
        responseIntent.putExtra(EXTRA_COMMAND, "ServiceStops");
        sendBroadcast(responseIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void soundNotificationStart() {
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), alarmUri);
        if (!ringtone.isPlaying()) {
            try {
                ringtone.play();
            } catch (Exception e) {
                myLog("Sound error");
            }
        }
    }

    void soundNotificationStop() {
        myLog("soundNotificationStop()");
        if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
    }
}
