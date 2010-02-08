/*
*/
#include "Connection.h"

#include "WProgram.h"

#include <EEPROM.h>
#include "Wire.h"

Connection Con;


void receive_register_response(int howMany) {
  if( Wire.receive() == I2C_CMD_OK) {  //TODO send explizit -set address- command
    Con.i2c_address = Wire.receive();
  }
}

void receive_register_request(int howMany) {
  if(Wire.receive() == I2C_CMD_HELLO) {
    //Con.register_slave( Wire.receive() );
  }
}

void receive_command(int howMany) {
  
}


void Connection::begin() { //TODO I2C & Serail callback FNC ??
  master = false;
  serial = false;
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
  Wire.onReceive(receive_register_response);
    
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
    //6a. use slave I2C adress, start slave mode
    Wire.onReceive(receive_command);
    master = false;
  }
  else { // failure
    //6b. use master I2C address, start master mode, open serial
    Serial.begin(BAUD_RATE);    
    i2c_address = I2C_MASTER_ADR;
    Wire.onReceive(receive_register_request);
    serial = true;
    master = true;
  }

  //7. start new I2C with given address
  Wire.begin(i2c_address);
  
  //8. save new adress to EEPROM
  EEPROM.write(I2C_EEPROM_ADR, i2c_address);
  
}

byte Connection::receive() {
  return (serial) ? Serial.read() : Wire.receive();
}

byte Connection::read() {
  return receive();
}


byte Connection::wait_and_read() {
 //TODO add timeout + error???
 while( !available() );
 return read();
}

void Connection::send(int data) {
  (serial) ? Serial.write(data) : Wire.send(data);
}

void Connection::write(int data) {
  send(data);
}

byte Connection::available() {
  return (serial) ? Serial.available() : Wire.available();
}

void Connection::beginTransmission(byte channel) {
  if(!serial) Wire.beginTransmission(channel);
}

void Connection::endTransmission() {
  if(!serial) Wire.endTransmission();
}

void Connection::ok(byte command, byte param) {  
   //Serial.flush();
   beginTransmission(I2C_MASTER_ADR);
   write(I2C_CMD_OK);
   write(command);
   write(param);
   endTransmission();
}


/*
struct message {
  byte command;
  byte 
}
*/


///////////////////////////////////////////////////////////////////////////
// Serial Stuff
//
// byte _read() {

//   return (serial) = Serial.read();

// }

// 



