package org.Server;
import org.Server.ServerModels.ServerEntities.User;
import org.Server.Repository.ContactsRepository;
import org.Server.Repository.DatabaseConnectionManager;
import org.Server.Repository.UserNotificationRepository;
import org.Server.Service.Contacts.ContactService;
import org.Server.Service.Contacts.InvitationService;
import org.Server.Service.User.UserService;
import org.Server.Service.UserSession;

import java.rmi.RemoteException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
public class TestApp {
    public static void main(String[] args) {

        User user1 = new User(
                "123456789",
                "John Doe",
                "john@example.com",
                null,
                "hashed_password",
                "Male",
                "USA",
                Date.valueOf("2000-01-01"),
                "Bio goes here",
                "Available",
                "Online",
                Timestamp.valueOf("2022-01-01 12:00:00")
        );
        User user2 = new User(
                "987654321",
                "Jane Smith",
                "jane@example.com",
                null,
                "hashed_password",
                "Female",
                "Canada",
                Date.valueOf("1995-05-15"),
                "Another bio",
                "Busy",
                "Offline",
                Timestamp.valueOf("2022-01-02 10:30:00")
        );

        try {
            DatabaseConnectionManager connectionManager = DatabaseConnectionManager.getInstance();
            UserService userService = new UserService();
//            userService.registerUser(user1);
//            userService.registerUser(user2);

            UserSession.setCurrentUser(userService.existsByPhoneNumber(user2.getPhoneNumber()));
            System.out.println("Logged Userid " + userService.existsByPhoneNumber(user2.getPhoneNumber()).getUserID());
            System.out.println("User To Be Accepted Userid " + userService.existsByPhoneNumber(user1.getPhoneNumber()).getUserID());


            InvitationService invitationService = new InvitationService(
                    userService,
                    new UserNotificationRepository(connectionManager.getMyConnection())
            );

            ContactService contactService = new ContactService(
                    invitationService,
                    new ContactsRepository(connectionManager.getMyConnection()),
                    UserSession.getCurrentUser(),
                    userService
            );

            contactService.acceptContact(user1.getPhoneNumber());

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }


    }


}
