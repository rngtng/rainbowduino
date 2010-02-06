package com.rngtng.rainbowduino;

/**
 * The LaunchpadListener Interface, implement this for interaction feedback
 *
 * @author rngtng - Tobias Bielohlawek
 *
 */

public interface RainbowduinoListener {
    
    public void rainbowuinoAvailable(Rainbowduino rainbowduino);
    public void rainbowuinoUnavailable(Rainbowduino rainbowduino);
    
}