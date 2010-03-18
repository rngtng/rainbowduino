/*
 * Rainbowduino.h version 2.1 - A driver to run Seeedstudio 8x8 RBG LED Matrix
 * Based on Rainbow.h by Seedstudio.com -> http://www.seeedstudio.com/depot/rainbowduino-led-driver-platform-plug-and-shine-p-371.html
 * Copyright (c) 2010 Tobias Bielohlawek -> http://www.rngtng.com
 *
 */
 
/* wihlist:
  * cascading serial + reset
  * debug LED
  * row selector one PORT (instead of 2 partly selected)
  * cascading external battery
  * onboard USB
  * easier get matrix
  * VCC on but LED power off => no Power LED burning as not functional
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
#define MAX_NUM_FRAMES 10 // = 24Bytes * 21 = 504Bytes 

#define NUM_LEVEL 16 
#define DEFAULT_LEVEL 1 //max 16

#define FRAME_HZ 75 //number of repeatings per second

class RainbowduinoDriver {

public:
  uint8_t* frameBuffer; // [FRAME_BUFFER_SIZE]; //size of EEPROM -> to read faster??
  uint8_t level;
    
  void begin();
  void reset();

  uint8_t get_num_frames();

  void set_current_frame_nr(uint8_t frame_nr);
  uint8_t get_current_frame_nr();

  void next_frame();

  /////////////////////////
  void set_frame(uint8_t frame_nr, byte* data);

  void set_frame_row(uint8_t frame_nr, uint8_t row, uint8_t data);
  uint8_t get_frame_row(uint8_t frame_nr, uint8_t row);

  //to set all 3 colors of each line
  void set_frame_line(uint8_t frame_nr, uint8_t x, uint8_t red, uint8_t green, uint8_t blue);

  //to set all 3 colors of each pixel
  void set_frame_pixel(uint8_t frame_nr, uint8_t x, uint8_t y, uint8_t red, uint8_t green, uint8_t blue);

  /////////////////////////
  void draw();

private:
  uint8_t num_frames;
  
  volatile uint8_t current_frame_nr;
  volatile word off;  //buffer offset  
  volatile uint8_t current_row;
  volatile uint8_t current_level;

  void draw_row(uint8_t row, uint8_t level, uint8_t r, uint8_t g, uint8_t b);
  void draw_color(uint8_t c);
  void enable_row(uint8_t row);
};

extern RainbowduinoDriver Rainbowduino;

#endif //RAINBOWDUINO.h

