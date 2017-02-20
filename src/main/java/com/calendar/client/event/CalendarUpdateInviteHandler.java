package com.calendar.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface CalendarUpdateInviteHandler extends EventHandler {
    void inviteUpdate(CalendarUpdateInvite event);
}
