/**
 * 
 */
package vroom.common.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * <code>FileBufferedWriter</code> is a specialization of {@link BufferedWriter} to write text to a file
 * <p>
 * Creation date: Nov 8, 2011 - 12:11:47 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class FileBufferedWriter extends BufferedWriter {

    /**
     * Creates a new <code>FileBufferedWriter</code>
     * 
     * @param path
     *            the path to the destination file
     * @throws IOException
     */
    public FileBufferedWriter(String path) throws IOException {
        super(new FileWriter(new File(path)));
    }

    /**
     * Write a formated string
     * 
     * @param format
     *            a format string
     * @param args
     *            arguments of the format string
     * @throws IOException
     * @see {@link String#format(String, Object...)}
     */
    public void write(String format, Object... args) throws IOException {
        super.write(String.format(format, args));
    }

    /**
     * Write a formated line
     * 
     * @param format
     *            a format string
     * @param args
     *            arguments of the format string
     * @throws IOException
     * @see {@link String#format(String, Object...)}
     */
    public void writeLine(String format, Object... args) throws IOException {
        write(format, args);
        super.newLine();
    }

    /**
     * Write a formated comment
     * 
     * @param format
     *            a format string
     * @param args
     *            arguments of the format string
     * @throws IOException
     * @see {@link String#format(String, Object...)}
     */
    public void writeCommentLine(String format, Object... args) throws IOException {
        write("# " + format, args);
        super.newLine();
    }

}
