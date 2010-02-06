
//get soundcipher here: http://soundcipher.org/
import arb.soundcipher.*;

import processing.serial.*;
import com.rngtng.rainbowduino.*;

Rainbowduino rainbowduino = null;
SoundCipher sc = new SoundCipher(this);

void setup() {
  RainbowduinoDetector.start(this);    
}

void draw() {
  if( rainbowduino != null && rainbowduino.ping() ) println("Ping successful");

  delay(2000); 
}


void rainbowuinoAvailable(Rainbowduino _rainbowduino) {
  rainbowduino = _rainbowduino;
  println("found");  
  
  sc.playNote(60, 100, 0.5);
  delay(300);
  sc.playNote(80, 100, 1.0);
}

void rainbowuinoUnavailable(Rainbowduino _rainbowduino) {
  rainbowduino = null;
  println("lost");
  
  
  sc.playNote(80, 100, 0.5);
  delay(300);
  sc.playNote(60, 100, 1.0);
}


