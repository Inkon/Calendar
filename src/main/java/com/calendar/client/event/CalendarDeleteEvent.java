package com.calendar.client.event;

import com.calendar.shared.dto.EventDTO;
import com.google.gwt.event.shared.GwtEvent;

public class CalendarDeleteEvent  extends GwtEvent<CalendarDeleteEventHandler> {
    private EventDTO event;

    public CalendarDeleteEvent(EventDTO eventDTO){
        event = eventDTO;
    }

    public EventDTO getEvent() {
        return event;
    }

    public static Type<CalendarDeleteEventHandler> TYPE = new Type<>();

    public Type<CalendarDeleteEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(CalendarDeleteEventHandler handler) {
        handler.eventOccur(this);
    }
}