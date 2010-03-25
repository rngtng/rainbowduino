import processing.serial.*;
import com.rngtng.rainbowduino.*;

Rainbowduino rainbowduino = null;
Rainbowduino rainbowduino_tmp = null;

int level  = 1;
int f = 1;

//byte colors[] = {1,16,3,4,5,6,7,8,10,12,14,16};


void setup() {
  RainbowduinoDetector.start(this);
}

void draw() {
  level += f;  
   if( rainbowduino != null ) rainbowduino.brightnessSet(level);
   if(level >= 15 || level < 1 )  f = -1 * f;
   delay(100); 
   
   if( level < 1 && rainbowduino_tmp != null) {
     if(rainbowduino != null) rainbowduino.brightnessSet(0);
  rainbowduino =   rainbowduino_tmp;
     delay(500); 
  //rainbowduino = new  Rainbowduino(_rainbowduino, 11);
  //RainbowduinoDetector.init(this).registerRainbowduino(rainbowduino);
rainbowduino_tmp = null;  

  //rainbowduino.brightnessSet(1);
  //println( "Ping: " + rainbowduino.ping() );
  
   rainbowduino.bufferSetAt(0, new int[]{
     255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255  }
   );     
   }
}

//callback funtion to register new rainbowduinos
void rainbowduinoAvailable(Rainbowduino _rainbowduino) {
 // if( rainbowduino != null) return;
  
  rainbowduino_tmp = _rainbowduino;

  println("New Rainbowduino found: " + _rainbowduino.slaveNr  );

} 