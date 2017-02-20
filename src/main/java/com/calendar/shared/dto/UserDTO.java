package com.calendar.shared.dto;

import com.calendar.shared.entity.Event;
import com.calendar.shared.entity.User;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UserDTO implements Serializable {
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private Set<EventDTO> ownEvents = new HashSet<>();
    private Set<InviteDTO> invites = new HashSet<>();
    private Set<FilterDTO> filters = new HashSet<>();

    public UserDTO() {
    }

    public UserDTO(Integer id, String email, String firstName, String lastName, Set<EventDTO> ownEvents, Set<InviteDTO> invites, Set<FilterDTO> filters) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.ownEvents = ownEvents;
        this.invites = invites;
        this.filters = filters;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<EventDTO> getOwnEvents() {
        return ownEvents;
    }

    public void setOwnEvents(Set<EventDTO> ownEvents) {
        this.ownEvents = ownEvents;
    }

    public Set<InviteDTO> getInvites() {
        return invites;
    }

    public void setInvites(Set<InviteDTO> invites) {
        this.invites = invites;
    }

    public Set<FilterDTO> getFilters() {
        return filters;
    }

    public void setFilters(Set<FilterDTO> filters) {
        this.filters = filters;
    }
}
