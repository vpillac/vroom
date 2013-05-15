package vroom.common.modeling.vrprep.xmlFileDescriptor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * JPanel used for every "type" in XML FileDescriptor
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a>
 *
 */
public class TypePanel extends JPanel {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -1214947445479161484L;

	private JLabel lblType;
	private JLabel lblDescrip;
	private JButton btnAdd;
	private JButton btnRemove;
	private JTextField textFieldType;
	private JTextField textFieldDescrip;
	private JList list;
	private DefaultListModel listModel;
	private JScrollPane listScroller;

	private TypePanel thisList;

	/**
	 * Create the panel.
	 */
	public TypePanel() {

		thisList = this;

		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(50dlu;default)"),
				ColumnSpec.decode("max(50dlu;default)"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(100dlu;default)"),
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
				FormFactory.RELATED_GAP_ROWSPEC,}));

		lblType = new JLabel("Type :");
		add(lblType, "2, 2");

		textFieldType = new JTextField();
		add(textFieldType, "2, 4, 2, 1, fill, default");
		textFieldType.setColumns(10);

		lblDescrip = new JLabel("Description :");
		add(lblDescrip, "2, 6");

		textFieldDescrip = new JTextField();
		add(textFieldDescrip, "2, 8, 2, 1, fill, default");
		textFieldDescrip.setColumns(10);

		btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(textFieldType.getText().equals("") || textFieldDescrip.getText().equals("")){
					JOptionPane.showMessageDialog(thisList,
							"One of the fields is not filled in");
				}else{
					Pattern p = Pattern.compile("^\\d+\\s*$");
					Matcher m = p.matcher(textFieldType.getText());
					if (m.matches()) {
						listModel.addElement(textFieldType.getText()+" : "+ textFieldDescrip.getText());
						textFieldType.setText("");
						textFieldDescrip.setText("");
					} else {
						JOptionPane.showMessageDialog(thisList,
								"Type must be an integer value");
					}
				}

			}
		});
		add(btnAdd, "5, 4");

		btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listModel.remove(list.getSelectedIndex());
			}
		});
		add(btnRemove, "5, 6");

		listModel = new DefaultListModel();
		list = new JList(listModel);
		listScroller = new JScrollPane(list);
		add(listScroller, "7, 2, 1, 7, fill, fill");
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
