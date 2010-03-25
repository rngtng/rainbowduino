/*
* Implementation
*/

#include "Connection.h"
#include "ConnectionMessage.h"
#include "WProgram.h"

#include <EEPROM.h>
#include "Wire.h"

Connection Con; 

void onReceiveInitCallback(int howMany) {
  uint8_t command = Wire.receive();  
  if( command == I2C_CMD_OK) {  //Maybe TODO send explizit -I2C_CMD_SET_ADDRESS- command
    Con.i2c_address = Wire.receive();
  }
}

//Master I2C Callback
void onReceiveMasterCallback(int howMany) {
  uint8_t command = Wire.receive();
  if( command == I2C_CMD_HELLO) {  //we can't instant replay here ;-(
    command = Wire.receive();
    if( Con.slave_address_to_register == 0 )  Con.slave_address_to_register = command;
  }
  else {   //forward data to serial
    Serial.write( command );
    while( howMany > 1 ) {
      command = Wire.receive();
      Serial.write( command );
      howMany--;
    } 
  }
}

//Slave I2C Callback
void onReceiveSlaveCallback(int howMany) {
  uint8_t serialByte;
  while( howMany > 0 ) {
    serialByte = Wire.receive();
    Con.inputMessage->consume(serialByte);
    howMany--;
  }
  //Rainbowduino.set_frame_line(0,7,0,serialByte,serialByte);  
}

Connection::Connection() {
  //Init default values
  slave_address_to_register = 0;
  last_slave_address = I2C_SLAVE_ADR;
  
  inputMessage = &buffer1;
  outputMessage = &buffer2;
}

void Connection::begin() {
  master = false;
  i2c_address = 0;

//TODO: if we detect a working serial port, take master role in any case, notify all others to restart
// -> http://stackoverflow.com/questions/195304/check-if-serial-port-is-listening

  // 1. read last_adress from EEPROM and normalize to value between 0-16 
  uint8_t old_i2c_address = EEPROM.read(I2C_EEPROM_ADR) & 0x0F;

  // 2. startup delay 
  if( old_i2c_address > I2C_MASTER_ADR) {
     int delay_time =  (old_i2c_address - I2C_MASTER_ADR ) * STARTUP_DELAY_FACTOR;
     delay(delay_time);
  }
  // 3. open I2C connection with I2C_START_ADR as offset
  i2c_address = old_i2c_address + I2C_START_ADR;  
  Wire.begin(i2c_address);

  // 4. register Callback fnc
  Wire.onReceive(onReceiveInitCallback);

  //5. wait for response with new slave address
  uint8_t c = I2C_WAIT_FOR_ADDRESS_RETRYS;  
  int delay_time2 =  max(1, old_i2c_address - I2C_MASTER_ADR) * I2C_WAIT_FOR_ADDRESS_TIMEOUT;
  while( i2c_address >= I2C_START_ADR && c > 0) {  
    // 5a. make call to master with static number.
    Wire.beginTransmission(I2C_MASTER_ADR);
    Wire.send(I2C_CMD_HELLO); //TODO user proper Protocoll here???
    Wire.send(i2c_address);   //send adress where to respond to
    Wire.endTransmission();
    delay(delay_time2);
    c--;
  }  

  if(i2c_address < I2C_START_ADR) {  //success
    beginSlave(i2c_address, false);
  }
  else { // failure
    beginMaster(I2C_MASTER_ADR, false);
  }
}

void Connection::beginMaster(uint8_t master_address, bool initWire) {
  //6b. use master I2C address, start master mode, open serial
  i2c_address = master_address;
  Serial.begin(BAUD_RATE);

  //7. start new I2C with given address
  if(initWire) {
    Wire.begin(i2c_address);
  }
  else {
    TWAR = i2c_address << 1; // twi_setAddress
  }
   
  Wire.onReceive(onReceiveMasterCallback);
  master = true;

  //8. save new adress to EEPROM
  EEPROM.write(I2C_EEPROM_ADR, i2c_address);
  
  //9.
  //TODO broadcast to all slave to reregister
}

void Connection::beginSlave(uint8_t slave_address, bool initWire) {
  //6a. use slave I2C adress, start slave mode
  i2c_address = slave_address;

  //7. start new I2C with given address
  if(initWire) {
    Wire.begin(i2c_address);
  }
  else {
    TWAR = i2c_address << 1; // twi_setAddress
  }  
  Wire.onReceive(onReceiveSlaveCallback);
  master = false;

  //8. save new adress to EEPROM
  EEPROM.write(I2C_EEPROM_ADR, i2c_address);
}

/******************************************************************/
void Connection::registerPendingSlave() {
  if( slave_address_to_register == 0) return;
  last_slave_address++;
  Wire.beginTransmission(slave_address_to_register);
  Wire.send(I2C_CMD_OK);
  Wire.send(last_slave_address);   //send adress where to respond to
  Wire.endTransmission();

  //notify serial about new slave
  sendCommand( SLAVE_NEW, last_slave_address);
  
  slave_address_to_register = 0;
}


/******************************************************************/
void Connection::onMessageAvailable(conCallbackFunction newFunction) {
  onMessageAvailableCallback = newFunction;
}

void Connection::loop() {  
  registerPendingSlave();
  if( Serial.available() ) { 
    uint8_t serialByte = Serial.read();
    //Rainbowduino.set_frame_line(0,7,serialByte,0,0);
    inputMessage->consume(serialByte);
  }
  processMessage();
}  


void Connection::processMessage() {
  if( !inputMessage->ready() ) return;
  //message fully received
  //1. swap buffers
  ConnectionMessage* tmpPointer = outputMessage;
  outputMessage = inputMessage;
  
  //2. reset inputBuffer and read process
  inputMessage = tmpPointer;
  inputMessage->reset();
  //3. execute callback
  if(master && outputMessage->receiver() > i2c_address) {
    forwardMessage();
    //Rainbowduino.set_frame_line(0,7,0,0,255);
  }
  else {
    if(onMessageAvailableCallback != NULL) (*onMessageAvailableCallback)();
    //Rainbowduino.set_frame_line(0,7,0,255,0);
  }
}

void Connection::sendCommand(uint8_t command, uint8_t param) {
  send(COMMAND, command, param);
}

void Connection::sendResponse(uint8_t command, uint8_t param) {
 send(RESPONSE, command, param);
}

void Connection::sendError(uint8_t command, uint8_t param) {
 send(ERROR, command, param);
}

void Connection::send(uint8_t type, uint8_t command, uint8_t param) {
  if( master ) {
//    Serial.flush();
      Serial.write(type);
      Serial.write((byte) 0); //as we are master!
      Serial.write(command);
      Serial.write(1); //one more to follow
      Serial.write(param);
  }
  else {
    Wire.beginTransmission(I2C_MASTER_ADR);
    Wire.send(type);
    Wire.send(i2c_address);
    Wire.send(command);
    Wire.send(1);
    Wire.send(param);
    Wire.endTransmission();
  }
}

void Connection::forwardMessage() {
  Wire.beginTransmission(outputMessage->receiver());
  for( uint8_t k = 0; k < outputMessage->totalLength(); k++) {
      uint8_t serialByte = outputMessage->data[k];
      //Rainbowduino.set_frame_line(0,3,0,serialByte,k);
      Wire.send(serialByte);
  }
  Wire.endTransmission();
}