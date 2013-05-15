package vroom.common.modeling.vrprep.xmlPrototyper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;



/**
 * Main Frame to launch XML Prototyper
 * 
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a> 
 */
public class XMLPrototyper extends JFrame {

	private static final long  serialVersionUID = 6167772640663968459L;
	// panels
	private JPanel             contentPane;
	private JPanel			   bottomPanel;
	private JPanel 			   description;

	// buttons
	private JButton nextStep;

	// tabbed pane
	private TabbedPane         tabbedPane;

	// tab content panes
	private InfoQuestions      infoQuestions;
	private NetworkQuestions   networkQuestions;
	private FleetQuestions     fleetQuestions;
	private RequestQuestions   requestQuestions;
	private JPanel             panelGenerate;

	// file chooser
	private final JFileChooser fileChooser      = new JFileChooser();

	/** * Boolean value to check network questions panel has been visited */
	private boolean            s1HasBeenVisited = false;
	/** * Boolean value to check fleet questions panel has been visited */
	private boolean            s2HasBeenVisited = false;
	/** * Boolean value to check request questions panel has been visited */
	private boolean            s3HasBeenVisited = false;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					// default fonts
					UIManager.put("Label.font", new Font(Font.SANS_SERIF, Font.BOLD, 12));
					UIManager.put("RadioButton.font", new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					UIManager.put("CheckBox.font", new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					UIManager.put("Button.font", new Font(Font.SANS_SERIF, Font.BOLD, 12));
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					// frame and frame properties
					XMLPrototyper frame = new XMLPrototyper();
					Dimension d = new Dimension(860, 700);
					frame.setMinimumSize(d);
					frame.setSize(d);
					frame.setVisible(true);
					frame.setLocation(10, 10);
					frame.setTitle("XML Prototype Generator");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public XMLPrototyper() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		// panels
		infoQuestions = new InfoQuestions();
		networkQuestions = new NetworkQuestions();
		fleetQuestions = new FleetQuestions();
		requestQuestions = new RequestQuestions();
		panelGenerate = new JPanel();
		
		
		// description panel
    	description = new JPanel();
    	JLabel descriptionText = new JLabel();
    	descriptionText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

    	String descrip = "<html>" +
    			"<span style=\"font-family:SANS_SERIF; font-style:italic; font-size:14px\"><strong>Introduction :</strong></span><br />" +
    			"<br /><strong>Why use the prototype generator? </strong><br />" +
    			"This prototype generator is a questionnaire to help you generate an XML file prototype based on our structure adapted to your routing problem." +
    			"<br /><br /><strong>Questions layout : </strong><br />" +
    			"The questions are layed out in the following order :<br />" +
    			"Step 1 : Fill in the details of the problem, and your details as you are the person developping the instances.<br >" +
    			"Step 2 : Describe the network structure (node, link and network characteristics).<br />" +
    			"Step 3 : Describe the fleet (fleet characteristics, vehicle characteristics, work load profile and compartment capacities).<br />" +
    			"Step 4 : Describe the requests (request charcateristics, service times, time windows, demands and dependencies)." +
    			"<br /><br /><strong>Further Information : </strong><br />" +
    			"If there are any characteristics or constraints that were not representable, then at the end of the node, link, vehicle and request questions, <br />" +
    			"there is the possibility to create elements to adapt the file structure to your problem.";
    	descriptionText.setText(descrip);	
    	description.add(descriptionText); 
    	
    	contentPane.add(description, BorderLayout.CENTER);
    	

		// tabbed pane
		tabbedPane = new TabbedPane(SwingConstants.TOP);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		// tabs
		tabbedPane.add(new TabPanel(infoQuestions), 0);
		tabbedPane.setTabComponentAt(0, new TabLabel("Problem Description"));

		tabbedPane.add(new TabPanel(networkQuestions), 1);
		tabbedPane.setTabComponentAt(1, new TabLabel("Network Description"));

		tabbedPane.add(new TabPanel(fleetQuestions), 2);
		tabbedPane.setTabComponentAt(2, new TabLabel("Fleet Description"));

		tabbedPane.add(new TabPanel(requestQuestions), 3);
		tabbedPane.setTabComponentAt(3, new TabLabel("Request Description"));

		tabbedPane.add(new TabPanel(panelGenerate), 4);
		tabbedPane.setTabComponentAt(4, new TabLabel("Generate XML Prototype"));

		// bottom panel
		bottomPanel = new JPanel();
		contentPane.add(bottomPanel, BorderLayout.SOUTH);

		nextStep = new JButton("Begin");
		nextStep.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(nextStep.getText().equals("Begin")){
					nextStep.setText("Next");
					contentPane.remove(description);
					contentPane.add(tabbedPane, BorderLayout.CENTER);
				}else if (checkStepValidity()) {
					if(nextStep.getText().equals("Next")){
						tabbedPane.setSelectedIndex(tabbedPane.getSelectedIndex()+1);
						if(tabbedPane.getSelectedIndex() == 4)
							nextStep.setText("Generate XML Prototype");	
					}else if(nextStep.getText().equals("Generate XML Prototype")){
						generateFile();
					}
				}
			}
		});
		
		Dimension nextStepSize = new Dimension(200,30);
		nextStep.setMaximumSize(nextStepSize);
		nextStep.setPreferredSize(nextStepSize);
		nextStep.setAlignmentY(CENTER_ALIGNMENT);
		bottomPanel.add(nextStep);
		
		
	}

	/**
	 * Checks if the tab has been properly filled in (no missing fields)
	 * 
	 * @return true if tab is properly complete
	 */
	private boolean checkStepValidity() {
		// info panel
		if (tabbedPane.getSelectedIndex() == 0) {
			boolean result = true;           
			// empty fields
			if (infoQuestions.getTextField_InstanceName().equals("")
					|| infoQuestions.getTextField_ProblemName().equals("")
					|| infoQuestions.getTextField_BiblioRef().equals("")
					|| infoQuestions.getTextField_ContriName().equals("")
					|| infoQuestions.getTextField_ContiEMail().equals("")) {
				JOptionPane.showMessageDialog(contentPane,
						"One or more of the information fields are not filled in", "Warning",
						JOptionPane.WARNING_MESSAGE);
				result = false;
			}

			// email form
			if(result){
				Pattern email = Pattern
						.compile("^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)+$");
				Matcher emailCheck = email.matcher(infoQuestions.getTextField_ContiEMail());

				if (!emailCheck.matches()) {
					JOptionPane.showMessageDialog(contentPane, "Email is not valid", "Warning",
							JOptionPane.WARNING_MESSAGE);
					result = false;
				}
			}
			return result;
		}

		// network panel
		if (tabbedPane.getSelectedIndex() == 1) {
			boolean result = true;
			// network structure
			if (!networkQuestions.getQ2_linkL().isSelected()
					&& !networkQuestions.getQ2_nodeL().isSelected()
					&& !networkQuestions.getQ2_both().isSelected()) {
				JOptionPane.showMessageDialog(contentPane, "Please select a network structure",
						"Warning", JOptionPane.WARNING_MESSAGE);
				result = false;
			}
			// node location type
			if (result && networkQuestions.getHasLocation()) {
				boolean[] lTypes = networkQuestions.getLocationTypes();
				if (!lTypes[0] && !lTypes[1] && !lTypes[2]) {
					JOptionPane.showMessageDialog(contentPane, "No location type was chosen",
							"Warning", JOptionPane.WARNING_MESSAGE);
					result = false;
				}
			}
			// extra node info list empty
			if(result && (networkQuestions.getQ2_nodeL().isSelected() || networkQuestions.getQ2_both().isSelected()) && 
					networkQuestions.getQ105_yes().isSelected() && networkQuestions.getAnyMoreAnyN().getListModel().isEmpty()){
				JOptionPane.showMessageDialog(contentPane, "No extra node constraints were given",
						"Warning", JOptionPane.WARNING_MESSAGE);
				result = false;
			}
			// link time type
			if (result && networkQuestions.getHasTime()) {
				boolean[] tTypes = networkQuestions.getTimeTypes();
				if (!tTypes[0] && !tTypes[1] && !tTypes[2] && !tTypes[3]) {
					JOptionPane.showMessageDialog(contentPane, "No time type was chosen",
							"Warning", JOptionPane.WARNING_MESSAGE);
					result = false;
				}
			}
			// extra link info list empty
			if(result && (networkQuestions.getQ2_linkL().isSelected() || networkQuestions.getQ2_both().isSelected()) && 
					networkQuestions.getQ106_yes().isSelected() && networkQuestions.getAnyMoreAnyL().getListModel().isEmpty()){
				JOptionPane.showMessageDialog(contentPane, "No extra link constraints were given",
						"Warning", JOptionPane.WARNING_MESSAGE);
				result = false;
			}
			// location any field empty
			if (result && networkQuestions.getQ4_other().isSelected()
					&& networkQuestions.getLocationAny().getListModel().isEmpty()) {
				JOptionPane.showMessageDialog(contentPane,
						"You have not specified new node location type", "Warning",
						JOptionPane.WARNING_MESSAGE);
				result = false;
			}
			// link any field empty
			if (result && networkQuestions.getQ12_other().isSelected()
					&& networkQuestions.getTimeAny().getListModel().isEmpty()) {
				JOptionPane.showMessageDialog(contentPane,
						"You have not specified new link time type", "Warning",
						JOptionPane.WARNING_MESSAGE);
				result = false;
			}
			// distance type field empty
			if (result && networkQuestions.getQ14_yes().isSelected()
					&& networkQuestions.getTextFieldDistMesure().getText().equals("")) {
				JOptionPane.showMessageDialog(contentPane,
						"You have not specified a distance measurement", "Warning",
						JOptionPane.WARNING_MESSAGE);
				result = false;
			}
			// rounding rule field empty
			if (result && networkQuestions.getQ15_yes().isSelected()
					&& networkQuestions.getTextFieldRoundingRule().getText().equals("")) {
				JOptionPane.showMessageDialog(contentPane,
						"You have not specified a rounding rule", "Warning",
						JOptionPane.WARNING_MESSAGE);
				result = false;
			}
			
			// descriptor any
			if (result && networkQuestions.getQ16_yes().isSelected()
					&& networkQuestions.getDescriptorAny().getListModel().isEmpty()) {
				JOptionPane.showMessageDialog(contentPane,
						"No extra network description elements were given", "Warning",
						JOptionPane.WARNING_MESSAGE);
				result = false;
			}
			
			// if no links then no requests attached to links
			if(networkQuestions.getQ2_nodeL().isSelected()){
				requestQuestions.getQ1_link().setEnabled(false);
				requestQuestions.getQ1_link().setText(
						requestQuestions.getQ1_link().getText()+
						" (Network links must be defined in order to attach requests to links)");
				requestQuestions.getQ1_both().setEnabled(false);
			}else{
				requestQuestions.getQ1_link().setEnabled(true);
				requestQuestions.getQ1_link().setText("Links (Arcs)");
				requestQuestions.getQ1_both().setEnabled(true);
			}
			
			
			
			// Network Description has been properly visited at least once now
			if (result) {
				s1HasBeenVisited = true;
			}
			return result;
		}

		// fleet panel
		if (tabbedPane.getSelectedIndex() == 2) {
			boolean result = true;
			// speed type
			if (fleetQuestions.getQ7_yes().isSelected()) {
				boolean[] sTypes = fleetQuestions.getSpeedTypes();
				if (!sTypes[0] && !sTypes[1]) {
					JOptionPane.showMessageDialog(contentPane, "No speed type was chosen",
							"Warning", JOptionPane.WARNING_MESSAGE);
					result = false;
				}
			}
			// capacity type
			if (result && fleetQuestions.getQ28_yes().isSelected()) {
				boolean[] cTypes = fleetQuestions.getCapacityTypes();
				if (!cTypes[0] && !cTypes[1]) {
					JOptionPane.showMessageDialog(contentPane, "No capacity type was chosen",
							"Warning", JOptionPane.WARNING_MESSAGE);
					result = false;
				}
			}
			// workload profile any field empty
			if (result && fleetQuestions.getQ101_yes().isSelected()
					&& fleetQuestions.getwLPAny().getListModel().isEmpty()) {
				JOptionPane.showMessageDialog(contentPane,
						"You have not specified new workload profile parameter", "Warning",
						JOptionPane.WARNING_MESSAGE);
				result = false;
			}
			// extra vehicle info list empty
			if(result && fleetQuestions.getQ105_yes().isSelected() && 
					fleetQuestions.getAnyMoreAny().getListModel().isEmpty()){
				JOptionPane.showMessageDialog(contentPane, "No extra vehicle constraints were given",
						"Warning", JOptionPane.WARNING_MESSAGE);
				result = false;
			}

			// Fleet Description has been properly visited at least once now
			if (result) {
				s2HasBeenVisited = true;
			}
			return result;
		}

		// request panel
		if (tabbedPane.getSelectedIndex() == 3) {
			boolean result = true;
			// capacity type
			if (requestQuestions.getQ9_yes().isSelected()) {
				boolean[] dTypes = requestQuestions.getDemandTypes();
				if (!dTypes[0] && !dTypes[1] && !dTypes[2] && !dTypes[3]) {
					JOptionPane.showMessageDialog(contentPane,
							"No quantity type to pick up or deliver was chosen", "Warning",
							JOptionPane.WARNING_MESSAGE);
					result = false;
				}
			}
			// time type
			if (result && requestQuestions.getQ15_yes().isSelected()) {
				boolean[] tTypes = requestQuestions.getTimeTypes();
				if (!tTypes[0] && !tTypes[1] && !tTypes[2] && !tTypes[3]) {
					JOptionPane.showMessageDialog(contentPane, "No time type was chosen",
							"Warning", JOptionPane.WARNING_MESSAGE);
					result = false;
				}
			}
			// dependency types
			if (result && requestQuestions.getQ17_yes().isSelected()) {
				if (!requestQuestions.getQ18_prec().isSelected()
						&& !requestQuestions.getQ18_succ().isSelected()) {
					JOptionPane.showMessageDialog(contentPane,
							"You have not specified how to declare the dependendies", "Warning",
							JOptionPane.WARNING_MESSAGE);
					result = false;
				}
			}
			// service time any field empty
			if (result && requestQuestions.getQ16_other().isSelected()
					&& requestQuestions.getTimeAny().getListModel().isEmpty()) {
				JOptionPane.showMessageDialog(contentPane,
						"You have not specified new service time type", "Warning",
						JOptionPane.WARNING_MESSAGE);
				result = false;
			}
			// demand any field empty
			if (result && requestQuestions.getQ11_other().isSelected()
					&& requestQuestions.getDemandAny().getListModel().isEmpty()) {
				JOptionPane.showMessageDialog(contentPane,
						"You have not specified new demand quantity type", "Warning",
						JOptionPane.WARNING_MESSAGE);
				result = false;
			}
			// extra vehicle info list empty
			if(result && requestQuestions.getQ105_yes().isSelected() && 
					requestQuestions.getAnyMoreAny().getListModel().isEmpty()){
				JOptionPane.showMessageDialog(contentPane, "No extra request constraints were given",
						"Warning", JOptionPane.WARNING_MESSAGE);
				result = false;
			}

			// Request Description has been properly visited at least once now
			if (result) {
				s3HasBeenVisited = true;
			}
			return result;
		}

		// generate panel
		if (tabbedPane.getSelectedIndex() == 4) {
			return true;
		}

		return false;
	}

	/**
	 * Finalizes JAXB objects, asks where to save the XML file and calls the marshaller to marshall the file 
	 */
	private void generateFile() {
		boolean compressFile = true;
		String defaultFileName = infoQuestions.getTextField_InstanceName() + "-XMLPrototype";

		fileChooser.setSelectedFile(new File(defaultFileName));
		int returnVal = fileChooser.showSaveDialog(contentPane);

		// file name and location given
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String fileName = fileChooser.getSelectedFile().getAbsolutePath().endsWith(".xml") ? fileChooser
					.getSelectedFile().getAbsolutePath() : fileChooser.getSelectedFile()
					.getAbsolutePath() + ".xml";
					File destinationFile = new File(fileName);
					// save xml to file
					XMLBinder.getInstance().generateXMLPrototyper(infoQuestions, requestQuestions,
							networkQuestions, fleetQuestions, compressFile, destinationFile);
					JOptionPane.showMessageDialog(contentPane, "XML Prototype generation successful");
		}

	}

	/**
	 * Class that extends JTabbedPane to override setSelectedIndex method allowing check to be performed
	 * 
	 * @author Maxim Hoskins
	 */
	class TabbedPane extends JTabbedPane {
		private static final long serialVersionUID = -3141178636679036067L;

		public TabbedPane() {
			super();
		}

		public TabbedPane(int tabPlacement) {
			super(tabPlacement);
		}

		@Override
		public void setSelectedIndex(int newIndex) {
			int oldIndex = model.getSelectedIndex();
			if (oldIndex != -1) {
				// check the panels are correctly filled in
				if (checkStepValidity()) {
					// check panels are completed in correct order
					if (newIndex == 0 || newIndex == 1) {
						super.setSelectedIndex(newIndex);
					}
					if (newIndex == 2) {
						if (!s1HasBeenVisited) {
							JOptionPane.showMessageDialog(contentPane,
									"Please complete Network Description", "Warning",
									JOptionPane.WARNING_MESSAGE);
						} else {
							super.setSelectedIndex(newIndex);
						}
					}
					if (newIndex == 3) {
						if (!s1HasBeenVisited) {
							JOptionPane.showMessageDialog(contentPane,
									"Please complete Network Description", "Warning",
									JOptionPane.WARNING_MESSAGE);
						} else {
							if (!s2HasBeenVisited) {
								JOptionPane.showMessageDialog(contentPane,
										"Please complete Fleet Description", "Warning",
										JOptionPane.WARNING_MESSAGE);
							} else {
								nextStep.setText("Generate XML Prototype");
								super.setSelectedIndex(newIndex);
							}
						}
					}
					if (newIndex == 4) {
						if (!s1HasBeenVisited) {
							JOptionPane.showMessageDialog(contentPane,
									"Please complete Network Description", "Warning",
									JOptionPane.WARNING_MESSAGE);
						} else {
							if (!s2HasBeenVisited) {
								JOptionPane.showMessageDialog(contentPane,
										"Please complete Fleet Description", "Warning",
										JOptionPane.WARNING_MESSAGE);
							} else {
								if (!s3HasBeenVisited) {
									JOptionPane.showMessageDialog(contentPane,
											"Please complete Request Description", "Warning",
											JOptionPane.WARNING_MESSAGE);
								} else {
									generateFile();
								}
							}
						}
					}
				}
			} else {
				super.setSelectedIndex(newIndex);
			}
		}
	}
}
