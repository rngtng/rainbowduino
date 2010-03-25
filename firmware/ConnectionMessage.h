/* Com:
 * Abstraction of Serial / Wire commmunication
 * message :
 * 1. type: OK | COMMAND | ERROR
 * 2. receiver
 * 3. command
 * 4. data length
 * 5. data
 */
 
#ifndef ConnectionMessage_h
#define ConnectionMessage_h

#define MESSAGE_DATA_SIZE 32 //max 128
#define HEADER_LENGTH 4

/* Header Codes */
#define COMMAND     0xFF  // followed by commands
#define REQUEST     0xFE  // followed by commands
#define RESPONSE    0x0F  // followed by return params
#define ERROR       0x01  // followed by error code

#define INDEX_TYPE     0
#define INDEX_RECEIVER 1
#define INDEX_COMMAND  2
#define INDEX_LENGTH   3

#include "WProgram.h"

class ConnectionMessage {
 public:
   uint8_t data[MESSAGE_DATA_SIZE];
   uint8_t writeIndex;
   uint8_t readIndex;
   
   ConnectionMessage();
   void consume( uint8_t dataByte );
   void reset();
   bool ready();
   bool isCommand();
   bool isRequest();
   bool isResponse();
   bool isError();
   bool is(uint8_t command);
   uint8_t type();
   uint8_t receiver();
   uint8_t command();
   uint8_t param();
   uint8_t paramRead();
   uint8_t totalLength();
};

#endif