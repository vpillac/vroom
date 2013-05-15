package vroom.common.modeling.dataModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import vroom.common.modeling.dataModel.attributes.AttributeKey;
import vroom.common.modeling.dataModel.attributes.IAttribute;

/**
 * Creation date: 10:01:02 AM<br/>
 * <code>ObjectWithAttributres</code> is the base type for all objects that have attributes associated to them. It
 * provides a mapping between {@link AttributeKey} and {@link IAttribute} values.
 * 
 * @param <A>
 *            the generic type
 * @param <K>
 *            the key type
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ObjectWithAttributes<A extends IAttribute, K extends AttributeKey<? extends A>>
        implements IObjectWithAttributes<A, K> {

    /** The mapping of attributes for this request. */
    protected final Map<K, A> mAttributes;

    /**
     * Instantiates a new object with attributes.
     */
    public ObjectWithAttributes() {
        super();
        mAttributes = new HashMap<K, A>();
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.IObjectWithAttributes#getAttribute(KE)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <AE extends A, KE extends AttributeKey<AE>> AE getAttribute(KE attributeKey) {
        return (AE) this.mAttributes.get(attributeKey);
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.IObjectWithAttributes#setAttribute(K, A)
     */
    @Override
    public A setAttribute(K attributeKey, A value) {
        if (attributeKey.isValidValue(value)) {
            return this.mAttributes.put(attributeKey, value);
        } else {
            throw new IllegalArgumentException(String.format(
                    "The given value is not valid for the specified key (key:%s value:%s)",
                    attributeKey, value));
        }
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.IObjectWithAttributes#getAttributesAsString()
     */
    @Override
    public String getAttributesAsString() {
        StringBuilder b = new StringBuilder(mAttributes.size() * 10);

        LinkedList<Entry<K, A>> keys = new LinkedList<Entry<K, A>>(mAttributes.entrySet());

        Collections.sort(keys, new Comparator<Entry<K, A>>() {

            @Override
            public int compare(Entry<K, A> o1, Entry<K, A> o2) {
                return o1.getKey().getName().compareTo(o2.getKey().getName());
            }
        });

        b.append('{');

        for (Entry<K, A> e : keys) {
            b.append(e.getKey().getName());
            b.append('=');
            b.append(e.getValue());
            b.append(',');
        }

        if (b.length() > 1) {
            b.setCharAt(b.length() - 1, '}');
        } else {
            b.append('}');
        }

        return b.toString();
    }

}