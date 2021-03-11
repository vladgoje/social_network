package socialnetwork.controller;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import socialnetwork.domain.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.stage.Modality;
import javafx.stage.Stage;

import socialnetwork.domain.validators.ValidationException;
import socialnetwork.service.UtilizatorService;
import socialnetwork.domain.Utilizator;

import socialnetwork.utils.Constants;
import socialnetwork.utils.observer.Observer;

import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class UserPageController implements Observer {

    /************FRIEND CELL****************/
    class FriendCell extends ListCell<Utilizator> {
        HBox hbox = new HBox();
        Label label = new Label("(empty)");
        Pane pane1 = new Pane();
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

            label.setOnMouseClicked((mouseEvent) -> {
                showUserPage(lastItem);
            });
            label.setStyle("-fx-cursor: hand");

            imageView.setOnMouseClicked((mouseEvent) -> {
                showUserPage(lastItem);
            });
            imageView.setStyle("-fx-cursor: hand");

            hbox.getChildren().addAll(imageView, pane1, label);
            hbox.setAlignment(Pos.CENTER);
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
    public Utilizator user;
    Utilizator userConnected = null;
    ObservableList<Utilizator> friendsModel = FXCollections.observableArrayList();
    ObservableList<Utilizator> usersModel = FXCollections.observableArrayList();
    ObservableList<FeedEvent> eventsModel = FXCollections.observableArrayList();

    @FXML
    ListView<Utilizator> usersListView;
    @FXML
    TextField searchTextField;
    @FXML
    VBox searchResults;
    @FXML
    ListView<Utilizator> friendsListView;
    @FXML
    Label connectedUserLabel;
    @FXML
    Label userFriendsLabel;
    @FXML
    Label userNameLabel;
    @FXML
    ImageView userProfileImg;
    @FXML
    Button friendButton;
    @FXML
    ImageView mainProfileImg;
    @FXML
    Label friendRequestLabel;
    @FXML
    Label notificationsLabel;
    @FXML
    VBox userPageOptionsVBox;
    @FXML
    private Pagination eventsPagination;

    private ListView<FeedEvent> eventsListView = new ListView<>();

    private final int nrEventsPerPage = 3;


    Button declineButton = new Button("Decline request");



    public void initialize(UtilizatorService UtilizatorService, Stage stage, Utilizator user, Utilizator userConnected) {
        service = UtilizatorService;
        this.userConnected = userConnected;
        this.dialogStage=stage;
        this.dialogStage.setMaximized(false);
        this.dialogStage.setMaximized(true);
        this.user = user;
        service.addObserver(this);

        usersListView.setCellFactory(param -> new SearchUserCell());
        usersListView.setItems(usersModel);
        searchTextField.textProperty().addListener(x -> handleFilter());
        searchResults.setVisible(false);
        searchResults.setManaged(false);

        friendsListView.setCellFactory(param -> new FriendCell());
        friendsListView.setItems(friendsModel);

        eventsListView.setCellFactory(param -> new EventCell());


        connectedUserLabel.setText(userConnected.getFirstName() + ' ' + userConnected.getLastName());

        try {
            FileInputStream inputstream = new FileInputStream("C:\\Users\\Vlad\\Desktop\\UBB\\SEM3\\MAP\\Lab\\social_network_project\\src\\main\\resources\\img\\account.png");
            Image image = new Image(inputstream);
            mainProfileImg.setImage(image);
        } catch(FileNotFoundException ex){
            ex.printStackTrace();
        }

        connectedUserLabel.setOnMouseClicked((mouseEvent) -> {
            handleBackToMain();
        });
        connectedUserLabel.setStyle("-fx-cursor: hand");

        mainProfileImg.setOnMouseClicked((mouseEvent) -> {
            handleBackToMain();
        });
        mainProfileImg.setStyle("-fx-cursor: hand");


        userFriendsLabel.setText(user.getFirstName() + ' ' + user.getLastName() + " friends");
        userNameLabel.setText(user.getFirstName() + ' ' + user.getLastName());
        try {
            FileInputStream inputstream = new FileInputStream("C:\\Users\\Vlad\\Desktop\\UBB\\SEM3\\MAP\\Lab\\social_network_project\\src\\main\\resources\\img\\account.png");
            Image image = new Image(inputstream);
            userProfileImg.setImage(image);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }

        eventsListView.setItems(eventsModel);
        eventsListView.getStyleClass().add("feedListView");
        VBox.setVgrow(eventsListView, Priority.ALWAYS);

        refresh();
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

    public void refresh(){
        userPageOptionsVBox.getChildren().remove(declineButton);
        if(service.areFriends(userConnected.getId(), user.getId())){
            friendButton.setText("Remove friend");
            friendButton.setOnAction(event -> {
                service.removeFriendship(userConnected.getId().toString(), user.getId().toString());
                refresh();
            });
        } else if (service.existPendingRequest(userConnected.getId(), user.getId())) {
            friendButton.setText("Cancel request");
            friendButton.setOnAction(event -> {
                service.deleteFriendRequest(userConnected.getId().toString(), user.getId().toString());
                refresh();
            });
        } else if(service.existPendingRequest(user.getId(), userConnected.getId())){
            friendButton.setText("Confirm request");
            friendButton.setOnAction(event -> {
                service.acceptFriendRequest(user.getId().toString(), userConnected.getId().toString());
                refresh();
            });
            declineButton.setOnAction(event -> {
                service.rejectFriendRequest(user.getId().toString(), userConnected.getId().toString());
                refresh();
            });
            userPageOptionsVBox.getChildren().add(declineButton);
        } else {
            friendButton.setText("Add Friend");
            friendButton.setOnAction(event -> {
                service.sendFriendRequest(userConnected.getId().toString(), user.getId().toString());
                refresh();
            });
        }
        setLabels();
    }


    private void initModel() {
        ArrayList<Utilizator> friends = new ArrayList<>();
        Iterable<Prietenie> friendships = service.getAllFriendships();
        friendships.forEach(f->{
            if(f.getId().getLeft().equals(user.getId())){
                friends.add(service.getUser(f.getId().getRight().toString()));
            } else if(f.getId().getRight().equals(user.getId())){
                friends.add(service.getUser(f.getId().getLeft().toString()));
            }
        });
        friendsModel.setAll(friends);

        int eventsNumber = service.getEventsNumber(user);
        int nrPages = eventsNumber/nrEventsPerPage;
        if(eventsNumber%nrEventsPerPage > 0){
            nrPages++;
        }

        eventsPagination.setPageCount(nrPages);
        eventsPagination.setCurrentPageIndex(0);
        eventsPagination.setMaxPageIndicatorCount(5);

        eventsPagination.setPageFactory((pageIndex) -> {
            List<FeedEvent> eventsPage = StreamSupport.stream(service.getEventsPage(user,pageIndex * nrEventsPerPage, nrEventsPerPage).spliterator(), false).collect(Collectors.toList());
            eventsModel.setAll(eventsPage);
            eventsListView.setItems(eventsModel);
            return new VBox(eventsListView);
        });
    }

    @FXML
    public void handleBackToMain(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/userView.fxml"));
            AnchorPane root = loader.load();
            dialogStage.setTitle("Main profile");
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            UserController userController = loader.getController();
            userController.initialize(service, dialogStage, userConnected);
            dialogStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleNewMessage(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/sendPersonalMessageView.fxml"));
            AnchorPane root = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("New message - " + user.getFirstName() + ' ' + user.getLastName());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            NewPersonalMessageController personalMessageController = loader.getController();
            personalMessageController.initialize(service, dialogStage, user, userConnected);
            dialogStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void showUserPage(Utilizator user){
        if(user.getId().equals(userConnected.getId())){
            handleBackToMain();
        } else {
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
    public void handleFilter(){
        searchResults.setVisible(true);
        searchResults.setManaged(true);
        String name = searchTextField.getText();
        Iterable<Utilizator> filtered = service.searchByName(name);
        if(name.equals("")){
            usersModel.setAll(new ArrayList<Utilizator>());
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

    @Override
    public void update() {
        initModel();
        refresh();
        setLabels();
    }

    @FXML
    public void handleLogout(){
        dialogStage.close();
    }

}
