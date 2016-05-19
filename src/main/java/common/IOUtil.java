package common;

import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by halmeida on 2/24/16.
 */
public class IOUtil {


    private static IOUtil INSTANCE = new IOUtil();

    public static IOUtil getINSTANCE(){
        return INSTANCE;
    }

    /**
     * Loads a unique dictionary
     * from a list of dictionary files
     * HA
     * @param path dictionary files variable (separated by ",")
     * @return
     */
    public HashMap<String, String> loadDictionary(String path, String file){

        String[] files = file.split(",");
        HashMap<String, String> dictionary = new HashMap<>();

        for(int i = 0; i < files.length; i++){
            String filename = files[i];
            dictionary.putAll(loadResourceList(path+filename));
        }

        return dictionary;
    }

    public HashMap<String,String> loadResourceList(String file){

        HashMap<String,String> list = new HashMap<String,String>();
        try{
            String featureLine = "";

            //listing features
            BufferedReader reader = new BufferedReader(new FileReader(file));

            int featureCount = 0;
            while (( featureLine = reader.readLine()) != null) {

                String[] content = StringUtils.split(featureLine,"\n");

                for(int i = 0; i < content.length; i++){
                    int count = i;
                    String[] featurename = null;
                    if(!content[i].contains("#")) featurename = StringUtils.split(content[i],"\t");
                    String key = "", value = "";

                    if(featurename != null) {
                        if (!file.contains("mapping")) {
                            // invert insertion in map, so resources
                            // are unique per value instead of per concept
                            key = featurename[0];
                            if(featurename.length > 1) value = featurename[1];

                        } else{
                            // invert insertion in map, so resources
                            // are unique per value instead of per concept
                            key = featurename[1];
                            value = featurename[0];
                        }
                        //check for duplicate features
                        if (!list.keySet().contains(key))
                            list.put(key, value);
                    }
                }
            }
            reader.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Loads feature list found in a file.
     * (e.g. stopword list, ngram list, sentiment list...)
     * HA
     * @param file
     * @return
     */
    public ArrayList<String> loadFeatureList(String file){

        ArrayList<String> list = new ArrayList<String>();
        try{
            String featureLine = "";

            //listing features
            BufferedReader reader = new BufferedReader(new FileReader(file));

            int featureCount = 0;
            while (( featureLine = reader.readLine()) != null) {

                String[] content = StringUtils.split(featureLine,"\n");

                for(int i = 0; i < content.length; i++){
                    String[] featurename = StringUtils.split(content[i],"\t");

                    //check for duplicate features
                    if(!(list.contains(featurename[0]))){
                        list.add(featurename[0]);
                    }
                }
            }
            reader.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Exports feature list to a given file
     * HA
     * @param location
     * @param list
     */
    public void exportFeatures(String location, HashMap<String,Integer> list, int numberDocs, String step){
        String SEPARATOR = "\n";
        StringBuffer line = new StringBuffer();

        if(!step.contains("UniTest")) {
            try {

                for (Map.Entry<String, Integer> entry : list.entrySet()) {
                    if (entry != null) {
                        String str = entry.getKey() + "\t" + entry.getValue();
                        line.append(str).append(SEPARATOR);
                    }
                }

                BufferedWriter writer = new BufferedWriter(new FileWriter(location, false));

                writer.write((line.toString()));
                writer.flush();
                writer.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Done! " + list.size() + " features extracted to file from " + numberDocs + " documents.\n");
        }
        else System.out.println("Done! " + list.size() + " features extracted for testing from " + numberDocs + " documents.\n");
    }
	
	 /**
     * Generic function for writing something to a given file
     */
    public void writeOutput(String path, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(content);
        writer.flush();
        writer.close();
        System.out.println("Predictions outputted in " + path);
    }

}
