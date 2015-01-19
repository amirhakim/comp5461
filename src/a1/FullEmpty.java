package a1;

public class FullEmpty {
static int n           = 0;
// ProducerTwo and Consumer Tow use 2 semaphores to achive syn
static Semaphore full  = new Semaphore (1);  // full bit
static Semaphore empty = new Semaphore (0);  // empty bit
// Producer and Consumer achive the same thing using one semaphore
static Semaphore one = new Semaphore (0);  //  bit


public static void main (String[] a) {
  System.out.println ("Main thread starts.");
  Consumer c = new Consumer ();
  Producer p = new Producer ();

  c.start ();
  
  p.start ();
  try {
    c.join ();
    p.join ();
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
    //value--;
  }

  public synchronized void WaitForFull () {
	    while( value <= 0 ) {
	      try { wait (); }
	      catch (InterruptedException e) { };
	    }
	    //value--;
	  }
  
  public synchronized void WaitForEmpty () {
	    while( value >= 1 ) {
	      try { wait (); }
	      catch (InterruptedException e) { };
	    }
	    //value--;
	  }


  
  public synchronized void Signal () {
    ++value;
    notify ();
  }
  
  public synchronized void SignalFull () {
	    ++value;
	    notify ();
	  }
  public synchronized void SignalEmpty () {
	    --value;
	    notify ();
	  }

}

class Consumer extends Thread {
  public void run () {
    System.out.println ("Consumer starts.");
    for( int j=1; j<=20; j++ ) {
   	 FullEmpty.one.WaitForFull();
      System.out.println( "Consumer: n = " + FullEmpty.n + ".");
      FullEmpty.one.SignalEmpty();
    };
    System.out.println ("Consumer terminates.");
  }
}

class Producer extends Thread {
  public void run () {
    System.out.println ("Producer starts.");
    for( int j=1; j<=20; j++ ) {
        FullEmpty.one.WaitForEmpty();
      System.out.println( "Producer: incrementing n.");
        FullEmpty.n++;
        FullEmpty.one.SignalFull();
    };
    
    System.out.println ("Producer terminates.");
  }
  
  
  
}

class ConsumerTwoSem extends Thread {
	  public void run () {
	    System.out.println ("Consumer starts.");
	    for( int j=1; j<=20; j++ ) {
	      FullEmpty.full.Wait(); // wait for the full bit to be set
	      System.out.println( "Consumer: n = " + FullEmpty.n + ".");
	      FullEmpty.empty.Signal(); // set the empty bit so the producer can do it's work
	    };
	    System.out.println ("Consumer terminates.");
	  }
	}

	class ProducerTwoSem extends Thread {
	  public void run () {
	    System.out.println ("Producer starts.");
	    for( int j=1; j<=20; j++ ) {
	      FullEmpty.empty.Wait(); // wait for the empty bit
	      System.out.println( "Producer: incrementing n.");
	      FullEmpty.n++;
	      FullEmpty.full.Signal(); // set the full bit so the consumer can do it's work
	    };
	    System.out.println ("Producer terminates.");
	  }
	}

