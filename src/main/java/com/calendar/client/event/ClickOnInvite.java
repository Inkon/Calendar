package com.calendar.client.event;

import com.calendar.shared.dto.FilterDTO;
import com.calendar.shared.dto.InviteDTO;
import com.google.gwt.event.shared.GwtEvent;

import java.util.List;

public class ClickOnInvite  extends GwtEvent<ClickOnInviteHandler> {
    private InviteDTO invite;
    private List<FilterDTO> filters;

    public ClickOnInvite(InviteDTO calendarInvite, List<FilterDTO> list){
        invite = calendarInvite;
        filters = list;
    }

    public InviteDTO getInvite() {
        return invite;
    }

    public List<FilterDTO> getFilters() {
        return filters;
    }

    public static Type<ClickOnInviteHandler> TYPE = new Type<>();

    public Type<ClickOnInviteHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(ClickOnInviteHandler handler) {
        handler.onClickOnInvite(this);
    }
}
