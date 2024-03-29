package org.Client.Controllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.Client.Models.Model;
import org.Client.Service.ImageServices;
import org.Client.UserSessionManager;

import javax.imageio.ImageIO;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ResourceBundle;

public class UserMenuController implements Initializable {
    public Button home_btn;
    public Button group_btn;
    public Button noti_btn;
    public Button profile_btn;
    public Button settings_btn;
    @FXML
    public Button logout_btn;
    public Button addContact_btn;
    public Label userName;
    @FXML
    public Circle imageCircle;


    public String getNameProperty() {
        return nameProperty.get();
    }

    public StringProperty namePropertyProperty() {
        return nameProperty;
    }

    public void setNameProperty(String nameProperty) {
        this.nameProperty.set(nameProperty);
    }

    StringProperty nameProperty = new SimpleStringProperty();


    public void setImagebytes(byte[] imagebytes) {
        this.imagebytes.set(imagebytes);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    ObjectProperty<byte[]> imagebytes = new SimpleObjectProperty<>();
    String name;


    StringProperty imageProperty = new SimpleStringProperty();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Model.getInstance().getViewFactory().setNotiButton(noti_btn);
        if (Model.getInstance().getProfilePicture() != null&& Model.getInstance().getProfilePicture().length>0) {
            imageCircle.setFill(new ImagePattern(ImageServices.convertToImage(Model.getInstance().getProfilePicture())));
        } else imageCircle.setFill(new ImagePattern(ImageServices.getDefaultImage()));

        Model.getInstance().profilePictureProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                imageCircle.setFill(new ImagePattern(ImageServices.convertToImage(newValue)));
            }
        });
        Model.getInstance().nameProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                userName.setText(newValue);
            }
        }));

        userName.setText("Welcome, " + Model.getInstance().getName());
        settings_btn.setOnAction(e -> {
            Model.getInstance().getViewFactory().showPopup(settings_btn);
        });



        addContact_btn.setOnAction(e -> {
            Model.getInstance().getViewFactory().showAddContacts(addContact_btn);
        });

        home_btn.setOnAction(e -> {
            if (Model.getInstance().getViewFactory().selectedMenuItemProperty().get().equals("home")) {
                Model.getInstance().getViewFactory().selectedMenuItemProperty().set("");
            }
            Model.getInstance().getViewFactory().selectedMenuItemProperty().set("home");
        });
        profile_btn.setOnAction(e -> {
            if (Model.getInstance().getViewFactory().selectedMenuItemProperty().get().equals("profile")) {
                Model.getInstance().getViewFactory().selectedMenuItemProperty().set("");
            }
            Model.getInstance().getViewFactory().selectedMenuItemProperty().set("profile");
        });

        noti_btn.setOnAction(e -> {
            Model.getInstance().getViewFactory().showNotificationPopUp(noti_btn);
        });
        group_btn.setOnAction(e -> {
            Model.getInstance().getViewFactory().showAddGroup(group_btn);
        });

        logout_btn.setOnAction(e -> {
            Model.getInstance().getViewFactory().logout();

        });
    }


    public String getImageProperty() {
        return imageProperty.get();
    }

    public StringProperty imagePropertyProperty() {
        return imageProperty;
    }

    public void setImageProperty(String imageProperty) {
        this.imageProperty.set(imageProperty);
    }
}
