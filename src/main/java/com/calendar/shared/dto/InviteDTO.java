package com.calendar.shared.dto;

import com.calendar.shared.entity.Invite;

import java.io.Serializable;

public class InviteDTO implements Serializable {
    private Integer id;
    private Invite.InviteStatus status;
    private String inviteEmail;
    private String inviteToken;
    private EventDTO event;
    private UserDTO user;

    public InviteDTO() {
    }

    public InviteDTO(Integer id, Invite.InviteStatus status, String inviteEmail, String inviteToken, EventDTO event, UserDTO user) {
        this.id = id;
        this.status = status;
        this.inviteEmail = inviteEmail;
        this.inviteToken = inviteToken;
        this.event = event;
        this.user = user;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Invite.InviteStatus getStatus() {
        return status;
    }

    public void setStatus(Invite.InviteStatus status) {
        this.status = status;
    }

    public String getInviteEmail() {
        return inviteEmail;
    }

    public void setInviteEmail(String inviteEmail) {
        this.inviteEmail = inviteEmail;
    }

    public String getInviteToken() {
        return inviteToken;
    }

    public void setInviteToken(String inviteToken) {
        this.inviteToken = inviteToken;
    }

    public EventDTO getEvent() {
        return event;
    }

    public void setEvent(EventDTO event) {
        this.event = event;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }
}
