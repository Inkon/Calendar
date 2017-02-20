package com.calendar.client.ui;

import com.calendar.client.EventBus;
import com.calendar.client.InviteService;
import com.calendar.client.InviteServiceAsync;
import com.calendar.client.event.*;
import com.calendar.shared.dto.EventDTO;
import com.calendar.shared.dto.FilterDTO;
import com.calendar.shared.dto.InviteDTO;
import com.calendar.shared.entity.Invite;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;

import java.util.*;

import static com.calendar.client.ui.Calendar.eventsService;

public class ViewEventModal extends AbstractEventModal {
    private EventDTO calendarEvent;
    private InviteDTO calendarInvite;
    private Invite.InviteStatus status;

    private enum Mode {EVENT, INVITE}

    private Mode mode;

    private boolean edited;

    private final InviteServiceAsync inviteService = GWT.create(InviteService.class);

    private List<Button> invitesActionButtons = new ArrayList<>();

    @UiTemplate("ViewEventModal.ui.xml")
    interface ViewEventModalUiBinder extends UiBinder<HTMLPanel, ViewEventModal> {
    }

    @UiField
    Modal viewEventModal;

    @UiField
    TextBox eventDescription, inviteEmail;

    @UiField
    Button sendInvite;

    @UiField
    Button inviteStatus, inviteStatusReadonly, deleteButton, okButton, discardButton, editButton, timeChange;

    @UiField
    FormGroup eventStatusGroup;

    @UiField
    FieldSet invitesList;

    @UiField
    FormGroup invitedPeople, addPeople;

    @UiHandler("editButton")
    public void switchModeButtonClick(final ClickEvent event) {
        switchEditable();
    }

    @UiHandler("okButton")
    public void okButtonClick(final ClickEvent event) {
        if (edited) {
            if (validate() || periodicValidate()) {
                okButton.state().loading();
                block();
                HashSet<FilterDTO> filters = new HashSet<>();
                for (FilterDTO filterDTO : calendarEvent.getAllFilters()) {
                    if (!userFilters.containsKey(filterDTO.getId())) {
                        filters.add(filterDTO);
                    }
                }
                for (int id : eventFilters) {
                    filters.add(userFilters.get(id));
                }
                calendarEvent.setAllFilters(filters);
                if (mode == Mode.EVENT) {
                    calendarEvent.setBeginDate(UIUtils.toDataBaseTimeZone(eventStart.getValue()));
                    calendarEvent.setEndDate(UIUtils.toDataBaseTimeZone(eventEnd.getValue()));
                    calendarEvent.setDescription(eventDescription.getText());
                    calendarEvent.setName(eventName.getText());
                    if (isPeriodic()){
                        calendarEvent.setFrequency(getEventFrequency());
                        calendarEvent.setLastDate(eventPeriodicFinish.getValue());
                        calendarEvent.setPeriod(Integer.parseInt(periodText.getValue()));
                    }
                    eventsService.updateEvent(calendarEvent, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                            UIUtils.pushNotify("Ошибка при обновлении события.", NotifyType.DANGER);
                            okButton.state().reset();
                            unblock();
                        }

                        @Override
                        public void onSuccess(Void aVoid) {
                            EventBus.getInstance().fireEvent(new CalendarUpdateEvent(calendarEvent));
                            UIUtils.pushNotify("Событие успешно обновлено.", NotifyType.SUCCESS);
                            okButton.state().reset();
                            unblock();
                        }
                    });
                } else {
                    calendarInvite.setEvent(calendarEvent);
                    calendarInvite.setStatus(status);
                    inviteService.updateInvite(calendarInvite, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                            UIUtils.pushNotify("Ошибка при обновлении приглашения.", NotifyType.DANGER);
                            okButton.state().reset();
                            unblock();
                        }

                        @Override
                        public void onSuccess(Void aVoid) {
                            EventBus.getInstance().fireEvent(new CalendarUpdateInvite(calendarInvite));
                            UIUtils.pushNotify("Приглашение успешно обновлено.", NotifyType.SUCCESS);
                            okButton.state().reset();
                            unblock();
                        }
                    });
                }
            }
        } else {
            reset();
        }
    }

    @UiHandler("discardButton")
    public void discardButtonClick(final ClickEvent event) {
        if (!edited || Window.confirm("Все изменения будут потеряны. Продолжить?")) {
            reset();
        }
    }

    @UiHandler("deleteButton")
    public void deleteButtonClick(final ClickEvent event) {
        boolean confirm = Window.confirm("Вы действительно хотите удалить " + (mode == Mode.EVENT ? "событие?" : "приглашение?"));
        if (confirm) {
            deleteButton.state().loading();
            block();
            if (mode == Mode.EVENT) {
                eventsService.deleteEvent(calendarEvent, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        UIUtils.pushNotify("Ошибка при удалении события.", NotifyType.DANGER);
                        deleteButton.state().reset();
                        unblock();
                    }

                    @Override
                    public void onSuccess(Void result) {
                        UIUtils.pushNotify("Событие успешно удалено.", NotifyType.SUCCESS);
                        EventBus.getInstance().fireEvent(new CalendarDeleteEvent(calendarEvent));
                        deleteButton.state().reset();
                        unblock();
                    }
                });
            } else {
                inviteService.deleteInvite(calendarInvite, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        UIUtils.pushNotify("Ошибка при удалении приглашения.", NotifyType.DANGER);
                        deleteButton.state().reset();
                        unblock();
                    }

                    @Override
                    public void onSuccess(Void result) {
                        UIUtils.pushNotify("Приглашение успешно удалено.", NotifyType.SUCCESS);
                        EventBus.getInstance().fireEvent(new CalendarDeleteEvent(calendarEvent));
                        deleteButton.state().reset();
                        unblock();
                    }
                });
            }
        }
    }

    @UiHandler("inviteAccept")
    public void acceptInvite(final ClickEvent event) {
        status = Invite.InviteStatus.ACCEPT;
        updateButtons();
    }

    @UiHandler("inviteReject")
    public void rejectInvite(final ClickEvent event) {
        status = Invite.InviteStatus.REJECT;
        updateButtons();
    }

    @UiHandler("inviteWait")
    public void waitInvite(final ClickEvent event) {
        status = Invite.InviteStatus.WAIT;
        updateButtons();
    }

    private void updateButtons() {
        switch (status) {
            case ACCEPT:
                inviteStatus.setText("Принято");
                inviteStatusReadonly.setText("Принято");
                inviteStatus.setType(ButtonType.SUCCESS);
                inviteStatusReadonly.setType(ButtonType.SUCCESS);
                break;
            case REJECT:
                inviteStatus.setText("Отклонено");
                inviteStatusReadonly.setText("Отклонено");
                inviteStatus.setType(ButtonType.DANGER);
                inviteStatusReadonly.setType(ButtonType.DANGER);
                break;
            case WAIT:
                inviteStatus.setText("Под вопросом");
                inviteStatusReadonly.setText("Под вопросом");
                inviteStatus.setType(ButtonType.WARNING);
                inviteStatusReadonly.setType(ButtonType.WARNING);
                break;
        }
    }

    @UiHandler("sendInvite")
    public void sendInviteButtonClick(final ClickEvent event) {
        if (inviteEmail.validate()) {
            sendInvite.state().loading();
            inviteService.sendInvite(calendarEvent, inviteEmail.getText(), new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(Throwable throwable) {
                    UIUtils.pushNotify("Серверная ошибка при отправке приглашения.", NotifyType.DANGER);
                    inviteEmail.clear();
                    sendInvite.state().reset();
                }

                @Override
                public void onSuccess(Boolean status) {
                    if (status) {
                        UIUtils.pushNotify("Приглашение выслано.", NotifyType.SUCCESS);
                    } else {
                        UIUtils.pushNotify("Не удалось выслать приглашение.", NotifyType.DANGER);
                    }
                    inviteEmail.clear();
                    sendInvite.state().reset();
                    fillInviteList();
                }
            });
        }
    }

    private static ViewEventModalUiBinder ourUiBinder = GWT.create(ViewEventModalUiBinder.class);

    public ViewEventModal() {
        initWidget(ourUiBinder.createAndBindUi(this));
        readOnly = true;
        addPeriodicValidators();
        EventBus.getInstance().addHandler(ClickOnEventInCalendar.TYPE, clickOnEvent -> {
            mode = Mode.EVENT;
            calendarEvent = clickOnEvent.getEvent();

            initModal(clickOnEvent.getFilters());
            onToggleSwitch();

            eventStatusGroup.setVisible(false);
            invitedPeople.setVisible(calendarEvent.getIsPeriodic() == 0);
            addPeople.setVisible(false);
            UIUtils.addEventValidators(eventName, eventStart, eventEnd);
            // Email field validators
            inviteEmail.setValidators(new Validators.emailValidator(), new Validators.isEmptyValidator<>("E-mail"));

            fillFilterList();
            fillInviteList();
        });

        EventBus.getInstance().addHandler(ClickOnInvite.TYPE, clickOnEvent -> {
            mode = Mode.INVITE;
            calendarEvent = clickOnEvent.getInvite().getEvent();
            calendarInvite = clickOnEvent.getInvite();

            initModal(clickOnEvent.getFilters());

            eventStatusGroup.setVisible(true);
            inviteStatus.setVisible(false);
            status = calendarInvite.getStatus();
            invitedPeople.setVisible(false);

            updateButtons();
            fillFilterList();
        });

        EventBus.getInstance().addHandler(DatePickedEvent.TYPE, clickOnEvent -> {
            long duration = eventEnd.getValue().getTime() - eventStart.getValue().getTime();
            eventStart.setValue(clickOnEvent.getDate());
            eventEnd.setValue(new Date(eventStart.getValue().getTime() + duration));
        });
    }

    private void initModal(List<FilterDTO> userFilterList) {
        viewEventModal.show();
        eventFilters = new HashSet<>();
        userFilters = new HashMap<>();
        for (FilterDTO filterDTO : userFilterList) {
            userFilters.put(filterDTO.getId(), filterDTO);
        }
        for (FilterDTO filterDTO : calendarEvent.getAllFilters()) {
            if (userFilters.containsKey(filterDTO.getId())) {
                eventFilters.add(filterDTO.getId());
            }
        }

        eventName.setValue(calendarEvent.getName());
        eventStart.setValue(UIUtils.toUserTimeZone(calendarEvent.getBeginDate()));
        eventEnd.setValue(UIUtils.toUserTimeZone(calendarEvent.getEndDate()));
        eventDescription.setValue(calendarEvent.getDescription());
        timeChange.setVisible(false);
        if (calendarEvent.getIsPeriodic() == 1){
            eventPeriodicFinish.setValue(calendarEvent.getLastDate());
            periodText.setValue(calendarEvent.getPeriod().toString());
            setEventFrequency(calendarEvent.getFrequency());
        }
    }

    private void fillInviteList() {
        invitesActionButtons.clear();
        invitesList.clear();
        Icon spinner = UIUtils.createSpinner(IconSize.TIMES3);
        invitesList.add(spinner);
        inviteService.getAllInvitesForEvent(calendarEvent, new AsyncCallback<List<InviteDTO>>() {
            @Override
            public void onFailure(Throwable throwable) {
                // TODO: what?
                Window.alert("Server error");
            }

            @Override
            public void onSuccess(List<InviteDTO> invites) {
                invitesList.remove(spinner);
                invitesList.clear();
                for (InviteDTO invite : invites) {
                    InputGroup inputGroup = UIUtils.getInviteInputGroup(invite.getInviteEmail(), invite.getStatus());
                    InputGroupButton inputGroupButton = UIUtils.getInviteInputGroupButton(invite.getStatus());
                    if (inputGroupButton != null) {
                        inputGroup.add(inputGroupButton);
                        for (Widget child : inputGroupButton) {
                            if (child instanceof Button) {
                                Button button = (Button) child;
                                invitesActionButtons.add(button);
                                button.addClickHandler(clickEvent -> {
                                    button.state().loading();
                                    block();
                                    if (invite.getStatus().equals(Invite.InviteStatus.WAIT)) {
                                        // Reject
                                        inviteService.rejectInvite(invite, new AsyncCallback<Void>() {
                                            @Override
                                            public void onFailure(Throwable throwable) {
                                                // TODO: add message
                                                button.state().reset();
                                                unblock();
                                            }

                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                button.state().reset();
                                                unblock();
                                                fillInviteList();
                                            }
                                        });
                                    } else {
                                        // Remove
                                        inviteService.removeInvite(invite, new AsyncCallback<Void>() {
                                            @Override
                                            public void onFailure(Throwable throwable) {
                                                // TODO: add message
                                                button.state().reset();
                                                unblock();
                                            }

                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                fillInviteList();
                                                button.state().reset();
                                                unblock();
                                            }
                                        });
                                    }
                                });
                                break;
                            }
                        }
                    }
                    invitesList.add(inputGroup);
                }

                // Disable all buttons in default mode
                if (!edited) {
                    for (Button inviteActionButton : invitesActionButtons) {
                        inviteActionButton.setEnabled(false);
                    }
                }

                timeChange.setVisible(true);
                timeChange.addClickHandler(clickEvent -> {
                    EventBus.getInstance().fireEvent(new PickUpDateEvent(calendarEvent.getId(),
                            eventStart.getValue(), eventEnd.getValue().getTime() - eventStart.getValue().getTime()));
                    edited = true;
                });
            }
        });
    }

    private void reset() {
        viewEventModal.hide();
        readOnly = false;
        switchEditable();
        edited = false;

        filterList.clear();

        eventName.reset();
        eventDescription.reset();
        eventStart.reset();
        eventStart.setValue(null);
        eventEnd.reset();
        eventEnd.setValue(null);
        clearPeriodicForms();
    }

    private void block() {
        readOnly = false;
        switchEditable();
        viewEventModal.setClosable(false);
        viewEventModal.setDataKeyboard(false);

        deleteButton.setEnabled(false);
        okButton.setEnabled(false);
        discardButton.setEnabled(false);
        editButton.setEnabled(false);

        for (Button inviteActionButton : invitesActionButtons) {
            inviteActionButton.setEnabled(false);
        }
    }

    private void unblock() {
        viewEventModal.setClosable(true);
        viewEventModal.setDataKeyboard(true);

        deleteButton.setEnabled(true);
        okButton.setEnabled(true);
        discardButton.setEnabled(true);
        editButton.setEnabled(true);

        for (Button inviteActionButton : invitesActionButtons) {
            inviteActionButton.setEnabled(true);
        }

        reset();
    }

    private void switchEditable() {
        edited = true;
        readOnly = !readOnly;

        for (Button inviteActionButton : invitesActionButtons) {
            inviteActionButton.setEnabled(!inviteActionButton.isEnabled());
        }

        if (mode == Mode.EVENT) {
            eventName.setReadOnly(readOnly);
            eventDescription.setReadOnly(readOnly);
            eventStart.setEnabled(!readOnly);
            eventEnd.setEnabled(!readOnly);
            addPeople.setVisible(!readOnly);
            periodicFromsUpdate();
        } else {
            inviteStatusReadonly.setVisible(readOnly);
            inviteStatus.setVisible(!readOnly);
        }
        rewriteFilterList();
    }

    @Override
    boolean isPeriodic() {
        return calendarEvent.getIsPeriodic() == 1;
    }
}