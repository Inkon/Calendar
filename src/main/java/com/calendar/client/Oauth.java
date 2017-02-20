package com.calendar.client;

import com.google.api.gwt.oauth2.client.Auth;
import com.google.api.gwt.oauth2.client.AuthRequest;
import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;

import java.util.Date;

public class Oauth {

    private static final Auth AUTH = Auth.get();
    private static final String
            GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/auth",
            GOOGLE_CLIENT_ID = "790928429290-fvcbfbfb94tc1gn7ivv35n5sigo5c76k.apps.googleusercontent.com",
            PLUS_ME_SCOPE = "https://www.googleapis.com/auth/plus.login",
            EMAIL_SCOPE ="https://www.googleapis.com/auth/plus.profile.emails.read",

            VK_AUTH_URL = "https://oauth.vk.com/authorize",
            VK_CLIENT_ID = "5776261",
            VK_EMAIL_SCOPE = "email",

            OAUTH_COOKIE = "oauthToken",
            SERVICE_COOKIE = "service";
    public static final String
            GOOGLE = "google",
            VK = "vk";

    public static void googleAuth(OauthCallback callback) {
        if (Cookies.getCookie(OAUTH_COOKIE) == null) {
            final AuthRequest req = new AuthRequest(GOOGLE_AUTH_URL, GOOGLE_CLIENT_ID)
                    .withScopes(PLUS_ME_SCOPE, EMAIL_SCOPE);

            AUTH.login(req, new Callback<String, Throwable>() {
                @Override
                public void onSuccess(String token) {
                    Date time = new Date();
                    long expires = time.getTime();
                    expires = expires + (long) AUTH.expiresIn(req);
                    time.setTime(expires);
                    Cookies.setCookie(OAUTH_COOKIE, token, time);
                    Cookies.setCookie(SERVICE_COOKIE, GOOGLE, time);
                    callback.success();
                }

                @Override
                public void onFailure(Throwable caught) {
                    callback.failure(caught);
                }
            });
        }
    }

    public static void vkAuth(OauthCallback callback){
        if (Cookies.getCookie(OAUTH_COOKIE) == null) {
            final AuthRequest req = new AuthRequest(VK_AUTH_URL, VK_CLIENT_ID)
                    .withScopes(VK_EMAIL_SCOPE);

            AUTH.login(req, new Callback<String, Throwable>() {
                @Override
                public void onSuccess(String token) {
                    Date time = new Date();
                    long expires = time.getTime();
                    expires = expires + (long) AUTH.expiresIn(req);
                    time.setTime(expires);
                    Cookies.setCookie(OAUTH_COOKIE, token, time);
                    Cookies.setCookie(SERVICE_COOKIE, VK, time);
                    callback.success();
                }

                @Override
                public void onFailure(Throwable caught) {
                    callback.failure(caught);
                }
            });
        }
    }

    public static String getToken() {
        return Cookies.getCookie(OAUTH_COOKIE);
    }

    public static String getService(){
        return Cookies.getCookie(SERVICE_COOKIE);
    }

    public static boolean authenticated() {
        return getToken() != null;
    }

    public static void logOut() {
        Cookies.removeCookie(OAUTH_COOKIE);
        AUTH.clearAllTokens();
    }
}
