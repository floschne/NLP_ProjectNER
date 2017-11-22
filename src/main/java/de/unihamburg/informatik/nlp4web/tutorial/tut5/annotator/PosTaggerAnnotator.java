package de.unihamburg.informatik.nlp4web.tutorial.tut5.annotator;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.Instance;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Following;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.ml.feature.extractor.TypePathExtractor;
import org.cleartk.ml.feature.function.CapitalTypeFeatureFunction;
import org.cleartk.ml.feature.function.CharacterNgramFeatureFunction;
import org.cleartk.ml.feature.function.CharacterNgramFeatureFunction.Orientation;
import org.cleartk.ml.feature.function.FeatureFunctionExtractor;
import org.cleartk.ml.feature.function.LowerCaseFeatureFunction;
import org.cleartk.ml.feature.function.NumericTypeFeatureFunction;

import com.thoughtworks.xstream.XStream;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unihamburg.informatik.nlp4web.tutorial.tut5.xml.XStreamFactory;

public class PosTaggerAnnotator extends CleartkSequenceAnnotator<String> {

	public static final String PARAM_FEATURE_EXTRACTION_FILE = "FeatureExtractionFile";

	/**
	 * if a feature extraction/context extractor filename is given the xml file is
	 * parsed and the features are used, otherwise it will not be used
	 */
	@ConfigurationParameter(name = PARAM_FEATURE_EXTRACTION_FILE, mandatory = false)
	private String featureExtractionFile = null;

	private FeatureExtractor1<Token> tokenFeatureExtractor;

	private CleartkExtractor<Token, Token> contextFeatureExtractor;
	private TypePathExtractor<Token> stemExtractor;

	List<FeatureExtractor1<Token>> features = new ArrayList<>();

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		// add feature extractors
		if (featureExtractionFile == null) {
			CharacterNgramFeatureFunction.Orientation fromRight = Orientation.RIGHT_TO_LEFT;

			stemExtractor = new TypePathExtractor<Token>(Token.class, "stem/value");

			this.tokenFeatureExtractor = new FeatureFunctionExtractor<Token>(new CoveredTextExtractor<Token>(),
					new LowerCaseFeatureFunction(), new CapitalTypeFeatureFunction(), new NumericTypeFeatureFunction(),
					new CharacterNgramFeatureFunction(fromRight, 0, 2));

			this.contextFeatureExtractor = new CleartkExtractor<Token, Token>(Token.class,
					new CoveredTextExtractor<Token>(), new Preceding(2), new Following(2));

			features.add(stemExtractor);
			features.add(tokenFeatureExtractor);
			features.add(contextFeatureExtractor);

		} else {// load the settings from a file
				// initialize the XStream if a xml file is given:
			XStream xstream = XStreamFactory.createXStream();
			features = (List<FeatureExtractor1<Token>>) xstream.fromXML(new File(featureExtractionFile));
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		for (Sentence sentence : select(jCas, Sentence.class)) {
			List<Instance<String>> instances = new ArrayList<Instance<String>>();
			List<Token> tokens = selectCovered(jCas, Token.class, sentence);
			for (Token token : tokens) {

				Instance<String> instance = new Instance<String>();

				for (FeatureExtractor1<Token> extractor : this.features) {
					if (extractor instanceof CleartkExtractor) {
						instance.addAll(
								(((CleartkExtractor<Token, Token>) extractor).extractWithin(jCas, token, sentence)));
					} else {
						instance.addAll(extractor.extract(jCas, token));
					}
				}

				if (this.isTraining()) {
					instance.setOutcome(token.getPos().getPosValue());
				}
				// add the instance to the list !!!
				instances.add(instance);
			}
			// differentiate between training and classifying
			if (this.isTraining()) {
				this.dataWriter.write(instances);
			} else {
				List<String> posTags = this.classify(instances);
				int i = 0;
				for (Token token : tokens) {
					POS pos = new POS(jCas, token.getBegin(), token.getEnd());
					pos.setPosValue(posTags.get(i++));
					token.setPos(pos);
				}
			}
		}
	}
}
