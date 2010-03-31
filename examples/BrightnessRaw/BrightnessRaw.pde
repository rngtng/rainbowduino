import processing.serial.*;

/*
 * Example sketch to test 4 Rainbowduinos by changing brightness 
 *
 */

Serial p;
int[] rainbowduinos = new int[]{0,11,12,13}; //master + 3 slaves 


void setup() {  
  p = new  Serial(this, Serial.list()[0], 57600);
}

void draw() {
  // cmd( 0, 254 ); //reset
  // cmd( as, 252 ); //ping
  // cmdSet( 0 ); //fill frame white
  // cmd( 0, 236 ); //save frames to EEPROM

  for(int k = 0; k < rainbowduinos.length; k++) {
   cmd( rainbowduinos[k], 208, 1); // set brightess low
  }
  delay(250);
  
  for(int k = 0; k < rainbowduinos.length; k++) {
   cmd( rainbowduinos[k], 208, 16); // set brightess high
  }
  delay(250);  
}


/*****************************************************************/

void cmdSet( int r ) {
  send( new int[]{ 
    255, r, 246, 25, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255 }
  );
}

void cmd( int receiver, int cmd ) {
  send( new int[] {255, receiver, cmd, 0 } );
}

void cmd( int receiver, int cmd, int data ) {
  send( new int[]{ 255, receiver, cmd, 1, data } );
}

/*****************************************************************/

void send( int[] data ) {
  for(int k = 0; k < data.length; k++) {
    p.write(data[k]);
  }
}

void read() {
  while( p.available() > 0) {
    println("Received: " + p.read());
  }
}