/*
 * Firmware.h version 2.0 - to run with Rainbowduino/Arduino Processing library
 * Copyright (c) 2009 Tobias Bielohlawek -> http://www.rngtng.com/mtxcontrol
 *
 */

#include "Wire.h"
#include "RCodes.h" //API Codes

#define BAUD_RATE 57600
#define DEFAULT_SPEED 150

int current_slave_nr;
int frame_nr;

void setup() {
  Serial.begin(BAUD_RATE);
  Wire.begin();
  frame_nr = 1;
  current_slave_nr = 1;
}

void loop() {
  check_serial();
}

void check_serial() {
  if(!Serial.available()) return;
  byte received = read_serial();
  if(received == COMMAND) {
    received = wait_and_read_serial(); // read slave nummber
    switch(received)  {
    case SLAVE_ACTIV:
      current_slave_nr = wait_and_read_serial();
      break;       
    case PING:
    case API_VERSION:
      ok(received, API_VERSION_NR);
      break;

      //send current slave
    case RESET:
    case STOP:
    case START:
    case FRAME_GET:
    case BRIGHTNESS_GET:    
    case BUFFER_LENGTH:      
    case BUFFER_SAVE: 
    case BUFFER_LOAD:      
      forward_command(received, 0);
      forward_response(3);
      break;

      //send to first
    case SPEED_GET:
      break;

      //send all
    case SPEED_INC:
    case SPEED_DEC:  
      break;

    case BUFFER_SET_AT:
      forward_command(received, 25);
      forward_response(3);
      break;

      // send current slave with param    
    case FRAME_SET:
    case BRIGHTNESS_SET: 
    case SPEED_SET:
      forward_command(received, 1);
      forward_response(3);
      break;

    case BUFFER_GET_AT:
      forward_command(received, 1);
      forward_response(27);
      break;
    }
  }
}

///////////////////////////////////////////////////////////////////////////
byte read_serial() {
  return Serial.read();
}


void forward_response(int num) {
  delay(50);
  Wire.requestFrom(current_slave_nr, num);    // request 6 bytes from slave device #2
  byte c = Wire.receive(); // receive a byte as character
  Serial.print(c);
  c = Wire.receive(); // receive a byte as character
  Serial.print(c);
  c = Wire.receive(); // receive a byte as character
  Serial.print(c);

  /*  while( 1 <= Wire.available())    // slave may send less than requested
   { 
   byte c = Wire.receive(); // receive a byte as character
   Serial.write(c);         // print the character
   }*/
}

byte wait_and_read_serial() {
  while( !Serial.available() );
  return read_serial();
}

boolean ok(byte command, byte param) {
  Serial.flush();
  Serial.write(OK);
  Serial.write(command);
  Serial.write(param);
}

void forward_command(byte command, byte num_param) {
  Wire.beginTransmission(current_slave_nr);
  Wire.send(COMMAND);  
  Wire.send(command);
  for(; num_param > 0; num_param--) {
    byte param = wait_and_read_serial();
    Wire.send(param);
  }
  Wire.endTransmission();
}





