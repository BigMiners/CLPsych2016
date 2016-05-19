package predictor;

import common.Configs;
import common.IOUtil;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.functions.*;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.LMT;
import weka.core.Instances;
import weka.core.Range;
import weka.core.converters.*;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

/**
 * Created by halmeida on 2/12/16.
 */
public class Predictor {


    public static int SEED = 1; //the seed for randomizing the data
    public static int FOLDS = 5; //the # of folds to generate

    String work_dir, resources_dir, model_dir, trainingData, testingData, classifier, results_dir, resultFile;
    Boolean CV, CFS, Cluster;
    Classifier cls;
    Instances trainData, testData, filtTrainData, filtTestData;
    Properties props;
    AttributeSelection selector;


    public Predictor(String classif){

        props = Configs.getInstance().getProps();

        work_dir = props.getProperty("WORK_DIRECTORY");
        resources_dir = props.getProperty("RESOURCES_DIR");
        model_dir = props.getProperty("MODEL_DIR") + "/";
        CV = Boolean.valueOf(props.getProperty("CV"));
        CFS = Boolean.valueOf(props.getProperty("CFS"));

        classifier = classif;
        selector = initializeCSF();

        results_dir = work_dir + resources_dir + props.getProperty("RESULTS_DIR");
        trainingData = work_dir +resources_dir + model_dir + props.getProperty("MODEL_FILE");
        testingData = work_dir +resources_dir + model_dir  + props.getProperty("TEST_FILE");

        if(CV) resultFile = results_dir + "/" + "CV_" + props.getProperty("MODEL_FILE").replace("arff", classifier);
        else resultFile = results_dir + "/" + props.getProperty("TEST_FILE").replace("arff", classifier);

        if(CFS) resultFile = resultFile + ".cfs";

    }



    public Evaluation predict(){

        Evaluation eval = null;

        cls = getClassifier(classifier);
        try {
    //Loading train data
            if(!trainingData.isEmpty()) trainData = new ConverterUtils.DataSource(trainingData).getDataSet();
            //Flagging the class index on data
            trainData.setClassIndex(trainData.numAttributes()-1);
            System.out.println("Training data loaded. Number of instances: " + trainData.numInstances() + "\n");

            //filter the file IDs, consider the new training set
            filtTrainData = filteredIDs(trainData);

            //CSF filtering
           if(CFS) filtTrainData = selectAttributes(filtTrainData, "train");

            if(CV) {
                //perform cross-validation
                eval = crossFold(filtTrainData, trainData, cls);
            }
            else {
                //perform train vs. test classification
                if(!testingData.isEmpty()) testData = loadData(testingData);
                testData.setClassIndex(testData.numAttributes()-1);
                System.out.println("Test data loaded. Number of instances: " + testData.numInstances() + "\n");

                filtTestData = filteredIDs(testData);
                if(CFS) filtTestData = selectAttributes(filtTestData, "test");

                eval = classify(filtTrainData, filtTestData, cls, testData);

            }

        }catch(NullPointerException | IllegalArgumentException e){
            System.out.println("MODEL_FILE or TEST_FILE names missing. Please check the config file.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return eval;
    }

    public Instances getRawInstances(){
        if (CV) return trainData;
        else return testData;
    }

    public Instances loadData(String path) throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(path);
        return source.getDataSet();
    }




    /**
     * Creates a output (PlainText) instance to
     * output predictions of a model
     * @param data
     * @return
     * @throws Exception
     */
    public PlainText initPredictions(Instances data) throws Exception{
        PlainText prediction = new PlainText();
        StringBuffer forPredictionsPrint = new StringBuffer();
        //prediction.setOptions(new String[]{"-p 1"});
        prediction.setNumDecimals(3);
        prediction.setHeader(data);
        prediction.setBuffer(forPredictionsPrint);
        prediction.setOutputDistribution(true);

        return prediction;
    }


    /**
     //	 * Executes k-fold cross validation
     //	 * on a given dataset
     //	 * @param data training data provided
     //	 * @param classif type of classifier usedsearch
     //	 * @throws Exception
     //	 */
	public Evaluation crossFold(Instances filteredData, Instances data, Classifier classif) throws Exception{

		Random random = new Random(SEED); //creating seed number generator
        Evaluation evaluateClassifier = new Evaluation(filteredData);

        System.out.println("Classifier: " + classif.getClass());
        System.out.println("Method: " + FOLDS + "-fold cross-validation");
        System.out.println("Evaluation in process...\n\n");

        try {
            //Predictor should not be trained when cross-validation is executed.
            //because subsequent calls to buildClassifier method will return the same results always.
            evaluateClassifier.crossValidateModel(classif, filteredData, FOLDS, random);
            stats(evaluateClassifier);

//            outputSystemPredictions(evaluateClassifier.predictions(), data);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return evaluateClassifier;
	}


    /**
     * Creates classifier according to user's choice
     * @param name
     * @return
     */
    private Classifier getClassifier(String name){
            if (name.contains("lmt"))
                return new LMT();
            else if (name.contains("perceptron"))
                return new MultilayerPerceptron();
            else if (name.contains("forest"))
                return new RandomForest();
            else if (name.contains("j48"))
                return new J48();
            else if(name.contains("bnet"))
                return new BayesNet();
            else if (name.contains("svm"))
                return new LibSVM();
            else if (name.contains("smo"))
                return new SMO();
            else
                return new NaiveBayes();
    }


    /**
     * Removes the ID attribute (index 1)
     * from a given dataset
     *
     * @param data instances
     * @return filtered dataset
     * @throws Exception
     */
    private Instances filteredIDs(Instances data) throws Exception {
        Remove remove = new Remove();
        //setting index to be removed
        remove.setAttributeIndices("1");
        remove.setInvertSelection(false);
        remove.setInputFormat(data);

        Instances dataSubset = Filter.useFilter(data, remove);
        return dataSubset;
    }


    /**
     * Trains and tests a classifier when two separated
     * datasets are provided.
     *
     * @param filteredTrain training data to build classifier
     * @param filteredTest  test data to evaluate classifier
     * @param classif  type of classifier applied
     * @throws Exception
     */
    public Evaluation classify(Instances filteredTrain, Instances filteredTest, Classifier classif, Instances test) throws Exception{

        StringBuffer sb = new StringBuffer();
        PlainText prediction = initPredictions(filteredTest);

        classif.buildClassifier(filteredTrain);
        Evaluation evaluateClassifier = new Evaluation(filteredTrain);
        evaluateClassifier.evaluateModel(classif, filteredTest, prediction, true);

        stats(evaluateClassifier);
//        outputSystemPredictions(evaluateClassifier.predictions(), test);

        return evaluateClassifier;
    }


    /**
     * Outputs classifier results.
     *
     * @param eval  Evaluation model built by a classifier
     * @throws Exception
     */
    public void stats(Evaluation eval) throws Exception{

        System.out.println("Number of attributes: " + eval.getHeader().numAttributes());
        System.out.println(eval.toSummaryString("\n======== RESULTS ========\n", false));
        System.out.println(eval.toClassDetailsString("\n\n======== Detailed accuracy by class ========\n"));
        System.out.println(eval.toMatrixString("\n\n======== Confusion Matrix ========\n"));
    }


    /****************************************************************************/


    private Instances selectAttributes(Instances data, String task){

        Instances selectedData = null;

        try {
            if(task.contains("train")) {
                selector.SelectAttributes(data);
                selector.toResultsString();

                System.out.println("Attributes reduced from " + data.numAttributes() + " to " + selector.selectedAttributes().length);
                System.out.println(selector.toResultsString());
            }

            selectedData = selector.reduceDimensionality(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return selectedData;
    }

    private AttributeSelection initializeCSF(){
        CfsSubsetEval cfsEval = new CfsSubsetEval();
        cfsEval.setPreComputeCorrelationMatrix(true);
        BestFirst bestSearch = new BestFirst();
        String[] searchOpts = {"-D 1","-N 5"};
        AttributeSelection selector = new AttributeSelection();

        try {
            bestSearch.setOptions(searchOpts);

            selector.setSearch(bestSearch);
            selector.setEvaluator(cfsEval);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return selector;
    }



    /**
     * Uses evaluation of features according to
     * selection method to remove attributes from
     * the dataset before training phase.
     *
     * @param threshold selection method threshold
     * @param values evaluation of attributes according to method
     * @param data dataset instances
     * @return filtered dataset instances
     * @throws Exception
     */
    private Instances applyFilter(String threshold, double[] values, Instances data) throws Exception{
        int numberRemoved = 0;

        String indexRemove = "";

        for(int i = 0; i < values.length; i++){
            if(values[i] == 0){

                int ind = i+1;

                if(indexRemove.length()==0) indexRemove = ind + "";
                else indexRemove = indexRemove + "," + ind;

                numberRemoved++;
            }
        }

        try{
            indexRemove = indexRemove.substring(0, indexRemove.length()-1);
            //if(verbose)
            System.out.println("\n = = = = => Filter removed " + numberRemoved +" attributes: " + indexRemove.toString() );
        }
        catch (Exception e){
            System.out.println("\n = = = = => Filter threshold did not remove any attribute.");
        }

        Remove remove = new Remove();
        remove.setAttributeIndices(indexRemove);
        remove.setInvertSelection(false);
        remove.setInputFormat(data);

        Instances dataSubset = Filter.useFilter(data, remove);
        return dataSubset;
    }

}
