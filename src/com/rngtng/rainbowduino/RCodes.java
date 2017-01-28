package com.rngtng.rainbowduino;

public interface RCodes {

	public final static int API_VERSION_NR = 1;

	/* init code */
	public final static int COMMAND     = 255;

	/* command code */
	public final static int API_VERSION = 253; //returns OK + Version number
	public final static int PING        = 252; //returns OK + Version number

	/* Display Control */
	public final static int RESET       = 254; //returns OK + current frame (which should be 0)
	public final static int START       = 218; //returns OK + current frame
	public final static int STOP        = 216; //returns OK + current frame
	public final static int FRAME_SET   = 214; //param: frame nummer, returns OK + current frame
	public final static int FRAME_GET   = 212; //returns OK + current frame

	/* Display Control */
	public final static int BRIGHTNESS_SET = 208; //param: brightness, returns OK + current frame
	public final static int BRIGHTNESS_GET = 206; //returns OK + brightness color

	/* Buffer Control */
	//XY
	//Y
	public final static int BUFFER_SET_AT = 246; //param: buffer addr, returns OK + buffer size
	//param2: buffer content, returns OK

	public final static int BUFFER_GET_AT = 244; //param: buffer addr, returns OK + buffer size
	//returns 2: buffer content

	public final static int BUFFER_LENGTH = 242; //returns OK + buffer length

	public final static int BUFFER_SAVE   = 236; //returns OK + buffer length

	public final static int BUFFER_LOAD   = 234; //returns OK + buffer length

	/* Speed Control */
	public final static int SPEED_SET   = 228; //param: SPEED value, returns OK + speed value
	public final static int SPEED_GET   = 226; //returns OK + speed value

	public final static int SPEED_INC   = 224; //returns OK + speed value
	public final static int SPEED_DEC   = 222; //returns OK + speed value

	public final static int SLAVE_NR_SET = 208; //returns OK + slave number
	public final static int SLAVE_NR_GET = 206; //returns OK + slave number
	public final static int SLAVE_ACTIV  = 204; //returns OK + slave number

	/* return codes */
	public final static int ERROR       = 255; // followed by error code
	public final static int OK          = 1;   //followed by return params

	public final static int ERROR_TIME_OUT   = 255; // followed by error code
	public final static int ERROR_MISSMATCH   = 254; // followed by error code

}
