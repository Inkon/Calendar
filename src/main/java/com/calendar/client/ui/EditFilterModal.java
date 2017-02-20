package com.calendar.client.ui;

import com.calendar.client.EventBus;
import com.calendar.client.FilterService;
import com.calendar.client.FilterServiceAsync;
import com.calendar.client.event.FilterListChangedEvent;
import com.calendar.client.event.ShowFilterEditModalEvent;
import com.calendar.shared.dto.FilterDTO;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.form.validator.Validator;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;

import java.util.List;

public class EditFilterModal extends Composite {

    @UiTemplate("EditFilterModal.ui.xml")
    interface AddFilterModalUiBinder extends UiBinder<HTMLPanel, EditFilterModal> {
    }

    @UiField
    TextBox colorPicker, filterName;

    @UiField
    Button editFilter, closeModal;

    @UiField
    Modal editFilterModal;

    private static AddFilterModalUiBinder ourUiBinder = GWT.create(AddFilterModalUiBinder.class);
    private final FilterServiceAsync filterService = GWT.create(FilterService.class);
    private FilterDTO targetFilter;

    public EditFilterModal() {
        initWidget(ourUiBinder.createAndBindUi(this));

        ScriptInjector.fromUrl("js/jscolor.js").setCallback(new Callback<Void, Exception>() {
            public void onFailure(Exception reason) {
                System.out.println("JSColor load fail.");
            }

            public void onSuccess(Void result) {
                System.out.println("JSColor load success.");
            }
        }).setWindow(ScriptInjector.TOP_WINDOW).inject();

        filterName.addValidator(new Validator<String>() {
            @Override
            public int getPriority() {
                return 0;
            }

            @Override
            public List<EditorError> validate(Editor<String> editor, String s) {
                return Validators.filterName(editor, s);
            }
        });

        EventBus.getInstance().addHandler(ShowFilterEditModalEvent.TYPE, event -> {
            targetFilter = event.getTargetFilter();
            // Fill form and show it
            filterName.setText(targetFilter.getDescription());
            colorPicker.setValue(targetFilter.getColor(), true);
            colorPicker.setText(targetFilter.getColor());
            editFilterModal.show();
        });
    }

    @UiHandler("editFilter")
    public void onEditFilterButtonClick(ClickEvent event) {
        if (filterName.validate()) {
            editFilter.state().loading();
            // Make modal non-closable
            editFilterModal.setClosable(false);
            editFilterModal.setDataKeyboard(false);
            closeModal.setEnabled(false);

            targetFilter.setDescription(filterName.getText());
            targetFilter.setColor(colorPicker.getValue());
            filterService.updateFilter(targetFilter, new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(Throwable throwable) {
                    // Make modal closable again and reset buttons
                    editFilterModal.setClosable(true);
                    editFilterModal.setDataKeyboard(true);
                    editFilter.state().reset();
                    closeModal.setEnabled(true);
                }

                @Override
                public void onSuccess(Boolean status) {
                    // Remove spinner and make modal closable
                    editFilter.state().reset();
                    editFilterModal.setClosable(true);
                    editFilterModal.setDataKeyboard(true);
                    closeModal.setEnabled(true);
                    editFilterModal.hide();

                    EventBus.getInstance().fireEvent(new FilterListChangedEvent());
                    if (status) {
                        UIUtils.pushNotify("Фильтр изменен.", NotifyType.SUCCESS);
                    } else {
                        UIUtils.pushNotify("Ошибка при обновлении фильтра.", NotifyType.DANGER);
                    }
                }
            });
        }
    }
}