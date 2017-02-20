package com.calendar.shared.entity;

import com.calendar.shared.dto.EventDTO;
import com.calendar.shared.dto.InviteDTO;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Entity
@Table(name = "events")
public class Event {
    private Integer id;
    private Date beginDate;
    private Date endDate;
    private Date lastDate;
    private Byte isPeriodic;
    private Integer period;
    private EventFrequency frequency;
    private String name;
    private String description;
    private User owner;
    private Set<Invite> invites = new HashSet<>();
    private Set<Filter> allFilters = new HashSet<>();

    public Event() {
    }

    public Event(EventDTO eventDTO) {
        id = eventDTO.getId();
        beginDate = eventDTO.getBeginDate();
        endDate = eventDTO.getEndDate();
        lastDate = eventDTO.getLastDate();
        isPeriodic = eventDTO.getIsPeriodic();
        period = eventDTO.getPeriod();
        name = eventDTO.getName();
        description = eventDTO.getDescription();
        owner = new User(eventDTO.getOwner());

        Set<InviteDTO> inviteDTOs = eventDTO.getInvites();
        if (inviteDTOs != null) {
            Set<Invite> invites = new HashSet<>(inviteDTOs.size());
            invites.addAll(inviteDTOs.stream().map(Invite::new).collect(Collectors.toList()));
            this.invites = invites;
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

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "begin_date", nullable = false)
    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_date", nullable = true)
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_date", nullable = true)
    public Date getLastDate() {
        return lastDate;
    }

    public void setLastDate(Date lastDate) {
        this.lastDate = lastDate;
    }

    @Basic
    @Column(name = "is_periodic", nullable = true)
    public Byte getIsPeriodic() {
        return isPeriodic;
    }

    public void setIsPeriodic(Byte isPeriodic) {
        this.isPeriodic = isPeriodic;
    }

    @Basic
    @Column(name = "period", nullable = true)
    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = true)
    public EventFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(EventFrequency frequency) {
        this.frequency = frequency;
    }

    @Basic
    @Column(name = "name", nullable = false, length = 100)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "description", nullable = true, length = -1)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne
    @JoinColumn(name = "owner_id")
    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    @OneToMany(mappedBy = "event")
    public Set<Invite> getInvites() {
        return invites;
    }

    public void setInvites(Set<Invite> invites) {
        this.invites = invites;
    }

    @Transient
    public Set<User> getInvitedUsers() {
        return getInvites().stream()
                .filter(invite -> invite.getUser() != null)
                .map(Invite::getUser)
                .collect(Collectors.toSet());
    }

    @ManyToMany
    @JoinTable(name = "events_filters",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "filter_id"))
    public Set<Filter> getAllFilters() {
        return allFilters;
    }

    public void setAllFilters(Set<Filter> allFilters) {
        this.allFilters = allFilters;
    }

    public void addFilter(Filter filter) {
        allFilters.add(filter);
        filter.attachEvent(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (id != null ? !id.equals(event.id) : event.id != null) return false;
        if (beginDate != null ? !beginDate.equals(event.beginDate) : event.beginDate != null) return false;
        if (endDate != null ? !endDate.equals(event.endDate) : event.endDate != null) return false;
        if (lastDate != null ? !lastDate.equals(event.lastDate) : event.lastDate != null) return false;
        if (isPeriodic != null ? !isPeriodic.equals(event.isPeriodic) : event.isPeriodic != null) return false;
        if (period != null ? !period.equals(event.period) : event.period != null) return false;
        if (frequency != event.frequency) return false;
        if (name != null ? !name.equals(event.name) : event.name != null) return false;
        if (description != null ? !description.equals(event.description) : event.description != null) return false;
        if (owner != null ? !owner.equals(event.owner) : event.owner != null) return false;
        if (invites != null ? !invites.equals(event.invites) : event.invites != null) return false;
        return allFilters != null ? allFilters.equals(event.allFilters) : event.allFilters == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (beginDate != null ? beginDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (lastDate != null ? lastDate.hashCode() : 0);
        result = 31 * result + (isPeriodic != null ? isPeriodic.hashCode() : 0);
        result = 31 * result + (period != null ? period.hashCode() : 0);
        result = 31 * result + (frequency != null ? frequency.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (invites != null ? invites.hashCode() : 0);
        result = 31 * result + (allFilters != null ? allFilters.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", beginDate=" + beginDate +
                ", endDate=" + endDate +
                ", lastDate=" + lastDate +
                ", isPeriodic=" + isPeriodic +
                ", period=" + period +
                ", frequency=" + frequency +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", owner=" + owner +
                '}';
    }

    public enum EventFrequency {
        MINUTELY, HOURLY, DAILY, WEEKLY, MONTHLY, YEARLY
    }
}
