package com.calendar.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class EventDeletionEnd extends GwtEvent<EventDeletionEndHandler> {
    public static Type<EventDeletionEndHandler> TYPE = new Type<EventDeletionEndHandler>();

    public Type<EventDeletionEndHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(EventDeletionEndHandler handler) {
        handler.onEventDeletionEnd(this);
    }
}
