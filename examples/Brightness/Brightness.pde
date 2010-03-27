import processing.serial.*;
import com.rngtng.rainbowduino.*;

Rainbowduino[] rainbowduinos = null;
Rainbowduino rainbowduino_tmp = null;

int level  = 1;
int f = 1;
int rIndex;
//byte colors[] = {1,16,3,4,5,6,7,8,10,12,14,16};


void setup() {
  rainbowduinos = new Rainbowduino[10];
  rIndex = 0;
  RainbowduinoDetector.start(this);
}

int li = 0;

void draw() {
  level += f; 
  
   for( int l = 0; l < rIndex; l++) {
    // if( l >= li ) { 
     rainbowduinos[l].bufferSetAt(0, new int[]{
       255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255  }
     );
    //// li++;     
    // }
     rainbowduinos[l].brightnessSet(level);     
   }
   if(level >= 15 || level < 1 )  f = -1 * f;
   delay(100); 
}

//callback funtion to register new rainbowduinos
void rainbowduinoAvailable(Rainbowduino _rainbowduino) {  
   
  
  rainbowduinos[rIndex] = _rainbowduino;
  rIndex++;
  println("New Rainbowduino found: " + _rainbowduino.slaveNr  );

} 