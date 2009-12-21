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
public class Rainbowduino {
	
	public static int width = 8;
	public static int height = width;

	public final String VERSION = "0.1";

//	Vector<LaunchpadListener> listeners;


	/**
	 * a Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the library.
	 *
	 * @example Rainbowduino
	 * @param _app parent Applet
	 */
	public Rainbowduino(PApplet _app) {
		//super(_app);
        port.addEventListener(this);
        port.notifyOnDataAvailable(true);
		
//		listeners = new Vector<LaunchpadListener>();
	//	addListener(new LaunchadPAppletListener(_app));
	}

	public void dispose() {
	}

	/**
	 * return the version of the library.
	 *
	 * @return String version number
	 */
	public String version() {
		return VERSION;
	}

	/* -- Listener Handling -- */

	/**
	 * 	Adds a listener who will be notified each time a new MIDI message is received from a MIDI input device. If the listener has already been added, it will not be added again.
	 *
	 * @param listener the listener to add.
	 * @return true if and only the listener was successfully added.
	 * @see #removeListener(LaunchpadListener listener)
	 */
	/* public boolean addListener(LaunchpadListener listener) {
		for(LaunchpadListener current : listeners) if(current == listener) return false;	
		listeners.add(listener);				
		return true;
	} */

	/**
	 * Removes a given listener.
	 *
	 * @param listener the listener to remove.
	 * @return true if and only the listener was successfully removed.
	 * @see #addListener(LaunchpadListener listener)
	 */
/*	public boolean removeListener(LaunchpadListener listener) {
		for(LaunchpadListener current : listeners) {
			if(current == listener) {
				listeners.remove(listener);
				return true;
			}
		}
		return false;
	} */	

	  synchronized public void serialEvent(SerialPortEvent serialEvent) {
		    if (serialEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
		      try {
		        while (input.available() > 0) {
		          synchronized (this.buffer) {
		            if (bufferLast == buffer.length) {
		              byte temp[] = new byte[bufferLast << 1];
		              System.arraycopy(buffer, 0, temp, 0, bufferLast);
		              buffer = temp;
		            }
		            buffer[bufferLast++] = (byte) input.read();
		            if (serialEventMethod != null) {
		              if ((bufferUntil &&
		                   (buffer[bufferLast-1] == bufferUntilByte)) ||
		                  (!bufferUntil &&
		                   ((bufferLast - bufferIndex) >= bufferSize))) {
		                try {
		                  serialEventMethod.invoke(parent, new Object[] { this });
		                } catch (Exception e) {
		                  String msg = "error, disabling serialEvent() for " + port;
		                  System.err.println(msg);
		                  e.printStackTrace();
		                  serialEventMethod = null;
		                }
		              }
		            }
		          }
		        }

		      } catch (IOException e) {
		        errorMessage("serialEvent", e);
		      }
		    }
		  }

}
