package socialnetwork.domain;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class Conversation {
    private ArrayList<Utilizator> users;
    private ArrayList<Message> messages;

    public ArrayList<Utilizator> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<Utilizator> users) {
        this.users = users;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public Conversation(ArrayList<Utilizator> users, ArrayList<Message> messages) {
        this.users = users;
        this.messages = messages;
    }

    @Override
    public String toString(){
        String string = "";
        for(Utilizator user : users){
            string = string + user.getFirstName() + " " + user.getLastName();
        }
        return string;
    }
}
