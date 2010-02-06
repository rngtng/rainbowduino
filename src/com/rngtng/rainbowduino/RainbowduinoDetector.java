package com.rngtng.rainbowduino;

import java.util.Vector;

import processing.core.PApplet;
import processing.serial.Serial;

public class RainbowduinoDetector {

	static RainbowduinoDetector rainbowduinoDetector; 

	PApplet app;
	private Vector<String> checked_ports;
	private Vector<RainbowduinoListener> listeners;
	
	public static RainbowduinoDetector init(PApplet _app) {
		if(rainbowduinoDetector == null) { 
			rainbowduinoDetector = new RainbowduinoDetector(_app);
		}
		return rainbowduinoDetector;
	}

	public static void start(PApplet _app) {
		//RainbowduinoAutodetector.init(_app).
	}

	public static String getAvailablePortName(PApplet _app) {
		Rainbowduino rainbowduino = RainbowduinoDetector.init(_app).detectRainbowudino();
		String portName = rainbowduino.getPortName();
		rainbowduino.dispose();
		return portName;		 
	}	

	RainbowduinoDetector(PApplet _app) {
		app = _app;
		//app.registerDispose(this);
		checked_ports = new Vector<String>();
		listeners = new Vector<RainbowduinoListener>();
		addListener(new RainbowduinoPAppletListener(_app));		
	}

	/* -- Listener Handling -- */

	/**
	 * 	Adds a listener who will be notified each time a new MIDI message is received from a MIDI input device. If the listener has already been added, it will not be added again.
	 *
	 * @param listener the listener to add.
	 * @return true if and only the listener was successfully added.
	 * @see #removeListener(RainbowduinoListener listener)
	 */
	private boolean addListener(RainbowduinoListener listener) {
		for(RainbowduinoListener current : listeners) if(current == listener) return false;	
		listeners.add(listener);				
		return true;
	}

	//TODO app is missing -> param??
	//public static boolean addListener(RainbowduinoListener listener) {
	//	return RainbowduinoAutodetector.init(_app).addListener(listener);
	//}

	/**
	 * Removes a given listener.
	 *
	 * @param listener the listener to remove.
	 * @return true if and only the listener was successfully removed.
	 * @see #addListener(RainbowduinoListener listener)
	 */
	public boolean removeListener(RainbowduinoListener listener) {
		for(RainbowduinoListener current : listeners) {
			if(current == listener) {
				listeners.remove(listener);
				return true;
			}
		}
		return false;
	}		

	/* -- Port Handling -- */

	/**
	 * Open serial port with given name and baud rate.
	 * No sensity checks
	 * 
	 */
	public Rainbowduino detectRainbowudino() {			
		String[] ports = Serial.list();
		for(int i = 0; i < ports.length; i++) {
			if(checked_ports.contains(ports[i])) continue; //it's already checked
			checked_ports.add(ports[i]);
			if(PApplet.match(ports[i], "tty") == null) continue;			
			return openRainbowduino(ports[i]);
			//PApplet.println(ports[i]);
			//TODO now, as one rainbwoudino is found, update thread time				
		}
		return null;
	}

	/* *********************** */

	/**
	 * Open serial port with given name. Send ping to check if port is working.
	 * If not port is closed and set back to null
	 * 
	 * @param portName port to open
	 * @param check whether to perform valid checks
	 * @return whether port could be opened sucessfully 
	 */
	private Rainbowduino openRainbowduino(String portName) {
		if(portName == null) return null;		
		try {			
			Rainbowduino rainbowduino = new Rainbowduino(this.app, portName);
			sleep(3000); //give Rainbowduino time to initialize			
			if(rainbowduino.ping()) return rainbowduino;
			PApplet.println("No response");							
		}
		catch (Exception e) {
			PApplet.println("Error port");
		}       							
		return null;
	}

	void registerRainbowduino(Rainbowduino rainbowduino) {
		app.registerDispose(rainbowduino);
		//TODO add to Rainbowudino list
		for(RainbowduinoListener listener : listeners) {				 
			listener.rainbowuinoAvailable(rainbowduino);
		}				
	}

	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		}
		catch(InterruptedException e) {
		}
	}
}
