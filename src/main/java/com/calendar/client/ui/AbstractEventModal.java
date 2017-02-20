package com.calendar.client.ui;

import com.calendar.shared.dto.FilterDTO;
import com.calendar.shared.entity.Event;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.gwtbootstrap3.client.ui.form.error.BasicEditorError;
import org.gwtbootstrap3.client.ui.form.validator.Validator;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.DateTimePicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public abstract class AbstractEventModal extends Composite {
    protected final String
            STRING_MINUTELY = "Минутная",
            STRING_HOURLY = "Часовая",
            STRING_DAILY = "Дневная",
            STRING_WEEKLY = "Недельная",
            STRING_MONTHLY = "Месячная",
            STRING_YEARLY = "Годовая";

    protected boolean readOnly = true;
    protected HashMap<Integer, FilterDTO> userFilters = new HashMap<>();
    protected HashSet<Integer> eventFilters = new HashSet<>();

    @UiField
    LinkedGroup filterList;

    @UiField
    DateTimePicker eventStart, eventEnd, eventPeriodicFinish;

    @UiField
    Button periodButton;

    @UiField
    TextBox eventName, periodText;

    @UiField
    FormGroup periodicFrequency, groupPeriodicEventFinish, groupPeriod;

    @UiField
    FormLabel eventEndLabel, eventEndPeriodicLabel, eventStartLabel, eventStartPeriodicLabel;

    @UiHandler("minutely")
    public void minutelyClick(final ClickEvent event) {
        periodButton.setText(STRING_MINUTELY);
    }

    @UiHandler("hourly")
    public void hourlyClick(final ClickEvent event) {
        periodButton.setText(STRING_HOURLY);
    }

    @UiHandler("daily")
    public void dailyClick(final ClickEvent event) {
        periodButton.setText(STRING_DAILY);
    }

    @UiHandler("weekly")
    public void weeklyClick(final ClickEvent event) {
        periodButton.setText(STRING_WEEKLY);
    }

    @UiHandler("monthly")
    public void monthlyClick(final ClickEvent event) {
        periodButton.setText(STRING_MONTHLY);
    }

    @UiHandler("yearly")
    public void yearlyClick(final ClickEvent event) {
        periodButton.setText(STRING_YEARLY);
    }

    protected void fillFilterList() {
        filterList.clear();
        for (int filterId : eventFilters) {
            FilterDTO filter = userFilters.get(filterId);
            LinkedGroupItem item = UIUtils.createBasicItem(filter.getDescription(), filter.getColor());
            if (!readOnly) {
                ButtonGroup group = new ButtonGroup();

                Button removeButton = new Button();
                removeButton.setIcon(IconType.REMOVE);
                removeButton.setType(ButtonType.DANGER);
                removeButton.setSize(ButtonSize.EXTRA_SMALL);
                removeButton.addClickHandler(clickEvent -> {
                    filterList.remove(item);
                    eventFilters.remove(filterId);
                    rewriteFilterList();
                });
                removeButton.setMarginLeft(5d);

                group.add(removeButton);
                item.add(group);
            }
            filterList.add(item);
        }
        if (!readOnly) {
            ButtonGroup addFilterGroup = new ButtonGroup();

            Button addingButton = new Button();
            addingButton.setDataToggle(Toggle.DROPDOWN);
            addingButton.setText("Добавить фильтр");
            addingButton.setType(ButtonType.INFO);
            addingButton.setMarginTop(10d);

            DropDownMenu dropDownMenu = new DropDownMenu();
            for (int filterId : userFilters.keySet()) {
                FilterDTO filter = userFilters.get(filterId);
                if (!eventFilters.contains(filterId)) {
                    AnchorListItem item = new AnchorListItem();
                    Button addButton = new Button(filter.getDescription());
                    addButton.getElement().setAttribute("style", "background-color: #" + filter.getColor() +
                            "; color: #" + UIUtils.textColor(filter.getColor()) + "; margin-left: 10px");
                    addButton.addClickHandler(clickEvent -> {
                        eventFilters.add(filterId);
                        rewriteFilterList();
                    });
                    item.add(addButton);
                    dropDownMenu.add(item);
                }
            }

            addFilterGroup.add(addingButton);
            addFilterGroup.add(dropDownMenu);

            filterList.add(addFilterGroup);
        }
    }

    protected void rewriteFilterList() {
        filterList.clear();
        fillFilterList();
    }

    protected boolean validate(){
        return !isPeriodic() && eventName.validate() && eventStart.validate() && eventEnd.validate();
    }

    protected boolean periodicValidate(){
        return isPeriodic() && eventName.validate() && eventStart.validate() && eventEnd.validate() &&
                eventPeriodicFinish.validate() && periodText.validate();
    }

    protected void addPeriodicValidators() {
        UIUtils.addEndValidator(eventEnd, eventPeriodicFinish, "Событие должно произойти хотя бы раз!");
        periodText.setValidators(new Validator<String>() {
            @Override
            public int getPriority() {
                return 0;
            }

            @Override
            public List<EditorError> validate(Editor<String> editor, String value) {
                List<EditorError> result = new ArrayList<>();
                if (value == null) {
                    value = "";
                }
                if (value.equals("")) {
                    result.add(new BasicEditorError(editor, value, "Частота события не может быть пустой!"));
                } else if (!UIUtils.isNumeric(value)) {
                    result.add(new BasicEditorError(editor, value, "Период должен быть числом!"));
                } else if (value.length() > 3) {
                    result.add(new BasicEditorError(editor, value, "Период должен быть меньше 1000!"));
                } else if (UIUtils.getPeriodicDate(eventStart.getValue(), 1, getEventFrequency(), Integer.parseInt(value))
                        .before(eventEnd.getValue())) {
                    result.add(new BasicEditorError(editor, value, "Периодичность события не может быть меньше его длительности!"));
                }
                return result;
            }
        });
    }

    protected void clearPeriodicForms(){
        periodButton.setText(STRING_DAILY);
        eventPeriodicFinish.reset();
        eventPeriodicFinish.setValue(null);
        periodText.reset();
        onToggleSwitch();
    }

    protected void periodicFromsUpdate(){
        onToggleSwitch();
        periodButton.setEnabled(!readOnly);
        eventPeriodicFinish.setEnabled(!readOnly);
        periodText.setReadOnly(readOnly);
    }

    protected void onToggleSwitch() {
        boolean periodic = isPeriodic();
        eventEndLabel.setVisible(!periodic);
        eventStartLabel.setVisible(!periodic);
        eventEndPeriodicLabel.setVisible(periodic);
        eventStartPeriodicLabel.setVisible(periodic);
        groupPeriodicEventFinish.setVisible(periodic);
        periodicFrequency.setVisible(periodic);
        groupPeriod.setVisible(periodic);
    }

    protected Event.EventFrequency getEventFrequency() {
        switch (periodButton.getText()) {
            case STRING_MINUTELY:
                return Event.EventFrequency.MINUTELY;
            case STRING_HOURLY:
                return Event.EventFrequency.HOURLY;
            case STRING_DAILY:
                return Event.EventFrequency.DAILY;
            case STRING_WEEKLY:
                return Event.EventFrequency.WEEKLY;
            case STRING_MONTHLY:
                return Event.EventFrequency.MONTHLY;
            case STRING_YEARLY:
                return Event.EventFrequency.YEARLY;
            default:
                return null;
        }
    }

    protected void setEventFrequency(Event.EventFrequency frequency){
        switch (frequency){
            case MINUTELY:
                periodButton.setText(STRING_MINUTELY);
                break;
            case HOURLY:
                periodButton.setText(STRING_HOURLY);
                break;
            case DAILY:
                periodButton.setText(STRING_DAILY);
                break;
            case WEEKLY:
                periodButton.setText(STRING_WEEKLY);
                break;
            case MONTHLY:
                periodButton.setText(STRING_MONTHLY);
                break;
            case YEARLY:
                periodButton.setText(STRING_YEARLY);
                break;
        }
    }

    abstract boolean isPeriodic();
}
