package vroom.common.modeling.dataModel.attributes;

import vroom.common.modeling.dataModel.Request;
import vroom.common.modeling.dataModel.Vehicle;

// TODO: Auto-generated Javadoc
/**
 * 
 * The Class <code>Tool</code> represents a Tool that can be associated to
 * either a {@link Request} or a {@link Vehicle}.
 * 
 * <p>
 * Creation date: Feb 8, 2011 - 2:43:07 PM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class Tool implements ICompatibilityAttribute, INodeAttribute,
		IRequestAttribute, IVehicleAttribute {

	/** the name of this tool *. */
	private final String mToolName;

	/**
	 * Getter for the name of this tool.
	 * 
	 * @return this tool name
	 */
	public String getToolName() {
		return this.mToolName;
	}

	/** a unique id for this tool *. */
	private final int mToolId;

	/**
	 * Getter for a unique id for this tool.
	 * 
	 * @return the unique Id for this tool
	 */
	public int getToolId() {
		return this.mToolId;
	}

	/**
	 * Instantiates a new tool, the name {@link String#hashCode() hashCode} will
	 * be used as unique ID.
	 * 
	 * @param toolName
	 *            the tool name
	 */
	public Tool(String toolName) {
		this(toolName, toolName.hashCode());
	}

	/**
	 * Instantiates a new tool, the id will be used as name.
	 * 
	 * @param toolId
	 *            the tool id
	 */
	public Tool(int toolId) {
		this("" + toolId, toolId);
	}

	/**
	 * Instantiates a new tool.
	 * 
	 * @param toolName
	 *            the tool name
	 * @param toolId
	 *            the tool id
	 */
	protected Tool(String toolName, int toolId) {
		super();
		mToolName = toolName;
		mToolId = toolId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.utilities.dataModel.IObjectWithName#getName()
	 */
	@Override
	public String getName() {
		return "Tool";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.common.modeling.dataModel.attributes.ICompatibilityAttribute#
	 * isCompatibleWith
	 * (vroom.common.modeling.dataModel.attributes.ICompatibilityAttribute)
	 */
	@Override
	public boolean isCompatibleWith(ICompatibilityAttribute otherAttribute) {
		return otherAttribute instanceof Tool
				&& getToolId() == ((Tool) otherAttribute).getToolId();
	}

	@Override
	public String toString() {
		return mToolName;
	}

}
