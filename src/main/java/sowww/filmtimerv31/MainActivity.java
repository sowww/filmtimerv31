package sowww.filmtimerv31;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextWatcher, View.OnFocusChangeListener {

    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String command = intent.getStringExtra(TimerService.EXTRA_COMMAND);

            if (command.equals("SetTime")) {
                int i = intent.getIntExtra(TimerService.EXTRA_SUBTIME, 0);
                int filmLengthSec = intent.getIntExtra(TimerService.EXTRA_FILMLENGTH, 0);

                timeBlock2.setTimeFromSec(filmLengthSec);
                mainTimerBlock.setTimeFromSec(i);
                updateTimerBlocksFromResult();
                blockUI();

            } else if (command.equals("ServiceStops")) {
                unblockUI();
                myLog("ServiceStops");
            }
        }
    }

    class MainTimerBlock {

        TextView tvH, tvM, tvS, tvColon1, tvColon2;

        MainTimerBlock(int idTvH, int idTvM, int idTvS, int idTvColon1, int idTvColon2) {
            tvH = (TextView) findViewById(idTvH);
            tvM = (TextView) findViewById(idTvM);
            tvS = (TextView) findViewById(idTvS);
            tvColon1 = (TextView) findViewById(idTvColon1);
            tvColon2 = (TextView) findViewById(idTvColon2);
        }

        void setTimeFromSec(int sec) {
            int H, M, S;
            S = sec;
            H = S / 3600;
            S = S % 3600;
            M = S / 60;
            S = S % 60;

            tvH.setText(String.format(defaultLocale, "%01d", H));
            tvM.setText(String.format(defaultLocale, "%02d", M));
            tvS.setText(String.format(defaultLocale, "%02d", S));
        }

        void setErrorOn() {
            TextView[] tvs = {tvH, tvM, tvS, tvColon1, tvColon2};
            int color = ContextCompat.getColor(getApplicationContext(), R.color.mySpecialErrorColor);
            for (TextView tv : tvs) {
                tv.setTextColor(color);
            }
        }

        void setErrorOff() {
            TextView[] tvs = {tvH, tvM, tvS, tvColon1, tvColon2};
            int color = ContextCompat.getColor(getApplicationContext(), R.color.mySpecialTextColor);
            for (TextView tv : tvs) {
                tv.setTextColor(color);
            }
        }

        int getH() {
            return parseInt(tvH.getText().toString());
        }

        int getM() {
            return parseInt(tvM.getText().toString());
        }

        int getS() {
            return parseInt(tvS.getText().toString());
        }

        private int parseInt(String s) {
            return Integer.parseInt(s);
        }
    }

    class TimeBlock {

        LinearLayout ll;
        EditText etH, etM, etS;

        TimeBlock(int idLl, int idEtH, int idEtM, int idEtS) {
            ll = (LinearLayout) findViewById(idLl);
            etH = (EditText) findViewById(idEtH);
            etM = (EditText) findViewById(idEtM);
            etS = (EditText) findViewById(idEtS);
        }

        Calendar getTime() {
            Calendar calendar = Calendar.getInstance();
            int H, M, S;
            try {
                H = Integer.parseInt(etH.getText().toString());
            } catch (NumberFormatException e) {
                H = 0;
            }
            try {
                M = Integer.parseInt(etM.getText().toString());
            } catch (NumberFormatException e) {
                M = 0;
            }
            try {
                S = Integer.parseInt(etS.getText().toString());
            } catch (NumberFormatException e) {
                S = 0;
            }
            calendar.set(1900, 0, 0, H, M, S);
            return calendar;
        }

        void setFocusable(boolean b) {
            EditText[] ets = {etH, etM, etS};
            for (EditText et : ets) {
                et.setFocusable(b);
                et.setFocusableInTouchMode(b);
                et.setCursorVisible(b);
            }
        }

        void block() {
            this.setFocusable(false);
        }

        void unblock() {
            this.setFocusable(true);
        }

        int getH() {
            return getTime().get(Calendar.HOUR_OF_DAY);
        }

        int getM() {
            return getTime().get(Calendar.MINUTE);
        }

        int getS() {
            return getTime().get(Calendar.SECOND);
        }

        int getFocusedItem() {
            int i;
            if (etH.isFocused()) {
                i = 1;
            } else if (etM.isFocused()) {
                i = 2;
            } else if (etS.isFocused()) {
                i = 3;
            } else {
                i = 0;
            }
            return i;
        }

        int getHLength() {
            return etH.getText().length();
        }

        int getMLength() {
            return etM.getText().length();
        }

        int getSLength() {
            return etS.getText().length();
        }

        void setFocus(int i) {
            switch (i) {
                case 1:
                    etH.requestFocus();
                    break;
                case 2:
                    etM.requestFocus();
                    break;
                case 3:
                    etS.requestFocus();
                    break;
                default:
                    break;
            }
        }

        void addTimeChangedListener(TextWatcher textWatcher) {
            etH.addTextChangedListener(textWatcher);
            etM.addTextChangedListener(textWatcher);
            etS.addTextChangedListener(textWatcher);
        }

        void setTimeFromSec(int sec) {
            int H, M, S;
            S = sec;
            H = S / 3600;
            S = S % 3600;
            M = S / 60;
            S = S % 60;

            etH.setText(String.format(defaultLocale, "%01d", H));
            etM.setText(String.format(defaultLocale, "%02d", M));
            etS.setText(String.format(defaultLocale, "%02d", S));

        }

        void setBlockOnFocusChangeListener(View.OnFocusChangeListener onFocusChangeListener) {
            etH.setOnFocusChangeListener(onFocusChangeListener);
            etM.setOnFocusChangeListener(onFocusChangeListener);
            etS.setOnFocusChangeListener(onFocusChangeListener);
        }
    }

    RadioGroup.OnCheckedChangeListener occlRg = (radioGroup, i) -> updateTimerBlock();


    //----------------------------------------
    // Declaration of variables
    //----------------------------------------

    final static String STATE_FILMLENGTH = "StateFilmLength";
    final static String STATE_TIMESET = "StateTimeSet";

    final static String LOGS_TAG = "MYLOGS_APP";

    void myLog(String s) {
        Log.d(LOGS_TAG, s);
    }

    void myToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    void vibro(long millis) {
        vibrator.vibrate(millis);
    }

    PowerManager.WakeLock partialWakeLock;
    PowerManager pm;

    SharedPreferences sharedPreferences;
    boolean soundIsOn;
    boolean soundIsTenSeconds;

    Vibrator vibrator;

    Ringtone ringtone;

    TimeBlock timeBlock1, timeBlock2;
    MainTimerBlock mainTimerBlock;

    RadioButton rb1;
    RadioGroup radioGroup;
    LinearLayout ll3zal;

    NotificationManager nm;
    boolean wrongTime;
    boolean timerRunning;

    boolean isTimeUpdated;

    Locale defaultLocale = Locale.ENGLISH;
    Intent serviceIntent;

    MyBroadcastReceiver myBroadcastReceiver;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myLog("APP CREATED!");
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        sharedPreferences = getPreferences(MODE_PRIVATE);
        soundIsOn = sharedPreferences.getBoolean("sound_on", true);
        soundIsTenSeconds = sharedPreferences.getBoolean("sound_ten_sec", true);

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        partialWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My partialWakeLock");

        isTimeUpdated = false;

        ll3zal = (LinearLayout) findViewById(R.id.ll3zal);

        mainTimerBlock = new MainTimerBlock(R.id.tv_H, R.id.tv_M, R.id.tv_S, R.id.tvColon1, R.id.tvColon2);

        timeBlock1 = new TimeBlock(R.id.ll1, R.id.et1_H, R.id.et1_M, R.id.et1_S);
        timeBlock2 = new TimeBlock(R.id.ll2, R.id.et2_H, R.id.et2_M, R.id.et2_S);

        timeBlock1.addTimeChangedListener(this);
        timeBlock2.addTimeChangedListener(this);

        timeBlock1.setBlockOnFocusChangeListener(this);
        timeBlock2.setBlockOnFocusChangeListener(this);

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        rb1 = (RadioButton) findViewById(R.id.rb1);

        radioGroup.setOnCheckedChangeListener(occlRg);
        rb1.setChecked(true);
        wrongTime = false;
        timerRunning = false;

        restoreState(savedInstanceState);
        loadTime();

        IntentFilter intentFilter = new IntentFilter(TimerService.SERVICE_RESPONSE_ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        myBroadcastReceiver = new MyBroadcastReceiver();
        registerReceiver(myBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        MyTime timeSet = new MyTime(timeBlock1.getH(), timeBlock1.getM(), timeBlock1.getS());
        myLog("osi: timeSet     = " + timeSet.toString());
        outState.putInt(STATE_TIMESET, timeSet.toSec());

        MyTime filmLength = new MyTime(timeBlock2.getH(), timeBlock2.getM(), timeBlock2.getS());
        myLog("osi: filmLength  = " + filmLength.toString());
        outState.putInt(STATE_FILMLENGTH, filmLength.toSec());

        saveTime();

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        myLog("App resumed");
        super.onResume();
    }

    @Override
    protected void onStart() {
        loadTime();
        super.onStart();
        myLog("App started");
    }

    @Override
    protected void onPause() {
        super.onPause();
        myLog("App paused");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        myLog("onRestoreInstanceState");
        restoreState(savedInstanceState);
    }

    void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {

            myLog("Restore timers");

            int filmLengthSec = savedInstanceState.getInt(STATE_FILMLENGTH);
            int timeSetSec = savedInstanceState.getInt(STATE_TIMESET);

            timeBlock1.setTimeFromSec(timeSetSec);
            timeBlock2.setTimeFromSec(filmLengthSec);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        myLog("App stopped");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myLog("APP DESTROYED!");
        unregisterReceiver(myBroadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (!wrongTime) sendStartCommand();
                hideKeyboard();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                sendStopCommand();
                soundNotificationStop();
                hideKeyboard();
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    private void sendStartCommand() {
        myLog("sendStartCommand");

        MyTime filmLength = new MyTime(timeBlock2.getH(), timeBlock2.getM(), timeBlock2.getS());
        MyTime endTime = new MyTime(MyTime.now());
        endTime.add(mainTimerBlock.getH(), mainTimerBlock.getM(), mainTimerBlock.getS());

        serviceIntent = new Intent(getApplicationContext(), TimerService.class);

        serviceIntent.putExtra(TimerService.EXTRA_COMMAND, "Start");
        serviceIntent.putExtra(TimerService.EXTRA_FILMLENGTH, filmLength.toSec());
        serviceIntent.putExtra(TimerService.EXTRA_ENDTIME, endTime.toSec());

        startService(serviceIntent);

        myLog("endTime = " + endTime.toString());
        myLog("filmLength = " + filmLength.toString());
    }

    private void sendStopCommand() {
        myLog("sendStopCommand");

        serviceIntent = new Intent(getApplicationContext(), TimerService.class);
        serviceIntent.putExtra(TimerService.EXTRA_COMMAND, "Stop");
        startService(serviceIntent);
    }

    private void saveTime() {
        MyTime timeSet = new MyTime(timeBlock1.getH(), timeBlock1.getM(), timeBlock1.getS());
        MyTime filmLength = new MyTime(timeBlock2.getH(), timeBlock2.getM(), timeBlock2.getS());
        myLog("pref: timeSet     = " + timeSet.toString());
        myLog("pref: filmLength  = " + filmLength.toString());

        sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = sharedPreferences.edit();

        editor.putInt(STATE_TIMESET, timeSet.toSec()).apply();
        editor.putInt(STATE_FILMLENGTH, filmLength.toSec()).apply();
    }

    private void loadTime() {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        int timeSetSec = sharedPreferences.getInt(STATE_TIMESET, 0);
        int filmLengthSec = sharedPreferences.getInt(STATE_FILMLENGTH, 0);

        timeBlock1.setTimeFromSec(timeSetSec);
        timeBlock2.setTimeFromSec(filmLengthSec);

        MyTime mt = new MyTime(timeSetSec);
        MyTime mt2 = new MyTime(filmLengthSec);

        myLog("Time loaded from prefs: " + mt.toString() + " / " + mt2.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, 0, 0, "Звук");
        menu.add(1, 1, 0, "Ограничить 10 секундами");
        menu.setGroupCheckable(1, true, false);
        menu.getItem(0).setChecked(soundIsOn);
        menu.getItem(0).setChecked(soundIsTenSeconds);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setChecked(soundIsOn);
        menu.getItem(1).setChecked(soundIsTenSeconds);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        soundNotificationStop();
        sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = sharedPreferences.edit();
        switch (item.getItemId()) {
            case 0:
                if (soundIsOn) {
                    soundIsOn = false;
                    item.setChecked(false);
                    editor.putBoolean("sound_on", false).apply();
                } else {
                    soundIsOn = true;
                    item.setChecked(true);
                    editor.putBoolean("sound_on", true).apply();
                }
                break;
            case 1:
                if (soundIsTenSeconds) {
                    soundIsTenSeconds = false;
                    item.setChecked(false);
                    editor.putBoolean("sound_ten_sec", false).apply();
                } else {
                    soundIsTenSeconds = true;
                    item.setChecked(true);
                    editor.putBoolean("sound_ten_sec", true).apply();
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        int twoDigits = 0;
        try {
            twoDigits = Integer.parseInt(editable.toString());
        } catch (NumberFormatException e) {
            myLog("BAD!");
        }
        if (twoDigits > 59) {
            editable.clear();
        }

        boolean blockChanged = false;

        if (timeBlock1.getFocusedItem() > 0) {
            int i = timeBlock1.getFocusedItem();
            switch (i) {
                case 1:
                    if (timeBlock1.getHLength() == 1) {
                        timeBlock1.setFocus(2);
                    }
                    break;
                case 2:
                    if (timeBlock1.getMLength() == 2) {
                        timeBlock1.setFocus(3);
                    }
                    break;
                case 3:
                    if (timeBlock1.getSLength() == 2) {
                        timeBlock2.setFocus(1);
                        blockChanged = true;
                    }
                    break;
            }
        }

        if (timeBlock2.getFocusedItem() > 0 && !blockChanged) {
            int i = timeBlock2.getFocusedItem();
            switch (i) {
                case 1:
                    if (timeBlock2.getHLength() == 1) {
                        timeBlock2.setFocus(2);
                    }
                    break;
                case 2:
                    if (timeBlock2.getMLength() == 2) {
                        timeBlock2.setFocus(3);
                    }
                    break;
            }
        }

        updateTimerBlock();
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        EditText et = (EditText) view;
        int i;
        if (!b) {
            try {
                i = Integer.parseInt(et.getText().toString());
            } catch (NumberFormatException e) {
                i = 0;
            }
            et.removeTextChangedListener(this);
            if (et.getId() == R.id.et1_H || et.getId() == R.id.et2_H || et.getId() == R.id.et3_H) {
                et.setText(String.valueOf(i));
            } else et.setText(String.format(defaultLocale, "%02d", i));
            et.addTextChangedListener(this);
        }


    }

    void updateTimerBlock() {
        if (!timerRunning) {
            int sec = calcTimeFromUI();
            if (sec <= 0) {
                mainTimerBlock.setErrorOn();
                wrongTime = true;
            } else {
                mainTimerBlock.setErrorOff();
                wrongTime = false;
            }
            sec = Math.abs(sec);
            mainTimerBlock.setTimeFromSec(sec);
        }
    }

    void updateTimerBlocksFromResult() {
        int h0, m0, s0, so0;
        int h2, m2, s2, so2;
        int rbS;

        h0 = mainTimerBlock.getH();
        m0 = mainTimerBlock.getM();
        s0 = mainTimerBlock.getS();
        so0 = h0 * 3600 + m0 * 60 + s0;

        h2 = timeBlock2.getH();
        m2 = timeBlock2.getM();
        s2 = timeBlock2.getS();
        so2 = h2 * 3600 + m2 * 60 + s2;

        rbS = secFromRadio();

        int so1 = Math.abs(so2 - rbS - so0);
        timeBlock1.setTimeFromSec(so1);
        isTimeUpdated = true;
    }

    int calcTimeFromUI() {
        int H, M, S, rbS;
        int sec;
        rbS = secFromRadio();
        H = timeBlock2.getH() - timeBlock1.getH();
        M = timeBlock2.getM() - timeBlock1.getM();
        S = timeBlock2.getS() - timeBlock1.getS();
        sec = H * 3600 + M * 60 + S - rbS;
        return sec;
    }

    int secFromRadio() {
        int rbS;
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.rb1:
                rbS = 20;
                break;
            case R.id.rb2:
                rbS = 30;
                break;
            case R.id.rb3:
                rbS = 40;
                break;
            default:
                rbS = 20;
                break;
        }
        return rbS;
    }

    void wakePhone() {
        PowerManager.WakeLock wlScreenOn = pm.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK,
                "My WakeUp");
        wlScreenOn.acquire();
        myLog("Screen On!");
        wlScreenOn.release();
    }

    void blockUI() {
        if (!partialWakeLock.isHeld()) {
            partialWakeLock.acquire();
            myLog("WakeLock acquire");
        }

        timeBlock1.block();
        timeBlock2.block();

        radioGroup.getChildAt(0).setVisibility(View.INVISIBLE);
        radioGroup.getChildAt(1).setVisibility(View.INVISIBLE);
        radioGroup.getChildAt(2).setVisibility(View.INVISIBLE);
        findViewById(radioGroup.getCheckedRadioButtonId()).setVisibility(View.VISIBLE);
    }

    void unblockUI() {
        timeBlock1.unblock();
        timeBlock2.unblock();

        radioGroup.getChildAt(0).setVisibility(View.VISIBLE);
        radioGroup.getChildAt(1).setVisibility(View.VISIBLE);
        radioGroup.getChildAt(2).setVisibility(View.VISIBLE);
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (partialWakeLock.isHeld()) {
                partialWakeLock.release();
                myLog("WakeLock release");
            }
        }, 1000);
        nm.cancel(101);
    }

    void soundNotificationStop() {
        if (ringtone != null) ringtone.stop();
    }

    void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
