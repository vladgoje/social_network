package socialnetwork.repository.database;

import socialnetwork.domain.*;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class NotificationsDbRepository extends DbRepository<Long, Notification> {

    private static NotificationsDbRepository instance = null;

    private NotificationsDbRepository(){
        try {
            dbconn = DriverManager.getConnection(host, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static NotificationsDbRepository getInstance() {
        if(instance == null){
            instance = new NotificationsDbRepository();
            return instance;
        }
        return instance;
    }


    @Override
    public Optional<Notification> findOne(Long aLong) {
        String notification_id = String.valueOf(aLong);
        if(selectAction("SELECT * FROM notifications WHERE nid = " + notification_id)){
            try {
                if(getResults().next()) {
                    Long nid = this.getResults().getLong("nid");
                    Long eid = this.getResults().getLong("eid");
                    String message = this.getResults().getString("message");
                    LocalDateTime date = this.getResults().getTimestamp("date").toLocalDateTime();
                    Notification notification = new Notification(eid, message);
                    notification.setId(nid);
                    notification.setDate(date);
                    return Optional.of(notification);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Notification> findAll() {
        Set<Notification> notifications = new HashSet<>();
        if(selectAction("SELECT * FROM notifications")){
            try {
                if(getResults().next()) {
                    Long nid = this.getResults().getLong("nid");
                    Long eid = this.getResults().getLong("eid");
                    String message = this.getResults().getString("message");
                    LocalDateTime date = this.getResults().getTimestamp("date").toLocalDateTime();
                    Notification notification = new Notification(eid, message);
                    notification.setId(nid);
                    notification.setDate(date);
                    notifications.add(notification);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return notifications;
    }

    @Override
    public Optional<Notification> save(Notification entity) {
        if(entity.getId() == null){
            throw new IllegalArgumentException("ID nu poate fi null.");
        }

        if(selectAction("SELECT * FROM users_events WHERE eid = " + entity.getEventId())) {
            try {
                while (getResults().next()) {
                    long nr = getResults().getLong("notifications");
                    long uid = getResults().getLong("uid");
                    nr++;
                    updateAction("UPDATE users_events SET notifications = " + nr + " WHERE eid = " + entity.getEventId() + " AND uid = " + uid);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        String values = entity.getId().toString() + ",'" + entity.getEventId().toString() + "','" + entity.getMessage() + "','" + Timestamp.valueOf(entity.getDate()) + "'";
        if(updateAction("INSERT INTO notifications (nid, eid, message, date) VALUES (" + values + ")") == 1){
            return Optional.empty();
        } else {
            return Optional.of(entity);
        }
    }

    @Override
    public Optional<Notification> delete(Long aLong) {
        String nid = String.valueOf(aLong);
        if(exists(aLong)) {
            Optional<Notification> deleted = findOne(aLong);
            updateAction("DELETE FROM notifications WHERE nid = '" + nid + "'");
            return deleted;
        }
        return Optional.empty();
    }

    @Override
    public Optional<Notification> update(Notification entity) {
        if(entity.getId() == null){
            throw new IllegalArgumentException("ID nu poate fi null.");
        }
        String nid = entity.getId().toString();
        if(exists(entity.getId())) {
            updateAction("UPDATE notifications SET eid = '" + entity.getEventId() + "', message = '" + entity.getMessage() + "' WHERE nid = '" + nid + "'");
            return Optional.empty();
        }
        return Optional.of(entity);
    }

    @Override
    public int count() {
        selectAction("SELECT COUNT(*) FROM notifications");
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

    public boolean addToNotify(Long eid, Long uid){
        String values = eid.toString() + ",'" + uid.toString() + "', 0";
        return updateAction("INSERT INTO users_events (eid, uid, notifications) VALUES (" + values + ")") == 1;
    }

    public boolean removeToNotify(Long eid, Long uid){
        return updateAction("DELETE FROM users_events WHERE eid = " + eid.toString() + " AND uid = " + uid.toString()) == 1;
    }

    public boolean existToNotify(Long eid, Long uid){
        int count = 0;
        if(selectAction("SELECT * FROM users_events WHERE eid = " + eid.toString() + " AND uid = " + uid.toString())){
            try {
                while(getResults().next()) {
                    count++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return count == 1;
    }

    public Iterable<Notification> getUserNotifications(Long uid){
        List<Long> eids = new ArrayList<>();
        List<Notification> notifications = new ArrayList<>();
        if(selectAction("SELECT eid FROM users_events WHERE uid = " + uid.toString())){
            try {
                while(getResults().next()) {
                    eids.add(this.getResults().getLong("eid"));
                }

                for(Long eid : eids) {
                    if (selectAction("SELECT * FROM notifications WHERE eid = " + eid.toString())) {
                        try {
                            while (getResults().next()) {
                                Long nid = this.getResults().getLong("nid");
                                String message = this.getResults().getString("message");
                                LocalDateTime date = this.getResults().getTimestamp("date").toLocalDateTime();
                                Notification notification = new Notification(eid, message);
                                notification.setId(nid);
                                notification.setDate(date);
                                notifications.add(notification);
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
        return notifications;
    }

    public int userNewNotifications(Long uid){
        int count = 0;
        if(selectAction("SELECT notifications FROM users_events WHERE uid = " + uid.toString())){
            try {
                while(getResults().next()) {
                    count += this.getResults().getInt("notifications");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    public void readNotifications(Long uid){
        updateAction("UPDATE users_events SET notifications = 0 WHERE uid = " + uid.toString());
    }


}
