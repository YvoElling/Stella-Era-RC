package ste.stella.erarc.stelladrive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import io.github.controlwear.virtual.joystick.android.JoystickView;
import no.nordicsemi.android.ble.observer.ConnectionObserver;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;

import ste.stella.erarc.stelladrive.ble.BLEManager;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {
    private ControlManager controlManager = ControlManager.getInstance();
    private BLEManager bleManager = new BLEManager(getApplicationContext());
    private BluetoothLeScannerCompat bleScanner = BluetoothLeScannerCompat.getScanner();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);

        handleBluetoothStatusCard();

        SeekBar seekBar = (SeekBar) findViewById(R.id.motor_power_selected_level);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                // 20 is defined as stepsize (5 levels, total of 100%, so 20% per step)
                controlManager.setMotorPowerLimit(20 * progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });

        JoystickView joystick = (JoystickView) findViewById(R.id.joystick_control);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onMove(int angle, int strength) {
                // Retrieve data fields for Power and angle
                TextView powerValue = (TextView) findViewById(R.id.power_val);
                TextView angelValue = (TextView) findViewById(R.id.angle_val);

                // Set latest data to the textfields in UI
                powerValue.setText((Integer.toString(strength)) + "%");
                angelValue.setText(Integer.toString(angle));

                // Update values in ControlManager
                if (angle <= 185 || angle >= 355) {
                    // Moving forward
                    if (angle > 85 && angle < 95) {
                        //Equal speed for both wheels
                        controlManager.setEqualSpeed((float)strength);
                    } else if (angle < 85) {
                        int diff = 80 - angle;
                        double result = (diff * (100.0 / 80.0)) * 0.01;
                        controlManager.setLeftSpeed((int)result);
                        controlManager.setRightSpeed(strength);
                    } else if (angle > 95 ) {
                        int diff = 80 - angle;
                        double result = (diff * (100.0 / 80.0)) * 0.01;
                        controlManager.setLeftSpeed(strength);
                        controlManager.setRightSpeed((int)result);
                    }
                } else {
                    controlManager.setEqualSpeed(0);
                    // Moving backwards; Not implemented for now.
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_buttons, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int optionId = item.getItemId();
        if (optionId == R.id.ble_settings) {
            this.onInitiateBleConnection();
        }

        return super.onOptionsItemSelected(item);
    }

    private void onInitiateBleConnection() {
        if (bleManager != null && !bleManager.isConnected()) {
            //Request required permissions


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Map<String, Integer> permissionsMap = new HashMap<>();
        permissionsMap.put(ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);

        //Map results to map to see if they have been granted
        for (int i = 0; i < permissions.length; ++i) {
            permissionsMap.put(permissions[i], grantResults[i]);
        }

        try {
            if (permissionsMap.get(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {

                }
            }

        } catch (Exception e) {
            System.err.println("Permissions were not granted, aborting application");
            finish();
        }

    }

    @SuppressLint("SetTextI18n")
    public void handleBluetoothStatusCard() {
        TextView bleStatus = (TextView) findViewById(R.id.ble_actual_status);

        //Get Connectivity object: contains all information about current
        //connected devices and such
        ConnectivityKing connectivityKing = ConnectivityKing.getInstance();

        if (connectivityKing.getConnectedStatus()) {
            // Device is connected. Get set name.
            String bluetoothName = connectivityKing.getBleName();
            // If we have a device name, set that, otherwise set the MAC address of the BLE device
            if (bluetoothName != null) {
                bleStatus.setText("Connected to: " + bluetoothName);
            } else {
                bleStatus.setText("Connected to: " + connectivityKing.getMacAddress());
            }
        } else { // No device is connected
            bleStatus.setText(getString(R.string.disconnected));
        }
    }
}