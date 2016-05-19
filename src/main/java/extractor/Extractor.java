package extractor;

import common.CLPsychLabels;
import common.Configs;
import common.Post;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import filter.WordFilter;
import normalizer.SpecialCharNormalizer;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Created by halmeida on 2/9/16.
 */
public class Extractor {

    HashMap<String,Integer> featureList = new HashMap<String,Integer>();
    SpecialCharNormalizer charNormalizer = new SpecialCharNormalizer();
    boolean useStopList,useZipfList, useDiscVocabulary;
    WordFilter wordFilter;
    String work_dir = "";
    String resources_dir = "";
    String features_dir = "";
    String tagger_dir = "";
    String location = "";

    Properties props;
    String taggerModel ="";
    MaxentTagger tagger;

    // HA: main constructor
    public Extractor(){
        props = Configs.getInstance().getProps();
        work_dir = props.getProperty("WORK_DIRECTORY");
        resources_dir = props.getProperty("RESOURCES_DIR");
        features_dir = props.getProperty("FEATURE_DIR")+"/";
        tagger_dir = props.getProperty("TAGGER_DIR") + "/";

        location = work_dir + resources_dir + features_dir;
        taggerModel= work_dir + resources_dir + tagger_dir + props.getProperty("TAGGER_FILE");

        useStopList = Boolean.valueOf(props.getProperty("USE_STOPFILTER"));
        useZipfList = Boolean.valueOf(props.getProperty("USE_ZIPFFILTER"));
        useDiscVocabulary = Boolean.valueOf(props.getProperty("USE_DISC_VOCAB"));
        wordFilter = new WordFilter();
    }


    /**
     * General method for feature extraction
     * @param posts
     * @return
     */
    public int extractFeatures(List<Post> posts, String step){

        for(int i = 0; i < posts.size(); i++){

            String post = normalizePost(selectPostContent(posts.get(i), step));

            ArrayList<String> tokens = tokenizePost(post);
            // handle extra punctuation on a single token
            tokens = normalizeSingleWord(tokens);

            for(int j = 0; j < tokens.size(); j++){
                featureList = setOccPerFeature(tokens.get(j), featureList);
            }
        }

        return featureList.size();
    }


    public void loadWordFilterLists(List<Post> posts, String step){
        if(useZipfList) wordFilter.loadOccByZipfs(featureList, step);
        if(useStopList) wordFilter.loadStopWords();
    }

//    public

    /**
     * Generate list of post content (fields)
     * to extract features or build vectors
     * HA
     * @param post
     * @return
     */
     public String selectPostContent(Post post, String step){
            StringBuilder sb = new StringBuilder();

         sb.append(post.getValue(post.SUBJECT) + " ");
         sb.append(post.getValue(post.BODY) + " ");
//         sb.append(post.getValue(post.AUTHOR_LOGIN) + " ");

         if(step.contains("model")) {
             sb.append(post.getValue(post.KUDOS) + " ");
             sb.append(post.getValue(post.VIEWS) + " ");

			for(CLPsychLabels label : CLPsychLabels.values()) {
                 String labelName = label.getPostFieldName();
                 String nbOccurrences = post.getValue(labelName);
                 if(nbOccurrences==null || nbOccurrences.isEmpty()) {
                     sb.append(labelName).append(0 + " ");
                 } else {
                     sb.append(labelName).append(post.getValue(labelName) + " ");
                 }
             }
         }

            return sb.toString();
    }

    /**
     * Performs specific normalization steps in a
     * given text, before feature extraction
     * HA
     * @param text
     * @return
     */
    public String normalizePost(String text){

        /* HA: the order of normalization steps is important,
        and must not be changed without performing tests first */

        text = text.toLowerCase();

        text = charNormalizer.handleLinks(text);
        text = charNormalizer.replaceSpecialHTML(text);
        text = charNormalizer.replaceSmileyChars(text);

        text = text.replace("</", "<");
        text = text.replace("/>", ">");
        text = text.replace("\\", "");
        text = text.replaceAll("\\s+", " ");

        text = charNormalizer.handleXMLTags(text);
        text = charNormalizer.handlePunctuation(text);

        return text;
    }

    /**
     * Handle normalization of single words applying
     * norms that shouldnt be valid to entire post msg
     * (deal with cases like "friends/family"
     * or "kind-of-thing")
     * HA
     * @param words
     * @return
     */
    public ArrayList<String> normalizeSingleWord(ArrayList<String> words){
        ArrayList<String> result = new ArrayList<String>();

        for(String w : words){
            w = charNormalizer.handleWordPunctuation(w);

            String[] splitted = StringUtils.split(w, " ");
            for(String s : splitted){
                if(isTokenValid(s))
                    result.add(s);
            }
        }

        return result;
    }

    /**
     * Provide string to be POS tagged,
     * instead of tokenized words.
     * tokenization was done to normalize
     * single tokens
     *
     * @param words
     * @return
     */
    public String concatenatePost(ArrayList<String> words){
        StringBuffer sb = new StringBuffer();

        for(int i = 0; i < words.size(); i++){
            if( i < words.size() - 1)
                sb.append(words.get(i) +" ");
            else sb.append(words.get(i));
        }

        return sb.toString();
    }

    /**
     * Informs features and their occurence
     * (i.e., counts all times a feature
     * was seen in a given list)
     * HA
     * @param feature
     * @param list
     * @return
     */
    public HashMap<String,Integer> setOccPerFeature(String feature, HashMap<String,Integer> list){

        if(!list.containsKey(feature)){
            list.put(feature,1);
        }
        else{
            int count = list.get(feature);
            list.put(feature, count+1);
        }
        return list;
    }

    /**
     * Returns a POS tagged text,
     * spplited by a white space
     * HA
     * @param normText
     * @param tagger
     * @return
     */
    public String[] getPOSTags(String normText, MaxentTagger tagger){
        return tagger.tagString(normText).split(" ");
    }

    /**
     * Returns the word of a (word_POS) string
     * @param taggedWord
     * @return
     */
    public String getPOSWord(String taggedWord){
        return taggedWord.substring(0, taggedWord.indexOf("_"));
    }

    /**
     * Returns the POS of a (word_POS) string
     * @param taggedWord
     * @return
     */
    public String getPOSTag(String taggedWord){
        return taggedWord.substring(taggedWord.indexOf("_")+1);
    }



    /**
     * Splits a text into tokens, using
     * single white space as criterion
     * HA
     * @param text
     * @return
     */
    public ArrayList<String> tokenizePost(String text){
        text = text.replace(" ", "*");
        String[] tokenized = StringUtils.split(text, "*");

        //filter tokens by length
        //get rid of tokens having single char
        ArrayList<String> cleaned = new ArrayList<String>();

        for(int i = 0; i < tokenized.length; i++){

            if(isTokenValid(tokenized[i])){
                cleaned.add(tokenized[i]);
            }
        }

        return cleaned;
    }

    /**
     * Sanity check for a ngram (token)
     * tokens must have:
     * at least one letter
     * at least three chars
     * HA
     * @param word
     * @return
     */
    public boolean isTokenValid(String word){
        if((word.length() >= 3 || word.equalsIgnoreCase("no"))
                && word.matches(".*[a-zA-Z]+.*"))
            return true;
        else return false;
    }

    /**
     * Provides only list of features (w/o count)
     * @return
     */
    public HashMap<String,Integer> getFeatureList(){
        return featureList;
    }
}
