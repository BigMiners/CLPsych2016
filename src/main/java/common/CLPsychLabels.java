package common;

/**
 * Created by mqueudot on 03/03/16.
 */
public enum CLPsychLabels {
    Green(0,"nbGreenClass"),
    Amber(1,"nbAmberClass"),
    Red(2,"nbRedClass"),
    Crisis(3,"nbCrisisClass");

    private int id;
    private String postFieldName;

    CLPsychLabels(int id, String postFieldName) {
        this.id=id;
        this.postFieldName=postFieldName;
    }

    public static CLPsychLabels getLabelFromId(int id) {
        for(CLPsychLabels label : values()) {
            if(label.ordinal()==id) {
                return label;
            }
        }
        return null;
    }

    public String getPostFieldName() {
        return this.postFieldName;
    }
}

