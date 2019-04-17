
import se.lth.control.DoublePoint;
import se.lth.control.realtime.AnalogIn;
import se.lth.control.realtime.AnalogOut;
import se.lth.control.realtime.DigitalIn;
import se.lth.control.realtime.DigitalOut;
import se.lth.control.realtime.IOChannelException;
import se.lth.control.realtime.Semaphore;


public class Regul extends Thread {
	public static final int OFF = 0;
	public static final int BEAM = 1;
	public static final int BALL = 2;
	public static final int BEAMPOS = 3;

	private PI inner = new PI("PI");
	private PID outer = new PID("PID");

	private AnalogIn analogInAngle;
	private AnalogIn analogInPosition;
	private AnalogOut analogOut;
	private DigitalIn digitalInPosition;

	private ReferenceGenerator referenceGenerator;
	private OpCom opcom;

	private int priority;
	private boolean WeShouldRun = true;
	private long starttime;
	private Semaphore mutex; // used for synchronization at shut-down

	private ModeMonitor modeMon;

	private final double min = -10.0;
	private final double max = 10.0;

	private double u;
	private double u2;
	private double y;
	private double y2;
	private double yref;
	private double phiff;
	private double uff;
	private double phiref;
	private double v;
	
	private boolean pitch;
	

	// Inner monitor class
	class ModeMonitor {
		private int mode;

		// Synchronized access methods
		public synchronized void setMode(int newMode) {
			mode = newMode;
			inner.reset();
			outer.reset();
		}

		public synchronized int getMode() {
			return mode;
		}
	}
	
	//Constructor
	public Regul(int pri) {
		priority = pri;
		mutex = new Semaphore(1);
		try {
			analogInAngle = new AnalogIn(0);
			analogInPosition = new AnalogIn(1);
			analogOut = new AnalogOut(0);
			digitalInPosition = new DigitalIn(0);
		} catch (IOChannelException e) {
			System.out.print("Error: IOChannelException: ");
			System.out.println(e.getMessage());
		}
		modeMon = new ModeMonitor();
	}

	public void setOpCom(OpCom opcom) {
		// Written by you
		this.opcom = opcom;
	}

	public void setRefGen(ReferenceGenerator referenceGenerator) {
		// Written by you
		this.referenceGenerator = referenceGenerator;
	}

	// Called in every sample in order to send plot data to OpCom
	private void sendDataToOpCom(double yref, double y, double u) {
		double x = (double) (System.currentTimeMillis() - starttime) / 1000.0;
		DoublePoint dp = new DoublePoint(x, u);
		PlotData pd = new PlotData(x, yref, y);
		opcom.putControlDataPoint(dp);
		opcom.putMeasurementDataPoint(pd);
	}

	public synchronized void setInnerParameters(PIParameters p) {
		// Written by you
		inner.setParameters(p);
		System.out.println("Parameters changed for the inner loop.");
	}

	public synchronized PIParameters getInnerParameters() {
		// Written by you
		return inner.getParameters();
	}

	public synchronized void setOuterParameters(PIDParameters p) {
		// Written by you
		outer.setParameters(p);
		System.out.println("Parameters changed for the outer loop.");
	}

	public synchronized PIDParameters getOuterParameters() {
		// Written by you
		return outer.getParameters();
	}

	public void setOFFMode() {
		// Written by you
		modeMon.setMode(0);
		System.out.println("Controller turned OFF.");
	}

	public void setBEAMMode() {
		// Written by you
		modeMon.setMode(1);
		System.out.println("Controller in BEAM mode.");
	}

	public void setBALLMode() {
		// Written by you
		modeMon.setMode(2);
		System.out.println("Controller in BALL mode.");
	}
	
	public void setBEAMPOSMode() {
		// Written by you
		modeMon.setMode(3);
		System.out.println("Controller in BEAMPOS mode.");
	}

	public int getMode() {
		// Written by you
		return modeMon.getMode();
	}

	// Called from OpCom when shutting down
	public synchronized void shutDown() {
		WeShouldRun = false;
		mutex.take();
		try {
			analogOut.set(0.0);
		} catch (IOChannelException x) {
		}
	}

	private double limit(double v, double min, double max) {
		if (v < min) {
			v = min;
		} else if (v > max) {
			v = max;
		}
		return v;
	}
	
	//Get angle. Called from Sequencing
	public double getAngle (){
		try {
			y2 = analogInAngle.get();
		} catch (Exception e) {
			System.out.println(e);
		}
		return y2;
	}
	
	//Get pitch value. Called from Sequencing
	public boolean getPitch (){
		try {
			pitch = digitalInPosition.get();
		} catch (Exception e) {
			System.out.println(e);
		}
		return pitch;
	}

	public void run() {
		long duration;
		long t = System.currentTimeMillis();
		starttime = t;
		
		setPriority(priority);
		mutex.take();
		int passage = 0;
		while (WeShouldRun) {
			switch (modeMon.getMode()) {
			case OFF: {
				// Code for the OFF mode. 
				// Written by you.
				// Should include resetting the controllers
				// Should include a call to sendDataToOpCom  
				y = 0.0;
				yref = 0.0;
				u = 0.0;
					
				u2 = 0.0;
				
				phiff = 0.0;
				uff = 0.0;
						
				try {
					analogOut.set(u2);
				} catch (Exception e) {
					System.out.println(e);
				}
						
				inner.reset();
					
				outer.reset();
				
				sendDataToOpCom(yref,y,u);
				
				try {
					y2 = analogInAngle.get();
					System.out.println(y2);
				} catch (Exception e) {
					System.out.println(e);
				}
				
				break;
			}
			case BEAM: {
				// Code for the BEAM mode
				// Written by you.
				// Should include a call to sendDataToOpCom
				
				/*try {
					y = analogInPosition.get();
				} catch (Exception e) {
					System.out.println(e);
				}*/
				
				yref = referenceGenerator.getRef();				
				
				try {
					y2 = analogInAngle.get();
				} catch (Exception e) {
					System.out.println(e);
				}
				
				//uff = referenceGenerator.getUff();
					
				synchronized(inner) {
					v = inner.calculateOutput(y2, yref);
					//u2 = limit(v+uff, min, max);
					u2 = limit(v, min, max);
					try {
						analogOut.set(u2);
					} catch (Exception e) {
						System.out.println(e);
					}
					
					//inner.updateState(u2-uff);
					inner.updateState(u2);
				}
				
				sendDataToOpCom(yref,y2,u2);
				
				break;
			}
			case BALL: {
				// Code for the BALL mode
				// Written by you.
				// Should include a call to sendDataToOpCom 
				
				try {
					y = analogInPosition.get();
				} catch (Exception e) {
					System.out.println(e);
				}
				yref = referenceGenerator.getRef();
				uff = referenceGenerator.getUff();
				phiff = referenceGenerator.getPhiff();
				
				synchronized (outer) {
					u = outer.calculateOutput(y, yref);
					phiref = limit(u+phiff, min, max);
					try {
						y2 = analogInAngle.get() + 0.23;
					} catch (Exception e) {
						System.out.println(e);
					}
					
					synchronized(inner) {
						v = inner.calculateOutput(y2, phiref);
						u2 = limit(v+uff, min, max);
						
						try {
							analogOut.set(u2);
						} catch (Exception e) {
							System.out.println(e);
						}
						
						inner.updateState(u2-uff);
					}
					
					if((v+uff) != u2) { //Inner loop saturated
						outer.updateState(y2-phiff);
					} else {
						outer.updateState(phiref-phiff);
					}
					
				}
				
				sendDataToOpCom(yref,y,u2);
				
				break;
			}
			case BEAMPOS:{
				
				/*try {
					y = analogInPosition.get();
				} catch (Exception e) {
					System.out.println(e);
				}*/
				
				try {
					y2 = analogInAngle.get();
					//System.out.println(y2);
				} catch (Exception e) {
					System.out.println(e);
				}
				
				try  {
					if(passage==0) {
					if(digitalInPosition.get()){
					
						yref = yref-0.004;
						//yref = yref+((yref-y2)/(Math.abs(yref-y2)))*0.005;
					}else{
						passage +=1;
						yref=y2-.17;
					}
				}
				} catch (Exception e) {
					System.out.println(e);
				}
				
				//System.out.println("yref: " + (yref));
				//System.out.println("y2: " + (y2));
				
				//uff = referenceGenerator.getUff();
					
				synchronized(inner) {
					v = inner.calculateOutput(y2, yref);
					//u2 = limit(v+uff, min, max);
					u2 = limit(v, min, max);
					try {
						analogOut.set(u2);
					} catch (Exception e) {
						System.out.println(e);
					}
					
					//inner.updateState(u2-uff);
					inner.updateState(u2);
				}
				
				sendDataToOpCom(yref,y2,u2);
				
				break;
			}
			default: {
				System.out.println("Error: Illegal mode.");
				break;
			}
			}

	// sleep
	t=t+inner.getHMillis();duration=t-System.currentTimeMillis();
	if(duration>0){
		try {
			sleep(duration);
		} catch (InterruptedException x) {
		}
	}
}mutex.give();}}
