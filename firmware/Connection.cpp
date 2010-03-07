/*
* Implementation
*/

#include "Connection.h"

#include "WProgram.h"

#include <EEPROM.h>
#include "Wire.h"

Connection Con; 

void receive_register_response_callback(int howMany) {
  if( Wire.receive() == I2C_CMD_OK) {  //TODO send explizit -set address- command
    Con.i2c_address = Wire.receive();
  }
}

void receive_register_request_callback(int howMany) {
  if(Wire.receive() == I2C_CMD_HELLO) {
    //Con.register_slave( Wire.receive() );
  }
  //else pass data to serial???
}

void receive_command_callback(int howMany) {
  //Con.processMessage(Wire.receive() );
  //TODO send recieved byte to Con.
}

void Connection::beginAuto() { //TODO I2C & Serail callback FNC ??
  master = false;
  i2c_address = 0;
  last_slave_address = 0;
  //TODO serial = _serial;
  //TODO baud_rate = _baud_rate;

  // 1. read last_adress from EEPROM and normalize to value between 0-16 
  i2c_address = EEPROM.read(I2C_EEPROM_ADR) & 0x0F;

  // 2. startup delay 
  int delay_time = i2c_address * STARTUP_DELAY_FACTOR;
  delay(delay_time);

  // 3. open I2C connection with I2C_START_ADR as offset
  i2c_address += I2C_START_ADR;
  Wire.begin(i2c_address);

  // 4. register Callback fnc
  Wire.onReceive(receive_register_response_callback);

  //5. wait for response with new slave address
  int c = I2C_WAIT_FOR_ADDRESS_RETRYS;
  while( i2c_address >= I2C_START_ADR && c > 0) {  
    // 5a. make call to master with static number.
    Wire.beginTransmission(I2C_MASTER_ADR);
    Wire.send(I2C_CMD_HELLO);
    Wire.send(i2c_address);   //send adress where to respond to
    Wire.endTransmission();

    delay(I2C_WAIT_FOR_ADDRESS_TIMEOUT);
    c--;
  }  

  if(i2c_address < I2C_START_ADR) {  //success
    beginSlave(i2c_address);
  }
  else { // failure
    beginMaster();
  }
}

void Connection::beginMaster(int master_address) {
  //6b. use master I2C address, start master mode, open serial
  i2c_address = master_address;
  Serial.begin(BAUD_RATE);

  //7. start new I2C with given address
  Wire.begin(i2c_address);
  Wire.onReceive(receive_register_request_callback);
  master = true;

  //8. save new adress to EEPROM
  EEPROM.write(I2C_EEPROM_ADR, i2c_address);
  
  messageAwaiting = false;
  inputBufferLength = 0;
  outputBuffer = &buffer1[0];
  inputBuffer  = &buffer2[0];
}

void Connection::beginSlave(int slave_address) {
  //6a. use slave I2C adress, start slave mode
  i2c_address = slave_address;

  //7. start new I2C with given address
  Wire.begin(i2c_address);   
  Wire.onReceive(receive_command_callback);
  master = false;

  //8. save new adress to EEPROM
  EEPROM.write(I2C_EEPROM_ADR, i2c_address);
}

void Connection::onMessageAvailable(conCallbackFunction newFunction) {
  callback = newFunction;
}

uint8_t Connection::process(byte serialByte) {
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

uint8_t Connection::processMessage(byte serialByte) {
  if( !messageAwaiting || inputBufferLength < 1 ) return 3;
  inputBuffer[inputBufferIndex] = serialByte;
  inputBufferIndex++;
  if( inputBufferIndex == inputBufferLength ) {
    //message fully received
    //1. swap buffers
    byte* tmpPointer = outputBuffer;
    outputBuffer = inputBuffer;
    outputBufferLength = inputBufferLength;
    outputBufferIndex = 0;
    
    //2. reset inputBuffer and read process
    inputBuffer = tmpPointer;
    inputBufferLength = 0;
    inputBufferIndex = 0;
    messageAwaiting = false;
    //3. execute callback
    if(callback != NULL) (*callback)();
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


void Connection::write(int w) {
}

void Connection::ok(byte command, byte param) {
  Serial.flush();
  Serial.write(MESSAGE_OK);
  Serial.write(command);
  Serial.write(param);
}