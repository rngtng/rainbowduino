/*
 * RCodes.h version 2.1 - API definition of  processing Rainbowduino Library communication
 * Copyright (c) 2010 Tobias Bielohlawek -> http://www.rngtng.com/mtxcontrol
 *
 */
#ifndef RCODES_h
#define RCODES_h

////////////// API COMMANDS ///////////////////////
#define API_VERSION_NR 2

/* command code */
#define API_VERSION 253 //returns RESPONSE + Version number
#define PING        252 //returns RESPONSE + Version number

/* Display Control */
#define RESET       254
#define START       218
#define STOP        216
#define FRAME_SET   214
#define FRAME_GET   212 //returns RESPONSE + current frame

#define BRIGHTNESS_SET 208 //param: brightness
#define BRIGHTNESS_GET 206 //returns RESPONSE + brightness color

/* Buffer Control */
//XY
//Y
#define BUFFER_SET_AT 246 //param1: buffer addr
//param2: buffer content

#define BUFFER_GET_AT 244 //param1: buffer addr, returns RESPONSE + buffer size
//returns 2: buffer content

#define BUFFER_NUM 242 //returns RESPONSE + buffer length

#define BUFFER_SAVE 236
#define BUFFER_LOAD 234 //returns RESPONSE + buffer length

/* Speed Control */
#define SPEED_SET   228 //param1: SPEED value
#define SPEED_GET   226 //returns RESPONSE + speed value

#define SPEED_INC   224
#define SPEED_DEC   222

#define ERROR_TIME_OUT 255 // followed by error code

#endif //RCODES.h

