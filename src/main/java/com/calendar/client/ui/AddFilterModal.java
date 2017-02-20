package com.calendar.client.ui;

import com.calendar.client.EventBus;
import com.calendar.client.FilterService;
import com.calendar.client.FilterServiceAsync;
import com.calendar.client.event.FilterListChangedEvent;
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

public class AddFilterModal extends Composite {

    @UiTemplate("AddFilterModal.ui.xml")
    interface AddFilterModalUiBinder extends UiBinder<HTMLPanel, AddFilterModal> {
    }

    @UiField
    TextBox colorPicker, filterName;

    @UiField
    Button addFilter, closeModal;

    @UiField
    Modal addFilterModal;

    private static AddFilterModalUiBinder ourUiBinder = GWT.create(AddFilterModalUiBinder.class);
    private final FilterServiceAsync filterService = GWT.create(FilterService.class);

    public AddFilterModal() {
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
    }

    @UiHandler("addFilter")
    public void onAddFilterButtonClick(ClickEvent event) {
        if (filterName.validate()) {
            addFilter.state().loading();
            // Make modal non-closable
            addFilterModal.setClosable(false);
            closeModal.setEnabled(false);


            filterService.addFilter(filterName.getText(), colorPicker.getText(), new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(Throwable throwable) {
                    // Make modal closable again and reset buttons
                    addFilterModal.setClosable(true);
                    addFilter.state().reset();
                    closeModal.setEnabled(true);
                }

                @Override
                public void onSuccess(Boolean status) {
                    // Remove spinner and make modal closable
                    addFilter.state().reset();
                    addFilterModal.setClosable(true);
                    closeModal.setEnabled(true);
                    // Clean fields and hide modal
                    resetForm();
                    addFilterModal.hide();

                    if (status) {
                        EventBus.getInstance().fireEvent(new FilterListChangedEvent());
                        UIUtils.pushNotify("Фильтр добавлен.", NotifyType.SUCCESS);
                    } else {
                        UIUtils.pushNotify("Ошибка при добавлении фильтра.", NotifyType.DANGER);
                    }
                }
            });
        }
    }

    private void resetForm() {
        filterName.reset();
        colorPicker.reset();
    }
}