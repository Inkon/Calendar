package com.calendar.shared.entity;

import com.calendar.shared.dto.EventDTO;
import com.calendar.shared.dto.FilterDTO;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Entity
@Table(name = "filters")
public class Filter {
    private Integer id;
    private String description;
    private String color;
    private User user;
    private Set<Event> attachedEvents = new HashSet<>();

    public Filter() {
    }

    public Filter(FilterDTO filterDTO) {
        id = filterDTO.getId();
        description = filterDTO.getDescription();
        color = filterDTO.getColor();
        user = new User(filterDTO.getUser());

        Set<EventDTO> eventDTOs = filterDTO.getAttachedEvents();
        if (eventDTOs != null) {
            Set<Event> attachedEvents = new HashSet<>(eventDTOs.size());
            attachedEvents.addAll(eventDTOs.stream().map(Event::new).collect(Collectors.toList()));
            this.attachedEvents = attachedEvents;
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
    @Column(name = "description", nullable = true, length = 100)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "color", nullable = true, length = 6)
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @ManyToMany
    @JoinTable(name = "events_filters",
            joinColumns = @JoinColumn(name = "filter_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id"))
    public Set<Event> getAttachedEvents() {
        return attachedEvents;
    }

    public void setAttachedEvents(Set<Event> attachedEvents) {
        this.attachedEvents = attachedEvents;
    }

    @Transient
    public void attachEvent(Event e) {
        this.getAttachedEvents().add(e);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Filter filter = (Filter) o;

        if (id != null ? !id.equals(filter.id) : filter.id != null) return false;
        if (description != null ? !description.equals(filter.description) : filter.description != null) return false;
        if (color != null ? !color.equals(filter.color) : filter.color != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Filter{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", color='" + color + '\'' +
                ", user=" + user +
                '}';
    }
}
