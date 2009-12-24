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
 * @example Launchpad
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
	 * a Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the library.
	 *
	 * @example Rainbowduino
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

	public void init(String port_name, int _baud) {
		if(_baud > 0) this.baud = _baud;
		openPort(port_name);
		String[] ports = Serial.list();
		for(int i = 0; port == null && i < ports.length; i++) {
			if( PApplet.match(ports[i], "tty") == null) continue;			
			openPort(ports[i]);
		}
	}

	private boolean openPort(String port_name) {
		if(port_name == null) return false;
		port = new Serial(app, port_name, this.baud);
		port.buffer(0);		
		try { //for some reason first byte is 252 when init Rainbowduino, so we wait for this first
			wait_and_read_serial();
		} catch (RainbowduinoTimeOut e) {
			e.printStackTrace();
		}
		if(ping()) return true;					
		PApplet.println("No response");        			
		if(port != null) port.stop();
		port = null;
		return false;
	}

	/* +++++++++++++++++++ */	
	public int api_version() {	   
		try {
			return command(API_VERSION);
		} catch (RainbowduinoError e) {
			e.printStackTrace();
		}		
		return 0;
	}

	public boolean ping() {	   
		try {
			command(PING);
			return true;
		} catch (RainbowduinoError e) {
			e.printStackTrace();
		}		
		return false;
	}

	/* +++++++++++++++++++ */	
	
	public boolean reset() {	   
		try {
			command(RESET);
			return true;
		} catch (RainbowduinoError e) {
			e.printStackTrace();
		}		
		return false;
	}

	public boolean start() {	   
		try {
			command(START);
			return true;
		} catch (RainbowduinoError e) {
			e.printStackTrace();
		}		
		return false;
	}

	public boolean stop() {	   
		try {
			command(STOP);
			return true;
		} catch (RainbowduinoError e) {
			e.printStackTrace();
		}		
		return false;
	}

	
	/* +++++++++++++++++++ */

	public int bufferSetAt(int adr, byte[] content) {	   
		try {
			int accepted_size = command(BUFFER_SET_AT, adr);
			if(content.length != accepted_size) return 0; //MISSMATCH!!!! 
			for(int i = 0; i < content.length; i++) {			
				send(content[i]);
			}
		} catch (RainbowduinoError e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int[] bufferGetAt(int adr) {	 		
		try {
			int size = command(BUFFER_GET_AT, adr);
			int[] content = new int[size];
			for(int i = 0; i < size; i++) {			
				try {
					content[i] = wait_and_read_serial();
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
			return command(BUFFER_LENGTH);
		} catch (RainbowduinoError e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public int bufferSave() {	   
		try {
			return command(BUFFER_SAVE);
		} catch (RainbowduinoError e) {
			e.printStackTrace();
		}
		return 0;
	}	
	
	public int bufferLoad() {	   
		try {
			return command(BUFFER_LOAD);
		} catch (RainbowduinoError e) {
			e.printStackTrace();
		}
		return 0;
	}		
	/* +++++++++++++++++++ */

	public int speedSet(int speed_value) {	   
		try {
			return command(SPEED_SET, speed_value);
		} catch (RainbowduinoError e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int speedGet() {	   
		try {
			return command(SPEED_GET);
		} catch (RainbowduinoError e) {
			e.printStackTrace();
		}
		return 0;
	}	

	public int speedUp() {	   
		try {
			return command(SPEED_INC);
		} catch (RainbowduinoError e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int speedDown() {	   		
		try {
			return command(SPEED_DEC);
		} catch (RainbowduinoError e) {
			e.printStackTrace();
		}
		return 0;
	}

	/* +++++++++++++++++++ */

	private int command(int command_code) throws RainbowduinoError {
		return command(command_code, 0);
	}

	private int command(int command_code, int param) throws RainbowduinoError { //RainbowduinoTimeOut
		//flush buffer
		port.clear();		
		try {
			//init command
			send(COMMAND);
			//send command
			send(command_code);
			//send param			
			if(param > 0 ) send(param);				
			//wait for response code
			int response = wait_and_read_serial();
			if(response == OK) {
				//return ok code
				return wait_and_read_serial();
			}
			else {
				PApplet.println(response);
				//return error code
				throw new RainbowduinoError( wait_and_read_serial() );
			}
		}
		catch( RainbowduinoTimeOut e) {  
			throw new RainbowduinoError( ERROR_TIME_OUT );
		}
	}

	private void send(int value) {
		if(port == null ) return;
		port.write(value);
	}

	/* +++++++++++++++++++ */

	private int wait_and_read_serial() throws RainbowduinoTimeOut {
		return wait_and_read_serial(50);
	}

	private int wait_and_read_serial(int timeout) throws RainbowduinoTimeOut {
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

		public RainbowduinoError(int waitAndReadSerial) {
			PApplet.println(waitAndReadSerial);
		}


	}


}

