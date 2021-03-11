package socialnetwork.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import socialnetwork.domain.*;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.repository.exceptions.RepositoryException;
import socialnetwork.service.UtilizatorService;
import socialnetwork.utils.Constants;
import socialnetwork.utils.observer.Observer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ConversationController implements Observer {

    /************CONVERSATION MESSAGE CELL****************/
    class ConversationMessageCell extends ListCell<Message> {
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        Label messageText = new Label();
        Label date = new Label();
        Pane pane1 = new Pane();
        Message lastItem;
        ImageView imageView = new ImageView();

        public ConversationMessageCell() {
            super();
            try {
                FileInputStream inputstream = new FileInputStream("C:\\Users\\Vlad\\Desktop\\UBB\\SEM3\\MAP\\Lab\\social_network_project\\src\\main\\resources\\img\\account.png");
                Image image = new Image(inputstream);
                imageView.setImage(image);
            } catch (FileNotFoundException e){
                e.printStackTrace();
            }
            imageView.setFitWidth(15);
            imageView.setFitHeight(15);
            pane1.setMinWidth(2);
            messageText.setMaxWidth(200);
            messageText.setStyle("-fx-background-color: #222; -fx-padding: 10px; -fx-border-radius: 7 7 7 7;  -fx-background-radius: 7 7 7 7; -fx-border-outsets: 5px; -fx-background-insets: 5px;");
            date.setStyle("-fx-font-size: 7pt ;");
            vbox.getChildren().addAll(imageView, pane1, messageText, date);
            hbox.getChildren().addAll(vbox);
        }

        @Override
        protected void updateItem(Message item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            if (item == null) {
                lastItem = null;
                setGraphic(null);
            } else {
                lastItem = item;
                messageText.setText(item.getMessage());
                date.setText(lastItem != null ? item.getDate().format(Constants.DATE_TIME_FORMATTER) : "");
                if(lastItem.getFrom().getId().equals(userConnected.getId())){
                    vbox.setAlignment(Pos.CENTER_RIGHT);
                    hbox.setAlignment(Pos.CENTER_RIGHT);
                } else {
                    vbox.setAlignment(Pos.CENTER_LEFT);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                }
                setGraphic(hbox);
            }
        }
    }

    UtilizatorService service;
    Stage dialogStage;
    Utilizator to;
    Utilizator userConnected;

    ObservableList<Message> messageModel = FXCollections.observableArrayList();

    @FXML
    ListView<Message> messagesListView;
    @FXML
    TextField textMessageBox;
    @FXML
    Label toLabel;
    @FXML
    DatePicker datePickerFrom;
    @FXML
    DatePicker datePickerTo;



    public void initialize(UtilizatorService UtilizatorService, Stage stage, String uid1, String uid2, Utilizator userConnected) {
        service = UtilizatorService;
        this.userConnected = userConnected;
        this.dialogStage=stage;
        this.dialogStage.setMinHeight(400);
        this.dialogStage.setMinWidth(600);
        service.addObserver(this);
        messagesListView.setItems(messageModel);
        messagesListView.setCellFactory(param -> new ConversationMessageCell());


        if(!uid1.equals("") && !uid2.equals("")){
            this.to = service.getUser(uid2);
            this.toLabel.setText(to.getFirstName() + " " + to.getLastName());
        }

        datePickerFrom.valueProperty().addListener((ov, oldValue, newValue) -> {
            filterMessages();
        });
        datePickerTo.valueProperty().addListener((ov, oldValue, newValue) -> {
            filterMessages();
        });

        initModel();
    }

    private void initModel() {
        if(to != null){
            List<Message> messages = StreamSupport.stream(service.getConversation(userConnected.getId().toString(), to.getId().toString())
                    .spliterator(), false).collect(Collectors.toList());
            messageModel.setAll(messages);
        }
    }

    private void filterMessages(){
        if(to != null){
            if(datePickerFrom.getValue() == null || datePickerTo.getValue() == null){
                initModel();
            } else {
                List<Message> messages = StreamSupport
                        .stream(service.getConversation(userConnected.getId().toString(), to.getId().toString())
                                .spliterator(), false)
                        .filter(m -> m.getDate().isAfter(datePickerFrom.getValue().atStartOfDay()) &&
                                m.getDate().isBefore(datePickerTo.getValue().atTime(LocalTime.now())))
                        .collect(Collectors.toList());
                messageModel.setAll(messages);
            }
        }
    }

    @FXML
    public void handleSendMessage(){
        datePickerFrom.setValue(null);
        datePickerTo.setValue(null);
        Utilizator selected = to;
        String messageText = textMessageBox.getText();
        if(selected != null){
            try{
                List<String> to = new ArrayList<>();
                to.add(selected.getId().toString());
                Message message = service.sendMessage(userConnected.getId().toString(), to, messageText);
                if(message != null){
                    MessageAlert.showErrorMessage(null, "Mesajul nu a fost trimis. Reincercati");
                }
            } catch(RepositoryException | ValidationException ex){
                MessageAlert.showErrorMessage(null, ex.getMessage());
            }
        } else {
            MessageAlert.showErrorMessage(null, "Nu ai selectat nimic");
        }
    }

    @Override
    public void update() {
        initModel();
    }
}
