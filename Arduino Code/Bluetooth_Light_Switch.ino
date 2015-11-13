
#include <Servo.h>

Servo lightServo;
Servo tvServo;
bool lightIsOn;  

void setup()
{
  Serial.begin(9600);
  lightIsOn = true;
}

void loop() 
{ 
  while (!Serial.available());
  
  int info = Serial.read();
  
  if(info == 255 && lightIsOn==true)
  {
    lightServo.attach(9);
    delay(15);
    lightServo.write(25);  
    delay(100);
    lightServo.detach();
    lightIsOn = !lightIsOn;
  }
  else if(info == 255 && lightIsOn==false)
  {
    lightServo.attach(9);
    delay(15);
    lightServo.write(0);  
    delay(100);
    lightServo.detach();
    lightIsOn = !lightIsOn;
  }
  else
  {
    tvServo.attach(10);
    delay(15);
    tvServo.write(-15);  
    delay(200);
    tvServo.detach();
  }

} 

