package es.age.apps.bridgit.core.remote;

import java.lang.ref.WeakReference;

import es.age.apps.bridgit.App;
import es.age.apps.bridgit.activities.RemoteActivity;
import es.age.apps.bridgit.core.Utilities.Utilities;
import es.age.apps.bridgit.core.communication.Communication;

import android.os.Handler;
import android.os.Message;

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
					 break;
				}
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
