package de.unihamburg.informatik.nlp4web.tutorial.tut5.ner;

import de.unihamburg.informatik.nlp4web.tutorial.tut5.xml.Features2Xml;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ExecuteFinalTest {

    private static final String FEATURE_EXTRACTOR_CONFIG_FILENAME = "src/main/resources/feature/finalConfig.xml";

    public static void main(String[] args) throws IOException {

        String language = "en";
        File nerTrain = new File("src/main/resources/ner/ner_eng.train");
        File nerTest = new File("src/main/resources/ner/ner_eng.dev");
        File nerFinalTest = new File("src/main/resources/ner/final/ner_eng.test");



        // Read the file as string
        String trainStr = FileUtils.readFileToString(nerTrain);
        String testStr = FileUtils.readFileToString(nerTest);

        // concatenated file (since it's allowed)
        File concatTrain = new File("src/main/resources/ner/final/ner_eng_concat.train");
        // do the concatenation
        FileUtils.writeStringToFile(concatTrain, trainStr);
        FileUtils.writeStringToFile(concatTrain, testStr);

        // generate the configuration files for the combination of feature extractors
        try {
            Features2Xml.generateFeatureExtractionCombination(FEATURE_EXTRACTOR_CONFIG_FILENAME);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //let it run with final config and concatenated training
        File configFile = new File(FEATURE_EXTRACTOR_CONFIG_FILENAME);
        new Thread(new ExecuteFeatureAblationTest.AblationTestRunner(language, configFile, concatTrain, nerFinalTest, 1337)).start();
    }
}
