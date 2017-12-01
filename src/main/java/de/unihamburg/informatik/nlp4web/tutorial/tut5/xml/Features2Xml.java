package de.unihamburg.informatik.nlp4web.tutorial.tut5.xml;

import com.thoughtworks.xstream.XStream;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unihamburg.informatik.nlp4web.tutorial.tut5.annotator.NERAnnotator;
import org.apache.uima.UIMAFramework;
import org.apache.uima.util.Level;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class Features2Xml {
    public static void generateTokenFeatureExtractors(String filename) throws IOException {

        List<FeatureExtractor1<Token>> featureExtractors = NERAnnotator.createFeatureExtractors();

        XStream xstream = XStreamFactory.createXStream();
        String x = xstream.toXML(featureExtractors);
        x = removeLogger(x);
        PrintStream ps = new PrintStream(filename);
        ps.println(x);
        ps.close();
    }

    /**
     * To make the xml file more readable remove the logger elements
     * that are'nt needed
     *
     * @param x
     * @return
     */
    private static String removeLogger(String x) {
        StringBuffer buffer = new StringBuffer();
        String[] lines = x.split("\n");
        boolean loggerFound = false;
        for (String l : lines) {
            if (l.trim().startsWith("<logger>")) {
                loggerFound = true;
            }
            if (!loggerFound) {
                buffer.append(l);
                buffer.append("\n");
            } else {
                if (l.trim().startsWith("</logger>")) {
                    loggerFound = false;
                }
            }
        }

        return buffer.toString();
    }

    public static void main(String[] args) throws IOException {
        UIMAFramework.getLogger().log(Level.INFO, "Writing features.xml file!");
        String featureFileName = "src/main/resources/feature/features.xml";
        generateTokenFeatureExtractors(featureFileName);
        UIMAFramework.getLogger().log(Level.INFO, "Done: " + featureFileName);
    }
}
