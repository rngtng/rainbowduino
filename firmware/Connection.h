/* Com:
 * Abstraction of Serial / Wire commmunication
 */
 
#ifndef Com_h
#define Com_h

#define BAUD_RATE 9600

#define I2C_START_ADR 100
#define I2C_MASTER_ADR 7
#define I2C_SLAVE_ADR 10

#define I2C_CMD_HELLO 23
#define I2C_CMD_OK 42

#define I2C_WAIT_FOR_ADRESS_TIMEOUT 500 //ms
#define I2C_WAIT_FOR_ADDRESS_RETRYS 3 //number of retrys to get an slave adress

#define I2C_EEPROM_ADR 0 //where the gets written to

#define STARTUP_DELAY_FACTOR 100 //in ms

#include "WProgram.h"

class Connection {

public:
  bool master = false;
  volatile int i2c_address = 0;
  volatile int last_slave_address = 0;
  //volatile int last_slave_address = 0; //slave number to adress mapper ??
  
  void begin();
  void send(int data);
  int receive();
  byte available();
  void beginTransmission(byte channel = 0);
  void endTransmission();

private: 
  //_read  
};

external Connection Con;

#endif