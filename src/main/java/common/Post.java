package common;

import analyse.UserHistoryRecovery;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mqueudot on 29/01/16.
 */
public class Post implements Comparable{

    private HashMap<String, String> fields = new HashMap<String, String>();

    public final String POST_ID = "postId";
    public final String SUBJECT = "subject";
    public final String BODY = "body";

    // postId of the board category ?
    public final String BOARD_ID = "boardId";
    public final String THREAD_ID = "threadId";

    public final String AUTHOR_LOGIN = "authorLogin";
    public final String AUTHOR_ID = "authorId";

    public final String LAST_AUTHOR_LOGIN = "lastAuthorLogin";
    public final String LAST_AUTHOR_ID = "lastAuthorId";

    //number of kudos (<=> Like ~empathy)
    public final String KUDOS = "kudos";
    public final String POST_TIME = "postTime";
    public final String LAST_EDIT_TIME = "lastEditTime";
    public final String VIEWS = "views";

    //to be confirmed which tag is used for class
    public final String POST_CLASS = "postClass";

    public final String WEIGHT = "weight";

    public final String NB_GREEN_CLASS = "nbGreenClass";
    public final String NB_AMBER_CLASS = "nbAmberClass";
    public final String NB_RED_CLASS = "nbRedClass";
    public final String NB_CRISIS_CLASS = "nbCrisisClass";

    public Post(){
        fields.put(POST_ID, "");
        fields.put(SUBJECT, "");
        fields.put(BODY, "");
        fields.put(BOARD_ID, "");
        fields.put(THREAD_ID,"");
        fields.put(AUTHOR_LOGIN, "");
        fields.put(AUTHOR_ID, "");
        fields.put(LAST_AUTHOR_LOGIN, "");
        fields.put(LAST_AUTHOR_ID, "");
        fields.put(KUDOS, "");
        fields.put(POST_TIME, "");
        fields.put(LAST_EDIT_TIME, "");
        fields.put(VIEWS, "");
        fields.put(POST_CLASS, "");
        fields.put(WEIGHT, "1");
        if(Boolean.parseBoolean(Configs.getInstance().getProps().getProperty("USE_CLASS_COUNT"))) {
            fields.put(NB_GREEN_CLASS,"");
            fields.put(NB_AMBER_CLASS,"");
            fields.put(NB_RED_CLASS,"");
            fields.put(NB_CRISIS_CLASS,"");
        }
    }

    public void setValue(String id, String value){
        if(fields.containsKey(id))
            fields.put(id, value);
        else
            System.out.println("Error setting value for " + id +" field.");
    }

    public String getValue(String id){
            return fields.get(id);
    }

    @Override
    public String toString() {
        return "\nPost postId=" + getValue(POST_ID) + "\n" +
                "subject=" + getValue(SUBJECT) + "\n" +
                "body=" + getValue(BODY) + "\n" +
                "boardId=" + getValue(BOARD_ID) + "\n" +
                "lastAuthorLogin=" + getValue(LAST_AUTHOR_LOGIN) + "\n" +
                "lastAuthorId=" + getValue(LAST_AUTHOR_ID) + "\n" +
                "authorLogin=" + getValue(AUTHOR_LOGIN) + "\n" +
                "authorId=" + getValue(AUTHOR_ID) + "\n" +
                "kudos=" + getValue(KUDOS) + "\n" +
                "postTime=" + getValue(POST_TIME) + "\n" +
                "lastEditTime=" + getValue(LAST_EDIT_TIME) + "\n" +
                "views=" + getValue(VIEWS) + "\n" +
                "postClass=" + getValue(POST_CLASS);
    }

    @Override
    public int compareTo(Object o) {
        Post otherPost = (Post) o;
        try {
            return formatDate(this.getValue(POST_TIME)).compareTo(formatDate(otherPost.getValue(POST_TIME)));
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException("Problem when parsing post dates. "+e.getMessage());
        }

    }

    private Date formatDate(String dateTime) throws ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        return df.parse(dateTime);
    }

}
