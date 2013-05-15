package vroom.common.modeling.vrprep.xmlPrototyper;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
/**
 * Class used to construct a tab label with default properties
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a>
 *
 */
public class TabLabel extends JLabel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6943244864168723861L;
	
	public TabLabel(String name){
		Dimension tabSize = new Dimension(142, 40);
		 
		super.setText(name);
		super.setPreferredSize(tabSize);
		super.setMinimumSize(tabSize);
		super.setHorizontalAlignment(SwingConstants.CENTER);
	}

}
