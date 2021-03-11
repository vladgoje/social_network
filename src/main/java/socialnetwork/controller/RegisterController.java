package socialnetwork.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import socialnetwork.domain.CererePrietenie;
import socialnetwork.domain.FriendshipDTO;
import socialnetwork.domain.Prietenie;
import socialnetwork.domain.Utilizator;

import socialnetwork.domain.validators.ValidationException;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import socialnetwork.repository.exceptions.RepositoryException;
import socialnetwork.service.UtilizatorService;
import socialnetwork.utils.events.UserChangeEvent;
import socialnetwork.utils.observer.Observer;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class RegisterController{

    @FXML
    private Label registerNotification;
    @FXML
    private TextField textFname;
    @FXML
    private TextField textLname;
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
    public void handleBackToLogin(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/loginView.fxml"));
            AnchorPane root = loader.load();
            stage.setTitle("Login");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            LoginController loginController = loader.getController();
            loginController.initialize(service, stage);
            stage.show();
        } catch(IOException ex){
            ex.printStackTrace();
        }
    }

    @FXML
    public void handleRegister(){
        try {
            String fname = textFname.getText();
            String lname = textLname.getText();
            String username = textUsername.getText();
            String password = textPassword.getText();
            try {
                Utilizator user = service.addUtilizator(fname, lname, username, password);
                if(user == null){
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("/views/loginView.fxml"));
                    AnchorPane root = loader.load();
                    stage.setTitle("Login");
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    LoginController loginController = loader.getController();
                    loginController.initialize(service, stage);
                    stage.show();
                } else {
                    registerNotification.setText("Un utilizator cu acest username exista deja. Reincercati");
                }
            } catch (ValidationException ex){
                registerNotification.setText("Date invalide. Reincercati");
                textFname.setText("");
                textLname.setText("");
                textUsername.setText("");
                textPassword.setText("");
            }
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

}
