package socialnetwork.domain;

import socialnetwork.utils.Constants;

import java.time.LocalDateTime;

public class FriendshipDTO {
    private String fname;
    private String lname;
    LocalDateTime date;

    public FriendshipDTO(String fname, String lname, LocalDateTime date) {
        this.fname = fname;
        this.lname = lname;
        this.date = date;
    }

    public String toString(){
        return this.getFname() + " | " + this.getLname() + " | " +
                this.getDate().format(Constants.DATE_TIME_FORMATTER);
    }

    public String getFname() {
        return fname;
    }

    public String getLname() {
        return lname;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
