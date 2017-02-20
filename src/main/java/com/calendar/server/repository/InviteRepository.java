package com.calendar.server.repository;

import com.calendar.server.repository.custom.InviteRepositoryCustom;
import com.calendar.shared.entity.Invite;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InviteRepository extends CrudRepository<Invite, Integer>, InviteRepositoryCustom {
    Invite findByInviteToken(String token);
    List<Invite> findByUserId(int id);
}
