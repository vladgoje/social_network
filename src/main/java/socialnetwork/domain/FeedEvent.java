package socialnetwork.domain;

import java.time.LocalDateTime;

public class FeedEvent extends Entity<Long> implements Comparable<FeedEvent>{


    private String title;
    private Utilizator creator;
    private LocalDateTime date;
    private LocalDateTime startDate;
    private String description;

    public FeedEvent(String title, Utilizator creator, LocalDateTime startDate, String description) {
        this.title = title;
        this.creator = creator;
        this.startDate = startDate;
        this.description = description;
        this.date = LocalDateTime.now();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Utilizator getCreator() {
        return creator;
    }

    public void setCreator(Utilizator creator) {
        this.creator = creator;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "FeedEvent{" +
                "title='" + title + '\'' +
                ", creator=" + creator +
                ", date=" + date +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public int compareTo(FeedEvent o) {
        return this.getDate().compareTo(o.getDate());
    }
}
