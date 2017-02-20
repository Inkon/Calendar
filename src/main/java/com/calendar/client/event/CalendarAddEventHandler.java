package com.calendar.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface CalendarAddEventHandler extends EventHandler {
    void eventOccur(CalendarAddEvent event);
}
