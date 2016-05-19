package model;

import common.Configs;
import common.IOUtil;
import extractor.Extractor;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Created by halmeida on 2/10/16.
 */
public class ArffVector {

    boolean useNgram, useSentiment, usePOStags, useKudos, useViews, useVocabulary, useClassCount;
    String ngramFeatures = "";
    String sentimentFeatures = "";
    String POSFeatures = "";
    String vocabFeatures = "";
    String feature_dir = "";
    String resources_dir = "";
    String work_dir = "";
    Properties props;

    ArrayList<String> ngrams, sentiments, posTags, vocabulary;
    Set<String> features;
    Extractor extractor;

    public ArffVector(){

        props = Configs.getInstance().getProps();

        work_dir = props.getProperty("WORK_DIRECTORY") + "/";
        resources_dir = props.getProperty("RESOURCES_DIR") + "/";
        feature_dir = props.getProperty("FEATURE_DIR")  + "/";
        ngramFeatures = work_dir + resources_dir + feature_dir + props.getProperty("NGRAM_FILE") + props.getProperty("NGRAM_SIZE");
        sentimentFeatures = work_dir + resources_dir + feature_dir + props.getProperty("SENTIMENT_FILE");
        POSFeatures = work_dir + resources_dir + feature_dir + props.getProperty("POS_FILE");
        vocabFeatures = work_dir + resources_dir + props.getProperty("VOCABULARY_DIC_DIR") + "/" + props.getProperty("VOCABULARY_DIC");

        useKudos = Boolean.valueOf(props.getProperty("USE_KUDOS"));
        useViews = Boolean.valueOf(props.getProperty("USE_VIEWS"));
        useNgram = Boolean.valueOf(props.getProperty("USE_NGRAM"));
        useSentiment = Boolean.valueOf(props.getProperty("USE_SENTIMENT"));
        usePOStags = Boolean.valueOf(props.getProperty("USE_POS"));
		useClassCount = Boolean.valueOf(props.getProperty("USE_CLASS_COUNT"));
        useVocabulary = Boolean.valueOf(props.getProperty("USE_VOCABULARY"));

        extractor = new Extractor();

        loadResources();
    }

    public void loadResources(){
        features = new HashSet<String>();

        if(useNgram){
            ngrams = IOUtil.getINSTANCE().loadFeatureList(ngramFeatures);
            features.addAll(ngrams);
        }
        if(useSentiment){
            sentiments = IOUtil.getINSTANCE().loadFeatureList(sentimentFeatures);
            features.addAll(sentiments);
        }
        if(usePOStags){
            posTags = IOUtil.getINSTANCE().loadFeatureList(POSFeatures);
            features.addAll(posTags);
        }
        if(useVocabulary){
            vocabulary = IOUtil.getINSTANCE().loadFeatureList(vocabFeatures);
            features.addAll(vocabulary);
        }
    }

    /**
     * Loads features from file and generates
     * feature list for ARFF header
     * HA
     * @param expType
     * @param featureSet
     * @return
     */
    public String getHeader(String expType, String featureSet){
        StringBuilder header= new StringBuilder();
        int size = 0;

        if(expType.contains("test")) header.append("% ARFF test file - CLPsych Task 2016\n\n");
        else header.append("% ARFF training file - CLPsych Task 2016\n\n");

        header.append("@RELATION clpsych\n");
        header.append(getHeaderValue("docid", size++, "docid"));
        header.append(getHeaderValue("bodylenght", size++, "bodylenght"));

        if(features.size() > 0){
            System.out.println("There are " + features.size() + " unique features for this setup.");
            Iterator iterator = features.iterator();

            while(iterator.hasNext()){
                String thisFeature = (String) iterator.next();
                if(useNgram && ngrams.contains(thisFeature)){
                    header.append(getHeaderValue(thisFeature, size++, "Ngram"));
                }
                //because of stemming
                //give priority to vocabulary and sentiments
                else if(useVocabulary && vocabulary.contains(thisFeature)){
                    header.append(getHeaderValue(thisFeature, size++, "Vocabulary"));
                }
                else if(useSentiment && sentiments.contains(thisFeature)){
                    header.append(getHeaderValue(thisFeature, size++, "Sentiment"));
                }
                else if(usePOStags && posTags.contains(thisFeature)) {
                    header.append(getHeaderValue(thisFeature, size++, "POSFeature"));
                }
            }
        }

        if(useKudos) header.append(getHeaderValue("kudos", size++, "kudos"));
        if(useViews) header.append(getHeaderValue("views", size++, "views"));
		if(useClassCount) {
            header.append(getHeaderValue("nbGreenClass",size++,"nbGreenClass"));
            header.append(getHeaderValue("nbAmberClass",size++,"nbAmberClass"));
            header.append(getHeaderValue("nbRedClass",size++,"nbRedClass"));
            header.append(getHeaderValue("nbCrisisClass",size++,"nbCrisisClass"));
        }

        header.append("@ATTRIBUTE class\t{" +
                "green, " +
                "amber, " +
                "red, " +
                "crisis}\n");
        header.append("@DATA\n");

        return header.toString();
    }

    /**
     * Generates a vector line for model matrix
     * Accounts for each feature occurrence
     * in the document
     * HA
     * @param post
     * @param postClass
     * @param docId
     * @param bodyLeght
     * @return
     */
    public String getVectorLine(String post, String postClass, String docId, int bodyLeght, float weight){

        StringBuilder vectorLine = new StringBuilder();

        vectorLine.append(docId+",");
        vectorLine.append(bodyLeght+",");

        if(features.size() > 0) {
            Iterator iterator = features.iterator();

            while(iterator.hasNext()){
                String thisFeature = (String) iterator.next();
                vectorLine.append(getVectorValue(thisFeature, post, weight));
            }
        }

        if(useKudos) vectorLine.append(getVectorValue("postKudos", post, weight));

        if(useViews) vectorLine.append(getVectorValue("postViews", post, weight));

		if(useClassCount) {
            vectorLine.append(getVectorValue("nbGreenClass",post,weight));
            vectorLine.append(getVectorValue("nbAmberClass",post,weight));
            vectorLine.append(getVectorValue("nbRedClass",post,weight));
            vectorLine.append(getVectorValue("nbCrisisClass",post,weight));
        }

        vectorLine.append(postClass);

        return vectorLine.toString();
    }


    /**
     * Determines the feature value (occurence)
     * to be written in the ARFF vector
     * HA
     * @param feature
     * @param post
     * @return
     */
    private String getVectorValue(String feature, String post, float weight){

        int featureCount = 0;

        post = post.toLowerCase();
        feature = feature.toLowerCase();

        if(feature.contains("postkudos")){
            String val = post.substring(post.indexOf("postkudos"));
            val = val.substring(0, val.indexOf(" "));
            val = val.replace("postkudos", "").trim();
            featureCount = Integer.parseInt(val.trim());
        }
        else if(feature.contains("postviews")){
            String val = post.substring(post.indexOf("postviews"));
            val = val.substring(0, val.indexOf(" "));
            val = val.replace("postviews","").trim();
            featureCount = Integer.parseInt(val.trim());
        }
		else if(feature.contains("nbgreenclass")) {
            String val = post.substring(post.indexOf("nbgreenclass"));
            val = val.substring(0, val.indexOf(" "));
            val = val.replace("nbgreenclass","").trim();
            featureCount = Integer.parseInt(val.trim());
        }
        else if(feature.contains("nbamberclass")) {
            String val = post.substring(post.indexOf("nbamberclass"));
            val = val.substring(0, val.indexOf(" "));
            val = val.replace("nbamberclass","").trim();
            featureCount = Integer.parseInt(val.trim());
        }
        else if(feature.contains("nbredclass")) {
            String val = post.substring(post.indexOf("nbredclass"));
            val = val.substring(0, val.indexOf(" "));
            val = val.replace("nbredclass","").trim();
            featureCount = Integer.parseInt(val.trim());
        }
        else if(feature.contains("nbcrisisclass")) {
            String val = post.substring(post.indexOf("nbcrisisclass"));
            val = val.substring(0, val.indexOf(" "));
            val = val.replace("nbcrisisclass","").trim();
            featureCount = Integer.parseInt(val.trim());
        }
        else if(post.contains(feature)){
            featureCount = StringUtils.countMatches(post, feature);
            if(weight != 0){
                Float weighted = featureCount * weight;
                featureCount = weighted.intValue();
            }
        }

        return (featureCount+",") ;
    }

    /**
     * Determines the feature name and attribute
     * to be written in the ARFF head
     * HA
     * @param feature
     * @param count
     * @param featureType
     * @return
     */
    private String getHeaderValue(String feature, int count, String featureType){

        String namefeature = feature.replaceAll("\\s", "-");
        namefeature = namefeature.replaceAll("[,:=+']", "-");

        String ref = featureType + String.valueOf(count) + namefeature;

         return ("@ATTRIBUTE " + ref + "\tREAL \t\t%" + feature + "\n");
    }


}
