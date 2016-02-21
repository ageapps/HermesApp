package es.age.apps.bridgit.core.remote;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import es.age.apps.bridgit.activities.RemoteActivity;
import es.age.apps.bridgit.ui.joystick.JoystickMovedListener;

public class RemoteActivityEvents {
	private RemoteActivity mActivity;

	public RemoteActivityEvents(RemoteActivity mActivity) {
		this.mActivity = mActivity;
	}
    public JoystickMovedListener _throttleListener = new JoystickMovedListener() {
        @Override
        public void OnMoved(int delta_yaw, int delta_throttle) {
			// Rango de valores entre -500 y 500
           // delta_yaw = delta_yaw / 10; //reduce yaw range. -50~50 SONG BO
            mActivity.rc.setAdjustedYaw(delta_yaw);
            mActivity.rc.setAdjustedThrottle(-delta_throttle);
			Log.i("Events", "Delta yaw: " + delta_yaw);
			Log.i("Events", "Delta throttle: " + delta_throttle );
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
			mActivity.rc.setMid(new byte[] { RCSignals.ROLL, RCSignals.PITCH });
		};
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

}
