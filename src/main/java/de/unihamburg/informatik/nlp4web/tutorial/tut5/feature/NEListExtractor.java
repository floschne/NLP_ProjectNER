package de.unihamburg.informatik.nlp4web.tutorial.tut5.feature;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.jcas.JCas;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.NamedFeatureExtractor1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Feature extractor that extracts NE features from a textual list of NE features and their corresponding NE-Tag
 * Constraints on the lists:
 * - must contain textual content only
 * - first word in the each line has to be the NE Feature Tag Name
 * - after NE Feature Tag Name, separated by the delimiterSymbol, there follows a sequence of words representing the NE
 */
public class NEListExtractor implements NamedFeatureExtractor1<Token> {

    private final Map<String, String> neToTag = new HashMap<>();
    private final String featureName;
    private String delimiterSymbol = " ";

    public NEListExtractor(File nerListFile, String name) throws IOException {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("Provide a valid, non-empty name for the NE feature!");
        this.featureName = name;

        generateNeToTagMap(nerListFile);
    }

    /**
     * Generates the map from NE to their corresponding tag.
     * @param nerListFile the file that contains the list of NE and their tags
     * @throws IOException if file not found or error while readings
     */
    private void generateNeToTagMap(File nerListFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(nerListFile))) {
            String line;
            StringBuilder sb = new StringBuilder();
            // read line by line
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(delimiterSymbol);
                // since a name can contain more than two space-separated strings, the splitted strings are combined again
                for (int i = 1; i < split.length; ++i)
                    sb.append(split[i]).append(" ");
                // add the NE and the NE tag to the map
                neToTag.put(sb.toString().trim(), split[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<Feature> extract(JCas view, Token focusAnnotation) throws CleartkExtractorException {
        String coveredText = focusAnnotation.getCoveredText();
        String tag = neToTag.get(coveredText);
        return tag == null ? Collections.emptyList() : Collections.singletonList(new Feature(this.featureName + "_" + tag));
    }

    @Override
    public String getFeatureName() {
        return this.featureName + "_ListFeature";
    }

    public void setDelimiterSymbol(String delimiterSymbol) {
        if(delimiterSymbol == null || delimiterSymbol.isEmpty() || delimiterSymbol.length() > 1)
            throw new IllegalArgumentException("Delimiter Symbol must contain exactly one character!");
        this.delimiterSymbol = delimiterSymbol;
    }
}