package common;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by mqueudot on 11/02/16.
 */
public class User {
    private String userId;
    private String login;
    //Posts of which this user is the author.
    private TreeSet<Post> userPosts;
    private Map<String, Integer> labelOccurences;

    public User(String userId) {
        this();
        this.userId = userId;
    }

    public User(){
        this.userPosts = new TreeSet<>();
        this.labelOccurences = new HashMap<>();
    }

    public TreeSet<Post> getUserPosts() {
        return userPosts;
    }

    public void addPost(Post post) {
        this.userPosts.add(post);
    }

    public Map<String, Integer> getLabelOccurences() {
        return labelOccurences;
    }

    public void setLabelOccurences(Map<String, Integer> labelOccurences) {
        this.labelOccurences = labelOccurences;
    }

    public void addLabelOccurence(String label) {
        if(this.labelOccurences.containsKey(label)) {
            this.labelOccurences.put(label, labelOccurences.get(label) + 1);
        } else {
            this.labelOccurences.put(label,1);
        }
    }
}
