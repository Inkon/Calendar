package com.calendar.client;

public interface OauthCallback {
    public void success();
    public void failure(Throwable caught);
}
