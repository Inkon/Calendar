package com.calendar.client.event;

import com.google.gwt.event.shared.GwtEvent;

import java.util.Date;

public class PickUpDateEvent extends GwtEvent<PickUpDateEventHandler> {
    private int eventId;
    private Date begin;
    private long duration;
    
    public PickUpDateEvent(int eventId, Date begin, long duration){
        this.eventId = eventId;
        this.begin = begin;
        this.duration = duration;
    }

    public int getEventId() {
        return eventId;
    }

    public Date getBeginDate() {
        return begin;
    }

    public long getDuration() {
        return duration;
    }

    public static Type<PickUpDateEventHandler> TYPE = new Type<>();

    public Type<PickUpDateEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(PickUpDateEventHandler handler) {
        handler.onChangeDate(this);
    }
}
