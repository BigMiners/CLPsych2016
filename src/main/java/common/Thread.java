package common;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by mqueudot on 10/02/16.
 */
public class Thread {

    int thread_id;
    TreeSet<Post> posts;

    public Thread(int thread_id) {
        this.thread_id = thread_id;
        this.posts = new TreeSet<>();
    }

    public void addPost(Post post) {
        posts.add(post);
    }

    public Set<Post> getPosts() {
        return posts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Thread thread = (Thread) o;

        return thread_id == thread.thread_id;
    }

    @Override
    public int hashCode() {
        return thread_id;
    }
}
