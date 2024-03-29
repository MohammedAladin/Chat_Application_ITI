module client {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires de.jensd.fx.glyphs.fontawesome;
    requires shared;
    requires java.rmi;
    requires java.desktop;
    requires javafx.swing;
    requires javafx.media;

    opens org.Client to javafx.fxml;
    opens org.Client.Controllers to javafx.fxml;
    exports org.Client;
    exports org.Client.Controllers;
    exports org.Client.Models;
    exports org.Client.Views;
    exports org.Client.ClientEntities;
    opens org.Client.ClientEntities to javafx.fxml;
}