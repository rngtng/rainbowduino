package com.rngtng.rainbowduino;

/**
 * The LaunchpadListener Interface, implement this for interaction feedback
 * 
 * @author rngtng - Tobias Bielohlawek
 * 
 */

public interface RainbowduinoListener {

    public void rainbowduinoAvailable(Rainbowduino rainbowduino);

    public void rainbowduinoUnavailable(Rainbowduino rainbowduino);

}