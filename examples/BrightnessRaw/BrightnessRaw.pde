import processing.serial.*;

Serial p;

void setup() {  
  p = new  Serial(this, Serial.list()[0], 57600);
}

int as =11;

void draw() {
  //cmd( 0, 254 ); 
  //delay(4000);
  /*
  cmdSet( 0 );
delay(1000);
cmd( 0, 236 ); 
delay(1000);
  */
/*  delay(120);
  cmd( as, 252 ); //ping
  delay(1);

read(); */
  
cmd( 0, 208, 1); // set brightess 1  
cmd( 11, 208, 1); // set brightess 1
cmd( 12, 208, 1); // set brightess 1
cmd( 13, 208, 1); // set brightess 1
delay(250);

//read();

cmd( 0, 208, 16); // set brightess 16
cmd( 11, 208, 16); // set brightess 16
cmd( 12, 208, 16); // set brightess 16
cmd( 13, 208, 16); // set brightess 16
delay(250);  
//read();
}

void cmd( int r, int cmd ) {
  send( new int[]{ 
    255, r, cmd, 0 }
  );
}


void cmd( int r, int cmd, int data ) {
  send( new int[]{ 
    255, r, cmd, 1, data   }
  );
}

void cmdSet( int r ) {
  send( new int[]{ 
    255, r, 246, 25, 0, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255 }
  );
}

void send( int[] data ) {
  for(int k = 0; k < data.length; k++) {
    p.write(data[k]);
  }
  //println("sending done"); 
}


void read() {
  while( p.available() > 0) {
    int v = p.read();
    if( v == 15) println();
    if( v == 15) println();    
    println("Received: " + v);
  }
}