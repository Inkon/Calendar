package com.calendar.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class FilterListChangedEvent extends GwtEvent<FilterListChangedEventHandler> {
    public static Type<FilterListChangedEventHandler> TYPE = new Type<FilterListChangedEventHandler>();

    public Type<FilterListChangedEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(FilterListChangedEventHandler handler) {
        handler.onAddNewFilter(this);
    }
}
