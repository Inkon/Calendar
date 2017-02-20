package com.calendar.client.event;

import com.calendar.shared.dto.EventDTO;
import com.calendar.shared.dto.FilterDTO;
import com.google.gwt.event.shared.GwtEvent;

import java.util.List;

public class ClickOnEventInCalendar extends GwtEvent<ClickOnEventInCalendarHandler> {
    private EventDTO calendarEvent;
    private List<FilterDTO> filters;

    public EventDTO getEvent(){
        return calendarEvent;
    }

    public List<FilterDTO> getFilters(){
        return filters;
    }

    public ClickOnEventInCalendar(EventDTO calendarEvent, List<FilterDTO> filters){
        this.calendarEvent = calendarEvent;
        this.filters = filters;
    }

    public static Type<ClickOnEventInCalendarHandler> TYPE = new Type<>();

    public Type<ClickOnEventInCalendarHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(ClickOnEventInCalendarHandler handler) {
        handler.onClickOnEventInCalendar(this);
    }
}
