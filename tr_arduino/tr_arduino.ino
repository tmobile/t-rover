
//[1,2,3] channel 1 steering (left set)
//[1,2,3] channel 2 throttle (right set)
//[PxA,PxW,PxB] ==> [1,2,3] 

// MCP4142 --> ARDUINO
// CS(1) --> 10
// SI(3) --> 11
// SCK(2) --> 13
// VSS(4) --> GND
// VDD(14) --> +5V

#include <SPI.h>
// set pin 10 as the slave select for the digital pot:
const int slaveSelectPin = 10;
const int WIPER_PIN = A0;        // Goes to X9C103P VW pin - Analog voltage output of pot
const float V_REF = 5.0;          // Change if using different Vref
const int SW_pin = 2; // digital pin connected to switch output
const int X_pin = A1; // analog pin connected to X output
const int Y_pin = A2; // analog pin connected to Y output

int CMD_BYTE_POT_0 = 0;
int CMD_BYTE_POT_1 = 16;
int x_val = 503;
int y_val = 62;
int y_val_old = 62;
int counter = 0;
unsigned long time = 1;
unsigned long prev_time = 0;
bool DEBUG = false;
float FPS = 60.0f;
float target_delay_ms = 1.0/FPS * 1000.0f;

void setup() {
  Serial.begin(9600);
  Serial.print ("Initial Voltage Setting: ");

  // set the slaveSelectPin as an output:
  pinMode (slaveSelectPin, OUTPUT);
  // initialize SPI:
  SPI.begin();
 }


void loop() {

  time = millis();

//  Serial.print("Switch:  ");
//  Serial.print(digitalRead(SW_pin));
  x_val = 128 - analogRead(X_pin) / 8;  //forward revere
  y_val = 128 - analogRead(Y_pin) / 8;  //left right

  printOutput(time, x_val, y_val);
  printDebug(counter, time, x_val, y_val, prev_time);
  
  counter++;
  int CommandByte = 16; // to Write to Pot 1
  digitalPotWrite(CMD_BYTE_POT_1, x_val); delayMicroseconds(5);
  digitalPotWrite(CMD_BYTE_POT_0, y_val); 
  y_val_old = y_val;
  checkDelay(time, prev_time);
  prev_time = time;

}

void printOutput(long time, int x_val, int y_val) {
  Serial.print("s,");
  Serial.print(time);
  Serial.print(",");
  Serial.print(x_val);
  Serial.print(",");
  Serial.print(y_val);
  Serial.print("\n");
}

void printDebug(int counter, long time, int x_val, int y_val, long prev_time) {
  if (counter % 1000 == 0) {
//    Serial.print("time:");
//    Serial.print(time);
    if (DEBUG) {
      Serial.print("time:");
      Serial.print(time);
      Serial.print("\n");
      Serial.print("X-axis: ");
      Serial.print(x_val);
      Serial.print("\n");
      Serial.print("Y-axis: ");
      Serial.println(y_val);
      Serial.print("\n");
      Serial.print("\ncurrent time:");
      Serial.print(time);
      Serial.print("\nprev time:");
      Serial.print(prev_time);
      Serial.print("\ndifference: ");
      Serial.print(time - prev_time);
      Serial.print("\ncounter: ");
      Serial.print(counter);
      Serial.print("\nfps: ");
      Serial.print(((long)counter * 1000L)/(time - prev_time));
      Serial.print("\n\n");
    }
    counter = 0;
  }
}

void checkDelay(int time, int prev_time) {
  float diff = target_delay_ms - (float) (time - prev_time) ;
  if (diff > 1.0f) {
    if (DEBUG) {
      Serial.print("delaying to keep frame rate down: ");
      Serial.print(diff);
      Serial.print("\n");
      Serial.print(target_delay_ms);
      Serial.print("\n");
      Serial.print((float) (time - prev_time));
      Serial.print("\n\n");
    }
    delay(diff); 
  }
}

int digitalPotWrite(int CommandByte, int value) {
// take the SS pin low to select the chip:
  digitalWrite(slaveSelectPin,LOW);
  // send in the address and value via SPI:
  SPI.transfer(CommandByte);
  SPI.transfer(value);
  // take the SS pin high to de-select the chip:
  digitalWrite(slaveSelectPin,HIGH);
  delayMicroseconds(5);
  PrintVoltage();
}

void PrintVoltage()
{
  return;
  int sampleADC = analogRead(WIPER_PIN);      // Take reading on wiper pin
  float volts = (sampleADC * V_REF) / 1023.0; // Convert to voltage
  Serial.println("   ADC = ");
  Serial.print(sampleADC);
  Serial.print("tVoltage = ");
  Serial.println(volts, 3);
}
