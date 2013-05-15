package vroom.optimization.online.jmsa.vrp.visu;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import vroom.optimization.online.jmsa.MSABase;
import vroom.optimization.online.jmsa.vrp.MSAVRPInstance;

public class MSAVisualizationFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private final DynamicInstanceGraph mGraph;

	private MSABase<?, ?> mMSA;

	/** the value of perfect information **/
	private final double mPI;
	private MSAInfoPanel mInfo;
	private MSAVisualizationPanel mVisu;

	/**
	 * Getter for the value of perfect information
	 * 
	 * @return the value of pi
	 */
	public double getPI() {
		return mPI;
	}

	/**
	 * Create the application.
	 */
	public MSAVisualizationFrame(MSABase<?, ?> msa, double pi) {
		super("MSA Visualization");
		mPI = pi;

		setPreferredSize(new Dimension(800, 500));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mMSA = msa;
		mGraph = new DynamicInstanceGraph((MSAVRPInstance) mMSA.getInstance());
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		// Visualization pane
		mVisu = new MSAVisualizationPanel(new DynamicInstanceViewer(mGraph));

		// Info pane
		mInfo = new MSAInfoPanel(mMSA, getPI());

		// Split pane
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				false, mInfo, mVisu);

		splitPane.setDividerSize(3);
		setContentPane(splitPane);
		pack();

		splitPane.setDividerLocation(0.3);
	}

	/**
	 * Detach this frame from the MSA procedure. Will attempt to remove any
	 * reference to free memory.
	 */
	public void detach() {
		mMSA = null;
		mInfo.detach();
		mVisu.detach();
	}

}
