#include <ArduinoBLE.h>

#define DEBUG true

// Start-up BLE procedure for controlling motor speed
const char* uuidSpeedService = "46126124-aa6c-11eb-bcbc-0242ac130002";
const char* uuidOfLeftSpeed = "461263fe-aa6c-11eb-bcbc-0242ac130002";
const char* uuidOfRightSpeed = "461264f8-aa6c-11eb-bcbc-0242ac130002";

// Set up BLE Speed services
BLEService speedService(uuidSpeedService);
BLEByteCharacteristic leftSpeed(uuidOfLeftSpeed, BLEWriteWithoutResponse | BLEWrite | BLENotify);
BLEByteCharacteristic rightSpeed(uuidOfRightSpeed, BLEWriteWithoutResponse | BLEWrite | BLENotify);

int motorLeftA = 2;
int motorLeftB = 3;
int enMotorLeft = 5;

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

  pinMode(motorLeftA, OUTPUT);
  pinMode(motorLeftB, OUTPUT);
  pinMode(enMotorLeft, OUTPUT);

  digitalWrite(motorLeftA, HIGH);
  digitalWrite(motorLeftB, LOW);
  analogWrite(enMotorLeft, 200);

  BLE.advertise();
}

void loop() {
  BLEDevice central = BLE.central();
  if (central) {
    Serial.println("Connected to Stella Drive app");
    
    while(central.connected()) {
      delay(100);
      int leftMotorSpeed = leftSpeed.value();
      analogWrite(enMotorLeft, leftMotorSpeed);
      Serial.println(leftMotorSpeed);
    }
  }
}
