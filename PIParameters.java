
public class PIParameters implements Cloneable {
	double K;
	double Ti;
	double Tr;
	double Beta;
	double H;
	boolean integratorOn;
	
	double Td;
	double N;
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException x) {
			return null;
		}
	}
}

