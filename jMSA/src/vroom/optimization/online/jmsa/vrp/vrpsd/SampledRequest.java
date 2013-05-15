package vroom.optimization.online.jmsa.vrp.vrpsd;

import java.util.Arrays;

import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.attributes.AttributeKey;
import vroom.common.modeling.dataModel.attributes.DeterministicDemand;
import vroom.common.modeling.dataModel.attributes.IDemand;
import vroom.common.modeling.dataModel.attributes.IRequestAttribute;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.common.utilities.IDerefenceable;
import vroom.common.utilities.IObservable;
import vroom.common.utilities.IObserver;
import vroom.common.utilities.Update;
import vroom.common.utilities.ValueUpdate;
import vroom.common.utilities.Wrapper;

/**
 * Creation date: Apr 19, 2010 - 12:03:54 PM<br/>
 * <code>SampledRequest</code> is a {@linkplain Wrapper wrapper} for a
 * {@linkplain IVRPRequest request} of the a VRPSD for which the demands have
 * been sampled
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SampledRequest implements IVRPRequest, Wrapper<IVRPRequest>,
		IObserver, IDerefenceable {

	private static final RequestAttributeKey<DeterministicDemand> SAMPLED_DEMAND = new RequestAttributeKey<DeterministicDemand>(
			"sampled_demand", DeterministicDemand.class);

	/** The wrapped request */
	private final IVRPRequest mRequest;

	/** An array containing the sampled demands for each product */
	private final double[] mSampledDemands;

	private final DeterministicDemand mSampledDemandAtt;

	/**
	 * Creates a new <code>SampledRequest</code>
	 * 
	 * @param request
	 *            the wrapped request
	 * @param sampledDemands
	 *            an array containing the sampled demand values
	 */
	public SampledRequest(IVRPRequest request, double[] sampledDemands) {
		mRequest = request;
		if (mRequest instanceof IObservable) {
			((IObservable) mRequest).addObserver(this);
		}
		mSampledDemands = sampledDemands;
		mSampledDemandAtt = new DeterministicDemand(mSampledDemands);
	}

	@Override
	public double getDemand() {
		return getDemand(0);
	}

	@Override
	public IDemand getDemandAttribute() {
		return mSampledDemandAtt;
	}

	@Override
	public double getDemand(int product) {
		return mSampledDemands[product];
	}

	@Override
	public Node getDestinationNode() {
		return mRequest.getDestinationNode();
	}

	@Override
	public Node getNode() {
		return mRequest.getNode();
	}

	@Override
	public boolean isOriginDestination() {
		return mRequest.isOriginDestination();
	}

	@Override
	public void setDemands(double... demands) {
		throw new IllegalAccessError(
				"Demands cannot be set on an instance of SampledRequest");
	}

	@Override
	public <AE extends IRequestAttribute, KE extends AttributeKey<AE>> AE getAttribute(
			KE attributeKey) {
		return mRequest.getAttribute(attributeKey);
	}

	@Override
	public String getAttributesAsString() {
		return mRequest.getAttributesAsString();
	}

	@Override
	public IRequestAttribute setAttribute(RequestAttributeKey<?> attributeKey,
			IRequestAttribute value) {
		throw new IllegalAccessError(
				"Cannot set attributes on an instance of SampledRequest");
	}

	@Override
	public String toString() {
		if (isOriginDestination()) {
			return String.format("[%s->%s %s (dem:%s)]", getNode(),
					getDestinationNode(), getAttributesAsString(),
					Arrays.toString(mSampledDemands));
		} else {
			return String.format("[%s %s(dem:%s)]", getDestinationNode(),
					getAttributesAsString(), Arrays.toString(mSampledDemands));
		}
	}

	@Override
	public IVRPRequest getWrappedObject() {
		return mRequest;
	}

	@Override
	public int getID() {
		return getWrappedObject().getID();
	}

	@Override
	public void update(IObservable source, Update update) {
		if (source instanceof VRPSDActualRequest
				&& ((VRPSDActualRequest) source).getWrappedObject() == getWrappedObject()
				&& update instanceof ValueUpdate
				&& VRPSDActualRequest.PROP_ACTUAL_DEMANDS.equals(update
						.getDescription())) {
			for (int p = 0; p < mSampledDemands.length; p++) {
				mSampledDemands[p] = ((double[]) ((ValueUpdate) update)
						.getNewValue())[p];
			}
		}
	}

	@Override
	public void dereference() {
		if (mRequest instanceof IObservable) {
			((IObservable) mRequest).removeObserver(this);
		}
	}
}
