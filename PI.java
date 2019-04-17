// PI class to be written by you
public class PI {
	// Current PI parameters
	private PIParameters p;
	
	//PI variables
	private double e; //Current control error
	private double v; //Desired control signal
	private double I; //Integrator state
	
	private double eOld;
	private double D; //Derivative state
	private double y; //Store current output
	private double yOld; //Store value output for the next time instant
	private double ad; //Precalculated constant D term
	private double bd; //Precalculated constant D term
	
	// Constructor
	public PI(String name){
		//Create reference to PIParameters and initialize parameters
		PIParameters p = new PIParameters();
		//Doubles
		p.K = 3;
		p.Ti = 0.3;
		p.Tr = 10.0;
		p.Beta = 1.0;
		p.H = 0.02; //Sampling interval in seconds
		
		p.Td = 0.2;
		p.N = 10.0;
		
		//Boolean
		p.integratorOn = false;
		
		//Clone parameters
		setParameters(p);
		
		//Inicialice atributs of PI class
		this.e = 0.0;
		this.v = 0.0;
		this.I = 0.0;
		
		this.D = 0.0;
	}
	
	// Calculates the control signal v.
	// Called from BeamRegul.
	public synchronized double calculateOutput(double y, double yref){
		this.y = y;
		this.e = yref - y;
		this.D = ad * D - bd * (y - yOld);
		this.yOld = y;
		this.v = p.K * (p.Beta * yref - y) + I + D;
		return this.v;
	}
	
	
	// Updates the controller state.
	// Should use tracking-based anti-windup
	// Called from BeamRegul.
	public synchronized void updateState(double u){
		if (p.integratorOn){
			this.I = I + (p.K * p.H / p.Ti) * e + (p.H / p.Tr) * (u - v);
		} else {
			I = 0.0;
		}
	}
	
	// Returns the sampling interval expressed as a long.
	// Note: Explicit type casting needed
	public synchronized long getHMillis(){
		return (long)(p.H*1000.0);
	}
	
	// Sets the PIParameters.
	// Called from PIGUI.
	// Must clone newParameters.
	public synchronized void setParameters(PIParameters newParameters){
		p = (PIParameters) newParameters.clone();
		
		if(!p.integratorOn){
			I = 0.0;
		}
		
		//Calculate ad and bd --> constants D term
		ad = p.Td / (p.Td + p.N * p.H);
		bd = p.K * p.Td * p.N / (p.Td + p.N * p.H);
		
	}
	

	  // Sets the I-part of the controller to 0.
	  // For example needed when changing controller mode.
	  public synchronized void reset() {
		  I = 0.0;
		  D = 0.0;
	  }
	  
	  // Returns the current PIParameters.
	  public synchronized PIParameters getParameters() {
		  return (PIParameters)p.clone();
	  }
	
}//PI class
