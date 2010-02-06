import processing.serial.*;
import com.rngtng.rainbowduino.*;

Rainbowduino rainbowduino;

void setup() {
  RainbowduinoDetector.start(this);    
}

void draw() {
  if( rainbowduino ) {
  println("API Version: " + rainbowduino.apiVersion());
  if( rainbowduino.ping() ) println("Ping successful");
  }
  sleep(1000); 

}


   void rainbowuinoAvailable(Rainbowduino _rainbowduino) {
     rainbowduino = _rainbowduino; 
   }
   
   void rainbowuinoUnavailable(Rainbowduino _rainbowduino) {
   rainbowduino = null;
   }


