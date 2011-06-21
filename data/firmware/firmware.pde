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
uint8_t running;
long frame_delay;

///////////////////////////////////////////////////
// Main
//
void setup() {
  Rainbowduino.begin();
  uint8_t last_adr = EEPROM.read(I2C_EEPROM_ADR) & 0x0F; //DEBUG
  Rainbowduino.set_frame_line(0,1,last_adr,last_adr,last_adr);  //DEBUG

  Con.begin();
  Con.onMessageAvailable(execute);
  
  reset();
  load_from_eeprom();
  start();
}

void loop() {
  Con.loop();

  // Rainbowduino.set_frame_pixel(0,0,0,1,Con.master,Con.master);
  // Rainbowduino.set_frame_line(100,1,10,0,0); //Con.last_slave_address, Con.slave_address_to_register);    

  // Rainbowduino.set_frame_line(0,5,Con.inputMessage->type(),Con.inputMessage->command(), Con.inputMessage->param());
  // Rainbowduino.set_frame_line(0,6,Con.outputMessage->type(),Con.outputMessage->command(), Con.outputMessage->param());

  //  Rainbowduino.set_frame_line(0,5,0,Con.inputMessage->writeIndex, Con.inputMessage->param());
  //  Rainbowduino.set_frame_line(0,6,0,0, Con.outputMessage->param());
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
  uint8_t command = Con.outputMessage->command();
  uint8_t return_value = 0;
  uint8_t param = 0;
  switch(command) {
      /* Basic Control */
    case PING:
    case API_VERSION:
      return_value = API_VERSION_NR;
      break;

    case RESET:
      Con.begin(false);
      reset();
      break;
    case STOP:
      running = false;
      break;
    case START:
      running = true;
      break;            
    case FRAME_SET:
      Rainbowduino.set_current_frame_nr(Con.outputMessage->paramRead());
      break;
    case FRAME_GET:
      return_value = Rainbowduino.get_current_frame_nr();
      break;

      /* Brightness Control */
    case BRIGHTNESS_SET:
      Rainbowduino.level = Con.outputMessage->paramRead();
      break;
    case BRIGHTNESS_GET:
      return_value = Rainbowduino.level;
      break;

      /* Buffer Control */
    case BUFFER_SET_AT:
      param = Con.outputMessage->paramRead(); //read adress value
      for(uint8_t row = 0; row < NUM_ROWS; row++) {
        Rainbowduino.set_frame_row(param, row, Con.outputMessage->paramRead());
      }
      break;
    case BUFFER_GET_AT:
      param = Con.outputMessage->paramRead(); //read adress value
      return_value = NUM_ROWS;
      for(uint8_t row = 0; row < NUM_ROWS; row++) {
        //TODO Con.write(Rainbowduino.get_frame_row(param, row));
      }
      break;
    case BUFFER_SAVE:    
      save_to_eeprom(); 
      break;
    case BUFFER_LOAD:
      load_from_eeprom();
      return_value = Rainbowduino.get_num_frames();
      break;
    case BUFFER_LENGTH:      
      return_value = Rainbowduino.get_num_frames();
      break;            

      /* Speed Control */
    case SPEED_SET:
      set_frame_delay(Con.outputMessage->paramRead());
      break;
    case SPEED_GET:
      return_value = frame_delay;
      break;
    case SPEED_INC: 
      if(frame_delay > SPEED_FACTOR) frame_delay -= SPEED_FACTOR;
      set_frame_delay(frame_delay);
      break;
    case SPEED_DEC:  
      frame_delay += SPEED_FACTOR;
      set_frame_delay(frame_delay);
      break;    
    default:
      //send error
    break;
    }
    if(Con.outputMessage->isRequest()) Con.sendResponse(command, return_value);
}


///////////////////////////////////////////////////////////////////////////
// EEPROM Stuff
//
void save_to_eeprom() {
  word addr = EEPROM_START;
  word num_frames = Rainbowduino.get_num_frames();
  EEPROM.write(addr++, num_frames);
  for(word frame_nr = 0; frame_nr < num_frames; frame_nr++) {
    for(uint8_t row = 0; row < NUM_ROWS; row++) {
      EEPROM.write(addr++, Rainbowduino.get_frame_row(frame_nr, row));
    }
  }
}

void load_from_eeprom() {
  word addr = EEPROM_START;
  word num_frames = EEPROM.read(addr++);
  if(num_frames > MAX_NUM_FRAMES) num_frames = MAX_NUM_FRAMES;
  for(word frame_nr = 0; frame_nr < num_frames; frame_nr++) {
    for(uint8_t row = 0; row < NUM_ROWS; row++) {
      Rainbowduino.set_frame_row(frame_nr, row, EEPROM.read(addr++));
    }
  }
}
  