package analyse;

import common.Post;
import common.User;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.Assert.*;

/**
 * Created by mqueudot on 11/02/16.
 */
public class UserPostHistoryRecoveryTest {
    @org.junit.Test
    public void testRecoverUsersFromCorpus() {
        final String FIRST_ID = "101";
        final String SECOND_ID = "102";

        final String FIRST_USER_ID = "0001";
        final String SECOND_USER_ID = "0002";

        Post a = new Post();
        a.setValue("threadId",FIRST_ID);
        a.setValue("subject","I'm not feeling good");
        a.setValue("postTime","2015-01-01T09:00:00+00:00");
        a.setValue("authorId",FIRST_USER_ID);

        Post b = new Post();
        b.setValue("threadId",SECOND_ID);
        b.setValue("subject","Something unrelated");
        b.setValue("postTime","2015-01-01T09:15:00+00:00");
        b.setValue("authorId",FIRST_USER_ID);


        Post c = new Post();
        c.setValue("threadId",FIRST_ID);
        c.setValue("subject","I hope you're better now");
        c.setValue("postTime","2016-01-01T09:00:00+00:00");
        c.setValue("authorId",SECOND_USER_ID);

        UserHistoryRecovery userHistoryRecovery = UserHistoryRecovery.getInstance();
//        Map<Integer, User> users = userHistoryRecovery.recoverUsersFromCorpus(Arrays.asList(a,b,c));
        Map<String, User> users = userHistoryRecovery.createUserPostMap();

        assertEquals("There should be 2 users", users.size(), 2);
        TreeSet<Post> userPostHistory = users.get(Integer.parseInt(FIRST_USER_ID)).getUserPosts();
        assertEquals("User1's history should be of size 2.",userPostHistory.size(),2);
        assertEquals("User 1's posts should be in his history and in chronological order (check first)",
                userPostHistory.pollFirst(), a);
        assertEquals("User 1's posts should be in his history and in chronological order (check second)",
                userPostHistory.pollFirst(), b);
    }


}