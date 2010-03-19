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

#define MESSAGE_DATA_SIZE 32 //max 128
#define HEADER_LENGTH 4

#define INDEX_TYPE     0
#define INDEX_RECEIVER 1
#define INDEX_COMMAND  2
#define INDEX_LENGTH   3

/* Header Codes */
#define COMMAND     0xFF  // followed by commands
#define OK          0x0F  // followed by return params
#define ERROR       0x01  // followed by error code

#define SLAVE_NEW   202 //returns OK + slave number

#include <Wire.h>
#include "WProgram.h"
//DEBUG 
#include "Rainbowduino.h"

 extern "C" {
 // callback function
     typedef void (*conCallbackFunction)(void);
 }

class Message {
 public:
   uint8_t data[MESSAGE_DATA_SIZE];
   uint8_t writeIndex;
   uint8_t readIndex;
   
   Message();
   void consume( uint8_t dataByte );
   void reset();
   bool ready();
   bool isError();
   bool isOk();
   bool isCommand();
   bool is(uint8_t command);
   uint8_t type();
   uint8_t param();
   uint8_t paramRead();
};
 
class Connection {
public:
  bool master;
  uint8_t last_slave_address;
  
  volatile uint8_t i2c_address;
  volatile uint8_t slave_address_to_register;
  
  void loop();
  void begin();
  void beginMaster(uint8_t master_address = I2C_MASTER_ADR, bool update_adress = true);
  void beginSlave(uint8_t slave_address, bool update_adress = true);
  void onMessageAvailable(conCallbackFunction newFunction);
  uint8_t read();
  uint8_t process(uint8_t serialByte);
  bool available();
  
  void registerPendingSlave();
  
  void ok(uint8_t command, uint8_t param);
  void command(uint8_t command, uint8_t param);
  
  //TODO
  // void error(uint8_t command, uint8_t param);
  // void command(uint8_t command, uint8_t param);
  //void send(uint8_t type, uint8_t command, uint8_t param);
  
  
  //TODO
  void write(uint8_t data);
  
//private: 
  conCallbackFunction onMessageAvailableCallback;
  
  Message* inputMessage; // Buffer that holds the data 
  Message* outputMessage; // Buffer that holds the data 
};

extern Connection Con;
#endif
