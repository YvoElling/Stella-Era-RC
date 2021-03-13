#include <ArduinoBLE.h>

#define DEBUG true

//Define motorpins for motor A
int motorApin1 = 2;
int motorApin2 = 3;

//define motorpins for motor b
int motorBpin1 = 11;
int motorBpin2 = 12;

// Start-up BLE procedure for controlling motor speed
const char* uuidSpeedService = "00000000-0000-1000-8000-00805f9b34fb";
const char* uuidOfSpeed = "00000001-0000-1000-8000-00805f9b34fb";

// Speed
int new_speed = 50;
int speed = 50;

// Set up BLE Speed services
BLEService speedService(uuidSpeedService);
BLEByteCharacteristic wheelSpeed(uuidOfSpeed, BLEWriteWithoutResponse | BLEWrite | BLENotify);


void setup() {
  if (DEBUG) {
    Serial.begin(9600);
    while (!Serial) {
      Serial.println("SERIAL not started");
    }
  }
  
  
  // Set PINS for the motorcontroller as outputs
  pinMode(motorApin1, OUTPUT);
  pinMode(motorApin2, OUTPUT);
  pinMode(motorBpin1, OUTPUT);
  pinMode(motorBpin2, OUTPUT);
  pinMode(9, OUTPUT); 
  pinMode(10, OUTPUT);

  pinMode(LED_BUILTIN, OUTPUT);

  if (!BLE.begin()) {
    Serial.println("starting BLE failed!");

    while (1);
  }
  
  BLE.setLocalName("Stella Era-RC '21");
  BLE.setAdvertisedService(speedService);
  speedService.addCharacteristic(wheelSpeed);
  BLE.addService(speedService);
  wheelSpeed.writeValue(50);

  BLE.advertise();
}

void loop() {
  digitalWrite(motorApin1, HIGH);
  digitalWrite(motorApin2, LOW);

  digitalWrite(motorBpin1, HIGH);
  digitalWrite(motorBpin2, LOW);

  analogWrite(9, 100); //ENA pin
  analogWrite(10, 100); //ENB pin
  
//  BLEDevice central = BLE.central();
//  if (central) {
//    //Serial.println("Central exists");
//    while(central.connected()) {
//      digitalWrite(LED_BUILTIN, HIGH);
//      delay(250);
//      digitalWrite(LED_BUILTIN, LOW);
//      int new_speed = wheelSpeed.value();
//      if (new_speed != speed) {
//        Serial.println(wheelSpeed.value());
//        speed = new_speed;
//        analogWrite(9, speed); //ENA pin
//        analogWrite(10, speed); //ENB pin
//        Serial.println("Wheels should be turning at new speed now");
//      }
//    }
//  }
}
