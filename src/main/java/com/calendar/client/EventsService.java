package com.calendar.client;

import com.calendar.shared.dto.EventDTO;
import com.calendar.shared.dto.UserDTO;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.HashMap;
import java.util.Set;

@RemoteServiceRelativePath("rpc/eventsService")
public interface EventsService  extends RemoteService {
    Set<EventDTO> getEvents();

    void deleteEvent(EventDTO eventDTO);

    HashMap<Integer, UserDTO> addEvent(EventDTO eventDTO);

    void updateEvent(EventDTO eventDTO);

    EventDTO parseQuickEventAdding(String query);
}
