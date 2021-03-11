package socialnetwork.repository.database;

import socialnetwork.domain.FeedEvent;
import socialnetwork.domain.Prietenie;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.Validator;
import socialnetwork.utils.Constants;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalField;
import java.util.*;
import java.util.stream.Collectors;

public class EventsRepository extends DbRepository<Long, FeedEvent> {

    private static EventsRepository instance = null;
    private Validator<FeedEvent> validator;

    private EventsRepository(Validator<FeedEvent> validator){
        try {
            dbconn = DriverManager.getConnection(host, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.validator = validator;
    }

    public static EventsRepository getInstance(Validator<FeedEvent> validator) {
        if(instance == null){
            instance = new EventsRepository(validator);
            return instance;
        }
        return instance;
    }

    @Override
    public Optional<FeedEvent> findOne(Long aLong) {
        String event_id = String.valueOf(aLong);
        if(selectAction("SELECT * FROM events WHERE eid = '" + event_id + "'")){
            try {
                if(getResults().next()) {
                    Long eid = this.getResults().getLong("eid");
                    Long uid = this.getResults().getLong("uid");
                    String title = this.getResults().getString("title");
                    String description = this.getResults().getString("description");
                    String date = this.getResults().getString("date");
                    String startDate = this.getResults().getString("startDate");
                    date = date.split("\\.")[0];
                    startDate = startDate.split("\\.")[0];
                    if (selectAction("SELECT * FROM users WHERE uid = '" + uid + "'")) {
                        try {
                            if(getResults().next()) {
                                Long id = this.getResults().getLong("uid");
                                String fname = this.getResults().getString("fname");
                                String lname = this.getResults().getString("lname");
                                String username = this.getResults().getString("username");
                                String password = this.getResults().getString("password");
                                String salt = this.getResults().getString("salt");
                                Utilizator utilizator = new Utilizator(fname, lname, username, password, salt);
                                utilizator.setId(id);
                                FeedEvent event = new FeedEvent(title, utilizator, LocalDateTime.parse(startDate, Constants.DATE_TIME_FORMATTER), description);
                                event.setDate(LocalDateTime.parse(date, Constants.DATE_TIME_FORMATTER));
                                event.setId(eid);
                                return Optional.of(event);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    @Override
    public Iterable<FeedEvent> findAll() {
        Set<FeedEvent> events = new HashSet<>();
        if(selectAction("SELECT * FROM events ORDER BY date DESC")) {
            ResultSet resultSet = this.getResults();
            try {
                while (resultSet.next()) {
                    Long id = resultSet.getLong("eid");
                    Optional<FeedEvent> event = findOne(id);
                    events.add(event.get());
                }
                return events;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return events;
    }

    private FeedEvent createEventFromResultSet(ResultSet resultSet) {
        try {
            Long eid = resultSet.getLong("eid");
            Long uid = resultSet.getLong("uid");
            String title = resultSet.getString("title");
            String description = resultSet.getString("description");
            String date = resultSet.getString("date");
            String startDate = resultSet.getString("startDate");
            date = date.split("\\.")[0];
            startDate = startDate.split("\\.")[0];
            if (selectAction("SELECT * FROM users WHERE uid = '" + uid + "'")) {
                try {
                    if (getResults().next()) {
                        Long id = this.getResults().getLong("uid");
                        String fname = this.getResults().getString("fname");
                        String lname = this.getResults().getString("lname");
                        String username = this.getResults().getString("username");
                        String password = this.getResults().getString("password");
                        String salt = this.getResults().getString("salt");
                        Utilizator utilizator = new Utilizator(fname, lname, username, password, salt);
                        utilizator.setId(id);
                        FeedEvent event = new FeedEvent(title, utilizator, LocalDateTime.parse(startDate, Constants.DATE_TIME_FORMATTER), description);
                        event.setDate(LocalDateTime.parse(date, Constants.DATE_TIME_FORMATTER));
                        event.setId(eid);
                        return event;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Iterable<FeedEvent> getEventsPage(Utilizator user){
        Set<FeedEvent> events = new HashSet<>();
        for(Utilizator usr : user.getFriends()) {
            if (selectAction("SELECT * FROM events WHERE uid = " + usr.getId().toString())) {
                try {
                    ResultSet resultSet = this.getResults();
                    while (resultSet.next()) {
                        events.add(createEventFromResultSet(resultSet));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        if (selectAction("SELECT * FROM events WHERE uid = " + user.getId().toString())) {
            try {
                ResultSet resultSet = this.getResults();
                while (resultSet.next()) {
                    events.add(createEventFromResultSet(resultSet));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return events;
    }


    @Override
    public Optional<FeedEvent> save(FeedEvent entity) {
        if(entity.getId() == null){
            throw new IllegalArgumentException("ID nu poate fi null.");
        }
        validator.validate(entity);
        String values = entity.getId().toString() + ",'" + entity.getCreator().getId().toString() + "','" + entity.getTitle() + "','" + entity.getDescription() + "','" + entity.getDate() + "','" + entity.getStartDate() + "'";
        if(updateAction("INSERT INTO events (eid, uid, title, description, date, startDate) VALUES (" + values + ")") == 1){
            return Optional.empty();
        } else {
            return Optional.of(entity);
        }
    }

    @Override
    public Optional<FeedEvent> delete(Long aLong) {
        String eid = String.valueOf(aLong);
        if(exists(aLong)) {
            Optional<FeedEvent> deleted = findOne(aLong);
            updateAction("DELETE FROM events WHERE eid = '" + eid + "'");
            return deleted;
        }
        return Optional.empty();
    }

    @Override
    public Optional<FeedEvent> update(FeedEvent entity) {
        if(entity.getId() == null){
            throw new IllegalArgumentException("ID nu poate fi null.");
        }
        validator.validate(entity);
        String eid = entity.getId().toString();
        if(exists(entity.getId())) {
            updateAction("UPDATE events SET title = '" + entity.getTitle() + "', description = '" + entity.getDescription() + "', startDate = '" + entity.getStartDate() + "' WHERE eid = '" + eid + "'");
            return Optional.empty();
        }
        return Optional.of(entity);
    }

    @Override
    public int count() {
        selectAction("SELECT COUNT(*) FROM events");
        try {
            this.getResults().next();
            return this.getResults().getInt(1);
        } catch(SQLException e){
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean exists(Long aLong) {
        return findOne(aLong).isPresent();
    }

}
