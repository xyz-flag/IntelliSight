#include <Wire.h> //i2c library
#include <math.h> //trig function
#include <Adafruit_Sensor.h> //base library for sensors
#include <Adafruit_BNO055.h>  //BNO055 specific library
#include <utility/imumaths.h> //vector matrix and IMU math library
#include "BluetoothSerial.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif


BluetoothSerial SerialBT;
#define BNO055_SAMPLERATE_DELAY_MS (50)// set pause btw sample
const int sampleNum = 20;
const int difference = 15;
int sampleIndex = sampleNum;
double sample[sampleNum];
float compass_data = 0;
double noddingSignal = -10;
bool collectingSample = false;
float minv,maxv,raw_compass;

Adafruit_BNO055 bno = Adafruit_BNO055();

void setup() {
  Serial.begin(115200); // create the serial connection at 115200 baud
  SerialBT.begin("ESP32test");

  if(!bno.begin()) //initialize the sensor
  {
    /* There was a problem detecting the BNO055 ... check your connections */
    Serial.print("Ooops, no BNO055 detected ... Check your wiring or I2C ADDR!");
    while(1);
  }
  uint8_t system, gyro,accel,mg = 0;
   bno.getCalibration(&system,&gyro,&accel,&mg);

  delay(1000); // offer time to initialize

  /*NOT SURE USE THIS OR NOT*/
  bno.setExtCrystalUse(false);  // Tell sensor to use external crystal
}



void loop() {

  sensors_event_t event;
  bno.getEvent(&event);

  float pitchDeg = event.orientation.y;
  Serial.print(event.orientation.y, 4);
  raw_compass = event.orientation.x;

  nodding_or_not(pitchDeg);
  //get_compass WILL BE CALLED IN THE FUNCTION WHEN IT IS TRUE




  // bluetooth communication

  if (Serial.available()) {
    SerialBT.write(Serial.read());
  }
  if (SerialBT.available()) {
    Serial.write(SerialBT.read());
  }


   delay(BNO055_SAMPLERATE_DELAY_MS);
  }

  void nodding_or_not(float pitchDeg){
    //determin it's first nodding or not
    if((pitchDeg < noddingSignal) && (sampleIndex == sampleNum)){
      sampleIndex --;
      collectingSample = true;
      Serial.print(sampleIndex);
      Serial.print(" | ");
      minv = pitchDeg;
      maxv = pitchDeg;
    }
    //start to collect the following pitch value
    //only collect data after first nodding detected
    else if(collectingSample == true) {

      //store in array 19,18,......0
      if (sampleIndex > 0){
        sample[sampleIndex] = pitchDeg;
        compass_data = compass_data + get_compass(raw_compass);
        sampleIndex --;
        Serial.println(pitchDeg);
        Serial.print(" | ");
        minv = min(minv,pitchDeg);
        maxv = max(maxv,pitchDeg);
      }

      //collecting ended
      //turn off collecting signal and start to compare
      else if (sampleIndex == 0){
        sample[sampleIndex] = pitchDeg;
        compass_data = compass_data + get_compass(raw_compass);
        sampleIndex --;
        minv = min(minv,pitchDeg);
        maxv = max(maxv,pitchDeg);

        collectingSample = false;
        sampleIndex = sampleNum;
        if ((maxv-minv) >= difference){
          Serial.println("yes");
          Serial.println("yes");
          SerialBT.println("yes");

          double compass_result = compass_data / sampleNum;
          compass_data = 0;
          SerialBT.println(compass_result);
        }
        else{
          Serial.println("no");
          }
        }
    }
  }

  float get_compass(float raw_compass){
    float i = 0;
    if ( raw_compass > 180){
      i = raw_compass -90;
    }
    else{
      i = raw_compass;
    }
    return i;
  }
