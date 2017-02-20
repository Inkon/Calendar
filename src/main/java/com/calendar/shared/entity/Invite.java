package com.calendar.shared.entity;

import com.calendar.shared.dto.InviteDTO;

import javax.persistence.*;


@Entity
@Table(name = "invites")
public class Invite {
    private Integer id;
    private InviteStatus status;
    private String inviteEmail;
    private String inviteToken;
    private Event event;
    private User user;

    public Invite() {
    }

    public Invite(InviteDTO inviteDTO) {
        id = inviteDTO.getId();
        status = inviteDTO.getStatus();
        inviteEmail = inviteDTO.getInviteEmail();
        inviteToken = inviteDTO.getInviteToken();
        event = new Event(inviteDTO.getEvent());
        user = new User(inviteDTO.getUser());
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name = "event_id")
    public Event getEvent() {
        return event;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(name = "invites_users",
            joinColumns = @JoinColumn(name = "invite_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", updatable = true))
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public InviteStatus getStatus() {
        return status;
    }

    public void setStatus(InviteStatus status) {
        this.status = status;
    }

    @Basic
    @Column(name = "invite_email", nullable = false, length = 100)
    public String getInviteEmail() {
        return inviteEmail;
    }

    public void setInviteEmail(String inviteEmail) {
        this.inviteEmail = inviteEmail;
    }

    @Basic
    @Column(name = "invite_token", nullable = true, length = 100)
    public String getInviteToken() {
        return inviteToken;
    }

    public void setInviteToken(String inviteToken) {
        this.inviteToken = inviteToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Invite invite = (Invite) o;

        if (id != null ? !id.equals(invite.id) : invite.id != null) return false;
        if (status != null ? !status.equals(invite.status) : invite.status != null) return false;
        if (inviteEmail != null ? !inviteEmail.equals(invite.inviteEmail) : invite.inviteEmail != null) return false;
        if (inviteToken != null ? !inviteToken.equals(invite.inviteToken) : invite.inviteToken != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (inviteEmail != null ? inviteEmail.hashCode() : 0);
        result = 31 * result + (inviteToken != null ? inviteToken.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Invite{" +
                "id=" + id +
                ", status=" + status +
                ", inviteEmail='" + inviteEmail + '\'' +
                ", inviteToken='" + inviteToken + '\'' +
                ", event=" + event +
                ", user=" + user +
                '}';
    }

    public enum InviteStatus {
        WAIT, ACCEPT, REJECT;
    }
}
