package de.unihamburg.informatik.nlp4web.tutorial.tut5.ner;

import de.tudarmstadt.ukp.dkpro.core.snowball.SnowballStemmer;
import de.unihamburg.informatik.nlp4web.tutorial.tut5.annotator.NERAnnotator;
import de.unihamburg.informatik.nlp4web.tutorial.tut5.reader.NERReader;
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

import java.io.File;
import java.io.IOException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

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

    /**
     * @param modelDirectory
     * @throws Exception
     */
    public static void trainModel(String modelDirectory) throws Exception {
        org.cleartk.ml.jar.Train.main(modelDirectory);
    }

    // TODO
    // The pipeline does not include the output writer. you SHOULD write a consumer
    // which extract the predicted Named entities. You can compute then the scores
    // accordingly

    /**
     * @param modelDirectory
     * @param testPosFile
     * @param language
     * @throws ResourceInitializationException
     * @throws UIMAException
     * @throws IOException
     */
    public static void classifyTestFile(String modelDirectory, File testPosFile, String language)
            throws ResourceInitializationException, UIMAException, IOException {

        CollectionReader testPosFileReader = FilesCollectionReader.getCollectionReaderWithSuffixes(testPosFile.getAbsolutePath(),
                NERReader.CONLL_VIEW, testPosFile.getName());

        AnalysisEngine nerReader = createEngine(NERReader.class);
        AnalysisEngine snowballStemmer = createEngine(SnowballStemmer.class, SnowballStemmer.PARAM_LANGUAGE, language);
        AnalysisEngine nerAnnotator = createEngine(NERAnnotator.class,
                NERAnnotator.PARAM_FEATURE_EXTRACTION_FILE, "src/main/resources/feature/features.xml",
                GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
                modelDirectory + "model.jar");

        runPipeline(
                testPosFileReader,
                nerReader,
                snowballStemmer,
                nerAnnotator/*
                             * , TODO: Replace this with your NER consumer
							 * createEngine(AnalyzeFeatures.class, AnalyzeFeatures.PARAM_INPUT_FILE,
							 * testPosFile.getAbsolutePath(),
							 * AnalyzeFeatures.PARAM_TOKEN_VALUE_PATH, "pos/PosValue")
							 */);
    }

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();
        String modelDirectory = "src/test/resources/model/";
        String language = "en";
        File nerTrain = new File("src/main/resources/ner/ner_eng.train");
        File nerTest = new File("src/main/resources/ner/ner_eng.dev");
        new File(modelDirectory).mkdirs();
        writeModel(nerTrain, modelDirectory, language);
        trainModel(modelDirectory);
        classifyTestFile(modelDirectory, nerTest, language);
        long now = System.currentTimeMillis();
        UIMAFramework.getLogger().log(Level.INFO, "Time: " + (now - start) + "ms");
    }
}