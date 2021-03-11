package socialnetwork.controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import socialnetwork.domain.Utilizator;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import socialnetwork.service.UtilizatorService;


import java.io.IOException;
import java.time.LocalDateTime;

public class LoginController{

    @FXML
    private Label loginNotification;
    @FXML
    private TextField textUsername;
    @FXML
    private TextField textPassword;



    private UtilizatorService service;
    Stage stage;

    public void initialize(UtilizatorService service, Stage stage){
        this.service = service;
        this.stage = stage;
        this.stage.setMinHeight(400);
        this.stage.setMinWidth(600);
        this.stage.setMaximized(false);
    }
 
    @FXML
    public void handleLogin() {
        String username = textUsername.getText();
        String password = textPassword.getText();
        Utilizator user = service.connectUser(username, password);
        if(user != null){
            user = service.updateUserNotifications(user);
            user.setLastLogin(LocalDateTime.now());
            service.updateUser(user);
            loginUser(user);
        } else {
            loginNotification.setText("Invalid name or password");
        }
    }

    public void handleRegister(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/registerView.fxml"));
            AnchorPane root = loader.load();
            stage.setTitle("Register");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            RegisterController registerController = loader.getController();
            registerController.initialize(service, stage);
            stage.show();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public void loginUser(Utilizator user) {
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/userView.fxml"));
            AnchorPane root = loader.load();
            Stage userStage = new Stage();
            userStage.setTitle("My profile");
            Scene scene = new Scene(root);
            userStage.setScene(scene);
            UserController userController = loader.getController();
            userController.initialize(service, userStage, user);
            userStage.show();
        } catch(IOException ex){
            ex.printStackTrace();
        }
    }

}
