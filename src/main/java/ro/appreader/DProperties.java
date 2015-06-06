package ro.appreader;

/**
 * Created by diana on 13.04.2015.
 */
public class DProperties {
    private String root;
    private String logsRoot;
    private static DProperties instance;

    private DProperties(){
    };

    public static DProperties getInstance(){
        if (instance == null){
            instance = new DProperties();
        }
        return instance;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getLogsRoot() {
        return logsRoot;
    }

    public void setLogsRoot(String logsRoot) {
        this.logsRoot = logsRoot;
    }
}
