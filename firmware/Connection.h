/* Com:
 * Abstraction of Serial / Wire commmunication
 * message :
 * 1. open BYTE 255
 * 2. adress BYTE 
 * 3. msg length BYTE
 * 4. data
 *      COMMAND
 *      DATA
 */
 
#ifndef Com_h
#define Com_h

#define BAUD_RATE 9600

#define I2C_START_ADR 100
#define I2C_MASTER_ADR 7
#define I2C_SLAVE_ADR 10

#define I2C_CMD_HELLO 23
#define I2C_CMD_OK 42

#define I2C_WAIT_FOR_ADDRESS_TIMEOUT 500 //ms
#define I2C_WAIT_FOR_ADDRESS_RETRYS 3 //number of retrys to get an slave adress

#define I2C_EEPROM_ADR 0 //where the gets written to

#define STARTUP_DELAY_FACTOR 100 //in ms

#define MESSAGE_BUFFER_SIZE 64 //max 128
#define MESSAGE_START 255

#include <Wire.h>
#include "WProgram.h"
#include "Rainbowduino.h"

 extern "C" {
 // callback function
     typedef void (*conCallbackFunction)(void);
 }

class Connection {

public:
  bool master;
  volatile int i2c_address;
  volatile int last_slave_address;
  //volatile int last_slave_address = 0; //slave number to adress mapper ??
  
  void beginAuto();
  void beginMaster(int master_address = I2C_MASTER_ADR);
  void beginSlave(int slave_address);
  void onMessageAvailable(conCallbackFunction newFunction);
  int read();
  uint8_t process(byte serialByte);
  uint8_t processMessage(byte serialByte);
  uint8_t available();
  
  //TODO
  void write(int data);
  
  /*void send(int data);
  
  byte receive();
  byte read();
  byte wait_and_read();
  //byte available();
  //void beginTransmission(byte channel = 0);
  //void endTransmission();
  void ok(byte command, byte param); */

private: 
  //uint8_t next();
  //void reset(); // to reset message parsing process
  
  bool messageAwaiting; //status wheter Message parsing is in progress
  
  conCallbackFunction callback;
  
  byte buffer1[MESSAGE_BUFFER_SIZE]; // Buffer that holds the data
  byte* inputBuffer; // Pointer to current data
  uint8_t inputBufferLength; //number of bytes to receive
  uint8_t inputBufferIndex; // Index where to write the data
  
  byte buffer2[MESSAGE_BUFFER_SIZE]; // Buffer that holds the data
  byte* outputBuffer;
  uint8_t outputBufferLength;
  uint8_t outputBufferIndex; // Index where to write the data
};

extern Connection Con;
#endif