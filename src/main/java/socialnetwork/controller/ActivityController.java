package socialnetwork.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import socialnetwork.domain.*;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import socialnetwork.service.UtilizatorService;
import socialnetwork.utils.observer.Observer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class ActivityController implements Observer{

    ObservableList<FriendshipDTO> usersModel = FXCollections.observableArrayList();
    ObservableList<Message> messagesModel = FXCollections.observableArrayList();


    @FXML
    private ListView<FriendshipDTO> usersListView;
    @FXML
    private ListView<Message> messagesListView;
    @FXML
    DatePicker datePickerFrom;
    @FXML
    DatePicker datePickerTo;

    private UtilizatorService service;
    Stage dialogStage;
    Utilizator userConnected;

    public void initModel(){
        usersListView.setItems(usersModel);
        messagesListView.setItems(messagesModel);
    }

    @FXML
    public void initialize(UtilizatorService service, Stage stage, Utilizator userConnected) {
        this.service = service;
        this.userConnected = userConnected;
        this.dialogStage=stage;
        this.dialogStage.setMinHeight(400);
        this.dialogStage.setMinWidth(600);
        service.addObserver(this);
        initModel();
        datePickerFrom.valueProperty().addListener((ov, oldValue, newValue) -> {
            filterUsers();
            filterMessages();
        });
        datePickerTo.valueProperty().addListener((ov, oldValue, newValue) -> {
            filterUsers();
            filterMessages();
        });
    }

    private void filterUsers(){
        if(datePickerFrom.getValue() != null && datePickerTo.getValue() != null) {
            List<FriendshipDTO> friendships = StreamSupport
                    .stream(service.oneUserFriendships(userConnected.getId().toString())
                            .spliterator(), false)
                    .filter(f -> f.getDate().isAfter(datePickerFrom.getValue().atStartOfDay()) &&
                            f.getDate().isBefore(datePickerTo.getValue().atTime(LocalTime.now())))
                    .collect(Collectors.toList());
            usersModel.setAll(friendships);
        }
    }

    private void filterMessages(){
        if(datePickerFrom.getValue() != null && datePickerTo.getValue() != null) {
            List<Message> messages = StreamSupport
                    .stream(service.getAllUserMessages(userConnected.getId().toString())
                            .spliterator(), false)
                    .filter(m -> m.getDate().isAfter(datePickerFrom.getValue().atStartOfDay()) &&
                            m.getDate().isBefore(datePickerTo.getValue().atTime(LocalTime.now())))
                    .collect(Collectors.toList());
            messagesModel.setAll(messages);
        }
    }


    private void addMessageTableHeader(PdfPTable table) {
        Stream.of("From", "To", "Date", "Message")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });
    }

    private void addRowsMessage(PdfPTable table, List<Message> list) {
        list.forEach(m -> {
            table.addCell(m.getFrom().toString());
            table.addCell(m.getTo().stream().findFirst().get().getFirstName() + " " + m.getTo().stream().findFirst().get().getLastName() );
            table.addCell(m.getDate().toString());
            table.addCell(m.getMessage());
        });

    }

    @FXML
    private void generateMessagesPdf(){
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("messages.pdf"));
            document.open();

            Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
            PdfPTable table = new PdfPTable(4);
            addMessageTableHeader(table);
            addRowsMessage(table, messagesModel);

            document.add(table);
            document.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }


    private void addUsersTableHeader(PdfPTable table) {
        Stream.of("Friend", "Date")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });
    }

    private void addRowFriend(PdfPTable table, List<FriendshipDTO> list) {
        list.forEach(m -> {
            table.addCell(m.getFname() + " " + m.getLname());
            table.addCell(m.getDate().toString());
        });

    }

    @FXML
    private void generateFriendsPdf(){
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("friends.pdf"));
            document.open();

            Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
            PdfPTable table = new PdfPTable(2);
            addUsersTableHeader(table);
            addRowFriend(table, usersModel);

            document.add(table);
            document.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }


    @FXML
    private void generateBoth(){
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("friends_and_messages.pdf"));
            document.open();

            Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);


            PdfPTable tableFriends = new PdfPTable(2);
            addUsersTableHeader(tableFriends);
            addRowFriend(tableFriends, usersModel);


            PdfPTable tableMessages = new PdfPTable(4);
            addMessageTableHeader(tableMessages);
            addRowsMessage(tableMessages, messagesModel);


            document.add(tableFriends);
            document.add(tableMessages);
            document.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void update() {
        initModel();
    }
}
