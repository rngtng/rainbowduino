package com.rngtng.rainbowduino;


public class RainbowduinoMessage implements RCodes {

	//BODY
	int HEADER_LENGTH = 4;
	
	int INDEX_TYPE     = 0;
	int INDEX_RECEIVER = 1;
	int INDEX_COMMAND  = 2;
	int INDEX_LENGTH   = 3;
	
	int[] data = new int[64 + HEADER_LENGTH]; //4 for HEADER: type, receiver, command, dataLength

	int writeIndex;
	int readIndex;

	public void consume(int dataByte) {
		if(ready()) return;
		if( writeIndex == 0 && !(dataByte == COMMAND || dataByte == OK || dataByte == ERROR)) return;		
        data[writeIndex++] = dataByte;
	}
	
	public void reset() {
	    writeIndex = 0;
	    readIndex = 0;
	}

	public boolean ready() {
		return writeIndex == (HEADER_LENGTH + data[INDEX_LENGTH]);
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

	public boolean is(int command) {
		return ready() && data[INDEX_COMMAND] == command;
	}

	/**********************************/
	
    public int type() {
        return data[INDEX_TYPE];
    }	
	   
	public int param() {
		return data[HEADER_LENGTH];
	}

	public int paramRead() {
		return data[HEADER_LENGTH + readIndex++];
	}

}
