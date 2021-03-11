package socialnetwork.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.image.Image;


import socialnetwork.domain.*;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.repository.exceptions.RepositoryException;
import socialnetwork.service.UtilizatorService;
import socialnetwork.utils.Constants;
import socialnetwork.utils.observer.Observer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class UserController implements Observer {

    /************FRIEND CELL****************/
    class FriendCell extends ListCell<Utilizator> {
        HBox hbox = new HBox();
        Label label = new Label("(empty)");
        Pane pane1 = new Pane();
        Pane pane2= new Pane();
        Button button = new Button("Remove");
        Utilizator lastItem;
        ImageView imageView = new ImageView();

        public FriendCell() {
            super();
            try {
                FileInputStream inputstream = new FileInputStream("C:\\Users\\Vlad\\Desktop\\UBB\\SEM3\\MAP\\Lab\\social_network_project\\src\\main\\resources\\img\\account.png");
                Image image = new Image(inputstream);
                imageView.setImage(image);
            } catch (FileNotFoundException e){
                e.printStackTrace();
            }
            imageView.setFitWidth(25);
            imageView.setFitHeight(25);
            pane1.setMinWidth(2);
            pane2.setMinWidth(2);

            button.setStyle("-fx-start-margin: 5px; -fx-padding: 2px");
            button.setOnAction(event -> {
                service.removeFriendship(lastItem.getId().toString(), userConnected.getId().toString());
            });

            label.setOnMouseClicked((mouseEvent) -> {
                showUserPage(lastItem);
            });
            label.setStyle("-fx-cursor: hand");

            imageView.setOnMouseClicked((mouseEvent) -> {
                showUserPage(lastItem);
            });
            imageView.setStyle("-fx-cursor: hand");

            hbox.getChildren().addAll(imageView, pane1, label, pane2, button);
            hbox.setAlignment(Pos.CENTER);
            HBox.setHgrow(pane2, Priority.ALWAYS);
        }

        @Override
        protected void updateItem(Utilizator item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            if (empty) {
                lastItem = null;
                setGraphic(null);
            } else {
                lastItem = item;
                label.setText(item != null ? item.getFirstName() + ' ' + item.getLastName() : "");
                setGraphic(hbox);
            }
        }
    }

    /************CONVERSATION CELL****************/
    class ConversationCell extends ListCell<Conversation> {
        HBox hbox = new HBox();
        Label label = new Label();
        Pane pane1 = new Pane();
        Pane pane2= new Pane();
        Conversation lastItem;
        ImageView imageView = new ImageView();

        public ConversationCell() {
            super();
            try {
                FileInputStream inputstream = new FileInputStream("C:\\Users\\Vlad\\Desktop\\UBB\\SEM3\\MAP\\Lab\\social_network_project\\src\\main\\resources\\img\\account.png");
                Image image = new Image(inputstream);
                imageView.setImage(image);
            } catch (FileNotFoundException e){
                e.printStackTrace();
            }
            imageView.setFitWidth(25);
            imageView.setFitHeight(25);
            pane1.setMinWidth(2);
            pane2.setMinWidth(2);

            hbox.getChildren().addAll(imageView, pane1, label, pane2);
            hbox.setAlignment(Pos.CENTER);
            HBox.setHgrow(pane2, Priority.ALWAYS);
        }

        @Override
        protected void updateItem(Conversation item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            if (item == null) {
                setMouseTransparent(true); //added this line
                setFocusTraversable(false); //added this line
                setDisable(true);
                lastItem = null;
                setGraphic(null);
            } else {
                setMouseTransparent(false); //added this line
                setFocusTraversable(true); //added this line
                setDisable(false);
                lastItem = item;
                AtomicReference<String> labelText = new AtomicReference<>();
                labelText.set("");
                item.getUsers().forEach(x -> {
                    if(labelText.get().equals("")){
                        labelText.set(labelText.get() + x.getFirstName() + ' ' + x.getLastName());
                    } else {
                        labelText.set(labelText.get() + ',' + x.getFirstName() + ' ' + x.getLastName());
                    }
                });
                label.setText(labelText.toString());
                setGraphic(hbox);
            }
        }
    }



    /************SEARCH USER CELL****************/
    class SearchUserCell extends ListCell<Utilizator> {
        HBox hbox = new HBox();
        ImageView img = new ImageView();
        Label label = new Label("");
        Pane pane = new Pane();
        Button button = new Button();

        public SearchUserCell(){
            super();
            try {
                FileInputStream inputstream = new FileInputStream("C:\\Users\\Vlad\\Desktop\\UBB\\SEM3\\MAP\\Lab\\social_network_project\\src\\main\\resources\\img\\account.png");
                Image image = new Image(inputstream);
                img.setImage(image);
            } catch (FileNotFoundException e){
                e.printStackTrace();
            }

            img.setFitWidth(25);
            img.setFitHeight(25);


            label.setOnMouseClicked((mouseEvent) -> {
                showUserPage(this.getItem());
            });
            label.setStyle("-fx-cursor: hand");

            img.setOnMouseClicked((mouseEvent) -> {
                showUserPage(this.getItem());
            });
            img.setStyle("-fx-cursor: hand");


            hbox.getChildren().addAll(img, label, pane, button);
            HBox.setHgrow(pane, Priority.ALWAYS);
            hbox.setAlignment(Pos.CENTER);
            hbox.setSpacing(5);
        }

        @Override
        public void updateItem(Utilizator item, boolean empty){
            super.updateItem(item, empty);
            setText(null);
            setGraphic(null);
            if (item != null && !empty) {
                label.setText(item.getFirstName() + " " + item.getLastName());
                refresh();
                setGraphic(hbox);
            }
        }

        public void refresh(){
            if(service.areFriends(userConnected.getId(), this.getItem().getId())){
                button.setText("Remove friend");
                button.setOnAction(event -> {
                    service.removeFriendship(this.getItem().getId().toString(), userConnected.getId().toString());
                    refresh();
                });
            } else if (service.existPendingRequest(userConnected.getId(), this.getItem().getId())) {
                button.setText("Cancel request");
                button.setOnAction(event -> {
                    service.deleteFriendRequest(userConnected.getId().toString(), this.getItem().getId().toString());
                    refresh();
                });

            } else if(service.existPendingRequest(this.getItem().getId(), userConnected.getId())){
                button.setText("Confirm");
                button.setOnAction(event -> {
                    service.acceptFriendRequest(this.getItem().getId().toString(), userConnected.getId().toString());
                    hbox.getChildren().remove(hbox.getChildren().size()-1);
                    refresh();
                });

                Button decline = new Button("Decline");
                decline.setOnAction(event -> {
                    service.rejectFriendRequest(this.getItem().getId().toString(), userConnected.getId().toString());
                    hbox.getChildren().remove(decline);
                    refresh();
                });
                if(hbox.getChildren().size() == 4) {
                    hbox.getChildren().add(decline);
                }

            } else {
                button.setText("Add friend");
                button.setOnAction(event -> {
                    service.sendFriendRequest(userConnected.getId().toString(), this.getItem().getId().toString());
                    refresh();
                });
            }
        }
    }


    /************EVENT CELL****************/
    class EventCell extends ListCell<FeedEvent> {
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        ImageView profileImg = new ImageView();
        ImageView actionImg = new ImageView();
        Label userLabel = new Label();
        Label title = new Label();
        Label description = new Label();
        Label date = new Label();
        Label startDate = new Label();
        Pane pane1 = new Pane();
        Pane pane2 = new Pane();
        Button joinButton = new Button("Join");
        Button leaveButton = new Button("Leave");
        FeedEvent lastItem;

        public EventCell() {
            super();
            try {
                FileInputStream inputstream = new FileInputStream("C:\\Users\\Vlad\\Desktop\\UBB\\SEM3\\MAP\\Lab\\social_network_project\\src\\main\\resources\\img\\account.png");
                Image image = new Image(inputstream);
                profileImg.setImage(image);
            } catch (FileNotFoundException e){
                e.printStackTrace();
            }

            profileImg.setFitWidth(25);
            profileImg.setFitHeight(25);
            actionImg.setFitWidth(25);
            actionImg.setFitHeight(25);
            actionImg.setStyle("-fx-cursor: hand;");
            pane1.setMinWidth(2);
            prefWidthProperty().bind(eventsListView.widthProperty().subtract(3));
            setMaxWidth(Control.USE_PREF_SIZE);
            setStyle("-fx-background-color: #555");
            title.setStyle("-fx-font-size: 24px; -fx-padding: 5px");
            description.setStyle("-fx-padding: 5px");
            startDate.setStyle("-fx-padding: 5px");
            date.setStyle("-fx-font-size: 10px");
            hbox.getChildren().addAll(profileImg, pane1, userLabel, pane2, actionImg);
            HBox.setHgrow(pane2, Priority.ALWAYS);
            vbox.getChildren().addAll(hbox, date, title, description, startDate);
        }

        private void handleEditEvent(){
            VBox editVBox = new VBox();
            HBox titleBox = new HBox();
            HBox descriptionBox = new HBox();
            HBox dateBox = new HBox();
            HBox editEventButtonsBox = new HBox();
            Label titleLabel = new Label("New title");
            Label descriptionLabel = new Label("New description");
            Label dateLabel = new Label("New date");
            TextField titleField = new TextField(lastItem.getTitle());
            TextArea descriptionField = new TextArea(lastItem.getDescription());
            DatePicker newDatePicker = new DatePicker(lastItem.getStartDate().toLocalDate());
            Button editEventButton = new Button("Save changes");
            Button cancelEventEditButton = new Button("Discard changes");

            editVBox.setAlignment(Pos.CENTER_LEFT);
            editVBox.setSpacing(10);
            titleLabel.setPadding(new Insets(10));
            descriptionLabel.setPadding(new Insets(10));
            dateLabel.setPadding(new Insets(10));
            editEventButtonsBox.setSpacing(20);

            editVBox.getChildren().addAll(titleBox, descriptionBox, dateBox, editEventButtonsBox);
            titleBox.getChildren().addAll(titleLabel, titleField);
            descriptionBox.getChildren().addAll(descriptionLabel, descriptionField);
            dateBox.getChildren().addAll(dateLabel, newDatePicker);
            editEventButtonsBox.getChildren().addAll(editEventButton, cancelEventEditButton);

            setGraphic(editVBox);

            editEventButton.setOnAction(event -> {
                Utilizator creator = userConnected;
                String title = titleField.getText();
                String description = descriptionField.getText();
                LocalDateTime startDate;
                try {
                    startDate = newDatePicker.getValue().atStartOfDay();
                } catch(RuntimeException ex){
                    MessageAlert.showErrorMessage(null, "Choose date!");
                    return;
                }
                try{
                    FeedEvent updated = service.updateEvent(lastItem, creator, title, description, startDate);
                    if(updated != null){
                        MessageAlert.showErrorMessage(null, "A aparut o eroare la adaugarea evenimentului. Reincercati");
                    } else {
                        setGraphic(vbox);
                    }
                } catch (ValidationException ex){
                    MessageAlert.showErrorMessage(null, ex.getErrors());
                }
            });

            cancelEventEditButton.setOnAction(event -> {
                setGraphic(vbox);
            });
        }

        private void handleChangeNotifications(){
            if(service.changeEventNotification(lastItem.getId(), userConnected.getId())){
                if(service.existToNotify(lastItem.getId(), userConnected.getId())){
                    try {
                        FileInputStream inputStreamAction = new FileInputStream("C:\\Users\\Vlad\\Desktop\\UBB\\SEM3\\MAP\\Lab\\social_network_project\\src\\main\\resources\\img\\notifications_active.png");
                        Image image = new Image(inputStreamAction);
                        actionImg.setImage(image);
                        actionImg.setOnMouseClicked(event -> handleChangeNotifications());
                    } catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                } else {
                    try {
                        FileInputStream inputStreamAction = new FileInputStream("C:\\Users\\Vlad\\Desktop\\UBB\\SEM3\\MAP\\Lab\\social_network_project\\src\\main\\resources\\img\\notifications.png");
                        Image image = new Image(inputStreamAction);
                        actionImg.setImage(image);
                        actionImg.setOnMouseClicked(event -> handleChangeNotifications());
                    } catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
            }
        }


        @Override
        protected void updateItem(FeedEvent item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            if (item == null) {
                lastItem = null;
                setGraphic(null);
            } else {
                lastItem = item;
                userLabel.setText(lastItem.getCreator().getFirstName() + ' ' + lastItem.getCreator().getLastName());
                title.setText(lastItem.getTitle());
                description.setText(lastItem.getDescription());
                date.setText(lastItem.getDate().format(Constants.DATE_TIME_FORMATTER));
                startDate.setText("Start date: " + lastItem.getStartDate().format(Constants.DATE_TIME_FORMATTER_BASIC));
                description.setWrapText(true);

//                if(!lastItem.getCreator().getId().equals(userConnected.getId())){
//                    if(service.existToNotify(lastItem.getId(), userConnected.getId())){
//                        leaveButton.setOnAction(event -> {
//                            service.joinEvent(lastItem.getId(), userConnected.getId());
//                            vbox.getChildren().remove(leaveButton);
//                            vbox.getChildren().add(joinButton);
//                        });
//                        vbox.getChildren().add(leaveButton);
//                    } else {
//                        joinButton.setOnAction(event -> {
//                            service.joinEvent(lastItem.getId(), userConnected.getId());
//                            vbox.getChildren().remove(joinButton);
//                            vbox.getChildren().add(leaveButton);
//                        });
//                        vbox.getChildren().add(joinButton);
//                    }
//                }


                if(getItem().getCreator().getId().equals(userConnected.getId())){
                    try {
                        FileInputStream inputStreamAction = new FileInputStream("C:\\Users\\Vlad\\Desktop\\UBB\\SEM3\\MAP\\Lab\\social_network_project\\src\\main\\resources\\img\\edit.png");
                        Image image = new Image(inputStreamAction);
                        actionImg.setImage(image);
                        actionImg.setOnMouseClicked(event -> handleEditEvent());
                    } catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                } else {
                    if(service.existToNotify(lastItem.getId(), userConnected.getId())){
                        try {
                            FileInputStream inputStreamAction = new FileInputStream("C:\\Users\\Vlad\\Desktop\\UBB\\SEM3\\MAP\\Lab\\social_network_project\\src\\main\\resources\\img\\notifications_active.png");
                            Image image = new Image(inputStreamAction);
                            actionImg.setImage(image);
                            actionImg.setOnMouseClicked(event -> handleChangeNotifications());
                        } catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            FileInputStream inputStreamAction = new FileInputStream("C:\\Users\\Vlad\\Desktop\\UBB\\SEM3\\MAP\\Lab\\social_network_project\\src\\main\\resources\\img\\notifications.png");
                            Image image = new Image(inputStreamAction);
                            actionImg.setImage(image);
                            actionImg.setOnMouseClicked(event -> handleChangeNotifications());
                        } catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }

                setGraphic(vbox);
            }
        }

    }


    UtilizatorService service;
    Stage dialogStage;
    Utilizator userConnected = null;
    ObservableList<Utilizator> friendsModel = FXCollections.observableArrayList();
    ObservableList<Utilizator> usersModel = FXCollections.observableArrayList();
    ObservableList<Conversation> conversationModel = FXCollections.observableArrayList();
    ObservableList<FeedEvent> eventsModel = FXCollections.observableArrayList();

    @FXML
    ListView<Utilizator> friendsListView;
    @FXML
    ListView<Conversation> conversationListView;
    @FXML
    ListView<Utilizator> usersListView;
    @FXML
    Label connectedUserLabel;
    @FXML
    TextField searchTextField;
    @FXML
    VBox searchResults;
    @FXML
    ImageView mainProfileImg;
    @FXML
    private Label friendRequestLabel;
    @FXML
    private Label notificationsLabel;
    @FXML
    private VBox newEventBox;
    @FXML
    private TextField eventTitle;
    @FXML
    private TextArea eventDescription;
    @FXML
    private DatePicker eventDate;
    @FXML
    private Pagination eventsPagination;

    private ListView<FeedEvent> eventsListView = new ListView<>();

    private final int nrEventsPerPage = 3;

    public void initialize(UtilizatorService UtilizatorService, Stage stage, Utilizator user) {
        searchResults.setVisible(false);
        searchResults.setManaged(false);
        newEventBox.setVisible(false);
        newEventBox.setManaged(false);

        service = UtilizatorService;
        service.addObserver(this);
        this.userConnected = user;
        this.dialogStage=stage;
        this.dialogStage.setMaximized(false);
        this.dialogStage.setMaximized(true);
        friendsListView.setCellFactory(param -> new FriendCell());
        usersListView.setCellFactory(param -> new SearchUserCell());
        conversationListView.setCellFactory(param -> new ConversationCell());
        eventsListView.setCellFactory(param -> new EventCell());
        connectedUserLabel.setText(userConnected.getFirstName() + " " + userConnected.getLastName());
        connectedUserLabel.setStyle("-fx-cursor: hand");
        mainProfileImg.setStyle("-fx-cursor: hand");

        try {
            FileInputStream inputstream = new FileInputStream("C:\\Users\\Vlad\\Desktop\\UBB\\SEM3\\MAP\\Lab\\social_network_project\\src\\main\\resources\\img\\account.png");
            Image image = new Image(inputstream);
            mainProfileImg.setImage(image);
        } catch(FileNotFoundException ex){
            ex.printStackTrace();
        }

        friendsListView.setItems(friendsModel);
        usersListView.setItems(usersModel);
        conversationListView.setItems(conversationModel);
        eventsListView.setItems(eventsModel);
        eventsListView.getStyleClass().add("feedListView");
        VBox.setVgrow(eventsListView, Priority.ALWAYS);

        searchTextField.textProperty().addListener(x -> handleFilter());
        conversationListView.setOnMouseClicked(event -> handleShowConversation());
        initModel();
        setLabels();
    }

    private void setLabels(){
        AtomicInteger pendingRequests = new AtomicInteger();
        pendingRequests.set(0);
        service.getUserFriendRequests(userConnected.getId().toString())
                .forEach(x -> {
                    if(x.getStatus().equals("pending") && x.getId().getRight().equals(userConnected.getId())){
                        pendingRequests.getAndIncrement();
                    }
                });

        if(pendingRequests.get() == 0){
            friendRequestLabel.setText("");
        } else if(pendingRequests.get() > 0) {
            friendRequestLabel.setText(String.valueOf(pendingRequests.get()));
        }
        
        int newNotifications = service.userNewNotifications(userConnected.getId());

        if(newNotifications == 0){
            notificationsLabel.setText("");
        } else if(newNotifications > 0) {
            notificationsLabel.setText(String.valueOf(newNotifications));
        }

    }

    private void initModel() {
        connectedUserLabel.setText(userConnected.getFirstName() + ' ' + userConnected.getLastName());
        ArrayList<Utilizator> friends = new ArrayList<>();
        Iterable<Prietenie> friendships = service.getAllFriendships();
        friendships.forEach(f->{
            if(f.getId().getLeft().equals(userConnected.getId())){
                friends.add(service.getUser(f.getId().getRight().toString()));
            } else if(f.getId().getRight().equals(userConnected.getId())){
                friends.add(service.getUser(f.getId().getLeft().toString()));
            }
        });

        friendsModel.setAll(friends);

        Iterable<Utilizator> all = service.getAllUsers();
        ArrayList<Conversation> conversations = new ArrayList<>();

        for (Utilizator utilizator : all) {
            try {
                List<Message> messages = StreamSupport.stream(service.getConversation(userConnected.getId().toString(), utilizator.getId().toString())
                        .spliterator(), false).collect(Collectors.toList());
                ArrayList<Utilizator> userList = new ArrayList<>();
                userList.add(utilizator);
                if(messages.size() > 0) {
                    conversations.add(new Conversation(userList, (ArrayList<Message>) messages));
                }
            } catch (RepositoryException | ValidationException ex){
                ex.printStackTrace();
            }
        }
        conversationModel.setAll(conversations);

        int eventsNumber = service.getEventsNumber(userConnected);
        int nrPages = eventsNumber/nrEventsPerPage;
        if(eventsNumber%nrEventsPerPage > 0){
            nrPages++;
        }

        eventsPagination.setPageCount(nrPages);
        eventsPagination.setCurrentPageIndex(0);
        eventsPagination.setMaxPageIndicatorCount(5);

        eventsPagination.setPageFactory((pageIndex) -> {
            List<FeedEvent> eventsPage = StreamSupport.stream(service.getEventsPage(userConnected, pageIndex * nrEventsPerPage, nrEventsPerPage).spliterator(), false).collect(Collectors.toList());
            eventsModel.setAll(eventsPage);
            eventsListView.setItems(eventsModel);
            VBox returned = new VBox(eventsListView);
            VBox.setVgrow(returned, Priority.ALWAYS);

            return returned;
        });
    }

    @FXML
    public void handleShowRequests() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/friendRequestsView.fxml"));
            AnchorPane root = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("My Friend Requests");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setMinWidth(400);
            dialogStage.setMinHeight(300);
            FriendRequestsController friendRequestsController = loader.getController();
            friendRequestsController.initialize(service, dialogStage, userConnected);
            dialogStage.show();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    @FXML
    public void handleShowNotifications() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/notificationsView.fxml"));
            AnchorPane root = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("My notifications");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setMinWidth(400);
            dialogStage.setMinHeight(300);
            NotificationsController notificationsController = loader.getController();
            notificationsController.initialize(service, dialogStage, userConnected);
            dialogStage.show();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    @FXML
    public void handleFilter(){
        searchResults.setVisible(true);
        searchResults.setManaged(true);
        String name = searchTextField.getText();
        Iterable<Utilizator> filtered = service.searchByName(name);
        if(name.equals("")){
            usersModel.setAll(new ArrayList<>());
            searchResults.setVisible(false);
            searchResults.setManaged(false);
        } else {
            List<Utilizator> filtered2 = StreamSupport
                    .stream(filtered.spliterator(), false)
                    .collect(Collectors.toList());
            usersModel.setAll(filtered2.
                    stream().
                    filter(u -> !u.getId().equals(userConnected.getId()))
                    .collect(Collectors.toList()));
        }
    }

    @FXML
    public void handleNewMessage(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/newMessageView.fxml"));
            AnchorPane root = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("New message");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            MessageController messageController = loader.getController();
            messageController.initialize(service, dialogStage, userConnected);
            dialogStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleShowConversation(){
        Conversation selected = conversationListView.getSelectionModel().getSelectedItem();
        if(selected == null) { return; }
        conversationListView.getSelectionModel().clearSelection();
        ArrayList<Utilizator> convUsers = selected.getUsers();
        Utilizator userTo = convUsers.get(0);
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/conversationView.fxml"));
            AnchorPane root = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Conversation - " + userTo.getFirstName() + ' ' + userTo.getLastName());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            ConversationController conversationController = loader.getController();
            conversationController.initialize(service, dialogStage, userConnected.getId().toString(), userTo.getId().toString(), userConnected);
            dialogStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleShowActivity(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/activityView.fxml"));
            AnchorPane root = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("My activity");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            ActivityController activityController = loader.getController();
            activityController.initialize(service, dialogStage, userConnected);
            dialogStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showUserPage(Utilizator user){
        if(!user.getId().equals(userConnected.getId())){
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/views/userPage.fxml"));
                AnchorPane root = loader.load();
                dialogStage.setTitle("Profile - " + user.getFirstName() + ' ' + user.getLastName());
                Scene scene = new Scene(root);
                dialogStage.setScene(scene);
                UserPageController userPageController = loader.getController();
                userPageController.initialize(service, dialogStage, user, userConnected);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleNewEvent(){
        newEventBox.setVisible(true);
        newEventBox.setManaged(true);
    }

    @FXML
    public void createEvent(){
        Utilizator creator = userConnected;
        String title = eventTitle.getText();
        String description = eventDescription.getText();
        LocalDateTime startDate;
        try {
            startDate = eventDate.getValue().atStartOfDay();
        } catch(RuntimeException ex){
            MessageAlert.showErrorMessage(null, "Choose date!");
            return;
        }
        try{
            FeedEvent event = service.createEvent(creator, title, description, startDate);
            if(event != null){
                MessageAlert.showErrorMessage(null, "A aparut o eroare la adaugarea evenimentului. Reincercati");
            } else {
                eventDate.setValue(null);
                eventTitle.setText("");
                eventDescription.setText("");
                newEventBox.setVisible(false);
                newEventBox.setManaged(false);
            }
        } catch (ValidationException ex){
            MessageAlert.showErrorMessage(null, ex.getErrors());
        }
    }

    @FXML
    public void cancelEvent(){
        newEventBox.setVisible(false);
        newEventBox.setManaged(false);
        eventDate.setValue(null);
        eventTitle.setText("");
        eventDescription.setText("");
    }

    @Override
    public void update() {
        initModel();
        setLabels();
    }

    @FXML
    public void handleLogout(){
        dialogStage.close();
    }

}
