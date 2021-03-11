package socialnetwork;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import socialnetwork.controller.LoginController;
import socialnetwork.domain.*;
import socialnetwork.domain.validators.EventValidator;
import socialnetwork.domain.validators.FriendshipValidator;
import socialnetwork.domain.validators.MessageValidator;
import socialnetwork.domain.validators.UtilizatorValidator;
import socialnetwork.repository.Repository;
import socialnetwork.repository.database.*;
import socialnetwork.service.UtilizatorService;

import java.io.IOException;

public class MainApp extends Application {

    Repository<Long, Utilizator> userDbRepository = UtilizatorDbRepository.getInstance(new UtilizatorValidator());
    Repository<Tuple<Long, Long>, Prietenie> friendshipDbRepository = FriendshipDbRepository.getInstance(new FriendshipValidator());
    Repository<Long, Message> messageRepository = MessageDbRepository.getInstance(new MessageValidator(), userDbRepository);
    Repository<Tuple<Long, Long>, CererePrietenie> friendshipRequestDbRepository = FriendshipRequestDbRepository.getInstance();
    Repository<Long, FeedEvent> eventsDbRepository = EventsRepository.getInstance(new EventValidator());
    Repository<Long, Notification> notificationsDbRepository = NotificationsDbRepository.getInstance();
    UtilizatorService srv = new UtilizatorService(userDbRepository, friendshipDbRepository, messageRepository,friendshipRequestDbRepository, eventsDbRepository, notificationsDbRepository);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        initView(primaryStage);
        primaryStage.setWidth(800);
        primaryStage.show();
    }

    private void initView(Stage primaryStage) throws IOException {
        FXMLLoader userLoader = new FXMLLoader();
        userLoader.setLocation(getClass().getResource("/views/loginView.fxml"));
        AnchorPane userLayout = userLoader.load();
        primaryStage.setScene(new Scene(userLayout));

        LoginController loginController = userLoader.getController();
        loginController.initialize(srv, primaryStage);
    }
}
