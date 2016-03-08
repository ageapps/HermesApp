package es.age.apps.hermes.activities;


import es.age.apps.hermes.App;
import es.age.apps.hermes.core.Utilities.Utilities;
import es.age.apps.hermes.core.communication.DeviceListActivity;
import es.age.apps.hermes.core.remote.RemoteActivityCommunicationHandler;
import es.age.apps.hermes.core.remote.RemoteActivityEvents;
import es.age.apps.hermes.core.remote.R;
import es.age.apps.hermes.core.remote.RCSignals;
import es.age.apps.hermes.ui.joystick.DualJoystickView;
import es.age.apps.hermes.ui.joystick.JoystickView;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class RemoteActivity extends BaseActivity {
    private boolean dualJoyStick = false;
    private long lastRequestTime = 0;
    static final int minRC = 1000;
    public static String GAMEACTIVITY_EXTRA = RemoteActivity.class.getName();
    public Typeface type;

    private enum InputMode {
        ACCELEROMETER, TOUCH
    }

    // Sensors
    private float I[] = new float[16];
    private float Rr[] = new float[16];
    static boolean sensorAvailable = false;
    private static float ALPHA = 0.02f;
    // Orientation values
    private float orientation[] = new float[3];
    public float pitch = 0.f;
    public int heading = 0;
    public float roll = 0.f;
    private float minValue = 0;
    private float maxValue = 0;
    private boolean isArmed = false;

    // Sensor objects
    static SensorManager mSensorManager;
    static SensorEventListener sensorEventListener;
    static Sensor accelerometer;
    static Sensor magnetometer;
    static Sensor gravity;


    private ProgressBar rcStickThrottleSlider, rcStickRollSlider, rcStickPitchSlider, rcStickYawSlider, rcStickAUX1Slider, rcStickAUX2Slider, rcStickAUX3Slider, rcStickAUX4Slider;
    private TextView txtV_th, txtV_p, txtV_r, txtV_y, txtV_a1, txtV_a2, txtV_a3, txtV_a4, txt_adress;
    private Button butt_connect_bt;
    private Vibrator vibrator;
    private JoystickView joystick;
    private ImageView arrowL, arrowR, arrowT, arrowB;
    private static final double MAX_VALUE_Y = 0.3;
    private static final double MAX_VALUE_X = 0.3;
    private String address;            // MAC-address


    private InputMode inputMode = InputMode.TOUCH;
    private DualJoystickView dualJoystickView;
    private ToggleButton auxBtn[] = new ToggleButton[4];
    private byte auxBtnid[] = {RCSignals.AUX1, RCSignals.AUX2, RCSignals.AUX3, RCSignals.AUX4};
    private TextView txtStatus;
    private long lastHeadingRefreshTime = 0;
    private int thisHeading = 0;
    private int mwcHeading = 0;
    private int lastValidateHeading = 0;
    private String debugTextTemplate = "%sPhone Heading: %d\nMWC Heading:%d\nDelay: %dms";
    private long delayTime = 0;
    private long[] delayTimeRaw = new long[5];
    private boolean isDelayTimeAvailable = false;
    private int delayTimeIdx = 0;
    private String status = "";

    public RCSignals rc;
    RemoteActivityEvents mEvents;
    private RemoteActivityCommunicationHandler mHandler;

    private final int BLUETOOTH_SEARCH_RETURN = 1;
    private final int SETTINGS_MODIFY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dualJoyStick = getIntent().getBooleanExtra(GAMEACTIVITY_EXTRA, false);

        if (dualJoyStick) {
            setContentView(R.layout.dual_joystick_activity);
            inputMode = InputMode.TOUCH;
            //DualJoystick
            dualJoystickView = (DualJoystickView) findViewById(R.id.DualJoystickView);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setContentView(R.layout.one_hand_activity);
            inputMode = InputMode.ACCELEROMETER;
            //Arrows
            arrowB = (ImageView) findViewById(R.id.arrow_bottom);
            arrowL = (ImageView) findViewById(R.id.arrow_left);
            arrowR = (ImageView) findViewById(R.id.arrow_right);
            arrowT = (ImageView) findViewById(R.id.arrow_top);
            //Joystick
            joystick = (JoystickView) findViewById(R.id.joystick);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            initSensor();

        }

        butt_connect_bt = (Button) findViewById(R.id.butt_connect);
        butt_connect_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Connect();
            }
        });

        //Progress Bars
        rcStickThrottleSlider = (ProgressBar) findViewById(R.id.progress_th);
        rcStickRollSlider = (ProgressBar) findViewById(R.id.progress_r);
        rcStickPitchSlider = (ProgressBar) findViewById(R.id.progress_p);
        rcStickYawSlider = (ProgressBar) findViewById(R.id.progress_y);
        rcStickAUX1Slider = (ProgressBar) findViewById(R.id.progress_a1);
        rcStickAUX2Slider = (ProgressBar) findViewById(R.id.progress_a2);
        rcStickAUX3Slider = (ProgressBar) findViewById(R.id.progress_a3);
        rcStickAUX4Slider = (ProgressBar) findViewById(R.id.progress_a4);

        //Progress Text
        type = Typeface.createFromAsset(getAssets(), "fonts/airstrike.ttf");
        txtV_th = (TextView) findViewById(R.id.txt_th);
        txtV_p = (TextView) findViewById(R.id.txt_p);
        txtV_r = (TextView) findViewById(R.id.txt_r);
        txtV_y = (TextView) findViewById(R.id.txt_y);
        txtV_a1 = (TextView) findViewById(R.id.txt_a1);
        txtV_a2 = (TextView) findViewById(R.id.txt_a2);
        txtV_a3 = (TextView) findViewById(R.id.txt_a3);
        txtV_a4 = (TextView) findViewById(R.id.txt_a4);
        txt_adress = (TextView) findViewById(R.id.bt_device);

        txtV_th.setTypeface(type);
        txtV_p.setTypeface(type);
        txtV_r.setTypeface(type);
        txtV_y.setTypeface(type);
        txtV_a1.setTypeface(type);
        txtV_a2.setTypeface(type);
        txtV_a3.setTypeface(type);
        txtV_a4.setTypeface(type);
        txt_adress.setTypeface(type);

        //TypeFace to Toggle Buttons
        ((ToggleButton) findViewById(R.id.aux1Btn)).setTypeface(type);
        ((ToggleButton) findViewById(R.id.aux2Btn)).setTypeface(type);
        ((ToggleButton) findViewById(R.id.aux3Btn)).setTypeface(type);
        ((ToggleButton) findViewById(R.id.aux4Btn)).setTypeface(type);


        address = App.MAC_ADDRES;
        txt_adress.setText(address);


        KeepScreenOn(true);

        rc = new RCSignals();
        mEvents = new RemoteActivityEvents(this);
        mHandler = new RemoteActivityCommunicationHandler(this);


        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);


        txtStatus = (TextView) findViewById(R.id.status);
        for (int x = 0; x < auxBtn.length; x++)
            auxBtn[x] = (ToggleButton) findViewById(getResources().getIdentifier("aux" + (x + 1) + "Btn", "id", getPackageName()));


        Init();
    }

    private void Init() {
        app.SetHandler(mHandler); //App class will automatically bind to commMW
        app.setRemoteActivity(this);


        //((Button) findViewById(R.id.switchModes)).setOnClickListener(mEvents.mClickListener);
        if (dualJoyStick) {
            dualJoystickView.stickR.setOnJostickMovedListener(mEvents._listener);
            dualJoystickView.stickL.setOnJostickMovedListener(mEvents._throttleListener);
            dualJoystickView.stickL.setAutoReturnToCenter(false);
            dualJoystickView.stickL.setAutoReturnToMid(true);
        } else {
            joystick.setOnJostickMovedListener(mEvents._throttleListener);
            joystick.setAutoReturnToCenter(false);
            joystick.setAutoReturnToMid(true);
        }

        for (int x = 0; x < auxBtn.length; x++)
            auxBtn[x].setOnClickListener(mEvents.mClickListener);
        settingsModified();
    }

    public void FrequentTasks() {
        mHandler.sendEmptyMessage(7);
        //send RC signal
        if (app.commMW.Connected) {
            status = "Connected";
            //Create payload TODO
            //new cycle begin if an ATTITUDE ACK is received or 300ms passed(package lost)
            long currentTime = System.currentTimeMillis();
            if (app.protocol.isIs_ATTITUDE_received() == true) {
                app.protocol.setIs_ATTITUDE_received(false);
                delayTimeRaw[delayTimeIdx++] = app.protocol.attitudeReceivedTime - lastRequestTime;
                if (delayTimeIdx == delayTimeRaw.length) {
                    delayTimeIdx = 0;
                    isDelayTimeAvailable = true;
                }
                if (isDelayTimeAvailable == true) {
                    long sum = 0;
                    for (long i : delayTimeRaw) {
                        sum += i;
                    }
                    delayTime = sum / delayTimeRaw.length;
                }
                lastRequestTime = currentTime;
                app.protocol.SendRequestMSP_ATTITUDE();
                mwcHeading = app.protocol.head;
            }
            //we consider it as a package lost, resend package
            else if (currentTime - lastRequestTime > 300) {
                app.protocol.SendRequestMSP_ATTITUDE();
            }
            app.protocol.SendRequestMSP_SET_RAW_RC(rc.get()); //TODO Check that delay isnt too big from other tasks

        }
        app.FrequentTasks();
    }

    public void setStatus(String status) {
        this.status = status;
        this.txtStatus.setText(status);
        app.Status = status;
    }

    public boolean isDualJoyStick() {
        return dualJoyStick;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BLUETOOTH_SEARCH_RETURN:
                if (resultCode == RESULT_OK) {
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    app.protocol.Connect(address, app.BaudRate, app.BluetoothConnectionStartDelay);
                    //initSensor();
                }
                break;
            case SETTINGS_MODIFY:
                settingsModified();
                break;
        }
        initSensor();
    }

    private void settingsModified() {
        rc.ThrottleResolution = app.ThrottleResolution;
        rc.TrimRoll = app.TrimRoll;
        rc.TrimPitch = app.TrimPitch;
        rc.RollPitchLimit = app.RollPitchLimit;
        rc.ThrottleLimit = app.RollPitchLimit;
        rc.blockyaw = app.BLOCKYAW;

        if (app.AuxTextChanged) {
            setAuxbtnTxt(auxBtn[0], app.Aux1Txt);
            setAuxbtnTxt(auxBtn[1], app.Aux2Txt);
            setAuxbtnTxt(auxBtn[2], app.Aux3Txt);
            setAuxbtnTxt(auxBtn[3], app.Aux4Txt);
            app.AuxTextChanged = false;
        }
        setStatus("Ready ");
    }

    private void setAuxbtnTxt(ToggleButton mButton, String text) {
        mButton.setText(text);
        mButton.setTextOn(text);
        mButton.setTextOff(text);
    }

    public void Connect() {
        Intent searchBtDevices = new Intent(this, DeviceListActivity.class);
        startActivityForResult(searchBtDevices, BLUETOOTH_SEARCH_RETURN);

    }

    public void UpdateUI() {
        txtStatus.setText(status);
        if (status == "Connected") {
            butt_connect_bt.setBackgroundResource(R.drawable.custom_shape_toggle_button_pressed);
        } else {
            butt_connect_bt.setBackgroundResource(R.drawable.custom_bt_button);
        }

        //Update Sliders
        if (app.PROGRESS) {
            rcStickThrottleSlider.setProgress(rc.get(RCSignals.THROTTLE) - minRC);
            rcStickPitchSlider.setProgress(rc.get(RCSignals.PITCH) - minRC);
            rcStickRollSlider.setProgress(rc.get(RCSignals.ROLL) - minRC);
            rcStickYawSlider.setProgress(rc.get(RCSignals.YAW) - minRC);
            rcStickAUX1Slider.setProgress(rc.get(RCSignals.AUX1) - minRC);
            rcStickAUX2Slider.setProgress(rc.get(RCSignals.AUX2) - minRC);
            rcStickAUX3Slider.setProgress(rc.get(RCSignals.AUX3) - minRC);
            rcStickAUX4Slider.setProgress(rc.get(RCSignals.AUX4) - minRC);

            //Update Slider Texts
            txtV_th.setText("Throttle:  " + rc.get(RCSignals.THROTTLE));
            txtV_p.setText("Pitch:  " + rc.get(RCSignals.PITCH));
            txtV_r.setText("Roll:  " + rc.get(RCSignals.ROLL));
            txtV_y.setText("Yaw:  " + rc.get(RCSignals.YAW));
            txtV_a1.setText("Aux 1:  " + rc.get(RCSignals.AUX1));
            txtV_a2.setText("Aux 2:  " + rc.get(RCSignals.AUX2));
            txtV_a3.setText("Aux 2:  " + rc.get(RCSignals.AUX3));
            txtV_a4.setText("Aux 2:  " + rc.get(RCSignals.AUX4));
        } else {
            rcStickThrottleSlider.setProgress(rc.get(RCSignals.THROTTLE) - minRC);
            rcStickPitchSlider.setVisibility(View.GONE);
            rcStickRollSlider.setVisibility(View.GONE);
            rcStickYawSlider.setVisibility(View.GONE);
            rcStickAUX1Slider.setVisibility(View.GONE);
            rcStickAUX2Slider.setVisibility(View.GONE);
            rcStickAUX3Slider.setVisibility(View.GONE);
            rcStickAUX4Slider.setVisibility(View.GONE);

            //Update Slider Texts
            txtV_th.setText("Throttle:  " + rc.get(RCSignals.THROTTLE));
            txtV_p.setText("");
            txtV_r.setText("");
            txtV_y.setText("");
            txtV_a1.setText("");
            txtV_a2.setText("");
            txtV_a3.setText("");
            txtV_a4.setText("");
        }

    }


    public void aux1_Click(View v) {
        // Replaced with When Throttle >= 1030
        vibrator.vibrate(500);
        if (app.PreventExitWhenFlying && rc.isFlying()) {
            Utilities.showToast(getString(R.string.message_is_flying), this);
            ((ToggleButton) v).setChecked(!((ToggleButton) v).isChecked());
            return;
        }
        rc.set(RCSignals.AUX1, ((ToggleButton) v).isChecked());
        isArmed = ((ToggleButton) v).isChecked();
        Log.i("AUX1", " " + isArmed);
    }

    public void aux2_Click(View v) {
        vibrator.vibrate(500);
        if (app.PreventExitWhenFlying && rc.isFlying()) {
            Utilities.showToast(getString(R.string.message_is_flying), this);
            ((ToggleButton) v).setChecked(!((ToggleButton) v).isChecked());
            return;
        }
        rc.set(RCSignals.AUX2, ((ToggleButton) v).isChecked());
        isArmed = ((ToggleButton) v).isChecked();
        Log.i("AUX2", " " + isArmed);
    }

    public void aux3_Click(View v) {
        vibrator.vibrate(500);
        if (app.PreventExitWhenFlying && rc.isFlying()) {
            Utilities.showToast(getString(R.string.message_is_flying), this);
            ((ToggleButton) v).setChecked(!((ToggleButton) v).isChecked());
            return;
        }
        rc.set(RCSignals.AUX3, ((ToggleButton) v).isChecked());
        isArmed = ((ToggleButton) v).isChecked();
        Log.i("AUX3", " " + isArmed);
    }

    public void aux4_Click(View v) {
        vibrator.vibrate(500);
        if (app.PreventExitWhenFlying && rc.isFlying()) {
            Utilities.showToast(getString(R.string.message_is_flying), this);
            ((ToggleButton) v).setChecked(!((ToggleButton) v).isChecked());
            return;
        }
        rc.set(RCSignals.AUX4, ((ToggleButton) v).isChecked());
        isArmed = ((ToggleButton) v).isChecked();
        Log.i("AUX4", " " + isArmed);
    }

    @Override
    protected void onStop() {
        super.onStop();
        app.stop();
        if (sensorAvailable) {
            exitSensor();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        for (int x = 0; x < auxBtn.length; x++)
            if (auxBtn[x].isChecked()) {
                rc.set(auxBtnid[x], !auxBtn[x].isChecked());
            }
        if (sensorAvailable) {
            exitSensor(); // Unregister sensorEventListener
        }
    }


    // ///////////////////Menu///////////////////
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connect:
                Connect();
                break;
            case R.id.menu_settings:
                if (rc.isFlying()) {
                    Utilities.showToast(
                            getString(R.string.message_is_flying), this);
                } else {
                    //this.startActivityForResult(new Intent(this, Settings.class), SETTINGS_MODIFY);
                    startActivity(new Intent(this, SetPreferenceActivity.class));
                }
                break;
        }
        return super.onContextItemSelected(item);
    }


    // /////////////////////////////End Menu/////////////////////////////
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (app.PreventExitWhenFlying && rc.getThrottle()> 1010 && (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_APP_SWITCH || event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {
            Utilities.showToast(getString(R.string.message_is_flying), this);
            return true;
        }
        if (isArmed && (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_APP_SWITCH || event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {
            Utilities.showToast("El Dron está armado, desarmalo antes de salir", this);
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN)
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    rc.adjustRcValue(-1);
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    rc.adjustRcValue(1);
                    return true;
                case KeyEvent.KEYCODE_SEARCH:
                    app.setManualModeOn(!app.getManualMode());
                    return true;
            }
        exitSensor();
        butt_connect_bt.setBackgroundResource(R.drawable.custom_bt_button);
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensorAvailable && !dualJoyStick) {
            initSensor();
        }
    }


    /**
     * ******************************************************************************
     * Manejo de Sensores
     * ******************************************************************************
     */

    class mSensorEventListener implements SensorEventListener {

        private float[] mGravity;
        private float[] mGeomagnetic;
        private float I[] = new float[16];
        private float Rr[] = new float[16];
        // Orientation values
        private float orientation[] = new float[3];


        // Define all SensorListener methods
        public void onSensorChanged(SensorEvent event) {
            if (!dualJoyStick && sensorAvailable) {

                switch (event.sensor.getType()) {
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        mGeomagnetic = lowPass(event.values.clone(), mGeomagnetic);
                        break;
                    case Sensor.TYPE_ACCELEROMETER:
                        mGravity = lowPass(event.values.clone(), mGravity);

                        break;
                }


                if (mGravity != null && mGeomagnetic != null) {
                    //exponentialSmoothing( mGravity.clone(), mGravity, 0.2 );
                    //exponentialSmoothing( mGeomagnetic.clone(), mGeomagnetic, 0.5 );


                    I = new float[16];
                    Rr = new float[16];
                    if (SensorManager.getRotationMatrix(Rr, I, mGravity, mGeomagnetic)) { // Got rotation matrix!
                        orientation = SensorManager.getOrientation(Rr, orientation);
                        //azimuth = orientation[0];
                        pitch = orientation[1];
                        roll = orientation[2];
                        if (roll > MAX_VALUE_X) {
                            arrowR.setImageDrawable(getResources().getDrawable(R.drawable.flecha_dcha_verde));
                        } else if (roll < -MAX_VALUE_X) {
                            arrowL.setImageDrawable(getResources().getDrawable(R.drawable.flecha_dcha_verde));
                        } else {
                            arrowR.setImageDrawable(getResources().getDrawable(R.drawable.flecha_dcha_negra));
                            arrowL.setImageDrawable(getResources().getDrawable(R.drawable.flecha_dcha_negra));
                        }

                        if (pitch > MAX_VALUE_Y) {
                            arrowT.setImageDrawable(getResources().getDrawable(R.drawable.flecha_dcha_verde));
                        } else if (pitch < -MAX_VALUE_Y) {
                            arrowB.setImageDrawable(getResources().getDrawable(R.drawable.flecha_dcha_verde));
                        } else {
                            arrowT.setImageDrawable(getResources().getDrawable(R.drawable.flecha_dcha_negra));
                            arrowB.setImageDrawable(getResources().getDrawable(R.drawable.flecha_dcha_negra));
                        }
                        rc.setAdjustedPitchAcc(pitch);
                        rc.setAdjustedRollAcc(roll);
                    }
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        /*
       * time smoothing constant for low-pass filter 0 \u2264 alpha \u2264 1 ; a smaller
         * value basically means more smoothing See:
         * http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
         */


        protected float[] lowPass(float[] input, float[] output) {
            if (output == null)
                return input;

            int inputLength = input.length;
            for (int i = 0; i < inputLength; i++) {
                output[i] = output[i] + ALPHA * (input[i] - output[i]);
            }
            return output;
        }
    }

    public void initSensor() {
        //Initiate instances
        sensorEventListener = new mSensorEventListener();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorAvailable = true;
        ALPHA = app.Alpha;

        //Register our listeners
        //SENSOR_DELAY_FASTEST   get sensor data as fast as possible
        //SENSOR_DELAY_GAME   rate suitable for games
        //SENSOR_DELAY_NORMAL   rate (default) suitable for  screen orientation changes
        //SENSOR_DELAY_UI

        mSensorManager.registerListener(sensorEventListener, accelerometer, mSensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(sensorEventListener, magnetometer, mSensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(sensorEventListener, gravity, mSensorManager.SENSOR_DELAY_FASTEST);
    }


    public void exitSensor() {
        if (sensorAvailable) {
            mSensorManager.unregisterListener(sensorEventListener);
            sensorAvailable = false;
        }
    }

    // int requests[] = new int[] { MultirotorData.MSP_DEBUG,
    // MultirotorData.MSP_ALTITUDE, MultirotorData.MSP_RC };
    public void onSensorsStateChangeRotate() {
        if (inputMode == InputMode.ACCELEROMETER) {
            float xCoordinate = (float) Utilities.mapCons(-app.sensors.pitch,
                    app.sensors.getMinValue(),
                    app.sensors.getMaxValue(),
                    -dualJoystickView.stickR.getMovementRange() / 2, dualJoystickView.stickR.getMovementRange() / 2);
            float yCoordinate = (float) Utilities.mapCons(-app.sensors.roll,
                    app.sensors.getMinValue(),
                    app.sensors.getMaxValue(),
                    -dualJoystickView.stickR.getMovementRange() / 2, dualJoystickView.stickR.getMovementRange() / 2);
            // dualJoystickView.stickR.setCoordinates(xCoordinate, yCoordinate);
        }
    }

}
