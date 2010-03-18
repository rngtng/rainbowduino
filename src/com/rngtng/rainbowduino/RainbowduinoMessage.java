package com.rngtng.rainbowduino;

import processing.core.PApplet;

public class RainbowduinoMessage implements RCodes {

	//HEADER
	public int messageType = -1;	
	public int receiver    = -1;  //0 = master, 1 = first slave etc.
	public int length      = -1;	

	//BODY
	public int command     = -1;		
	int[] paramBuffer = new int[64];

	int toRead;
	int toReceive;


	public void consume(int serialByte) {
		if(ready()) return; //false
		//PApplet.println(serialByte);
		if( messageType == -1) {
			if( serialByte == COMMAND || serialByte == OK || serialByte == ERROR ) { 
				messageType = serialByte;				
				toReceive = 0;
				toRead = 0;
			}
			return;
		}

		if( receiver == -1 ) {
			receiver = serialByte;
			return;
		}		

		if( length == -1 ) {
			//TODO waht if size > 64????
			length = serialByte;						
			return;
		}	   

		if( command == -1 ) {
			command = serialByte;
			toReceive++;
			return;
		}

		paramBuffer[toReceive++] = serialByte;
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
