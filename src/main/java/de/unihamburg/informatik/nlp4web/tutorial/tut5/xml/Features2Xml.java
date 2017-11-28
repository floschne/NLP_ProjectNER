package de.unihamburg.informatik.nlp4web.tutorial.tut5.xml;

import com.thoughtworks.xstream.XStream;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unihamburg.informatik.nlp4web.tutorial.tut5.feature.NEListExtractor;
import org.apache.uima.UIMAFramework;
import org.apache.uima.util.Level;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.ml.feature.extractor.TypePathExtractor;
import org.cleartk.ml.feature.function.*;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Features2Xml {
	public static void generateTokenFeatureExtractors(String filename) throws IOException {

	    List<FeatureExtractor1<Token>> featureExtractors  = new ArrayList<>();

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
            FeatureExtractor1<Token> personNames = new FeatureFunctionExtractor<>(
                    new CoveredTextExtractor<Token>(),
                    FeatureFunctionExtractor.BaseFeatures.EXCLUDE,
                    new NEListExtractor("src/main/resources/ner/fullNames.txt", "PER"));

            FeatureExtractor1<Token> foreNames = new FeatureFunctionExtractor<>(
                    new CoveredTextExtractor<Token>(),
                    FeatureFunctionExtractor.BaseFeatures.EXCLUDE,
                    new NEListExtractor("src/main/resources/ner/foreNames.txt", "PER"));

            FeatureExtractor1<Token> surNames = new FeatureFunctionExtractor<>(
                    new CoveredTextExtractor<Token>(),
                    FeatureFunctionExtractor.BaseFeatures.EXCLUDE,
                    new NEListExtractor("src/main/resources/ner/surNames.txt", "PER"));

            FeatureExtractor1<Token> germanCityNames = new FeatureFunctionExtractor<>(
                    new CoveredTextExtractor<Token>(),
                    FeatureFunctionExtractor.BaseFeatures.EXCLUDE,
                    new NEListExtractor("src/main/resources/ner/germanCityNames.txt", "LOC"));

            FeatureExtractor1<Token> germanCountryNames = new FeatureFunctionExtractor<>(
                    new CoveredTextExtractor<Token>(),
                    FeatureFunctionExtractor.BaseFeatures.EXCLUDE,
                    new NEListExtractor("src/main/resources/ner/germanCountryNames.txt", "LOC"));

            FeatureExtractor1<Token> englishCountryNames = new FeatureFunctionExtractor<>(
                    new CoveredTextExtractor<Token>(),
                    FeatureFunctionExtractor.BaseFeatures.EXCLUDE,
                    new NEListExtractor("src/main/resources/ner/englishCountryNames.txt", "LOC"));

            FeatureExtractor1<Token> germanOrganizationNames = new FeatureFunctionExtractor<>(
                    new CoveredTextExtractor<Token>(),
                    FeatureFunctionExtractor.BaseFeatures.EXCLUDE,
                    new NEListExtractor("src/main/resources/ner/germanOrganizationNames.txt", "LOC"));

            featureExtractors.add(personNames);
            featureExtractors.add(surNames);
            featureExtractors.add(foreNames);
            featureExtractors.add(germanCityNames);
            featureExtractors.add(germanCountryNames);
            featureExtractors.add(englishCountryNames);
            featureExtractors.add(germanOrganizationNames);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }


        // add all the extractors to the list of feature extractors that'll be serialized in the XML
        featureExtractors.add(stemExtractor);
        featureExtractors.add(tokenFeatureExtractor);
        featureExtractors.add(contextFeatureExtractor);
        

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
	 * @param x
	 * @return
	 */
	private static String removeLogger(String x) {
		StringBuffer buffer = new StringBuffer();
		String[] lines=x.split("\n");
		boolean loggerFound=false;
		for(String l:lines){
			if(l.trim().startsWith("<logger>")){
				loggerFound=true;
			}
			if(!loggerFound){
				buffer.append(l);
				buffer.append("\n");
			}else{
				if(l.trim().startsWith("</logger>")){
					loggerFound=false;
				}
			}
		}

		return buffer.toString();
	}

	public static void main(String[] args) throws IOException {
        UIMAFramework.getLogger().log(Level.INFO, "Writing features.xml file!");
        String featureFileName="src/main/resources/feature/features.xml";
        generateTokenFeatureExtractors(featureFileName);
        UIMAFramework.getLogger().log(Level.INFO, "Done: " + featureFileName);
    }
}
