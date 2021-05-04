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
int motorRightA = 9;
int motorRightB = 10;
int enMotorRight = 11;
int enMotorLeft = 5;

int ledBrakeLightLeft = 4;
int ledBrakeLightRight = 7;

int prev_left_speed = 0;
int prev_right_speed = 0;

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
  pinMode(motorRightA, OUTPUT);
  pinMode(motorRightB, OUTPUT);
  pinMode(enMotorRight, OUTPUT);
  pinMode(ledBrakeLightLeft, OUTPUT);
  pinMode(ledBrakeLightRight, OUTPUT);

  digitalWrite(motorLeftA, HIGH);
  digitalWrite(motorLeftB, LOW);
  digitalWrite(motorRightA, HIGH);
  digitalWrite(motorRightB, LOW);
  digitalWrite(ledBrakeLightLeft, LOW);
  digitalWrite(ledBrakeLightRight, LOW);
  analogWrite(enMotorLeft, 200);
  analogWrite(enMotorRight, 200);

  BLE.advertise();
}

void loop() {
  BLEDevice central = BLE.central();
  if (central) {
    Serial.println("Connected to Stella Drive app");
    
    while(central.connected()) {
      delay(100);
      int leftMotorSpeed = leftSpeed.value();
      int rightMotorSpeed = rightSpeed.value();
      if (leftMotorSpeed < prev_left_speed && rightMotorSpeed < prev_right_speed) {
        digitalWrite(ledBrakeLightLeft, HIGH); 
        digitalWrite(ledBrakeLightRight, HIGH); 
      } else {
        digitalWrite(ledBrakeLightLeft, LOW);
        digitalWrite(ledBrakeLightRight, LOW); 
      }
      analogWrite(enMotorLeft, leftMotorSpeed);
      analogWrite(enMotorRight, rightMotorSpeed);
      prev_left_speed = leftMotorSpeed;
      prev_right_speed = rightMotorSpeed;
      Serial.println(leftMotorSpeed);
    }
  }
}
