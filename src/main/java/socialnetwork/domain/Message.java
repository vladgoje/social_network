package socialnetwork.domain;

import socialnetwork.utils.Constants;

import java.time.LocalDateTime;
import java.util.List;

public class Message extends Entity<Long>{
    private Utilizator from;
    private List<Utilizator> to;
    private String message;
    private LocalDateTime date;

    public Message(Utilizator from, List<Utilizator> to, String message, LocalDateTime date) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.date = date;
    }

    public Utilizator getFrom() {
        return from;
    }

    public void setFrom(Utilizator from) {
        this.from = from;
    }

    public List<Utilizator> getTo() {
        return to;
    }

    public void setTo(List<Utilizator> to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "(From: " + from.getFirstName() + " " + from.getLastName() + " | " + date.format(Constants.DATE_TIME_FORMATTER) + ") " + message;
    }
}
