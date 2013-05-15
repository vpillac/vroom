package vroom.common.modeling.vrprep.xmlFileDescriptor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import vroom.common.modeling.vrprepDescrip.Description;
import vroom.common.modeling.vrprepDescrip.Description.TypeDefinitions;
import vroom.common.modeling.vrprepDescrip.Description.TypeDefinitions.Link;
import vroom.common.modeling.vrprepDescrip.Description.TypeDefinitions.Node;
import vroom.common.modeling.vrprepDescrip.Description.TypeDefinitions.Other;
import vroom.common.modeling.vrprepDescrip.Description.TypeDefinitions.Request;
import vroom.common.modeling.vrprepDescrip.Description.TypeDefinitions.Vehicle;
import vroom.common.modeling.vrprepDescrip.ObjectFactory;
import vroom.common.modeling.vrprepDescrip.Type;

/**
 * Main frame to launch XML file descriptor application.
 * <br /> This application is used to describe the file format of problem
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a>
 *
 */
public class XMLFileDescriptor extends JFrame {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -5653478918163142916L;
	private JPanel	contentPane;

	private JScrollPane scrollPane;

	private ContentPanel contentPanel;

	private JButton generate;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					XMLFileDescriptor frame = new XMLFileDescriptor();
					frame.setVisible(true);
					Dimension d = new Dimension(630, 700);
					frame.setMinimumSize(d);
					frame.setSize(d);
					frame.setTitle("XML File Description Generator");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public XMLFileDescriptor() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);


		contentPanel = new ContentPanel(this);

		scrollPane = new JScrollPane(contentPanel);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(15);
		contentPane.add(scrollPane, BorderLayout.CENTER);


		generate = new JButton("Generate File");
		generate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				generateFile();
			}
		});
		contentPane.add(generate, BorderLayout.SOUTH);
	}

	/**
	 * Converts questionnaire to JAXB objects and asks where to save the new XML file
	 */
	private void generateFile(){
		ObjectFactory obj = new ObjectFactory();

		Description d = obj.createDescription();

		d.setInstances(contentPanel.getTextFieldInstances().getText());
		
		if(!contentPanel.getDescriptBox().getText().equals(""))
			d.setFurtherDetails("\n"+contentPanel.getDescriptBox().getText()+"\n");

		
		TypeDefinitions tD = null;

		String [] split;
		
		if(contentPanel.getChckbxNode().isSelected()){
			if(tD == null)
				tD = obj.createDescriptionTypeDefinitions();
			
			Node n = obj.createDescriptionTypeDefinitionsNode();
			vroom.common.modeling.vrprepDescrip.Type t; 
			for(int i = 0; i < contentPanel.getPanelNode().getListModel().getSize(); i++){
				split = contentPanel.getPanelNode().getListModel().get(i).toString().split(" : ");
				t = obj.createType();
				t.setId(BigInteger.valueOf(Integer.valueOf(split[0])));
				t.setValue(split[1]);
				n.getType().add(t);
			}
			tD.setNode(n);
		}
		if(contentPanel.getChckbxLink().isSelected()){
			if(tD == null)
				tD = obj.createDescriptionTypeDefinitions();
			
			Link l = obj.createDescriptionTypeDefinitionsLink();
			vroom.common.modeling.vrprepDescrip.Type t; 
			for(int i = 0; i < contentPanel.getPanelLink().getListModel().getSize(); i++){
				split = contentPanel.getPanelLink().getListModel().get(i).toString().split(" : ");
				t = obj.createType();
				t.setId(BigInteger.valueOf(Integer.valueOf(split[0])));
				t.setValue(split[1]);
				l.getType().add(t);
			}
			tD.setLink(l);
		}
		if(contentPanel.getChckbxVehicle().isSelected()){
			if(tD == null)
				tD = obj.createDescriptionTypeDefinitions();
			
			Vehicle v = obj.createDescriptionTypeDefinitionsVehicle();
			vroom.common.modeling.vrprepDescrip.Type t; 
			for(int i = 0; i < contentPanel.getPanelVehicle().getListModel().getSize(); i++){
				split = contentPanel.getPanelVehicle().getListModel().get(i).toString().split(" : ");
				t = obj.createType();
				t.setId(BigInteger.valueOf(Integer.valueOf(split[0])));
				t.setValue(split[1]);
				v.getType().add(t);
			}
			tD.setVehicle(v);
		}
		if(contentPanel.getChckbxRequest().isSelected()){
			if(tD == null)
				tD = obj.createDescriptionTypeDefinitions();
			
			Request r = obj.createDescriptionTypeDefinitionsRequest();
			vroom.common.modeling.vrprepDescrip.Type t; 
			for(int i = 0; i < contentPanel.getPanelRequest().getListModel().getSize(); i++){
				split = contentPanel.getPanelRequest().getListModel().get(i).toString().split(" : ");
				t = obj.createType();
				t.setId(BigInteger.valueOf(Integer.valueOf(split[0])));
				t.setValue(split[1]);
				r.getType().add(t);
			}
			tD.setRequest(r);
		}
		if(contentPanel.getChckbxOther().isSelected()){
			Other o;
			for(int i = 0; i < contentPanel.getOtherLs().size(); i++){
				if(tD == null)
					tD = obj.createDescriptionTypeDefinitions();
				
				o = obj.createDescriptionTypeDefinitionsOther();
				o.setName(contentPanel.getOtherLs().get(i).getText().replaceAll("<html><br />", ""));
				vroom.common.modeling.vrprepDescrip.Type t; 
				for(int j = 0; j < contentPanel.getOtherTs().get(i).getListModel().getSize(); j++){
					split = contentPanel.getOtherTs().get(i).getListModel().get(j).toString().split(" : ");
					t = obj.createType();
					t.setId(BigInteger.valueOf(Integer.valueOf(split[0])));
					t.setValue(split[1]);
					o.getType().add(t);
				}
				tD.getOther().add(o);
			}			
		}
		
		if(tD != null)
			d.setTypeDefinitions(tD);		
				
		String defaultFileName = "FileFormat - " +d.getInstances();

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setSelectedFile(new File(defaultFileName));
		int returnVal = fileChooser.showSaveDialog(contentPane);

		// file name and location given
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String fileName = fileChooser.getSelectedFile().getAbsolutePath().endsWith(".xml") ? fileChooser
					.getSelectedFile().getAbsolutePath() : fileChooser.getSelectedFile()
					.getAbsolutePath() + ".xml";
					File destinationFile = new File(fileName);
					
					// save xml to file
					writeFile(d, destinationFile, false);
					JOptionPane.showMessageDialog(contentPane, "XML Description generated successfully");
		}		
	}
	
	/**
	 * Marhshall jaxb objects to an XML File
	 * @param description
	 * @param destFile
	 * @param compress
	 */
	private void writeFile(Description description, File destFile, boolean compress){	
		try {
			
			JAXBContext context = JAXBContext.newInstance(Description.class.getPackage().getName());
			Marshaller marshaller = context.createMarshaller();
			
			// Nicelly format the output XML
	        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
	        //marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "http://vroom-project.net23.net/schemas/VRPRep.xsd");

	        // Write the instance
	        OutputStream os;
	        if (compress) {
	            String zipFile = destFile.getAbsolutePath().endsWith(".zip") ? destFile
	                    .getAbsolutePath() : destFile.getAbsolutePath() + ".zip";
				ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
	            ZipEntry ze = new ZipEntry(destFile.getName());
	            zos.putNextEntry(ze);

	            os = zos;
	        } else {
	            os = new FileOutputStream(destFile);
	        }

	        marshaller.marshal(description, os);

	        os.flush();
	        os.close();
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
