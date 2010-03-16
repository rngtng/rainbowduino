/*
* Implementation
*/

#include "Connection.h"

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
    //Serial.write(command);
    Serial.println(command, DEC);
    //TODO send slave number???
    //TODO send message length??
    while( howMany > 1 ) {
      command = Wire.receive();
      Serial.println( command, DEC);
      howMany--;
    } 
  }
}

//Slave I2C Callback
void onReceiveSlaveCallback(int howMany) {
  Con.inputBufferLength = howMany; //MaybeTODO use explizit -initMessage- command
  while( howMany > 0 ) {
    Con.processMessage( Wire.receive() );
    howMany--;
  }
}

void Connection::begin() {
  master = false;
  i2c_address = 0;
  //TODO serial = _serial;
  //TODO baud_rate = _baud_rate;

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
    Wire.send(I2C_CMD_HELLO);
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

void Connection::beginMaster(uint8_t master_address, bool update_adress) {
  //6b. use master I2C address, start master mode, open serial
  i2c_address = master_address;
  Serial.begin(BAUD_RATE);

  //7. start new I2C with given address
  if(update_adress) {
    Wire.begin(i2c_address);
  }
  else {
    TWAR = i2c_address << 1; // twi_setAddress
  }
   
  Wire.onReceive(onReceiveMasterCallback);
  master = true;

  //8. save new adress to EEPROM
  EEPROM.write(I2C_EEPROM_ADR, i2c_address);
  
  messageAwaiting = false;
  inputBufferLength = 0;
  slave_address_to_register = 0;
  
  last_slave_address = I2C_SLAVE_ADR;
  
  inputBuffer  = (uint8_t*) calloc( MESSAGE_BUFFER_SIZE, sizeof(uint8_t)); //buffer1; //point to buffers
  outputBuffer = (uint8_t*) calloc( MESSAGE_BUFFER_SIZE, sizeof(uint8_t)); //buffer2; //point to buffers
}

void Connection::beginSlave(uint8_t slave_address, bool update_adress) {
  //6a. use slave I2C adress, start slave mode
  i2c_address = slave_address;

  //7. start new I2C with given address
  if(update_adress) {
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

  //TODO notify serial about new slave

  slave_address_to_register = 0;
}


/******************************************************************/
void Connection::onMessageAvailable(conCallbackFunction newFunction) {
  onMessageAvailableCallback = newFunction;
}

void Connection::loop() {  
  if( Serial.available() ) Con.process( Serial.read() );  
  registerPendingSlave();
}  


uint8_t Connection::process(uint8_t serialByte) {
  //DEBUG Rainbowduino.set_frame_line(0,1,serialByte,0,0);
  if( !messageAwaiting && serialByte == MESSAGE_START ) {
    messageAwaiting = true;
    return 0;
  }

  if( messageAwaiting && inputBufferLength == 0 ) {
    inputBufferLength = serialByte;
    //TODO waht if message siez > allowed msg SIZE???
    return 1;
  }

  processMessage(serialByte);
}

uint8_t Connection::processMessage(uint8_t serialByte) {
  if( !messageAwaiting || inputBufferLength < 1 ) return 3;
  inputBuffer[inputBufferIndex] = serialByte;
  inputBufferIndex++;
  if( inputBufferIndex == inputBufferLength ) {
    //message fully received
    //1. swap buffers
    uint8_t* tmpPointer = outputBuffer;
    outputBuffer = inputBuffer;
    outputBufferLength = inputBufferLength;
    outputBufferIndex = 0;
    
    //2. reset inputBuffer and read process
    inputBuffer = tmpPointer;
    inputBufferLength = 0;
    inputBufferIndex = 0;
    messageAwaiting = false;
    //3. execute callback
    if(onMessageAvailableCallback != NULL) (*onMessageAvailableCallback)();
  }
  //TODO if (bufferIndex >= bufferLastIndex) reset();
  return 3;
}

int Connection::read() {
  if( available() < 1) return 0;
  return outputBuffer[outputBufferIndex++];
}

uint8_t Connection::available() {
  return (outputBufferLength - outputBufferIndex);
}


void Connection::write(uint8_t w) {
}

void Connection::ok(uint8_t command, uint8_t param) {
  if( master ) {
    Serial.flush();
    Serial.write(MESSAGE_OK);
    Serial.write(command);
    Serial.write(param);
  }
  else {
    //TODO I2C
  }
}