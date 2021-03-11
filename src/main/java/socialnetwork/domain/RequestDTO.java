package socialnetwork.domain;

import jdk.jshell.execution.Util;

import java.time.LocalDateTime;

public class RequestDTO {

    private Utilizator user1;
    private Utilizator user2;
    private LocalDateTime date;
    private String status;

    public RequestDTO(Utilizator user1, Utilizator user2, LocalDateTime date, String status) {
        this.user1 = user1;
        this.user2 = user2;
        this.date = date;
        this.status = status;
    }

    public Utilizator getUser1() {
        return user1;
    }

    public void setUser1(Utilizator user1) {
        this.user1 = user1;
    }

    public Utilizator getUser2() {
        return user2;
    }

    public void setUser2(Utilizator user2) {
        this.user2 = user2;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString(){
        return user1.getFirstName() + ' ' + user1.getLastName() + " | DATE: " + date + " | STATUS: " + status;
    }

}
