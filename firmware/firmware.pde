/*
 * Firmware.h version 2.1 - to run with Rainbowduino/Arduino Processing library
 * Copyright (c) 2010 Tobias Bielohlawek -> http://www.rngtng.com/mtxcontrol
 *
 */

#include <EEPROM.h>

#include <MsTimer2.h>

/* 
 * Rainbowduino.h && Rainbowduino.cpp are symbolically linked to make it compile right from the beginning.
 * It's way more clean if you move "rainbowduino" Folder (including Rainbowduino.h && Rainbowduino.cpp) to you Arduino library 
 * and remove those links
 */
#include "Rainbowduino.h"
#include "Master.h"
#include "Slave.h"
#include "RCodes.h" //API Codes

#define BAUD_RATE 57600

#define SPEED_FACTOR 100 //ms
#define DEFAULT_FRAME_DELAY 10 // * 100ms => 1s

//running mode
byte running;
long frame_delay;

///////////////////////////////////////////////////
// Main
//
void setup() {
  Serial.begin(BAUD_RATE);
  Rainbowduino.initialize();
  
  reset();
  load_from_eeprom(0);
  start();
}

void loop() {
  check_serial();
}

///////////////////////////////////////////////////
// Workflow Stuff
//
void reset() {
  stop();
  Rainbowduino.reset();
  set_frame_delay(DEFAULT_FRAME_DELAY);
}

void start() {
  running = true;
  MsTimer2::start();
}

void stop() {
  running = false;
  MsTimer2::stop();
}

void next_frame_interrupt() {  
   Rainbowduino.next_frame();
}

void set_frame_delay(long frame_delay_value) {
  frame_delay = frame_delay_value * SPEED_FACTOR;
  MsTimer2::set(frame_delay, next_frame_interrupt); // 500ms period

  frame_delay = frame_delay_value;
}

///////////////////////////////////////////////////
// API Stuff
//
void check_serial() {
  if(!Serial.available()) return;
  byte received = read_serial();
  byte param;
  if(received == COMMAND) {
    received = wait_and_read_serial();
    switch(received) {
      /* Basic Control */
    case PING:
    case API_VERSION:
      ok(received, API_VERSION_NR);
      break;

    case RESET:
      load_from_eeprom(0);
      reset();
      ok(received, Rainbowduino.get_current_frame_nr());
      break;
    case STOP:
      running = false;
      ok(received, Rainbowduino.get_current_frame_nr());
      break;
    case START:
      running = true;
      ok(received, Rainbowduino.get_current_frame_nr());
      break;            
    case FRAME_SET:
      Rainbowduino.set_current_frame_nr(wait_and_read_serial());
      ok(received, Rainbowduino.get_current_frame_nr());
      break;
    case FRAME_GET:
      ok(received, Rainbowduino.get_current_frame_nr());
      break;

      /* Brightness Control */
    case BRIGHTNESS_SET:
      Rainbowduino.level = wait_and_read_serial();
      ok(received, Rainbowduino.level);
      break;
    case BRIGHTNESS_GET:
      ok(received, Rainbowduino.level);
      break;

      /* Buffer Control */
    case BUFFER_SET_AT:
      param = wait_and_read_serial(); //read adress value
      ok(received, NUM_ROWS);
      for(byte row = 0; row < NUM_ROWS; row++) {
        Rainbowduino.set_frame_row(param, row, wait_and_read_serial());
      }
      break;
    case BUFFER_GET_AT:
      param = wait_and_read_serial(); //read adress value
      ok(received, NUM_ROWS);
      for(byte row = 0; row < NUM_ROWS; row++) {
        Serial.write(Rainbowduino.get_frame_row(param, row));
      }
      break;
    case BUFFER_LENGTH:      
      ok(received, Rainbowduino.get_num_frames());
      break;
    case BUFFER_SAVE:    
      save_to_eeprom(0);   
      ok(received, Rainbowduino.get_num_frames());
      break;      
    case BUFFER_LOAD:      
      load_from_eeprom(0);
      ok(received, Rainbowduino.get_num_frames());
      break;      

      /* Speed Control */
    case SPEED_SET:
      param = wait_and_read_serial(); //read speed value
      set_frame_delay(param);
      ok(received, frame_delay);
      break;
    case SPEED_GET:
      ok(received, frame_delay);
      break;
    case SPEED_INC: 
      if(frame_delay > SPEED_FACTOR) frame_delay -= SPEED_FACTOR;
      set_frame_delay(frame_delay);
      ok(received, frame_delay);
      break;
    case SPEED_DEC:  
      frame_delay += SPEED_FACTOR;
      set_frame_delay(frame_delay);
      ok(received, frame_delay);
      break;    
    default:
      //send error
    break;
    }
  }
}

///////////////////////////////////////////////////////////////////////////
// Serial Stuff
//
byte read_serial() {
  return Serial.read();
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

///////////////////////////////////////////////////////////////////////////
// EEPROM Stuff
//
void save_to_eeprom(word addr) {
  word num_frames = Rainbowduino.get_num_frames();
  EEPROM.write(addr++, num_frames);
  for( word frame_nr = 0; frame_nr < num_frames; frame_nr++ ) {
    for( byte row = 0; row < NUM_ROWS; row++ ) {
      EEPROM.write(addr++, Rainbowduino.get_frame_row(frame_nr, row));
    }
  }
}


void load_from_eeprom(word addr) {
  word num_frames = EEPROM.read(addr++);
  for( word frame_nr = 0; frame_nr < num_frames; frame_nr++ ) {
    for( byte row = 0; row < NUM_ROWS; row++ ) {
      Rainbowduino.set_frame_row(frame_nr, row, EEPROM.read(addr++));
    }
  }
}

