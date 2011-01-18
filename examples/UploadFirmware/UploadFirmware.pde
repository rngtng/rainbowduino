import processing.serial.*;
import com.rngtng.rainbowduino.*;
import com.rngtng.arduinoloader.*;

Rainbowduino rainbowduino;

void setup() {
  rainbowduino = new Rainbowduino(this);
  rainbowduino.initPort();
  println("found");

  if( rainbowduino.connected() ) {
    if(rainbowduino.uploadFirmware() ) {
      println("upload done");
    }
  }
  else {
    //try manually open port
    String p = Serial.list()[0];
    int b = rainbowduino.UPLOAD_BAUD;

    if( rainbowduino.uploadFirmware(p, b) ) {
      println("upload done");
    }
  }

  noLoop();
}

void draw() {
}




