package vroom.common.modeling.vrprep.xmlPrototyper;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * Contains all questions relevant to the problem description
 * 
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a> 
 */
public class InfoQuestions extends JPanel {

    private static final long serialVersionUID = 6500998495552084736L;
    private JTextField        textField_InstanceName;
    private JTextField        textField_ProblemName;
    private JTextField        textField_BiblioRef;
    private JTextField        textField_ContriName;
    private JTextField        textField_ContiEMail;

    /**
     * Create the panel.
     */
    public InfoQuestions() {
    	
    	// panel layout
    	setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
    	
    	
    	
    	   	
    	
    	// questions panel
    	JPanel questions = new JPanel();

        FormLayout fl_this = new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("left:max(60dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, });
        fl_this.setColumnGroups(new int[][] { new int[] { 12, 10, 8, 6, 4, 2 } });
        questions.setLayout(fl_this);
        
        JLabel lbl1 = new JLabel("<html>Network Structure :<br /><br />");
        lbl1.setFont(new Font(Font.SANS_SERIF, Font.BOLD + Font.ITALIC, 18));
        // lbl1.setVisible(false);
        questions.add(lbl1, "2, 6, left, default");
        

        JLabel lblProblemName = new JLabel("Instance Name :");
        questions.add(lblProblemName, "2, 8, left, default");

        textField_InstanceName = new JTextField("Autofill");
        //textField_InstanceName = new JTextField();
        questions.add(textField_InstanceName, "4, 8, 3, 1, fill, default");
        textField_InstanceName.setColumns(10);

        JLabel lblProblemAuthor = new JLabel("Problem Name :");
        questions.add(lblProblemAuthor, "2, 10, left, default");

        textField_ProblemName = new JTextField("Autofill");
        //textField_ProblemName = new JTextField();
        questions.add(textField_ProblemName, "4, 10, 3, 1, fill, default");
        textField_ProblemName.setColumns(10);
        
        JLabel lblBiblioRef = new JLabel("Bibiographical Reference :");
        questions.add(lblBiblioRef, "2, 12, left, default");

        textField_BiblioRef = new JTextField("Autofill");
        //textField_BiblioRef = new JTextField();
        questions.add(textField_BiblioRef, "4, 12, 3, 1, fill, default");
        textField_BiblioRef.setColumns(10);

        JLabel lblYourDetails = new JLabel("<html><br />Your details :");
        questions.add(lblYourDetails, "2, 14");

        JLabel lblName = new JLabel("Name :");
        questions.add(lblName, "2, 16, left, default");

        textField_ContriName = new JTextField("Autofill");
        //textField_ContriName = new JTextField();
        textField_ContriName.setColumns(10);
        questions.add(textField_ContriName, "4, 16, 3, 1, fill, default");

        JLabel lblEmail = new JLabel("E-Mail :");
        questions.add(lblEmail, "2, 18, left, default");

        textField_ContiEMail = new JTextField("autofill@valid.com");
        //textField_ContiEMail = new JTextField();
        textField_ContiEMail.setColumns(10);
        questions.add(textField_ContiEMail, "4, 18, 3, 1, fill, default");        

        JLabel lblmustBeForm = new JLabel("(must be form *****@***.***)");
        questions.add(lblmustBeForm, "8, 18");
        
        add(questions, "2, 4");
    }

    /**
     * Getter for the variable textField_InstanceName
     * 
     * @return the textField_ProbName
     */
    public String getTextField_InstanceName() {
        return textField_InstanceName.getText();
    }

    /**
     * Getter for the variable textField_ProblemName
     * 
     * @return the textField_ProblemAuthor
     */
    public String getTextField_ProblemName() {
        return textField_ProblemName.getText();
    }
    
    /**
     * Getter for the variable textField_BiblioRef
     * 
     * @return the textField_BiblioRef
     */
    public String getTextField_BiblioRef() {
        return textField_BiblioRef.getText();
    }

    /**
     * Getter for the variable textField_ContriName
     * 
     * @return the textField_ContriName
     */
    public String getTextField_ContriName() {
        return textField_ContriName.getText();
    }

    /**
     * Getter for the variable textField_ContiEMail
     * 
     * @return the textField_ContiEMail
     */
    public String getTextField_ContiEMail() {
        return textField_ContiEMail.getText();
    }

}
