package es.age.apps.hermes.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import es.age.apps.hermes.core.remote.R;


public class SetPreferenceActivity extends AppCompatActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// set an exit transition
		overridePendingTransition(android.R.anim.slide_in_left,
				android.R.anim.slide_out_right);
  
	getFragmentManager().beginTransaction().replace(R.id.settings_container,
                new PrefsFragment()).commit();
	}


}