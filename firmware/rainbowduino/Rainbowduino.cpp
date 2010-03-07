/*
 * Rainbowduino.h version 2.1 - A driver to run Seeedstudio 8x8 RBG LED Matrix
 * Copyright (c) 2010 Tobias Bielohlawek -> http://www.rngtng.com
 *
 */

// include this library's description file
#include "Rainbowduino.h"

#include "TimerOne.h"

// include core Wiring API
#include "WProgram.h"

// include description files for other libraries used (if any)
#include "pins_arduino.h"
#include "WConstants.h"

RainbowduinoDriver Rainbowduino;


// wrapper fnc to call rawing method, maybe use member function pointer instead?
void draw_callback() {
    Rainbowduino.draw();
}

///////////////////////////////////////////////////////////////////////
// Initializer to init Timer, IO and starting values 
// for some reasons, this cant be writien as constructor.
void RainbowduinoDriver::begin() {
  
  //setup up timer  
  long period = 1000000 / FRAME_HZ / NUM_LINES / NUM_LEVEL;
  Timer1.initialize(period);
  Timer1.attachInterrupt(draw_callback);  // attaches callback() as a timer overflow callback      
  
  //setups IO 
  DDRD = 0xff;
  DDRC = 0xff;
  DDRB = 0xff;
  PORTB = 0;
  PORTD = 0;
  
  reset();  
}

//=== Frame Show Manipulating ==========================================================
void RainbowduinoDriver::reset() {
  level = DEFAULT_LEVEL;
  current_frame_nr = 0;
  current_row = 0;
  current_level = 0;
  num_frames = 0;
}

byte RainbowduinoDriver::get_num_frames() {
  return num_frames;
}

void RainbowduinoDriver::set_current_frame_nr(byte frame_nr) {
  if(frame_nr < num_frames) current_frame_nr = frame_nr;
}

byte RainbowduinoDriver::get_current_frame_nr() {
  return current_frame_nr;
} 

void RainbowduinoDriver::next_frame() {
  current_frame_nr++;
  if(current_frame_nr >= num_frames) current_frame_nr = 0;
}

//=== Frame Data Manipulating ==========================================================
void RainbowduinoDriver::set_frame(byte frame_nr, byte* data) {
  if(frame_nr >= MAX_NUM_FRAMES) return;
  word offset = frame_nr * NUM_ROWS;
  for(byte row = 0; row < NUM_ROWS; row++) {
    frame_buffer[offset+row] = data[row];
  }
  if(frame_nr >= num_frames) num_frames = frame_nr + 1;
}

void RainbowduinoDriver::set_frame_row(byte frame_nr, byte row, byte data) {
  if(frame_nr >= MAX_NUM_FRAMES) return;
  word offset = frame_nr * NUM_ROWS;
  frame_buffer[offset+row] = data;
  if(frame_nr >= num_frames) num_frames = frame_nr + 1;
}

byte RainbowduinoDriver::get_frame_row(byte frame_nr, byte row) {
  if(frame_nr >= num_frames) return 0;
  word offset = frame_nr * NUM_ROWS;
  return frame_buffer[offset+row];
}

void RainbowduinoDriver::set_frame_line(byte frame_nr, byte x, byte red, byte green, byte blue) {
  if(frame_nr >= MAX_NUM_FRAMES) return;
  word offset = frame_nr * NUM_ROWS + x * 3;
  frame_buffer[offset  ] = red;
  frame_buffer[offset+1] = green;
  frame_buffer[offset+2] = blue;
  if(frame_nr >= num_frames) num_frames = frame_nr + 1;
}

void RainbowduinoDriver::set_frame_pixel(byte frame_nr, byte x, byte y, byte red, byte green, byte blue) {
  if(frame_nr >= MAX_NUM_FRAMES) return;
  word offset = frame_nr * NUM_ROWS + x * 3;
  frame_buffer[offset  ] = (red   > 0) ? frame_buffer[offset  ] | (1<<y) : frame_buffer[offset  ] & ~(1<<y);
  frame_buffer[offset+1] = (green > 0) ? frame_buffer[offset+1] | (1<<y) : frame_buffer[offset+1] & ~(1<<y);
  frame_buffer[offset+2] = (blue  > 0) ? frame_buffer[offset+2] | (1<<y) : frame_buffer[offset+2] & ~(1<<y);
  if(frame_nr >= num_frames) num_frames = frame_nr + 1;
}

//=== Drawing ===========================================================
void RainbowduinoDriver::draw() {
  if(num_frames == 0) return; //no frames available
  off = current_frame_nr * NUM_ROWS + current_row;
  draw_row(current_row / 3, level, frame_buffer[off++], frame_buffer[off++], frame_buffer[off++]);
  if(current_row >= NUM_ROWS - 1) {
    current_row =  0;
    current_level = (current_level >= NUM_LEVEL - 1) ? 0 : current_level+1;    
  }
  else {
    current_row = current_row + 3;
  }
}

//--- colors to shift: blue, red,  green 
void RainbowduinoDriver::draw_row(byte row, byte level, byte r, byte g, byte b) {
  disable_oe;
  enable_row(row);
  le_high;
  draw_color( (current_level < level) ? b : 0 );
  draw_color( (current_level < level) ? r : 0 );
  draw_color( (current_level < level) ? g : 0 );
  le_low;
  enable_oe;
}

void RainbowduinoDriver::draw_color(byte c) {
  for(byte color = 0; color < 8; color++) {
    if((c & 1) == 1) {
      shift_data_1;
    }
    else {
      shift_data_0;
    }
    c = c >> 1;
    clk_rising;
  }
}

//==============================================================
void RainbowduinoDriver::enable_row(byte row) {   // open the scaning row
  //better? shift word value, take upper and lower part!?  
  if(row < 3) {
    PORTB  = (PINB & ~0x07) | 0x04 >> row;
    PORTD  = (PIND & ~0xF8);
  }
  else {
    PORTB  = (PINB & ~0x07);
    PORTD  = (PIND & ~0xF8) | 0x80 >> (row - 3);
  }
}
