package ste.stella.erarc.stelladrive.ble;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;

public class BLEManager extends BleManager {
    final static UUID SpeedService = UUID.fromString("00000000-0000-1000-8000-00805f9b34fb");
    final static UUID SpeedLeft = UUID.fromString("00000001-0000-1000-8000-00805f9b34fb");
    final static UUID SpeedRight = UUID.fromString("00000002-0000-1000-8000-00805f9b34fb");

    private BluetoothGattCharacteristic speedLeft, speedRight;

    public BLEManager(@NonNull Context context) {
        super(context);
    }

    public BLEManager(@NonNull Context context, @NonNull Handler handler) {
        super(context, handler);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new BleGattCallback();
    }

    private class BleGattCallback extends BleManagerGattCallback {

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(SpeedService);
            if (service == null) {
                return false;
            }
            speedLeft = service.getCharacteristic(SpeedLeft);
            speedRight = service.getCharacteristic(SpeedRight);

            if (speedRight != null && speedLeft != null) {
                final int properties_left = speedLeft.getProperties();
                final int properties_right = speedRight.getProperties();

                boolean speedLeftValid =
                        (properties_left & BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE) != 0;
                boolean speedRightValid =
                        (properties_right & BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE) != 0;

                return speedLeftValid && speedRightValid;
            }
            return false;
        }

        @Override
        protected void initialize() {
            beginAtomicRequestQueue();
            writeCharacteristic(speedLeft, "0".getBytes()).enqueue();
            writeCharacteristic(speedRight, "0".getBytes()).enqueue();
        }

        @Override
        protected void onDeviceDisconnected() {
            System.err.println("Device disconnected");
        }
    }
}