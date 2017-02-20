package com.calendar.client.event;

import com.calendar.shared.dto.FilterDTO;
import com.google.gwt.event.shared.GwtEvent;

public class ShowFilterEditModalEvent extends GwtEvent<ShowFilterEditModalEventHandler> {
    public static Type<ShowFilterEditModalEventHandler> TYPE = new Type<ShowFilterEditModalEventHandler>();
    FilterDTO targetFilter;

    public ShowFilterEditModalEvent(FilterDTO filter) {
        targetFilter = filter;
    }

    public FilterDTO getTargetFilter() {
        return targetFilter;
    }

    public void setTargetFilter(FilterDTO targetFilter) {
        this.targetFilter = targetFilter;
    }

    public Type<ShowFilterEditModalEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(ShowFilterEditModalEventHandler handler) {
        handler.onFilterEdit(this);
    }
}
