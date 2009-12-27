import processing.serial.*;
import com.rngtng.rainbowduino.*;

Rainbowduino rainbowduino;

void setup() {
  rainbowduino = new Rainbowduino(this);
  rainbowduino.initPort();
  println("Init done");
  
  println("API Version: " + rainbowduino.apiVersion());
  if( rainbowduino.ping() ) println("Ping successful");
  noLoop();
}

void draw() { 
}


