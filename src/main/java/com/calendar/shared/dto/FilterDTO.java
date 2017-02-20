package com.calendar.shared.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class FilterDTO implements Serializable {
    private Integer id;
    private String description;
    private String color;
    private UserDTO user;
    private Set<EventDTO> attachedEvents = new HashSet<>();

    public FilterDTO() {
    }

    public FilterDTO(Integer id, String description, String color, UserDTO user, Set<EventDTO> attachedEvents) {
        this.id = id;
        this.description = description;
        this.color = color;
        this.user = user;
        this.attachedEvents = attachedEvents;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public Set<EventDTO> getAttachedEvents() {
        return attachedEvents;
    }

    public void setAttachedEvents(Set<EventDTO> attachedEvents) {
        this.attachedEvents = attachedEvents;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FilterDTO ? Objects.equals(((FilterDTO) obj).getId(), getId()) : super.equals(obj);
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
