
// PID class to be written by you
public class PID {
	// Current PID parameters
	private PIDParameters p;
	
	//PID variables
	private double e; //Current control error
	private double eOld;
	private double v; //Desired control signal
	private double I; //Integrator state
	private double D; //Derivative state
	private double y; //Store current output
	private double yOld; //Store value output for the next time instant
	private double ad; //Precalculated constant D term
	private double bd; //Precalculated constant D term
	
	// Constructor
	public PID(String name){
		//Create reference to PIParameters and initialize parameters
		PIDParameters p = new PIDParameters();
		//Doubles
		p.K = -0.2;
		p.Ti = 0.0;
		p.Tr = 10.0;
		p.Td = 1.2;
		p.N = 10.0;
		p.Beta = 1.0;
		p.H = 0.02; //Sampling interval in seconds
		//Boolean
		p.integratorOn = false;
		
		//Clone parameters
		setParameters(p);
		
		//Inicialice atributs of PID class
		this.e = 0.0;
		this.v = 0.0;
		this.I = 0.0;
		this.D = 0.0;
	}
	
	// Calculates the control signal v.
	// Called from BallAndBeamRegul.
	public synchronized double calculateOutput(double y, double yref){
		this.y = y;
		this.e = yref - y;
		this.D = ad * D - bd * (y - yOld);
		this.v = p.K * (p.Beta * yref - y) + I + D;
		return this.v;
	}
	
	// Updates the controller state.
	// Should use tracking-based anti-windup
	// Called from BallAndBeamRegul.
	public synchronized void updateState(double u){
		if (p.integratorOn){
			this.I = I + (p.K * p.H / p.Ti) * e + (p.H / p.Tr) * (u - v);
		} else {
			I = 0.0;
		}
		
		//Store output for the next time instant
		yOld = y;
		eOld = e;
	}
	
	// Returns the sampling interval expressed as a long.
	// Explicit type casting needed.
	public synchronized long getHMillis(){
		return (long)(p.H*1000.0);
	}
	
	// Sets the PIDParameters.
	// Called from PIDGUI.
	// Must clone newParameters.
	public synchronized void setParameters(PIDParameters newParameters){
		p = (PIDParameters) newParameters.clone();
		
		if(!p.integratorOn){
			I = 0.0;
		}
		
		//Calculate ad and bd --> constants D term
		ad = p.Td / (p.Td + p.N * p.H);
		bd = p.K * p.Td * p.N / (p.Td + p.N * p.H);
	}
	
	 
	  // Sets the I-part and D-part of the controller to 0.
	  // For example needed when changing controller mode.
	  public synchronized void reset() {
		  I = 0.0;
		  D = 0.0;
	  }

	  // Returns the current PIDParameters.
	  public synchronized PIDParameters getParameters() {
		  return (PIDParameters)p.clone();
	  }
	
}//PID class