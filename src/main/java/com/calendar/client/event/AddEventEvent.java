package com.calendar.client.event;

import com.calendar.shared.dto.FilterDTO;
import com.google.gwt.event.shared.GwtEvent;

import java.util.Date;
import java.util.List;

public class AddEventEvent  extends GwtEvent<AddEventEventHandler> {
    private List<FilterDTO> userFilters = null;
    private Date dateBegin = null;
    private Date dateEnd = null;
    private String name = null;

    public AddEventEvent(List<FilterDTO> list){
        userFilters = list;
    }

    public AddEventEvent(List<FilterDTO> userFilters, Date dateBegin, Date dateEnd, String name) {
        this.userFilters = userFilters;
        this.dateBegin = dateBegin;
        this.dateEnd = dateEnd;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Date getDateBegin() {
        return dateBegin;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public List<FilterDTO> getUserFilters() {
        return userFilters;
    }

    public static Type<AddEventEventHandler> TYPE = new Type<>();

    public Type<AddEventEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(AddEventEventHandler handler) {
        handler.addEvent(this);
    }
}
