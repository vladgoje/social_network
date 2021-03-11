package socialnetwork.repository.database;

import socialnetwork.domain.CererePrietenie;
import socialnetwork.domain.Tuple;
import socialnetwork.utils.Constants;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


public class FriendshipRequestDbRepository extends DbRepository<Tuple<Long, Long>, CererePrietenie> {
    private static FriendshipRequestDbRepository instance = null;

    private FriendshipRequestDbRepository(){
        try {
            dbconn = DriverManager.getConnection(host, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static FriendshipRequestDbRepository getInstance() {
        if(instance == null){
            instance = new FriendshipRequestDbRepository();
            return instance;
        }
        return instance;
    }

    @Override
    public Optional<CererePrietenie> findOne(Tuple<Long, Long> tuple) {
        String uid1 = "'" + tuple.getLeft() + "'";
        String uid2 = "'" + tuple.getRight() + "'";
        if(selectAction("SELECT * FROM friendship_requests WHERE uid1 = " + uid1 + " AND uid2 = " + uid2)){
            try {
                if(getResults().next()) {
                    Long left = this.getResults().getLong("uid1");
                    Long right = this.getResults().getLong("uid2");
                    String status = this.getResults().getString("status");
                    String date = this.getResults().getString("date");
                    date = date.split("\\.")[0];
                    CererePrietenie CererePrietenie = new CererePrietenie();
                    CererePrietenie.setId(new Tuple(left, right));
                    CererePrietenie.setStatus(status);
                    CererePrietenie.setDate(LocalDateTime.parse(date, Constants.DATE_TIME_FORMATTER));
                    return Optional.of(CererePrietenie);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }


    @Override
    public Iterable<CererePrietenie> findAll(){
        Set<CererePrietenie> friendshipRequests = new HashSet<>();
        if(selectAction("SELECT * FROM friendship_requests")) {
            ResultSet resultSet = this.getResults();
            try {
                while (resultSet.next()) {
                    Long left = this.getResults().getLong("uid1");
                    Long right = this.getResults().getLong("uid2");
                    String status = this.getResults().getString("status");
                    String date = this.getResults().getString("date");
                    date = date.split("\\.")[0];
                    CererePrietenie CererePrietenie = new CererePrietenie();
                    CererePrietenie.setId(new Tuple(left, right));
                    CererePrietenie.setStatus(status);
                    CererePrietenie.setDate(LocalDateTime.parse(date, Constants.DATE_TIME_FORMATTER));
                    friendshipRequests.add(CererePrietenie);
                }
                return friendshipRequests;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return friendshipRequests;
    }

    @Override
    public Optional<CererePrietenie> save(CererePrietenie entity) {
        if(entity.getId() == null) {
            throw new IllegalArgumentException("ID nu poate fi null.");
        }
        String values = entity.getId().getLeft() + ",'" + entity.getId().getRight() + "','" + entity.getStatus() + "','" + entity.getDate() + "'";
        if(updateAction("INSERT INTO friendship_requests (uid1, uid2, status, date) VALUES (" + values + ")") == 1){
            return Optional.empty();
        } else {
            return Optional.of(entity);
        }
    }

    @Override
    public Optional<CererePrietenie> delete(Tuple<Long, Long> tuple) {
        String uid1 = "'" + tuple.getLeft().toString() + "'";
        String uid2 = "'" + tuple.getRight().toString() + "'";
        if(exists(tuple)) {
            Optional<CererePrietenie> deleted = findOne(tuple);
            updateAction("DELETE FROM friendship_requests WHERE uid1 = " + uid1 + " AND uid2 = " + uid2);
            return deleted;
        }
        return Optional.empty();
    }

    @Override
    public Optional<CererePrietenie> update(CererePrietenie entity) {
        String uid1 = "'" + entity.getFrom().toString() + "'";
        String uid2 = "'" + entity.getTo().toString() + "'";
        String status = "'" + entity.getStatus() + "'";
        if (updateAction("UPDATE friendship_requests SET status = " + status + "WHERE uid1 = " + uid1 + "AND uid2 = " + uid2) == 1) {
            return Optional.empty();
        } else{
            return Optional.of(entity);
        }
    }

        @Override
    public int count() {
        selectAction("SELECT COUNT(*) FROM friendship_requests");
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
}
