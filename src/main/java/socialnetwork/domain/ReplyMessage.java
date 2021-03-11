package socialnetwork.domain;

import socialnetwork.domain.validators.Validator;
import socialnetwork.repository.database.MessageDbRepository;
import socialnetwork.repository.database.UtilizatorDbRepository;

import java.time.LocalDateTime;
import java.util.List;

public class ReplyMessage extends Message {
    private Message message;

    public ReplyMessage(Utilizator from, List<Utilizator> to, String message, LocalDateTime date, Message message1) {
        super(from, to, message, date);
        this.message = message1;
    }


}
