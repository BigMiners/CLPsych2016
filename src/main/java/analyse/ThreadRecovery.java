package analyse;

/**
 * Created by mqueudot on 10/02/16.
 */

import common.Post;
import common.Thread;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ThreadRecovery {

    /**
     * Takes the corpus as an input and recovers a list of threads from it.
     * A thread object is identified by its threadId.
     * @param posts
     * @return
     */
    public Map<Integer, Thread> recoverThreadsFromCorpus(List<Post> posts) {
        Map<Integer, Thread> threads = new HashMap<>();

        for(Post post : posts) {
            int threadId = Integer.parseInt(post.getValue("threadId"));
            if(threads.containsKey(threadId)) {
                Thread thread = threads.get(threadId);
                thread.addPost(post);
            } else {
                Thread t = new Thread(threadId);
                t.addPost(post);
                threads.put(threadId,t);
            }
        }

        return threads;
    }
}
