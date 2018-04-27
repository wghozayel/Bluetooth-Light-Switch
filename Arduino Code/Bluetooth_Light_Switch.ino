
#include <Servo.h>

Servo lightServo;
int curtainsEn = 10;
int curtainsIn1 = 6;
int curtainsIn2 = 7;
bool lightIsOn;  
int val;

void setup()
{
  Serial.begin(9600);
  lightIsOn = true;
  pinMode(curtainsEn, OUTPUT);
  pinMode(curtainsIn1, OUTPUT);
  pinMode(curtainsIn2, OUTPUT);
}

void loop() 
{ 
  while (!Serial.available());
  int info = Serial.read(); 
  Serial.println(Serial.read());
  if(info == 255 && lightIsOn==true)
  {
    lightServo.attach(9);
    delay(15);
    lightServo.write(100);  
    delay(430);
    lightServo.detach();
    lightIsOn = !lightIsOn;
  }
  else if(info == 255 && lightIsOn==false)
  {
    lightServo.attach(9);
    delay(15);
    lightServo.write(0);  
    delay(430);
    lightServo.detach();
    lightIsOn = !lightIsOn;
  }
  else if(info == 254)
  {
      digitalWrite(curtainsIn1, HIGH);
      digitalWrite(curtainsIn2, LOW);
      analogWrite(curtainsEn, 255);
      delay(11000);
      digitalWrite(curtainsIn1, LOW);
      digitalWrite(curtainsIn2, LOW);
  }
  else if(info ==253)
  {
      digitalWrite(curtainsIn1, LOW);
      digitalWrite(curtainsIn2, HIGH);
      analogWrite(curtainsEn, 255);
      delay(8000);
      digitalWrite(curtainsIn1, LOW);
      digitalWrite(curtainsIn2, LOW);
  }
  else
  {
    val = map(info, 0, 252, 0, 3);
    Serial.println(val);
    if(val == 0){
      digitalWrite(curtainsIn1, HIGH);
      digitalWrite(curtainsIn2, LOW);
    }
    else if(val ==1){
      digitalWrite(curtainsIn1, LOW);
      digitalWrite(curtainsIn2, LOW);
    }
    else if(val ==2){
      digitalWrite(curtainsIn1, LOW);
      digitalWrite(curtainsIn2, HIGH);
    }
    analogWrite(curtainsEn, 255);
  }

} 

