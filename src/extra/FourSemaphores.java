package extra;

public class FourSemaphores {
static int[] buffer       = {0,0,0,0,0,0,0,0};
static int n              = 0;
static int size           = 8;
static int front          = 0;
static int rear           = 0;
static Semaphore sender   = new Semaphore (1);
static Semaphore receiver = new Semaphore (1);
static Semaphore slot     = new Semaphore (size);
static Semaphore item     = new Semaphore (0);


public static void main (String[] a) {
  System.out.println ("Main thread starts.");
  Consumer c1 = new Consumer ();
  c1.name="c1";
  Consumer c2 = new Consumer ();
  c2.name="c2";
  Producer p1 = new Producer ();
  p1.name="p1";
  Producer p2 = new Producer ();
  p2.name="p2";

  c1.start ();
  c2.start ();
  p1.start ();
  p2.start ();

  try {
    c1.join ();
    c2.join ();
    p1.join ();
    p2.join ();
  }
  catch (InterruptedException e) { };
  System.out.println ("System terminates normally.");
  }
}

class Semaphore {
  private int value;
  Semaphore (int value1) {
    value = value1;
  }

  public synchronized void Wait () {
    while( value <= 0 ) {
      try { wait (); }
      catch (InterruptedException e) { };
    }
    value--;
  }

  public synchronized void Signal () {
    ++value;
    notify ();
  }

}

class Consumer extends Thread {
	public String name;
  public void run () {
    System.out.println ("Consumer "+name+ " starts.");
    for( int j=1; j<=20; j++ ) {
      FourSemaphores.item.Wait(); // wait for an item to be available
        FourSemaphores.receiver.Wait(); // prevent receiver concurency
          FourSemaphores.front = (FourSemaphores.front + 1) % FourSemaphores.size;
          System.out.println("Consumer "+name+": n = " + FourSemaphores.buffer[FourSemaphores.front] + ".");
        FourSemaphores.receiver.Signal(); // allow the receivers to continue
      FourSemaphores.slot.Signal(); // signal that a new slot is free
    };
    System.out.println ("Consumer terminates.");
  }
}

class Producer extends Thread {
	public String name;
  public void run () {
    System.out.println ("Producer "+name+ " starts.");
    for( int j=1; j<=20; j++ ) {
      FourSemaphores.slot.Wait(); // wait for an empty buffer slot
        FourSemaphores.sender.Wait(); // prevent sender concurrency
          System.out.println( "Producer "+name+": incrementing n.");
          FourSemaphores.rear = (FourSemaphores.rear + 1) % FourSemaphores.size;
          FourSemaphores.n++;
          FourSemaphores.buffer[FourSemaphores.rear] = FourSemaphores.n;
        FourSemaphores.sender.Signal(); // allow new sender to go
      FourSemaphores.item.Signal(); // signal that a new item is ready
    };
    System.out.println ("Producer terminates.");
  }
}

