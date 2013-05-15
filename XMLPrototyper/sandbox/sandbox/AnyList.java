package sandbox;

import javax.swing.JPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
/**
 * JPanel test taht will be used for any elements. Allows user to add elements to a jlist via a jtextfield
 * @author Maxim Hoskins (https://plus.google.com/115909706630698463631)
 *
 */
public class AnyList extends JPanel {


	private static final long serialVersionUID = -3152701763128812381L;
		
	// buttons
	private JButton btnAdd;
	private JButton btnRemove;
	
	// fields
	private JTextField textField;
	private JList<String> list;

	// list elements
	private DefaultListModel<String> listModel;
	private JScrollPane listScroller;
	
	/**
	 * Create the panel.
	 */
	public AnyList() {
		FormLayout fl_this = new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("30dlu"),
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,});
		fl_this.setColumnGroups(new int[][]{new int[]{2, 6}});
		fl_this.setRowGroups(new int[][]{new int[]{2, 3}});
		this.setLayout(fl_this);
		
		textField = new JTextField();
		add(textField, "2, 2, 1, 2, fill, center");
		textField.setColumns(10);
		
		btnAdd = new JButton("> ADD >");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!textField.getText().equals(""))
					listModel.addElement(textField.getText());
				textField.setText("");
			}
		});
		add(btnAdd, "4, 2");
		
		listModel = new DefaultListModel<String>();		
		list = new JList<String>(listModel);
		listScroller = new JScrollPane(list);
		add(listScroller, "6, 2, 1, 2, fill, fill");
		
		btnRemove = new JButton("< REMOVE <");
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				listModel.remove(list.getSelectedIndex());
			}
		});
		add(btnRemove, "4, 3");

	}
	
	/**
	 * Getter for the variable list
	 * @return the list
	 */
	public JList<String> getList() {
		return list;
	}

}
