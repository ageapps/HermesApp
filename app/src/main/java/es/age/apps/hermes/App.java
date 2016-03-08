package es.age.apps.hermes;


import es.age.apps.hermes.activities.RemoteActivity;
import es.age.apps.hermes.core.communication.Bluetooth;
import es.age.apps.hermes.core.communication.Communication;
import es.age.apps.hermes.core.communication.CommunicationMode;
import es.age.apps.hermes.core.remote.R;
import es.age.apps.hermes.core.remote.RepeatTimer;
import es.age.apps.hermes.core.protocol.MultiWii230;
import es.age.apps.hermes.core.protocol.MultirotorData;
import es.age.apps.hermes.core.Utilities.Sensors;
import es.age.apps.hermes.core.Utilities.Utilities;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Locale;

public class App extends Application {

    /////Settings variables/////////
    public String FLIGHTMODE;
    public boolean PROGRESS;
    public CommunicationMode comMode;
    public int RollPitchLimit;
    public int RefreshRate = 40; //TODO ADD TO XML
    public int TrimRoll;
    public int TrimPitch;
    public boolean BLOCKYAW;
    public int ThrottleResolution;
    public int BaudRate = 115200;
    public String SensorFilterAlpha;
    public float Alpha;
    public static String MAC_ADDRES = "00:15:FF:F2:19:5F";

    public boolean PreventExitWhenFlying;
    public Sensors sensors;

    private RemoteActivity remoteActivity;
    public String Aux1Txt;
    public String Aux2Txt;
    public String Aux3Txt;
    public String Aux4Txt;

    public void setRemoteActivity(RemoteActivity remoteActivity) {
        this.remoteActivity = remoteActivity;
    }

    public RemoteActivity getRemoteActivity() {
        return remoteActivity;
    }

    public enum SettingsConstants {
        LOWSIGNALTHRESHOLD(30),
        TEXTTOSPEACH(true),
        PROGRESS(false),
        REFRESHRATE(40), //TODO add xml
        TRIMROLL(0),
        TRIMPITCH(0),
        THROTTLERESOLUTION(10),
        BAUDRATE("115200"),
        AUX1TXT("AUX 1"),
        AUX2TXT("AUX 2"),
        AUX3TXT("AUX 3"),
        AUX4TXT("AUX 4"),
        BLOCKYAW(true),
        USECAMERA(false),
        SENSORFILTERALPHA(0.03f),
        //KEEPSCREENON(true),
        PREVENTEXITWHENFLYING(true),
        ROLLPITCHLIMIT(250),
        FLIGHTMODE("Normal"),
        SSID("");

        private String value;
        private Boolean bValue;
        private Float fValue;
        private Integer iValue;

        SettingsConstants(String value) {
            this.value = value;
        }

        SettingsConstants(boolean value) {
            this(value + "");
            this.bValue = value;
        }

        SettingsConstants(float value) {
            this(value + "");
            this.fValue = value;
        }

        SettingsConstants(int value) {
            this(value + "");
            this.iValue = value;
        }

        public String DefaultS() {
            return value;
        }

        public boolean DefaultB() {
            return bValue;
        }

        public float DefaultF() {
            return fValue;
        }

        public int DefaultI() {
            return iValue;
        }

        public boolean validate(Object newValue) {
            if (bValue != null) { //Object should be a boolean
                return Utilities.IsBoolean(newValue);
            } else if (fValue != null) {
                return Utilities.IsFloat(newValue);
            } else if (iValue != null) {
                return Utilities.IsInteger(newValue);
            }
            return true;
        }
    }

    //////////Constants///////////////////
    private static final String TAG = "App";
    public final static int SENSORSCHANGED = 9;
    public final int BluetoothConnectionStartDelay = 0;


    ///////End Constants//////////////////

    public boolean D = true;
    private boolean ManualMode = true;
    public boolean UsePhoneHeading = false;
    public int WriteRepeatDelayMillis = 40;
    public boolean AuxTextChanged = false;
    public String Status = "";

    /////////////Objects///////////
    //public Sensors sensors;
    private Handler mHandler;


    public Communication commMW;
    public MultirotorData protocol;
    private SharedPreferences prefs;
    private Editor prefsEditor;
    private RepeatTimer signalStrengthTimer = new RepeatTimer(5000);
    /////////End Objects/////////////

    public void SetHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    public void onCreate() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefsEditor = prefs.edit();

        sensors = new Sensors(getApplicationContext(), this);
        Init();


    }

    protected void Init() {
        ReadSettings();
    }

    public void ReadSettings() {
        RefreshRate = Integer.parseInt(prefs.getString(SettingsConstants.REFRESHRATE.toString(), SettingsConstants.REFRESHRATE.DefaultS()));
        PROGRESS = prefs.getBoolean(SettingsConstants.PROGRESS.toString(), SettingsConstants.PROGRESS.DefaultB());
        TrimRoll = Integer.parseInt(prefs.getString(SettingsConstants.TRIMROLL.toString(), SettingsConstants.TRIMROLL.DefaultS()));
        TrimPitch = Integer.parseInt(prefs.getString(SettingsConstants.TRIMPITCH.toString(), SettingsConstants.TRIMPITCH.DefaultS()));
        FLIGHTMODE = prefs.getString(SettingsConstants.FLIGHTMODE.toString(), SettingsConstants.FLIGHTMODE.DefaultS());
        BLOCKYAW = prefs.getBoolean(SettingsConstants.BLOCKYAW.toString(), SettingsConstants.BLOCKYAW.DefaultB());

        ThrottleResolution = getThrottleResolution(FLIGHTMODE);
        RollPitchLimit = getRollPitchLimit(FLIGHTMODE);

        Aux1Txt = prefs.getString(SettingsConstants.AUX1TXT.toString(), SettingsConstants.AUX1TXT.DefaultS());
        Aux2Txt = prefs.getString(SettingsConstants.AUX2TXT.toString(), SettingsConstants.AUX2TXT.DefaultS());
        Aux3Txt = prefs.getString(SettingsConstants.AUX3TXT.toString(), SettingsConstants.AUX3TXT.DefaultS());
        Aux4Txt = prefs.getString(SettingsConstants.AUX4TXT.toString(), SettingsConstants.AUX4TXT.DefaultS());
        AuxTextChanged = true;

        PreventExitWhenFlying = prefs.getBoolean(SettingsConstants.PREVENTEXITWHENFLYING.toString(), SettingsConstants.PREVENTEXITWHENFLYING.DefaultB());
        comMode = CommunicationMode.valueOf(prefs.getString("comMode", "Bluetooth").toUpperCase(Locale.US));
        SensorFilterAlpha = prefs.getString(SettingsConstants.SENSORFILTERALPHA.toString(), SettingsConstants.SENSORFILTERALPHA.DefaultS());
        Alpha = getAlpha(SensorFilterAlpha);
        updateComMode();
    }

    @SuppressLint("NewApi")
    public void SaveSettings() {
        prefsEditor.putBoolean(SettingsConstants.PROGRESS.toString(), PROGRESS);
        prefsEditor.putBoolean(SettingsConstants.BLOCKYAW.toString(), BLOCKYAW);
        prefsEditor.putString(SettingsConstants.REFRESHRATE.toString(), RefreshRate + "");
        prefsEditor.putString(SettingsConstants.TRIMROLL.toString(), TrimRoll + "");
        prefsEditor.putString(SettingsConstants.TRIMPITCH.toString(), TrimPitch + "");
        prefsEditor.putString(SettingsConstants.THROTTLERESOLUTION.toString(), ThrottleResolution + "");

        prefsEditor.putString(SettingsConstants.AUX1TXT.toString(), Aux1Txt);
        prefsEditor.putString(SettingsConstants.AUX2TXT.toString(), Aux2Txt);
        prefsEditor.putString(SettingsConstants.AUX3TXT.toString(), Aux3Txt);
        prefsEditor.putString(SettingsConstants.AUX4TXT.toString(), Aux4Txt);

        prefsEditor.putBoolean(SettingsConstants.PREVENTEXITWHENFLYING.toString(), PreventExitWhenFlying);
        prefsEditor.putString(SettingsConstants.FLIGHTMODE.toString(), FLIGHTMODE + "");
        prefsEditor.putString(SettingsConstants.ROLLPITCHLIMIT.toString(), RollPitchLimit + "");

        prefsEditor.putString(SettingsConstants.SENSORFILTERALPHA.toString(), SensorFilterAlpha + "");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD)
            prefsEditor.apply();
        else
            prefsEditor.commit();
    }

    public void FrequentTasks() {
        if (commMW.Connected) {
            if (signalStrengthTimer.isTime()) {
                signalStrengthTimer.reset();
                int signalStrength = commMW.getStrength();
                if (signalStrength != 0 && signalStrength < Integer.parseInt(SettingsConstants.LOWSIGNALTHRESHOLD.toString())) {
                    Utilities.showToast("Low Signal", remoteActivity);
                }
            }

        }
        //TODO check low phone battery
    }

    protected void updateComMode() {
        if(commMW != null && commMW.getMode() == comMode) return;

        if (commMW != null) commMW.Close();

        switch (comMode) {
            case BLUETOOTH:
                commMW = new Bluetooth(getApplicationContext());
                WriteRepeatDelayMillis = 40;
                break;
            case WIFI:
               /* commMW = new Wifi(getApplicationContext());
                WriteRepeatDelayMillis = 20;
                break;*/
                Log.d(TAG,"IMPOSIBLEEEEEE");
        }
        if (mHandler != null) commMW.SetHandler(mHandler);
        SelectProtocol();
    }
    public void SelectProtocol() {
        if (protocol != null) protocol.stop();

        protocol = new MultiWii230(commMW);
    }

    public boolean getManualMode() {
        return ManualMode;
    }

    public void setManualModeOn(boolean ManualMode) {
        this.ManualMode = ManualMode;
    }

    public void onResume() {
        ReadSettings();
        if(!remoteActivity.isDualJoyStick()) {
            this.sensors.start();
        }
    }

    public void onPause() {
        this.SaveSettings();
        if(!remoteActivity.isDualJoyStick()) {
            this.sensors.stop();
        }
    }

    public void stop() {
        if (protocol != null) protocol.stop();
        if (commMW != null) commMW.Close();

    }

    @Override
    public void onTerminate() {
        stop();
    }

    public int getRollPitchLimit(String flightMode) {
        int limit = 0;

        switch (flightMode) {
            case "Professional":
                limit = 500;
                break;
            case "Normal":
                limit = 250;
                break;
            case "Beginner":
                limit = 200;
        }
        return limit;

    }

    public int getThrottleResolution(String flightMode) {
        int resolution = 0;
        switch (flightMode) {
            case "Professional":
                resolution = 5;
                break;
            case "Normal":
                resolution = 10;
                break;
            case "Beginner":
                resolution = 20;
        }
        return resolution;

    }

    public float getAlpha(String filterMode) {
        float alpha = 0;
        switch (filterMode) {
            case "High":
                alpha = 0.1f;
                break;
            case "Medium":
                alpha = 0.5f;
                break;
            case "Low":
                alpha = 0.7f;
        }
        return alpha;

    }
   /* @Override
    public void onSensorsStateChangeMagAcc() {
        if(mHandler != null) mHandler.sendEmptyMessage(SENSORSCHANGED);
    }
*/

}
