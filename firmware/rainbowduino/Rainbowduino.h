/*
 * Rainbowduino.h version 2.1 - A driver to run Seeedstudio 8x8 RBG LED Matrix
 * Based on Rainbow.h by Seedstudio.com -> http://www.seeedstudio.com/depot/rainbowduino-led-driver-platform-plug-and-shine-p-371.html
 * Copyright (c) 2010 Tobias Bielohlawek -> http://www.rngtng.com
 *
 */

#ifndef RAINBOWDUINO_h
#define RAINBOWDUINO_h

// include types & constants of Wiring core API
#include <WProgram.h>

//=============================================
//MBI5168
#define SH_DIR   DDRC
#define SH_PORT  PORTC

#define SH_BIT_SDI   0x01
#define SH_BIT_CLK   0x02

#define SH_BIT_LE    0x04
#define SH_BIT_OE    0x08
//============================================
#define clk_rising  {SH_PORT &= ~SH_BIT_CLK; SH_PORT |= SH_BIT_CLK; }
#define le_high     {SH_PORT |= SH_BIT_LE; }
#define le_low      {SH_PORT &= ~SH_BIT_LE; }
#define enable_oe   {SH_PORT &= ~SH_BIT_OE; }
#define disable_oe  {SH_PORT |= SH_BIT_OE; }

#define shift_data_1 {SH_PORT |= SH_BIT_SDI;}
#define shift_data_0 {SH_PORT &= ~SH_BIT_SDI;}

#define NUM_LINES 8
#define NUM_ROWS 24 // 3 BYTES per ROW  x 8 Lines = 24Bytes
#define MAX_NUM_FRAMES 21 // = 24Bytes * 21 = 504Bytes 

#define NUM_LEVEL 16 
#define DEFAULT_LEVEL 1 //max 16

#define FRAME_HZ 75 //number of repeatings per second

class RainbowduinoDriver {

public:
  byte frame_buffer[NUM_ROWS*MAX_NUM_FRAMES]; // [FRAME_BUFFER_SIZE]; //size of EEPROM -> to read faster??
  byte level;
    
  void initialize();
  void reset();

  byte get_num_frames();

  void set_current_frame_nr(byte frame_nr);
  byte get_current_frame_nr();

  void next_frame();

  /////////////////////////
  void set_frame(byte frame_nr, byte* data);

  void set_frame_row(byte frame_nr, byte row, byte data);
  byte get_frame_row(byte frame_nr, byte row);

  //to set all 3 colors of each line
  void set_frame_line(byte frame_nr, byte x, byte red, byte green, byte blue);

  //to set all 3 colors of each pixel
  void set_frame_pixel(byte frame_nr, byte x, byte y, byte red, byte green, byte blue);

  /////////////////////////
  void draw();

private:
  byte num_frames;
  
  volatile byte current_frame_nr;
  volatile word off;  //buffer offset  
  volatile byte current_row;
  volatile byte current_level;

  void draw_row(byte row, byte level, byte r, byte b, byte g);
  void draw_color(byte c);
  void enable_row(byte row);
};

extern RainbowduinoDriver Rainbowduino;

#endif //RAINBOWDUINO.h

