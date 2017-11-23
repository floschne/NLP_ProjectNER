package de.unihamburg.informatik.nlp4web.tutorial.tut5.annotator;

import com.thoughtworks.xstream.XStream;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unihamburg.informatik.nlp4web.tutorial.tut5.feature.NEListExtractor;
import de.unihamburg.informatik.nlp4web.tutorial.tut5.type.NEIOBAnnotation;
import de.unihamburg.informatik.nlp4web.tutorial.tut5.xml.XStreamFactory;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.Instance;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.ml.feature.extractor.TypePathExtractor;
import org.cleartk.ml.feature.function.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;


public class NERAnnotator extends CleartkSequenceAnnotator<String> {

    public static final String PARAM_FEATURE_EXTRACTION_FILE = "FeatureExtractionFile";

    /**
     * if a feature extraction/context extractor filename is given the xml file
     * is parsed and the featureExtractors are used, otherwise it will not be used
     */
    @ConfigurationParameter(name = PARAM_FEATURE_EXTRACTION_FILE, mandatory = false)
    private String featureExtractionFile = null;

    private List<FeatureExtractor1<Token>> featureExtractors = new ArrayList<>();

    /*
     * CharacterCategoryPatternFunction -> good feature extractor for NER (hint by tutor)
     */

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        // instantiate and add feature extractors
        if (featureExtractionFile == null) {
            TypePathExtractor<Token> stemExtractor = new TypePathExtractor<>(Token.class, "stem/value");

            // create a function feature extractor that creates features corresponding to the token
            // Note the difference between feature extractors and feature functions here. Feature extractors take an Annotation
            // from the JCas and extract features from it. Feature functions take the features produced by the feature extractor
            // and generate new features from the old ones. Since feature functions don’t need to look up information in the JCas,
            // they may be more efficient than feature extractors. So, the e.g. the CharacterNgramFeatureFunction simply extract
            // suffixes from the text returned by the CoveredTextExtractor.
            FeatureExtractor1<Token> tokenFeatureExtractor = new FeatureFunctionExtractor<>(
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
                    new CharacterNgramFeatureFunction(CharacterNgramFeatureFunction.Orientation.RIGHT_TO_LEFT, 0, 3));

            // create a feature extractor that extracts the surrounding token texts (within the same sentence)
            CleartkExtractor<Token, Token> contextFeatureExtractor = new CleartkExtractor<>(Token.class,
                    // the FeatureExtractor that takes the token annotation from the JCas and produces the covered text
                    new CoveredTextExtractor<>(),
                    // also include the two preceding words
                    new CleartkExtractor.Preceding(2),
                    // and the two following words
                    new CleartkExtractor.Following(2));


            // create the custom feature extractors
            try {
                FeatureFunctionExtractor personName = new FeatureFunctionExtractor<>(
                        new NEListExtractor(new File("src/main/resources/ner/fullNames.txt"), "personName"));
                FeatureFunctionExtractor surName = new FeatureFunctionExtractor<>(
                        new NEListExtractor(new File("src/main/resources/ner/foreNames.txt"), "foreName"));
                FeatureFunctionExtractor foreName = new FeatureFunctionExtractor<>(
                        new NEListExtractor(new File("src/main/resources/ner/surNames.txt"), "surName"));
                FeatureFunctionExtractor germanCityName = new FeatureFunctionExtractor<>(
                        new NEListExtractor(new File("src/main/resources/ner/germanCityNames.txt"), "cityName"));
                FeatureFunctionExtractor germanCountryName = new FeatureFunctionExtractor<>(
                        new NEListExtractor(new File("src/main/resources/ner/germanCountryNames.txt"), "cityName"));
                FeatureFunctionExtractor englishCountryName = new FeatureFunctionExtractor<>(
                        new NEListExtractor(new File("src/main/resources/ner/englishCountryNames.txt"), "countryName"));
                FeatureFunctionExtractor organizationName = new FeatureFunctionExtractor<>(
                        new NEListExtractor(new File("src/main/resources/ner/germanOrganizationNames.txt"), "organizationName"));

                featureExtractors.add(personName);
                featureExtractors.add(surName);
                featureExtractors.add(foreName);
                featureExtractors.add(germanCityName);
                featureExtractors.add(germanCountryName);
                featureExtractors.add(englishCountryName);
                featureExtractors.add(organizationName);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // add all the extractors to the list of feature extractors that'll be used in the process method
            featureExtractors.add(stemExtractor);
            featureExtractors.add(tokenFeatureExtractor);
            featureExtractors.add(contextFeatureExtractor);
        } else {
            // load the settings from a file
            // initialize the XStream if a xml file is given:
            XStream xstream = XStreamFactory.createXStream();
            featureExtractors = (List<FeatureExtractor1<Token>>) xstream.fromXML(new File(featureExtractionFile));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        //iterate over all sentences in the document (represented by the JCas instance)
        for (Sentence sentence : select(jCas, Sentence.class)) {

            // list of instances -> instance is just a list of features represented by (in this case by T=) String
            List<Instance<String>> instances = new ArrayList<>();

            // iterate over all tokens in the sentence
            List<Token> tokensInSentence = selectCovered(jCas, Token.class, sentence);
            for (Token token : tokensInSentence) {

                //the features of the token
                Instance<String> instance = new Instance<>();

                // apply all feature extractors on the token
                for (FeatureExtractor1<Token> extractor : this.featureExtractors) {
                    // special handling of ClearTkExtractors
                    if (extractor instanceof CleartkExtractor) {
                        instance.addAll(
                                (((CleartkExtractor<Token, Token>) extractor).extractWithin(jCas, token, sentence)));
                    } else {
                        instance.addAll(extractor.extract(jCas, token));
                    }
                }

                // TRAINING
                if (this.isTraining()) {
                    // get the labeled annotation of the token (from the training data)
                    NEIOBAnnotation goldNE = JCasUtil.selectCovered(jCas, NEIOBAnnotation.class, token).get(0);
                    // assign the the label (aka gold value ) to the list of features
                    instance.setOutcome(goldNE.getGoldValue());
                }

                // add the instance to the list
                instances.add(instance);
            }

            // TRAINING -> serialize instances (features of tokens) for later classification
            if (this.isTraining())
                this.dataWriter.write(instances);
            else {
                // CLASSIFYING -> classify the NE annotations for the tokens based the (before) gathered features of
                // the tokens(= instances)
                List<String> namedEntities = this.classify(instances);
                int i = 0;
                for (Token token : tokensInSentence) {
                    // create the NE annotation for the token
                    NEIOBAnnotation namedEntity = new NEIOBAnnotation(jCas, token.getBegin(), token.getEnd());
                    // set the predicted NE classification for the token
                    namedEntity.setPredictValue(namedEntities.get(i++));
                    // add annotation to JCas indexes
                    namedEntity.addToIndexes();
                }
            }
        }
    }
}