package com.calendar.client.ui;

import com.calendar.client.EventBus;
import com.calendar.client.InviteService;
import com.calendar.client.InviteServiceAsync;
import com.calendar.client.event.DatePickedEvent;
import com.calendar.client.event.PickUpDateEvent;
import com.calendar.shared.dto.InviteDTO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.form.error.BasicEditorError;
import org.gwtbootstrap3.client.ui.form.validator.Validator;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.DateTimePicker;

import java.util.*;

import static com.calendar.client.ui.UIUtils.DAY;

public class DateFindModal extends Composite {
    private Date result = new Date();
    private long duration;
    private int id;
    private InviteServiceAsync inviteService = GWT.create(InviteService.class);

    @UiTemplate("DateFindModal.ui.xml")
    interface DateFindModalUiBinder extends UiBinder<HTMLPanel, DateFindModal> {
    }

    @UiField
    Modal dateFindModal;

    @UiField
    DateTimePicker happenAfter, happenBefore;

    @UiField
    FormControlStatic pickedDate;

    @UiField
    FormGroup notAttend;

    @UiField
    FieldSet invitesList;

    @UiField
    Button closeModal, okButton, pickUpButton;

    private static DateFindModalUiBinder ourUiBinder = GWT.create(DateFindModalUiBinder.class);

    @UiHandler("okButton")
    public void okButtonClick(final ClickEvent event) {
        EventBus.getInstance().fireEvent(new DatePickedEvent(result));
        dateFindModal.hide();
    }

    @UiHandler("pickUpButton")
    public void retryButtonClick(final ClickEvent event) {
        if (happenAfter.validate() && happenBefore.validate()) {
            pickUpDate();
        }
    }

    @UiHandler("closeModal")
    public void closeButton(final ClickEvent event) {
        dateFindModal.hide();
    }

    public DateFindModal() {
        initWidget(ourUiBinder.createAndBindUi(this));

        EventBus.getInstance().addHandler(PickUpDateEvent.TYPE, clickOnEvent -> {
            dateFindModal.show();
            result.setTime(clickOnEvent.getBeginDate().getTime());
            clearForms();
            duration = clickOnEvent.getDuration();
            id = clickOnEvent.getEventId();
            if (happenAfter.getValue() == null) {
                happenAfter.setValue(clickOnEvent.getBeginDate());
                Date end = new Date();
                end.setTime(clickOnEvent.getBeginDate().getTime() + DAY);
                happenBefore.setValue(end);
            }
            happenAfter.setValidators(new Validators.PastValidator());
            happenBefore.setValidators(new Validators.PastValidator(), (new Validator<Date>() {
                @Override
                public int getPriority() {
                    return 2;
                }

                @Override
                public List<EditorError> validate(Editor<Date> editor, Date value) {
                    List<EditorError> result = new ArrayList<>();
                    if (value.before(happenAfter.getValue())) {
                        result.add(new BasicEditorError(editor, value, "Верхняя граница должна начинаться позже нижней!"));
                    }
                    return result;
                }
            }));
        });
    }

    private void pickUpDate() {
        blocking(true);
        clearForms();
        inviteService.pickUpDate(id, UIUtils.toDataBaseTimeZone(happenAfter.getValue()),
                UIUtils.toDataBaseTimeZone(happenBefore.getValue()), duration, new AsyncCallback<TreeMap<Date, List<InviteDTO>>>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        Window.alert("Произошла ошибка");
                        blocking(false);
                    }

                    @Override
                    public void onSuccess(TreeMap<Date, List<InviteDTO>> map) {
                        Date date = map.firstKey();
                        result = UIUtils.toUserTimeZone(date);
                        pickedDate.setText(result.toLocaleString());
                        List<InviteDTO> invites = map.get(date);
                        if (!invites.isEmpty()) {
                            notAttend.setVisible(true);
                            for (InviteDTO invite : invites) {
                                InputGroup inputGroup = UIUtils.getInviteInputGroup(invite.getInviteEmail(), invite.getStatus());
                                invitesList.add(inputGroup);
                            }
                        }
                        blocking(false);
                    }
                });
    }

    private void clearForms() {
        pickedDate.setText(result.toLocaleString());
        notAttend.setVisible(false);
        invitesList.clear();
    }

    private void blocking(boolean block) {
        if (block) {
            pickUpButton.state().loading();
        } else {
            pickUpButton.state().reset();
        }
        dateFindModal.setClosable(!block);
        dateFindModal.setDataKeyboard(!block);
        happenAfter.setEnabled(!block);
        happenBefore.setEnabled(!block);
        closeModal.setEnabled(!block);
        okButton.setEnabled(!block);
        pickUpButton.setEnabled(!block);
    }
}
