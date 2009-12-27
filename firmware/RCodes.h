/*
 * RCodes.h version 2.0 - API definition of  processing Rainbowduino Library communication
 * Copyright (c) 2009 Tobias Bielohlawek -> http://www.rngtng.com/mtxcontrol
 *
 */
#ifndef RCODES_h
#define RCODES_h

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
#define FRAME_SET   214 //returns OK + current frame
#define FRAME_GET   212 //returns OK + current frame

#define BRIGHTNESS_SET 208 //param: brightness, returns OK + current frame
#define BRIGHTNESS_GET 206 //returns OK + brightness color

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

#endif //RCODES.h

