package com.calendar.client.event;

import com.calendar.shared.dto.EventDTO;
import com.calendar.shared.dto.InviteDTO;
import com.google.gwt.event.shared.GwtEvent;

public class CalendarAddEvent  extends GwtEvent<CalendarAddEventHandler> {
    private EventDTO event;
    private InviteDTO inviteDTO;
    private int id;

    public CalendarAddEvent(EventDTO eventDTO, int id){
        event = eventDTO;
        this.id = id;
        inviteDTO = null;
    }

    public CalendarAddEvent(InviteDTO inviteDTO){
        this.inviteDTO = inviteDTO;
        event = inviteDTO.getEvent();
        id = inviteDTO.getEvent().getId();
    }

    public InviteDTO getInviteDTO() {
        return inviteDTO;
    }

    public int getId() {
        return id;
    }

    public EventDTO getEvent() {
        return event;
    }

    public static Type<CalendarAddEventHandler> TYPE = new Type<>();

    public Type<CalendarAddEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(CalendarAddEventHandler handler) {
        handler.eventOccur(this);
    }
}