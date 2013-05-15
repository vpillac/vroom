/**
 * 
 */
package vroom.common.utilities;

import java.util.Arrays;

/**
 * The interface <code>StringParser</code> defines a generic parser that takes as input a {@link String} and returns an
 * object of type {@code  <T>}
 * <p>
 * Creation date: Jun 26, 2012 - 9:52:18 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 * @param <T>
 *            the type of the parsed object
 */
public interface StringParser<T> {

    /**
     * Parse a string in an instance of {@code  <T>}
     * 
     * @param string
     *            the string to be parsed
     * @return the instance of {@code  <T>} described by {@code  string}
     */
    public T parse(String string);

    /**
     * Convert an instance of {@code  <T>} into a {@link String}
     * 
     * @param object
     *            the object to be converted
     * @return a string describing {@code  object}
     */
    public String toString(T object);

    /**
     * The class <code>ArrayParser</code> is an implementation of {@link StringParser} that handle arrays of objects
     * <p>
     * Creation date: Jun 26, 2012 - 9:57:04 AM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     * @param <TT>
     */
    public static class ArrayParser<TT> implements StringParser<TT[]> {

        private final StringParser<TT> mParser;
        private final TT[]             mBaseArray;

        /**
         * Creates a new <code>ArrayParser</code>
         * 
         * @param parser
         *            the parser that will be used to parse each object
         * @param baseArray
         *            an array of the target type, used to instanciate new arrays at runtime
         */
        public ArrayParser(StringParser<TT> parser, TT[] baseArray) {
            super();
            mParser = parser;
            mBaseArray = baseArray;
        }

        @Override
        public TT[] parse(String string) {
            String[] strings = Utilities.toArray(string);
            TT[] objects = Arrays.copyOf(mBaseArray, strings.length);
            for (int i = 0; i < strings.length; i++) {
                objects[i] = mParser.parse(strings[i]);
            }
            return objects;
        }

        @Override
        public String toString(TT[] objects) {
            String[] strings = new String[objects.length];
            for (int i = 0; i < strings.length; i++) {
                strings[i] = mParser.toString(objects[i]);
            }
            return Utilities.toShortString(strings);
        }
    }

    /**
     * The class <code>DoubleParse</code> is an implementation of {@link StringParser} that handles {@link Double}
     * <p>
     * Creation date: Jun 26, 2012 - 10:03:32 AM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class DoubleParser implements StringParser<Double> {

        @Override
        public Double parse(String string) {
            return Double.valueOf(string);
        }

        @Override
        public String toString(Double object) {
            return object.toString();
        }

    }

    /**
     * The class <code>IntegerParser</code> is an implementation of {@link StringParser} that handles {@link Integer}
     * <p>
     * Creation date: Jun 26, 2012 - 10:03:32 AM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class IntegerParser implements StringParser<Integer> {

        @Override
        public Integer parse(String string) {
            return Integer.valueOf(string);
        }

        @Override
        public String toString(Integer object) {
            return object.toString();
        }

    }

    /**
     * The class <code>BooleanParser</code> is an implementation of {@link StringParser} that handles {@link Boolean}
     * <p>
     * Creation date: Jun 26, 2012 - 10:03:32 AM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class BooleanParser implements StringParser<Boolean> {

        @Override
        public Boolean parse(String string) {
            return Boolean.valueOf(string);
        }

        @Override
        public String toString(Boolean object) {
            return object.toString();
        }

    }

    /**
     * The class <code>DummyStringParser</code> is an implementation of {@link StringParser} for {@link String}.
     * <p>
     * Creation date: Jun 26, 2012 - 10:03:32 AM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class DummyStringParser implements StringParser<String> {

        @Override
        public String parse(String string) {
            return string;
        }

        @Override
        public String toString(String object) {
            return object;
        }

    }

    /**
     * The class <code>EnumStringParser</code> is an implementation of {@link StringParser} for {@link Enum}
     * <p>
     * Creation date: 18/02/2013 - 4:55:35 PM
     * 
     * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>
     * @version 1.0
     * @param <E>
     */
    public static class EnumStringParser<E extends Enum<E>> implements StringParser<E> {

        private final Class<E> mEnumType;

        public EnumStringParser(Class<E> enumType) {
            mEnumType = enumType;
        }

        @Override
        public E parse(String string) {
            return Enum.valueOf(mEnumType, string);
        }

        @Override
        public String toString(E object) {
            return object.name();
        }

    }
}
