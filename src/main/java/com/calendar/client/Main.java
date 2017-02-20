package com.calendar.client;

import com.calendar.client.ui.LoginWidget;
import com.calendar.client.ui.MainPage;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

public class Main implements EntryPoint, ValueChangeHandler<String> {
    public static final String LOGIN_TOKEN = "login";
    public static final String MAIN_PAGE_TOKEN = "calendar";

    public void onModuleLoad() {
        setupHistory();
    }

    private void setupHistory() {
        History.addValueChangeHandler(this);
        if (Window.Location.getParameter("invite") != null) {
            processInvite(Window.Location.getParameter("invite"));
        } else {
            if (Oauth.authenticated()) {
                History.newItem(MAIN_PAGE_TOKEN, true);
            } else {
                History.newItem(LOGIN_TOKEN, true);
            }
        }
    }

    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        String historyToken = event.getValue();
        if (historyToken.equals(LOGIN_TOKEN)) {
            loadLoginView();
        } else if (historyToken.equals(MAIN_PAGE_TOKEN)) {
            loadMainPage();
        }
    }

    private void processInvite(String token) {
        Cookies.setCookie("invite_token", token);
        Window.Location.replace("/calendar");
    }

    private void loadLoginView(){
        RootPanel.get().clear();
        RootPanel.get().add(new LoginWidget());
    }

    private void loadMainPage(){
        RootPanel.get().clear();
        RootPanel.get().add(new MainPage());
    }
}