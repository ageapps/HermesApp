package es.age.apps.bridgit.activities;

import es.age.apps.bridgit.App;
import es.age.apps.bridgit.core.Utilities.Utilities;
import es.age.apps.bridgit.App.SettingsConstants;
import es.age.apps.bridgit.core.remote.R;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;


public class Settings extends PreferenceActivity {

		private Preference[] mPreferenceEntries;
		private Activity currentActivity;
		
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		currentActivity = this;
		SettingsConstants[] myConst = SettingsConstants.values();
		mPreferenceEntries = new Preference[myConst.length];
		
		for (int i = 0; i < myConst.length; i++) {
			mPreferenceEntries[i] = this.getPreferenceScreen().findPreference(myConst[i].toString().trim());
			mPreferenceEntries[i].setOnPreferenceChangeListener(changeListener);
		}
	}
	protected void onPause() {
        super.onPause();
        ((App) getApplication()).ReadSettings();
    }
	
	Preference.OnPreferenceChangeListener changeListener = new Preference.OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			boolean result = validate(preference, newValue);
			
			if(!result) Utilities.showToast("Invalid value", currentActivity);
			
			return result;
		}
	};
	
	private boolean validate(Preference preference, Object newValue) {
		SettingsConstants setting = SettingsConstants.valueOf(preference.getKey());
		
		switch(setting) {
			case TRIMROLL: return isTrimInRange(newValue);
			case TRIMPITCH: return isTrimInRange(newValue);
			default: return setting.validate(newValue);
		}
	}
	private boolean isTrimInRange(Object newValue) {
		if(!Utilities.IsInteger(newValue)) return false;
		
		int x = Integer.parseInt(newValue.toString());
		
		return Math.abs(x) < 500; //TODO associate rc_value 
	}
}