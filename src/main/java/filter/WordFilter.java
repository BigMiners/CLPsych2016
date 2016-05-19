package filter;

import common.Configs;
import common.IOUtil;
import common.Post;
import extractor.Extractor;

import java.util.*;

/*
 * Created by halmeida on 2/4/16.
 *
 * Filter to remove zipfsWords from corpus.
 * Stopwords can be filtered using:
 * - a zipfsWords list
 * - occurrence of corpus words
 *
 */
public class WordFilter {


    ArrayList<String> zipfsWords = new ArrayList<String>();
    ArrayList<String> stopWords = new ArrayList<String>();

    String location;
    Properties props;
    String stopFile;

    public WordFilter(){
        props = Configs.getInstance().getProps();

        location = props.getProperty("WORK_DIRECTORY") + "/" +
                props.getProperty("RESOURCES_DIR") + "/";
                //props.getProperty("FEATURE_DIR") + "/"

        stopFile =  props.getProperty("STOP_FILE");
    }


    /**
     * Removes filter words from a
     * given word list
     * HA
     * @param words
     * @return
     */
    public ArrayList<String> filterWordByList(ArrayList<String> words, String list){

        ArrayList<String> result = new ArrayList<>();

        for(int i = 0; i < words.size(); i ++){
            if(list.contains("zipf")) {
                if (!zipfsWords.contains(words.get(i))) {
                    result.add(words.get(i));
                }
            }
            else if(list.contains("stop")){
                if (!stopWords.contains(words.get(i))) {
                    result.add(words.get(i));
                }
            }
        }
        return result;
    }
    

    public int loadStopWords(){
        stopWords = IOUtil.getINSTANCE().loadFeatureList(location+stopFile);
        return stopWords.size();
    }



    public int loadOccByZipfs(HashMap<String,Integer> occurence, String step){

        HashMap<String, Integer> zipfs = new HashMap<>();

        // sort features by occurence
        SortedSet<Map.Entry<String,Integer>> sorted = sortMapByValues(occurence);

        Iterator iterator = sorted.iterator();
        while(iterator.hasNext()){
            Map.Entry<String,Integer> entry = (Map.Entry<String,Integer>) iterator.next();
            if (entry.getValue() < 10 || entry.getValue() > 500){
                zipfs.put(entry.getKey(), entry.getValue());
                zipfsWords.add(entry.getKey());
            }
        }

        System.out.println("Exporting zipfs list...");
        IOUtil.getINSTANCE().exportFeatures(location+"zipfslist.txt", zipfs, 0, step);

        return zipfsWords.size();
    }



      /**
     * @param map Give this method a map <String, Int>
     * @return it will return a SortedSet, sorted by values.
     */
      public <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> sortMapByValues(Map<K, V> map) {
          SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
                  new Comparator<Map.Entry<K, V>>() {
                      @Override
                      public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                          int res = e1.getValue().compareTo(e2.getValue());
                          return res != 0 ? res : 1;
                      }
                  }
          );
          sortedEntries.addAll(map.entrySet());
          return sortedEntries;
      }

}
