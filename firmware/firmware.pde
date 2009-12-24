/*
 * Firmware.h version 1.02 - to run mtXcontrol Rainbowduino/Arduino
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

#define LIVE 0
#define STANDALONE 1

#define BAUD_RATE 9600

#define DEFAULT_SPEED 5000
#define SPEED_FACTOR 100

////////////// API COMMANDS ///////////////////////
#define API_VERSION_NR 1

/* init code */
#define COMMAND     255

/* command code */
#define API_VERSION 253 //returns OK + Version number	
#define PING        252 //returns OK + Version number

/* Display Control */
#define RESET       254 //returns OK + current frame (which should be 0)
#define START       218 //returns OK + current frame
#define STOP        216 //returns OK + current frame
#define FRAME_GET   214 //returns OK + current frame
#define FRAME_SET   212 //returns OK + current frame

/* Buffer Control */
//XY
//Y
#define BUFFER_SET_AT 246 //param1: buffer addr, returns OK + buffer size
//param2: buffer content

#define BUFFER_GET_AT 244 //param1: buffer addr, returns OK + buffer size
//returns 2: buffer content

#define BUFFER_LENGTH 242 //returns OK + buffer length

#define BUFFER_SAVE 236 //returns OK + buffer length
#define BUFFER_LOAD 234 //returns OK + buffer length

/* Speed Control */
#define SPEED_SET   228 //param1: SPEED value, returns OK + speed value
#define SPEED_GET   226 //returns OK + speed value

#define SPEED_INC   224 //returns OK + speed value
#define SPEED_DEC   222 //returns OK + speed value

/* return codes */
#define ERROR       255 // followed by error code
#define OK          1   //followed by return params

#define ERROR_TIME_OUT   255 // followed by error code	

Rainbowduino rainbow = Rainbowduino(10);  //max 10 Frames

//running mode
byte mode = STANDALONE;

word current_delay = 0;
word current_speed = DEFAULT_SPEED;

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
  rainbow.draw();
}

void setup() {
  Serial.begin(BAUD_RATE);
  load_from_eeprom(0);
  reset();
  setup_timer();
}

void reset() {
  rainbow.reset();
  current_delay = 0;
  current_speed = DEFAULT_SPEED;
  mode = STANDALONE;
}

void loop() {
  check_serial();
  next_frame();
}

void next_frame() {
  if( mode == LIVE ) return;
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
      ok(rainbow.get_frame_nr());
      break;

      
    case FRAME_SET:
      rainbow.set_frame_nr( wait_and_read_serial() );
      ok(rainbow.get_frame_nr());
      break;
    case FRAME_GET:
      ok(rainbow.get_frame_nr());
      break;


      /* Buffer Control */
    case BUFFER_SET_AT:
      value = wait_and_read_serial(); //read adress value
      ok(rainbow.num_rows);
      for(byte row = 0; row < rainbow.num_rows; row++) {
        rainbow.set_frame_row(value, row, wait_and_read_serial());
      }
      // ok();      
      break;
    case BUFFER_GET_AT:
      value = wait_and_read_serial(); //read adress value
      ok(rainbow.num_rows);
      for(byte row = 0; row < rainbow.num_rows; row++) {
        rainbow.get_frame_row(value, row);
      }
      break;
    case BUFFER_LENGTH:      
      ok(rainbow.num_frames);
      break;
    case BUFFER_SAVE:    
      save_to_eeprom(0);   
      ok(rainbow.num_frames);
      break;      
    case BUFFER_LOAD:      
      load_from_eeprom(0);
      ok(rainbow.num_frames);
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
  num_frames = min(rainbow.max_num_frames, num_frames);
  Serial.write(num_frames);

  for( word frame_nr = 0; frame_nr < num_frames; frame_nr++ ) {
    for( byte row = 0; row < rainbow.num_rows; row++ ) {
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
    for( byte row = 0; row < rainbow.num_rows; row++ ) {
      EEPROM.write(addr++, wait_and_read_serial()); //TODO: this will likely fail if addr is bigger than we actually can adress
    }
  }
}

void save_to_eeprom( word addr ) {
  word num_frames = rainbow.num_frames;
  EEPROM.write(addr++, num_frames);

  for( word frame_nr = 0; frame_nr < num_frames; frame_nr++ ) {
    for( byte row = 0; row < rainbow.num_rows; row++ ) {
      EEPROM.write(addr++, rainbow.get_frame_row(frame_nr, row));
    }
  }
}


void load_from_eeprom( word addr ) {
  rainbow.set_num_frames(EEPROM.read(addr++));
  for( word frame_nr = 0; frame_nr < rainbow.num_frames; frame_nr++ ) {
    for( byte row = 0; row < rainbow.num_rows; row++ ) {
      rainbow.set_frame_row(frame_nr, row, EEPROM.read(addr++));
    }
  }
}





