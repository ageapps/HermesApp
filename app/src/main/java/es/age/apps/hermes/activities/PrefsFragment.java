package es.age.apps.hermes.activities;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import es.age.apps.hermes.App;
import es.age.apps.hermes.core.Utilities.Utilities;
import es.age.apps.hermes.core.remote.R;


public class PrefsFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
  
		// Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
	}

	Preference.OnPreferenceChangeListener changeListener = new Preference.OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			boolean result = validate(preference, newValue);

			if(!result) Utilities.showToast("Invalid value", getActivity());

			return result;
		}
	};

	private boolean validate(Preference preference, Object newValue) {
		App.SettingsConstants setting = App.SettingsConstants.valueOf(preference.getKey());

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

	public void onPause() {
		super.onPause();
		((App) getActivity().getApplication()).ReadSettings();
	}
}