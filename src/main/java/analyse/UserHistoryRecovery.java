package analyse;

import common.Configs;
import common.Post;
import common.User;
import parser.PostParser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mqueudot on 11/02/16.
 */
public class UserHistoryRecovery {

    private Map<String, User> users;
    private int nPrevious;
    private String unlabeledPosts;
    private String labelledPosts;
    String outputPostsWithContext;
    PostParser postParser;
    private float weight;
    Properties props;

    /** Pre-initialized unique instance */
    private static UserHistoryRecovery INSTANCE = new UserHistoryRecovery();
    public static UserHistoryRecovery getInstance() {
        return INSTANCE;
    }

    private UserHistoryRecovery() {
        props = Configs.getInstance().getProps();
        nPrevious = Integer.parseInt(props.getProperty("N_PREVIOUS"));
        unlabeledPosts = props.getProperty("NON_ANNOTATED_DIRECTORY");
        labelledPosts = props.getProperty("CORPUS_DIRECTORY")+"/"+props.getProperty("TASK");
        outputPostsWithContext = props.getProperty("OUTPUT_POSTS_WITH_CONTEXT");
        users = new HashMap<>();
        postParser = new PostParser();
        weight = Float.parseFloat(props.getProperty("USER_HIST_WEIGHT"));
    }

    private void keepStats(User user, Post post) {
        //Important to keep track of the green ones, so that it's not the same as adding all the unlabelled posts the green counter
        if(post.getValue(post.POST_CLASS)!=null && !post.getValue(post.POST_CLASS).equals("")) {
            user.addLabelOccurence(post.getValue(post.POST_CLASS));
        }
    }


    public List<Post> addLabelOccurrences(List<Post> posts) {
        if(isUserPostMapEmpty()) {
            return posts;
        }
        for (Post post : posts) {
            User author = users.get(post.getValue(post.AUTHOR_ID));
            Map<String, Integer> labelOccurrences = author.getLabelOccurences();
            labelOccurrences.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals("?"))
                    .forEach(entry -> {
                String test = convertNaming(entry.getKey());
                post.setValue(test, entry.getValue().toString());
            });
        }
        return posts;
    }

    private String convertNaming(String label) {
        label = label.replaceFirst("CLPsychLabel","");
        String end = label.subSequence(1,label.length()).toString();
        StringBuilder sb = new StringBuilder("nb");
        sb.append(Character.toUpperCase(label.charAt(0)));
        sb.append(end);
        sb.append("Class");
        return sb.toString();
    }

    public List<Post> addUserHistory(List<Post> labeledPosts){
        List<String> labeledPostsId = postParser.getLabeledIDs(labeledPosts);

        createUserPostMap();

        for(int i = 0; i < labeledPosts.size(); i ++){
            Post post = labeledPosts.get(i);
            //retrieve user history for given post
            //use labeled IDs to avoid adding duplicates in list
            TreeSet<Post> history =  getHistoricContext(post,labeledPostsId);
            if(history.size() > 0) {
                //add not duplicated post History to post list
                Iterator<Post> iter = history.iterator();

                while(iter.hasNext()){
                    //first attempt: add histPost as a new doc in the vector
//                   labeledPosts.add(iter.next());

                    //second attempt: add content of hsitPost to current post
                    Post historyPost = iter.next();
                    String historyContent = historyPost.getValue(historyPost.BODY);
                    String currentContent = post.getValue(post.BODY);
                    post.setValue(post.BODY, (currentContent +" "+  historyContent));
                    labeledPosts.set(i, post);
                }
            }
        }
        return labeledPosts;
    }

    /**
     * Retrieves a UserHistory from the posts in the corpus in parameters.
     * @return
     */
    protected Map<String, User> createUserPostMap() {

        List<Post> labelledCorpus = postParser.parseCorpus(labelledPosts);
        for(Post labelledPost : labelledCorpus) {
            String userId = labelledPost.getValue(labelledPost.AUTHOR_ID);

            if(!users.containsKey(userId)){
                User oneUser = new User(userId);
                keepStats(oneUser,labelledPost);
                oneUser.addPost(labelledPost);
                users.put(userId, oneUser);
            }
            else{
                User oneUser = users.get(userId);
                keepStats(oneUser, labelledPost);
                oneUser.addPost(labelledPost);
            }
        }

        List<Post> corpus = postParser.parseCorpus(unlabeledPosts);

        for (Post post : corpus) {
            post.setValue(post.WEIGHT, String.valueOf(weight));
            String userId = post.getValue(post.AUTHOR_ID);

            if(!users.containsKey(userId)){
                User oneUser = new User(userId);
                keepStats(oneUser,post);
                oneUser.addPost(post);
                users.put(userId, oneUser);
            }
            else{
                User user = users.get(userId);
                //Really important : avoids that the labelled post be replaced by the non labelled one.
                if(!user.getUserPosts().contains(post)) {
                    keepStats(user, post);
                    user.addPost(post);
                }
            }
        }

        System.out.println("User history recovery processed " + corpus.size() + " posts finding " + users.size() +" users with history");
        return users;
    }

    /**
     * For a given post, returns the nPrevious previous posts in anti-chronological order.
     * @param post
     * @return
     */
    private TreeSet<Post> getHistoricContext(Post post, List<String> postsId) {

        TreeSet<Post> lastNposts = new TreeSet<>();
        String userId = post.getValue(post.AUTHOR_ID);
        String postClass = post.getValue(post.POST_CLASS);

        if(users.containsKey(userId)){
            User user = users.get(userId);
            TreeSet<Post> userPosts = user.getUserPosts();
            TreeSet subset = (TreeSet) userPosts.headSet(post);
            Iterator<Post> it = subset.descendingIterator();

            for(int i = 0 ; i < nPrevious ; i++ ) {
                if(it.hasNext()) {
                    Post historyPost = it.next();
                    //adding the class to a post from the raw data
                    historyPost.setValue(historyPost.POST_CLASS, postClass);
                    //make sure we do not add duplicates from labeled posts
                    if(!postsId.contains(historyPost.getValue(historyPost.POST_ID)))
                        lastNposts.add(historyPost);
                }
            }
        }

        return lastNposts;
    }


     /***********************************************************************************************/

    /**
     * Retrieves userHistory for every user.
     * Takes a veeeeerry long time if it's the first time it's called in the program execution.
     * @return
     */
    public Map<String, User> getUsers() {
        if(!users.isEmpty()) {
            return users;
        } else {
            return createUserPostMap();
        }
    }

    public boolean isUserPostMapEmpty() {
        return users.isEmpty();
    }

    /**
     * Returns the userHistory for every user whose id is in the the parameter "ids".
     * @param ids
     * @return
     */
    public Map<String, User> getUsersByIds(Set<Integer> ids) {
        return getUsers().entrySet()
                .stream()
                .filter(p -> ids.contains(p.getKey()))
                .collect(Collectors.toMap(p->p.getKey(),p->p.getValue()));
    }

    /**
     * Returns the userHistory for the user whose id is the parameter "id".
     * @param id
     * @return
     */
    public User getUserById(String id) {
        return getUsers().get(id);
    }


//    /**
//     * Gives context to a list of posts. A context is the nPrevious previous posts of the same author.
//     * @param posts
//     * @return
//     */
//    public List<TreeSet<Post>> getHistoricalContext(List<Post> posts) {
//        List<TreeSet<Post>> postsInContext = new ArrayList<>();
//        for(Post post : posts) {
//            TreeSet<Post> postWithContext = getHistoricContext(post);
//            //If we can't get no context, we put the post anyway.
//            if(postWithContext.isEmpty()) {
//                postWithContext.add(post);
//            }
//            postsInContext.add(postWithContext);
//        }
//        return postsInContext;
//    }

    public void writePostsInContext(List<TreeSet<Post>> postsInContext) {
        File outputDirectory = new File(outputPostsWithContext);
        for(File file: outputDirectory.listFiles()) file.delete();
        parallelWrite(postsInContext);
//        sequentialWrite(postsInContext);
    }
    private void sequentialWrite(List<TreeSet<Post>> postsInContext) {
        for(TreeSet<Post> onePostWithContext : postsInContext) {
            String outputFilename = "post-"+onePostWithContext.last().getValue(onePostWithContext.first().POST_ID)+".xml";

            for(Post post : onePostWithContext) {
                String postFilename = "post-"+post.getValue(post.POST_ID)+".xml";
                String postContent;
                try {
                    postContent = readFile(unlabeledPosts +"/"+postFilename,Charset.forName("utf8")).split("(\\n)+",2)[1];
                    writeFile(outputPostsWithContext+"/"+outputFilename, postContent);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void parallelWrite(List<TreeSet<Post>> postsInContext) {

        postsInContext.parallelStream().forEach((treeset) -> {
            String outputFilename = "post-"+treeset.last().getValue(treeset.first().POST_ID)+".xml";
            treeset.forEach((post) -> {
                String postFilename = "post-"+post.getValue(post.POST_ID)+".xml";
                String postContent;
                try {
                    postContent = readFile(unlabeledPosts +"/"+postFilename,Charset.forName("utf8")).split("(\\n)+",2)[1];
//                    outputPostWithContext+=readFile(postFilename,Charset.forName("utf8")).split("\n",1)[1];
                    writeFile(outputPostsWithContext+"/"+outputFilename, postContent);
                } catch (IOException e) {
                    e.printStackTrace();
                    postContent="";
                }
            });
        });
    }

    private String readFile(String path, Charset encoding) throws IOException {
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
    }

}
