/**
 * 
 * @author Maxim Hoskins (https://plus.google.com/115909706630698463631)
 *
 */
package vroom.common.modeling.vrprep.xmlPrototyper;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 * Class used to construct a tab panel with default properties
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a>
 *
 */
public class TabPanel extends JScrollPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8894275788567900628L;
	/**
	 * Maximum value the vertical scrollBar had in its previous state
	 */
	private int prevMax;
	/**
	 * Visible amount the scrollBar had in its previous state
	 */
	private int prevVisAmount = 0;
	/**
	 * True if the scrollBar was visible in its previous state
	 */
	private boolean prevVisState;

	/**
	 * Constructor for the class TabPanel.java
	 * @param panel JPanel to add to the scrollBar
	 */
	public TabPanel(JPanel panel){
		// scrollBar properties
		verticalScrollBar.setUnitIncrement(15);
		super.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		super.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		// scrollPane content
		setViewportView(panel);		
		
		//intitial values
		prevMax = verticalScrollBar.getMaximum();
		prevVisAmount = verticalScrollBar.getVisibleAmount();
		prevVisState = verticalScrollBar.isVisible();

		// adjustment listener
		AdjustmentListener l = new AdjustmentListener(){
			public void adjustmentValueChanged(AdjustmentEvent e) {
				// if scrollBar was already visible, is now bigger than before and is nearly at bottom of page
				if(prevVisState && 
						prevMax < verticalScrollBar.getMaximum() &&
						verticalScrollBar.getValue()+prevVisAmount >= prevMax-60){
					
					verticalScrollBar.setValue(verticalScrollBar.getMaximum());		
				}
				
				// save new values
				prevMax = verticalScrollBar.getMaximum();
				prevVisAmount = verticalScrollBar.getVisibleAmount();
				prevVisState = verticalScrollBar.isVisible();
			}
		};
		verticalScrollBar.addAdjustmentListener(l);
	}
}

