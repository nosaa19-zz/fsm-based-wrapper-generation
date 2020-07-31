package Domain;

import java.util.ArrayList;

public class SummaryLogFSM {
    private String status;
    private ArrayList<LogFSM> details;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<LogFSM> getDetails() {
        return details;
    }

    public void setDetails(ArrayList<LogFSM> details) {
        this.details = details;
    }
}
