package com.calendar.client.event;

import com.calendar.shared.dto.InviteDTO;
import com.google.gwt.event.shared.GwtEvent;

public class CalendarUpdateInvite extends GwtEvent<CalendarUpdateInviteHandler> {
    private InviteDTO invite;

    public CalendarUpdateInvite(InviteDTO inviteDTO){
        invite = inviteDTO;
    }

    public InviteDTO getInvite() {
        return invite;
    }

    public static Type<CalendarUpdateInviteHandler> TYPE = new Type<>();

    public Type<CalendarUpdateInviteHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(CalendarUpdateInviteHandler handler) {
        handler.inviteUpdate(this);
    }
}