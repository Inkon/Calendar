package com.calendar.client.ui;

import com.calendar.client.EventBus;
import com.calendar.client.event.AddEventEvent;
import com.calendar.client.event.CalendarAddEvent;
import com.calendar.shared.dto.EventDTO;
import com.calendar.shared.dto.FilterDTO;
import com.calendar.shared.dto.UserDTO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;
import org.gwtbootstrap3.extras.toggleswitch.client.ui.ToggleSwitch;

import java.util.HashMap;
import java.util.HashSet;

import static com.calendar.client.ui.Calendar.eventsService;

public class AddEventModal extends AbstractEventModal {
    @UiTemplate("AddEventModal.ui.xml")
    interface ViewEventModalUiBinder extends UiBinder<HTMLPanel, AddEventModal> {
    }

    @UiField
    ToggleSwitch toggleSwitch;

    @UiField
    Modal addEventModal;

    @UiField
    Button okButton, discardButton;

    @UiField
    TextBox eventDescription;

    @UiHandler("okButton")
    public void okButtonClick(final ClickEvent event) {
        if (validate() || periodicValidate()) {
            okButton.state().loading();
            block();
            EventDTO eventDTO = new EventDTO();
            eventDTO.setDescription(eventDescription.getText());
            eventDTO.setName(eventName.getText());
            eventDTO.setBeginDate(UIUtils.toDataBaseTimeZone(eventStart.getValue()));
            eventDTO.setEndDate(UIUtils.toDataBaseTimeZone(eventEnd.getValue()));
            if (isPeriodic()){
                eventDTO.setPeriod(Integer.parseInt(periodText.getValue()));
                eventDTO.setIsPeriodic((byte) 1);
                eventDTO.setLastDate(eventPeriodicFinish.getValue());
                eventDTO.setFrequency(getEventFrequency());
            } else {
                eventDTO.setPeriod(0);
                eventDTO.setIsPeriodic((byte) 0);
            }
            HashSet<FilterDTO> filters = new HashSet<>();
            for (int id : eventFilters) {
                filters.add(userFilters.get(id));
            }
            eventDTO.setAllFilters(filters);

            eventsService.addEvent(eventDTO, new AsyncCallback<HashMap<Integer, UserDTO>>() {
                @Override
                public void onFailure(Throwable throwable) {
                    UIUtils.pushNotify("Ошибка при добавлении события.", NotifyType.DANGER);
                    addEventModal.hide();
                    okButton.state().reset();
                    unblock();
                }

                @Override
                public void onSuccess(HashMap<Integer, UserDTO> map) {
                    int eventId = map.keySet().iterator().next();
                    eventDTO.setId(eventId);
                    UserDTO user = map.get(eventId);
                    eventDTO.setOwner(user);
                    if (eventId == -1) {
                        UIUtils.pushNotify("Ошибка при добавлении события.", NotifyType.DANGER);
                        unblock();
                    } else {
                        EventBus.getInstance().fireEvent(new CalendarAddEvent(eventDTO, eventId));
                        UIUtils.pushNotify("Событие успешно добавлено.", NotifyType.SUCCESS);
                        unblock();
                        clearForms();
                    }
                    okButton.state().reset();
                    addEventModal.hide();
                }
            });
        }
    }

    @UiHandler("discardButton")
    public void discardButtonClick(final ClickEvent event) {
        if (eventName.getValue() == null && eventDescription.getValue() == null && eventEnd == null &&
                eventStart == null || Window.confirm("Отменить создание события?")) {
            addEventModal.hide();
            clearForms();
        }
    }

    private static ViewEventModalUiBinder ourUiBinder = GWT.create(ViewEventModalUiBinder.class);

    public AddEventModal() {
        initWidget(ourUiBinder.createAndBindUi(this));
        readOnly = false;

        EventBus.getInstance().addHandler(AddEventEvent.TYPE, clickOnEvent -> {
            addEventModal.show();
            eventFilters = new HashSet<>();
            userFilters = new HashMap<>();
            for (FilterDTO filterDTO : clickOnEvent.getUserFilters()) {
                userFilters.put(filterDTO.getId(), filterDTO);
            }

            if (clickOnEvent.getDateBegin() != null && clickOnEvent.getDateEnd() != null) {
                eventStart.setValue(clickOnEvent.getDateBegin());
                eventEnd.setValue(clickOnEvent.getDateEnd());
            }

            if (clickOnEvent.getName() != null) {
                eventName.setValue(clickOnEvent.getName());
            }

            fillFilterList();
        });
        UIUtils.addEventValidators(eventName, eventStart, eventEnd);

        toggleSwitch.addValueChangeHandler(valueChangeEvent -> {
            onToggleSwitch();
        });
        addPeriodicValidators();
    }

    private void clearForms() {
        eventName.reset();
        eventDescription.reset();
        eventStart.reset();
        eventStart.setValue(null);
        eventEnd.reset();
        eventEnd.setValue(null);
        filterList.clear();

        clearPeriodicForms();
    }

    private void block() {
        readOnly = true;
        updateForms();
    }

    private void unblock() {
        readOnly = false;
        updateForms();
    }

    private void updateForms() {
        rewriteFilterList();
        addEventModal.setClosable(!readOnly);
        addEventModal.setDataKeyboard(!readOnly);
        eventName.setReadOnly(readOnly);
        eventDescription.setReadOnly(readOnly);
        eventStart.setEnabled(!readOnly);
        eventEnd.setEnabled(!readOnly);
        discardButton.setEnabled(!readOnly);

        periodicFromsUpdate();
    }

    @Override
    protected void clearPeriodicForms() {
        toggleSwitch.setValue(false);
        super.clearPeriodicForms();
    }

    @Override
    protected void periodicFromsUpdate() {
        toggleSwitch.setEnabled(!readOnly);
        super.periodicFromsUpdate();
    }

    @Override
    boolean isPeriodic() {
        return toggleSwitch.getValue();
    }
}
