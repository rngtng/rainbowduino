package com.rngtng.rainbowduino;

public class RainbowduinoMessage implements RCodes {
	
	public int messageType = -1;	
	public int receiver    = -1;  //0 = master, 1 = first slave etc.
	public int command     = -1;	
	public int length      = -1;
	
	int toRead;
	int toReceive;
	
	int[] paramBuffer = new int[64];

	public void consume(int value) {
		if(ready()) return; //false

		if( messageType == -1) {
			if( value == COMMAND || value == OK || value == ERROR ) { 
				messageType = value;				
				toRead = 0;
			}
			return;
		}
		
		if( command == -1 ) {
			command = value;
			return;
		}

		if( receiver == -1 ) {
			command = value;
			return;
		}		
		
		if( length == -1 ) {
			//TODO waht if size > 64????
			length = value;
			toRead = 0;
			return;
		}	   

		paramBuffer[toReceive++] = value;
		return;	
	}
	
	public boolean ready() {
	  return toReceive == length;
	}

	public boolean isError() {
		return messageType == ERROR;	
	}

	public boolean isOK() {
		return messageType == OK;	
	}	
	
	public boolean isCommand() {
		return messageType == COMMAND;	
	}	
	
	public boolean is(int _command) {
		return ready() && command == _command;	
	}
	
	public int param() {
		return paramBuffer[0];	
	}

	public int paramRead() {
		return paramBuffer[toRead++];	
	}
	
}
