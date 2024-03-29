package org.Server.GUI.Controllers;

import Interfaces.CallBacks.Client.CallBackServicesClient;
import Interfaces.CallBacks.Server.CallBackServicesServer;
import Model.Enums.UserField;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.converter.DateStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LocalDateStringConverter;
import org.Server.Repository.UserRepository;
import org.Server.ServerModels.ServerEntities.User;
import org.Server.Service.ServerCallBacks.CallBackServicesImpl;

import java.net.URL;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static org.Server.Service.ServerCallBacks.CallBackServicesImpl.clients;


public class ServerDatabaseController implements Initializable {
    @FXML
    public VBox root;

    UserRepository userRepository = new UserRepository();
    TableView<User> tableView = new TableView<User>();
    private CallBackServicesServer callBackServicesServer;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            callBackServicesServer = new CallBackServicesImpl();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        createColumns();
        inputData();

        root.getChildren().add(tableView);
    }

    @FXML
    private void refreshTables(){
        tableView.getItems().clear();
        inputData();
    }

    private void inputData(){
        List<User> users;
        try {
            users = userRepository.findAll();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        users.forEach(user -> tableView.getItems().add(user));

        tableView.setEditable(true);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        tableView.prefHeightProperty().bind(root.heightProperty());
    }
    private void createColumns (){
        // PhoneNumbers
        TableColumn<User, String> phoneNumbersColumn = new TableColumn<User, String>("Phone Number");
        phoneNumbersColumn.setCellValueFactory(new PropertyValueFactory<User, String>("phoneNumber"));
        phoneNumbersColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        phoneNumbersColumn.setOnEditCommit(event -> {
            User user = event.getRowValue();
            String newValue = event.getNewValue();
            if ((clients.containsKey(user.getUserID()))){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Cannot update phone number of an online user!");
                alert.showAndWait();
                tableView.refresh();
            }
            else if (newValue.matches("\\d+")) { // Only numbers
                user.setPhoneNumber(newValue);
                try {
                    userRepository.updateUser(user);
                    HashMap<String, String> myMap = new HashMap<>();
                    myMap.put(UserField.PHONE_NUMBER.getFieldName(), user.getPhoneNumber());
                    callBackServicesServer.updateProfile(user.getUserID(), myMap);
                } catch (SQLException | RemoteException e) {
                    throw new RuntimeException(e);
                }

            } else  {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Invalid phone number format!");
                alert.showAndWait();
                tableView.refresh();
            }
        });
        tableView.getColumns().add(phoneNumbersColumn);

        // User IDs
        TableColumn<User, Integer> userIDsColumn = new TableColumn<User, Integer>("User ID");
        userIDsColumn.setCellValueFactory(new PropertyValueFactory<User, Integer>("userID"));
        tableView.getColumns().add(userIDsColumn);

        // Display Names
        TableColumn<User, String> displayNamesColumn = new TableColumn<User, String>("Display Name");
        displayNamesColumn.setCellValueFactory(new PropertyValueFactory<User, String>("displayName"));
        displayNamesColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        displayNamesColumn.setOnEditCommit(event -> {
            User user = event.getRowValue();
            user.setDisplayName(event.getNewValue());
            try {
                userRepository.updateUser(user);

                // Client callback
                HashMap<String, String> myMap = new HashMap<>();
                myMap.put(UserField.NAME.getFieldName(), user.getDisplayName());
                callBackServicesServer.updateProfile(user.getUserID(), myMap);
            } catch (SQLException | RemoteException e) {
                throw new RuntimeException(e);
            }
        });
        tableView.getColumns().add(displayNamesColumn);

        // Email Addresses
        TableColumn<User, String> emailAddressesColumn = new TableColumn<User, String>("Email Address");
        emailAddressesColumn.setCellValueFactory(new PropertyValueFactory<User, String>("emailAddress"));
        emailAddressesColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        emailAddressesColumn.setOnEditCommit(event -> {
            User user = event.getRowValue();
            String newValue = event.getNewValue();
            if (newValue.matches("\\w+@\\w+\\.\\w+")) { // Email regex pattern
                user.setEmailAddress(newValue);
                try {
                    userRepository.updateUser(user);
                    HashMap<String, String> myMap = new HashMap<>();
                    myMap.put(UserField.EMAIL.getFieldName(), user.getEmailAddress());
                    callBackServicesServer.updateProfile(user.getUserID(), myMap);
                } catch (SQLException | RemoteException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Invalid email address format!");
                alert.showAndWait();
                tableView.refresh();
            }
        });
        tableView.getColumns().add(emailAddressesColumn);

        // Passwords
        TableColumn<User, String> passwordsColumn = new TableColumn<User, String>("Password");
        passwordsColumn.setCellValueFactory(new PropertyValueFactory<User, String>("passwordHash"));
        passwordsColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        passwordsColumn.setOnEditCommit(event -> {
            User user = event.getRowValue();
            user.setPasswordHash(event.getNewValue());
            try {
                userRepository.updateUser(user);

                HashMap<String, String> myMap = new HashMap<>();
                myMap.put(UserField.PASSWORD.getFieldName(), user.getPasswordHash());
                callBackServicesServer.updateProfile(user.getUserID(), myMap);
            } catch (SQLException | RemoteException e) {
                throw new RuntimeException(e);
            }
        });
        tableView.getColumns().add(passwordsColumn);

        // Genders
        TableColumn<User, String> gendersColumn = new TableColumn<User, String>("Gender");
        gendersColumn.setCellValueFactory(new PropertyValueFactory<User, String>("gender"));
        gendersColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        gendersColumn.setOnEditCommit(event -> {
            User user = event.getRowValue();
            String newValue = event.getNewValue();
            if (newValue.equalsIgnoreCase("Male") || newValue.equalsIgnoreCase("Female")) {
                user.setGender(newValue);
                try {
                    userRepository.updateUser(user);
                    HashMap<String, String> myMap = new HashMap<>();
                    myMap.put(UserField.GENDER.getFieldName(), user.getGender());
                    callBackServicesServer.updateProfile(user.getUserID(), myMap);
                } catch (SQLException | RemoteException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Gender must be Male or Female only!");
                alert.showAndWait();
                tableView.refresh();
            }
        });
        tableView.getColumns().add(gendersColumn);

        // Countries
        TableColumn<User, String> countriesColumn = new TableColumn<User, String>("Country");
        countriesColumn.setCellValueFactory(new PropertyValueFactory<User, String>("country"));
        countriesColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        countriesColumn.setOnEditCommit(event -> {
            User user = event.getRowValue();
            user.setCountry(event.getNewValue());
            try {
                userRepository.updateUser(user);

                HashMap<String, String> myMap = new HashMap<>();
                myMap.put(UserField.COUNTRY.getFieldName(), user.getCountry());
                callBackServicesServer.updateProfile(user.getUserID(), myMap);
            } catch (SQLException | RemoteException e) {
                throw new RuntimeException(e);
            }
        });
        tableView.getColumns().add(countriesColumn);

        // Date of Birth
        TableColumn<User, Date> datesColumn = new TableColumn<User, Date>("Date of Birth");
        datesColumn.setCellValueFactory(new PropertyValueFactory<User, Date>("dateOfBirth"));
        datesColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DateStringConverter()));
        datesColumn.setOnEditCommit(event -> {
            User user = event.getRowValue();
            user.setDateOfBirth(event.getNewValue());
            try {
                userRepository.updateUser(user);

                HashMap<String, String> myMap = new HashMap<>();

                // adjusting patterns
                String pattern = "EEE MMM dd HH:mm:ss zzz yyyy"; // Input date pattern
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(pattern);
                Date dateOfBirth = user.getDateOfBirth();
                LocalDate localDateOfBirth = dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String formattedDateOfBirth = localDateOfBirth.format(outputFormatter);

                myMap.put(UserField.DATE_OF_BIRTH.getFieldName(),formattedDateOfBirth);
                callBackServicesServer.updateProfile(user.getUserID(), myMap);
            } catch (SQLException | RemoteException e) {
                throw new RuntimeException(e);
            }
        });
        tableView.getColumns().add(datesColumn);

        // Bio
        TableColumn<User, String> biosColumn = new TableColumn<User, String>("Bio");
        biosColumn.setCellValueFactory(new PropertyValueFactory<User, String>("bio"));
        biosColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        biosColumn.setOnEditCommit(event -> {
            User user = event.getRowValue();
            user.setBio(event.getNewValue());
            try {
                userRepository.updateUser(user);
                HashMap<String, String> myMap = new HashMap<>();
                myMap.put(UserField.BIO.getFieldName(), user.getBio());
                callBackServicesServer.updateProfile(user.getUserID(), myMap);
            } catch (SQLException | RemoteException e) {
                throw new RuntimeException(e);
            }
        });
        tableView.getColumns().add(biosColumn);

        // Status
        TableColumn<User, String> statusesColumn = new TableColumn<User, String>("Status");
        statusesColumn.setCellValueFactory(new PropertyValueFactory<User, String>("userStatus"));
        statusesColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        statusesColumn.setOnEditCommit(event -> {
            User user = event.getRowValue();
            String newValue = event.getNewValue();
            if (newValue.equalsIgnoreCase("Online") || newValue.equalsIgnoreCase("Offline")) {
                user.setUserStatus(newValue);
                try {
                    userRepository.updateUser(user);
                    HashMap<String, String> myMap = new HashMap<>();
                    myMap.put(UserField.STATUS.getFieldName(), user.getUserStatus());
                    callBackServicesServer.updateProfile(user.getUserID(), myMap);
                } catch (SQLException | RemoteException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // Show alert for invalid status
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Status must be 'Online' or 'Offline'!");
                alert.showAndWait();
                tableView.refresh();
            }
        });
        tableView.getColumns().add(statusesColumn);
    }
}
