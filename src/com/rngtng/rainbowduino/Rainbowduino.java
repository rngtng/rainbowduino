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

import java.io.IOException;
import java.util.ArrayList;
import java.util.TooManyListenersException;
import java.util.Vector;

import processing.core.PApplet;
import processing.serial.Serial;

import gnu.io.*;

/**
 * 
 *
 * @example Launchpad
 * @author rngtng - Tobias Bielohlawek
 *
 */
public class Rainbowduino  { //implements SerialPortEventListener

	PApplet app;
	int CRTL  = 255;
	int RESET = 255;

	int PING = 254;
	int HELLO = 254;

	int WRITE_FRAME  = 253;
	int WRITE_EEPROM = 252;
	int READ_EEPROM  = 251;

	int SPEED = 249;
	int SPEED_INC = 128; //B1000 0000
	int SPEED_DEC = 1;   //B0000 0001

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
		this.baud = _baud;
		init_port(port_name);
		String[] ports = Serial.list();
		for(int i = 0; port == null && i < ports.length; i++) {
			if( PApplet.match(ports[i], "tty") == null) continue;
			init_port(ports[i]);
		}
		 /* if(port != null) {
			try {
				port.port.addEventListener(this);
			} catch (TooManyListenersException e) { 
				e.printStackTrace();
			}
			port.port.notifyOnDataAvailable(true);
		} */
	}

	public boolean init_port(String port_name) {
		if(port_name == null) return false;
		port = new Serial(app, port_name, this.baud);
		port.buffer(0);
		try {
			if(wait_and_read_serial(20) == 252) {
				command(PING);
				if(wait_and_read_serial(20) == HELLO) return true;         
				port.stop();
			}
		} catch (Exception e) {
			PApplet.println("Failed");
			e.printStackTrace();
		}
		PApplet.println("No response");        			
		if(port != null) port.stop();
		port = null;
		return false;
	}
	
	/* +++++++++++++++++++ */

	private void command( int command ) {
		send(CRTL);
		send(command);
	}

	private void send_row(byte[] row) {
		for(int i = 0; i < row.length; i++) {
			send(row[i]);
		}
	}

	private int wait_and_read_serial() {
		try {
			return wait_and_read_serial(50);
		}
		catch( Exception e) {  
			PApplet.println("Matrix Timeout");
			return 0;
		}
	}

	private int wait_and_read_serial(int timeout) throws Exception {
		//what if port is NULL??
		while( timeout > 0 && port.available() < 1) {
			//print(".");
			sleep(100);
			timeout--;
			if(timeout == 0) throw new Exception();
		}
		return port.read();
	}

	private void send(int value) {
		if(port == null ) return;
		port.write(value);
	}
	
	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		}
		catch(InterruptedException e) {
		}
	}

	/*
	synchronized public void serialEvent(SerialPortEvent serialEvent) {
		buffer.add(port.read());
	}
	*/

}
