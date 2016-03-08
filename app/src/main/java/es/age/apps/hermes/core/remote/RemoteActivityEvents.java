package es.age.apps.hermes.core.remote;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import es.age.apps.hermes.activities.RemoteActivity;
import es.age.apps.hermes.ui.joystick.JoystickMovedListener;

public class RemoteActivityEvents {
    private RemoteActivity mActivity;

    public RemoteActivityEvents(RemoteActivity mActivity) {
        this.mActivity = mActivity;
    }

    public JoystickMovedListener _throttleListenerHand = new JoystickMovedListener() {
        @Override
        public void OnMoved(int delta_yaw, int delta_throttle) {
            // Rango de valores entre -500 y 500
            // delta_yaw = delta_yaw / 10; //reduce yaw range. -50~50 SONG BO
            mActivity.rc.setAdjustedYaw(delta_yaw);
            mActivity.rc.addThrottle(delta_throttle);
            //mActivity.seekThrottle.setProgress(mActivity.rc.getThrottle());
            Log.i("Events", "Delta bloqueado: " + mActivity.app.BLOCKYAW);
        }

        @Override
        public void OnReleased() {

        }

        @Override
        public void OnReturnedToCenter() {

        }
    };


    public JoystickMovedListener _throttleListener = new JoystickMovedListener() {
        @Override
        public void OnMoved(int delta_yaw, int delta_throttle) {
            // Rango de valores entre -500 y 500
            // delta_yaw = delta_yaw / 10; //reduce yaw range. -50~50 SONG BO
            //mActivity.rc.setAdjustedYaw(delta_yaw);
            mActivity.rc.setAdjustedYaw(delta_yaw);
            mActivity.rc.setAdjustedThrottle(-delta_throttle);
        }

        @Override
        public void OnReleased() {

        }

        @Override
        public void OnReturnedToCenter() {

        }
    };
    public JoystickMovedListener _listener = new JoystickMovedListener() {
        @Override
        public void OnMoved(int pan, int tilt) {
            mActivity.rc.setAdjustedRoll(pan);
            mActivity.rc.setAdjustedPitch(-tilt);
        }

        @Override
        public void OnReleased() {

        }

        public void OnReturnedToCenter() {
            mActivity.rc.setMid(new byte[]{RCSignals.ROLL, RCSignals.PITCH});
        }

        ;
    };

    public OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.aux1Btn:
                    mActivity.aux1_Click(v);
                    break;
                case R.id.aux2Btn:
                    mActivity.aux2_Click(v);
                    break;
                case R.id.aux3Btn:
                    mActivity.aux3_Click(v);
                    break;
                case R.id.aux4Btn:
                    mActivity.aux4_Click(v);
                    break;
            }
        }
    };

    public OnSeekBarChangeListener seek_listener = new OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mActivity.rc.setAdjustedThrottle(progress - 500);
            Log.i("Events", "SeekBar: " + mActivity.rc.getThrottle());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };


}
