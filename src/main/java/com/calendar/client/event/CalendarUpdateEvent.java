package com.calendar.client.event;

import com.calendar.shared.dto.EventDTO;
import com.google.gwt.event.shared.GwtEvent;

public class CalendarUpdateEvent  extends GwtEvent<CalendarUpdateEventHandler> {
    private EventDTO event;

    public CalendarUpdateEvent(EventDTO eventDTO){
        event = eventDTO;
    }

    public EventDTO getEvent() {
        return event;
    }

    public static Type<CalendarUpdateEventHandler> TYPE = new Type<>();

    public Type<CalendarUpdateEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(CalendarUpdateEventHandler handler) {
        handler.eventOccur(this);
    }
}
