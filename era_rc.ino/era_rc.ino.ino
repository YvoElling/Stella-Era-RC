#include <ArduinoBLE.h>
#define DEBUG false

// Start-up BLE procedure for controlling motor speed
const char* uuidSpeedService = "46126124-aa6c-11eb-bcbc-0242ac130002";
const char* uuidOfLeftSpeed = "461263fe-aa6c-11eb-bcbc-0242ac130002";
const char* uuidOfRightSpeed = "461264f8-aa6c-11eb-bcbc-0242ac130002";
const char* uuidOfLightService = "952f05b6-af47-11eb-8529-0242ac130003";
const char* uuidOfLightOn = "952f0804-af47-11eb-8529-0242ac130003";
const char* uuidOfLightLeftBlinker = "952f08ea-af47-11eb-8529-0242ac130003";
const char* uuidOfLightRightBlinker = "952f09b2-af47-11eb-8529-0242ac130003";

// Set up BLE Speed services
BLEService speedService(uuidSpeedService);
BLEByteCharacteristic leftSpeed(uuidOfLeftSpeed, BLEWriteWithoutResponse | BLEWrite | BLENotify);
BLEByteCharacteristic rightSpeed(uuidOfRightSpeed, BLEWriteWithoutResponse | BLEWrite | BLENotify);

BLEService lightService(uuidOfLightService);
BLEBoolCharacteristic lightsOn(uuidOfLightOn, BLEWriteWithoutResponse | BLEWrite | BLENotify);
BLEBoolCharacteristic lightsBlinkerLeftOn(uuidOfLightLeftBlinker, BLEWriteWithoutResponse | BLEWrite | BLENotify);
BLEBoolCharacteristic lightsBlinkerRightOn(uuidOfLightRightBlinker, BLEWriteWithoutResponse | BLEWrite | BLENotify);

// Motor pins
int motorLeftA = 2;
int motorLeftB = 3;
int motorRightA = 9;
int motorRightB = 10;
int enMotorRight = 11;
int enMotorLeft = 5;

// Braking lights leds
int ledBrakeLightLeft = 4;
int ledBrakeLightRight = 7;

// Front lights, both full and blinking.
int ledFrontLeft = 6;
int ledFrontRight = 12;

// Keep track of previous speed to engage brakelights. 
int prev_left_speed = 0;
int prev_right_speed = 0;

// Keep track of lightstatus
int lightsOnStatus = 2;
int blinkerLeftStatus = 2;
int blinkerRightStatus = 2;

void setup() {
//  if (DEBUG) {
//    Serial.begin(9600);
//    while (!Serial) {
//      Serial.println("SERIAL not started");
//    }
//  }

  Serial.println("Starting...");
  if (!BLE.begin()) {
    Serial.println("starting BLE failed!");

    while (1);
  }

  Serial.println("BLE has started");
  BLE.setLocalName("Stella Era-RC '21");
  
  BLE.setAdvertisedService(speedService);
  BLE.setAdvertisedService(lightService);
  speedService.addCharacteristic(leftSpeed);
  speedService.addCharacteristic(rightSpeed);
  lightService.addCharacteristic(lightsOn);
  lightService.addCharacteristic(lightsBlinkerLeftOn);
  lightService.addCharacteristic(lightsBlinkerRightOn);
  
  BLE.addService(speedService);
  BLE.addService(lightService);

  pinMode(motorLeftA, OUTPUT);
  pinMode(motorLeftB, OUTPUT);
  pinMode(enMotorLeft, OUTPUT);
  pinMode(motorRightA, OUTPUT);
  pinMode(motorRightB, OUTPUT);
  pinMode(enMotorRight, OUTPUT);
  pinMode(ledBrakeLightLeft, OUTPUT);
  pinMode(ledBrakeLightRight, OUTPUT);
  pinMode(ledFrontLeft, OUTPUT);
  pinMode(ledFrontRight, OUTPUT);

  digitalWrite(motorLeftA, HIGH);
  digitalWrite(motorLeftB, LOW);
  digitalWrite(motorRightA, HIGH);
  digitalWrite(motorRightB, LOW);
  digitalWrite(ledBrakeLightLeft, LOW);
  digitalWrite(ledBrakeLightRight, LOW);
  digitalWrite(ledFrontLeft, LOW);
  digitalWrite(ledFrontRight, LOW);
  analogWrite(enMotorLeft, 200);
  analogWrite(enMotorRight, 200);

  lightsOn.setEventHandler(BLEWritten, bleLightsOnEvent);
  lightsBlinkerLeftOn.setEventHandler(BLEWritten, bleLeftBlinkerService);
  lightsBlinkerRightOn.setEventHandler(BLEWritten, bleRightBlinkerService);

  BLE.advertise();
}

void loop() {
  BLEDevice central = BLE.central();
  if (central) {
    Serial.println("Connected to Stella Drive app");
    
    while(central.connected()) {
      delay(100);

      // Write motor speeds and handle brake lights
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

      // Check if BLE events have changed
      BLE.poll();

      // Handle light services
      if (blinkerLeftStatus) {
        if (digitalRead(ledFrontLeft) == LOW) {
          digitalWrite(ledFrontLeft, HIGH);
        } else {
          digitalWrite(ledFrontLeft, LOW);
        }
      } else {
        digitalWrite(ledFrontLeft, lightsOnStatus);
      }

      if (blinkerRightStatus) {
        if (digitalRead(ledFrontRight) == LOW) {
          digitalWrite(ledFrontRight, HIGH);
        } else {
          digitalWrite(ledFrontRight, LOW);
        }
      } else {
        digitalWrite(ledFrontRight, lightsOnStatus);
      }
    } 
  }
}

void bleLightsOnEvent(BLEDevice central, BLECharacteristic characteristic) {
  Serial.println("Event callback for ledOn called");
  if (lightsOnStatus == 0) {
    Serial.println("Turning light on.");
    lightsOnStatus = 1;
    digitalWrite(ledFrontLeft, HIGH);
    digitalWrite(ledFrontRight, HIGH);
  } else {
    Serial.println("Turning light off.");
    lightsOnStatus = 0;
    digitalWrite(ledFrontLeft, LOW);
    digitalWrite(ledFrontRight, LOW);
  } 
}

void bleLeftBlinkerService(BLEDevice central, BLECharacteristic characteristic) {
  if (blinkerLeftStatus == 0) {
    blinkerLeftStatus = 1;
    digitalWrite(ledFrontLeft, HIGH);
  } else {
    blinkerLeftStatus = 0;
    digitalWrite(ledFrontLeft, LOW);
  } 
}

void bleRightBlinkerService(BLEDevice central, BLECharacteristic characteristic) {
  if (blinkerRightStatus == 0) {
    blinkerRightStatus = 1;
    digitalWrite(ledFrontRight, HIGH);
  } else {
    blinkerRightStatus = 0;
    digitalWrite(ledFrontRight, LOW);
  } 
}
