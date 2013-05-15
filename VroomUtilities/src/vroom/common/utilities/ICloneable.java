package vroom.common.utilities;

/**
 * <code>Cloneable</code> is a improved interface aimed to replace the {@link java.lang.Cloneable} interface from the
 * JRE that does not provide typesafe cloning.
 * <p>
 * Creation date: Apr 27, 2010 - 3:41:25 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 * @<T> the type of clone objects
 */
public interface ICloneable<T> {

    /**
     * Creates and returns a copy of this object. The precise meaning of "copy" may depend on the class of the object.
     * The general intent is that, for any object x, the expression: <br/>
     * <code>x.cloneObject() != x</code> <br/>
     * will be true, and that the expression: <br/>
     * <code>x.cloneObject().getClass() == x.getClass()</code> <br/>
     * will be true, but these are not absolute requirements.
     * <p/>
     * While it is typically the case that: <br/>
     * <code>x.cloneObject().equals(x)</code> <br/>
     * will be true, this is not an absolute requirement.
     * 
     * @return a clone of this object
     * @see Object#clone()
     * @see java.lang.Cloneable#clone()
     */
    public T clone();
}
