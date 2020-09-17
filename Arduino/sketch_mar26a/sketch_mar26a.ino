
// declare variables
char command;
String string;
//define leds
int led4 = 4;
int led5 = 5;
int led6 = 6;
int led7 = 7;
int led8 = 8;

//initialize output pins in start
  void setup()
  {
    Serial.begin(9600);
    pinMode(led4, OUTPUT);
    pinMode(led5, OUTPUT);
    pinMode(led6, OUTPUT);
    pinMode(led7, OUTPUT);
    pinMode(led8, OUTPUT);
  }

//get serial input commands
  void loop()
  {
    if (Serial.available() > 0) 
    {string = "";}

   // while serial input command is coming
    while(Serial.available() > 0)
    {
      command = ((byte)Serial.read());
      
      if(command == ':')
      {
        break;
      }
      
      else
      {
        string += command;  // array each command to string
      }
      
      delay(3);
    }

   // if input string is P1 (Port 1)
    if(string == "P1")
    {
        ledOn(led4);

    }
     // if input string is P2 (Port 2)
       if(string == "P2")
    {
        ledOn(led5);

    }
     // if input string is P3 (Port 3)
           if(string == "P3")
    {
        ledOn(led6);

    }
       // if input string is P4 (Port 4)
    if(string == "P4")
    {
        ledOn(led7);

    }
       // if input string is P5 (Port 5)
        if(string == "P5")
    {
        ledOn(led7);

    }

     // if input string is TF (turn of or cancel)
    if(string =="TF")
    {
        ledOff(led4);
        ledOff(led5);
        ledOff(led6);
        ledOff(led7);
        ledOff(led8);

        Serial.println(string);
    }
 }

 // basic function for turn on LEDs
void ledOn(int led)
   {
      analogWrite(led, 255);
      delay(15);
    }
  // basic function for turn off LEDs
 void ledOff(int led)
 {
      analogWrite(led, 0);
      delay(15);
 }

