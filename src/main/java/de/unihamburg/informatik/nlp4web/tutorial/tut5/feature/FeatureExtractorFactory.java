package de.unihamburg.informatik.nlp4web.tutorial.tut5.feature;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.ml.feature.extractor.TypePathExtractor;
import org.cleartk.ml.feature.function.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to instantiate the selected Feature Extractors
 * (I know this is not a 'real Factory' in terms of GoF)
 */
public class FeatureExtractorFactory {

    /**
     * Creates all the features extractors that will be used. To remove code redundancy the method is public static and
     * therefore accessible in the Features2Xml class
     *
     * @return the list of feature extractors
     */
    public static List<FeatureExtractor1<Token>> createAllFeatureExtractors() throws IOException {
        //create all feature extractors
        List<FeatureExtractor1<Token>> allFeatureExtractors = new ArrayList<>();
        TypePathExtractor<Token> stemExtractor = FeatureExtractorFactory.createTokenTypePathExtractors();
        FeatureExtractor1<Token> tokenFeatureExtractor = FeatureExtractorFactory.createTokenFeatureExtractors();
        CleartkExtractor<Token, Token> contextFeatureExtractor = FeatureExtractorFactory.createTokenContextExtractors();
        FeatureFunctionExtractor nameListExtractors = FeatureExtractorFactory.createNameListExtractors();
        FeatureFunctionExtractor cityListExtractors = FeatureExtractorFactory.createCityListExtractors();
        FeatureFunctionExtractor countryListExtractors = FeatureExtractorFactory.createCountryListExtractors();
        FeatureFunctionExtractor miscListExtractors = FeatureExtractorFactory.createMiscListExtractors();
        FeatureFunctionExtractor orgListExtractors = FeatureExtractorFactory.createOrgListExtractors();
        FeatureFunctionExtractor locListExtractors = FeatureExtractorFactory.createLocListExtractors();
        allFeatureExtractors.add(stemExtractor);
        allFeatureExtractors.add(tokenFeatureExtractor);
        allFeatureExtractors.add(contextFeatureExtractor);
        allFeatureExtractors.add(nameListExtractors);
        allFeatureExtractors.add(cityListExtractors);
        allFeatureExtractors.add(countryListExtractors);
        allFeatureExtractors.add(miscListExtractors);
        allFeatureExtractors.add(orgListExtractors);
        allFeatureExtractors.add(locListExtractors);

        return allFeatureExtractors;
    }

    private static FeatureFunctionExtractor createLocListExtractors() throws IOException {
        return new FeatureFunctionExtractor<>(
                        new CoveredTextExtractor<Token>(),
                        FeatureFunctionExtractor.BaseFeatures.EXCLUDE,
                        new NEListExtractor("src/main/resources/ner/eng_LOC.txt", "eng_LOC"),
                        new NEListExtractor("src/main/resources/ner/deu_LOC.txt", "deu_LOC"));
    }

    private static FeatureFunctionExtractor createOrgListExtractors() throws IOException {
        return new FeatureFunctionExtractor<>(
                        new CoveredTextExtractor<Token>(),
                        FeatureFunctionExtractor.BaseFeatures.EXCLUDE,
                        new NEListExtractor("src/main/resources/ner/eng_ORG.txt", "eng_ORG"),
                        new NEListExtractor("src/main/resources/ner/deu_ORG.txt", "deu_ORG"));
    }

    private static FeatureFunctionExtractor createMiscListExtractors() throws IOException {
        return new FeatureFunctionExtractor<>(
                        new CoveredTextExtractor<Token>(),
                        FeatureFunctionExtractor.BaseFeatures.EXCLUDE,
                        new NEListExtractor("src/main/resources/ner/eng_MISC.txt", "eng_MISC"),
                        new NEListExtractor("src/main/resources/ner/deu_MISC.txt", "deu_MISC"));
    }

    private static FeatureFunctionExtractor createCountryListExtractors() throws IOException {
        return new FeatureFunctionExtractor<>(
                        new CoveredTextExtractor<Token>(),
                        FeatureFunctionExtractor.BaseFeatures.EXCLUDE,
                        new NEListExtractor("src/main/resources/ner/germanCountryNames.txt", "gerCountry_LOC"),
                        new NEListExtractor("src/main/resources/ner/englishCountryNames.txt", "engCountry_LOC"));
    }

    private static FeatureFunctionExtractor createCityListExtractors() throws IOException {
        return new FeatureFunctionExtractor<>(
                        new CoveredTextExtractor<Token>(),
                        FeatureFunctionExtractor.BaseFeatures.EXCLUDE,
                        new NEListExtractor("src/main/resources/ner/germanCityNames.txt", "gerCity_LOC"),
                        new NEListExtractor("src/main/resources/ner/englishCityNames.txt", "engCity_LOC"));
    }

    private static FeatureFunctionExtractor createNameListExtractors() throws IOException {
        return new FeatureFunctionExtractor<>(
                        new CoveredTextExtractor<Token>(),
                        FeatureFunctionExtractor.BaseFeatures.EXCLUDE,
                        new NEListExtractor("src/main/resources/ner/firstNames.txt", "firstName_PER"),
                        new NEListExtractor("src/main/resources/ner/lastNames.txt", "lastName_PER"));
    }

    private static CleartkExtractor<Token, Token> createTokenContextExtractors() {
        // create a feature extractor that extracts the surrounding token texts (within the same sentence)
        return new CleartkExtractor<>(Token.class,
                // the FeatureExtractor that takes the token annotation from the JCas and produces the covered text
                new CoveredTextExtractor<>(),
                // also include the two preceding words
                new CleartkExtractor.Preceding(2),
                // and the two following words
                new CleartkExtractor.Following(2));
    }

    private static FeatureExtractor1<Token> createTokenFeatureExtractors() {
        // create a function feature extractor that creates features corresponding to the token
        // Note the difference between feature extractors and feature functions here. Feature extractors take an Annotation
        // from the JCas and extract features from it. Feature functions take the features produced by the feature extractor
        // and generate new features from the old ones. Since feature functions don’t need to look up information in the JCas,
        // they may be more efficient than feature extractors. So, the e.g. the CharacterNgramFeatureFunction simply extract
        // suffixes from the text returned by the CoveredTextExtractor.
        return new FeatureFunctionExtractor<>(
                // the FeatureExtractor that takes the token annotation from the JCas and produces the covered text
                new CoveredTextExtractor<Token>(),
                // feature function that produces the lower cased word (based on the output of the CoveredTextExtractor)
                new LowerCaseFeatureFunction(),
                // feature function that produces the capitalization type of the word (e.g. all uppercase, all lowercase...)
                new CapitalTypeFeatureFunction(),
                // feature function that produces the numeric type of the word (numeric, alphanumeric...)
                new NumericTypeFeatureFunction(),
                // feature function that produces the suffix of the word as character bigram (last two chars of the word)
                new CharacterNgramFeatureFunction(CharacterNgramFeatureFunction.Orientation.RIGHT_TO_LEFT, 0, 2),
                // feature function that produces the suffix of the word as character trigram (last three chars of the word)
                new CharacterNgramFeatureFunction(CharacterNgramFeatureFunction.Orientation.RIGHT_TO_LEFT, 0, 3),
                // feature function that produces the Character Category Pattern (based on the Unicode Categories) for the Token
                new CharacterCategoryPatternFunction());
    }

    private static TypePathExtractor<Token> createTokenTypePathExtractors() {
        return new TypePathExtractor<>(Token.class, "stem/value");
    }
}
