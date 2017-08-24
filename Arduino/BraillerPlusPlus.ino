#include <MeetAndroid.h>
MeetAndroid meetAndroid;

const int BUTTON1 = 2; 
const int BUTTON2 = 5;
const int BUTTON3 = 8;
const int BUTTON4 = 4;
const int BUTTON5 = 6;
const int BUTTON6 = 10;
const int BUTTON7 = 3;
const int BUTTON8 = 7;
const int BUTTON9 = 9;



boolean active1 = false, active2 = false, active3 = false, active4 = false, 
        active5 = false, active6 = false, active7 = false, active8 = false, active9 = false,
        activeGlobal = false;
char active[]={'0','0','0','0','0','0','0','0','0'};

byte i;


void sendPressed(){
  if(digitalRead(BUTTON1) == LOW && digitalRead(BUTTON2) == LOW && digitalRead(BUTTON3) == LOW &&
     digitalRead(BUTTON4) == LOW && digitalRead(BUTTON5) == LOW && digitalRead(BUTTON6) == LOW &&
     digitalRead(BUTTON7) == LOW && digitalRead(BUTTON8) == LOW && digitalRead(BUTTON9) == LOW){

      delay(10);
      if(digitalRead(BUTTON1) == LOW && digitalRead(BUTTON2) == LOW && digitalRead(BUTTON3) == LOW &&
        digitalRead(BUTTON4) == LOW && digitalRead(BUTTON5) == LOW && digitalRead(BUTTON6) == LOW &&
        digitalRead(BUTTON7) == LOW && digitalRead(BUTTON8) == LOW && digitalRead(BUTTON9) == LOW){
        meetAndroid.send(active);

        for(i=0;i<9;i++){
          active[i]='0';
       }     
       activeGlobal=false;
       delay(100);
      }
    
  }  
}

void setup() {
  pinMode(BUTTON1,INPUT);
  pinMode(BUTTON2,INPUT);
  pinMode(BUTTON3,INPUT);
  pinMode(BUTTON4,INPUT);
  pinMode(BUTTON5,INPUT);
  pinMode(BUTTON6,INPUT);
  pinMode(BUTTON7,INPUT);
  pinMode(BUTTON8,INPUT);
  pinMode(BUTTON9,INPUT);
  Serial.begin(115200);
  while(!Serial){
  }
  Serial.println("serial ok");
  Serial.println("");
  Serial.println("");
  delay(100);

}

void loop() {  
  if(digitalRead(BUTTON1) == HIGH){
    activeGlobal = true;
    active[0]='1';
  }
  if(digitalRead(BUTTON2) == HIGH){
    activeGlobal = true;
    active[1]='1';
  }
  if(digitalRead(BUTTON3) == HIGH){
    activeGlobal = true; 
    active[2]='1';
  }
  if(digitalRead(BUTTON4) == HIGH){
    activeGlobal = true;
    active[3]='1';
  }
  if(digitalRead(BUTTON5) == HIGH){
    activeGlobal = true;
    active[4]='1';
  }
  if(digitalRead(BUTTON6) == HIGH){
    activeGlobal = true;
    active[5]='1';
  }
  if(digitalRead(BUTTON7) == HIGH){
    activeGlobal = true;
    active[6]='1';
  }
  if(digitalRead(BUTTON8) == HIGH){
    activeGlobal = true;
    active[7]='1';
  }
  if(digitalRead(BUTTON9) == HIGH){
    activeGlobal = true;
    active[8]='1';
  }
  
  if(activeGlobal){
    sendPressed();
  }
}
