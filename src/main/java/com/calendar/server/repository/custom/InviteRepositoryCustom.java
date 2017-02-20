package com.calendar.server.repository.custom;

import com.calendar.shared.entity.Invite;
import com.calendar.shared.entity.User;

public interface InviteRepositoryCustom {
    Invite createInvite(Invite invite);

    Invite acceptInvite(Invite invite, User user);

    Invite rejectInvite(Invite invite);
}
