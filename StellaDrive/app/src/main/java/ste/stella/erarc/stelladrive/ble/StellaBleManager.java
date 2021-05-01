package ste.stella.erarc.stelladrive.ble;

import androidx.annotation.NonNull;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import ste.stella.erarc.stelladrive.ControlManager;

public class StellaBleManager extends BleManager {
    final static UUID SpeedService = UUID.fromString("46126124-aa6c-11eb-bcbc-0242ac130002");
    final static UUID SpeedLeft = UUID.fromString("461263fe-aa6c-11eb-bcbc-0242ac130002");
    final static UUID SpeedRight = UUID.fromString("461264f8-aa6c-11eb-bcbc-0242ac130002");

    private BluetoothGattCharacteristic speedLeft, speedRight;

    public StellaBleManager(@NonNull Context context) {
        super(context);
    }

    public static byte[] toByteArrayUnsigned(BigInteger bi) {
        byte[] extractedBytes = bi.toByteArray();
        int skipped = 0;
        boolean skip = true;
        for (byte b : extractedBytes) {
            boolean signByte = b == (byte) 0x00;
            if (skip && signByte) {
                skipped++;
                continue;
            } else if (skip) {
                skip = false;
            }
        }
        extractedBytes = Arrays.copyOfRange(extractedBytes, skipped,
                extractedBytes.length);
        return extractedBytes;
    }

    public void update() {
        int leftSpeed = (int)(ControlManager.getInstance().getLeftSpeed()*2.55);
        BigInteger bigInt = BigInteger.valueOf(leftSpeed);
        byte[] bytes = toByteArrayUnsigned(bigInt);

        System.out.println(leftSpeed);
        System.out.println(bytes);
        writeCharacteristic(speedLeft, bytes).enqueue();
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
                        (properties_left & 28) != 0;
                boolean speedRightValid =
                        (properties_right & 28) != 0;

                return speedLeftValid && speedRightValid;
            }
            return false;
        }

        @Override
        protected void initialize() {
            beginAtomicRequestQueue();

            BigInteger bigInt = BigInteger.valueOf(0);
            byte[] bytes = bigInt.toByteArray();

            writeCharacteristic(speedLeft, bytes).enqueue();
            writeCharacteristic(speedRight, bytes).enqueue();
        }

        @Override
        protected void onDeviceDisconnected() {
            System.err.println("Device disconnected");
        }
    }
}