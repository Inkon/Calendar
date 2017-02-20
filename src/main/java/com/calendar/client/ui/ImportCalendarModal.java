package com.calendar.client.ui;

import com.calendar.client.EventBus;
import com.calendar.client.event.FilterListChangedEvent;
import com.calendar.client.event.ShowImportCalendarForm;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;

public class ImportCalendarModal extends Composite {

    @UiTemplate("ImportCalendarModal.ui.xml")
    interface ImportCalendarModalUiBinder extends UiBinder<HTMLPanel, ImportCalendarModal> {
    }

    private static ImportCalendarModalUiBinder ourUiBinder = GWT.create(ImportCalendarModalUiBinder.class);

    @UiField
    ModalBody modalBody;

    @UiField
    Modal importCalendarModal;

    @UiField
    Button sendForm;

    public ImportCalendarModal() {
        initWidget(ourUiBinder.createAndBindUi(this));

        VerticalPanel panel = new VerticalPanel();
        final FormPanel form = new FormPanel();
        final FileUpload fileUpload = new FileUpload();
        form.setAction(GWT.getModuleBaseURL() + "fileUploadController/upload");
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setMethod(FormPanel.METHOD_POST);

        fileUpload.setName("file");
        panel.add(fileUpload);
        sendForm.addClickHandler(event -> {
            String filename = fileUpload.getFilename();
            if (filename.length() != 0) {
                form.submit();
                sendForm.state().loading();
            }
        });

        form.addSubmitCompleteHandler(submitCompleteEvent -> {
            sendForm.state().reset();
            form.reset();
            importCalendarModal.hide();
            int result = parseResponse(submitCompleteEvent.getResults());
            if (result == -1) {
                UIUtils.pushNotify("Ошибка при импорте календаря!", NotifyType.DANGER);
            } else {
                EventBus.getInstance().fireEvent(new FilterListChangedEvent());
                UIUtils.pushNotify("Успешно импортировано " + result + " событий.", NotifyType.SUCCESS);
            }
        });

        panel.setSpacing(10);

        form.add(panel);

        modalBody.add(form);

        EventBus.getInstance().addHandler(ShowImportCalendarForm.TYPE, event -> importCalendarModal.show());
    }

    private int parseResponse(String s) {
        return Integer.parseInt(s.substring(s.indexOf('>') + 1, s.indexOf('<', 2)));
    }

}