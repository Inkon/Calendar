package com.calendar.shared.dto;

import com.calendar.shared.entity.Event;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class EventDTO implements Serializable {
    private Integer id;
    private Date beginDate;
    private Date endDate;
    private Date lastDate;
    private Byte isPeriodic;
    private Integer period;
    private Event.EventFrequency frequency;
    private String name;
    private String description;
    private UserDTO owner;
    private Set<InviteDTO> invites = new HashSet<>();
    private Set<FilterDTO> allFilters = new HashSet<>();

    public EventDTO() {
    }

    public EventDTO(Integer id, Date beginDate, Date endDate, Date lastDate, Byte isPeriodic, Integer period, Event.EventFrequency frequency, String name, String description, UserDTO owner, Set<InviteDTO> invites, Set<FilterDTO> allFilters) {
        this.id = id;
        this.beginDate = beginDate;
        this.endDate = endDate;
        this.lastDate = lastDate;
        this.isPeriodic = isPeriodic;
        this.period = period;
        this.frequency = frequency;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.invites = invites;
        this.allFilters = allFilters;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Byte getIsPeriodic() {
        return isPeriodic;
    }

    public void setIsPeriodic(Byte isPeriodic) {
        this.isPeriodic = isPeriodic;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserDTO getOwner() {
        return owner;
    }

    public void setOwner(UserDTO owner) {
        this.owner = owner;
    }

    public Set<InviteDTO> getInvites() {
        return invites;
    }

    public void setInvites(Set<InviteDTO> invites) {
        this.invites = invites;
    }

    public Set<FilterDTO> getAllFilters() {
        return allFilters;
    }

    public void setAllFilters(Set<FilterDTO> allFilters) {
        this.allFilters = allFilters;
    }

    public Date getLastDate() {
        return lastDate;
    }

    public void setLastDate(Date lastDate) {
        this.lastDate = lastDate;
    }

    public Event.EventFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Event.EventFrequency frequency) {
        this.frequency = frequency;
    }
}
