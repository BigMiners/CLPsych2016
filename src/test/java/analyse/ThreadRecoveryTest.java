package analyse;


import common.Configs;
import common.Post;
import common.Thread;
import parser.PostParser;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by mqueudot on 11/02/16.
 */
public class ThreadRecoveryTest {
    @org.junit.Test
    public void testRecoverThreadsFromCorpusGeneralBehaviour() throws Exception {
        final String FIRST_ID = "101";
        final String SECOND_ID = "102";


        ThreadRecovery tr = new ThreadRecovery();
        Post a = new Post();
        a.setValue("threadId",FIRST_ID);
        a.setValue("subject","I'm not feeling good");
        a.setValue("postTime","2015-01-01T09:00:00+00:00");

        Post c = new Post();
        c.setValue("threadId",FIRST_ID);
        c.setValue("subject","I hope you're better now");
        c.setValue("postTime","2016-01-01T09:00:00+00:00");

        Post b = new Post();
        b.setValue("threadId",SECOND_ID);
        b.setValue("postTime","2015-01-01T09:15:00+00:00");


        List<Post> posts = Arrays.asList(a,b,c);
        Map<Integer, Thread> threads = tr.recoverThreadsFromCorpus(posts);

        assertTrue("Recover threads for a list of posts with only two distinct threadIds should return 2 threads",
                threads.size() == 2);

        Thread t = threads.get(Integer.parseInt(FIRST_ID));
        assertFalse("The map of threads should not return null when getting the object linked to an existing threadId",t == null);
        assertTrue("A thread with a specific threadId should contain a post which references this threadId.",
                t.getPosts().contains(a));

        TreeSet<Post> tree = (TreeSet) t.getPosts();
        assertEquals("The thread should contain 2 posts.", tree.size(), 2);
        assertEquals("The first post in thread should be the oldest one.",tree.pollFirst(),a);
        assertEquals("The second post in thread should be the second oldest one.",tree.pollFirst(),c);


    }

    @org.junit.Test
    public void testRecoverThreadsFromCorpusOnCrisisData() {
        ThreadRecovery threadRecovery = new ThreadRecovery();
        PostParser pp = new PostParser();
        Map<Integer, Thread> threads = threadRecovery.recoverThreadsFromCorpus(
//                pp.parsePostsInDirectory(Configs.getINSTANCE().getProps().getProperty("CORPUS_DIRECTORY")+"/crisis")
                pp.parseCorpus(Configs.getInstance().getProps().getProperty("CORPUS_DIRECTORY")+"/crisis")
        );
        assertFalse("At least one user history should be recovered from the crisis posts",
                threads.isEmpty());
    }
}