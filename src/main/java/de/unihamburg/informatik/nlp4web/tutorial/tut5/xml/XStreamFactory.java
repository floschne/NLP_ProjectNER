package de.unihamburg.informatik.nlp4web.tutorial.tut5.xml;

import java.util.ArrayList;

import org.cleartk.ml.feature.FeatureCollection;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Bag;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Covered;
import org.cleartk.ml.feature.extractor.CleartkExtractor.FirstCovered;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Following;
import org.cleartk.ml.feature.extractor.CleartkExtractor.LastCovered;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Ngram;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.ml.feature.extractor.CombinedExtractor1;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.extractor.DirectedDistanceExtractor;
import org.cleartk.ml.feature.extractor.DistanceExtractor;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.ml.feature.extractor.FeatureExtractor2;
import org.cleartk.ml.feature.extractor.NamedFeatureExtractor1;
import org.cleartk.ml.feature.extractor.NamingExtractor1;
import org.cleartk.ml.feature.extractor.RelativePositionExtractor;
import org.cleartk.ml.feature.extractor.TypePathExtractor;
import org.cleartk.ml.feature.extractor.WhiteSpaceExtractor;

import com.thoughtworks.xstream.XStream;

public class XStreamFactory {
	public static XStream createXStream() {
		//define alias so the xml file can be read easier
		XStream xstream = new XStream();
		// org.cleartk.classifier.feature.*
		xstream.alias("TypePathExtractor", TypePathExtractor.class);
		xstream.alias("FeatureCollection", FeatureCollection.class);

		// org.cleartk.ml.feature.extractor.*
		xstream.alias("CleartkExtractor", CleartkExtractor.class);
		xstream.alias("CombinedExtractor1", CombinedExtractor1.class);
		xstream.alias("CoveredTextExtractor", CoveredTextExtractor.class);
		xstream.alias("DirectedDistanceExtractor", DirectedDistanceExtractor.class);
        xstream.alias("DistanceExtractor", DistanceExtractor.class);
        xstream.alias("FeatureExtractor1", FeatureExtractor1.class);
        xstream.alias("FeatureExtractor2", FeatureExtractor2.class);
        xstream.alias("NamedFeatureExtractor1", NamedFeatureExtractor1.class);
        xstream.alias("NamingExtractor1", NamingExtractor1.class);
        xstream.alias("RelativePositionExtractor", RelativePositionExtractor.class);
        xstream.alias("WhiteSpaceExtractor", WhiteSpaceExtractor.class);


		// within CleartkExtractor
		xstream.alias("Bag", Bag.class);
		xstream.alias("Preceding", Preceding.class);
		xstream.alias("Following", Following.class);
		xstream.alias("Covered", Covered.class);
		xstream.alias("FirstCovered", FirstCovered.class);
		xstream.alias("LastCovered", LastCovered.class);
		xstream.alias("Ngram", Ngram.class);

		xstream.alias("list", ArrayList.class);
		return xstream;
	}
}
