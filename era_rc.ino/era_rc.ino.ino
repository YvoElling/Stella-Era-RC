#include <ArduinoBLE.h>

#define DEBUG false

// Start-up BLE procedure for controlling motor speed
const char* uuidSpeedService = "46126124-aa6c-11eb-bcbc-0242ac130002";
const char* uuidOfLeftSpeed = "461263fe-aa6c-11eb-bcbc-0242ac130002";
const char* uuidOfRightSpeed = "461264f8-aa6c-11eb-bcbc-0242ac130002";

// Set up BLE Speed services
BLEService speedService(uuidSpeedService);
BLEByteCharacteristic leftSpeed(uuidOfLeftSpeed, BLEWriteWithoutResponse | BLEWrite | BLENotify);
BLEByteCharacteristic rightSpeed(uuidOfRightSpeed, BLEWriteWithoutResponse | BLEWrite | BLENotify);

int speed = 0;

void setup() {
  if (DEBUG) {
    Serial.begin(9600);
    while (!Serial) {
      Serial.println("SERIAL not started");
    }
  }

  Serial.println("Starting...");
  if (!BLE.begin()) {
    Serial.println("starting BLE failed!");

    while (1);
  }

  Serial.println("BLE has started");
  BLE.setLocalName("Stella Era-RC '21");
  BLE.setAdvertisedService(speedService);
  speedService.addCharacteristic(leftSpeed);
  speedService.addCharacteristic(rightSpeed);
  BLE.addService(speedService);

  BLE.advertise();
}

void loop() {
  BLEDevice central = BLE.central();
  if (central) {
    //Serial.println("Central exists");
    
    while(central.connected()) {
      
      delay(250);

      int new_speed = leftSpeed.value();
      if (new_speed != speed) {
        Serial.println(leftSpeed.value());
      }
    }
  }
}
