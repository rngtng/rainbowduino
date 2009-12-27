import processing.serial.*;
import com.rngtng.rainbowduino.*;

Rainbowduino rainbowduino;

int level;
int f = 1;

//byte colors[] = {1,16,3,4,5,6,7,8,10,12,14,16};

void setup() {
  rainbowduino = new Rainbowduino(this);
  rainbowduino.initPort();
  rainbowduino.bufferSetAt(0, new int[]{
    255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255  }
  );
  rainbowduino.brightnessSet(1);
  level =1; // rainbowduino.brightnessGet();
}

void draw() {
  level += f;  
  rainbowduino.brightnessSet(level);
  if(level >= 15 || level < 1 )  f = -1 * f;
  delay(50);
}




