package com.calendar.client.event;

import com.calendar.shared.dto.FilterDTO;
import com.google.gwt.event.shared.GwtEvent;

import java.util.List;

public class FilterListLoadedEvent extends GwtEvent<FilterListLoadedEventHandler> {
    private List<FilterDTO> list;

    public List<FilterDTO> getList(){
        return list;
    }

    public FilterListLoadedEvent(List<FilterDTO> list){
        this.list = list;
    }

    public static Type<FilterListLoadedEventHandler> TYPE = new Type<>();

    @Override
    public Type<FilterListLoadedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(FilterListLoadedEventHandler handler) {
        handler.filterListLoaded(this);
    }
}
