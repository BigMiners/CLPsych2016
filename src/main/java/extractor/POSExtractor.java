package extractor;

import common.Configs;
import common.IOUtil;
import common.Post;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by halmeida on 2/18/16.
 */
public class POSExtractor extends Extractor{


    String taggerFile = "";

    public POSExtractor(){
        super();
        taggerFile = props.getProperty("POS_FILE");
    }

    /**
     * Extraction of features for POS tags
     * @param postContent
     * @return
     */
    @Override
    public int extractFeatures(List<Post> postContent, String step){
        int count = 0;
        //load POS tagger
        tagger = new MaxentTagger(taggerModel);

        for(int i = 0; i < postContent.size(); i++) {
            //normalize post and get POS tags
            String thisPost = normalizePost(selectPostContent(postContent.get(i), step));
            ArrayList<String> words = tokenizePost(thisPost);

            // handle extra punctuation on a single token
            words = normalizeSingleWord(words);
            String[] tagged = getPOSTags(concatenatePost(words), tagger);

            for(int j = 0; j < tagged.length; j++){
                //retrieve each tag, and (word_tag)
                String taggedWord = tagged[j].replace("_", "\t");
                String POS = getPOSTag(tagged[j]);

                //check if word has a relevant POS tag
                if(isPOSValid(POS))
                    //add to features list
                    featureList = setOccPerFeature(taggedWord, featureList);
            }
        }

        System.out.println("Exporting POS features...");
        IOUtil.getINSTANCE().exportFeatures(location + taggerFile, featureList, postContent.size(), step);

        return featureList.size();
    }


    /**
     * Allows to filter a POS set.
     *
     * HA
     * POS tags to keep:
     * set 1: IN, JJ, NN, PDT, PRP, RB, RP, UH, VB
     * set 2: JJ, NN, PDT, RP, VB
     *
     * */
    private boolean isPOSValid(String text){
        if(     //text.contains("IN") ||
                text.contains("JJ") ||
                        text.contains("NN") ||
                        text.contains("PDT") ||
                        //text.contains("PRP") ||
                        //text.contains("RB") ||
                        text.contains( "RP") ||
                        //text.contains("UH") ||
                        text.contains("VB"))
            return true;
        else return false;
    }

}
