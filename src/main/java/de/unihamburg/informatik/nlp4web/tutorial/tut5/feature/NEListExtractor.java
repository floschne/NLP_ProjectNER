package de.unihamburg.informatik.nlp4web.tutorial.tut5.feature;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.jcas.JCas;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.NamedFeatureExtractor1;
import org.cleartk.ml.feature.function.FeatureFunction;
import org.cleartk.ml.feature.function.FeatureFunctionExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
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
public class NEListExtractor implements FeatureFunction {

    private final String neListName;
    private final String delimiterSymbol = " ";
    private final String featureName;
    private Map<String, String> ne2tag;

    public NEListExtractor(String neListName, String featureName) throws IOException {
        if (neListName == null || neListName.isEmpty() || !new File(neListName).exists())
            throw new IllegalArgumentException("Provide a valid, non-empty path to the list of NE features");
        this.neListName = neListName;

        if (featureName == null || featureName.isEmpty())
            throw new IllegalArgumentException("Please provide a valid name for the NE feature!");
        this.featureName = featureName;

        this.ne2tag = null;
    }

    /**
     * Generates the map from NE to their corresponding tag.
     *
     * @throws IOException if file not found or error while readings
     */
    private void generateNeToTagMap() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(this.neListName))) {
            this.ne2tag = new HashMap<>();
            String line;
            StringBuilder sb = new StringBuilder();
            // read line by line
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(delimiterSymbol);
                // since a name can contain more than two space-separated strings, the splitted strings are combined again
                for (int i = 1; i < split.length; ++i)
                    sb.append(split[i]).append(" ");
                // add the NE and the NE tag to the map
                this.ne2tag.put(sb.toString().trim(), split[0]);
                //reset sb
                sb.setLength(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<Feature> apply(Feature feature) {
        if (feature == null || feature.getValue() == null)
            throw new IllegalArgumentException("Feature must not be null and has to have an non-empty value!");
        try {
            if (this.ne2tag == null)
                this.generateNeToTagMap();
            String featureValue = feature.getValue().toString();
            return ne2tag.containsKey(featureValue) ? Collections.singletonList(new Feature("NameEntity<"+neListName+">", this.featureName)) : Collections.emptyList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    public void setDelimiterSymbol(String delimiterSymbol) {
        if (delimiterSymbol == null || delimiterSymbol.isEmpty() || delimiterSymbol.length() > 1)
            throw new IllegalArgumentException("Delimiter Symbol must contain exactly one character!");
        this.delimiterSymbol = delimiterSymbol;
    }
    */
}