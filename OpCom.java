import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import se.lth.control.*;
import se.lth.control.plot.*;

/** Class that creates and maintains a GUI for the Ball and Beam process. 
Uses two PlotterPanels for the plotters */
public class OpCom {    

	public static final int OFF=0, BEAM=1, BALL=2;
	private static final double eps = 0.000001;

	private Regul regul;
	private PIParameters innerPar;
	private PIDParameters outerPar;
	private int priority;
	private int mode;

	// Declarartion of main frame.
	private JFrame frame;

	// Declarartion of panels.
	private BoxPanel guiPanel, plotterPanel, innerParPanel, outerParPanel, parPanel;
	private JPanel innerParLabelPanel, innerParFieldPanel, outerParLabelPanel, outerParFieldPanel, buttonPanel, somePanel, leftPanel;
	private PlotterPanel measPanel, ctrlPanel;

	// Declaration of components.
	private DoubleField innerParKField = new DoubleField(5,3);
	private DoubleField innerParTiField = new DoubleField(5,3);
	private DoubleField innerParTrField = new DoubleField(5,3);
	private DoubleField innerParTdField = new DoubleField(5,3);
	private DoubleField innerParNField = new DoubleField(5,3);
	private DoubleField innerParBetaField = new DoubleField(5,3);
	private DoubleField innerParHField = new DoubleField(5,3);
	private JButton innerApplyButton;

	private DoubleField outerParKField = new DoubleField(5,3);
	private DoubleField outerParTiField = new DoubleField(5,3);
	private DoubleField outerParTdField = new DoubleField(5,3);
	private DoubleField outerParTrField = new DoubleField(5,3);
	private DoubleField outerParNField = new DoubleField(5,3);
	private DoubleField outerParBetaField = new DoubleField(5,3);
	private DoubleField outerParHField = new DoubleField(5,3);
	private JButton outerApplyButton;

	private JRadioButton offModeButton;
	private JRadioButton beamModeButton;
	private JRadioButton ballModeButton;
	private JButton stopButton;

	private boolean hChanged = false;
	private boolean isInitialized = false;

	/** Constructor. */
	public OpCom(int plotterPriority) {
		priority = plotterPriority;
	}

	/** Starts the threads. */
	public void start() {
		measPanel.start();
		ctrlPanel.start();
	}

	/** Sets up a reference to Regul. Called by Main. */
	public void setRegul(Regul r) {
		regul = r;
	}

	/** Creates the GUI. Called from Main. */
	public void initializeGUI() {
		// Create main frame.
		frame = new JFrame("Ball and Beam GUI");

		// Create a panel for the two plotters.
		plotterPanel = new BoxPanel(BoxPanel.VERTICAL);
		// Create PlotterPanels.
		measPanel = new PlotterPanel(2, priority);
		measPanel.setYAxis(20, -10, 2, 2);
		measPanel.setXAxis(10, 5, 5);
		measPanel.setUpdateFreq(10);
		ctrlPanel = new PlotterPanel(1, priority);
		ctrlPanel.setYAxis(20, -10, 2, 2);
		ctrlPanel.setXAxis(10, 5, 5);
		ctrlPanel.setUpdateFreq(10);

		plotterPanel.add(measPanel);
		plotterPanel.addFixed(10);
		plotterPanel.add(ctrlPanel);

		// Get initial parameters from Regul
		innerPar = regul.getInnerParameters();
		outerPar = regul.getOuterParameters();

		// Create panels for the parameter fields and labels, add labels and fields 
		innerParPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		innerParLabelPanel = new JPanel();
		innerParLabelPanel.setLayout(new GridLayout(0,1));
		innerParLabelPanel.add(new JLabel("K: "));
		innerParLabelPanel.add(new JLabel("Ti: "));
		innerParLabelPanel.add(new JLabel("Tr: "));
		innerParLabelPanel.add(new JLabel("Td: "));
		innerParLabelPanel.add(new JLabel("N: "));
		innerParLabelPanel.add(new JLabel("Beta: "));
		innerParLabelPanel.add(new JLabel("h: "));
		innerParFieldPanel = new JPanel();
		innerParFieldPanel.setLayout(new GridLayout(0,1));
		innerParFieldPanel.add(innerParKField); 
		innerParFieldPanel.add(innerParTiField);
		innerParFieldPanel.add(innerParTrField);
		innerParFieldPanel.add(innerParTdField);
		innerParFieldPanel.add(innerParNField);
		innerParFieldPanel.add(innerParBetaField);
		innerParFieldPanel.add(innerParHField);

		// Set initial parameter values of the fields
		innerParKField.setValue(innerPar.K);
		innerParTiField.setValue(innerPar.Ti);
		innerParTiField.setMinimum(-eps);
		innerParTrField.setValue(innerPar.Tr);
		innerParTrField.setMinimum(-eps);
		innerParTdField.setValue(innerPar.Td);
		innerParTdField.setMinimum(-eps);
		innerParNField.setValue(innerPar.N);
		innerParNField.setMinimum(-eps);
		innerParBetaField.setValue(innerPar.Beta);
		innerParBetaField.setMinimum(-eps);
		innerParHField.setValue(innerPar.H);
		innerParHField.setMinimum(-eps);

		// Add action listeners to the fields
		innerParKField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				innerPar.K = innerParKField.getValue();
				innerApplyButton.setEnabled(true);
			}
		});
		innerParTiField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				innerPar.Ti = innerParTiField.getValue();
				if (innerPar.Ti < eps) {
					innerPar.integratorOn = false;
				}
				else {
					innerPar.integratorOn = true;
				}
				innerApplyButton.setEnabled(true);
			}
		});
		innerParTrField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				innerPar.Tr = innerParTrField.getValue();
				innerApplyButton.setEnabled(true);
			}
		});
		
		innerParTdField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				innerPar.Td = innerParTdField.getValue();
				innerApplyButton.setEnabled(true);
			}
		});
		innerParNField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				innerPar.N = innerParNField.getValue();
				innerApplyButton.setEnabled(true);
			}
		});
		
		innerParBetaField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				innerPar.Beta = innerParBetaField.getValue();
				innerApplyButton.setEnabled(true);
			}
		});
		innerParHField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				innerPar.H = innerParHField.getValue();
				outerPar.H = innerPar.H;
				outerParHField.setValue(innerPar.H);
				innerApplyButton.setEnabled(true);
				hChanged = true;
			}
		});

		// Add label and field panels to parameter panel
		innerParPanel.add(innerParLabelPanel);
		innerParPanel.addGlue();
		innerParPanel.add(innerParFieldPanel);
		innerParPanel.addFixed(10);

		// Create apply button and action listener.
		innerApplyButton = new JButton("Apply");
		innerApplyButton.setEnabled(false);
		innerApplyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setInnerParameters(innerPar);
				if (hChanged) {
					regul.setOuterParameters(outerPar);
				}	
				hChanged = false;
				innerApplyButton.setEnabled(false);
			}
		});

		// Create panel with border to hold apply button and parameter panel
		BoxPanel innerParButtonPanel = new BoxPanel(BoxPanel.VERTICAL);
		innerParButtonPanel.setBorder(BorderFactory.createTitledBorder("Inner Parameters"));
		innerParButtonPanel.addFixed(10);
		innerParButtonPanel.add(innerParPanel);
		innerParButtonPanel.addFixed(10);
		innerParButtonPanel.add(innerApplyButton);

		// The same as above for the outer parameters
		outerParPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		outerParLabelPanel = new JPanel();
		outerParLabelPanel.setLayout(new GridLayout(0,1));
		outerParLabelPanel.add(new JLabel("K: "));
		outerParLabelPanel.add(new JLabel("Ti: "));
		outerParLabelPanel.add(new JLabel("Td: "));
		outerParLabelPanel.add(new JLabel("N: "));
		outerParLabelPanel.add(new JLabel("Tr: "));
		outerParLabelPanel.add(new JLabel("Beta: "));
		outerParLabelPanel.add(new JLabel("h: "));

		outerParFieldPanel = new JPanel();
		outerParFieldPanel.setLayout(new GridLayout(0,1));
		outerParFieldPanel.add(outerParKField); 
		outerParFieldPanel.add(outerParTiField);
		outerParFieldPanel.add(outerParTdField);
		outerParFieldPanel.add(outerParNField);
		outerParFieldPanel.add(outerParTrField);
		outerParFieldPanel.add(outerParBetaField);
		outerParFieldPanel.add(outerParHField);
		outerParKField.setValue(outerPar.K);
		outerParTiField.setValue(outerPar.Ti);
		outerParTiField.setMinimum(-eps);
		outerParTdField.setValue(outerPar.Td);
		outerParTdField.setMinimum(-eps);
		outerParNField.setValue(outerPar.N);
		outerParTrField.setValue(outerPar.Tr);
		outerParBetaField.setValue(outerPar.Beta);
		outerParBetaField.setMinimum(-eps);
		outerParHField.setValue(outerPar.H);
		outerParHField.setMinimum(-eps);
		outerParKField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outerPar.K = outerParKField.getValue();
				outerApplyButton.setEnabled(true);
			}
		});
		outerParTiField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outerPar.Ti = outerParTiField.getValue();
				if (outerPar.Ti < eps) {
					outerPar.integratorOn = false;
				}
				else {
					outerPar.integratorOn = true;
				}
				outerApplyButton.setEnabled(true);
			}
		});
		outerParTdField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outerPar.Td = outerParTdField.getValue();
				outerApplyButton.setEnabled(true);
			}
		});
		outerParNField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outerPar.N = outerParNField.getValue();
				outerApplyButton.setEnabled(true);
			}
		});
		outerParTrField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outerPar.Tr = outerParTrField.getValue();
				outerApplyButton.setEnabled(true);
			}
		});
		outerParBetaField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outerPar.Beta = outerParBetaField.getValue();
				outerApplyButton.setEnabled(true);
			}
		});
		outerParHField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outerPar.H = outerParHField.getValue();
				innerPar.H = outerPar.H;
				innerParHField.setValue(outerPar.H);
				outerApplyButton.setEnabled(true);
				hChanged = true;
			}
		});

		outerParPanel.add(outerParLabelPanel);
		outerParPanel.addGlue();
		outerParPanel.add(outerParFieldPanel);
		outerParPanel.addFixed(10);

		outerApplyButton = new JButton("Apply");
		outerApplyButton.setEnabled(false);
		outerApplyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setOuterParameters(outerPar);
				if (hChanged) {
					regul.setInnerParameters(innerPar);
				}	
				hChanged = false;
				outerApplyButton.setEnabled(false);
			}
		});

		BoxPanel outerParButtonPanel = new BoxPanel(BoxPanel.VERTICAL);
		outerParButtonPanel.setBorder(BorderFactory.createTitledBorder("Outer Parameters"));
		outerParButtonPanel.addFixed(10);
		outerParButtonPanel.add(outerParPanel);
		outerParButtonPanel.addFixed(10);
		outerParButtonPanel.add(outerApplyButton);

		// Create panel for parameter fields, labels and apply buttons
		parPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		parPanel.add(innerParButtonPanel);
		parPanel.addGlue();
		parPanel.add(outerParButtonPanel);

		// Create panel for the radio buttons.
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.setBorder(BorderFactory.createEtchedBorder());
		// Create the buttons.
		offModeButton = new JRadioButton("OFF");
		beamModeButton = new JRadioButton("BEAM");
		ballModeButton = new JRadioButton("BALL");
		stopButton = new JButton("STOP");
		// Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(offModeButton);
		group.add(beamModeButton);
		group.add(ballModeButton);
		// Button action listeners.
		offModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setOFFMode();
			}
		});
		beamModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setBEAMMode();
			}
		});
		ballModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.setBALLMode();
			}
		});
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regul.shutDown();
				measPanel.stopThread();
				ctrlPanel.stopThread();
				System.exit(0);
			}
		});

		// Add buttons to button panel.
		buttonPanel.add(offModeButton, BorderLayout.NORTH);
		buttonPanel.add(beamModeButton, BorderLayout.CENTER);
		buttonPanel.add(ballModeButton, BorderLayout.SOUTH);

		// Panel for parameter panel and radio buttons
		somePanel = new JPanel();
		somePanel.setLayout(new BorderLayout());
		somePanel.add(parPanel, BorderLayout.CENTER);
		somePanel.add(buttonPanel, BorderLayout.SOUTH);

		// Select initial mode.
		mode = regul.getMode();
		switch (mode) {
		case OFF:
			offModeButton.setSelected(true);
			break;
		case BEAM:
			beamModeButton.setSelected(true);
			break;
		case BALL:
			ballModeButton.setSelected(true);
		}


		// Create panel holding everything but the plotters.
		leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		leftPanel.add(somePanel, BorderLayout.CENTER);
		leftPanel.add(stopButton, BorderLayout.SOUTH);

		// Create panel for the entire GUI.
		guiPanel = new BoxPanel(BoxPanel.HORIZONTAL);
		guiPanel.add(leftPanel);
		guiPanel.addGlue();
		guiPanel.add(plotterPanel);

		// WindowListener that exits the system if the main window is closed.
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				regul.shutDown();
				measPanel.stopThread();
				ctrlPanel.stopThread();
				System.exit(0);
			}
		});

		// Set guiPanel to be content pane of the frame.
		frame.getContentPane().add(guiPanel, BorderLayout.CENTER);

		// Pack the components of the window.
		frame.pack();

		// Position the main window at the screen center.
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension fd = frame.getSize();
		frame.setLocation((sd.width-fd.width)/2, (sd.height-fd.height)/2);

		// Make the window visible.
		frame.setVisible(true);
		
		isInitialized = true;
	}

	/** Called by Regul to plot a control signal data point. */
	public synchronized void putControlDataPoint(DoublePoint dp) {
		if (isInitialized) {
			ctrlPanel.putData(dp.x, dp.y);
		} else {
			DebugPrint("Note: GUI not yet initialized. Ignoring call to putControlDataPoint().");
		}
	}

	/** Called by Regul to plot a measurement data point. */
	public synchronized void putMeasurementDataPoint(PlotData pd) {
		if (isInitialized) {
			measPanel.putData(pd.x, pd.yref, pd.y);
		} else {
			DebugPrint("Note: GUI not yet initialized. Ignoring call to putMeasurementDataPoint().");
		}
	}
	
	private void DebugPrint(String message) {
		//System.out.println(message);
	}
}
