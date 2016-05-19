package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by halmeida on 2/2/16.
 */
public class Configs {
    Properties props;
    private final String configFileParam = "config.file";

    /** Pre-initialized unique instance */
    private static Configs INSTANCE = new Configs();

    public static Configs getInstance() {
        return INSTANCE;
    }

    private Configs(){
        this.props = new Properties();
        if (System.getProperty(configFileParam) == null) {
            System.err.println("You must set the variable 'config.file', java -Dconfig.file=... -jar ...");
            System.exit(-1);
        }
        String configFile = System.getProperty(configFileParam);
        try {
            this.props.load(new FileInputStream(new File(configFile)));
        } catch (IOException ex) {
            System.err.print("Could not load resources " + configFile +  ex);
        }
    }

    public Properties getProps() {
        return props;
    }

}
