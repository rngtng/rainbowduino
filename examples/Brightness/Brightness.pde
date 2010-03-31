import processing.serial.*;
import com.rngtng.rainbowduino.*;

/*
 * Example sketch to test connected Rainbowduinos by fading brightness up and down
 *
 */

Vector rainbowduinos;
int level   = 1;
int level_f = 1; // 1 -> increase brightness, -1 => decrease brightness

int[] WHITE = new int[]{255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255};


void setup() {
  rainbowduinos = new Vector(); //simple array to store all connected rainbowduinos
  RainbowduinoDetector.start(this);
}

void draw() {
   
   for( int i = 0; i < rainbowduinos.size(); i++) {
     Rainbowduino rainbowduino = (Rainbowduino) rainbowduinos.get(i);
     rainbowduino.bufferSetAt(0, WHITE);  //Just send this on init!?
     rainbowduino.brightnessSet(level);
   }
   
   level += level_f;
   if(level >= 16 || level < 1)  level_f *= -1; //invert
   delay(100); 
}

//callback funtion to register new rainbowduinos
void rainbowduinoAvailable(Rainbowduino _rainbowduino) {
  rainbowduinos.add(_rainbowduino);
  println("New Rainbowduino found: " + _rainbowduino.slaveNr  );
}