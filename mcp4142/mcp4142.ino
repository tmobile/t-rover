#include <SPI.h>
// set pin 10 as the slave select for the digital pot:
const int slaveSelectPin = 10;
const int WIPER_PIN = A0;        // Goes to X9C103P VW pin - Analog voltage output of pot
const float V_REF = 5.0;          // Change if using different Vref

void setup() {
  Serial.begin(9600);
  Serial.print ("Initial Voltage Setting: ");
  PrintVoltage();                             // Print X9C103P power up value

  // set the slaveSelectPin as an output:
  pinMode (slaveSelectPin, OUTPUT);
  // initialize SPI:
  SPI.begin();
 }

void loop() {
  // go through Pot 1 first then Pot 2, CommandByte = 16 and then CommandByte = 0:
  digitalPotWrite(0, 0);  //turn off Pot 0 so it doesn’t float, partially lit:
  delay(10);
  int CommandByte = 16; // to Write to Pot 1
  // change the resistance on this pot from min to max:
  for (int level = 0; level < 127; level++) {
    digitalPotWrite(CommandByte, level);
    delay(10);
  }
  // wait at the top:
  delay(100);
  // change the resistance on this channel from max to min:
  for (int level = 0; level < 127; level++) {
    digitalPotWrite(CommandByte, 127 - level);
    delay(10);
  }
  
  digitalPotWrite(16, 0); //make sure Pot 1 is off:
  delay(10);
  CommandByte = 0; // to Write to Pot 0
  // change the resistance on this pot from min to max:
  for (int level = 0; level < 127; level++) {
    digitalPotWrite(CommandByte, level);
    delay(10);
  }
  // wait at the top:
  delay(100);
  // change the resistance on this channel from max to min:
  for (int level = 0; level < 127; level++) {
    digitalPotWrite(CommandByte, 127 - level);
    delay(10);
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
  PrintVoltage();
}

void PrintVoltage()
{
  int sampleADC = analogRead(WIPER_PIN);      // Take reading on wiper pin
  float volts = (sampleADC * V_REF) / 1023.0; // Convert to voltage
  Serial.print("   ADC = ");
  Serial.print(sampleADC);
  Serial.print("tVoltage = ");
  Serial.println(volts, 3);
}
