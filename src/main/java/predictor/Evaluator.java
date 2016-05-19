package predictor;

import common.Configs;
import common.IOUtil;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.core.Instances;

import java.io.IOException;
import java.util.*;

/**
 * Created by halmeida on 3/10/16.
 */
public class Evaluator {

    String work_dir, resources_dir, results_dir, resultFile;
    boolean CV, CFS, useRules, overwriteRules;
    Properties props;


    public Evaluator(String classifier){
        props = Configs.getInstance().getProps();
        work_dir = props.getProperty("WORK_DIRECTORY");
        resources_dir = props.getProperty("RESOURCES_DIR");
        results_dir = work_dir + resources_dir + props.getProperty("RESULTS_DIR");
        CV = Boolean.valueOf(props.getProperty("CV"));
        CFS = Boolean.valueOf(props.getProperty("CFS"));
        useRules = Boolean.valueOf(props.getProperty("USE_RULES"));
        overwriteRules = Boolean.valueOf(props.getProperty("OVERWRITE_RULES"));

        if(CV) resultFile = results_dir + "/" + "CV_" + props.getProperty("MODEL_FILE").replace("arff", classifier);
        else resultFile = results_dir + "/" + props.getProperty("TEST_FILE").replace("arff", classifier);

        if(CFS) resultFile = resultFile + ".cfs";

        if (useRules) {
            if (!overwriteRules) resultFile = resultFile + ".rulesBefore";
            else if(overwriteRules) resultFile = resultFile + ".rulesAfter";
        }

    }

//    SortedSet<String> keys = new TreeSet<String>(myHashMap.keySet());


    private void outputPredictions(TreeMap<String,String> predicitons){
        StringBuilder sb = new StringBuilder();
        Iterator<String> iterator = predicitons.keySet().iterator();

        while(iterator.hasNext()){
            String id = iterator.next();
            String predicted = predicitons.get(id);
                    sb.append(id).append("\t")
                      .append(predicted).append("\n");

        }

        try {
            IOUtil.getINSTANCE().writeOutput(resultFile,sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Uses predictions from a model to
     * write to a file
     * @param output
     * @param data
     */
    public TreeMap<String,String> getAllPredictions(ArrayList<Prediction> output, Instances data, HashMap<String,String> rulePredictions, boolean overwriteRules) {

        TreeMap<String,String> results = new TreeMap<String,String>();
        StringBuilder sb = new StringBuilder();

        String id, predicted = "", actual;
        double act, pred, weight;

        for(int i = 0; i < output.size(); i++){
            act = output.get(i).actual();
            pred = output.get(i).predicted();
            id = data.get(i).toString(0);

            if(overwriteRules && rulePredictions != null && rulePredictions.keySet().contains(id)){
                predicted = rulePredictions.get(id).split(",")[1];
            }
            else {
                if (act == 1.0) actual = "amber";
                else if (act == 2.0) actual = "red";
                else if (act == 3.0) actual = "crisis";
                else if (act == 0.0) actual = "green";
                else actual = "?";

                if (pred == 1.0) predicted = "amber";
                else if (pred == 2.0) predicted = "red";
                else if (pred == 3.0) predicted = "crisis";
                else predicted = "green";
            }
            results.put(id,predicted);
        }

        if(!overwriteRules && rulePredictions != null){
            Iterator<String> iter = rulePredictions.keySet().iterator();

            while(iter.hasNext()){
                id = iter.next();
                predicted = rulePredictions.get(id).split(",")[1];
                results.put(id,predicted);
            }
        }

        outputPredictions(results);

        return results;
    }


    public Evaluation updateCfMatrix(Evaluation eval, HashMap<String, String> rulePosts){

        double[][] confusionMatrix = eval.confusionMatrix();
        int index = confusionMatrix.length-1;
        Iterator iter = rulePosts.keySet().iterator();

        while(iter.hasNext()){
            String entry = (String) iter.next();
            String[] className = rulePosts.get(entry).split(",");
            if(className[0].contains("crisis")){
                confusionMatrix[index][index] += 1;
            }
            else if(className[0].contains("red")){
                confusionMatrix[index][index-1] += 1;
            }
            else if(className[0].contains("amber")){
                confusionMatrix[index][index-2] += 1;
            }
            else if(className[0].contains("green")){
                confusionMatrix[index][index-3] += 1;
            }
        }

        try {
            System.out.println("Adjusted matrix: " + getMatrix(confusionMatrix));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return eval;
    }



    public String getMatrix(double[][] matrix){
        StringBuffer sb = new StringBuffer();
        sb.append("\n");

        for (int i = 0; i < matrix.length; i ++){

            for(int j = 0; j < matrix.length; j ++){
                if(j < matrix.length-1)
                    sb.append( (int) matrix[i][j] + "\t");
                else sb.append((int) matrix[i][j] + "\n");
            }

        }
        return sb.toString();
    }
}
