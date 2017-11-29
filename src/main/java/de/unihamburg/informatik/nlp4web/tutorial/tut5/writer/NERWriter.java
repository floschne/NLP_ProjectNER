package de.unihamburg.informatik.nlp4web.tutorial.tut5.writer;

import de.unihamburg.informatik.nlp4web.tutorial.tut5.type.NEIOBAnnotation;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Level;
import org.uimafit.util.JCasUtil;

import java.util.*;

/**
 * Consumer to output gold/prediction pairs and to calculate statistics as performance measurements.
 */
public class NERWriter extends JCasConsumer_ImplBase {
	private static final String LS = System.lineSeparator();
	
	/**
	 * String used to identify a non-named entity
	 */
	public static final String PARAM_NULL_TYPE = "Null type";
	
	/**
	 * Expected number of entity types
	 */
	public static final String PARAM_EXPECTED_ENTITY_TYPE_NUM = "Expected entity type num";
	
	/**
	 * Set for verbose output
	 */
	public static final String PARAM_VERBOSE = "Verbose";
	
	@ConfigurationParameter(name = PARAM_NULL_TYPE, mandatory = true)
	private String nullType = null;
	@ConfigurationParameter(name = PARAM_EXPECTED_ENTITY_TYPE_NUM, mandatory = false)
	private int expectedEntityTypeNum = 9;
	@ConfigurationParameter(name = PARAM_VERBOSE, mandatory = false)
	private boolean verbose = false;
	
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
		List<String> entityTypes = new ArrayList<>(expectedEntityTypeNum);
		// Maps pairs of gold/prediction entity types to the number of their joint appearance 
		Map<Pair<String, String>, Integer> classifications = new HashMap<>();
		// Stores the log
		StringBuilder s = new StringBuilder();
		s.append("-- Wrong NER Annotations --" + LS);
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
			// Append information onto log
			if (!goldType.equals(predType)) {
				s.append("Gold:\t" + goldType + "\t");
				s.append("Pred.:\t" + predType + "\t");
				s.append("(" + pred.getBegin() + ", " + pred.getEnd() + ") ");
				s.append(pred.getCoveredText() + " ");
				s.append(LS);
			}
		}
		s.append(LS);
		if (verbose)
			getContext().getLogger().log(Level.INFO, s.toString());
		// Compute statistics
		int truePositives = 0;
		int falsePositives = 0;
		int trueNegatives = 0;
		int falseNegatives = 0;
		int correct = 0;
		int all = 0;
		// Append statistics onto log
		s = new StringBuilder();
		s.append("-- Statistics --" + LS);
		s.append(LS);
		s.append("Classifications (correct classes on the left, predictions on top):" + LS);
		s.append("\t");
		for (String predType : entityTypes)
			s.append(predType + "\t");
		s.append(LS);
		for (String goldType : entityTypes) {
			s.append(goldType + "\t");
			for (String predType : entityTypes) {
				Pair<String, String> key = new Pair<>(goldType, predType);
				int num = classifications.containsKey(key) ? classifications.get(key) : 0;
				s.append(num + "\t");
				if (goldType.equals(nullType))
					if (predType.equals(nullType)) {
						trueNegatives += num;
						correct += num;
					}
					else
						falsePositives += num;
				else
					if (predType.equals(nullType))
						falseNegatives += num;
					else {
						truePositives += num;
						if (goldType.equals(predType))
							correct += num;
					}
				all += num;
			}
			s.append(LS);
		}
		s.append(LS);
		// Append aggregate statistics onto log
		s.append("Correct classifications:" + LS);
		s.append("  " + correct + "/" + all + " (" + (((double) correct * 100) / all) + "%)" + LS);
		s.append(LS);
		s.append("Aggregate classification results (correct on the left, predictions on top):" + LS);
		s.append("\tNE\tO" + LS);
		s.append("NE\t" + truePositives + "\t" + falseNegatives + LS);
		s.append("O\t" + falsePositives + "\t" + trueNegatives + LS);
		s.append(LS);
		int correctNEs = correct - trueNegatives;
		int NEs = all - trueNegatives - falsePositives;
		s.append("Correct classifications for tokens that are named entities:" + LS);
		s.append("  " + correctNEs + "/" + NEs + " (" + (((double) correctNEs * 100) / NEs) + "%)" + LS);
		s.append(LS);
		getContext().getLogger().log(Level.INFO, s.toString());
	}
}
