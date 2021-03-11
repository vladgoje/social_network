package socialnetwork.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import socialnetwork.domain.*;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import socialnetwork.service.UtilizatorService;
import socialnetwork.utils.observer.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class NotificationsController implements Observer{

    ObservableList<Notification> model = FXCollections.observableArrayList();

    @FXML
    private Pagination notificationsPagination;

    private ListView<Notification> notificationsListView = new ListView<>();

    private UtilizatorService service;
    Stage dialogStage;
    Utilizator userConnected;

    @FXML
    public void initialize(UtilizatorService service, Stage stage, Utilizator userConnected) {
        this.service = service;
        this.userConnected = userConnected;
        this.dialogStage=stage;
        this.dialogStage.setMinHeight(400);
        this.dialogStage.setMinWidth(600);
        service.addObserver(this);
        initModel();

        int nrNotificationsPerPage = 3;
        int notificationsNumber = service.countUserNotifications(userConnected.getId());

        int nrPages = notificationsNumber/nrNotificationsPerPage;
        if(notificationsNumber%nrNotificationsPerPage > 0){
            nrPages++;
        }

        notificationsPagination.setPageCount(nrPages);
        notificationsPagination.setCurrentPageIndex(0);
        notificationsPagination.setMaxPageIndicatorCount(5);

        notificationsPagination.setPageFactory((pageIndex) -> {
            List<Notification> notificationsPage = StreamSupport.stream(service.getUserNotifications(userConnected.getId(), pageIndex * nrNotificationsPerPage, nrNotificationsPerPage).spliterator(), false).collect(Collectors.toList());
            model.setAll(notificationsPage);
            notificationsListView.setItems(model);
            VBox returned = new VBox(notificationsListView);
            VBox.setVgrow(returned, Priority.ALWAYS);
            return returned;
        });

        service.readNotifications(userConnected.getId());
    }


    private void initModel() {
//        List<Notification> notifications = StreamSupport.stream(service.getUserNotifications(userConnected.getId(), ).spliterator(), false).collect(Collectors.toList());
//        model.setAll(notifications);
    }

    @FXML
    public void closeNotifications(){
        dialogStage.close();
    }

    @Override
    public void update() {
        initModel();
    }
}
