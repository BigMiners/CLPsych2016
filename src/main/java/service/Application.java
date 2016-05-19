package service;

import analyse.UserHistoryRecovery;
import common.Configs;
import common.Post;
import extractor.*;
import model.MatrixModel;
import parser.PostParser;
import predictor.Evaluator;
import predictor.Predictor;
import predictor.RulePredictor;
import weka.classifiers.Evaluation;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Created by mqueudot on 29/01/16.
 */
public class Application {

    public static void main(String[] args) {

        String module = System.getProperty("module");
        Properties props = Configs.getInstance().getProps();
        boolean verbose = false;

        boolean useNgrams = Boolean.valueOf(props.getProperty("USE_NGRAM"));
        boolean usePOS = Boolean.valueOf(props.getProperty("USE_POS"));
        boolean useSentiment = Boolean.valueOf(props.getProperty("USE_SENTIMENT"));
        boolean useVocabulary = Boolean.valueOf(props.getProperty("USE_VOCABULARY"));
        boolean userHistory = Boolean.valueOf(props.getProperty("USE_USER_HIST"));
		boolean useClassCount = Boolean.valueOf(props.getProperty("USE_CLASS_COUNT"));
        boolean useRules = Boolean.valueOf(props.getProperty("USE_RULES"));
        boolean overwriteRule = Boolean.valueOf(props.getProperty("OVERWRITE_RULES"));


        String task = props.getProperty("TASK");

        PostParser postParser = new PostParser();
        List<Post> labeledPosts, posts = new ArrayList<Post>();
        List<String> labeledIDs = new ArrayList<>();
        RulePredictor rulePredictor = new RulePredictor();

        if(useRules) rulePredictor.loadPredictionByRule();

        //if we are not classifying, we want to parse data
        if(!module.contains("classify") || useClassCount){

            //parse labeled data and list all IDs in it
            posts = postParser.parseCorpus("");

            //rule classified posts are not to be used
            // for extraction or model, so filter them out.
            if(useRules && !overwriteRule) posts = rulePredictor.filterRulePredictions(posts);

            if(userHistory && !task.contains("test")){
                posts = UserHistoryRecovery.getInstance().addUserHistory(posts);
            }

			if(useClassCount) {
                posts = UserHistoryRecovery.getInstance().addLabelOccurrences(posts);
            }
        }

        switch(module) {

            case("extract"): {

                if(useNgrams) {
                  NgramExtractor extractor = new NgramExtractor();
					extractor.extractFeatures(posts, module);
                }
                if(usePOS){
                   POSExtractor extractor = new POSExtractor();
					extractor.extractFeatures(posts, module);
                }
                if(useSentiment){
                   SentimentExtractor extractor = new SentimentExtractor();
					extractor.extractFeatures(posts, module);
                }
                if(useVocabulary){
                   VocabularyExtractor extractor = new VocabularyExtractor();
                    extractor.extractFeatures(posts, module);
                }

                break;
            }

            case("model"):{
                MatrixModel model = new MatrixModel();
                model.generateModel(posts);
                break;
            }

            case("classify"): {
                String classifier = System.getProperty("classifier");
                Predictor predictor = new Predictor(classifier);
                Evaluation supervResults = predictor.predict();
                Instances rawData = predictor.getRawInstances();

                Evaluator evaluator = new Evaluator(classifier);
//                evaluator.updateCfMatrix(supervResults, rulePredictor.getRulePosts());
                evaluator.getAllPredictions(supervResults.predictions(), rawData, rulePredictor.getRulePosts(), overwriteRule);


                break;
            }
        }
        System.exit(1);
    }
}
