package extractor;

import common.IOUtil;
import common.Post;
import edu.stanford.nlp.ling.WordTag;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by halmeida on 3/4/16.
 */
public class VocabularyExtractor extends Extractor{

    String vocabFile;
    String vocabDictionary;
    String vocabDir;


    public VocabularyExtractor(){
        super();

        vocabFile = props.getProperty("VOCABULARY_FILE");
        vocabDictionary = props.getProperty("VOCABULARY_DIC");
        vocabDir = work_dir + resources_dir + props.getProperty("VOCABULARY_DIC_DIR") + "/";
    }

    @Override
    public int extractFeatures(List<Post> postContent, String step) {

        //load vocabulary dictionaries
        HashMap<String, String> vocabDic = IOUtil.getINSTANCE().loadDictionary(vocabDir, vocabDictionary);

        for(int i = 0; i < postContent.size(); i++) {
            //normalize post content and get POS tags
            String text = normalizePost(selectPostContent(postContent.get(i),"extract"));
            ArrayList<String> words = tokenizePost(text);

            // handle extra punctuation on a single token
            words = normalizeSingleWord(words);

            for(int j = 0; j < words.size(); j++) {
                String thisWord = words.get(j);

                //if word is in the vocab dictionary
                if(vocabDic.keySet().contains(thisWord)){
                    //add to feature list
                    featureList = setOccPerFeature(thisWord, featureList);
                }
            }
        }

        System.out.println("Exporting Vocabulary features...");
        IOUtil.getINSTANCE().exportFeatures(location + vocabFile, featureList, postContent.size(), step);

        return featureList.size();
    }


}
