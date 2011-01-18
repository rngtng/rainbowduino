import processing.serial.*;
import com.rngtng.rainbowduino.*;

Rainbowduino rainbowduino;

void setup() {
  rainbowduino = new Rainbowduino(this);
  rainbowduino.initPort();

  rainbowduino.stop();
  rainbowduino.reset();

  //Green
  rainbowduino.bufferSetAt(0, new int[]{
    255,0,0,255,0,0,255,0,0,255,0,0,255,0,0,255,0,0,255,0,0,255,0,0  }
  );
  //Blue
  rainbowduino.bufferSetAt(1, new int[]{
    0,255,0,0,255,0,0,255,0,0,255,0,0,255,0,0,255,0,0,255,0,0,255,0  }
  );
  //Red
  rainbowduino.bufferSetAt(2, new int[]{
    0,0,255,0,0,255,0,0,255,0,0,255,0,0,255,0,0,255,0,0,255,0,0,255  }
  );
  rainbowduino.start();

  println("BufferLength: " + rainbowduino.bufferLength());
  //  rainbowduino.bufferSave();
  noLoop();
}

void draw() {
}





