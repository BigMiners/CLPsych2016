package parser;

import analyse.UserHistoryRecovery;
import common.Configs;
import common.Post;
import common.User;
import extractor.NgramExtractor;
import extractor.POSExtractor;
import extractor.SentimentExtractor;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;


/**
 * Created by halmeida on 2/8/16.
 */

/**
 * Test class for Parser
 * (and Extractors if needed)
 */
public class PostParserTest extends TestCase {

    @org.junit.Test
    public void testParseCorpus() throws IOException {

        System.setProperty("config.file", "./config.properties");

        PostParser parser = new PostParser();
        NgramExtractor extractor = new NgramExtractor();

        List<Post> list = parser.parseCorpus("");
    //    List<Post> listHistory = new ArrayList<Post>(list);

//        UserHistoryRecovery userRecover = new UserHistoryRecovery();
//        Map<String, User> userPostMap =  userRecover.createUserPostMap();
//
//        System.out.println("History of " + userPostMap.size() + " users retrieved.");
//
//        for(Post post : list){
//            TreeSet<Post> histPost = userRecover.getHistoricContext(post);
//            if(histPost.size() > 0){
//                Post onePost = histPost.first();
//                onePost.setValue(onePost.POST_CLASS, post.getValue(post.POST_CLASS));
//                listHistory.add(onePost);
//            }
//        }
//
//        System.out.println("Post list enchanced from " + list.size() + " to " + listHistory.size());

        String text = extractor.selectPostContent(list.get(0), "extractor");

        //test normalization
        String normText = extractor.normalizePost(text);
        ArrayList<String> normPost = new ArrayList<String>();
        normPost.add(normText);

        //test ngrams extraction
        ArrayList<String> ngrams = new ArrayList<String>();
        ngrams.add(text);
        int ngramcount = extractor.extractFeatures(list, "extract");

        //test POS tag + extraction
        POSExtractor posExtractor = new POSExtractor();
        int poscount = posExtractor.extractFeatures(list,"extract");

        //test stemming + sentiments
        SentimentExtractor senExtractor = new SentimentExtractor();
        int sentcount = senExtractor.extractFeatures(list, "extract");


        System.out.println("Plain post: " + list.get(0));
        System.out.println("Normalized post: " + normText);
        System.out.println("Ngrams extracted: " + ngramcount);
        System.out.println("POS tagged: " + poscount);
        System.out.println("Lemmas: " + sentcount);

        System.out.println("\nPosts correctly parsed, features correctly extracted. Happy classifying! \n");


    }

    @org.junit.Test
    public void testParseCorpusv2() {
        PostParser pp = new PostParser();
        List<Post> crisisPosts = pp.parseCorpus(Configs.getInstance().getProps().getProperty("CORPUS_DIRECTORY")+"/crisis");
        assertFalse("Parsing the corpus should not return an empty list.",crisisPosts.isEmpty());
        List<Post> posts = pp.parseCorpus("");

        assertTrue("The number of crisis posts should be less than than the whole corpus.",
                crisisPosts.size()<posts.size());    }

}