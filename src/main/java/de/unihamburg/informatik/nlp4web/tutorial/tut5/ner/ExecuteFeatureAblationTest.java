package de.unihamburg.informatik.nlp4web.tutorial.tut5.ner;

import de.tudarmstadt.ukp.dkpro.core.snowball.SnowballStemmer;
import de.unihamburg.informatik.nlp4web.tutorial.tut5.annotator.NERAnnotator;
import de.unihamburg.informatik.nlp4web.tutorial.tut5.reader.NERReader;
import de.unihamburg.informatik.nlp4web.tutorial.tut5.writer.NERWriter;
import de.unihamburg.informatik.nlp4web.tutorial.tut5.xml.Features2Xml;
import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.util.Level;
import org.cleartk.ml.crfsuite.CrfSuiteStringOutcomeDataWriter;
import org.cleartk.ml.jar.DefaultDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.util.cr.FilesCollectionReader;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

public class ExecuteFeatureAblationTest {

    private static final String EVALUATION_OUTPUT_DIRECTORY = "src/test/resources/evaluation/";
    private static final String FEATURE_EXTRACTOR_CONFIG_DIRECTORY = "src/main/resources/feature/";
    private static final String MODEL_DIRECTORY = "src/test/resources/model";
    private static final Integer MAX_THREADS = 4;
    private static final Integer MIN_NUMBER_OF_EXTRACTORS = 7;


    public static class AblationTestRunner implements Runnable {

        private String language;
        private File configFile;
        private File nerTrain;
        private File nerTest;
        private Integer id;

        /**
         * @param language the language of the document that'll be analysed and trained with
         * @param configFile the XML configuration file to instantiate the Feature Extractors
         * @param nerTrain training file
         * @param nerTest test file
         * @param id the id of the runner. this is used to store the generated models separate directories.
         */
        AblationTestRunner(String language, File configFile, File nerTrain, File nerTest, Integer id) {
            this.language = language;
            this.configFile = configFile;
            this.nerTrain = nerTrain;
            this.nerTest = nerTest;
            this.id = id;
        }


        /**
         * build the model directory path as a string. just to reduce code redundancy.
         */
        private String getModelDir() {
            return MODEL_DIRECTORY + this.id.toString() + "/";
        }
        /**
         * @param posTagFile
         * @param configFileName
         * @param language
         * @throws UIMAException
         * @throws IOException
         */
        private void writeModel(File posTagFile, String language, String configFileName) throws UIMAException, IOException {

            new File(getModelDir()).mkdirs();

            CollectionReader posTagFileReader = FilesCollectionReader.getCollectionReaderWithSuffixes(
                    posTagFile.getAbsolutePath(), NERReader.CONLL_VIEW, posTagFile.getName());

            AnalysisEngine snowballStemmer = createEngine(SnowballStemmer.class, SnowballStemmer.PARAM_LANGUAGE, language);

            AnalysisEngine nerAnnotator = createEngine(NERAnnotator.class,
                    NERAnnotator.PARAM_FEATURE_EXTRACTION_FILE, FEATURE_EXTRACTOR_CONFIG_DIRECTORY + configFileName,
                    NERAnnotator.PARAM_IS_TRAINING, true,
                    DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY, getModelDir(),
                    DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME, CrfSuiteStringOutcomeDataWriter.class);

            runPipeline(
                    posTagFileReader,
                    createEngine(NERReader.class),
                    snowballStemmer,
                    nerAnnotator
            );
        }

        private void trainModel() throws Exception {
            org.cleartk.ml.jar.Train.main(getModelDir());
        }

        private void classifyTestFile(File testPosFile, String language, String evaluationOutputFile, String featureExtractorConfigFile)
                throws UIMAException, IOException {

            CollectionReader testPosFileReader = FilesCollectionReader.getCollectionReaderWithSuffixes(testPosFile.getAbsolutePath(),
                    NERReader.CONLL_VIEW, testPosFile.getName());

            AnalysisEngine nerReader = createEngine(NERReader.class);

            AnalysisEngine snowballStemmer = createEngine(SnowballStemmer.class, SnowballStemmer.PARAM_LANGUAGE, language);

            AnalysisEngine nerAnnotator = createEngine(NERAnnotator.class,
                    NERAnnotator.PARAM_FEATURE_EXTRACTION_FILE, FEATURE_EXTRACTOR_CONFIG_DIRECTORY + featureExtractorConfigFile,
                    GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, getModelDir() + "model.jar");

            AnalysisEngine nerWriter = createEngine(NERWriter.class,
                    NERWriter.PARAM_NULL_TYPE, "O",
                    NERWriter.PARAM_EXPECTED_ENTITY_TYPE_NUM, 9,
                    NERWriter.PARAM_FILENAME, EVALUATION_OUTPUT_DIRECTORY + evaluationOutputFile,
                    NERWriter.PARAM_VERBOSE, false);

            runPipeline(
                    testPosFileReader,
                    nerReader,
                    snowballStemmer,
                    nerAnnotator,
                    nerWriter);
        }

        /**
         * This just runs the basic algorithm using the methods defined above
         */
        @Override
        public void run() {
            if (configFile.isFile()) {
                try {
                    writeModel(nerTrain, language, configFile.getName());
                    trainModel();
                    classifyTestFile(nerTest, language, configFile.getName() + "_evalOutput.txt", configFile.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.gc();
        }
    }

    public static void main(String[] args) {

        String language = "en";
        File nerTrain = new File("src/main/resources/ner/ner_eng.train");
        File nerTest = new File("src/main/resources/ner/ner_eng.dev");

        // generate the configuration files for the combination of feature extractors
        try {
            Features2Xml.generateFeatureAblationTestFiles(MIN_NUMBER_OF_EXTRACTORS, FEATURE_EXTRACTOR_CONFIG_DIRECTORY);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // read all XML config files from disk
        File configDir = new File(FEATURE_EXTRACTOR_CONFIG_DIRECTORY);
        File[] configFiles = configDir.listFiles();
        UIMAFramework.getLogger().log(Level.WARNING, "Running Feature Ablation Test for " + configFiles.length + " different Extractor Configurations!");

        // create a fixed sized thread pool. the number of maximum threads should be adjusted to the machines cores
        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);
        int i = 0;
        // for each config file instantiate a test runner that is managed by the thread pool
        for (File configFile : configFiles)
            threadPool.submit(new AblationTestRunner(language, configFile, nerTrain, nerTest, i++));

        threadPool.shutdown();
    }
}