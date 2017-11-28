package de.unihamburg.informatik.nlp4web.tutorial.tut5.writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Level;
import org.uimafit.util.JCasUtil;

import de.unihamburg.informatik.nlp4web.tutorial.tut5.type.NEIOBAnnotation;

/**
 * Consumer to output gold/prediction pairs and to calculate statistics as performance measurements.
 */
public class NERWriter extends JCasConsumer_ImplBase {
	private static final String LS = System.lineSeparator();
	private static final int EXPECTED_ENTITY_TYPE_NUM = 9;

	/**
	 * Helper class to be used in a HashMap.
	 * @param <A> the first element of the pair
	 * @param <B> the second element of the pair
	 */
	private class Pair<A, B> {
		private A a;
		private B b;
		
		private Pair(A a, B b) {
			this.a = a;
			this.b = b;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof Pair) {
				Pair<?, ?> p = (Pair<?, ?>) o;
				return this.a.equals(p.a) && this.b.equals(p.b);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return a.hashCode() + b.hashCode();
		}
	}
	
	/**
	 * Processes the JCas to write gold/prediction pairs to the log and to calculate statistics.
	 * @param aJCas a JCas
	 */
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// Lists all seen entity types
		List<String> entityTypes = new ArrayList<>(EXPECTED_ENTITY_TYPE_NUM);
		// Maps pairs of gold/prediction entity types to the number of their joint appearance 
		Map<Pair<String, String>, Integer> classifications = new HashMap<>();
		// Stores the log
		StringBuilder s = new StringBuilder();
		s.append("-- NER Annotations --");
		s.append(LS);
		Iterator<NEIOBAnnotation> it = JCasUtil.select(aJCas, NEIOBAnnotation.class).iterator();
		while (it.hasNext()) {
			/* When classifying, NERReader and NERAnnotator create NEIOBAnnotations independently.
			 * One is used to store the gold value, the other one to store the predicted value.
			 * This setup assures that classification can also be done when not in a test situation.
			 * Here, both pieces of information are combined. */
			NEIOBAnnotation pred = it.next();
			if (pred.getPredictValue() == null)
				continue; // Only consider predicted values
			NEIOBAnnotation gold = null;
			for (NEIOBAnnotation annotation : JCasUtil.selectCovered(aJCas, NEIOBAnnotation.class, pred)) {
				if (annotation.getGoldValue() != null) {
					gold = annotation;
					break;
				}
			}
			if (gold == null) {
				System.err.println("No gold annotation found for "
						+ "[" + pred.getType().getShortName() + "] " + pred.getCoveredText() + " "
						+ "(" + pred.getBegin() + ", " + pred.getEnd() + ")");
				continue; // Only consider predictions for which there are gold values
			}
			String goldType = gold.getGoldValue();
			String predType = pred.getPredictValue();
			// Collect statistics
			if (!entityTypes.contains(goldType))
				entityTypes.add(goldType);
			if (!entityTypes.contains(predType))
				entityTypes.add(predType);
			Pair<String, String> key = new Pair<>(goldType, predType);
			if (classifications.containsKey(key))
				classifications.put(key, classifications.get(key) + 1);
			else
				classifications.put(key, 1);
			// TODO Compute aggregate statistics
			// Append information onto log
			s.append("Gold: " + goldType + "\t");
			s.append("Predicted: " + predType + "\t");
			s.append("(" + pred.getBegin() + ", " + pred.getEnd() + ") ");
			s.append(pred.getCoveredText() + " ");
			s.append(LS);
		}
		s.append(LS);
		// Append statistics onto log
		s.append("Classifications (correct classes on the left, predictions on top):" + LS);
		s.append("\t");
		for (String predType : entityTypes)
			s.append(predType + "\t");
		s.append(LS);
		for (String goldType : entityTypes) {
			s.append(goldType + "\t");
			for (String predType : entityTypes) {
				Pair<String, String> key = new Pair<>(goldType, predType);
				s.append((classifications.containsKey(key) ? classifications.get(key) : 0) + "\t");
			}
			s.append(LS);
		}
		// TODO Append aggregate statistics onto log
		s.append(LS);
		getContext().getLogger().log(Level.INFO, s.toString());
	}
}
