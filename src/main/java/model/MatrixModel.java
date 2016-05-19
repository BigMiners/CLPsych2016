package model;

import common.Configs;
import common.Post;
import extractor.*;
import parser.PostParser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by halmeida on 2/10/16.
 */
public class MatrixModel {

    String expType = "";
    String feature_set = "";
    String arffFile = "";
    String work_dir = "";
    String resources_dir = "";
    String model_dir = "";
    String location = "";
    String timeStamp = "";
    Properties props;
    Extractor extractor;
    PostParser parser;
    ArffVector vector;

    public MatrixModel(){

        props = Configs.getInstance().getProps();
        expType = props.getProperty("TASK");
        feature_set = informFeatureSet();
        work_dir = props.getProperty("WORK_DIRECTORY") + "/";
        resources_dir = props.getProperty("RESOURCES_DIR")  + "/";
        model_dir = props.getProperty("MODEL_DIR") + "/";
        location = work_dir + resources_dir + model_dir;

        extractor = new Extractor();
        parser = new PostParser();
        vector = new ArffVector();

        timeStamp = new SimpleDateFormat("yyyyMMdd_hh:mm").format(new Date());
        arffFile = location + "CLPsych" + feature_set + "_" + expType +"_"+ timeStamp + ".arff";
    }


    public void generateModel(List<Post> posts){

        BufferedWriter writer;

        try {
            writer = new BufferedWriter(new FileWriter(arffFile));

            String outHeaderArff = vector.getHeader(expType, feature_set);
            writer.write(outHeaderArff + "\n");

            int count = 0;

            for(Post post : posts) {

                //HA: get class of each post
                String postClass = post.getValue(post.POST_CLASS);
                postClass = postClass.replace("CLPsychLabel","");
                String docId = post.getValue(post.POST_ID);
                int bodyLenght = post.getValue(post.BODY).length();
                float weight = Float.parseFloat(post.getValue(post.WEIGHT));


                // HA: get selected fields for the model
                String fields = extractor.selectPostContent(post, "model");
                fields = extractor.normalizePost(fields);
                String arffLine = vector.getVectorLine(fields, postClass, docId, bodyLenght, weight);

                count++;
                arffLine = arffLine + "\n";
                writer.write(arffLine);

                if(count%100 == 0){
                    System.out.println(" -> " + count + " documents processed.");
                }
            }

            writer.flush();
            writer.close();

            System.out.println("Done! " + expType + " model generated for " + posts.size() + " documents.");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    /**
     * List features used for task
     * (from triage)
     * @return
     */
    private String informFeatureSet(){
        String value = "";
//        if(Boolean.valueOf(props.getProperty("USE_RULES")))
//            value += "_rules";
        if(Boolean.valueOf(props.getProperty("USE_POS")))
            value += "_POS";
        if(Boolean.valueOf(props.getProperty("USE_SENTIMENT")))
            value += "_sentiment";
        if(Boolean.valueOf(props.getProperty("USE_NGRAM")))
            value += "_ngrams_s"+ props.getProperty("NGRAM_SIZE");
        if(Boolean.valueOf(props.getProperty("USE_STOPFILTER")))
            value += "_stopwords";
        if(Boolean.valueOf(props.getProperty("USE_VOCABULARY")))
            value += "_vocabulary";
        if(Boolean.valueOf(props.getProperty("USE_CLASS_COUNT")))
            value += "_classCount" + props.getProperty("N_PREVIOUS");
        else if(Boolean.valueOf(props.getProperty("USE_USER_HIST")))
            value += "_userHist_s" + props.getProperty("N_PREVIOUS");

        return value;
    }
}
