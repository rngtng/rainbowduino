import processing.serial.*;

Serial p;

void setup() {  
  p = new  Serial(this, Serial.list()[0], 57600);
}

int as =11;

void draw() {
  delay(120);
  cmd( as, 252 ); //ping
  delay(1);

read();
  
cmd( as, 208, 1); // set brightess 1
delay(15);

read();

cmd( as, 208, 16); // set brightess 16
delay(15);  
read();
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

void send( int[] data ) {
  for(int k = 0; k < data.length; k++) {
    p.write(data[k]);
    delay(10);
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