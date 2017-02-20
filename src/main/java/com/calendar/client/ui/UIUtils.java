package com.calendar.client.ui;

import com.calendar.shared.entity.Event;
import com.calendar.shared.entity.Invite;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.form.error.BasicEditorError;
import org.gwtbootstrap3.client.ui.form.validator.Validator;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.DateTimePicker;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;
import org.gwtbootstrap3.extras.notify.client.ui.NotifySettings;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UIUtils {
    public static final long MINUTE = 1000 * 60,
            HOUR = MINUTE * 60,
            DAY = HOUR * 24,
            WEEK = DAY * 7;

    public static Icon createSpinner(IconSize size) {
        Icon spinner = new Icon(IconType.SPINNER);
        spinner.setSpin(true);
        spinner.setSize(size);
        return spinner;
    }

    public static Notify pushNotify(String text, NotifyType type) {
        NotifySettings settings = NotifySettings.newSettings();
        settings.setType(type);
        return Notify.notify(text, settings);
    }

    public static Date getPeriodicDate(Date begin, int shift, Event.EventFrequency frequency, int period) {
        Date result = new Date(begin.getTime());
        switch (frequency) {
            case MINUTELY:
                result.setTime(result.getTime() + MINUTE * shift * period);
                break;
            case HOURLY:
                result.setTime(result.getTime() + HOUR * shift * period);
                break;
            case DAILY:
                result.setTime(result.getTime() + DAY * shift * period);
                break;
            case WEEKLY:
                result.setTime(result.getTime() + WEEK * shift * period);
                break;
            case MONTHLY:
                result.setMonth((begin.getMonth() + period * shift) % 12);
                result.setYear(begin.getYear() + (begin.getMonth() * period * shift) / 12);
                break;
            case YEARLY:
                result.setYear(begin.getYear() + period * shift);
                break;
        }
        return result;
    }

    public static boolean isPeriodicBeginDate(Date begin, Date date, Event.EventFrequency frequency, int period) {
        Date sub;
        for (int i = 0; ; i++) {
            sub = getPeriodicDate(begin, i, frequency, period);
            if (sub.equals(date)) {
                return true;
            } else if (sub.after(date)) {
                return false;
            }
        }
    }

    static void addEventValidators(TextBox eventName, DateTimePicker eventStart, DateTimePicker eventEnd) {
        eventName.addValidator(new Validators.isEmptyValidator<>("Название события"));
        addDateValidators(eventStart, eventEnd);
    }

    static void addDateValidators(DateTimePicker eventStart, DateTimePicker eventEnd) {
        eventStart.setValidators(new Validators.PastValidator());
        addEndValidator(eventStart, eventEnd, "Событие не может закончиться раньше, чем началось!");

    }

    static void addEndValidator(DateTimePicker eventStart, DateTimePicker eventEnd, String message){
        eventEnd.setValidators(new Validator<Date>() {
            @Override
            public int getPriority() {
                return 2;
            }

            @Override
            public List<EditorError> validate(Editor<Date> editor, Date value) {
                List<EditorError> result = new ArrayList<>();
                Date now = new Date();
                if (value == null){
                    value = now;
                    result.add(new BasicEditorError(editor, value, "Укажите время события!"));
                } else {
                    if (value.before(now)) {
                        result.add(new BasicEditorError(editor, value, "Событие не может происходить в прошлом!"));
                    } else {
                        if (eventStart.getValue() != null && value.before(eventStart.getValue())) {
                            result.add(new BasicEditorError(editor, value, message));
                        }
                    }
                }
                return result;
            }
        });
    }

    static Date toUserTimeZone(Date dbTimeZoneDate) {
        Date result = new Date();
        long shift = result.getTimezoneOffset() * 60 * 1000;
        result.setTime(dbTimeZoneDate.getTime() - shift);
        return result;
    }

    static Date toDataBaseTimeZone(Date userTimeZoneDate) {
        Date result = new Date();
        long shift = result.getTimezoneOffset() * 60 * 1000;
        result.setTime(userTimeZoneDate.getTime() + shift);
        return result;
    }

    static LinkedGroupItem createBasicItem(String name, String colorCode) {
        LinkedGroupItem item = new LinkedGroupItem();
        item.setText(name);
        item.addStyleName("text-left");

        Badge colorLabel = new Badge("0");
        colorLabel.getElement().setAttribute("style", "background-color: #" + colorCode + "; color: #" + colorCode);
        item.add(colorLabel);
        return item;
    }

    static InputGroup getInviteInputGroup(String email, Invite.InviteStatus status) {
        InputGroup inputGroup = new InputGroup();
        inputGroup.addStyleName("email-invite-input");

        InputGroupAddon inputGroupAddon = new InputGroupAddon();
        switch (status) {
            case WAIT:
                inputGroupAddon.setIcon(IconType.CLOCK_O);
                break;
            case ACCEPT:
                inputGroupAddon.setIcon(IconType.CHECK);
                break;
            case REJECT:
                inputGroupAddon.setIcon(IconType.TIMES);
                break;
        }
        inputGroup.add(inputGroupAddon);

        TextBox textBox = new TextBox();
        textBox.setPlaceholder(email);
        textBox.setEnabled(false);
        inputGroup.add(textBox);

        return inputGroup;
    }

    static InputGroupButton getInviteInputGroupButton(Invite.InviteStatus status) {
        if (status.equals(Invite.InviteStatus.WAIT) || status.equals(Invite.InviteStatus.REJECT)) {
            InputGroupButton inputGroupButton = new InputGroupButton();
            Button button = new Button();
            button.setText(status.equals(Invite.InviteStatus.WAIT) ? "Отозвать" : "Удалить");
            button.setDataLoadingText("Выполнение...");
            button.addStyleName("inline-invite-button");
            button.setType(ButtonType.DANGER);
            inputGroupButton.add(button);
            return inputGroupButton;
        }
        return null;
    }

    static String textColor(String backgroundColor) {
        String black = "000000";
        String white = "FFFFFF";

        int red = Integer.parseInt(backgroundColor.substring(0, 2), 16);
        int green = Integer.parseInt(backgroundColor.substring(2, 4), 16);
        int blue = Integer.parseInt(backgroundColor.substring(4, 6), 16);

        // Counting the perceptive luminance, human eye favors green color
        // To see the example visit http://codepen.io/WebSeed/full/pvgqEq/
        double result = 1 - (0.299 * red + 0.587 * green + 0.114 * blue) / 255;
        return result < 0.5 ? black : white;
    }

    public static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}
