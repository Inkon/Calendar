package com.calendar.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class EventDeletionBegin extends GwtEvent<EventDeletionBeginHandler> {
    public static Type<EventDeletionBeginHandler> TYPE = new Type<EventDeletionBeginHandler>();

    public Type<EventDeletionBeginHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(EventDeletionBeginHandler handler) {
        handler.onEventDeletionBegin(this);
    }
}
