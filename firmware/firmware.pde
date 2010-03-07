/*
 * Firmware.h version 2.1 - to run with Rainbowduino/Arduino Processing library
 * Copyright (c) 2010 Tobias Bielohlawek -> http://www.rngtng.com/mtxcontrol
 *
 */

#include <EEPROM.h>

#include <Wire.h> //needed for connection
#include "Connection.h"

#include <MsTimer2.h>

/* 
 * Rainbowduino.h && Rainbowduino.cpp are symbolically linked to make it compile right from the beginning.
 * It's way more clean if you move "rainbowduino" Folder (including Rainbowduino.h && Rainbowduino.cpp) to you Arduino library 
 * and remove those links
 */
#include "Rainbowduino.h"
#include "RCodes.h" //API Codes

#define SPEED_FACTOR 100 //ms
#define DEFAULT_FRAME_DELAY 10 // * 100ms => 1s

#define EEPROM_START 10 //0 is reserved, user sth > 0

//running mode
byte running;
long frame_delay;

///////////////////////////////////////////////////
// Main
//
void setup() {
  Rainbowduino.begin();
  Con.beginMaster();
  Con.onMessageAvailable(execute);
  
  reset();
  load_from_eeprom();
  start();
}

void loop() {
  while ( Serial.available() ) Con.process( Serial.read() );
  
  //execute3(7, 255, 200);
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

void next_frame_callback() {
   Rainbowduino.next_frame();
}

void set_frame_delay(long frame_delay_value) {
  frame_delay = frame_delay_value * SPEED_FACTOR;
  MsTimer2::set(frame_delay, next_frame_callback); // 500ms period

  frame_delay = frame_delay_value;
}


///////////////////////////////////////////////////
// API Stuff
//
void execute() {
  byte command = Con.read();
  byte return_value = 0;
  byte param = 0;
  switch(command) {
      /* Basic Control */
    case PING:
    case API_VERSION:
      return_value = API_VERSION_NR;
      break;

    case RESET:
      load_from_eeprom();
      reset();
      return_value = Rainbowduino.get_current_frame_nr();
      break;
    case STOP:
      running = false;
      return_value = Rainbowduino.get_current_frame_nr();
      break;
    case START:
      running = true;
      return_value = Rainbowduino.get_current_frame_nr();
      break;            
    case FRAME_SET:
      Rainbowduino.set_current_frame_nr(Con.read());
    case FRAME_GET:
      return_value = Rainbowduino.get_current_frame_nr();
      break;

      /* Brightness Control */
    case BRIGHTNESS_SET:
      Rainbowduino.level = Con.read();
    case BRIGHTNESS_GET:
      return_value = Rainbowduino.level;
      break;

      /* Buffer Control */
    case BUFFER_SET_AT:
      param = Con.read(); //read adress value
      return_value = NUM_ROWS;
      for(byte row = 0; row < NUM_ROWS; row++) {
        Rainbowduino.set_frame_row(param, row, Con.read());
      }
      break;
    case BUFFER_GET_AT:
      param = Con.read(); //read adress value
      return_value = NUM_ROWS;
      for(byte row = 0; row < NUM_ROWS; row++) {
        Con.write(Rainbowduino.get_frame_row(param, row));
      }
      break;
    case BUFFER_SAVE:    
      save_to_eeprom(); 
      return_value = Rainbowduino.get_num_frames();
      break;      
    case BUFFER_LOAD:      
      load_from_eeprom();
    case BUFFER_LENGTH:      
      return_value = Rainbowduino.get_num_frames();
      break;            

      /* Speed Control */
    case SPEED_SET:
      set_frame_delay(Con.read());
    case SPEED_GET:
      return_value = frame_delay;
      break;
    case SPEED_INC: 
      if(frame_delay > SPEED_FACTOR) frame_delay -= SPEED_FACTOR;
      set_frame_delay(frame_delay);
      return_value = frame_delay;
      break;
    case SPEED_DEC:  
      frame_delay += SPEED_FACTOR;
      set_frame_delay(frame_delay);
      return_value = frame_delay;
      break;    
    default:
      //send error
    break;
    }
    //Con.ok(command, return_value);
}


///////////////////////////////////////////////////////////////////////////
// EEPROM Stuff
//
void save_to_eeprom() {
  word addr = EEPROM_START;
  word num_frames = Rainbowduino.get_num_frames();
  EEPROM.write(addr++, num_frames);
  for( word frame_nr = 0; frame_nr < num_frames; frame_nr++ ) {
    for( byte row = 0; row < NUM_ROWS; row++ ) {
      EEPROM.write(addr++, Rainbowduino.get_frame_row(frame_nr, row));
    }
  }
}

void load_from_eeprom() {
  word addr = EEPROM_START;
  word num_frames = EEPROM.read(addr++);
  for( word frame_nr = 0; frame_nr < num_frames; frame_nr++ ) {
    for( byte row = 0; row < NUM_ROWS; row++ ) {
      Rainbowduino.set_frame_row(frame_nr, row, EEPROM.read(addr++));
    }
  }
}





void execute2() {
  byte x = Con.read();
  byte y = Con.read();
  byte c = Con.read();
  Rainbowduino.set_frame_pixel(0,x, y, c, 0,0);
}

bool on3 = false;
void execute3(int x, int c, int delays) {
  if(on3) {
    Rainbowduino.set_frame_line(0,x,c,0,0);
  }
  else {
    Rainbowduino.set_frame_line(0,x,0,c,0);
  }
  on3 = !on3;
  delay(delays);
}