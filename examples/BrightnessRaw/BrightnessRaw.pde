import processing.serial.*;

Serial p;

void setup() {  
  p = new  Serial(this, Serial.list()[0], 57600);
}

void draw() {

  cmd( 252 );
  println("done");
  delay(1000);  

  while( p.available() > 0) {
    println(p.read());
  }
  // cmd( 208, 1);
  // println("done"); 
  // delay(1000);
}

void cmd( int cmd ) {
  send( new int[]{ 
    255, 0, 1, cmd }
  );
}


void cmd( int cmd, int data ) {
  send( new int[]{ 
    255, 0, 2, cmd, data   }
  );
}

void send( int[] data ) {
  for(int k = 0; k < data.length; k++) {
    p.write(data[k]);
    delay(200);
  }
}

