package es.age.apps.bridgit.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import es.age.apps.bridgit.core.communication.DeviceListActivity;
import es.age.apps.bridgit.core.remote.R;


public class MainActivity extends AppCompatActivity {

    private boolean dualjoysstick = true;

   private ImageButton button_joystick,button_accel,button_settings,button_bt;
    private BluetoothAdapter localAdapter=null;

    public static final int REQUEST_CONNECT_DEVICE_BT = 1;
    public final static int REQUEST_ENABLE_BT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        localAdapter = BluetoothAdapter.getDefaultAdapter();
        if (localAdapter != null &&  !localAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        button_joystick = (ImageButton) findViewById(R.id.button_joystick);
        button_accel = (ImageButton) findViewById(R.id.button_accel);
        button_settings = (ImageButton) findViewById(R.id.button_settings);
        button_bt = (ImageButton) findViewById(R.id.button_connectBT);

        button_joystick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),RemoteActivity.class);
                i.putExtra(RemoteActivity.GAMEACTIVITY_EXTRA,dualjoysstick);
                startActivity(i);
                // set an exit transition
                overridePendingTransition(android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right);
            }
        });

        button_accel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),RemoteActivity.class);
                i.putExtra(RemoteActivity.GAMEACTIVITY_EXTRA,!dualjoysstick);
                startActivity(i);
                // set an exit transition
                overridePendingTransition(android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right);
            }
        });

        button_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SetPreferenceActivity.class));
                // set an exit transition
                overridePendingTransition(android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right);
            }
        });

        button_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                localAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!localAdapter.isEnabled() && localAdapter != null)
                {
                    Toast.makeText(v.getContext(),"Es necesario activar el Bluetooth", Toast.LENGTH_LONG).show();
                }
                Intent serverIntent = null;
                serverIntent = new Intent(v.getContext(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_BT);
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    Toast.makeText(this," La MAC del dispositivo seleccionado es " + address, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
