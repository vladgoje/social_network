package socialnetwork.repository.database;

import socialnetwork.domain.FriendshipDTO;
import socialnetwork.domain.Prietenie;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.Validator;
import socialnetwork.utils.Constants;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FriendshipDbRepository extends DbRepository<Tuple<Long, Long>, Prietenie> {
    private static FriendshipDbRepository instance = null;

    private final Validator<Prietenie> validator;

    private FriendshipDbRepository(Validator<Prietenie> validator){
        try {
            dbconn = DriverManager.getConnection(host, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.validator = validator;
    }

    public static FriendshipDbRepository getInstance(Validator<Prietenie> validator) {
        if(instance == null){
            instance = new FriendshipDbRepository(validator);
            return instance;
        }
        return instance;
    }

    @Override
    public Optional<Prietenie> findOne(Tuple<Long, Long> tuple) {
        String uid1 = "'" + tuple.getLeft() + "'";
        String uid2 = "'" + tuple.getRight() + "'";
        if(selectAction("SELECT * FROM friendships WHERE uid1 = " + uid1 + " AND uid2 = " + uid2)){
            try {
                if(getResults().next()) {
                    Long left = this.getResults().getLong("uid1");
                    Long right = this.getResults().getLong("uid2");
                    String date = this.getResults().getString("date");
                    date = date.split("\\.")[0];
                    Prietenie Prietenie = new Prietenie(LocalDateTime.parse(date, Constants.DATE_TIME_FORMATTER));
                    Prietenie.setId(new Tuple(left, right));
                    return Optional.of(Prietenie);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }


    @Override
    public Iterable<Prietenie> findAll(){
        Set<Prietenie> friendships = new HashSet<>();
        if(selectAction("SELECT * FROM friendships")) {
            ResultSet resultSet = this.getResults();
            try {
                while (resultSet.next()) {
                    Long left = this.getResults().getLong("uid1");
                    Long right = this.getResults().getLong("uid2");
                    String date = this.getResults().getString("date");
                    date = date.split("\\.")[0];
                    Prietenie Prietenie = new Prietenie(LocalDateTime.parse(date, Constants.DATE_TIME_FORMATTER));
                    Prietenie.setId(new Tuple(left, right));
                    friendships.add(Prietenie);
                }
                return friendships;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return friendships;
    }

    @Override
    public Optional<Prietenie> save(Prietenie entity) {
        if(entity.getId() == null){
            throw new IllegalArgumentException("ID nu poate fi null.");
        }
        validator.validate(entity);
        String values = entity.getId().getLeft() + ",'" + entity.getId().getRight() + "','" + entity.getDate() + "'";
        if(updateAction("INSERT INTO friendships (uid1, uid2, date) VALUES (" + values + ")") == 1){
            return Optional.empty();
        } else {
            return Optional.of(entity);
        }
    }

    @Override
    public Optional<Prietenie> delete(Tuple<Long, Long> tuple) {
        String uid1 = "'" + tuple.getLeft().toString() + "'";
        String uid2 = "'" + tuple.getRight().toString() + "'";
        if(exists(tuple)) {
            Optional<Prietenie> deleted = findOne(tuple);
            updateAction("DELETE FROM friendships WHERE uid1 = " + uid1 + " AND uid2 = " + uid2);
            return deleted;
        }
        return Optional.empty();
    }

    @Override
    public Optional<Prietenie> update(Prietenie entity) {
        return Optional.empty();
    }

    @Override
    public int count() {
        selectAction("SELECT COUNT(*) FROM friendships");
        try {
            this.getResults().next();
            return this.getResults().getInt(1);
        } catch(SQLException e){
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean exists(Tuple<Long, Long> id){
        return findOne(id).isPresent();
    }

    public Iterable<Prietenie> findUserFriendships(Long uid){
        Set<Prietenie> friendships = new HashSet<>();
        if(selectAction("SELECT * FROM friendships WHERE uid1 = " + uid.toString() + " OR uid2 = " + uid.toString())) {
            ResultSet resultSet = this.getResults();
            try {
                while (resultSet.next()) {
                    Long left = this.getResults().getLong("uid1");
                    Long right = this.getResults().getLong("uid2");
                    String date = this.getResults().getString("date");
                    date = date.split("\\.")[0];
                    Prietenie Prietenie = new Prietenie(LocalDateTime.parse(date, Constants.DATE_TIME_FORMATTER));
                    Prietenie.setId(new Tuple<>(left, right));
                    friendships.add(Prietenie);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return friendships;
    }

}
