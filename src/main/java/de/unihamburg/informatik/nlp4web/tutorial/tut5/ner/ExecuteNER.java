package de.unihamburg.informatik.nlp4web.tutorial.tut5.ner;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.cleartk.ml.crfsuite.CrfSuiteStringOutcomeDataWriter;
import org.cleartk.ml.jar.DefaultDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.util.cr.FilesCollectionReader;

import de.tudarmstadt.ukp.dkpro.core.snowball.SnowballStemmer;
import de.unihamburg.informatik.nlp4web.tutorial.tut5.annotator.NERAnnotator;
import de.unihamburg.informatik.nlp4web.tutorial.tut5.reader.NERReader;
import de.unihamburg.informatik.nlp4web.tutorial.tut5.writer.NERWriter;

public class ExecuteNER {

    /**
     * @param posTagFile
     * @param modelDirectory
     * @param language
     * @throws UIMAException
     * @throws IOException
     */
    public static void writeModel(File posTagFile, String modelDirectory, String language) throws UIMAException, IOException {

        CollectionReader posTagFileReader = FilesCollectionReader.getCollectionReaderWithSuffixes(
                posTagFile.getAbsolutePath(), NERReader.CONLL_VIEW, posTagFile.getName());

        AnalysisEngine snowballStemmer = createEngine(SnowballStemmer.class, SnowballStemmer.PARAM_LANGUAGE, language);

        AnalysisEngine nerAnnotator = createEngine(NERAnnotator.class,
                NERAnnotator.PARAM_FEATURE_EXTRACTION_FILE, "src/main/resources/feature/features.xml",
                NERAnnotator.PARAM_IS_TRAINING, true,
                DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY, modelDirectory,
                DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME, CrfSuiteStringOutcomeDataWriter.class);

        runPipeline(
                posTagFileReader,
                createEngine(NERReader.class),
                snowballStemmer,
                nerAnnotator
        );
    }

	public static void trainModel(String modelDirectory) throws Exception {
		org.cleartk.ml.jar.Train.main(modelDirectory);
	}

	public static void classifyTestFile(String modelDirectory, File testPosFile, String language)
			throws ResourceInitializationException, UIMAException, IOException {

		CollectionReader testPosFileReader = FilesCollectionReader.getCollectionReaderWithSuffixes(testPosFile.getAbsolutePath(),
				NERReader.CONLL_VIEW, testPosFile.getName());

		AnalysisEngine nerReader = createEngine(NERReader.class);
		AnalysisEngine snowballStemmer = createEngine(SnowballStemmer.class, SnowballStemmer.PARAM_LANGUAGE, language);
		AnalysisEngine nerAnnotator = createEngine(NERAnnotator.class,
													NERAnnotator.PARAM_FEATURE_EXTRACTION_FILE,
													"src/main/resources/feature/features.xml",
													GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, modelDirectory + "model.jar");
		AnalysisEngine nerWriter = createEngine(NERWriter.class,
		NERWriter.PARAM_NULL_TYPE, "O",
				NERWriter.PARAM_EXPECTED_ENTITY_TYPE_NUM, 9,
				NERWriter.PARAM_VERBOSE, true);

		runPipeline(
				testPosFileReader,
				nerReader,
				snowballStemmer,
				nerAnnotator,
				nerWriter);
	}

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();
        long now = 0;
        String modelDirectory = "src/test/resources/model/";
        String language = "en";
        File nerTrain = new File("src/main/resources/ner/ner_eng.train");
        File nerTest = new File("src/main/resources/ner/ner_eng.dev");
        new File(modelDirectory).mkdirs();
        now = System.currentTimeMillis();
        UIMAFramework.getLogger().log(Level.INFO, "Starting 'writing model' Time: " + (now - start) + "ms");
        writeModel(nerTrain, modelDirectory, language);
        now = System.currentTimeMillis();
        UIMAFramework.getLogger().log(Level.INFO, "Starting 'training model' Time: " + (now - start) + "ms");
        trainModel(modelDirectory);
        now = System.currentTimeMillis();
        UIMAFramework.getLogger().log(Level.INFO, "Starting 'classifying model' Time: " + (now - start) + "ms");
        classifyTestFile(modelDirectory, nerTest, language);
        now = System.currentTimeMillis();
        UIMAFramework.getLogger().log(Level.INFO, "All done! Time: " + (now - start) + "ms");
    }
}