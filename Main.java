
import javax.swing.*;

public class Main { 
    public static void main(String[] argv) { 

        final int regulPriority = 8; 
        final int refGenPriority = 6; 
        final int plotterPriority = 7; 
        final int seqPriority = 5;

        ReferenceGenerator refgen = new ReferenceGenerator(refGenPriority); 
        Regul regul = new Regul(regulPriority); 
        Sequencing seq = new Sequencing(seqPriority);

	final  OpCom opcom = new OpCom(plotterPriority); // Must be declared final since it is used in an inner class

        regul.setOpCom(opcom); 
        regul.setRefGen(refgen);

        seq.setRefGen(refgen);
        seq.setRegul(regul);

        opcom.setRegul(regul); 

	Runnable initializeGUI = new Runnable(){
		public void run(){
		    opcom.initializeGUI();
		    opcom.start();
		}
	};
	try{
	    SwingUtilities.invokeAndWait(initializeGUI);
	}catch(Exception e){
	    return;
	}
        
        refgen.start(); 
        regul.start(); 
        seq.start()
    } 
} 
