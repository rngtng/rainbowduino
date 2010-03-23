package com.rngtng.rainbowduino;

import java.util.Vector;

import processing.core.PApplet;
import processing.serial.Serial;

public class RainbowduinoDetector extends Thread {

	final int START_THREAD_SLEEP = 3000; //milliseconds
	final int THREAD_SLEEP = 10000; //milliseconds

	static RainbowduinoDetector rainbowduinoDetector; 

	PApplet app;

	//private HashMap
	private Vector<Rainbowduino> rainbowduinos;
	private Vector<String> checked_ports;
	private Vector<RainbowduinoListener> listeners;

	private int thread_sleep = START_THREAD_SLEEP;
	public boolean running = false;

	public static RainbowduinoDetector init(PApplet _app) {
		if(rainbowduinoDetector == null) { 
			rainbowduinoDetector = new RainbowduinoDetector(_app);
		}
		return rainbowduinoDetector;
	}

	public static void start(PApplet _app) {
		if(rainbowduinoDetector == null) RainbowduinoDetector.init(_app);
		if(rainbowduinoDetector.running) return;
		rainbowduinoDetector.running = true;
		((Thread) rainbowduinoDetector).start();		
	}

	public static void halt() {
		if(rainbowduinoDetector == null || !rainbowduinoDetector.running) return;
		rainbowduinoDetector.running = false;		
	}

	public static String getAvailablePortName(PApplet _app) {
		Rainbowduino rainbowduino = RainbowduinoDetector.init(_app).detectRainbowudino();
		if( rainbowduino == null ) return null;
		String portName = rainbowduino.getPortName();
		rainbowduino.close(); //close port
		return portName;		 
	}	

	public static void notifyError(Rainbowduino rainbowduino) {
		if(rainbowduino == null || rainbowduinoDetector == null) return; 
		rainbowduinoDetector.unregisterRainbowduino(rainbowduino);	 
	}	

	/***************************************************************/

	RainbowduinoDetector(PApplet _app) {
		this.app = _app;
		PApplet.println( "Rainbowduino Version 1");
		//app.registerDispose(this);
		checked_ports = new Vector<String>();
		listeners = new Vector<RainbowduinoListener>();
		rainbowduinos = new Vector<Rainbowduino>();
		addListener(new RainbowduinoPAppletListener(this.app));		
	}

	public void run() {
		while(running) {
			PApplet.println("check");
			Rainbowduino rainbowduino = detectRainbowudino();
			registerRainbowduino(rainbowduino);
			this.sleep(thread_sleep);			
		}
	}

	/* -- Listener Handling -- */

	/**
	 * 	Adds a listener who will be notified each time new Rainbowduino is connected
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

	/**
	 * 	Adds a listener who will be notified each time new Rainbowduino is connected	 
	 */
	public static boolean addListener(PApplet _app, RainbowduinoListener listener) {
		return RainbowduinoDetector.init(_app).addListener(listener);
	}

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

	/**
	 * 	Removes a given listener.	 
	 */
	public static boolean removeListener(PApplet _app, RainbowduinoListener listener) {
		return RainbowduinoDetector.init(_app).addListener(listener);
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
			sleep(1000); //give new serial port time to init
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
	 * @return whether port could be opened successfully 
	 */
	private Rainbowduino openRainbowduino(String portName) {
		if(portName == null) return null;
		PApplet.println(portName);
		try {			
			Rainbowduino rainbowduino = new Rainbowduino(this.app, portName);
			sleep(3000); //give Rainbowduino time to initialize			
			if(rainbowduino.ping()) return rainbowduino;			
		}
		catch (RuntimeException e) {
		}       							
		return null;
	}

	void registerRainbowduino(Rainbowduino rainbowduino) {
		if(rainbowduino == null || rainbowduinos.contains(rainbowduino)) return;

		rainbowduinos.add(rainbowduino);
		for(RainbowduinoListener listener : listeners) {				 
			listener.rainbowduinoAvailable(rainbowduino);
		}			

		//now, as rainbowudino is found, lower polling time
		this.thread_sleep = THREAD_SLEEP;
	}

	void unregisterRainbowduino(Rainbowduino rainbowduino) {
		if(!rainbowduinos.contains(rainbowduino)) return;

		for(RainbowduinoListener listener : listeners) {				 
			listener.rainbowduinoUnavailable(rainbowduino);
		}	
		checked_ports.remove(rainbowduino.getPortName());
		rainbowduinos.remove(rainbowduino);

		//now, as rainbowudino is no found, increase polling time
		this.thread_sleep = START_THREAD_SLEEP;
	}	

	void messageAvailable(RainbowduinoMessage message) {
		for(Rainbowduino rainbowduino : rainbowduinos) {
			if(rainbowduino.slaveNr == message.receiver()) {
				rainbowduino.messageAvailable(message);
				return;
			}		
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
