package ru.ifsoft.network.model;

public class SuccessDialog {
    private String message;
    private boolean isShow;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }
}
