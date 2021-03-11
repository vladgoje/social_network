package socialnetwork.repository.database;

import socialnetwork.domain.Message;
import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.MessageValidator;
import socialnetwork.domain.validators.Validator;
import socialnetwork.repository.Repository;
import socialnetwork.utils.Constants;

import java.awt.image.AreaAveragingScaleFilter;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MessageDbRepository extends DbRepository<Long, Message> {

    private static MessageDbRepository instance = null;
    private Validator<Message> validator;
    private Repository<Long, Utilizator> repo;

    private MessageDbRepository(Validator<Message> validator, Repository<Long, Utilizator> repo){
        try {
            this.dbconn = DriverManager.getConnection(host, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.validator = validator;
        this.repo = repo;
    }

    public static MessageDbRepository getInstance(Validator<Message> validator, Repository<Long, Utilizator> repo) {
        if(instance == null){
            instance = new MessageDbRepository(validator, repo);
            return instance;
        }
        return instance;
    }


    @Override
    public Optional<Message> findOne(Long aLong) {
        String mid = String.valueOf(aLong);
        Long from = null, id = (long) -1;
        List<Long> to = new ArrayList<>();
        String text = null;
        LocalDateTime date = null;

        if(selectAction("SELECT * FROM messages WHERE mid = '" + mid + "'")){
            try {
                if(getResults().next()) {
                    id = this.getResults().getLong("mid");
                    text = this.getResults().getString("text");
                    date = LocalDateTime.parse(this.getResults().getString("date").split("\\.")[0],
                            Constants.DATE_TIME_FORMATTER);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if(selectAction("SELECT * FROM users_messages WHERE mid = '" + mid + "'")){
            try {
                while(getResults().next()) {
                    from = this.getResults().getLong("uid1");
                    to.add(this.getResults().getLong("uid2"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if(id != -1){
            List<Utilizator> users = to.stream().map(u -> {
                return repo.findOne(u).get();
            }).collect(Collectors.toList());
            Message message = new Message(repo.findOne(from).get(), users, text, date);
            return Optional.of(message);
        }

        return Optional.empty();
    }

    @Override
    public Iterable<Message> findAll() {
        Set<Message> messages = new HashSet<>();
        List<Long> ids = new ArrayList<>();
        if(selectAction("SELECT * FROM messages")){

            try {
                while(getResults().next()) {
                    ids.add(this.getResults().getLong("mid"));
                }
                ids.forEach(mid -> {
                    messages.add(findOne(mid).get());
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    @Override
    public Optional<Message> save(Message entity) {
        if(entity.getId() == null){
            throw new IllegalArgumentException("ID nu poate fi null.");
        }
        validator.validate(entity);
        String values = entity.getId().toString() + ",'" + entity.getMessage() + "','" + entity.getDate() + "'";
        if(updateAction("INSERT INTO messages (mid, text, date) VALUES (" + values + ")") == 1){
            entity.getTo().forEach(u -> {
                String values2 = entity.getId().toString() + ",'" + entity.getFrom().getId().toString() + "','" + u.getId().toString() + "'";
                updateAction("INSERT INTO users_messages (mid, uid1, uid2) VALUES (" + values2 + ")");
            });
            return Optional.empty();
        } else {
            return Optional.of(entity);
        }
    }

    @Override
    public Optional<Message> delete(Long aLong) {
        String mid = String.valueOf(aLong);
        if(exists(aLong)) {
            Optional<Message> deleted = findOne(aLong);
            updateAction("DELETE FROM messages WHERE mid = '" + mid + "'");
            return deleted;
        }
        return Optional.empty();
    }

    @Override
    public Optional<Message> update(Message entity) {
        return Optional.empty();
    }

    @Override
    public int count() {
        selectAction("SELECT COUNT(*) FROM messages");
        try {
            this.getResults().next();
            return this.getResults().getInt(1);
        } catch(SQLException e){
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean exists(Long id){
        return findOne(id).isPresent();
    }

}
