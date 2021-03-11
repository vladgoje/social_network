package socialnetwork.domain;

import socialnetwork.utils.Constants;

import java.time.LocalDateTime;


public class Prietenie extends Entity<Tuple<Long,Long>> {

    LocalDateTime date;

    public Prietenie() {
        this.date = LocalDateTime.now();
    }

    public Prietenie(LocalDateTime date){
        this.date = date;
    }

    /**
     *
     * @return the date when the friendship was created
     */
    public LocalDateTime getDate() { return date; }

    public String toString(){
        return this.getId().getLeft() + " - " +
                this.getId().getRight() + " (date: " +
                this.getDate().format(Constants.DATE_TIME_FORMATTER) + ")";
    }
}
