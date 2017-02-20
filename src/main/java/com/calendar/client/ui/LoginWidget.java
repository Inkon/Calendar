package com.calendar.client.ui;

import com.calendar.client.Oauth;
import com.calendar.client.OauthCallback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import static com.calendar.client.Main.MAIN_PAGE_TOKEN;

public class LoginWidget extends Composite {

    @UiTemplate("Login.ui.xml")
    interface MyUiBinder extends UiBinder<Widget, LoginWidget> {
    }

    @UiHandler("loginWithGoogle")
    public void onButtonClickGoogle(final ClickEvent event) {
        Oauth.googleAuth(new OauthCallback() {
            @Override
            public void success() {
                History.newItem(MAIN_PAGE_TOKEN);
            }

            @Override
            public void failure(Throwable caught) {
                Window.alert("Произошла ошибка при авторизации через google+, попробуйте еще раз.");
                Oauth.logOut();
            }
        });
    }

    @UiHandler("loginWithVkontakte")
    public void onButtonClickVkontakte(final ClickEvent event) {
        Oauth.vkAuth(new OauthCallback() {
            @Override
            public void success() {
                History.newItem(MAIN_PAGE_TOKEN);
            }

            @Override
            public void failure(Throwable caught) {
                Window.alert("Произошла ошибка при авторизации через вконтакте, попробуйте еще раз.");
                Oauth.logOut();
            }
        });
    }

    private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    public LoginWidget() {
        initWidget(uiBinder.createAndBindUi(this));
    }

}
