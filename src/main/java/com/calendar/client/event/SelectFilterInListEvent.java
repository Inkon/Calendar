package com.calendar.client.event;

import com.calendar.shared.dto.FilterDTO;
import com.google.gwt.event.shared.GwtEvent;

public class SelectFilterInListEvent extends GwtEvent<SelectFilterInListEventHandler> {
    public static Type<SelectFilterInListEventHandler> TYPE = new Type<SelectFilterInListEventHandler>();
    private FilterDTO selectedFilter;

    public SelectFilterInListEvent(FilterDTO filterDTO) {
        selectedFilter = filterDTO;
    }

    public FilterDTO getSelectedFilter() {
        return selectedFilter;
    }

    public void setSelectedFilter(FilterDTO selectedFilter) {
        this.selectedFilter = selectedFilter;
    }

    public Type<SelectFilterInListEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(SelectFilterInListEventHandler handler) {
        handler.onSelectFilterInList(this);
    }
}
