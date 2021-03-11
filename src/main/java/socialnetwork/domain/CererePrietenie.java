package socialnetwork.domain;

import java.time.LocalDateTime;

public class CererePrietenie extends Entity<Tuple<Long,Long>> {

    private Utilizator user1;
    private Utilizator user2;

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

    private String status;
    private LocalDateTime date;


    public CererePrietenie(){
        this.status = "pending";
        this.date = LocalDateTime.now();
    }

    public Long getFrom(){
        return super.getId().getLeft();
    }
    public Long getTo(){
        return super.getId().getRight();
    }
    public String getStatus(){
        return status;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public void setDate(LocalDateTime date){
        this.date = date;
    }

    public LocalDateTime getDate() { return date; }
}