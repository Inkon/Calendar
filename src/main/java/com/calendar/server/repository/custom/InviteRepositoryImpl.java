package com.calendar.server.repository.custom;

import com.calendar.server.repository.InviteRepository;
import com.calendar.server.repository.UserRepository;
import com.calendar.shared.entity.Invite;
import com.calendar.shared.entity.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public class InviteRepositoryImpl implements InviteRepositoryCustom {
    @Autowired
    InviteRepository inviteRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public Invite createInvite(Invite invite) {
        invite.setStatus(Invite.InviteStatus.WAIT);
        invite.setInviteToken(UUID.randomUUID().toString().replaceAll("-", ""));
        return inviteRepository.save(invite);
    }

    @Override
    public Invite acceptInvite(Invite invite, User user) {
        invite.setStatus(Invite.InviteStatus.ACCEPT);
        user.linkInvite(invite);
        userRepository.save(user);

        return invite;
    }

    @Override
    public Invite rejectInvite(Invite invite) {
        invite.setStatus(Invite.InviteStatus.REJECT);
        return inviteRepository.save(invite);
    }
}
