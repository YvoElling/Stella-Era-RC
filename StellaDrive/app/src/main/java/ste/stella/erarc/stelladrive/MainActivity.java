package ste.stella.erarc.stelladrive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Map;

import io.github.controlwear.virtual.joystick.android.JoystickView;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;

import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;
import ste.stella.erarc.stelladrive.ble.StellaBleManager;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {
    private ControlManager controlManager = ControlManager.getInstance();
    private StellaBleManager stellaBleManager;
    private BluetoothLeScannerCompat bleScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);

        stellaBleManager = new StellaBleManager(getBaseContext());
        bleScanner = BluetoothLeScannerCompat.getScanner();
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
                        double result = (diff * (100.0 / 80.0));
                        controlManager.setRightSpeed((int)result);
                        controlManager.setLeftSpeed((float)result-strength);
                    } else if (angle > 95 ) {
                        int diff = angle - 80;
                        double result = (diff * (100.0 / 80.0));
                        controlManager.setRightSpeed(strength);
                        controlManager.setLeftSpeed((float) ((float) strength-result));
                    }
                    stellaBleManager.update();
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
        if (stellaBleManager != null && !stellaBleManager.isConnected()) {
            //Request required permissions
            updateBluetoothText("Requesting Permissions...");
            requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 200);
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
                    Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBt, 0);
                } else {
                    updateBluetoothText("Scanning ...");
                    start_scan();
                }
            } else {
                finish();
            }

        } catch (Exception e) {
            System.err.println("Permissions were not granted, aborting application");
            finish();
        }

    }

    void start_scan() {
        System.out.println("Searching for Stella RC");
        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setUseHardwareBatchingIfSupported(true)
                .build();
        bleScanner.startScan(null, scanSettings, new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, @NonNull ScanResult result) {
                super.onScanResult(callbackType, result);

                BluetoothDevice device = result.getDevice();
                String deviceName = device.getName();
                if (device.getName() != null && device.getName().equals("Stella Era-RC '21")) {
                    updateBluetoothText("Connecting ...");
                    stellaBleManager.connect(device)
                              .done(new SuccessCallback() {
                                  @Override
                                  public void onRequestCompleted(@NonNull BluetoothDevice device) {
                                        ConnectivityKing.getInstance().updateBleName(device.getName());
                                        ConnectivityKing.getInstance().updateConnectivityStatus(true);
                                        ConnectivityKing.getInstance().updateMacAddress(device.getAddress());
                                        handleBluetoothStatusCard();
                                  }
                              })
                              .fail(new FailCallback() {
                                  @Override
                                  public void onRequestFailed(@NonNull BluetoothDevice device, int status) {
                                      System.out.println("Couldnot connect ... " + status);
                                  }
                              }).enqueue();
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                System.out.println("Scan failed....");
            }
        });

    }

    public void updateBluetoothText(String text) {
        TextView bleStatus = (TextView) findViewById(R.id.ble_actual_status);
        bleStatus.setText(text);
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