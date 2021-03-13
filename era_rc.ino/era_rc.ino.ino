//Define motorpins for motor A
int motorApin1 = 2;
int motorApin2 = 3;

//define motorpins for motor b
int motorBpin1 = 11;
int motorBpin2 = 12;

void setup() {
  // Define motorpins as outputs
  pinMode(motorApin1, OUTPUT);
  pinMode(motorApin2, OUTPUT);
  pinMode(motorBpin1, OUTPUT);
  pinMode(motorBpin2, OUTPUT);

  pinMode(9, OUTPUT); 
  pinMode(10, OUTPUT);
}

void loop() {

  analogWrite(9, 90); //ENA pin
  analogWrite(10, 90); //ENB pin
  
  digitalWrite(motorApin1, HIGH);
  digitalWrite(motorApin2, LOW);

  digitalWrite(motorBpin1, HIGH);
  digitalWrite(motorBpin2, LOW);
}
