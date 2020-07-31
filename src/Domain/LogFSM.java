package Domain;

public class LogFSM {

    private String filename;
    private String message;

    public LogFSM(String name, String result) {
        this.filename = name;
        this.message = result;
    }


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
