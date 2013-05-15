package vroom.common.utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * Creation date: Mar 1, 2010 - 6:07:49 PM<br/>
 * <code>Reflection</code> is a utility class used for reflection on java objects
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class Reflection {

    /**
     * Finds a constructor that can accept the given arguments
     * 
     * @param <T>
     * @param clazz
     *            the class for which a constructor is needed
     * @param argsTypes
     *            the type of arguments that will be passed to the constructor
     * @return a constructor that can accept the given argument types
     * @throws NoSuchMethodException
     *             if no matching constructor has been found
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getMatchingConstructor(Class<T> clazz, Class<?>... argsTypes)
            throws NoSuchMethodException {

        Constructor<?>[] constructors = clazz.getConstructors();

        for (Constructor<?> c : constructors) {
            Type[] types = c.getGenericParameterTypes();
            if (types.length == argsTypes.length) {
                if (types.length == 0) {
                    return (Constructor<T>) c;
                }

                int match = 0;
                for (int i = 0; i < argsTypes.length; i++) {
                    Class<?> argClass = null;

                    // Check if the type is a Class
                    if (types[i] instanceof Class<?>) {
                        argClass = (Class<?>) types[i];
                    } else if (types[i] instanceof ParameterizedType
                            && ((ParameterizedType) types[i]).getRawType() instanceof Class) {
                        argClass = (Class<?>) ((ParameterizedType) types[i]).getRawType();
                    }

                    if (argClass != null && argClass.isAssignableFrom(argsTypes[i])) {
                        match++;
                    }
                }

                if (match == argsTypes.length) {
                    return (Constructor<T>) c;
                }
            }
        }

        throw new NoSuchMethodException(clazz.getName() + ".<init>" + Arrays.toString(argsTypes));

    }
}
