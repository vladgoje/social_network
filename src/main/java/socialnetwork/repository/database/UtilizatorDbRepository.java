package socialnetwork.repository.database;

import jdk.jshell.execution.Util;
import socialnetwork.domain.Prietenie;

import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.FriendshipValidator;
import socialnetwork.domain.validators.Validator;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class UtilizatorDbRepository extends DbRepository<Long, Utilizator> {

    private static UtilizatorDbRepository instance = null;
    private final Validator<Utilizator> validator;
    private final Validator<Prietenie> friendshipValidator = new FriendshipValidator();
    private final FriendshipDbRepository friendshipRepo = FriendshipDbRepository.getInstance(friendshipValidator);

    private UtilizatorDbRepository(Validator<Utilizator> validator){
        try {
            dbconn = DriverManager.getConnection(host, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.validator = validator;
    }

    public static UtilizatorDbRepository getInstance(Validator<Utilizator> validator) {
        if(instance == null){
             instance = new UtilizatorDbRepository(validator);
             return instance;
        }
        return instance;
    }

    public Optional<Utilizator> findByUsername(String uname) {
        if(selectAction("SELECT * FROM users WHERE username = '" + uname + "'")){
            Utilizator user = createUserFromSql();
            if(user == null){
                return Optional.empty();
            }
            Iterable<Prietenie> userFriendships = friendshipRepo.findUserFriendships(user.getId());
            userFriendships.forEach(x -> {
                if(x.getId().getLeft().equals(user.getId())) {
                    if (selectAction("SELECT * FROM users WHERE uid = '" + x.getId().getRight().toString() + "'")) {
                        Utilizator friend = createUserFromSql();
                        user.addFriend(friend);
                    }
                } else{
                    if (selectAction("SELECT * FROM users WHERE uid = '" + x.getId().getLeft().toString() + "'")) {
                        Utilizator friend = createUserFromSql();
                        user.addFriend(friend);
                    }
                }
            });
            return Optional.of(user);
        }
        return Optional.empty();
    }

    private Utilizator createUserFromSql(){
        try {
            if(getResults().next()) {
                Long id = this.getResults().getLong("uid");
                String fname = this.getResults().getString("fname");
                String lname = this.getResults().getString("lname");
                String username = this.getResults().getString("username");
                String password = this.getResults().getString("password");
                String salt = this.getResults().getString("salt");
                LocalDateTime last_login = this.getResults().getTimestamp("last_login").toLocalDateTime();
                Utilizator utilizator = new Utilizator(fname, lname, username, password, salt);
                utilizator.setId(id);
                utilizator.setLastLogin(last_login);
                return utilizator;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Optional<Utilizator> findOne(Long aLong) {
        String uid = String.valueOf(aLong);
        if(selectAction("SELECT * FROM users WHERE uid = '" + uid + "'")) {
            Utilizator user = createUserFromSql();
            if(user != null){
                Iterable<Prietenie> userFriendships = friendshipRepo.findUserFriendships(user.getId());
                userFriendships.forEach(x -> {
                    if (x.getId().getLeft().equals(user.getId())) {
                        if (selectAction("SELECT * FROM users WHERE uid = '" + x.getId().getRight().toString() + "'")) {
                            Utilizator friend = createUserFromSql();
                            user.addFriend(friend);
                        }
                    } else {
                        if (selectAction("SELECT * FROM users WHERE uid = '" + x.getId().getLeft().toString() + "'")) {
                            Utilizator friend = createUserFromSql();
                            user.addFriend(friend);
                        }
                    }
                });
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }


    @Override
    public Iterable<Utilizator> findAll(){
        Set<Utilizator> users = new HashSet<>();
        if(selectAction("SELECT * FROM users")) {
            ResultSet resultSet = this.getResults();
            try {
                while (resultSet.next()) {
                    Long id = resultSet.getLong("uid");
                    Optional<Utilizator> user = findOne(id);
                    users.add(user.get());
                }
                return users;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return users;
    }

    @Override
    public Optional<Utilizator> save(Utilizator entity) {
        if(entity.getId() == null){
            throw new IllegalArgumentException("ID nu poate fi null.");
        }
        validator.validate(entity);
        String values = entity.getId().toString() + ",'" + entity.getFirstName() + "','" + entity.getLastName() + "','" + entity.getUsername() + "','" + entity.getPassword() + "','" + entity.getSalt() + "','" + entity.getLastLogin() + "'";
        if(updateAction("INSERT INTO users (uid, fname, lname, username, password, salt, last_login) VALUES (" + values + ")") == 1){
            return Optional.empty();
        } else {
            return Optional.of(entity);
        }
    }

    @Override
    public Optional<Utilizator> delete(Long aLong) {
        String uid = String.valueOf(aLong);
        if(exists(aLong)) {
            Optional<Utilizator> deleted = findOne(aLong);
            updateAction("DELETE FROM users WHERE uid = '" + uid + "'");
            return deleted;
        }
        return Optional.empty();
    }

    @Override
    public Optional<Utilizator> update(Utilizator entity) {
        if(entity.getId() == null){
            throw new IllegalArgumentException("ID nu poate fi null.");
        }
        validator.validate(entity);
        if(updateAction("UPDATE users SET fname = '" + entity.getFirstName() + "', lname = '" + entity.getLastName() + "', last_login = '" + entity.getLastLogin().toString() + "' WHERE uid = " + entity.getId().toString()) == 1){
            return Optional.empty();
        } else {
            return Optional.of(entity);
        }
    }

    @Override
    public int count() {
        selectAction("SELECT COUNT(*) FROM users");
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
