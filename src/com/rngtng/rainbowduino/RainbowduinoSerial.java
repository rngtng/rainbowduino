package com.rngtng.rainbowduino;

/*
 RainbowduinoSerial - class for serial port goodness
 Extension to the PSerial class, part of the Processing project - http://processing.org

 PSerial Copyright (c) 2004-05 Ben Fry & Casey Reas
 */

import gnu.io.SerialPortEvent;

import java.io.IOException;

import processing.serial.Serial;

/**
 * Class for sending and receiving data using the serial communication protocol.
 * 
 * @webref
 */
public class RainbowduinoSerial extends Serial {

    // defaults
    Rainbowduino rainbowduino = null;
    RainbowduinoMessage message = null;

    public RainbowduinoSerial(Rainbowduino irainbowduino, String iname,
            int irate) {
        super(irainbowduino.app, iname, irate);
        this.rainbowduino = irainbowduino;
        this.buffer(20);
        this.message = new RainbowduinoMessage();
    }

    /**
     * Used by PApplet to shut things down.
     */
    @Override
    public void dispose() {
        this.rainbowduino = null;
        super.dispose();
    }

    @Override
    synchronized public void serialEvent(SerialPortEvent serialEvent) {
        // super.serialEvent(serialEvent);
        if (serialEvent.getEventType() != SerialPortEvent.DATA_AVAILABLE)
            return;

        try {
            while (input.available() > 0) {
                message.consume(input.read());
                if (message.ready()) {
                    this.rainbowduino.messageAvailable(message);
                    message = new RainbowduinoMessage();
                }
            }
        } catch (IOException e) {
            errorMessage("serialEvent", e);
        }
    }

}
