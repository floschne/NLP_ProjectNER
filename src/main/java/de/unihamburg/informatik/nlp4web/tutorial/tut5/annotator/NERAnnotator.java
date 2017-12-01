package de.unihamburg.informatik.nlp4web.tutorial.tut5.annotator;

import com.thoughtworks.xstream.XStream;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unihamburg.informatik.nlp4web.tutorial.tut5.feature.FeatureExtractorFactory;
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

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        // instantiate and add feature extractors
        if (featureExtractionFile == null) {
            try {
                featureExtractors = FeatureExtractorFactory.createAllFeatureExtractors();
            } catch (IOException e) {
                e.printStackTrace();
            }
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