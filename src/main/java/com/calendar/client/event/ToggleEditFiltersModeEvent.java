package com.calendar.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class ToggleEditFiltersModeEvent extends GwtEvent<ToggleEditFiltersModeEventHandler> {
    public static Type<ToggleEditFiltersModeEventHandler> TYPE = new Type<ToggleEditFiltersModeEventHandler>();
    private boolean inEditMode;

    public ToggleEditFiltersModeEvent(boolean state) {
        this.inEditMode = state;
    }

    public boolean isInEditMode() {
        return inEditMode;
    }

    public void setInEditMode(boolean state) {
        this.inEditMode = state;
    }

    public Type<ToggleEditFiltersModeEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(ToggleEditFiltersModeEventHandler handler) {
        handler.onToggleEditFiltersMode(this);
    }
}
