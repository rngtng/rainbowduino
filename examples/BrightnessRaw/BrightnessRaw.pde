import processing.serial.*;

Serial p;

void setup() {  
  p = new  Serial(this, Serial.list()[0], 57600);
}

void draw() {

  cmd( 252 ); //ping
  delay(5000);

  while( p.available() > 0) {
    println("Received: " + p.read());
  }
  
  // cmd( 208, 1); // set brightess 1
  // delay(500);
  // 
  // cmd( 208, 16); // set brightess 16
  // delay(50);  
}

void cmd( int cmd ) {
  send( new int[]{ 
    255, 0, cmd, 0 }
  );
}


void cmd( int cmd, int data ) {
  send( new int[]{ 
    255, 0, cmd, 1, data   }
  );
}

void send( int[] data ) {
  for(int k = 0; k < data.length; k++) {
    p.write(data[k]);
    delay(1000);
  }
  //println("sending done"); 
}

