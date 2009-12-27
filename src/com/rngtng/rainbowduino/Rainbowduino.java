/*
A nice wrapper class to control the Rainbowduino 

(c) copyright 2009 by rngtng - Tobias Bielohlawek

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General
Public License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330,
Boston, MA  02111-1307  USA
 */

package com.rngtng.rainbowduino;

import processing.core.PApplet;
import processing.serial.Serial;

/**
 * 
 *
 * @author rngtng - Tobias Bielohlawek
 *
 */
public class Rainbowduino  implements RCodes{

	PApplet app;

	int baud = 9600;

	Serial port;
	String port_name;	

	public static int width = 8;
	public static int height = width;

	public final String VERSION = "0.1";

	/**
	 * Create a new instance to communicate with the rainbowduino. Make sure to (auto)init the serial port, too 
	 * 
	 * @param _app parent Applet
	 */
	public Rainbowduino(PApplet _app) {
		this.app = _app;
		app.registerDispose(this);
	}

	public void dispose() {
		port.stop();
	}

	/**
	 * return the version of the library.
	 *
	 * @return String version number
	 */
	public String version() {
		return VERSION;
	}

	/**
	 * auto init serial port by default values
	 */
	public void initPort() {
		this.initPort(null, 0, true);
	}

	/**
	 * Auto init serial port with given baud rate
	 * @param _baud
	 */
	public void initPort(int _baud) {
		this.initPort(null, _baud, true);
	}	
	
	/**
	 * Open serial port with given name and baud rate.
	 * No sensity checks
	 * 
	 */
	public void initPort(String port_name, int _baud, boolean check) {
		if(_baud > 0) this.baud = _baud;
		openPort(port_name, check);
		String[] ports = Serial.list();
		for(int i = 0; port == null && i < ports.length; i++) {
			if(PApplet.match(ports[i], "tty") == null) continue;
			//PApplet.println(ports[i]);
			openPort(ports[i], check);
		}			
	}

	/* *********************** */
	
	/**
	 * Open serial port with given name. Send ping to check if port is working.
	 * If not port is closed and set back to null
	 * 
	 * @param port_name port to open
	 * @param check whether to perform valid checks
	 * @return whether port could be opened sucessfully 
	 */
	private boolean openPort(String port_name, boolean check) {
		if(port_name == null) return false;
		port = new Serial(app, port_name, this.baud);
		port.buffer(0);				
		if(!check) return true; //skip check
		try { //for some reason first byte is 252 when init Rainbowduino, so we wait for this first
			waitAndReadSerial();
		} catch (RainbowduinoTimeOut e) {
			e.printStackTrace();
		}
		PApplet.println("pinging");
		if(ping()) return true;					
		PApplet.println("No response");        			
		if(port != null) port.stop();
		port = null;
		return false;
	}

	/* +++++++++++++++++++ */	
	public int apiVersion() {	   
		try {
			sendCommand(API_VERSION);
			return receive();
		} catch (RainbowduinoError e) {
			e.printStackTrace();
		}		
		return 0;
	}

	public boolean ping() {		
		try {
			sendCommand(PING);
			receive();
			return true;
		} catch (RainbowduinoError e) {
			e.printStackTrace();
		}		
		return false;
	}

	/* +++++++++++++++++++ */	

	public void reset() {
		sendCommand(RESET);
	}

	public void start() {	   
		sendCommand(START);
	}

	public void stop() {
		sendCommand(STOP);
	}

	public void frameSet(int frame_pos) {
		sendCommand(FRAME_SET);
		sendParam(frame_pos);
	}

	public int frameGet() {	   
		try {
			sendCommand(FRAME_GET);
			return receive();
		} catch (RainbowduinoError e) {
			e.printStackTrace();
		}		
		return 0;
	}

	/* +++++++++++++++++++ */
	
	public void brightnessSet(int brightness) {
		sendCommand(BRIGHTNESS_SET);
		sendParam(brightness);
	}

	public int brightnessGet() {	   
		try {
			sendCommand(BRIGHTNESS_GET);
			return receive();
		} catch (RainbowduinoError e) {
			e.printStackTrace();
		}		
		return 0;
	}	
	
	/* +++++++++++++++++++ */
	public int bufferSetAt(int adr, int[] content) {
		return bufferSetAt(adr, content, false);
	}
	
	public int bufferSetAt(int adr, int[] content, boolean check) {	   
		try {
			sendCommand(BUFFER_SET_AT);
			sendParam(adr);
			if(check) {
			  int accepted_size = receive(); 
			  if(content.length != accepted_size) return 0; //MISSMATCH!!!!
			}
			for(int i = 0; i < content.length; i++) {
				send(content[i]);
			}						
		} catch (Exception e) {			
			e.printStackTrace();
		}
		return 0;
	}

	public int[] bufferGetAt(int adr) {	 		
		try {
			sendCommand(BUFFER_GET_AT);
			sendParam(adr);
			int size = receive();
			int[] content = new int[size];
			for(int i = 0; i < size; i++) {			
				try {
					content[i] = waitAndReadSerial();
				} catch (RainbowduinoTimeOut e) {
					throw new RainbowduinoError( ERROR_TIME_OUT );
				}
			}
			return content;
		} catch (RainbowduinoError e) {
			e.printStackTrace();
		}	
		return null;
	}	

	public int bufferLength() {
		try {
			sendCommand(BUFFER_LENGTH);
			return receive();
		} catch (RainbowduinoError e) {
			e.printStackTrace();
		}
		return 0;		
	}

	public void bufferSave() {
		sendCommand(BUFFER_SAVE);
	}	

	public void bufferLoad() {
		sendCommand(BUFFER_LOAD);
	}		

	/* +++++++++++++++++++ */
	public void speedSet(int speed_value) {
		sendCommand(SPEED_SET);
		sendParam(speed_value);
	}

	public int speedGet() {	   
		try {
			sendCommand(SPEED_GET);
			return receive();
		} catch (RainbowduinoError e) {
			e.printStackTrace();
		}
		return 0;
	}	

	public void speedUp() {
		sendCommand(SPEED_INC);
	}

	public void speedDown() {
		sendCommand(SPEED_DEC);
	}

	/* +++++++++++++++++++ */

	public void sendCommand(int command_code) {
		//PApplet.println("Command: " + command_code);		
		//init command			
		send(COMMAND);
		//flush buffer		
		port.clear();		
		//send command
		send(command_code);
	}

	public void sendParam(int param) {
		//send param			
		send(param);						
	}

	public int receive() throws RainbowduinoError {
		//PApplet.println("Wait return");
		//wait for response code
		try {	
			int response = waitAndReadSerial();
			if(response == OK) {
				//return ok code
				//PApplet.println("Get return param");
				return waitAndReadSerial();
			}
			else {
				PApplet.println("ERROR: Returns: " + response);
				//return error code
				throw new RainbowduinoError( waitAndReadSerial() );
			}
		}
		catch( RainbowduinoTimeOut e) {  
			throw new RainbowduinoError(ERROR_TIME_OUT);
		}		
	}

	/* +++++++++++++++++++ */
	private void send(int value) {
		if(port == null ) return;
		port.write(value);
	}

	private int waitAndReadSerial() throws RainbowduinoTimeOut {
		return waitAndReadSerial(50);
	}

	private int waitAndReadSerial(int timeout) throws RainbowduinoTimeOut {
		//what if port is NULL??
		while( timeout > 0 && port.available() < 1) {
			//print(".");
			sleep(100);
			timeout--;
			if(timeout == 0) throw new RainbowduinoTimeOut();
		}
		return port.read();
	}

	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		}
		catch(InterruptedException e) {
		}
	}

	class RainbowduinoTimeOut extends Exception {}
	class RainbowduinoError extends Exception {
		public RainbowduinoError(int error) {
			PApplet.println(error);
		}
	}

}
