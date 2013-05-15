package vroom.common.modeling.vrprep.xmlFileDescriptor;

import javax.swing.JPanel;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.border.EtchedBorder;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * Represents the content pane of the XML File descriptor
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a>
 *
 */
public class ContentPanel extends JPanel {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -2480614412666861085L;

	private JLabel lblInstances;
	private JLabel lblWhichTypesNeed;
	private JLabel description;

	private JTextField textFieldInstances;
	private JTextArea descriptBox;


	private JCheckBox chckbxNode;
	private JCheckBox chckbxLink;
	private JCheckBox chckbxVehicle;
	private JCheckBox chckbxRequest;
	private JCheckBox chckbxOther;

	private TypePanel panelNode;
	private TypePanel panelLink;
	private TypePanel panelVehicle;
	private TypePanel panelRequest;	

	private JLabel lblOtherTypes;
	private JPanel otherTypes;
	private JButton btnGenerate;

	private JLabel lblNodePanel;
	private JLabel lblLinkPanel;
	private JLabel lblVehiclePanel;
	private JLabel lblRequestPanel;
	private JLabel lblOtherPanel;	

	private int rowIndex = 16;

	private ArrayList<JLabel> otherLs = null;
	private ArrayList<TypePanel> otherTs = null;
	private int anyRowIndex = 26;


	private ContentPanel thisPanel;



	/**
	 * Create the panel.
	 */
	public ContentPanel(final JFrame parent) {

		thisPanel = this;
		FormLayout formLayout = new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(140dlu;default)"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,},
				new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,		
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,		
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,});
		formLayout.setColumnGroups(new int[][]{new int[]{2, 4}});
		setLayout(formLayout);


		JLabel l = new JLabel("<html>The following program allows you to create a XML File which shall be used to describe" +
				" the file structure of your vehicle routing problems. <br />" +
				"Firstly you must write for which of your instances this description shall be valid.<br />" +
				"Secondly you must describe any particularities that your structure has. This could be :<br />" +
				"&nbsp;&nbsp;&nbsp;- Any elements you added to the original file structure, <br />" +
				"&nbsp;&nbsp;&nbsp;- Any constraints that were not defined or could be complicated to understand, <br />" +
				"&nbsp;&nbsp;&nbsp;- Simply a description or how your instances should be approached.<br >" +
				"Finally you must define the values of all the types in your instances. For example node type 1 is a depot and " +
				"node type 2 is a customer. Default attributes have been offered to you for this but if there are any other" +
				" types you need to define, you have the possibility to add them by clicking other. <br /><br />");
		l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		add(l, "2, 2, 3, 1");

		lblInstances = new JLabel("Please enter the name of the instances this description file will describe : ");
		add(lblInstances, "2, 4, 3, 1");

		textFieldInstances = new JTextField();
		add(textFieldInstances, "2, 6, 3, 1");
		textFieldInstances.setColumns(10);


		description = new JLabel("Please write a description of structure explaining any particularites if necessary :");
		add(description, "2, 8, 3, 1");

		descriptBox = new JTextArea(10,1);
		descriptBox.setLineWrap(true);
		descriptBox.setWrapStyleWord(true);
		JScrollPane descripScroll = new JScrollPane(descriptBox);
		add(descripScroll, "2, 10, 3, 1");

		lblWhichTypesNeed = new JLabel("Which types need to be described :");
		add(lblWhichTypesNeed, "2, 12, 3, 1");

		chckbxNode = new JCheckBox("Node");
		chckbxNode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(chckbxNode.isSelected()){
					panelNode.setVisible(true);
					lblNodePanel.setVisible(true);
				}else{
					panelNode.setVisible(false);
					lblNodePanel.setVisible(false);
				}
			}
		});
		add(chckbxNode, "2, 13");

		chckbxLink = new JCheckBox("Link");
		chckbxLink.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(chckbxLink.isSelected()){
					panelLink.setVisible(true);
					lblLinkPanel.setVisible(true);
				}else{
					panelLink.setVisible(false);
					lblLinkPanel.setVisible(false);
				}
			}
		});
		add(chckbxLink, "4, 13");

		chckbxVehicle = new JCheckBox("Vehicle");
		chckbxVehicle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(chckbxVehicle.isSelected()){
					panelVehicle.setVisible(true);
					lblVehiclePanel.setVisible(true);
				}else{
					panelVehicle.setVisible(false);
					lblVehiclePanel.setVisible(false);
				}
			}
		});
		add(chckbxVehicle, "2, 14");

		chckbxRequest = new JCheckBox("Request");
		chckbxRequest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(chckbxRequest.isSelected()){
					panelRequest.setVisible(true);
					lblRequestPanel.setVisible(true);
				}else{
					panelRequest.setVisible(false);
					lblRequestPanel.setVisible(false);
				}
			}
		});
		add(chckbxRequest, "4, 14");

		chckbxOther = new JCheckBox("Other");
		chckbxOther.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(chckbxOther.isSelected()){
					otherTypes.setVisible(true);
					lblOtherTypes.setVisible(true);
					
					if(otherLs != null && otherTs != null){
						for(int i = 0; i < otherLs.size(); i++){
							otherLs.get(i).setVisible(true);
							otherTs.get(i).setVisible(true);
						}
					}
				}else{
					otherTypes.setVisible(false);
					lblOtherTypes.setVisible(false);

					if(otherLs != null && otherTs != null){
						for(int i = 0; i < otherLs.size(); i++){
							otherLs.get(i).setVisible(false);
							otherTs.get(i).setVisible(false);
						}
					}
				}
			}
		});
		add(chckbxOther, "2, 15");

		/*
		 * 
		 * other types panel to add new types
		 * 
		 * 
		 * 
		 * 
		 */
		lblOtherTypes = new JLabel("<html><br />Please enter the types you wish to create :");
		lblOtherTypes.setVisible(false);
		add(lblOtherTypes, "2, "+rowIndex+++", 3, 1");
		otherTypes = new JPanel();

		FormLayout fl_this = new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, },
				new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("30dlu"),
				FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, });
		fl_this.setColumnGroups(new int[][] { new int[] { 2, 6 } });
		fl_this.setRowGroups(new int[][] { new int[] { 2, 3 } });
		otherTypes.setLayout(fl_this);

		final JTextField textField = new JTextField();
		otherTypes.add(textField, "2, 2, 1, 2, fill, center");
		textField.setColumns(10);
		textField.setToolTipText("Capital and Minuscule letters only with no spaces");

		final DefaultListModel listModel = new DefaultListModel();
		final JList list = new JList(listModel);
		JScrollPane listScroller = new JScrollPane(list);
		otherTypes.add(listScroller, "6, 2, 1, 2, fill, fill");

		JButton btnAdd = new JButton("> ADD >");
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listModel.addElement(textField.getText());
				textField.setText("");
			}
		});
		otherTypes.add(btnAdd, "4, 2");

		JButton btnRemove = new JButton("< REMOVE <");
		btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(list.getSelectedIndex() >= 0)
					listModel.remove(list.getSelectedIndex());
			}
		});
		otherTypes.add(btnRemove, "4, 3");

		btnGenerate = new JButton("Generate Extra Types");
		btnGenerate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				anyRowIndex = 26;
				// remove previous componenents
				if(otherLs != null && otherTs != null){
					for(int i = 0; i < otherLs.size(); i++){
						otherLs.get(i).setVisible(false);
						otherTs.get(i).setVisible(false);
						thisPanel.remove(otherLs.get(i));
						thisPanel.remove(otherTs.get(i));
					}
					otherLs = null;
					otherTs = null;
				}

				if(otherTs == null)
					otherTs = new ArrayList<TypePanel>();
				if(otherLs == null)
					otherLs = new ArrayList<JLabel>();
				// generate new components
				for(int j = 0; j < listModel.getSize(); j++){
					otherLs.add(new JLabel("<html><br />"+listModel.get(j).toString()));
					otherTs.add(new TypePanel());
					otherTs.get(otherTs.size()-1).setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
				}
				// add new components to panel
				for(int k = 0; k < otherLs.size(); k++){
					thisPanel.add(otherLs.get(k), "2, "+anyRowIndex+++" 3, 1");
					thisPanel.add(otherTs.get(k), "2, "+anyRowIndex+++" 3, 1");
					otherLs.get(k).setVisible(true);
					otherTs.get(k).setVisible(true);
				}	
				// refresh panel
				thisPanel.validate();
				parent.validate();
			}
		});
		otherTypes.add(btnGenerate, "4, 4");
		/*
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 */

		otherTypes.setVisible(false);
		otherTypes.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		add(otherTypes, "2, "+rowIndex+++", 3, 1");

		lblNodePanel = new JLabel("<html><br />Node :");
		lblNodePanel.setVisible(false);
		add(lblNodePanel, "2, "+rowIndex+++", 3, 1");
		panelNode = new TypePanel();
		panelNode.setVisible(false);
		panelNode.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		add(panelNode, "2, "+rowIndex+++", 3, 1");

		lblLinkPanel = new JLabel("<html><br />Link :");
		lblLinkPanel.setVisible(false);
		add(lblLinkPanel, "2, "+rowIndex+++", 3, 1");
		panelLink = new TypePanel();
		panelLink.setVisible(false);
		panelLink.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		add(panelLink, "2, "+rowIndex+++", 3, 1");

		lblVehiclePanel = new JLabel("<html><br />Vehicle :");
		lblVehiclePanel.setVisible(false);
		add(lblVehiclePanel, "2, "+rowIndex+++", 3, 1");
		panelVehicle = new TypePanel();
		panelVehicle.setVisible(false);
		panelVehicle.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		add(panelVehicle, "2, "+rowIndex+++", 3, 1");

		lblRequestPanel = new JLabel("<html><br />Request :");
		lblRequestPanel.setVisible(false);
		add(lblRequestPanel, "2, "+rowIndex+++" 3, 1");
		panelRequest = new TypePanel();
		panelRequest.setVisible(false);
		panelRequest.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		add(panelRequest, "2, "+rowIndex+++", 3, 1");

		lblOtherPanel = new JLabel("<html><br />Other :");
		lblOtherPanel.setVisible(false);
		add(lblOtherPanel, "2, "+rowIndex+++", 3, 1");



	}



	/**
	 * Getter for the variable descriptBox
	 * @return the descriptBox
	 */
	public JTextArea getDescriptBox() {
		return descriptBox;
	}



	/**
	 * Getter for the variable chckbxNode
	 * @return the chckbxNode
	 */
	public JCheckBox getChckbxNode() {
		return chckbxNode;
	}



	/**
	 * Getter for the variable chckbxLink
	 * @return the chckbxLink
	 */
	public JCheckBox getChckbxLink() {
		return chckbxLink;
	}



	/**
	 * Getter for the variable chckbxVehicle
	 * @return the chckbxVehicle
	 */
	public JCheckBox getChckbxVehicle() {
		return chckbxVehicle;
	}



	/**
	 * Getter for the variable chckbxRequest
	 * @return the chckbxRequest
	 */
	public JCheckBox getChckbxRequest() {
		return chckbxRequest;
	}



	/**
	 * Getter for the variable chckbxOther
	 * @return the chckbxOther
	 */
	public JCheckBox getChckbxOther() {
		return chckbxOther;
	}



	/**
	 * Getter for the variable panelNode
	 * @return the panelNode
	 */
	public TypePanel getPanelNode() {
		return panelNode;
	}



	/**
	 * Getter for the variable panelLink
	 * @return the panelLink
	 */
	public TypePanel getPanelLink() {
		return panelLink;
	}



	/**
	 * Getter for the variable panelVehicle
	 * @return the panelVehicle
	 */
	public TypePanel getPanelVehicle() {
		return panelVehicle;
	}



	/**
	 * Getter for the variable panelRequest
	 * @return the panelRequest
	 */
	public TypePanel getPanelRequest() {
		return panelRequest;
	}



	/**
	 * Getter for the variable otherLs
	 * @return the otherLs
	 */
	public ArrayList<JLabel> getOtherLs() {
		return otherLs;
	}



	/**
	 * Getter for the variable otherTs
	 * @return the otherTs
	 */
	public ArrayList<TypePanel> getOtherTs() {
		return otherTs;
	}



	/**
	 * Getter for the variable textFieldInstances
	 * @return the textFieldInstances
	 */
	public JTextField getTextFieldInstances() {
		return textFieldInstances;
	}





}
