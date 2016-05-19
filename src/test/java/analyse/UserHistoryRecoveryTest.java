package analyse;

import common.Configs;
import common.Post;
import common.User;
import parser.PostParser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by mqueudot on 23/02/16.
 */
public class UserHistoryRecoveryTest {

    @org.junit.Test
    public void testRecoverUsersHistoryFromCorpus() {
        UserHistoryRecovery userHistoryRecovery = UserHistoryRecovery.getInstance();
        PostParser pp = new PostParser();
        Map<String, User> users = userHistoryRecovery.createUserPostMap();
//                pp.parseCorpus(Configs.getInstance().getProps().getProperty("CORPUS_DIRECTORY")+"/crisis")
//        );
        assertFalse("At least one user history should be recovered from the crisis posts",
                users.isEmpty());
//        Map<Integer, User> generalUsers = userHistoryRecovery.recoverUsersFromCorpus(pp.parseAnnotatedCorpus());
//        Map<String, User> generalUsers = userHistoryRecovery.createUserPostMap();
//
//        assertTrue("",users.size()<generalUsers.size());
//        User talitha93 = generalUsers.get(6384);
        int[] stats = getStatsOnUsers(users);
        System.out.println("Number of users : "+users.size());
        System.out.println("Average number of posts per user : "+stats[0]);
        System.out.println("Maximum number of posts per user : "+stats[1]);
        System.out.println("Mimimum number of posts per user : "+stats[2]);

    }

    @org.junit.Test
    public void testGetUsersByIds() {
        UserHistoryRecovery historyRecovery = UserHistoryRecovery.getInstance();
        //Bad test : is highly dependant of the data
//        Set<Integer> ids = new HashSet<>(Arrays.asList(6384,5155,6852,5111));
//        Map m =historyRecovery.getUsersByIds(ids);
//        assertTrue("Asking for 4 existing usersIds should return a map of 4 entries.",
//                m.size()==4);

        PostParser postParser = new PostParser();
        List<Post> crisisPosts = postParser.parseCorpus(Configs.getInstance().getProps().getProperty("CORPUS_DIRECTORY")+"/crisis");
        Set<Integer> ids = new HashSet<>();
        for(Post post : crisisPosts) {
            ids.add(Integer.parseInt(post.getValue("authorId")));
        }
        Map m2 = historyRecovery.getUsersByIds(ids);
        assertTrue("Asking for n existing usersIds should return a map of n entries.",
                m2.size()==ids.size());
    }

    @org.junit.Test
    public void getStatsOnCorpus() {
        PostParser parser = new PostParser();
        UserHistoryRecovery historyRecovery = UserHistoryRecovery.getInstance();
//        UserHistoryRecovery historyRecovery = new UserHistoryRecovery();

//        Properties properties = Configs.getInstance().getProps();
//        List<Post> allPosts = parser.parseCorpus(properties.getProperty("NON_ANNOTATED_DIRECTORY"));
//        Map m3 = historyRecovery.recoverUsersFromCorpus(allPosts);
        Map m3 = historyRecovery.createUserPostMap();

        int[] stats = getStatsOnUsers(m3);
        System.out.println("Number of users : "+m3.size());
        System.out.println("Average number of posts per user : "+stats[0]);
        System.out.println("Maximum number of posts per user : "+stats[1]);
        System.out.println("Mimimum number of posts per user : "+stats[2]);
    }



    /**
     *  // [0] : avg, [1] : nMax, [2] : nMin
     * @param users
     * @return
     */
    private int[] getStatsOnUsers(Map<String, User> users) {

        // [0] : sum, [1] : nMax, [2] : nMin
        int[] stats = {0,0,Integer.MAX_VALUE};
//        users.forEach((k,v)->sum+=v.getMyPosts().size());
        for(User user : users.values()) {
            int size =  user.getUserPosts().size();
            stats[0] += size;
            stats[1] = size>stats[1] ? size : stats[1];
            stats[2] = size<stats[2] ? size : stats[2];
        }

        stats[0]=stats[0]/users.size();

        return stats;
    }

//    @org.junit.Test
//    public void dummyTest() throws IOException {
//        Properties properties = Configs.getInstance().getProps();
//        String nonAnnotatedCorpusDirectory = properties.getProperty("NON_ANNOTATED_DIRECTORY");
//        String postFilename = "post-13656.xml";
//        String postContent = readFile(nonAnnotatedCorpusDirectory+"/"+postFilename, Charset.forName("utf8")).split("(\\n)+",2)[1];
//        System.out.println(postContent);
//    }

/*    private String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    private void writeFile(String path, String content) throws IOException {
        try {
            Files.write(Paths.get(path), content.getBytes("utf8"), StandardOpenOption.APPEND);
        } catch (NoSuchFileException e) {
            Files.createFile(Paths.get(path));
            writeFile(path, content);
        }
    }*/

}