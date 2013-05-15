package vroom.common.modeling.vrprep.xmlPrototyper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * JPanel used for any elements. Allows user to add elements to a {@link JList} via a {@link JTextField}
 * 
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a> 
 */
public class AnyList extends JPanel {

    private static final long              serialVersionUID = -3152701763128812381L;

    // buttons
    private final JButton                  btnAdd;
    private final JButton                  btnRemove;

    // fields
    private final JTextField               textField;
    private final JList            	       list;

    // list elements
    private final DefaultListModel 		   listModel;
    private final JScrollPane              listScroller;

    // this
    private final AnyList                  thisList;

    /**
     * Create the panel.
     */
    public AnyList() {
    	// Standard tooltip delay
    	ToolTipManager.sharedInstance().setInitialDelay(10);
    	
    	
        thisList = this;
        FormLayout fl_this = new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, },
                new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("30dlu"),
                        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, });
        fl_this.setColumnGroups(new int[][] { new int[] { 2, 6 } });
        fl_this.setRowGroups(new int[][] { new int[] { 2, 3 } });
        setLayout(fl_this);

        textField = new JTextField();
        add(textField, "2, 2, 1, 2, fill, center");
        textField.setColumns(10);
        textField.setToolTipText("Capital and Minuscule letters only with no spaces");

        btnAdd = new JButton("> ADD >");
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Pattern p = Pattern.compile("^[a-zA-Z]+\\s*$");
                Matcher m = p.matcher(textField.getText());
                if (m.matches()) {
                    listModel.addElement(textField.getText());
                    textField.setText("");
                } else {
                    JOptionPane.showMessageDialog(thisList,
                            "Element can only contain lower or upper case letters with no spaces");
                }

            }
        });
        add(btnAdd, "4, 2");

        listModel = new DefaultListModel();
        list = new JList(listModel);
        listScroller = new JScrollPane(list);
        add(listScroller, "6, 2, 1, 2, fill, fill");

        btnRemove = new JButton("< REMOVE <");
        btnRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if(list.getSelectedIndex() >= 0)
            		listModel.remove(list.getSelectedIndex());
            }
        });
        add(btnRemove, "4, 3");

    }

    /**
     * Getter for the variable listModel
     * 
     * @return the listModel
     */
    public DefaultListModel getListModel() {
        return listModel;
    }

}
