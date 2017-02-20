package com.calendar.client.ui;

import com.calendar.client.*;
import com.calendar.client.event.CalendarAddEvent;
import com.calendar.client.event.ShowImportCalendarForm;
import com.calendar.client.event.ToggleEditFiltersModeEvent;
import com.calendar.shared.dto.InviteDTO;
import com.calendar.shared.dto.UserDTO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorButton;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;

import static com.calendar.client.Main.LOGIN_TOKEN;

public class MainPage extends Composite {
    private static MainPageUiBinder ourUiBinder = GWT.create(MainPageUiBinder.class);
    private LoginServiceAsync loginService = GWT.create(LoginService.class);
    private InviteServiceAsync inviteService = GWT.create(InviteService.class);

    @UiTemplate("MainPage.ui.xml")
    interface MainPageUiBinder extends UiBinder<Widget, MainPage> {
    }

    @UiField
    Anchor logout;

    @UiField
    HTMLPanel calendarPanel, filterListPanel, addFilterModalPanel, addEventModalPanel, editEventModalPanel,
            viewEventModalPanel, editFilterModalPanel, dateFindModalPanel, calendarImportModalPanel;

    @UiField
    Strong name, email;

    @UiField
    AnchorButton buttonName;

    @UiField
    Button toggleEditFiltersMode;

    @UiField
    Div overlay;

    @UiField
    AnchorListItem calendarImportAnchor;

    @UiHandler("logout")
    public void onButtonClick(final ClickEvent event) {
        loginService.clear(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
            }

            @Override
            public void onSuccess(Void aVoid) {
            }
        });
        logout();
    }

    @UiHandler("calendarImportAnchor")
    public void onCalendarImportAnchorClick(final ClickEvent event) {
        EventBus.getInstance().fireEvent(new ShowImportCalendarForm());
    }

    public MainPage() {
        initWidget(ourUiBinder.createAndBindUi(this));
        loginService.getUser(Oauth.getToken(), Oauth.getService(), new AsyncCallback<UserDTO>() {
            @Override
            public void onFailure(Throwable throwable) {
                failAuth();
            }

            @Override
            public void onSuccess(UserDTO user) {
                init(user);

                filterListPanel.add(new FilterList());
                addFilterModalPanel.add(new AddFilterModal());
                editFilterModalPanel.add(new EditFilterModal());
                viewEventModalPanel.add(new ViewEventModal());
                addEventModalPanel.add(new AddEventModal());
                calendarPanel.add(new Calendar());
                calendarImportModalPanel.add(new ImportCalendarModal());
                dateFindModalPanel.add(new DateFindModal());

                toggleEditFiltersMode.addClickHandler(clickEvent -> {
                    EventBus.getInstance().fireEvent(new ToggleEditFiltersModeEvent(!toggleEditFiltersMode.isActive()));
                });

                // Check cookies for invite_token
                String token = Cookies.getCookie("invite_token");
                if (token != null) {
                    // Add attached event if token is correct
                    inviteService.activateInvite(token, new AsyncCallback<InviteDTO>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                            // TODO: what?
                        }

                        @Override
                        public void onSuccess(InviteDTO inviteDTO) {
                            if (inviteDTO != null) {
                                EventBus.getInstance().fireEvent(new CalendarAddEvent(inviteDTO));
                                UIUtils.pushNotify("Вы добавлены в приглашенное событие.", NotifyType.SUCCESS);
                            } else {
                                UIUtils.pushNotify("Ошибка при добавлении в приглашенное событие.", NotifyType.DANGER);
                            }
                        }
                    });
                    Cookies.removeCookie("invite_token");
                }
            }
        });
    }

    private void init(UserDTO user) {
        if (user != null) {
            String fullName = user.getFirstName() + " " + user.getLastName();
            name.setText(fullName);
            buttonName.setText(fullName);
            email.setText(user.getEmail());

            overlay.setVisible(false);
        } else {
            Window.alert("shiet");
            failAuth();
        }
    }

    private static void logout(){
        Oauth.logOut();
        History.newItem(LOGIN_TOKEN);
        EventBus.clear();
    }

    static void failAuth() {
        logout();
    }

}