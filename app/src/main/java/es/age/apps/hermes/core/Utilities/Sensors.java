/*  MultiWii EZ-GUI
    Copyright (C) <2012>  Bartosz Szczygiel (eziosoft)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.age.apps.hermes.core.Utilities;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

import es.age.apps.hermes.App;

public class Sensors {

    private final App app;


    // Filtrado de datos de los Senores
    boolean useFilter = true;
    LowPassFilter filterYaw = new LowPassFilter(0.03f);
    LowPassFilter filterPitch = new LowPassFilter(0.03f);
    LowPassFilter filterRoll = new LowPassFilter(0.03f);
    private static float ALPHA = 0.05f;


    private boolean sensorAvailable = false;
    // Sensores
    SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor gravity;
    Sensor magnetometer;
    private float[] mGravity;
    private float[] mGeomagnetic;
    private SensorEventListener sensorEventListener;


    private float[] m_rotationMatrix = new float[9];
    private float[] m_orientation = new float[3];

    public float pitch = 0.f;
    public int heading = 0;
    public float roll = 0.f;
    private float minValue = 0;
    private float maxValue = 0;
    private Handler mHandler;

    private Context context;


    public float getMaxValue() {
        return maxValue;
    }

    public float getMinValue() {
        return minValue;
    }


    public boolean isSensorSupported() {
        return mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
                || mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null
                || mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null;
    }


    public Sensors(Context applicationContext, final App app) {
        this.context = applicationContext;
        this.app = app;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        //sensor listener
        sensorEventListener = new SensorEventListener() {
            private float[] I = new float[16];
            private float[] Rr = new float[16];
            // Orientation values
            private float orientation[] = new float[3];

            @Override
            public void onSensorChanged(SensorEvent event) {

                switch (event.sensor.getType()) {
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        mGeomagnetic = lowPass(event.values.clone(), mGeomagnetic);
                        break;
                    case Sensor.TYPE_ACCELEROMETER:
                        mGravity = lowPass(event.values.clone(), mGravity);
                        break;
                }
                if (mGravity != null && mGeomagnetic != null) {

                    I = new float[16];
                    Rr = new float[16];
                    if (SensorManager.getRotationMatrix(Rr, I, mGravity, mGeomagnetic)) { // Got rotation matrix!
                        SensorManager.getOrientation(Rr, orientation);
                        //azimuth = orientation[0];
                        //Math.toDegrees(azimuthInRadians)+360)%360;
                        pitch = orientation[1];
                        roll = orientation[2];
                    /*if (roll > MAX_VALUE_X) {
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
                    }*/

                        Log.i("SENSORS", "Pitch: " + Float.toString(pitch) + " Roll: " + Float.toString(roll));
                    }
                }


            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };


    }


    public void start() {
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        maxValue = (float) accelerometer.getMaximumRange();
        minValue = -maxValue;


        //Register our listeners
        //SENSOR_DELAY_FASTEST   get sensor data as fast as possible
        //SENSOR_DELAY_GAME   rate suitable for games
        //SENSOR_DELAY_NORMAL   rate (default) suitable for screen orientation changes
        //SENSOR_DELAY_UI
        sensorAvailable = true;
        mSensorManager.registerListener(sensorEventListener, magnetometer, mSensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(sensorEventListener, accelerometer, mSensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(sensorEventListener, gravity, mSensorManager.SENSOR_DELAY_FASTEST);
    }


    public void stop() {
        if (sensorAvailable) {
            mSensorManager.unregisterListener(sensorEventListener);
        }
    }

    protected float[] lowPass(float[] input, float[] output) {
        if (output == null)
            return input;

        int inputLength = input.length;
        for (int i = 0; i < inputLength; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    public void setAlpha(float alpha) {
        this.ALPHA = alpha;
    }


    public void setFilter(float ALPHA) {
        if (ALPHA <= 0) {
            useFilter = ALPHA > 0;
            return;
        }
        if (ALPHA > 1)
            throw new ArithmeticException("Alpha must be between 0 and 1");
        filterYaw.setFilter(ALPHA);
        filterPitch.setFilter(ALPHA);
        filterRoll.setFilter(ALPHA);
    }

}
