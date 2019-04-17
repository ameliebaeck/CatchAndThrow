//import Regul.ModeMonitor;
import se.lth.control.realtime.AnalogOut;
import se.lth.control.realtime.IOChannelException;
import se.lth.control.realtime.Semaphore;
import se.lth.control.realtime.AnalogIn;
import se.lth.control.realtime.DigitalIn;
import se.lth.control.realtime.DigitalOut;



public class Sequencing extends Thread{


	public static final int SMALL = 0;
	public static final int MEDIUM = 1;
	public static final int BIG = 2;



	private Regul regul;
	private ReferenceGenerator refgen;
	private ModeMonitor modeMon;

	private int priority;
	private Semaphore mutex; // used for synchronization at shut-down

	private AnalogIn analogInAngle;
	private AnalogIn analogInPosition;
	private AnalogOut analogOut;

	private DigitalIn pitch;
	private DigitalOut fire;

	//Inner monitor class
	class ModeMonitor{
		private int mode;

		// Synchronized access methods
		public synchronized void setMode(int newMode) {
			mode = newMode;
		}

		public synchronized int getMode() {
			return mode;
		}
	}

	public Sequencing(int pri){
		priority = pri;
		mutex = new Semaphore(1);
		try {
			fire = new DigitalOut(0);
			pitch = new DigitalIn(0);
			analogInAngle = new AnalogIn(0);
			analogInPosition = new AnalogIn(1);
			analogOut = new AnalogOut(0);
		} catch (IOChannelException e) {
			System.out.print("Error: IOChannelException: ");
			System.out.println(e.getMessage());
		}
		modeMon = new ModeMonitor();
	}

	public void setSMALLMode() {
		modeMon.setMode(0);
		System.out.println("SMALL ball detected");
	}

	public void setMEDIUMMode() {
		modeMon.setMode(1);
		System.out.println("MEDIUM ball detected");
	}

	public void setBIGMode() {
		modeMon.setMode(2);
		System.out.println("BIG ball detected");
	}

	public void run() {
		setPriority(priority);
		mutex.take();
		while(true) {

			//initial state
			regul.setBEAMMode();
			// set ref angle to 0
			// while(){}
			refgen.setRef(0.0);
			try {
				while(0.0 != analogInAngle.get()) {
				}
			} catch (Exception e) {
				System.out.println(e);
			}
			//beam-position state
			// decrease angle until pitch signal false
			try {
				double tempref=0.0;
				while(pitch.get()) {
					tempref = tempref - 0.1;
					refgen.setRef(tempref);
				}
			} catch (Exception e) {
				System.out.println(e);

			}

			// push ball state

			try {
				fire.set(true);
				while(-10.0 != analogInPosition.get()) {
				}
			} catch (Exception e) {
				System.out.println(e);
			}

			// ball mode state
			//set ballMode state true
			regul.setBALLMode();

			//ball position state
			//go to the measure position
			refgen.setRef(5.0); // WE TRY WITH 5.0
			try {
				// this range might be needed to fix
				while(4.9 > analogInPosition.get() && analogInPosition.get() > 5.1) {
				}
			} catch (Exception e) {
				System.out.println(e);
			}

			// measure size state
			// measure the control signal to determine the size of the ball
			double controlsignal = 0.0;
			for(int i=0; i<10; i++) {
			controlsignal = controlsignal+regul.getControlSignal();
			}
			controlsignal = controlsignal/10.0;
			if(controlsignal < 1) {
				setSMALLMode();


			} else if (1 < controlsignal && controlsignal < 2) {
				setMEDIUMMode();
			} else {
				setBIGMode();
			}

			switch (modeMon.getMode()) {
			case SMALL: {
				regul.setBEAMMode();
				//small state
				//increase angle
				refgren.setRef(5);
				try {
					while(-7.0 < analogInPosition.get()) {
					}
				} catch (Exception e) {
					System.out.println(e);
				}
				//decrease angle to throw in the small basket
				refgren.setRef(-5);
				try {
					while(-9.5 < analogInPosition.get()) {
					}
				} catch (Exception e) {
					System.out.println(e);
				}
				break;
			}
			case MEDIUM: {
				regul.setBEAMMode();
				refgen.setRef(5);
				try {
					while(0 < regul.getAnalogInPosition()) {
					}
				} catch (Exception e) {
					System.out.println(e);
				}
				refgen.setRef(-5);
				try {
					while(5 > regul.getAnalogInPosition()) {
					}
				} catch (Exception e) {
					System.out.println(e);
				}
				refgen.setRef(5);

			}
			case BIG: {
				//Drop ball on the floor
				regul.setBEAMMode();
				refgen.setRef(-5);
				try {
					while(7 > analogInPosition.get() || -4.5 > analogInAngle.get()) {
					}
				} catch (Exception e) {
					System.out.println(e);
				}
				break;
			}
			}
		}
	}

}
