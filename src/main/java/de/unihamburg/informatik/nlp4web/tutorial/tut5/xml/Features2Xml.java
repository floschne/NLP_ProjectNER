package de.unihamburg.informatik.nlp4web.tutorial.tut5.xml;

import com.thoughtworks.xstream.XStream;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unihamburg.informatik.nlp4web.tutorial.tut5.feature.FeatureExtractorFactory;
import org.apache.commons.math3.util.Combinations;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.uima.UIMAFramework;
import org.apache.uima.util.Level;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Features2Xml {

    private static final String OUTPUT_DIRECTORY = "src/main/resources/feature/";

    private static void generateTokenFeatureExtractors(String filename) throws IOException {

        List<FeatureExtractor1<Token>> featureExtractors = FeatureExtractorFactory.createAllFeatureExtractors();

        writeXML(filename, featureExtractors);
    }

    private static void writeXML(String filename, List<FeatureExtractor1<Token>> featureExtractors) throws FileNotFoundException {
        XStream xstream = XStreamFactory.createXStream();
        String xml = xstream.toXML(featureExtractors);
        xml = removeLogger(xml);
        PrintStream ps = new PrintStream(filename);
        ps.println(xml);
        ps.close();
    }

    /**
     * To make the xml file more readable remove the logger elements
     * that are'nt needed
     *
     * @param xml
     * @return
     */
    private static String removeLogger(String xml) {
        StringBuffer buffer = new StringBuffer();
        String[] lines = xml.split("\n");
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

    private static void generateFeatureAblationTestFiles() throws IOException {

        //create all feature extractors
        List<FeatureExtractor1<Token>> allFeatureExtractors = FeatureExtractorFactory.createAllFeatureExtractors();

        //compute all possible combinations of the feature extractors
        int numOfCombinations = 0;
        List<Combinations> combinations = new ArrayList<>();
        for (int i = 1; i <= allFeatureExtractors.size(); ++i) {
            Combinations combination = new Combinations(allFeatureExtractors.size(), i);
            combinations.add(combination);
            numOfCombinations += CombinatoricsUtils.binomialCoefficient(combination.getN(), combination.getK());
        }
        UIMAFramework.getLogger().log(Level.INFO, "Generating " + numOfCombinations + " different combinations of FeatureExtractor config files for the Feature Ablation Test!");

        //list of extractor names just for better readability in file names
        List<String> extractorNames = new ArrayList<>();
        extractorNames.add("stem");
        extractorNames.add("tokenFeature");
        extractorNames.add("contextFeature");
        extractorNames.add("nameList");
        extractorNames.add("cityList");
        extractorNames.add("countryList");
        extractorNames.add("miscList");
        extractorNames.add("orgList");
        extractorNames.add("locList");

        StringBuilder fileName = new StringBuilder();
        fileName.append(OUTPUT_DIRECTORY).append("featureExtractorCombination_");
        for (Combinations combination : combinations) {
            for (int[] tuple : combination) {
                //the tuple contains the indices of feature extractors that'll be used in this combination
                List<FeatureExtractor1<Token>> featureExtractorCombination = new ArrayList<>();
                for (int i : tuple) {
                    //use the current extractor in this combination
                    featureExtractorCombination.add(allFeatureExtractors.get(i));
                    //append the extractors name to the file name (better readability)
                    fileName.append(extractorNames.get(i)).append(",");
                }
                //remove last "," in file name
                fileName.setLength(fileName.length() - 1);
                fileName.append(".xml");
                writeXML(fileName.toString(), featureExtractorCombination);
                //reset filename
                fileName.setLength(0);
                fileName.append(OUTPUT_DIRECTORY).append("featureExtractorCombination_");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        UIMAFramework.getLogger().log(Level.INFO, "Writing " + OUTPUT_DIRECTORY + "features.xml file!");
        String featureFileName = OUTPUT_DIRECTORY + "features.xml";
        generateTokenFeatureExtractors(featureFileName);
        UIMAFramework.getLogger().log(Level.INFO, "Done: " + featureFileName);

        generateFeatureAblationTestFiles();
    }
}
