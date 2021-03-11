package socialnetwork.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Utilizator extends Entity<Long>{

    private List<Utilizator> friends = new ArrayList<>();
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String salt;
    private LocalDateTime last_login;

    public Utilizator(String firstName, String lastName, String username, String password, String salt) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.salt = salt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Utilizator> getFriends() {
        return friends;
    }

    public LocalDateTime getLastLogin() {
        return last_login;
    }

    public void setLastLogin(LocalDateTime last_login) {
        this.last_login = last_login;
    }


    public void addFriend(Utilizator user){ friends.add(user); }
    public void removeFriend(Utilizator user) { friends.remove(user); }
    public boolean hasFriend(Optional<Utilizator> user){
        return friends.contains(user);
    }

    @Override
    public String toString() {
        return firstName + ' ' + lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utilizator)) return false;
        Utilizator that = (Utilizator) o;
        return getFirstName().equals(that.getFirstName()) &&
                getLastName().equals(that.getLastName()) &&
                getFriends().equals(that.getFriends());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirstName(), getLastName(), getFriends());
    }


}