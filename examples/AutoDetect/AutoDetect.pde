import processing.serial.*;
import com.rngtng.rainbowduino.*;

Rainbowduino rainbowduino = null;

void setup() {
  RainbowduinoDetector.start(this);    
}

void draw() {
  if( rainbowduino != null) {
    println("API Version: " + rainbowduino.apiVersion());
    if( rainbowduino.ping() ) println("Ping successful");
  }
  //  println("go");
  delay(2000); 

}


void rainbowuinoAvailable(Rainbowduino _rainbowduino) {
  rainbowduino = _rainbowduino; 
  //RainbowduinoDetector.stopp();
  println("found");
}

void rainbowuinoUnavailable(Rainbowduino _rainbowduino) {
  rainbowduino = null;
  println("lost");
}



