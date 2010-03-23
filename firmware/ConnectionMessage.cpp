/*
* Implementation
*/

#include "ConnectionMessage.h"
#include "WProgram.h"

ConnectionMessage::ConnectionMessage() {
  reset();
}

void ConnectionMessage::consume( uint8_t dataByte ){
	if(ready()) return;
	if( writeIndex == 0 && dataByte != COMMAND && dataByte != OK && dataByte != ERROR) return;
  data[writeIndex++] = dataByte;
}

void ConnectionMessage::reset() {
  writeIndex = 0;
  readIndex = 0;
  data[INDEX_LENGTH] = 0;
}

bool ConnectionMessage::ready() {
return (writeIndex == totalLength());
}

bool ConnectionMessage::isError() {
  return type() == ERROR;
}

bool ConnectionMessage::isOk() {
  return type() == OK;
}

bool ConnectionMessage::isCommand() {
  return type() == COMMAND;
}

bool ConnectionMessage::is(uint8_t command) {
  return ready() && data[INDEX_COMMAND] == command;
}

uint8_t ConnectionMessage::type() {
  if( !ready() ) return 0;  
  return data[INDEX_TYPE];
}

uint8_t ConnectionMessage::receiver() {
  if( !ready() ) return 0;  
  return data[INDEX_RECEIVER];
}

uint8_t ConnectionMessage::command() {
  if( !ready() ) return 0;  
  return data[INDEX_COMMAND];
}

uint8_t ConnectionMessage::param() {
  if( !ready() ) return 0;  
  return data[HEADER_LENGTH];
}

uint8_t ConnectionMessage::paramRead() {
  if( !ready() ) return 0;
  return data[HEADER_LENGTH + readIndex++];
}

uint8_t ConnectionMessage::totalLength() {
 return HEADER_LENGTH + data[INDEX_LENGTH]; 
}