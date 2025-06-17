package com.collabrix.collabrix.request;

public class ForgotPasswordTokenRequest {
    public String getSendTo() {
        return sendTo;
    }

    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
    }

    private String sendTo;
}
