package normalizer;

import common.Configs;
import common.IOUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created by halmeida on 2/4/16.
 *
 * Normalizes a forum post replacing:
 * - html chars
 * - special and math symbols
 * - smileys / emojis
 *
 */
public class SpecialCharNormalizer {


    IOUtil util;
    String location;
    String smiley_dic_file;
    HashMap<String, String> smileyDic = new HashMap<>();
    Properties props;

    public SpecialCharNormalizer(){
        props = Configs.getInstance().getProps();
        util = new IOUtil();
        location = props.getProperty("WORK_DIRECTORY") +
                props.getProperty("RESOURCES_DIR") +
                props.getProperty("SMILEY_DIC_DIR") + "/";

        smiley_dic_file = props.getProperty("SMILEYS_DIC");

        smileyDic = util.loadDictionary(location, smiley_dic_file);
    }




    /**
     * Replace or handle punctuation
     * in a given text
     * HA
     * @param text
     * @return
     */
    public String handlePunctuation(String text) {

        /*HA : if no apostrophe+S is found
        check if the apostrophe is different, then change to regular version
        if apostrophe+S is not found at all, then get rid of any apostrophe */
        if (text.contains("’s")) text = text.replace("’s", "'s");
        if (text.contains("'s")) text = text.replace("'s", "");
        if (text.contains("\'")) text = text.replace("\'", " ");

        text = text.replace(",", " ");

        // HA: only replace actual periods. avoid replacing periods in URLs
        text = text.replace("..", " ");
        text = text.replace(". ", " ");

        text = text.replace("@", " ");
        //HA: replace brackets leaving content between
        text = text.replaceAll("\\{(.*?)\\}", "$1");
        text = text.replaceAll("\\[(.*?)\\]", "$1");
        text = text.replace("”", "");
        text = text.replace("“","");
        text = text.replace("\"", "");
        text = text.replace("<", " ");
        text = text.replace(">", " ");
        // HA: if replaced, we will lose URL addresses
        // text = text.replace("/", " ");
        text = text.replace("\\", " ");
        text = text.replace("~", "");
        text = text.replace("_", "");
        text = text.replace("#", ""); //will lose html tags
        text = text.replace("*", "");
        text = text.replace("%", ""); //will lose html tags
        text = text.replace("&", ""); //will lose html tags
        text = text.replace("=", "");
        text = text.replace("?", "");
        text = text.replace("!", "");
        text = text.replace(";", "");
        text = text.replace(":", "");
        text = text.replace(")", "");
        text = text.replace("(", "");
        text = text.replace("\t\t", "\t");
        text = text.replace("+", "");
        // HA: keeping URL that have hifens
        if (!text.contains("/")) text = text.replace("-", " ");
        text = text.replace("\\s+", " ");

        return text;
    }

    public String handleWordPunctuation(String word){
        if(!word.contains("url")){
            word = word.replace("-", " ");
            word = word.replace("/"," ");
            word = word.replace("."," ");
        }

        //HA: handle apostrophes in single words
        // regex was not working
        if(word.contains("'")){
            if((word.indexOf("'") == 0) || (word.indexOf("'") == word.length()-1) ){
                word = word.replaceAll("\\'", "");
            }
        }

        word = handleContractions(word);
        word = word.trim();
        word = word.replaceAll("\\s+", " ");

        return word;
    }

    public String handleContractions(String word){
        word = word.replaceAll("'m|’m"," am");
        word = word.replaceAll("'re|’re"," are");
        word = word.replaceAll("'ll|’ll"," will");
        word = word.replaceAll("'ve|’ve"," have");
        if(!word.contains("wo")) word = word.replaceAll("n't|n’t", " not");

        return word;
    }

     /**
     * Handle html links
     * HA
     * @param text text to be cleaned
     * @return
     */
    public String handleLinks(String text) {

        text = text.replace("http://", "URL//");
        text = text.replace("https://", "URL//");
        text = text.replace("<a href=\"", " link ");
        text = text.replace("\\s+", " ");

        return text;

    }
    /**
     * Handle html tags
     * HA
     * @param text text to be cleaned
     * @return
     */
    public String handleXMLTags(String text) {

        text = text.replace("<img src=\"[^>]+\">", "");
        //replaces all <xxx> tags
        text = text.replaceAll("\\<(.*?)\\>"," ");
        text = text.replace("\\s+", " ");

        return text;
    }

    /**
     * Replaces special HTML characters to:
     * Punctuations, Letters, Symbols
     *
     * @param text text document
     * @return text with replaced codes
     *
     * http://www.w3schools.com/charsets/ref_utf_basic_latin.asp
     * https://www.utexas.edu/learn/html/spchar.html
     *
     */
    public String replaceSpecialHTML(String text) {

        text = text.replaceAll("&#32;", " ");
        text = text.replaceAll("&#33;", "!");
        text = text.replaceAll("(?i)(&#34;|&quot;)", "\"");
        text = text.replaceAll("&#35;", "#");
        text = text.replaceAll("&#36;", "$");
        text = text.replaceAll("&#37;", "%");
        text = text.replaceAll("(?i)(&#38;|&amp;)", " and ");
        text = text.replaceAll("(?i)(&#39;|&apos;)", "'");
        text = text.replaceAll("&#40;", "(");
        text = text.replaceAll("&#41;", ")");
        text = text.replaceAll("&#42;", "*");
        text = text.replaceAll("&#43;", "+");
        text = text.replaceAll("&#44;", ",");
        text = text.replaceAll("&#45;", "-");
        text = text.replaceAll("&#46;", ".");
        text = text.replaceAll("(?i)(&#47;|&frasl;)", "/");
        text = text.replaceAll("&#48;", "0");
        text = text.replaceAll("&#49;", "1");
        text = text.replaceAll("&#50;", "2");
        text = text.replaceAll("&#51;", "3");
        text = text.replaceAll("&#52;", "4");
        text = text.replaceAll("&#53;", "5");
        text = text.replaceAll("&#54;", "6");
        text = text.replaceAll("&#55;", "7");
        text = text.replaceAll("&#56;", "8");
        text = text.replaceAll("&#57;", "9");
        text = text.replaceAll("&#58;", ":");
        text = text.replaceAll("&#59;", ";");
        text = text.replaceAll("(?i)(&#60;|&lt;)", " less than "); // <
        text = text.replaceAll("&#61;", "=");
        text = text.replaceAll("(?i)(&#62;|&gt;)", " greater than "); // >
        text = text.replaceAll("&#63;", "?");
        text = text.replaceAll("&#64;", "@");
        text = text.replaceAll("&#65;", "A");
        text = text.replaceAll("&#66;", "B");
        text = text.replaceAll("&#67;", "C");
        text = text.replaceAll("&#68;", "D");
        text = text.replaceAll("&#69;", "E");
        text = text.replaceAll("&#70;", "F");
        text = text.replaceAll("&#71;", "G");
        text = text.replaceAll("&#72;", "H");
        text = text.replaceAll("&#73;", "I");
        text = text.replaceAll("&#74;", "J");
        text = text.replaceAll("&#75;", "K");
        text = text.replaceAll("&#76;", "L");
        text = text.replaceAll("&#77;", "M");
        text = text.replaceAll("&#78;", "N");
        text = text.replaceAll("&#79;", "O");
        text = text.replaceAll("&#80;", "P");
        text = text.replaceAll("&#81;", "Q");
        text = text.replaceAll("&#82;", "R");
        text = text.replaceAll("&#83;", "S");
        text = text.replaceAll("&#84;", "T");
        text = text.replaceAll("&#85;", "U");
        text = text.replaceAll("&#86;", "V");
        text = text.replaceAll("&#87;", "W");
        text = text.replaceAll("&#88;", "X");
        text = text.replaceAll("&#89;", "Y");
        text = text.replaceAll("&#90;", "Z");
        text = text.replaceAll("&#91;", "[");
        text = text.replaceAll("&#92;", "\\");
        text = text.replaceAll("&#93;", "]");
        text = text.replaceAll("&#94;", "^");
        text = text.replaceAll("&#95;", "_");
        text = text.replaceAll("&#96;", "`");
        text = text.replaceAll("&#97;", "a");
        text = text.replaceAll("&#98;", "b");
        text = text.replaceAll("&#99;", "c");
        text = text.replaceAll("&#100;", "d");
        text = text.replaceAll("&#101;", "e");
        text = text.replaceAll("&#102;", "f");
        text = text.replaceAll("&#103;", "g");
        text = text.replaceAll("&#104;", "h");
        text = text.replaceAll("&#105;", "i");
        text = text.replaceAll("&#106;", "j");
        text = text.replaceAll("&#107;", "k");
        text = text.replaceAll("&#108;", "l");
        text = text.replaceAll("&#109;", "m");
        text = text.replaceAll("&#110;", "n");
        text = text.replaceAll("&#111;", "o");
        text = text.replaceAll("&#112;", "p");
        text = text.replaceAll("&#113;", "q");
        text = text.replaceAll("&#114;", "r");
        text = text.replaceAll("&#115;", "s");
        text = text.replaceAll("&#116;", "t");
        text = text.replaceAll("&#117;", "u");
        text = text.replaceAll("&#118;", "v");
        text = text.replaceAll("&#119;", "w");
        text = text.replaceAll("&#120;", "x");
        text = text.replaceAll("&#121;", "y");
        text = text.replaceAll("&#122;", "z");
        text = text.replaceAll("&#123;", "{");
        text = text.replaceAll("&#124;", "|");
        text = text.replaceAll("&#125;", "}");
        text = text.replaceAll("&#126;", "~");
        text = text.replaceAll("(?i)(&#150;|&ndash;)", "");
        text = text.replaceAll("(?i)(&#151;|&mdash;)", "");//if apostrophe+S is not found at all, then get rid of any apostrophe
        text = text.replaceAll("(?i)(&#160;|&nbsp;)", "");
        text = text.replaceAll("(?i)(&#161;|&iexcl;)", "¡");
        text = text.replaceAll("(?i)(&#162;|&cent;)", "¢");
        text = text.replaceAll("(?i)(&#163;|&pound;)", "£");
        text = text.replaceAll("(?i)(&#164;|&curren;)", "¤");
        text = text.replaceAll("(?i)(&#165;|&yen;)", "¥");
        text = text.replaceAll("(?i)(&#166;|&brvbar;|&brkbar;)", "¦");
        text = text.replaceAll("(?i)(&#167;|&sect;)", "§");
        text = text.replaceAll("(?i)(&#168;|&uml;|&die;)", "¨");
        text = text.replaceAll("(?i)(&#169;|&copy;)", "©");
        text = text.replaceAll("(?i)(&#170;|&ordf;)", "ª");
        text = text.replaceAll("(?i)(&#171;|&laquo;)", "«");
        text = text.replaceAll("(?i)(&#172;|&not;)", "¬");
        text = text.replaceAll("(?i)(&#173;|&shy;)", "");
        text = text.replaceAll("(?i)(&#174;|&reg;)", "®");
        text = text.replaceAll("(?i)(&#175;|&macr;|&hibar;)", "¯");

        text = text.replaceAll("\\s+", " ");

        return text;
    }


    /**
     *
     * Remove | replace ISO 8859-1 Symbols
     * From NormalizeWebDoc class
     *
     * @param text
     * @return
     */
    public String replaceSymbols(String text){

        if (text.length() == 0) { return text; }

        text = text.replaceAll("&nbsp;", " "); // non-breaking space
        text = text.replaceAll("&iexcl;", " "); // inverted exclamation mark
        text = text.replaceAll("&cent;", " cent "); // cent -> ! FULLNAME
        text = text.replaceAll("&pound;", " pound "); // pound -> ! FULLNAME
        text = text.replaceAll("&curren;", " currency "); // currency -> ! FULLNAME
        text = text.replaceAll("&yen;", " yen "); // yen -> ! FULLNAME
        text = text.replaceAll("&brvbar;", " "); // broken vertical bar
        text = text.replaceAll("&sect;", " "); // section
        text = text.replaceAll("&uml;", " - "); // spacing diaeresis
        text = text.replaceAll("&copy;", " copyright "); // copyright -> ! FULLNAME
        text = text.replaceAll("&ordf;", " "); // feminine ordinal indicator
        text = text.replaceAll("&laquo;", "\""); // angle quotation mark (left)
        text = text.replaceAll("&not;", " not "); // negation  -> ! FULLNAME
        text = text.replaceAll("&shy;", " "); // soft hyphen
        text = text.replaceAll("&reg;", " registered trademark "); // registered trademark  -> ! FULLNAME
        text = text.replaceAll("&macr;", " - "); // spacing macron
        text = text.replaceAll("&deg;", " degree "); // degree  -> ! FULLNAME
        text = text.replaceAll("&plusmn;", " "); // plus-or-minus
        text = text.replaceAll("&sup2;", " "); // superscript 2
        text = text.replaceAll("&sup3;", " "); // superscript 3
        text = text.replaceAll("&acute;", " "); // spacing acute
        text = text.replaceAll("&micro;", " micro "); // micro  -> ! FULLNAME
        text = text.replaceAll("&para;", " "); // paragraph
        text = text.replaceAll("&middot;", " "); // middle dot
        text = text.replaceAll("&cedil;", " "); // spacing cedilla
        text = text.replaceAll("&sup1;", " "); // superscript 1
        text = text.replaceAll("&ordm;", " "); // masculine ordinal indicator
        text = text.replaceAll("&raquo;", "\""); // angle quotation mark (right)
        text = text.replaceAll("&frac14;", " "); // fraction 1/4
        text = text.replaceAll("&frac12;", " "); // fraction 1/2
        text = text.replaceAll("&frac34;", " "); // fraction 3/4
        text = text.replaceAll("&iquest;", " "); // inverted question mark
        text = text.replaceAll("&times;", " "); // multiplication
        text = text.replaceAll("&divide;", " "); // division

        return(text);
    }


    /**
     * Replaces smileys/emoticons by words
     * (to be completed)
     * http://www.unicode.org/emoji/index.html
     */
    public String replaceSmileyChars(String text){

        Iterator iterator = smileyDic.entrySet().iterator();

        //handling image smiley
        if(text.contains("<img class=\"emoticon emoticon-smiley")){
            String smiley_word = text.substring(text.indexOf("<img class=\"emoticon emoticon-smiley"));
            smiley_word = smiley_word.replace("<img class=\"emoticon emoticon-smiley","");
            smiley_word = smiley_word.substring(0, smiley_word.indexOf("\""));
            text = text.replaceAll("<img class=\"emoticon[^>]+\" \\/>", smiley_word);
            text = text.replaceAll("<img class=\"emoticon[^>]+\"\\/>", smiley_word);
        }

        while(iterator.hasNext()){
            Map.Entry<String,String> smiley = (Map.Entry<String,String>) iterator.next();
            //handling smileys that contain letters
            //so they do not match middle of words
            //example: "xp" matching "experience"
            if(smiley.getKey().matches("^.*(?=.*[a-zA-Z]).*$")){ //if smiley contains letter
                if(text.contains(" " + smiley.getKey() + " ")){ //replace only if found space
                    text = text.replace(smiley.getKey(), " " + smiley.getValue() + " ");
                }
            }
            else if (text.contains(" " + smiley.getKey()) || text.contains(smiley.getKey() + " ")){
                text = text.replace(smiley.getKey(), " " + smiley.getValue() + " ");
            }
        }
        return text;
    }

    /**
     * Replace smileys
     * From NormalizeWebDoc class
     * @param text
     * @return
     */
    public String replaceHtmlSmiley(String text){

            if (text.length() == 0) { return text; }

            text = text.replaceAll("\\{\\}:   \\)", " happy "); // evilly happy
            text = text.replaceAll("\\{\\}:   \\(", " angry "); // evilly angry
            text = text.replaceAll("\\{\\}\' \\.    \\)", ""); //
            text = text.replaceAll("\\{\\} \\.  - \\)", ""); //
            text = text.replaceAll("&#9786;", " happy "); // happy white
            text = text.replaceAll("&#9787;", " happy "); // happy black
            text = text.replaceAll("&#1578;", " happy "); // happy
            text = text.replaceAll("&#12485;", " happy "); // happy
            text = text.replaceAll("&#12484;", " happy "); // happy
            text = text.replaceAll("&#12483;", " happy "); // happy
            text = text.replaceAll("&#12471;", " happy "); // happy
            text = text.replaceAll("&#220;", " happy "); // happy
            text = text.replaceAll("&#993;", " happy "); // happy
            text = text.replaceAll("&#64354;", " happy "); // happy

        return(text);
    }

    /**
     *
     * Replace math symbols
     * From NormalizeWebDoc class
     *
     * @param text
     * @return
     */
    public static String ReplaceHtmlMathSymbol(String text){

        if (text.length() == 0) { return text; }

        // http://www.w3schools.com/tags/ref_symbols.asp
        text = text.replaceAll("&forall;", " for all "); //
        text = text.replaceAll("&part;", " part "); //
        text = text.replaceAll("&exist;", " exists "); //
        text = text.replaceAll("&empty;", " empty "); //
        text = text.replaceAll("&nabla;", " nabla "); //
        text = text.replaceAll("&isin;", " is in"); //
        text = text.replaceAll("&notin;", " not in "); //
        text = text.replaceAll("&ni;", " ni "); //
        text = text.replaceAll("&prod;", " prod "); //
        text = text.replaceAll("&sum;", " sum "); //
        text = text.replaceAll("&minus;", " minus "); //
        text = text.replaceAll("&lowast;", " lowast "); //
        text = text.replaceAll("&radic;", " square root "); //
        text = text.replaceAll("&prop;", " proportional to "); //
        text = text.replaceAll("&infin;", " infinity "); //
        text = text.replaceAll("&ang;", " angle "); //
        text = text.replaceAll("&and;", " and "); //
        text = text.replaceAll("&or;", " or "); //
        text = text.replaceAll("&cap;", " cap "); //
        text = text.replaceAll("&cup;", " cup "); //
        text = text.replaceAll("&int;", " integral "); //
        text = text.replaceAll("&there4;", " therefore "); //
        text = text.replaceAll("&sim;", " similar to "); //
        text = text.replaceAll("&cong;", " congruent to "); //
        text = text.replaceAll("&asymp;", " almost equal "); //
        text = text.replaceAll("&ne;", " not equal "); //
        text = text.replaceAll("&equiv;", " equivalent "); //
        text = text.replaceAll("&le;", " less or equal "); //
        text = text.replaceAll("&ge;", " greater or equal "); //
        text = text.replaceAll("&sub;", " subset of "); //
        text = text.replaceAll("&sup;", " superset of "); //
        text = text.replaceAll("&nsub;", " not subset of "); //
        text = text.replaceAll("&sube;", " subset or equal "); //
        text = text.replaceAll("&supe;", " superset or equal "); //
        text = text.replaceAll("&oplus;", " circled plus "); //
        text = text.replaceAll("&otimes;", " circled times "); //
        text = text.replaceAll("&perp;", " perpendicular "); //
        text = text.replaceAll("&sdot;", " dot operator "); //

        return(text);
    }



}
