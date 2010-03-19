import processing.serial.*;

Serial p;

void setup() {  
  p = new  Serial(this, Serial.list()[0], 57600);
}
int k  = 2000;

void draw() {
delay(k);  
 k = 500;
  // cmd( 252 );
  // println("done");


  while( p.available() > 0) {
    println(p.read());
  }
  cmd( 208, 1);
  println("done"); 
  delay(500);

  cmd( 208, 16);
  println("done"); 
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
    delay(10);
  }
}

