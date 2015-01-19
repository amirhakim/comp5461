package a2;

/*Issued: February 24, 2012                                   Due: March 9, 2012
EAS submission

Years ago, Java 1 and Java 2 had a 'yield ();' command that functioned like
a time-slice expiration.  It was a wonderful tool to program the Daemon into
our programs.  Alas, it no longer exists.

Since I believe concurrent programming without a Daemon is silly, I set about
emulating 'yield ();' in more recent versions of Java.

Alas, I had to use blocking synchronization, which makes my 'yield ();' VERY
dangerous to use inside atoms.  At least I think; I might be wrong.  Perhaps
some of you can experiment.

I have written a Java program in which 'getSpace ();' and 'release (r);' are
decidely NOT atomic.

If I don't activate the Daemon inside these two procedures, everything appears
to proceed normally.

Your job is to activate the Daemon first inside one procedure and then inside
the other (or perhaps inside both), and then to explain the hell that breaks
loose.

I use a destructive read in 'getSpace ();' because if a process removes a
resource from the stack, no other thread should be able to get that resource
until the first process returns it.

Here is the Java program with the Daemon very active in processes but totally
inactive inside our two procedures.

- ---*/

public class GSRL {
static int top            = 5;
static char stack[]       = new char[12];
static Semaphore resource = new Semaphore (5);  // resource counter
static Daemon    daemon   = new Daemon ();      // interrupt emulator
static Manager   manager  = new Manager ();     // resource manager

public static void main (String[] a) {
	
System.out.println ("Main thread starts.");

stack[0]  = 'u';
stack[1]  = 'a';
stack[2]  = 'b';
stack[3]  = 'g';
stack[4]  = 'd';
stack[5]  = 'e';
stack[6]  = 'z';
stack[7]  = 'n';
stack[8]  = 'o';
stack[9]  = 'i';
stack[10] = 'k';
stack[11] = 'l';


Cautious c[] = new Cautious[2];               // two cautious threads
for( int j=0; j<2; j++ )
c[j] = new Cautious (j+1);

Bold b[] = new Bold[2];                       // two bold threads
for( int j=0; j<2; j++ )
b[j] = new Bold (j+1);

for( int j=0; j<2; j++ )                      // cautious threads started
c[j].start ();

for( int j=0; j<2; j++ )                      // bold threads started
b[j].start ();

for( int j=0; j<2; j++ ) {                    // wait for all to finish
try { c[j].join (); b[j].join (); }
catch (InterruptedException e) { };
};

System.out.println ("System terminates normally.");
System.out.println ("The free resources now are:");
for( int j=top; j>0; j-- )
System.out.println (stack[j]);
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
};
value--;
};

public synchronized void Signal () {
++value;
notify ();
};

}

class Daemon {

private void SuspendMe () {
try { wait (); }
catch (InterruptedException e) { };
};

private void ResumeHim () {
notify ();
};

public synchronized void ContextSwitch () {
ResumeHim ();  SuspendMe ();
};

public synchronized void freeOne () {
notify ();
};

}

class Manager {

public char getSpace () {
GSRL.resource.Wait ();
char ch = GSRL.stack[GSRL.top];
GSRL.stack[GSRL.top] = 'x';
GSRL.daemon.ContextSwitch();
--GSRL.top;
return ch;
};

public void release (char ch) {
++GSRL.top;
GSRL.daemon.ContextSwitch();
GSRL.stack[GSRL.top] = ch;
GSRL.resource.Signal ();
};

}

class Cautious extends Thread {
private char ch1, ch2;
private int tid;
Cautious (int tid1) {
tid = tid1;
}

public void run () {
System.out.println ("Cautious thread " + tid + " begins execution.");
GSRL.daemon.ContextSwitch ();

char ch1 = GSRL.manager.getSpace ();      
System.out.println( "Cautious thread " + tid + " acquires resource " +
ch1 + ".");
GSRL.daemon.ContextSwitch ();
GSRL.manager.release (ch1);
System.out.println( "Cautious thread " + tid + " releases resource " +
ch1 + ".");
GSRL.daemon.ContextSwitch ();
char ch2 = GSRL.manager.getSpace ();      
System.out.println( "Cautious thread " + tid + " acquires resource " +
ch2 + ".");
GSRL.daemon.ContextSwitch ();
GSRL.manager.release (ch2);
System.out.println( "Cautious thread " + tid + " releases resource " +
ch2 + ".");
GSRL.daemon.ContextSwitch ();
GSRL.daemon.freeOne ();
System.out.println ("Cautious thread " + tid + " terminates.");
}
}

class Bold extends Thread {
private char ch1, ch2;
private int tid;
Bold (int tid1) {
tid = tid1;
}

public void run () {
System.out.println ("Bold     thread " + tid + " begins execution.");
GSRL.daemon.ContextSwitch ();

char ch1 = GSRL.manager.getSpace ();      
System.out.println ("Bold     thread " + tid + " acquires resource " +
ch1 + ".");
GSRL.daemon.ContextSwitch ();
char ch2 = GSRL.manager.getSpace ();
System.out.println ("Bold     thread " + tid + " acquires resource " +
ch2 + ".");
GSRL.daemon.ContextSwitch ();
GSRL.manager.release (ch2);      
System.out.println( "Bold     thread " + tid + " releases resource " +
ch2 + ".");
GSRL.daemon.ContextSwitch ();
GSRL.manager.release (ch1);
System.out.println( "Bold     thread " + tid + " releases resource " +
ch1 + ".");
GSRL.daemon.ContextSwitch ();
GSRL.daemon.freeOne ();
System.out.println ("Bold     thread " + tid + " terminates.");
}
}

