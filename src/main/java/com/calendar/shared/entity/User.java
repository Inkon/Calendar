package com.calendar.shared.entity;

import com.calendar.shared.dto.EventDTO;
import com.calendar.shared.dto.FilterDTO;
import com.calendar.shared.dto.InviteDTO;
import com.calendar.shared.dto.UserDTO;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Entity
@Table(name = "users")
public class User {
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private Set<Event> ownEvents = new HashSet<>();
    private Set<Invite> invites = new HashSet<>();
    private Set<Filter> filters = new HashSet<>();

    public User() {
    }

    public User(UserDTO userDTO) {
        id = userDTO.getId();
        email = userDTO.getEmail();
        firstName = userDTO.getFirstName();
        lastName = userDTO.getLastName();

        Set<EventDTO> ownEventsDTOs = userDTO.getOwnEvents();
        if (ownEventsDTOs != null) {
            Set<Event> ownEvents = new HashSet<>(ownEventsDTOs.size());
            ownEvents.addAll(ownEventsDTOs.stream().map(Event::new).collect(Collectors.toList()));
            this.ownEvents = ownEvents;
        }

        Set<InviteDTO> invitesDTOs = userDTO.getInvites();
        if (invitesDTOs != null) {
            Set<Invite> invites = new HashSet<>(invitesDTOs.size());
            invites.addAll(invitesDTOs.stream().map(Invite::new).collect(Collectors.toList()));
            this.invites = invites;
        }

        Set<FilterDTO> filterDTOs = userDTO.getFilters();
        if (filterDTOs != null) {
            Set<Filter> filters = new HashSet<>(filterDTOs.size());
            filters.addAll(filterDTOs.stream().map(Filter::new).collect(Collectors.toList()));
            this.filters = filters;
        }
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

    @Basic
    @Column(name = "email", nullable = false, length = 100)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Basic
    @Column(name = "first_name", nullable = true, length = 100)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Basic
    @Column(name = "last_name", nullable = true, length = 100)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner", orphanRemoval = true)
    public Set<Event> getOwnEvents() {
        return ownEvents;
    }

    public void setOwnEvents(Set<Event> ownEvents) {
        this.ownEvents = ownEvents;
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public Set<Invite> getInvites() {
        return invites;
    }

    public void setInvites(Set<Invite> invites) {
        this.invites = invites;
    }

    @Transient
    public void linkInvite(Invite invite) {
        invite.setUser(this);
        this.getInvites().add(invite);
    }

    @Transient
    public Set<Event> getInvitedEvents() {
        return getInvites().stream()
                .map(Invite::getEvent)
                .collect(Collectors.toSet());
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", orphanRemoval = true)
    public Set<Filter> getFilters() {
        return filters;
    }

    public void setFilters(Set<Filter> filters) {
        this.filters = filters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != null ? !id.equals(user.id) : user.id != null) return false;
        if (email != null ? !email.equals(user.email) : user.email != null) return false;
        if (firstName != null ? !firstName.equals(user.firstName) : user.firstName != null) return false;
        if (lastName != null ? !lastName.equals(user.lastName) : user.lastName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
