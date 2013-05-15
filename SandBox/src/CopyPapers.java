import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CopyPapers {

    public static String sPathReport = "/home/vpillac/Documents/Dropbox/Doctorat/Documents/Dissertation/report/";
    public static String sPathPapers = "/home/vpillac/Documents/Dropbox/Doctorat/Documents/";

    /**
     * @param args
     */
    public static void main(String[] args) {
        String[][] paths = new String[][] {
                { "D-VRP/Pillac_DVRP_tech.tex", "Pillac_DVROOM_deter.tex" },//
                { "D-TRSP/Pillac_DTRSP_tech.tex", "Pillac_DVROOM_TRSP_dynamic.tex" } };

        for (String[] path : paths) {
            try {
                copyFile(sPathPapers + path[0], sPathReport + path[1]);
                System.out.printf("Copyied %s to %s\n", path[0], path[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void copyFile(String source, String destination) throws IOException {
        BufferedReader src = new BufferedReader(new FileReader(source));
        BufferedReader destIn = new BufferedReader(new FileReader(destination));

        // Read the destination header
        StringBuilder header = new StringBuilder();
        String line;
        do {
            line = destIn.readLine();
            header.append(line);
            header.append("\n");
        } while (!line.startsWith("%% BEGIN"));
        boolean subsection = line.contains("sub");
        // Read the destination footer
        while (!line.startsWith("%% END")) {
            line = destIn.readLine();
        }
        StringBuilder footer = new StringBuilder();
        do {

            footer.append(line);
            footer.append("\n");
            line = destIn.readLine();
        } while (line != null);

        StringBuilder destString = new StringBuilder();

        // Write the header
        destString.append(header.toString());

        // Move to the start of the document
        line = src.readLine();
        do {
            line = src.readLine();
        } while (!line.startsWith("%% BEGIN"));
        line = src.readLine();
        // Copy the content
        do {
            if (subsection) {
                if (line.contains("\\paragraph"))
                    System.err.println("Paragraph detected: " + line);
                else if (line.contains("\\subsubsection"))
                    line = line.replace("\\subsubsection", "\\paragraph");
                else if (line.contains("\\subsection"))
                    line = line.replace("\\subsection", "\\subsubsection");
                else if (line.contains("\\section"))
                    line = line.replace("\\section", "\\subsection");
            }
            destString.append(line);
            destString.append("\n");
            line = src.readLine();
        } while (!line.startsWith("%% END"));

        // Write the footer
        destString.append(footer.toString());

        BufferedWriter dest = new BufferedWriter(new FileWriter(destination, false));
        dest.write(destString.toString());
        // Flush
        dest.flush();
        dest.close();
        src.close();
        destIn.close();
    }
}
