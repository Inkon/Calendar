package com.calendar.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface FilterListChangedEventHandler extends EventHandler {
    void onAddNewFilter(FilterListChangedEvent event);
}
