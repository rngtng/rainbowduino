/*
 * Firmware.h version 2.0 - to run with Rainbowduino/Arduino Processing library
 * Copyright (c) 2009 Tobias Bielohlawek -> http://www.rngtng.com/mtxcontrol
 *
 */

#include <EEPROM.h>

/* 
 * Rainbowduino.h && Rainbowduino.cpp are symbolically linked to make it compile right from the beginning.
 * It's way more clean if you move "rainbowduino" Folder (including Rainbowduino.h && Rainbowduino.cpp) to you Arduino library 
 * and remove those links
 */
#include "Rainbowduino.h"
#include "RCodes.h" //API Codes

#define BAUD_RATE 9600

#define DEFAULT_SPEED 150
#define DEFAULT_BRIGHTNESS 2
#define SPEED_FACTOR 100

Rainbowduino rainbow = Rainbowduino();  //max 10 Frames

//running mode
byte running;
word current_delay;
word current_speed;
byte brightness;

void setup_timer()               
{
  TCCR2A |= (1 << WGM21) | (1 << WGM20);   
  TCCR2B |= (1<<CS22); // by clk/64
  TCCR2B &= ~((1<<CS21) | (1<<CS20));   // by clk/64
  TCCR2B &= ~((1<<WGM21) | (1<<WGM20));   // Use normal mode
  ASSR |= (0<<AS2);       // Use internal clock - external clock not used in Arduino
  TIMSK2 |= (1<<TOIE2) | (0<<OCIE2B);   //Timer2 Overflow Interrupt Enable
  TCNT2 = 0xE7; //gamma value
  sei();   
}

//Timer2 overflow interrupt vector handler
ISR(TIMER2_OVF_vect) {
  TCNT2 = 0xE7; //gamma value
  rainbow.draw(brightness);
}

void setup() {
  Serial.begin(BAUD_RATE);  
  reset();
  load_from_eeprom(0);
  running = true;
  setup_timer();
}

void reset() {
  rainbow.reset();
  current_delay = 0;
  current_speed = DEFAULT_SPEED * SPEED_FACTOR;
  brightness = DEFAULT_BRIGHTNESS;
  running = false;
}

void loop() {
  check_serial();
  // next frame
  if(running) {
    if(current_delay < 1) {
      current_delay = current_speed;
      rainbow.next_frame();
    }
    current_delay--;
  }
}

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
      ok(received, rainbow.get_current_frame_nr());
      break;
    case STOP:
      running = false;
      ok(received, rainbow.get_current_frame_nr());
      break;
    case START:
      running = true;
      ok(received, rainbow.get_current_frame_nr());
      break;            
    case FRAME_SET:
      rainbow.set_current_frame_nr(wait_and_read_serial());
      ok(received, rainbow.get_current_frame_nr());
      break;
    case FRAME_GET:
      ok(received, rainbow.get_current_frame_nr());
      break;

      /* Brightness Control */
    case BRIGHTNESS_SET:
      brightness = wait_and_read_serial();
      ok(received, brightness);
      break;
    case BRIGHTNESS_GET:
      ok(received, brightness);
      break;

      /* Buffer Control */
    case BUFFER_SET_AT:
      param = wait_and_read_serial(); //read adress value
      ok(received, NUM_ROWS);
      for(byte row = 0; row < NUM_ROWS; row++) {
        rainbow.set_frame_row(param, row, wait_and_read_serial());
      }
      break;
    case BUFFER_GET_AT:
      param = wait_and_read_serial(); //read adress value
      ok(received, NUM_ROWS);
      for(byte row = 0; row < NUM_ROWS; row++) {
        Serial.write(rainbow.get_frame_row(param, row));
      }
      break;
    case BUFFER_LENGTH:      
      ok(received, rainbow.get_num_frames());
      break;
    case BUFFER_SAVE:    
      save_to_eeprom(0);   
      ok(received, rainbow.get_num_frames());
      break;      
    case BUFFER_LOAD:      
      load_from_eeprom(0);
      ok(received, rainbow.get_num_frames());
      break;      

      /* Speed Control */
    case SPEED_SET:
      param = wait_and_read_serial(); //read speed value
      current_speed = param * SPEED_FACTOR;
      ok(received, param);
      break;
    case SPEED_GET:
      ok(received, current_speed / SPEED_FACTOR);
      break;
    case SPEED_INC:  
      if(current_speed > SPEED_FACTOR) current_speed -= SPEED_FACTOR;
      ok(received, current_speed / SPEED_FACTOR);
      break;
    case SPEED_DEC:  
      current_speed += SPEED_FACTOR;
      ok(received, current_speed / SPEED_FACTOR);
      break;
    }

  }
}

///////////////////////////////////////////////////////////////////////////
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
void save_to_eeprom(word addr) {
  word num_frames = rainbow.get_num_frames();
  EEPROM.write(addr++, num_frames);
  for( word frame_nr = 0; frame_nr < num_frames; frame_nr++ ) {
    for( byte row = 0; row < NUM_ROWS; row++ ) {
      EEPROM.write(addr++, rainbow.get_frame_row(frame_nr, row));
    }
  }
}


void load_from_eeprom(word addr) {
  word num_frames = EEPROM.read(addr++);
  for( word frame_nr = 0; frame_nr < num_frames; frame_nr++ ) {
    for( byte row = 0; row < NUM_ROWS; row++ ) {
      rainbow.set_frame_row(frame_nr, row, EEPROM.read(addr++));
    }
  }
}

