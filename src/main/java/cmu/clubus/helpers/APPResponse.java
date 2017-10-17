package cmu.clubus.helpers;

public class APPResponse {
    public boolean success;
    public Object content;
    public APPResponse(Object content) {
        this.success = true;
        this.content = content;
    }
    public APPResponse(Object content, boolean ifsuccess) {
        this.success = ifsuccess;
        this.content = content;
    }

    public APPResponse() {
        this.success = true;
    }
}