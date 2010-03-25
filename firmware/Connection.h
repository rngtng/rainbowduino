/* Com:
 * Abstraction of Serial / Wire commmunication
 * message :
 * 1. type: OK | COMMAND | ERROR
 * 2. receiver
 * 3. command
 * 4. data length
 * 5. data
 */
 
#ifndef Connection_h
#define Connection_h

#define BAUD_RATE 57600

#define I2C_START_ADR 64
#define I2C_MASTER_ADR 7
#define I2C_SLAVE_ADR 10

#define I2C_CMD_HELLO 23
#define I2C_CMD_OK 42

#define I2C_WAIT_FOR_ADDRESS_TIMEOUT 30 //ms
#define I2C_WAIT_FOR_ADDRESS_RETRYS 3 //number of retrys to get an slave adress

#define I2C_EEPROM_ADR 0 //where the gets written to

#define STARTUP_DELAY_FACTOR 250 //in ms

#define SLAVE_NEW 202 // param Slave nr

#include <Wire.h>
#include "WProgram.h"
//DEBUG   
#include "Rainbowduino.h"
#include "ConnectionMessage.h"

 extern "C" {
 // callback function
     typedef void (*conCallbackFunction)(void);
 }
 
class Connection {
public:
  bool master;
  uint8_t last_slave_address;
  
  volatile uint8_t i2c_address;
  volatile uint8_t slave_address_to_register;
  
  Connection();
  void loop();
  void begin();
  void beginMaster(uint8_t master_address = I2C_MASTER_ADR, bool initWire = true);
  void beginSlave(uint8_t slave_address, bool initWire = true);
  void onMessageAvailable(conCallbackFunction newFunction);
  void processMessage();
  bool available();
  
  void registerPendingSlave();
  
  void sendResponse(uint8_t command, uint8_t param);
  void sendCommand(uint8_t command, uint8_t param);
  void sendError(uint8_t command, uint8_t param);
  void send(uint8_t type, uint8_t command, uint8_t param);
  void forwardMessage();

//private: 
  conCallbackFunction onMessageAvailableCallback;
  
  ConnectionMessage* inputMessage; // Buffer that holds the data 
  ConnectionMessage* outputMessage; // Buffer that holds the data 
  ConnectionMessage buffer1; // Buffer that holds the data 
  ConnectionMessage buffer2; // Buffer that holds the data  
};

extern Connection Con;
#endif
