/**
 * 
 */
package vroom.common.utilities.logging;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * <code>LogPrintStream</code> is a {@link PrintStream} designed to print simultaneously in the console and in a file.
 * <p>
 * Creation date: Jun 17, 2010 - 11:22:22 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class LogPrintStream extends PrintStream {

    private final PrintStream mFileStream;

    /**
     * Creates a new <code>LogPrintStream</code> that will write in both the given output stream and file.
     * 
     * @param logFile
     *            the file in which messages will be logged
     * @param outputStream
     *            the base output stream (typically one of {@link System#out} or {@link System#out})
     * @see PrintStream#PrintStream(OutputStream, boolean)
     */
    public LogPrintStream(File logFile, OutputStream outputStream) throws IOException {
        super(outputStream, true);
        mFileStream = new PrintStream(logFile);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#flush()
     */
    @Override
    public void flush() {
        super.flush();
        mFileStream.flush();
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#append(char)
     */
    @Override
    public PrintStream append(char c) {
        mFileStream.append(c);
        return super.append(c);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#append(java.lang.CharSequence)
     */
    @Override
    public PrintStream append(CharSequence csq) {
        mFileStream.append(csq);
        return super.append(csq);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#append(java.lang.CharSequence, int, int)
     */
    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        mFileStream.append(csq, start, end);
        return super.append(csq, start, end);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#checkError()
     */
    @Override
    public boolean checkError() {
        mFileStream.checkError();
        return super.checkError();
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#close()
     */
    @Override
    public void close() {
        mFileStream.close();
        super.close();
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#write(byte[], int, int)
     */
    @Override
    public void write(byte[] buf, int off, int len) {
        mFileStream.write(buf, off, len);
        super.write(buf, off, len);
    }

    /* (non-Javadoc)
     * @see java.io.PrintStream#write(int)
     */
    @Override
    public void write(int b) {
        mFileStream.write(b);
        super.write(b);
    }

    /* (non-Javadoc)
     * @see java.io.FilterOutputStream#write(byte[])
     */
    @Override
    public void write(byte[] b) throws IOException {
        mFileStream.write(b);
        super.write(b);
    }

}
