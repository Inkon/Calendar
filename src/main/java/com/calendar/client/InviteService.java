package com.calendar.client;

import com.calendar.shared.dto.EventDTO;
import com.calendar.shared.dto.InviteDTO;
import com.calendar.shared.dto.UserDTO;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.*;

@RemoteServiceRelativePath("rpc/inviteService")
public interface InviteService extends RemoteService {

    List<InviteDTO> getInvites();

    void deleteInvite(InviteDTO inviteDTO);

    void updateInvite(InviteDTO inviteDTO);

    InviteDTO activateInvite(String token);

    boolean sendInvite(EventDTO event, String email);

    List<InviteDTO> getAllInvitesForEvent(EventDTO event);

    void rejectInvite(InviteDTO invite);

    void removeInvite(InviteDTO invite);

    TreeMap<Date, List<InviteDTO>> pickUpDate(int id, Date happenAfter, Date happenBefore, long duration);
}
