//import Regul.ModeMonitor;
import se.lth.control.realtime.AnalogOut;
import se.lth.control.realtime.IOChannelException;
import se.lth.control.realtime.Semaphore;
import se.lth.control.realtime.AnalogIn;
import se.lth.control.realtime.DigitalIn;
import se.lth.control.realtime.DigitalOut;
import java.util.concurrent.TimeUnit; // for sleep function



public class Sequencing extends Thread{
	
	
	public static final int SMALL = 0;
	public static final int MEDIUM = 1;
	public static final int BIG = 2;
	
	private OpCom opcom;	
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
	
	//Constructor
	public Sequencing(int pri){
		priority = pri;
		mutex = new Semaphore(1);
		try {
			fire = new DigitalOut(0);
			pitch = new DigitalIn(0);
			//analogInAngle = new AnalogIn(0); //Read from Regul
			analogInPosition = new AnalogIn(1);
			analogOut = new AnalogOut(0);
		} catch (IOChannelException e) {
			System.out.print("Error: IOChannelException: ");
			System.out.println(e.getMessage());
		}
		modeMon = new ModeMonitor();
	}
	
	public void setRegul(Regul regul) {
		this.regul = regul;
	}
	
	public void setOpCom(OpCom opcom) {
		this.opcom = opcom;
	}
	
	public void setRefGen(ReferenceGenerator refgen) {
		this.refgen = refgen;
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
		boolean passageSeq = false;
		while(true) {
		
			//initial state
			// set ref angle to 0
			// while(){}
			//if(!passageSeq) {
				regul.setBEAMMode();
				refgen.setRef(0.0);
				
				try {
					while((0.0 > regul.getAngle()) || (regul.getAngle() > 0.12)) {
						System.out.println("In Init State");
						System.out.println(regul.getAngle());
					}		
				} catch (Exception e) {
					System.out.println(e);
				}
				
				//JUST FOR DEBUGGING
				/*refgen.setRef(5.0);
				try {
					while((4.65 > regul.getAngle()) || (regul.getAngle() > 5.2)) {
						System.out.println("In Init State");
						System.out.println(regul.getAngle());
					}		
				} catch (Exception e) {
					System.out.println(e);
				}*/
				
				System.out.println("1 - Initial state finished"); //Debugging
				System.out.println("--------------------------");
				
				
				//beam-position state
				// decrease angle until pitch signal false
				//hold final position --> IMPLEMENT THIS
				regul.setBEAMPOSMode();
				try {
					//double tempref=0.0;
					while(regul.getPitch()) {
						//tempref = tempref - 0.004;
						//refgen.setRef(tempref);
					}
				} catch (Exception e) {
					System.out.println(e);
					System.out.println("Exception pitch.get");
				}

				refgen.setRef(regul.getAngle());
				regul.setBEAMMode();
				
				System.out.println("2 - Beam-position state finished"); //Debugging
				System.out.println("--------------------------");
				
			
				// push ball state
				try {
					fire.set(true);
					while(-10.0 == regul.getBallPos()) {
						System.out.println("No ball on the beam"); //Debugging
					}
				} catch (Exception e) {
					System.out.println(e);
				}
				
				System.out.println("3 - Push ball state finished"); //Debugging
				System.out.println("--------------------------");

				// ball mode state
				//set ballMode state true
				refgen.setRef(0.0); // WE TRY WITH 5.0
				regul.setBALLMode();

				System.out.println("4 - Ball mode state finished"); //Debugging
				System.out.println("--------------------------");
				
				//ball position state
				//go to the measure position
				try {
					// this range might be needed to fix
					boolean resettime = true;
                    double catchtime = 0.0;
                    double intervaltime = 0.0;
                    boolean release = false;
                    while(!release) {
                        System.out.println("Inside release while");
                        
                        while(-1 > regul.getBallPos() && regul.getBallPos() > 1) {
                            System.out.println("Ball on the beam at postion " + regul.getAngle()); //Debugging
                            System.out.println("Outside the interval -1, 1");
                            }
                    
                        while(-1 < regul.getBallPos() && regul.getBallPos() < 1){
                                if (resettime){
                                    catchtime = System.currentTimeMillis();
                                    intervaltime = catchtime;
                                    resettime = false;
                                } else {
                                    intervaltime = System.currentTimeMillis();
                                }
                                
                                if((intervaltime - catchtime) > 3000){
                                    release = true;
                                    break;
                                }
                                
                                System.out.println("Within the interval -1, 1");
                        }
                        
                        resettime = true;
                        
                    }
				} catch (Exception e) {
					System.out.println(e);
				}

				System.out.println("5 - Ball position state finished"); //Debugging
				System.out.println("--------------------------");

			//	passageSeq=true;
			//}
			
			// measure size state
			// measure the control signal to determine the size of the ball
			/*double controlsignal = 0.0;
			for(int i=0; i<10; i++) {
			//controlsignal = controlsignal+regul.getControlSignal(); //IMPLEMENT in REGUL
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
			case SMALL: {*/
				refgen.setRef(7);
				try{
					TimeUnit.SECONDS.sleep(6);
				} catch (Exception e){}
				regul.setBEAMMode();
				//small state
				//increase angle
				refgen.setRef(5);
				try {
					while(3 < regul.getBallPos()) {
					}
				} catch (Exception e) {
					System.out.println(e);
				}
				//decrease angle to throw in the small basket
				refgen.setRef(-7);
				try {
					TimeUnit.SECONDS.sleep(4);
				} catch (Exception e) {
					System.out.println(e);
				}
				/*break;
				
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
			case BIG: { */
				//Drop ball on the floor
				regul.setBEAMMode();
				refgen.setRef(5);
				try {
					while(-3 < regul.getAngle() && 4.5 > regul.getAngle()) {
					}
					TimeUnit.SECONDS.sleep(2);
				} catch (Exception e) {
					System.out.println(e);
				}
			/*	break;
			}
			}*/
			
		} //While loop
	}//Run method
	
}
