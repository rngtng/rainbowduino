import processing.serial.*;
import com.rngtng.rainbowduino.*;

Rainbowduino rainbowduino;

void setup() {
  rainbowduino = new Rainbowduino(this);
  rainbowduino.initPort();

  rainbowduino.reset();
  //Green
  rainbowduino.bufferSetAt(0, new int[]{
    255,0,0,255,0,0,255,0,0,255,0,0,255,0,0,255,0,0,255,0,0,255,0,0    } 
  );
  //Blue
  rainbowduino.bufferSetAt(1, new int[]{
    0,255,0,0,255,0,0,255,0,0,255,0,0,255,0,0,255,0,0,255,0,0,255,0    } 
  );
}

void draw() {
  rainbowduino.reset();
  delay(1000);
  rainbowduino.bufferLoad();
  delay(5000);
}






