package com.calendar.client.event;

import com.google.gwt.event.shared.GwtEvent;

import java.util.Date;

public class DatePickedEvent extends GwtEvent<DatePickedEventHandler> {
    private Date date;

    public DatePickedEvent(Date date){
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public static Type<DatePickedEventHandler> TYPE = new Type<>();

    public Type<DatePickedEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(DatePickedEventHandler handler) {
        handler.onDatePicked(this);
    }
}
