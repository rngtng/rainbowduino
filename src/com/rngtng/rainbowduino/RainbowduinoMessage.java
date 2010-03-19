package com.rngtng.rainbowduino;

import processing.core.PApplet;

public class RainbowduinoMessage implements RCodes {

	//BODY
	int HEADER_LENGTH = 4;
	
	int INDEX_TYPE     = 0
	int INDEX_RECEIVER = 1
	int INDEX_COMMAND  = 2
	int INDEX_LENGTH   = 3
	
	int[] data = new int[64 + HEADER_LENGTH]; //4 for HEADER: type, receiver, command, dataLength

	int writeIndex;
	int readIndex;

	public void consume(int serialByte) {
		if(ready()) return;
		if( writeIndex == 0 && !(serialByte == COMMAND || serialByte == OK || serialByte == ERROR)) return;
		
        data[writeIndex++] = serialByte;
		return;	
	}
	
	public void reset() {
	    writeIndex = 0;
	    readIndex = 0;
	}

	public boolean ready() {
		return writeIndex == (HEADER_LENGTH + data[LENGTH_INDEX]);
	}

	public boolean isError() {
		return type() == ERROR;
	}

	public boolean isOK() {
		return type() == OK;
	}	

	public boolean isCommand() {
		return type() == COMMAND;
	}	

	public boolean is(int _command) {
		return ready() && command() == _command;
	}

    public int command() {
        return data[INDEX_COMMAND];
    }

	public int param() {
		return data[HEADER_LENGTH + 1];
	}

	public int paramRead() {
		return paramBuffer[HEADER_LENGTH + 1 + readIndex++];
	}

}
