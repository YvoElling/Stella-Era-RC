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

    final static UUID LightService = UUID.fromString("952f05b6-af47-11eb-8529-0242ac130003");
    final static UUID LightOn = UUID.fromString("952f0804-af47-11eb-8529-0242ac130003");
    final static UUID LightBlinkerLeftOn = UUID.fromString("952f08ea-af47-11eb-8529-0242ac130003");
    final static UUID LightBlinkerRightOn = UUID.fromString("952f09b2-af47-11eb-8529-0242ac130003");

    private int lastLeftBlinker = 0;
    private int lastRightblinker = 0;
    private int lightStatus = 0;

    private BluetoothGattCharacteristic speedLeft, speedRight, lightOn, lightBlinkerLeftOn,
            lightBlinkerRightOn;

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

    public void updateLightStatus() {
        if (lightStatus == 1) {
            BigInteger bigInt = BigInteger.valueOf(0);
            byte[] bytes = bigInt.toByteArray();
            writeCharacteristic(lightOn, bytes).enqueue();
            lightStatus = 0;
        } else {
            BigInteger bigInt = BigInteger.valueOf(1);
            byte[] bytes = bigInt.toByteArray();
            writeCharacteristic(lightOn, bytes).enqueue();
            lightStatus = 1;
        }
    }

    public void updateBlinker(boolean left) {
        if (left) {
            if (lastLeftBlinker == 1) {
                BigInteger bigInt = BigInteger.valueOf(0);
                byte[] bytes = bigInt.toByteArray();
                writeCharacteristic(lightBlinkerLeftOn, bytes).enqueue();
                lastLeftBlinker = 0;
            } else {
                BigInteger bigInt = BigInteger.valueOf(1);
                byte[] bytes = bigInt.toByteArray();
                writeCharacteristic(lightBlinkerLeftOn, bytes).enqueue();
                lastRightblinker = 1;
            }
        } else {
            if (lastRightblinker == 1) {
                BigInteger bigInt = BigInteger.valueOf(0);
                byte[] bytes = bigInt.toByteArray();
                writeCharacteristic(lightBlinkerRightOn, bytes).enqueue();
                lastRightblinker = 0;
            } else {
                BigInteger bigInt = BigInteger.valueOf(1);
                byte[] bytes = bigInt.toByteArray();
                writeCharacteristic(lightBlinkerRightOn, bytes).enqueue();
                lastRightblinker = 1;
            }
        }
    }


    public void update() {
        // Get right and left speed
        int leftSpeed = (int)(ControlManager.getInstance().getLeftSpeed());
        int rightSpeed = (int)(ControlManager.getInstance().getRightSpeed());

        // Convert into BigInteger object to get bytes
        BigInteger leftSpeedBytes = BigInteger.valueOf(leftSpeed);
        byte[] lsBytes = toByteArrayUnsigned(leftSpeedBytes);
        BigInteger rightSpeedBytes = BigInteger.valueOf(rightSpeed);
        byte[] rsBytes = toByteArrayUnsigned(rightSpeedBytes);

        // Write bytes for respective speed to characteristics of ble service.
        writeCharacteristic(speedLeft, lsBytes).enqueue();
        writeCharacteristic(speedRight, rsBytes).enqueue();
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
            final BluetoothGattService light_service = gatt.getService(LightService);
            if (service == null || light_service == null) {
                return false;
            }
            speedLeft = service.getCharacteristic(SpeedLeft);
            speedRight = service.getCharacteristic(SpeedRight);
            lightBlinkerLeftOn = light_service.getCharacteristic(LightBlinkerLeftOn);
            lightBlinkerRightOn = light_service.getCharacteristic(LightBlinkerRightOn);
            lightOn = light_service.getCharacteristic(LightOn);

            // If the speed requirements are satisfied, no need to check the light requirements.
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

            // Set all bluetooth fields to zero.
            writeCharacteristic(speedLeft, bytes).enqueue();
            writeCharacteristic(speedRight, bytes).enqueue();
            writeCharacteristic(lightBlinkerLeftOn, bytes).enqueue();
            writeCharacteristic(lightBlinkerRightOn, bytes).enqueue();
            writeCharacteristic(lightOn, bytes).enqueue();
        }

        @Override
        protected void onDeviceDisconnected() {
            System.err.println("Device disconnected");
        }
    }
}