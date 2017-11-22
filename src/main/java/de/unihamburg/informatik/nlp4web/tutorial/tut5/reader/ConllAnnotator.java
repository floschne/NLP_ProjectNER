package de.unihamburg.informatik.nlp4web.tutorial.tut5.reader;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class ConllAnnotator extends JCasAnnotator_ImplBase {
	public static final String CONLL_VIEW = "ConnlView";
	private Logger logger = null;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		logger = context.getLogger();
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		JCas docView;
		String tbText;
		try {
			docView = jcas.getView(CAS.NAME_DEFAULT_SOFA);
			tbText = jcas.getView(CONLL_VIEW).getDocumentText();
		} catch (CASException e) {
			throw new AnalysisEngineProcessException(e);
		}
		// a new sentence always starts with a new line
		if (tbText.charAt(0) != '\n') {
			tbText = "\n" + tbText;
		}

		String[] tokens = tbText.split("(\r\n|\n)");
		Sentence sentence = null;
		int idx = 0;
		Token token = null;
		POS posTag;
		String pos;
		boolean initSentence = false;
		StringBuffer docText = new StringBuffer();
		for (String line : tokens) {
			// new sentence if there's a new line
			if (line.equals("")) {
				if (sentence != null && token != null) {
					terminateSentence(sentence, token, docText);
					docText.append("\n");
					idx++;
				}
				//init new sentence with the next recognized token
				initSentence = true;
			} else {
				String[] tag = line.split("\\s");
				String word = tag[0];
				pos = tag.length >= 2 ? tag[1] : "";
				docText.append(word);
				if (!word.matches("^(\\p{Punct}).*")) {
					token = new Token(docView, idx, idx + word.length());
					posTag = new POS(docView, idx, idx + word.length());

					docText.append(" ");
					idx++;
				} else {
					if ((docText.length() - word.length()) > 0
							&& (docText.charAt(idx - word.length()) == ' ')) {
						docText.deleteCharAt(idx - word.length());
						idx--;
					}
					token = new Token(docView, idx, idx + word.length());
					posTag = new POS(docView, idx, idx + word.length());
				}
				//start new sentence
				if (initSentence) {
					sentence = new Sentence(docView);
					sentence.setBegin(token.getBegin());
					initSentence = false;
				}
				//increment actual index of text
				idx += word.length();
				//set POS value and add POS to the token and to the index
				posTag.setPosValue(pos);
				token.setPos(posTag);
				token.addToIndexes();
				logger.log(
						Level.FINE,
						"Token: ["
								+ docText.substring(token.getBegin(),
										token.getEnd()) + "]"
								+ token.getBegin() + "\t" + token.getEnd());

			}
		}
		if (sentence != null && token != null) {
			terminateSentence(sentence, token, docText);
		}

		docView.setSofaDataString(docText.toString(), "text/plain");
	}

	private void terminateSentence(Sentence sentence, Token token,StringBuffer docText){
		sentence.setEnd(token.getEnd());
		sentence.addToIndexes();
		logger.log(
				Level.FINE,
				"Sentence:["
						+ docText.substring(sentence.getBegin(),
								sentence.getEnd()) + "]\t"
						+ sentence.getBegin() + "\t" + sentence.getEnd());
	}

}
