package predictor;

import common.Configs;
import common.IOUtil;
import common.Post;
import parser.PostParser;
import weka.classifiers.Evaluation;

import javax.annotation.processing.SupportedSourceVersion;
import java.util.*;

/**
 * Created by halmeida on 3/10/16.
 */
public class RulePredictor {

    Properties props;
    String work_dir, resources_dir, rulesFile, rulesPath;
    ArrayList<String> rules;
    List<Post> posts;
    PostParser postParser;
    HashMap<String,String> rulePosts;

    public RulePredictor(){

        props = Configs.getInstance().getProps();
        work_dir = props.getProperty("WORK_DIRECTORY");
        resources_dir = props.getProperty("RESOURCES_DIR") + "/vocabulary/";
        rulesFile = "crisis_rules,red_rules"
                //+                ",amber_rules"
        ;
        rulesPath = work_dir + resources_dir;

        postParser = new PostParser();
        rulePosts = new HashMap<String,String>();
    }

    public HashMap<String,String> getRulePosts(){
        return rulePosts;
    }

    public List<Post> filterRulePredictions(List<Post> posts){


        int count = 0;

        if(!rulePosts.isEmpty()) {
            for (int i = 0; i < posts.size(); i++) {
                Post thisPost = posts.get(i);
                String id = thisPost.getValue(thisPost.POST_ID);
                if (rulePosts.containsKey(id)) {
                    posts.remove(i);
                    count++;
                }
            }
        }
        return posts;
    }


    public void loadPredictionByRule(){

        posts = postParser.parseCorpus("");

        HashMap<String,String> rules = IOUtil.getINSTANCE().loadDictionary(rulesPath, rulesFile);

        for(int i = 0; i < posts.size(); i++){

            Post thisPost = posts.get(i);
            String postContent = thisPost.getValue(thisPost.BODY);

            for(String rule : rules.keySet()){
                String ruleClass = rules.get(rule);

                if (postContent.contains(rule)) {
                    String postClass = thisPost.getValue(thisPost.POST_CLASS);
                    String postId = thisPost.getValue(thisPost.POST_ID);

                    if(rulePosts.keySet().contains(postId)){
                        String existentClass = rulePosts.get(postId);

                        if(ruleClass.contains("amber") && !existentClass.contains("crisis")
                                    && !existentClass.contains("red"))
                                rulePosts.put(postId, postClass + "," + ruleClass);

                        if (ruleClass.contains("red") && !rulePosts.get(postId).contains("crisis"))
                            rulePosts.put(postId, postClass + "," + ruleClass);

                        if(ruleClass.contains("crisis"))
                            rulePosts.put(postId, postClass + "," + ruleClass);
                    }
                    else rulePosts.put(postId, postClass + "," + ruleClass);
                    System.out.println("post id: " + postId + " label: " + ruleClass);
                }


            }
        }

        System.out.println(rulePosts.size() + " posts labeled with rules.");

    }

//    public Evaluation updateCfMatrix(Evaluation eval){
//
//        double[][] confusionMatrix = eval.confusionMatrix();
//        int index = confusionMatrix.length-1;
//        Iterator iter = rulePosts.keySet().iterator();
//
//        while(iter.hasNext()){
//            String entry = (String) iter.next();
//            String[] className = rulePosts.get(entry).split(",");
//            if(className[0].contains("crisis")){
//                confusionMatrix[index][index] += 1;
//            }
//            else if(className[0].contains("red")){
//                confusionMatrix[index][index-1] += 1;
//            }
//            else if(className[0].contains("amber")){
//                confusionMatrix[index][index-2] += 1;
//            }
//            else if(className[0].contains("green")){
//                confusionMatrix[index][index-3] += 1;
//            }
//        }
//
//        try {
//            System.out.println("Adjusted matrix: " + getMatrix(confusionMatrix));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return eval;
//    }
//
//
//
//    public String getMatrix(double[][] matrix){
//        StringBuffer sb = new StringBuffer();
//        sb.append("\n");
//
//        for (int i = 0; i < matrix.length; i ++){
//
//            for(int j = 0; j < matrix.length; j ++){
//                if(j < matrix.length-1)
//                    sb.append( (int) matrix[i][j] + "\t");
//                else sb.append((int) matrix[i][j] + "\n");
//            }
//
//        }
//        return sb.toString();
//    }





}
