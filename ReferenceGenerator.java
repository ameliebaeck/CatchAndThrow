
import javax.swing.*; 
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import se.lth.control.*;

public class ReferenceGenerator extends Thread {
	private static final int MANUAL=0, SQUARE=1, OPTIMAL=2;
	private final int priority;
	private final double K_PHI=4.5, K_V=10.0;
	
	private double amplitude;
	private double period;
	private double max_ctrl;
	private double sign = -1.0;
	private double ref;
	private double uff = 0.0;
	private double phiff = 0.0;
	private double manual;
	private int mode = MANUAL;
	private boolean ampChanged, periodChanged, maxCtrlChanged;
	private boolean parChanged = false;
	
	private class RefGUI {
		private BoxPanel guiPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		private JPanel sliderPanel = new JPanel();
		private JPanel paramsLabelPanel = new JPanel();
		private JPanel paramsFieldPanel = new JPanel();
		private BoxPanel paramsPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		private BoxPanel parAndButtonPanel = new BoxPanel(BoxPanel.VERTICAL);
		private BoxPanel buttonsPanel = new BoxPanel(BoxPanel.VERTICAL);
		private JPanel rightPanel = new JPanel();
		
		private DoubleField paramsAmpField = new DoubleField(5,3);
		private DoubleField paramsPeriodField = new DoubleField(5,3);
		private DoubleField paramsMaxCtrlField = new DoubleField(5,3);
		private JButton paramsButton = new JButton("Apply");
		private JRadioButton manButton = new JRadioButton("Manual");
		private JRadioButton sqButton = new JRadioButton("Square");
		private JRadioButton toButton = new JRadioButton("Time-optimal");
		private JSlider slider = new JSlider(JSlider.VERTICAL,-10,10,0);
		
		public RefGUI(double start_amp, double start_period) {
		    double start_max_ctrl = 0.1;
			MainFrame.showLoading();
			paramsLabelPanel.setLayout(new GridLayout(0,1));
			paramsLabelPanel.add(new JLabel("Amp: "));
			paramsLabelPanel.add(new JLabel("Period: "));
			paramsLabelPanel.add(new JLabel("Max ctrl: "));
			
			paramsFieldPanel.setLayout(new GridLayout(0,1));
			paramsFieldPanel.add(paramsAmpField); 
			paramsFieldPanel.add(paramsPeriodField);   
			paramsFieldPanel.add(paramsMaxCtrlField);   
			paramsPanel.add(paramsLabelPanel);
			paramsPanel.addGlue();
			paramsPanel.add(paramsFieldPanel);
			paramsPanel.addFixed(10);
			paramsAmpField.setValue(start_amp);
			paramsAmpField.setMaximum(10.01);
			paramsAmpField.setMinimum(0.0);
			paramsPeriodField.setValue(start_period);
			paramsPeriodField.setMinimum(0.0);
			paramsMaxCtrlField.setMaximum(10.01);
			paramsMaxCtrlField.setMinimum(0.0);
			paramsMaxCtrlField.setValue(start_max_ctrl);
			
			parAndButtonPanel.setBorder(BorderFactory.createEtchedBorder());
			parAndButtonPanel.addFixed(10);
			parAndButtonPanel.add(paramsPanel);
			paramsPanel.addFixed(10);
			parAndButtonPanel.add(paramsButton);
			
			buttonsPanel.setBorder(BorderFactory.createEtchedBorder());
			buttonsPanel.add(manButton);
			buttonsPanel.addFixed(10);
			buttonsPanel.add(sqButton);
			buttonsPanel.addFixed(10);
			buttonsPanel.add(toButton);
			ButtonGroup group = new ButtonGroup();
			group.add(manButton);
			group.add(sqButton);
			group.add(toButton);
			manButton.setSelected(true);
			
			rightPanel.setLayout(new BorderLayout());
			rightPanel.add(parAndButtonPanel, BorderLayout.CENTER);
			rightPanel.add(buttonsPanel, BorderLayout.SOUTH);
			
			slider.setPaintTicks(true);
			slider.setMajorTickSpacing(5); 
			slider.setMinorTickSpacing(2); 
			slider.setLabelTable(slider.createStandardLabels(10)); 
			slider.setPaintLabels(true);
			sliderPanel.setBorder(BorderFactory.createEtchedBorder());
			sliderPanel.add(slider);
			
			guiPanel.add(sliderPanel);
			guiPanel.addGlue();
			guiPanel.add(rightPanel);
			
			paramsAmpField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ampChanged = true;
				}
			});
			paramsPeriodField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					periodChanged = true;
				}
			});  
			paramsMaxCtrlField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					maxCtrlChanged = true;
				}
			});  
			paramsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (ampChanged) {
						amplitude = paramsAmpField.getValue();
						ampChanged = false;
					}
					if (periodChanged) {
						period = paramsPeriodField.getValue();
						periodChanged = false;
						setParChanged();
					}
					if (maxCtrlChanged) {
						max_ctrl = paramsMaxCtrlField.getValue();
						maxCtrlChanged = false;
					}
				}
			});
			manButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setManMode();
				}
			});
			sqButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setSqMode();
				}
			});
			toButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setOptMode();
				}
			});
			slider.addChangeListener(new ChangeListener() { 
				public void stateChanged(ChangeEvent e) { 
					if (!slider.getValueIsAdjusting()) { 
						setManual(slider.getValue()); 
					} 
				} 
			}); 

			MainFrame.setPanel(guiPanel,"RefGen");
		}
	}
	
	public ReferenceGenerator(int refGenPriority) {
		priority = refGenPriority;
		amplitude = 5.0;
		period = 15.0; 
		manual = 0.0;
		ref = 0.0;
                max_ctrl = 0.1;
		new RefGUI(amplitude, period);
	}
	
	public synchronized void setRef(double newRef) {
		ref = newRef;
	}
	
	private synchronized void setParChanged() {
		parChanged = true;
	}
	
	private synchronized boolean getParChanged() {
		boolean val = parChanged;
		parChanged = false;
		return val;
	}
	
	
	private synchronized void setManual(double newManual) {
		manual = newManual;
	}
	
	private synchronized void setSqMode() {
		mode = SQUARE;
	}
	
	private synchronized void setManMode() {
		mode = MANUAL;
	}
	
	private synchronized void setOptMode() {
		mode = OPTIMAL;
	}
	
	public synchronized double getRef() 
	{
		//return (mode == MANUAL) ? manual : ref;
		return ref;
	}
	
	public synchronized double getUff() 
	{
		return (mode == OPTIMAL) ? uff : 0.0;
	}
	
	public synchronized double getPhiff() 
	{
		return (mode == OPTIMAL) ? phiff : 0.0;
	}
	
	public void run() {
		long h = 10;
		long timebase = System.currentTimeMillis();
		long timeleft = 0;
		long duration;
		
		double setpoint = 0.0;
		double new_setpoint;
		double u0 = 0.0, distance, now, t;
		double tf = 0.001 * (double) timebase;
		double ts = tf;
		double T = 0.0;
		double zf = 0.0, z0 = 0.0;
		
		setPriority(priority);
		
		try {
		    while (!isInterrupted()) {
				now = 0.001 * (double) timebase;
				/*synchronized (this) {
				    /*if (mode == MANUAL) {
					setpoint = manual;
					ref = manual;
				    } else {
					timeleft -= h;
					if (getParChanged()) {
						timeleft = 0;
					}
					
					if (timeleft <= 0) {
						timeleft += (long) (500.0 * period);
						sign = - sign;
					}
					new_setpoint = amplitude * sign;
					if (new_setpoint != setpoint) {
						if (mode == SQUARE) {
							setpoint = new_setpoint;
							ref = setpoint;
						} else if (mode == OPTIMAL) {
							ts = now;
							z0 = ref;
							zf = new_setpoint;
							distance = zf - z0;
							u0 = Math.signum(distance) * max_ctrl;
							T = Math.cbrt(Math.abs(distance) / (2.0 * K_PHI * K_V * max_ctrl));
							tf = ts + 4.0 * T;
							setpoint = new_setpoint;
						}
					}

					
					if (ref != setpoint) {
						t = now - ts;	
						if (t <= T) {
							uff = -u0;
							phiff = -K_PHI * u0 * t;
							ref = z0 + K_PHI * K_V * u0 * t*t*t/6;
						} else if (t <= 3.0*T) {
							uff = u0;
							phiff = K_PHI * u0 * (t - 2*T);
							ref = z0 - K_PHI * K_V * u0 * (t*t*t/6 - T*t*t + T*T*t - T*T*T/3);
						} else if (t <= 4.0*T) {
							uff = -u0;
							phiff = -K_PHI * u0 * (t - 4*T);
							ref = z0 + K_PHI * K_V * u0 * (t*t*t/6 - 2*T*t*t + 8*T*T*t - 26*T*T*T/3);
						} else {
							uff = 0.0;
							phiff = 0.0;
							ref = setpoint;
						}
					}
				    }
				}*/				
				timebase += h;
				duration = timebase - System.currentTimeMillis();
				if (duration > 0) {
					sleep(duration);			
				}
			}
		} catch (InterruptedException e) {
			// Requested to stop
		}
	}
}
