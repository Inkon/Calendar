package com.calendar.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface DatePickedEventHandler extends EventHandler {
    void onDatePicked(DatePickedEvent event);
}
