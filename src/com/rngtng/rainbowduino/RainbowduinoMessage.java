package com.rngtng.rainbowduino;

import processing.core.PApplet;


public class RainbowduinoMessage implements RCodes {

	//BODY
	int HEADER_LENGTH = 4;

	/* Header Codes */
	public final static int COMMAND     = 0xFF;  // followed by commands
	public final static int REQUEST     = 0xFE;  // followed by commands	
	public final static int RESPONSE    = 0x0F;  // followed by return params	
	public final static int ERROR       = 0x01;  // followed by error code	

	public final static int INDEX_TYPE     = 0;
	public final static int INDEX_RECEIVER = 1;
	public final static int INDEX_COMMAND  = 2;
	public final static int INDEX_LENGTH   = 3;

	int[] data = new int[64 + HEADER_LENGTH]; //4 for HEADER: type, receiver, command, dataLength

	int writeIndex;
	int readIndex;

	public void consume(int dataByte) {	
		if(ready()) return;
		if( writeIndex == 0 && !(dataByte == COMMAND || dataByte == REQUEST || dataByte == RESPONSE || dataByte == ERROR)) return;		
		data[writeIndex++] = dataByte;
	}

	public void reset() {
		writeIndex = 0;
		readIndex = 0;
	}

	public boolean ready() {
		return (writeIndex >= HEADER_LENGTH) && (writeIndex == (HEADER_LENGTH + data[INDEX_LENGTH]));
	}

	public boolean isCommand() {
		return type() == COMMAND;
	}	

	public boolean isRequest() {
		return type() == REQUEST;
	}	
	
	public boolean isResponse() {
		return type() == RESPONSE;
	}
	
	public boolean isError() {
		return type() == ERROR;
	}
	
	public boolean is(int command) {
		return ready() && data[INDEX_COMMAND] == command;
	}

	public String toString() {
		return "TYPE: " + type() + " Receiver: " + receiver() + " Command: " + data[INDEX_COMMAND];
	}

	/**********************************/

	public int type() {
		return data[INDEX_TYPE];
	}	

	public int receiver() {
		return data[INDEX_RECEIVER];
	}	    

	public int param() {
		return data[HEADER_LENGTH];
	}

	public int paramRead() {
		return data[HEADER_LENGTH + readIndex++];
	}

}
