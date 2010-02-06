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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.rngtng.arduinoloader.ArduinoLoader;

import processing.core.PApplet;
import processing.serial.Serial;

/**
 * 
 *
 * @author rngtng - Tobias Bielohlawek
 *
 */
public class Rainbowduino  implements RCodes {

	public final int UPLOAD_BAUDRATE = 19200;	
	public final int BAUDRATE = 57600;	

	public final int TIMEOUT = 20; // * 100 ms
	public final String VERSION = "0.2";

	PApplet app;
	Serial port;

	public static int width = 8;
	public static int height = width;	

	/**
	 * Create a new instance to communicate with the rainbowduino. Make sure to (auto)init the serial port, too 
	 * 
	 * @param _app parent Applet
	 */	
	public Rainbowduino(PApplet _app) {
		this(_app, RainbowduinoDetector.getAvailablePortName(_app));
	}	

	public Rainbowduino(PApplet _app, String portName) {
		this.app = _app;
		app.registerDispose(this);
		openPort(portName);	
	}

	public void close() {
		dispose();
	}

	public void dispose() {
		if(connected()) port.stop();
		port = null;
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
	 * @return wheter rainbowudino is connected
	 */
	public boolean connected() {
		return (port != null);
	}	

	/**
	 * @return boolean if successfull 
	 */
	public boolean uploadFirmware() {	
		if(!connected()) return false;

		this.port.stop();
		if(uploadFirmware( this.getPortName(), this.UPLOAD_BAUDRATE) ) {
			sleep(3000);
			this.openPort(this.getPortName());
			return true;
		}
		return false;
	}

	/**
	 * @return boolean if successfull 
	 */
	public boolean uploadFirmware(String portName, int baud) {	   
		try {
			ClassLoader cl = this.getClass().getClassLoader();			
			BufferedReader in = new BufferedReader( new InputStreamReader( cl.getResourceAsStream("firmware.hex") ) );		

			int imagesize = ArduinoLoader.upload( in, portName, baud, 128);
			System.out.println("Completed, " + imagesize + " bytes uploaded");
		}
		catch(Exception e ) {
			PApplet.println( e.getMessage());
			return false;
		}
		return true;
	}	

	/**
	 * @return int version number of the api 
	 */
	public int apiVersion() {	   
		try {
			sendCommand(API_VERSION);
			return receive(API_VERSION);
		} catch (RainbowduinoError e) {
			e.print();
		}		
		return 0;
	}

	/**
	 * 
	 * @return wheter ping was successfull
	 * TODO: add time??
	 */
	public boolean ping() {
		try {
			sendCommand(PING);
			receive(PING);
			return true;
		} catch (RainbowduinoError e) {
			e.print();
		}		
		return false;
	}

	/**
	 *  resets rainbowduino
	 */
	public void reset() {
		sendCommand(RESET);
	}

	/**
	 *  start running standalone, looping through set frames
	 */
	public void start() {	   
		sendCommand(START);
	}

	/**
	 *  stops running
	 */
	public void stop() {
		sendCommand(STOP);
	}

	/**
	 * set frame pointer to given position
	 *  
	 * @param frame_pos number of frame to set to
	 */
	public void frameSet(int frame_pos) {
		sendCommand(FRAME_SET);
		sendParam(frame_pos);
	}

	/**
	 * @return current position of frame pointer
	 */
	public int frameGet() {	   
		try {
			sendCommand(FRAME_GET);
			return receive(FRAME_GET);
		} catch (RainbowduinoError e) {
			e.print();
		}		
		return 0;
	}

	/* +++++++++++++++++++ */
	/**
	 * Sets the brightness value
	 */
	public void brightnessSet(int brightness) {
		sendCommand(BRIGHTNESS_SET);
		sendParam(brightness);
	}

	/**
	 * @return the current brightness value
	 */
	public int brightnessGet() {	   
		try {
			sendCommand(BRIGHTNESS_GET);
			return receive(FRAME_GET);
		} catch (RainbowduinoError e) {
			e.print();
		}		
		return 0;
	}	

	/* +++++++++++++++++++ */
	/**
	 * fills buffer with given data at given position
	 */
	public void bufferSetAt(int adr, int[] content) {
		bufferSetAt(adr, content, false);
	}

	/**
	 * fills buffer with given data at given position
	 * @param adr position
	 * @param content buffer data
	 * @param check wheter to perform sensity check
	 */
	public void bufferSetAt(int adr, int[] content, boolean check) {	   
		try {
			sendCommand(BUFFER_SET_AT);
			sendParam(adr);
			if(check) {
				int accepted_size = receive(BUFFER_SET_AT); 
				if(content.length != accepted_size) throw new RainbowduinoError(ERROR_MISSMATCH);
			}
			for(int i = 0; i < content.length; i++) {
				send(content[i]);
			}			
		} catch (RainbowduinoError e) {			
			e.print();
		}		
	}

	/** 
	 * @param adr
	 * @return buffer data at given position
	 */
	public int[] bufferGetAt(int adr) {	 		
		try {
			sendCommand(BUFFER_GET_AT);
			sendParam(adr);
			int size = receive(BUFFER_GET_AT);
			PApplet.println("Size :" + size);
			int[] content = new int[size];
			for(int i = 0; i < size; i++) {			
				try {
					content[i] = waitAndReadSerial();
				} catch (RainbowduinoTimeOut e) {
					throw new RainbowduinoError(ERROR_TIME_OUT);
				}
			}
			return content;
		} catch (RainbowduinoError e) {
			e.print();
		}	
		return null;
	}	

	/**
	 * @return buffer length
	 */
	public int bufferLength() {
		try {
			sendCommand(BUFFER_LENGTH);
			return receive(BUFFER_LENGTH);
		} catch (RainbowduinoError e) {
			e.print();
		}
		return 0;		
	}

	/**
	 * saves current buffer to EEPROM
	 */
	public void bufferSave() {
		sendCommand(BUFFER_SAVE);
	}	

	/**
	 * loads current buffer from EEPROM
	 */
	public int bufferLoad() {
		try {
			sendCommand(BUFFER_LOAD);
			return receive(BUFFER_LOAD);
		} catch (RainbowduinoError e) {
			e.print();
		}
		return 0;				
	}		

	/* +++++++++++++++++++ */
	/**
	 * sets speed to given value
	 */
	public void speedSet(int speed_value) {
		sendCommand(SPEED_SET);
		sendParam(speed_value);
	}

	/**
	 * @return current speed
	 */
	public int speedGet() {	   
		try {
			sendCommand(SPEED_GET);
			return receive(SPEED_GET);
		} catch (RainbowduinoError e) {
			e.print();
		}
		return 0;
	}	

	/**
	 * increases speed value
	 */
	public void speedUp() {
		sendCommand(SPEED_INC);
	}

	/**
	 * decreases speed value
	 */
	public void speedDown() {
		sendCommand(SPEED_DEC);
	}

	/* +++++++++++++++++++ */
	/**
	 * sets speed to given value
	 */
	public void slaveActiv(int slave_nr) {
		sendCommand(SLAVE_ACTIV);
		sendParam(slave_nr);
	}

	/**
	 * sets speed to given value
	 */
	public void slaveNrSet(int slave_nr) {
		sendCommand(SLAVE_NR_SET);
		sendParam(slave_nr);
	}

	/**
	 * @return current speed
	 */
	public int slaveNrGet() {	   
		try {
			sendCommand(SLAVE_NR_GET);
			return receive(SLAVE_NR_GET);
		} catch (RainbowduinoError e) {
			e.print();
		}
		return 0;
	}	

	/* +++++++++++++++++++ */
	private void sendCommand(int command_code) {		
		//init command			
		send(COMMAND);
		//flush buffer		
		if( connected() ) port.clear();		
		//send command
		send(command_code);
		sleep(10);
	}

	private void sendParam(int param) {
		//send param			
		send(param);						
	}

	private int receive(int command) throws RainbowduinoError {		
		//wait for response code
		try {	
			switch(waitAndReadSerial()) {
			case ERROR:
				//return error code
				throw new RainbowduinoError(waitAndReadSerial());							
			case OK:
				//return ok code
				if(waitAndReadSerial() == command) return waitAndReadSerial();
			default:
				return receive(command);
			}
		}
		catch( RainbowduinoTimeOut e) {  
			throw new RainbowduinoError(ERROR_TIME_OUT);
		}	
	}

	public String getPortName() {
		if(connected()) return port.port.getName();
		return null;
	}

	/* +++++++++++++++++++ */
	/**
	 * Open serial port with given name. Send ping to check if port is working.
	 * If not port is closed and set back to null
	 * 
	 * @param portName port to open
	 * @param check whether to perform valid checks
	 * @return whether port could be opened sucessfully 
	 */
	private boolean openPort(String portName) {		
		try {
			port = new Serial(app, portName, BAUDRATE);
			port.buffer(20);
			return true;
		}
		catch (RuntimeException e) {
		}
		return false;
	}

	private void send(int value) {
		if(!connected()) return;
		try {
			port.write(value);			
		}
		catch(RuntimeException e) {
			RainbowduinoDetector.notifyError(this);
			this.close();
		}
	}

	private int waitAndReadSerial() throws RainbowduinoTimeOut {
		return waitAndReadSerial(TIMEOUT);
	}

	private int waitAndReadSerial(int timeout) throws RainbowduinoTimeOut {
		if(!connected()) throw new RainbowduinoTimeOut();
		while( timeout > 0 && port.available() < 1) {
			//print(".");
			sleep(100); //in ms
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
		int error;

		public RainbowduinoError(int _error) {
			this.error = _error;			
		}

		public void print() {
			PApplet.println("Error happend: " + this.error);
		}		
	}

	/******************* deprecated Stuf ************************************/
	public void initPort() {
		this.initPort(null, 0, true);
	}

	public void initPort(int _baud) {
		this.initPort(null, _baud, true);
	}    

	public boolean initPort(String port_name, int _baud, boolean check) {
		PApplet.println("initPort() is deprectated and not needed anymore, just delete the line");
		return true;
	}	


}
