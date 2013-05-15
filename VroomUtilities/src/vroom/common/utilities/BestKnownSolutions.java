/**
 * 
 */
package vroom.common.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.optimization.OptimizationSense;

/**
 * <code>BestKnownSolutions</code> is a utility class to read a property file and get best known solutions.
 * <p>
 * Creation date: Jul 6, 2010 - 6:00:37 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class BestKnownSolutions {

    /**
     * Getter for this class logger
     * 
     * @return the logger associated with this class
     */
    public static LoggerHelper getLogger() {
        return LoggerHelper.getLogger(BestKnownSolutions.class);
    }

    /**
     * A suffix added to instance name to state whether the best known solution is optimal or not
     */
    public static final String OPT_SUFFIX        = "-Opt";

    /**
     * A suffix added to instance name to specify the detailed solution
     */
    public static final String SOLUTION_SUFFIX   = "-S";

    /**
     * A suffix added to instance name to specify the authors for a BKS
     */
    public static final String AUTHOR_SUFFIX     = "-A";

    /**
     * A suffix added to instance name to specify the number of vehicles
     */
    public static final String FLEET_SIZE_SUFFIX = "-K";

    /** A flag that can be set to <code>true</code> if all bks are optimal */
    public static final String ALL_OPTIMAL       = "AllOptimal";

    /** String representing that no BKS is konwn */
    public static final String NA                = "na";

    private final Properties   mProperties;

    private boolean            mAllOpt;

    private final File         mFile;

    private boolean            mChanged          = false;

    private String             mComments;

    /**
     * Creates a new <code>BestKnownSolutions</code>
     * 
     * @param file
     *            the property file containing the best known solutions
     * @throws FileNotFoundException
     * @throws IOException
     */
    public BestKnownSolutions(String file) {
        mProperties = new SortedProperties();

        mFile = new File(file);

        FileReader reader = null;
        try {
            if (!mFile.exists())
                mFile.createNewFile();

            reader = new FileReader(mFile);

            mProperties.load(reader);

            // Read and store the comment lines
            BufferedReader r = new BufferedReader(reader);

            String line = r.readLine();
            StringBuffer coms = new StringBuffer(250);

            while (line != null && line.startsWith("#")) {
                if (coms.length() > 0) {
                    coms.append("\n");
                }

                coms.append(line.substring(1));
                line = r.readLine();
            }
            mComments = coms.toString();
        } catch (FileNotFoundException e) {
            getLogger().exception("BestKnownSolution constructor", e);
        } catch (IOException e) {
            getLogger().exception("BestKnownSolution constructor", e);
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    getLogger().exception("BestKnownSolutions.BestKownSolutions", e);
                }
        }

        if (mProperties.containsKey(ALL_OPTIMAL)) {
            mAllOpt = Boolean.valueOf(mProperties.getProperty(ALL_OPTIMAL));
        } else {
            mAllOpt = false;
        }
    }

    /**
     * Return a list of all the instances' name contained in this instance
     * 
     * @return a list of all the instances' name contained in this instance
     */
    public List<String> getInstanceNames() {
        List<String> names = new ArrayList<>();

        for (Object key : mProperties.keySet()) {
            if (key instanceof String) {
                String s = (String) key;
                if (!s.endsWith(AUTHOR_SUFFIX) && !s.endsWith(FLEET_SIZE_SUFFIX)
                        && !s.endsWith(OPT_SUFFIX) && !s.endsWith(SOLUTION_SUFFIX))
                    names.add(s);
            }
        }

        Collections.sort(names);

        return names;
    }

    /**
     * Getter for the best known solution for a given instance
     * 
     * @param instanceName
     *            the name of the considered instance
     * @return the corresponding BKS, or <code>null</code> if no value is associated with the given instance.
     */
    public Double getBKS(String instanceName) {
        if (mProperties.containsKey(instanceName)) {
            String value = mProperties.getProperty(instanceName);
            if (NA.equalsIgnoreCase(value)) {
                return null;
            } else {
                return Double.valueOf(value);
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the gap to the best known solution, or 0 if the BKS is not defined
     * 
     * @param instanceName
     * @param sol
     * @param sense
     * @return the gap to the best known solution, or 0 if the BKS is not defined
     */
    public double getGapToBKS(String instanceName, double sol, OptimizationSense sense) {
        Double bks = getBKS(instanceName);
        if (bks != null)
            if (bks != 0)
                return sense.getImprovement(sol, bks) / bks;
            else
                return Double.NaN;
        else
            return Double.NaN;

    }

    /**
     * Getter for the a value associated with a given instance
     * 
     * @param instanceName
     *            the name of the considered instance
     * @param suffix
     *            the suffix to the instance name
     * @return the String associated with key <code>instanceName+"-"+suffix</code>
     */
    public String getValue(String instanceName, String suffix) {
        String key = instanceName + suffix;
        if (mProperties.containsKey(key)) {
            return mProperties.getProperty(key);
        } else {
            return null;
        }
    }

    /**
     * Getter for the a value associated with a given instance
     * 
     * @param instanceName
     *            the name of the considered instance
     * @param suffix
     *            the suffix to the instance name
     * @return the String associated with key <code>instanceName+"-"+suffix</code>
     */
    public Integer getIntValue(String instanceName, String suffix) {
        String val = getValue(instanceName, suffix);
        return val != null ? Integer.valueOf(val) : null;
    }

    /**
     * Getter for the a value associated with a given instance
     * 
     * @param instanceName
     *            the name of the considered instance
     * @param suffix
     *            the suffix to the instance name
     * @return the String associated with key <code>instanceName+"-"+suffix</code>
     */
    public double getDoubleValue(String instanceName, String suffix) {
        return Double.valueOf(getValue(instanceName, suffix));
    }

    /**
     * Check if a best known solution is an optimal solution
     * 
     * @param instanceName
     *            the name of the considered instance
     * @return <code>true</code> if the BKS is optimal, or <code>false</code> if it is not or no value is associated
     *         with the given instance
     */
    public boolean isOptimal(String instanceName) {
        if (mAllOpt && mProperties.containsKey(instanceName)) {
            return true;
        } else {
            String b = getValue(instanceName, OPT_SUFFIX);
            return b != null ? Boolean.valueOf(b) : false;
        }
    }

    /**
     * Returns the authors associated with a BKS
     * 
     * @param instancename
     *            the name of the considered instance
     * @return the authors associated with a BKS
     */
    public String getAuthors(String instancename) {
        return getValue(instancename, AUTHOR_SUFFIX);
    }

    /**
     * Returns the number of vehicles used in a BKS
     * 
     * @param instancename
     *            the name of the considered instance
     * @return the number of vehicles used in a BKS
     */
    public String getFleetSize(String instancename) {
        return getValue(instancename, FLEET_SIZE_SUFFIX);
    }

    /**
     * Returns the detailed BKS as a string
     * 
     * @param instancename
     *            the name of the considered instance
     * @return the detailed BKS as a string
     */
    public String getSolution(String instancename) {
        return getValue(instancename, SOLUTION_SUFFIX);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return mProperties.toString();
    }

    /**
     * Returns a string with the BKS in a CSV format
     * 
     * @param linesPerInstance
     *            the number of times a row has to be repeated
     * @return a string with the BKS in a CSV format
     */
    public String toCSVString(int linesPerInstance) {
        StringBuffer sb = new StringBuffer();
        sb.append("name;bks;bks_opt;bks_K;bks_authors;bks_sol\n");
        for (Enumeration<Object> e = mProperties.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            if (!key.endsWith(OPT_SUFFIX) && !key.endsWith(AUTHOR_SUFFIX)
                    && !key.endsWith(FLEET_SIZE_SUFFIX) && !key.endsWith(SOLUTION_SUFFIX)) {
                for (int i = 0; i < linesPerInstance; i++) {
                    sb.append(String.format("%s;%s;%s;%s;%s;%s\n", key, getBKS(key),
                            isOptimal(key), getFleetSize(key), getAuthors(key), getSolution(key)));
                }
            }
        }
        return sb.toString();
    }

    /**
     * Set the all optimal flag
     * 
     * @param b
     *            <code>true</code> if all solutions are optimal
     */
    public void setAllOptimal(boolean b) {
        mProperties.setProperty(ALL_OPTIMAL, "" + b);
        mAllOpt = b;
        mChanged = true;
    }

    /**
     * Set the best known solution
     * 
     * @param instance
     *            the instance name
     * @param obj
     *            the objective value
     * @param optimal
     *            <code>true</code> if <code>obj</code> is the optimal value for the given <code>instance</code>
     * @param fleetSize
     *            the number of vehicles used, {@code null} if ignored
     * @param authors
     *            the name of the authors, {@code null} if ignored
     * @param solution
     *            the detailed solution, {@code null} if ignored
     */
    public void setBestKnownSolution(String instance, double obj, boolean optimal,
            Integer fleetSize, String authors, String solution) {
        mProperties.setProperty(instance, "" + obj);
        if (!mAllOpt) {
            mProperties.setProperty(instance + OPT_SUFFIX, "" + optimal);
        }
        if (fleetSize != null)
            mProperties.setProperty(instance + FLEET_SIZE_SUFFIX, fleetSize.toString());
        if (authors != null)
            mProperties.setProperty(instance + AUTHOR_SUFFIX, authors);
        if (solution != null)
            mProperties.setProperty(instance + SOLUTION_SUFFIX, solution);
        mChanged = true;
    }

    /**
     * Getter for <code>file</code>
     * 
     * @return the file
     */
    public File getFile() {
        return mFile;
    }

    /**
     * Save the current values to the best known solution file
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void save(String comment) throws FileNotFoundException, IOException {
        if (mChanged) {
            FileOutputStream output = new FileOutputStream(mFile);
            mProperties.store(output, mComments.isEmpty() ? comment : mComments + "\n" + comment);
            output.close();
            mChanged = false;
        }
    }
}
