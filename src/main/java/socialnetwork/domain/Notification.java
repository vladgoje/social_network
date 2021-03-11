package socialnetwork.domain;

import socialnetwork.utils.Constants;

import java.time.LocalDateTime;

public class Notification extends Entity<Long> implements Comparable<Notification>{
    private Long eventId;
    private String message;
    private LocalDateTime date;

    public Notification(Long eventId, String message) {
        this.message = message;
        this.eventId = eventId;
        this.date = LocalDateTime.now();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getDate() { return date; }

    public void setDate(LocalDateTime date) { this.date = date; }

    @Override
    public String toString(){ return "(" + date.format(Constants.DATE_TIME_FORMATTER) + ") " + message; }

    @Override
    public int compareTo(Notification o) {
        return this.getDate().compareTo(o.getDate());
    }

}
