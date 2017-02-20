package com.calendar.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class ShowImportCalendarForm extends GwtEvent<ShowImportCalendarFormHandler> {
    public static Type<ShowImportCalendarFormHandler> TYPE = new Type<ShowImportCalendarFormHandler>();

    public Type<ShowImportCalendarFormHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(ShowImportCalendarFormHandler handler) {
        handler.onShowImportCalendarForm(this);
    }
}
