package es.age.apps.hermes.core.remote;

import java.lang.ref.WeakReference;


import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;

import es.age.apps.hermes.App;
import es.age.apps.hermes.activities.RemoteActivity;
import es.age.apps.hermes.core.Utilities.Utilities;
import es.age.apps.hermes.core.communication.Communication;
import es.age.apps.hermes.core.communication.CommunicationMode;

public class RemoteActivityCommunicationHandler extends Handler {
	private final WeakReference<RemoteActivity> mActivity;

	public RemoteActivityCommunicationHandler(RemoteActivity activity) {
		mActivity = new WeakReference<RemoteActivity>(activity);
	}
	@Override
	public void handleMessage(Message msg) {
		final RemoteActivity myNewActivity = mActivity.get();
		if(myNewActivity != null)
			switch (msg.what) {
				case Communication.MESSAGE_STATE_CHANGE:
					switch (msg.arg1) {
						case Communication.STATE_CONNECTED:
							myNewActivity.setStatus("Connected");
							if(myNewActivity.app.comMode == CommunicationMode.BLUETOOTH)
								new CountDownTimer(5000, 10) {
									public void onTick(long millisUntilFinished ) {
										myNewActivity.setStatus(millisUntilFinished + "");
									}
									public void onFinish() {
							    	 /*if(myNewActivity.isCamera)
							    	 myNewActivity.startWebCam();*/
										myNewActivity.setStatus("Connected");
									}
								}.start();
							break;
					}
					break;
				case App.SENSORSCHANGED:
					myNewActivity.onSensorsStateChangeRotate();
					break;
				case Communication.MESSAGE_TOAST:
					Utilities.showToast(msg.getData().getString(Communication.TOAST), myNewActivity);
					break;
				case 7: //Update UI
					myNewActivity.UpdateUI();
					break;
			}
	}

};
