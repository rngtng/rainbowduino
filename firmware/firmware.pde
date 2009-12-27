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

#define DEFAULT_SPEED 5000
#define SPEED_FACTOR 100

Rainbowduino rainbow = Rainbowduino();  //max 10 Frames

//running mode
byte running = false;

word current_delay = 0;
word current_speed = DEFAULT_SPEED;

byte brightness = 8;

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
  current_speed = DEFAULT_SPEED;
  running = false;
}

void loop() {
  check_serial();
  next_frame();
}

void next_frame() {
  if( !running ) return;
  if(current_delay < 1) {
    current_delay = current_speed;
    rainbow.next_frame();
  }
  current_delay--;
}

void check_serial() {
  if( !Serial.available() ) return;
  byte value = read_serial();
  if( value == COMMAND ) {
    switch( wait_and_read_serial() ) {
      /* Basic Control */
    case PING:
    case API_VERSION:
      ok(API_VERSION_NR);
      break;
      
    case RESET:
      load_from_eeprom(0);
      reset();
      ok(rainbow.get_current_frame_nr());
      break;
    case STOP:
      running = false;
      ok(rainbow.get_current_frame_nr());
      break;
    case START:
      running = true;
      ok(rainbow.get_current_frame_nr());
      break;            
    case FRAME_SET:
      rainbow.set_current_frame_nr(wait_and_read_serial());
      ok(rainbow.get_current_frame_nr());
      break;
    case FRAME_GET:
      ok(rainbow.get_current_frame_nr());
      break;
      
      /* Brightness Control */
    case BRIGHTNESS_SET:
      brightness = wait_and_read_serial();
      ok(brightness);
      break;
    case BRIGHTNESS_GET:
      ok(brightness);
      break;

      /* Buffer Control */
    case BUFFER_SET_AT:
      value = wait_and_read_serial(); //read adress value
      ok(NUM_ROWS);
      for(byte row = 0; row < NUM_ROWS; row++) {
        rainbow.set_frame_row(value, row, wait_and_read_serial());
      }
      ok(NUM_ROWS); //Do we need that??     
      break;
    case BUFFER_GET_AT:
      value = wait_and_read_serial(); //read adress value
      ok(NUM_ROWS);
      for(byte row = 0; row < NUM_ROWS; row++) {
        rainbow.get_frame_row(value, row);
      }
      break;
    case BUFFER_LENGTH:      
      ok(rainbow.get_num_frames());
      break;
    case BUFFER_SAVE:    
      save_to_eeprom(0);   
      ok(rainbow.get_num_frames());
      break;      
    case BUFFER_LOAD:      
      load_from_eeprom(0);
      ok(rainbow.get_num_frames());
      break;      
      
      /* Speed Control */
    case SPEED_SET:
      value = wait_and_read_serial(); //read speed value
      current_speed = value * SPEED_FACTOR;
      ok(value);
      break;
    case SPEED_GET:
      ok(current_speed / SPEED_FACTOR);
      break;
    case SPEED_INC:  
      if(current_speed > SPEED_FACTOR) current_speed -= SPEED_FACTOR;
      ok(current_speed / SPEED_FACTOR);
      break;
    case SPEED_DEC:  
      current_speed += SPEED_FACTOR;
      ok(current_speed / SPEED_FACTOR);
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

boolean ok(byte param) {
  Serial.flush();
  Serial.write(OK);
  Serial.write(param);
}


 
///////////////////////////////////////////////////////////////////////////

void send_eeprom( word addr ) {
  word num_frames = EEPROM.read(addr++); 
  num_frames = min(MAX_NUM_FRAMES, num_frames);
  Serial.write(num_frames);

  for( word frame_nr = 0; frame_nr < num_frames; frame_nr++ ) {
    for( byte row = 0; row < NUM_ROWS; row++ ) {
      Serial.write( EEPROM.read(addr++) );
    }
    delay(1);
  }
}

///////////////////////////////////////////////////////////////////////////

void write_to_eeprom( word addr ) {
  word num_frames = wait_and_read_serial();
  EEPROM.write(addr++, num_frames);

  for( word frame_nr = 0; frame_nr < num_frames; frame_nr++ ) {
    for( byte row = 0; row < NUM_ROWS; row++ ) {
      EEPROM.write(addr++, wait_and_read_serial()); //TODO: this will likely fail if addr is bigger than we actually can adress
    }
  }
}

void save_to_eeprom( word addr ) {
  word num_frames = rainbow.get_num_frames();
  EEPROM.write(addr++, num_frames);

  for( word frame_nr = 0; frame_nr < num_frames; frame_nr++ ) {
    for( byte row = 0; row < NUM_ROWS; row++ ) {
      EEPROM.write(addr++, rainbow.get_frame_row(frame_nr, row));
    }
  }
}


void load_from_eeprom( word addr ) {
  word num_frames = EEPROM.read(addr++);
  for( word frame_nr = 0; frame_nr < num_frames; frame_nr++ ) {
    for( byte row = 0; row < NUM_ROWS; row++ ) {
      rainbow.set_frame_row(frame_nr, row, EEPROM.read(addr++));
    }
  }
}





