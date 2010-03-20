import processing.serial.*;
import com.rngtng.rainbowduino.*;

Rainbowduino rainbowduino = null;

int level;
int f = 1;

//byte colors[] = {1,16,3,4,5,6,7,8,10,12,14,16};

void setup() {
  RainbowduinoDetector.start(this);
  //rainbowduino = new Rainbowduino(this);

  level = 1; // rainbowduino.brightnessGet();
}

void draw() {
  level += f;  
  if( rainbowduino != null ) rainbowduino.brightnessSet(level);
  if(level >= 15 || level < 1 )  f = -1 * f;
  delay(50);
}


void rainbowuinoAvailable(Rainbowduino _rainbowduino) {
  rainbowduino = _rainbowduino;
  rainbowduino.bufferSetAt(0, new int[]{
    255,255,255,255,255,255,255,255,0,255,255,8,255,255,128,63,255,255,255,255,255,255,255,255  }
  );
  rainbowduino.brightnessSet(1);  
  println("hi " + rainbowduino.slaveNr );
}