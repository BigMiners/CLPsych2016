package parser;

import analyse.UserHistoryRecovery;
import common.Configs;
import common.Post;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by mqueudot on 29/01/16.
 */
public class PostParser {

    private final String ID_TAG = "/id/";
    private String work_dir = "";
    private String task = "";
    private String location = "";
    Properties props;


    public PostParser(){
        props = Configs.getInstance().getProps();
        work_dir = props.getProperty("CORPUS_DIRECTORY");
        task = props.getProperty("TASK");
        location = work_dir + "/"
                + task
//              + "/crisis"
        ;

    }

     /**
     * Parse all the posts in the annotated corpus.
     * @return
     */
    public ArrayList<Post> parseCorpus(String path){

        File[] xmlPosts;

        if(!path.isEmpty()) xmlPosts = getCorpus(path);
        else xmlPosts = getCorpus(location);

        ArrayList<Post> parsedPosts = new ArrayList<Post>();

        try {
            for (File file : xmlPosts) {
                Post thisPost = parseXMLPost(file);
                parsedPosts.add(thisPost);
            }
        }catch(NullPointerException e){
            System.out.println("Corpus directory for task " + task + " not found. " +
                    "\n Please check if directory is properly created. " + e.getMessage());
        }
        System.out.println("Corpus parsed for: " + parsedPosts.size() +" documents.");
        return parsedPosts;
    }

    public ArrayList<String> getLabeledIDs(List<Post> posts){

        ArrayList<String> parsedIds = new ArrayList();

        for (Post post : posts){
            String id = post.getValue(post.POST_ID);
            parsedIds.add(id);
        }
        return parsedIds;
    }


    private File[] getCorpus(String path){

        File pathFile = new File(path);
        File[] srcXMLs = pathFile.listFiles(new FilenameFilter(){
                @Override
                public boolean accept(File dir, String name){return name.endsWith(".xml");
                }
            });

        return srcXMLs;
    }

    private Post parseXMLPost(File file){
        Document doc = new Document("");

        try {
            doc = Jsoup.parse(file, "utf-8");
        } catch (IOException e) {
            System.err.println("Failed to parse file: " + file + e);
        }
        Post post = new Post();

        String postId = doc.getElementsByTag("message").first().attr("href");
        if(postId.contains("/id/")) postId = postId.substring(postId.indexOf("/id/")+4);
        post.setValue(post.POST_ID, postId);

        String postSubject = doc.getElementsByTag("subject").first().text();
        post.setValue(post.SUBJECT, postSubject);

        /* HA : might need to be changed...
        // for some reason, when creating internal HTML parsed text,
        // the body tag changes levels:
        // - <message> is child of <body>
        // - <message> holds <body> text content
        // previous solution:
        //String postBody = doc.getElementsByTag("body").text(); */

        String postBody = doc.getElementsByTag("message").first().ownText();
        post.setValue(post.BODY, postBody);

        String boardId = doc.getElementsByTag("board_id").first().text();
        post.setValue(post.BOARD_ID, boardId);

        String authorId = doc.getElementsByTag("author").first().attr("href");
        if(authorId.contains(ID_TAG)) authorId = authorId.substring(authorId.indexOf(ID_TAG)+4);
        post.setValue(post.AUTHOR_ID,authorId);

        String authorLogin = doc.getElementsByTag("author").first().child(0).text();
        post.setValue(post.AUTHOR_LOGIN,authorLogin);

        String lastAuthorId = doc.getElementsByTag("last_edit_author").attr("href");
        if(lastAuthorId.contains(ID_TAG)) lastAuthorId = lastAuthorId.substring(lastAuthorId.indexOf(ID_TAG)+4);
        post.setValue(post.LAST_AUTHOR_ID,lastAuthorId);

        String threadId = doc.getElementsByTag("thread").first().attr("href");
        if(threadId.contains(ID_TAG)) threadId = threadId.substring(threadId.indexOf(ID_TAG)+4);
        post.setValue(post.THREAD_ID, threadId);

        String lastAuthorLogin = doc.getElementsByTag("last_edit_author").first().child(0).text();
        post.setValue(post.LAST_AUTHOR_LOGIN,lastAuthorLogin);

        String postTime = doc.getElementsByTag("post_time").first().text();
        post.setValue(post.POST_TIME,postTime);

        String editTime = doc.getElementsByTag("last_edit_time").first().text();
        post.setValue(post.LAST_EDIT_TIME,editTime);

        // HA: adding a prefix avoids that these
        // integer values get lost and filtered out
        String kudos = "postKudos" + doc.getElementsByTag("kudos").first().child(0).text();
        post.setValue(post.KUDOS,kudos);

        String views = "postViews" + doc.getElementsByTag("views").first().child(0).text();
        post.setValue(post.VIEWS,views);

        if(doc.getElementsByTag("clpsychlabel")!=null && !doc.getElementsByTag("clpsychlabel").isEmpty()) {
            String postClass = "CLPsychLabel" + doc.getElementsByTag("clpsychlabel").first().text();
            post.setValue(post.POST_CLASS, postClass);
        }
        else post.setValue(post.POST_CLASS, "?");

        return post;
    }

    private Date formatDate(String dateTime) throws ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        return df.parse(dateTime);
    }

    public String getLocation() {
        return location;
    }
}
