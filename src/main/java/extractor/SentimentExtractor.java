package extractor;

import common.Configs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import common.IOUtil;
import common.Post;
import edu.stanford.nlp.ling.WordTag;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * Created by halmeida on 2/4/16.
 */
public class SentimentExtractor extends Extractor{

    String sentimentFile;
    String sentimentDictionary;
    String sentimentDir;

    public SentimentExtractor() {
        super();
        sentimentFile = props.getProperty("SENTIMENT_FILE");
        sentimentDictionary = props.getProperty("SENTIMENT_DIC");
        sentimentDir = work_dir + resources_dir + props.getProperty("SENTIMENT_DIC_DIR") + "/";
    }

    /**
     * Extraction of features for sentiments
     */
    @Override
    public int extractFeatures(List<Post> postContent, String step) {

        //load POS tagger
       tagger = new MaxentTagger(taggerModel);
        //load sentiment dictionaries
        HashMap<String, String> sentimentDic = IOUtil.getINSTANCE().loadDictionary(sentimentDir, sentimentDictionary);

        for(int i = 0; i < postContent.size(); i++) {
            //normalize post content and get POS tags
            String text = normalizePost(selectPostContent(postContent.get(i),"extract"));
            ArrayList<String> words = tokenizePost(text);

            // handle extra punctuation on a single token
            words = normalizeSingleWord(words);

            String[] tagged = getPOSTags(concatenatePost(words), tagger);

            for(int j = 0; j < tagged.length; j++) {

                //   for(int j = 0; j < tagged.length; j++) {
                //retrieve each word and each tag
                String taggedWord = getPOSWord(tagged[j]);
                String POS = getPOSTag(tagged[j]);


                //get stemmed word using POS tag
                WordTag stemmed = Morphology.stemStatic(taggedWord, POS);

                //if stemmed word is in the sentiment dictionary
                if(sentimentDic.keySet().contains(stemmed.word())
                        || sentimentDic.values().contains(stemmed.word())){

                    //add to feature list
                    featureList = setOccPerFeature(stemmed.word(), featureList);
                }
            }
        }

        System.out.println("Exporting Sentiment features...");
        IOUtil.getINSTANCE().exportFeatures(location + sentimentFile, featureList, postContent.size(), step);

        return featureList.size();
    }



}
