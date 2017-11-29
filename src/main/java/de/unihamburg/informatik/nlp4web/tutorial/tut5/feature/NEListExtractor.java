package de.unihamburg.informatik.nlp4web.tutorial.tut5.feature;

import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.function.FeatureFunction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Feature extractor that extracts NE features from a textual list of NE features
 */
public class NEListExtractor implements FeatureFunction {

    private final String neListName;
    private final String featureName;
    private Set<String> namedEntities;

    public NEListExtractor(String neListName, String featureName) throws IOException {
        if (neListName == null || neListName.isEmpty() || !new File(neListName).exists())
            throw new IllegalArgumentException("Provide a valid, non-empty path to the list of NE features");
        this.neListName = neListName;

        if (featureName == null || featureName.isEmpty())
            throw new IllegalArgumentException("Please provide a valid name for the NE feature!");
        this.featureName = featureName;

        this.namedEntities = null;
    }

    /**
     * Generates the map from NE to their corresponding tag.
     *
     * @throws IOException if file not found or error while readings
     */
    private void generateNeToTagMap() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(this.neListName))) {
            this.namedEntities = new HashSet<>();
            String neToken = null;
            while ((neToken = reader.readLine()) != null)
                this.namedEntities.add(neToken.toLowerCase());
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
            if (this.namedEntities == null)
                this.generateNeToTagMap();
            String featureValue = feature.getValue().toString().toLowerCase();
            return namedEntities.contains(featureValue) ? Collections.singletonList(new Feature("NamedEntityList<" + neListName + ">", this.featureName)) : Collections.emptyList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}