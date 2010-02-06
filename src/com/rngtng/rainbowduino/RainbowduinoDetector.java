package com.rngtng.rainbowduino;

import java.util.Vector;

import processing.core.PApplet;
import processing.serial.Serial;

public class RainbowduinoDetector extends Thread {

	final int START_THREAD_SLEEP = 500;
	final int THREAD_SLEEP = 5000;
	
	static RainbowduinoDetector rainbowduinoDetector; 

	PApplet app;
	private Vector<String> checked_ports;
	private Vector<RainbowduinoListener> listeners;

	private static Thread thread;
	
	private int thread_sleep = START_THREAD_SLEEP;
	public boolean running = false;
	
	public static RainbowduinoDetector init(PApplet _app) {
		if(rainbowduinoDetector == null) { 
			rainbowduinoDetector = new RainbowduinoDetector(_app);
		}
		return rainbowduinoDetector;
	}

	public static void start(PApplet _app) {
		thread = RainbowduinoDetector.init(_app);
		((RainbowduinoDetector) thread).running = true;
		thread.start();
	}

	public static void stopp() {		
		((RainbowduinoDetector) thread).running = false;
	}
	
	public static String getAvailablePortName(PApplet _app) {
		Rainbowduino rainbowduino = RainbowduinoDetector.init(_app).detectRainbowudino();
		String portName = rainbowduino.getPortName();
		rainbowduino.close(); //close port
		return portName;		 
	}	

	RainbowduinoDetector(PApplet _app) {
		app = _app;
		//app.registerDispose(this);
		checked_ports = new Vector<String>();
		listeners = new Vector<RainbowduinoListener>();
		addListener(new RainbowduinoPAppletListener(_app));		
	}

	public void run() {
		while(running) {
			PApplet.println("check");
			Rainbowduino rainbowduino = detectRainbowudino();
			if(rainbowduino != null) registerRainbowduino(rainbowduino);
			PApplet.println("done");
			this.sleep(5000);			
		}
		PApplet.println("stopped");
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
		PApplet.println(portName);
		try {			
			Rainbowduino rainbowduino = new Rainbowduino(this.app, portName);
			sleep(this.thread_sleep); //give Rainbowduino time to initialize			
			if(rainbowduino.ping()) return rainbowduino;
			PApplet.println("No response " + portName);							
		}
		catch (Exception e) {
			PApplet.println("Error port " + portName);
		}       							
		return null;
	}

	void registerRainbowduino(Rainbowduino rainbowduino) {
		//TODO add to Rainbowudino list
		for(RainbowduinoListener listener : listeners) {				 
			listener.rainbowuinoAvailable(rainbowduino);
		}	
		//now, as rainbowudino is found, lower polling time
		this.thread_sleep = THREAD_SLEEP;
	}

	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		}
		catch(InterruptedException e) {
		}
	}
}
