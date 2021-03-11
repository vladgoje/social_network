package socialnetwork.service;

import socialnetwork.domain.*;
import socialnetwork.repository.Repository;
import socialnetwork.repository.database.EventsRepository;
import socialnetwork.repository.database.NotificationsDbRepository;
import socialnetwork.repository.database.UtilizatorDbRepository;
import socialnetwork.repository.exceptions.RepositoryException;
import socialnetwork.service.validator.ServiceValidator;
import socialnetwork.utils.Constants;
import socialnetwork.utils.observer.Observable;
import socialnetwork.utils.observer.Observer;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UtilizatorService implements Observable {
    private final Repository<Long, Utilizator> repo;
    private final Repository<Tuple<Long, Long>, Prietenie> frepo;
    private final Repository<Long, Message> mrepo;
    private final Repository<Tuple<Long, Long>, CererePrietenie> requestRepo;
    private final EventsRepository eventsRepo;
    private final NotificationsDbRepository notificationRepo;
    private final ServiceValidator val = new ServiceValidator();

    private List<Observer> observers=new ArrayList<>();

    public UtilizatorService(Repository<Long, Utilizator> repo,
                             Repository<Tuple<Long, Long>, Prietenie> frepo,
                             Repository<Long, Message> mrepo,
                             Repository<Tuple<Long, Long>, CererePrietenie> requestRepo,
                             Repository<Long, FeedEvent> eventsRepo,
                             Repository<Long, Notification> notificationRepo) {
        this.repo = repo;
        this.frepo = frepo;
        this.mrepo = mrepo;
        this.requestRepo = requestRepo;
        this.eventsRepo = (EventsRepository) eventsRepo;
        this.notificationRepo = (NotificationsDbRepository) notificationRepo;
    }

    @Override
    public void addObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        observers.forEach(Observer::update);
    }

    private Long generateUserId(){
        for(int i = 1; i <= repo.count(); i++){
            if(repo.findOne((long) i).isEmpty())
                return (long) i;
        }
        return (long) repo.count() + 1;
    }

    private Long generateMessageId(){
        for(int i = 1; i <= mrepo.count(); i++){
            if(mrepo.findOne((long) i).isEmpty())
                return (long) i;
        }
        return (long) mrepo.count() + 1;
    }

    private Long generateEventId(){
        for(int i = 1; i <= eventsRepo.count(); i++){
            if(eventsRepo.findOne((long) i).isEmpty())
                return (long) i;
        }
        return (long) eventsRepo.count() + 1;
    }

    private Long generateNotificationId(){
        for(int i = 1; i <= notificationRepo.count(); i++){
            if(notificationRepo.findOne((long) i).isEmpty())
                return (long) i;
        }
        return (long) notificationRepo.count() + 1;
    }

    public Utilizator connectUser(String username, String password){
        Optional<Utilizator> user = ((UtilizatorDbRepository) repo).findByUsername(username);
        if(user.isPresent()){
            if(hash(password + user.get().getSalt()).equals(user.get().getPassword())){
                return user.get();
            }
        }
        return null;
    }


    public Iterable<Utilizator> searchByName(String name){
        Iterable<Utilizator> users = repo.findAll();
        List<Utilizator> filtered = new ArrayList<>();
        for (Utilizator u : users) {
            String username1 = u.getFirstName() + ' ' + u.getLastName();
            String username2 = u.getLastName() + ' ' + u.getFirstName();
            if (username1.contains(name) || username2.contains(name)) {
                filtered.add(u);
            }
        }
        return filtered;
    }

    public Iterable<Utilizator> getAllUsers(){
        Iterable<Utilizator> users = repo.findAll();
        Iterable<Prietenie> friendships = frepo.findAll();
        for(Utilizator user : users){
            for(Prietenie fr : friendships){
                if(user.getId().equals(fr.getId().getLeft())){
                    user.addFriend(repo.findOne(fr.getId().getRight()).get());
                } else if (user.getId().equals(fr.getId().getRight())){
                    user.addFriend(repo.findOne(fr.getId().getLeft()).get());
                }
            }
        }
        return StreamSupport.stream(users.spliterator(), false).collect(Collectors.toList());
    }

    public Utilizator getUser(String id){
        val.validateLong(id);
        Optional<Utilizator> user = repo.findOne(Long.valueOf(id));
        return user.orElse(null);
    }

    public Iterable<Prietenie> getAllFriendships(){
        Iterable<Prietenie> friendships = frepo.findAll();
        return StreamSupport.stream(friendships.spliterator(), false).collect(Collectors.toList());
    }

    public Utilizator addUtilizator(String fname, String lname, String username, String password) {
        String salt = hash(LocalDateTime.now().format(Constants.DATE_TIME_FORMATTER));
        password = hash(password + salt);
        Utilizator user = new Utilizator(fname, lname, username, password, salt);
        user.setId(generateUserId());
        user.setLastLogin(LocalDateTime.now());
        Optional<Utilizator> added = repo.save(user);
        return added.orElse(null);
    }

    public Utilizator updateUser(Utilizator user){
        Optional<Utilizator> updated = repo.update(user);
        notifyObservers();
        return updated.orElse(null);
    }

    public Utilizator deleteUtilizator(String id){
        val.validateLong(id);
        Optional<Utilizator> deleted = repo.delete(Long.valueOf(id));
        return deleted.orElse(null);
    }

    public Prietenie addFriendship(String id1, String id2) {
        val.validateLong(id1); val.validateLong(id2);
        Prietenie friendship = new Prietenie();
        friendship.setId(new Tuple<>(Long.parseLong(id1), Long.parseLong(id2)));
        Optional<Prietenie> fr = frepo.save(friendship);
        return fr.orElse(null);
    }

    public Prietenie removeFriendship(String id1, String id2) {
        val.validateLong(id1); val.validateLong(id2);
        Long p1 = Long.parseLong(id1); Long p2 = Long.parseLong(id2);

        Optional<Prietenie> fr = frepo.delete(new Tuple<>(Long.parseLong(id1), Long.parseLong(id2)));
        requestRepo.delete(new Tuple<>(Long.parseLong(id1), Long.parseLong(id2)));
        if(fr.isEmpty()) {
            fr = frepo.delete(new Tuple<>(Long.parseLong(id2), Long.parseLong(id1)));
            requestRepo.delete(new Tuple<>(Long.parseLong(id2), Long.parseLong(id1)));
        }

        notifyObservers();
        return fr.orElse(null);
    }


    private void DFS(Long x, List<Tuple<Long, Long>> adj, Map<Long, Long> viz, Long nrc) {
        viz.put(x, nrc);
        for(Map.Entry<Long, Long> entry : viz.entrySet()){
            if(adj.contains(new Tuple<>(x, entry.getKey())) && entry.getValue().equals((long) 0)){
                DFS(entry.getKey(), adj, viz, nrc);
            }
        }
    }

    private Long ComponenteConexe(List<Tuple<Long, Long>> adj, Map<Long,Long> viz, Long nrc) {
        for(Map.Entry<Long, Long> entry : viz.entrySet())
            if(entry.getValue() == 0){
                nrc++;
                DFS(entry.getKey(), adj, viz, nrc);
            }
        return nrc;
    }

    private List<Tuple<Long, Long>> createAdj(){
        List<Tuple<Long,Long>> adj = new ArrayList<>();
        Iterable<Prietenie> friendships = frepo.findAll();
        for(Prietenie friendship : friendships){
            adj.add(new Tuple<>(friendship.getId().getLeft(), friendship.getId().getRight()));
            adj.add(new Tuple<>(friendship.getId().getRight(), friendship.getId().getLeft()));
        }
        return adj;
    }

    private Map<Long, Long> createViz(){
        Map<Long, Long> viz = new HashMap<>();
        Iterable<Utilizator> users = repo.findAll();
        for(Utilizator user : users) {
            viz.putIfAbsent(user.getId(), (long) 0);
        }
        return viz;
    }

    public int numberOfCommunities(){
        List<Tuple<Long, Long>> adj = createAdj();
        Map<Long, Long> viz = createViz();
        Long totalCom = ComponenteConexe(adj, viz, (long) 0);
        int nrc = 0;

        for(long i = 1; i <= totalCom; i++){
            if(Collections.frequency(viz.values(), i) > 1)
                nrc++;
        }
        return nrc;
    }

    private List<Utilizator> getCommunity(Long cnumber, Map<Long, Long> viz){
        List<Utilizator> community = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : viz.entrySet()) {
            if (entry.getValue() == cnumber) {
                Optional<Utilizator> user = repo.findOne(entry.getKey());
                user.ifPresent(community::add);
            }
        }
        return community;
    }

    public List<List<Utilizator>> biggestCommunities() {
        List<Tuple<Long, Long>> adj = createAdj();
        Map<Long, Long> viz = createViz();
        Long totalCom = ComponenteConexe(adj, viz, (long) 0);

        int maxMembers = 2;
        List<List<Utilizator>> biggestCommunities = new ArrayList<>();

        for (long i = 1; i <= totalCom; i++) {
            int nrMembers = Collections.frequency(viz.values(), i);
            if (nrMembers > maxMembers)
                maxMembers = nrMembers;
        }

        for (long i = 1; i <= totalCom; i++) {
            if (Collections.frequency(viz.values(), i) == maxMembers) {
                biggestCommunities.add(getCommunity(i, viz));
            }
        }

        return biggestCommunities;
    }


    public Iterable<FriendshipDTO> oneUserFriendships(String uid){
        val.validateLong(uid);
        Long id = Long.parseLong(uid);
        if(repo.findOne(id).isEmpty()){
            return null;
        }

        Set<Prietenie> allFriendships = (Set<Prietenie>) frepo.findAll();

        return allFriendships
                .stream()
                .map(f->{
                    Utilizator friend = null;
                    if(f.getId().getLeft().equals(id)){
                        friend = repo.findOne(f.getId().getRight()).get();
                    }
                    if(f.getId().getRight().equals(id)){
                        friend = repo.findOne(f.getId().getLeft()).get();
                    }
                    return (friend == null) ? null : new FriendshipDTO(friend.getFirstName(), friend.getLastName(), f.getDate());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    public Iterable<FriendshipDTO> oneUserFriendshipsMonth(String uid, String month) {
        val.validateLong(month);
        int mon = Integer.parseInt(month);
        ArrayList<FriendshipDTO> userFriendships = (ArrayList<FriendshipDTO>) oneUserFriendships(uid);
        return userFriendships.
                stream().
                filter(f -> f.getDate().getMonthValue() == mon).
                collect(Collectors.toList());
    }


    public Message sendMessage(String from, List<String> to, String message){
        val.validateLong(from);
        to.forEach(val::validateLong);
        Long id1 = Long.parseLong(from);
        List<Utilizator> to_list = to.stream().map(u->repo.findOne(Long.parseLong(u)).get()).collect(Collectors.toList());
        if(repo.findOne(id1).isEmpty() || to_list.isEmpty()) {
            throw new RepositoryException("Utilizatori invalizi\n");
        }
        Message toSave = new Message(repo.findOne(id1).get(), to_list, message, LocalDateTime.now());
        toSave.setId(generateMessageId());
        Optional<Message> msg = mrepo.save(toSave);
        notifyObservers();
        return msg.orElse(null);
    }


    public Message replyTo(String mid, String from, String message){
        val.validateLong(mid);
        val.validateLong(from);
        Optional<Message> msg = mrepo.findOne(Long.parseLong(mid));
        if(msg.isEmpty()){
            throw new RepositoryException("Mesajul cu id introdus nu exista.\n");
        }
        Optional<Utilizator> user = repo.findOne(Long.parseLong(from));
        if(user.isEmpty()){
            throw new RepositoryException("Utilizatorul cu id introdus nu exista.\n");
        }

        if(!msg.get().getTo().contains(user.get())){
            throw new RepositoryException("Utilizatorul cu id introdus nu poate raspunde.\n");
        }

        List<Utilizator> to_list = new ArrayList<>();
        to_list.add(msg.get().getFrom());
        Message toSave = new ReplyMessage(user.get(), to_list, message, LocalDateTime.now(), msg.get());
        toSave.setId(generateMessageId());
        Optional<Message> reply_msg = mrepo.save(toSave);
        return reply_msg.orElse(null);
    }


    public Message replyAll(String mid, String from, String message){
        val.validateLong(mid);
        val.validateLong(from);
        Optional<Message> msg = mrepo.findOne(Long.parseLong(mid));
        if(msg.isEmpty()){
            throw new RepositoryException("Mesajul cu id introdus nu exista.\n");
        }
        Optional<Utilizator> user = repo.findOne(Long.parseLong(from));
        if(user.isEmpty()){
            throw new RepositoryException("Utilizatorul cu id introdus nu exista.\n");
        }
        if(!msg.get().getTo().contains(user.get())){
            throw new RepositoryException("Utilizatorul cu id introdus nu poate raspunde.\n");
        }

        List<Utilizator> to_list = new ArrayList<>();
        to_list.add(msg.get().getFrom());
        msg.get().getTo().forEach( u -> {
            if(!u.getId().equals(user.get().getId())){
                to_list.add(u);
            }
        });

        Message toSave = new ReplyMessage(user.get(), to_list, message, LocalDateTime.now(), msg.get());
        toSave.setId(generateMessageId());
        Optional<Message> reply_msg = mrepo.save(toSave);
        return reply_msg.orElse(null);
    }


    public Iterable<Message> getConversation(String uid1, String uid2){
        val.validateLong(uid1); val.validateLong(uid2);
        if(repo.findOne(Long.parseLong(uid1)).isEmpty() || repo.findOne(Long.parseLong(uid2)).isEmpty()){
            throw new RepositoryException("ID introduse nu sunt valide.\n");
        }
        Iterable<Message> all = mrepo.findAll();
        List<Message> conversation = new ArrayList<>();

        Long id1 = Long.valueOf(uid1);
        Long id2 = Long.valueOf(uid2);

        all.forEach(m -> {
            if(m.getFrom().getId().equals(id1)){
                for(Utilizator u : m.getTo()){
                    if(u.getId().equals(id2)){
                        conversation.add(m);
                        break;
                    }
                }
            }
            if(m.getFrom().getId().equals(id2)){
                for(Utilizator u : m.getTo()){
                    if(u.getId().equals(id1)){
                        conversation.add(m);
                        break;
                    }
                }
            }
        });

/*        all.forEach(m -> {
            if(m.getFrom().getId().equals(id1) && m.getTo().contains(repo.findOne(id2).get())){
                conversation.add(m);
            }
            if(m.getFrom().getId().equals(id2) && m.getTo().contains(repo.findOne(id1).get())){
                conversation.add(m);
            }
        });*/


        return conversation.stream().sorted(Comparator.comparing(Message::getDate)).collect(Collectors.toList());
    }

    public Iterable<Message> getAllUserMessages(String uid){
        val.validateLong(uid);
        Iterable<Message> all = mrepo.findAll();
        List<Message> userMessages = new ArrayList<>();
        all.forEach(m -> {
            if(m.getFrom().getId().equals(Long.parseLong(uid)) || m.getTo().contains(repo.findOne(Long.parseLong(uid)).get())){
                userMessages.add(m);
            }
        });
        return userMessages.stream().sorted(Comparator.comparing(Message::getDate)).collect(Collectors.toList());
    }


    public CererePrietenie sendFriendRequest(String uid1, String uid2){
        val.validateLong(uid1); val.validateLong(uid2);

        Tuple<Long, Long> fid = new Tuple<>(Long.parseLong(uid1), Long.parseLong(uid2));

        CererePrietenie friendRequest = new CererePrietenie();
        friendRequest.setId(fid);
        Optional<CererePrietenie> added = requestRepo.save(friendRequest);
        notifyObservers();
        return added.orElse(null);
    }

    public CererePrietenie updateFriendRequest(String uid1, String uid2, String status){
        val.validateLong(uid1); val.validateLong(uid2);

        Tuple<Long, Long> fid1 = new Tuple<>(Long.parseLong(uid1), Long.parseLong(uid2));
        Tuple<Long, Long> fid2 = new Tuple<>(Long.parseLong(uid2), Long.parseLong(uid1));

        Optional<CererePrietenie> toChange = requestRepo.findOne(fid1);
        if(toChange.isEmpty()){
            toChange = requestRepo.findOne(fid2);
        }
        toChange.get().setStatus(status);
        Optional<CererePrietenie> friendRequest = requestRepo.update(toChange.get());
        notifyObservers();
        return friendRequest.orElse(null);
    }

    public CererePrietenie acceptFriendRequest(String uid1, String uid2){
        Prietenie prietenie = addFriendship(uid1, uid2);
        if(prietenie == null) {
            deleteFriendRequest(uid1, uid2);
            notifyObservers();
            return null;
        }
        throw new RepositoryException("Eroare la adaugare prietenie in sistem.\n");
    }

    public CererePrietenie rejectFriendRequest(String uid1, String uid2){
        return deleteFriendRequest(uid1, uid2);
    }

    public Iterable<CererePrietenie> getUserFriendRequests(String uid){
        Iterable<CererePrietenie> all = requestRepo.findAll();
        List<CererePrietenie> filtered = new ArrayList<>();
        for(CererePrietenie request : all){
            if(request.getId().getRight().equals(Long.valueOf(uid)) || request.getId().getLeft().equals(Long.valueOf(uid))){
                filtered.add(request);
            }
        }
        return filtered;
    }

    public CererePrietenie deleteFriendRequest(String uid1, String uid2){
        Long id1 = Long.valueOf(uid1);
        Long id2 = Long.valueOf(uid2);
        Optional<CererePrietenie> deleted = requestRepo.delete(new Tuple<>(id1, id2));
        notifyObservers();
        return deleted.orElse(null);
    }

    public boolean areFriends(Long id1, Long id2){
        Optional<Prietenie> friendship = frepo.findOne(new Tuple<>(id1, id2));
        if(friendship.isPresent()){
            return true;
        }
        return frepo.findOne(new Tuple<>(id2, id1)).isPresent();
    }

    public boolean existPendingRequest(Long id1, Long id2){
        Optional<CererePrietenie> req = requestRepo.findOne(new Tuple<>(id1, id2));
        return req.isPresent() && req.get().getStatus().equals("pending");
    }

    public int getEventsNumber(Utilizator user){
        return StreamSupport.stream(eventsRepo.getEventsPage(user).spliterator(), false).sorted(Collections.reverseOrder()).collect(Collectors.toList()).size();
    }

    public Iterable<FeedEvent> getEventsPage(Utilizator user, int from, int limit){
        List<FeedEvent> allEvents = StreamSupport.stream(eventsRepo.getEventsPage(user).spliterator(), false).sorted(Collections.reverseOrder()).collect(Collectors.toList());
        List<FeedEvent> filtered = new ArrayList<>();
        for(int index = from; index < from + limit && index < allEvents.size(); index++){
            filtered.add(allEvents.get(index));
        }
        return filtered;
    }

    public FeedEvent createEvent(Utilizator creator, String title, String description, LocalDateTime startDate){
        FeedEvent event = new FeedEvent(title, creator, startDate, description);
        event.setId(generateEventId());
        Optional<FeedEvent> saved = eventsRepo.save(event);
        if(saved.isEmpty()) {
            for(Utilizator user : creator.getFriends()){
                notificationRepo.addToNotify(event.getId(), user.getId());
            }
        }
        notifyObservers();
        return saved.orElse(null);
    }

    public void joinEvent(Long eid, Long uid){
        if(existToNotify(eid, uid)){
            notificationRepo.removeToNotify(eid, uid);
        }
        notificationRepo.addToNotify(eid, uid);
        notifyObservers();
    }

    public void leaveEvent(Long eid, Long uid){
        notificationRepo.removeToNotify(eid, uid);
        notifyObservers();
    }

    public boolean changeEventNotification(Long eid, Long uid){
        return notificationRepo.existToNotify(eid, uid) ? notificationRepo.removeToNotify(eid, uid) : notificationRepo.addToNotify(eid, uid);
    }

    public boolean existToNotify(Long eid, Long uid){
        return notificationRepo.existToNotify(eid, uid);
    }

    public FeedEvent updateEvent(FeedEvent event, Utilizator creator, String title, String description, LocalDateTime startDate){
        FeedEvent newEvent = new FeedEvent(title, creator, startDate, description);
        newEvent.setId(event.getId());
        Optional<FeedEvent> updated = eventsRepo.update(newEvent);
        eventNotify(event.getId(), creator.getFirstName() + ' ' + creator.getLastName() + " a modificat evenimentul " + event.getTitle());
        notifyObservers();
        return updated.orElse(null);
    } 

    public Notification eventNotify(Long eid, String message){
        Notification notification = new Notification(eid, message);
        notification.setId(generateNotificationId());
        Optional<Notification> saved = notificationRepo.save(notification);
        return saved.orElse(null);
    }

    public void readNotifications(Long uid){
        notificationRepo.readNotifications(uid);
        notifyObservers();
    }

    public Iterable<Notification> getUserNotifications(Long uid, int from, int limit){
        List<Notification> allNotifications = StreamSupport.stream(notificationRepo.getUserNotifications(uid).spliterator(), false).sorted(Collections.reverseOrder()).collect(Collectors.toList());
        List<Notification> filtered = new ArrayList<>();
        for(int index = from; index < from + limit && index < allNotifications.size(); index++){
            filtered.add(allNotifications.get(index));
        }
        return filtered;
    }

    public int countUserNotifications(Long uid){
        return StreamSupport.stream(notificationRepo.getUserNotifications(uid).spliterator(), false).sorted(Collections.reverseOrder()).collect(Collectors.toList()).size();
    }

    public int userNewNotifications(Long uid){
        return notificationRepo.userNewNotifications(uid);
    }

    public Utilizator updateUserNotifications(Utilizator user){
        long days = Period.between(user.getLastLogin().toLocalDate(), LocalDate.now()).getDays();
        if(days > 0) {
            List<FeedEvent> userEvents = StreamSupport
                    .stream(eventsRepo.getEventsPage(user)
                            .spliterator(), false)
                    .sorted(Collections.reverseOrder())
                    .collect(Collectors.toList());

            for(FeedEvent event : userEvents){
                long daysToEvent = Period.between(LocalDate.now(), event.getStartDate().toLocalDate()).getDays();
                if(daysToEvent > 0 && !event.getCreator().getId().equals(user.getId())) {
                    eventNotify(event.getId(), "Au mai ramas " + daysToEvent + " zile pana la evenimentul " + event.getTitle());
                }
            }
        }
        return user;
    }


    public static String hash(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
