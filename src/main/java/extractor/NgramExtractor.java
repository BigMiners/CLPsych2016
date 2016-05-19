package extractor;

import common.IOUtil;
import common.Post;
import filter.WordFilter;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by halmeida on 2/4/16.
 */
public class NgramExtractor extends Extractor{

    String ngramFile = "";
    int ngramSize = 0;


    // HA: main constructor
    public NgramExtractor() {
        super();
        ngramSize = Integer.parseInt(props.getProperty("NGRAM_SIZE"));
        ngramFile = props.getProperty("NGRAM_FILE") + ngramSize;
    }


    /**
     * Extracts n-grams from a given text
     * and exports it to a file
     * HA
     * @param posts post text to extract ngramsList
     * @return list of normalized extracted ngramsList
     */
    @Override
    public int extractFeatures(List<Post> posts, String step) {

        int ngramLenght = ((ngramSize > 1) ? ngramSize - 1 : ngramSize);
        loadWordFilterLists(posts, step);

        // normalized and stop-word filtered posts
        ArrayList<String> words, filteredWords = null;

        for(int i = 0; i < posts.size(); i++) {

            String text = selectPostContent(posts.get(i), step);
            text = normalizePost(text);
            words = tokenizePost(text);

            // handle extra punctuation on a single token
            words = normalizeSingleWord(words);
            int sizeFilter = words.size();

            // HA TODO: clean up stop words
            if(useStopList) words = wordFilter.filterWordByList(words, "stop");

            if(useZipfList) words = wordFilter.filterWordByList(words, "zipf");

            //generate a ngram according to size of "n"
            for (int j = 0; j < words.size() - ngramLenght; j++) {
                String ngram = "";
                int size = 0;

                do {
                    if (ngram.isEmpty())
                        ngram = words.get(j + size).toLowerCase();
                    else
                        ngram += " " + words.get(j + size).toLowerCase();
                    size++;
                } while (size < ngramSize);

                //export feature list
                featureList = setOccPerFeature(ngram, featureList);
            }
        }
        System.out.println("Exporting Ngram features...");
        IOUtil.getINSTANCE().exportFeatures((location + ngramFile), featureList, posts.size(), step);

        return featureList.size();
    }



}
