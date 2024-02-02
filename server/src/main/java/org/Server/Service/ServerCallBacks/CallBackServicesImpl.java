package org.Server.Service.ServerCallBacks;

import Interfaces.CallBacks.Client.CallBackServicesClient;
import Interfaces.CallBacks.Server.CallBackServicesServer;
import Model.DTO.ChatDto;
import Model.DTO.ContactDto;
import Model.DTO.MessageDTO;
import Model.DTO.NotificationDTO;
import SharedEnums.StatusEnum;
import javafx.application.Platform;
import org.Server.Repository.ContactsRepository;
import org.Server.Repository.UserRepository;
import org.Server.ServerModels.ServerEntities.Contact;
import org.Server.ServerModels.ServerEntities.User;
import org.Server.ServerModels.ServerEntities.UserNotification;
import org.Server.Service.Chat.ChatServices;
import org.Server.Service.Contacts.ContactService;
import org.Server.Service.Contacts.InvitationService;
import org.Server.Service.Contacts.NotificationMapper;
import org.Server.Service.Messages.MessageServiceImpl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallBackServicesImpl extends UnicastRemoteObject implements CallBackServicesServer {
    MessageServiceImpl messageService;
    ChatServices chatServices;

    Map<Integer, CallBackServicesClient> clients = new HashMap<>();


    public void register(CallBackServicesClient client, String clientphone) throws RemoteException {
        UserRepository userRepository = new UserRepository();
        Integer clientId = null;
        User user = null;
        String username = null;
        byte[] profilePicture = null;
        try {
            user = userRepository.findByPhoneNumber(clientphone);
            clientId = user.getUserID();
            username = user.getDisplayName();
            profilePicture = user.getProfilePicture();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        ArrayList<NotificationDTO> notificationDTOS = getNotificationList(clientId);
        clients.put(clientId, client);
        client.setClientId(clientId);
        System.out.println("Client registered :id = " + clientId);
        client.setData(clientphone, username, profilePicture);
        try {
            client.setNotificationList(notificationDTOS);
            client.setContactList(new ContactService().getContacts(clientId));
            client.setGroupList(ChatServices.getInstance().getGroupChats(clientId));
            changeStatus(clientId, "Online");
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Client registered :id = " + clientId);
    }

    private ArrayList<NotificationDTO> getNotificationList(Integer clientId) {
        ArrayList<UserNotification> notifications = new ArrayList<>(new InvitationService().getInvitations(clientId));
        NotificationMapper notificationMapper = new NotificationMapper();
        return notificationMapper.mapToDTOList(notifications);
    }

    public void unRegister(Integer clientId , String phoneNumber) throws RemoteException {
        changeStatus(clientId, "Offline");
        clients.remove(clientId);


    }


    public CallBackServicesImpl() throws RemoteException {
        messageService = MessageServiceImpl.getInstance();
        chatServices = ChatServices.getInstance();
    }

    @Override
    public void sendMessage(MessageDTO messageDTO) throws RemoteException {
        messageService.sendMessage(messageDTO);

        List<Integer> chatParticipantsIds = chatServices.getAllParticipants(messageDTO.getChatID());

    }


    public void sendAnnouncement(String announcement) throws RemoteException {

    }

    public void addContact(Integer clientId, String contactPhoneNumber) throws RemoteException {
        CallBackServicesClient client = clients.get(clientId);
        User user;
        try {
            user = new UserRepository().findByPhoneNumber(contactPhoneNumber);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        boolean exists = new ContactService().addContact(clientId, contactPhoneNumber);
        Platform.runLater(() -> {
            try {
                client.contactExists(exists);
                if (clients.containsKey(user.getUserID()))
                    clients.get(user.getUserID()).setNotificationList(getNotificationList(user.getUserID()));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });


    }

    @Override
    public void acceptInvitation(Integer clientId, Integer acceptedUserID) throws RemoteException {
        CallBackServicesClient client = clients.get(clientId);
        CallBackServicesClient acceptedClient = clients.get(acceptedUserID);
        new ContactService().acceptInvitation(clientId, acceptedUserID);
        System.out.println("accepted client" + acceptedUserID);
        Platform.runLater(() -> {
            try {
                client.deleteNotification(acceptedUserID);
                client.setContactList(new ContactService().getContacts(clientId));
                acceptedClient.setContactList(new ContactService().getContacts(acceptedUserID));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void rejectInvitation(Integer clientId, Integer username) throws RemoteException {
        CallBackServicesClient client = clients.get(username);
        new InvitationService().deleteInvitation(clientId, username);
        Platform.runLater(() -> {
            try {
                client.deleteNotification(clientId);
                System.out.println("rejected client" + clientId);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void changeStatus(Integer ID, String status) throws RemoteException {
        try {
            new UserRepository().updateStatus(clients.get(ID).getPhone(), StatusEnum.fromValue(status));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        List<Integer> contacts = ContactsRepository.getInstance().getContacts(ID);
        for (Integer contact : contacts) {
            if (!clients.containsKey(contact))
                continue;
            CallBackServicesClient client = clients.get(contact);
            Platform.runLater(() -> {
                try {
                    client.changeStatus(ID, status);
                    client.setContactList(new ContactService().getContacts(contact));
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            });
        }

    }

    @Override
    public ContactDto searchForContact(String phoneNumber) throws RemoteException {
        try {
            User user = new UserRepository().findByPhoneNumber(phoneNumber);
            if (user == null)
                return null;
            else return new ContactService().mapUserToContactDto(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void createGroupChat(Integer clientId, String text, ArrayList<Integer> selected) throws RemoteException {
        ContactService contactService = new ContactService();
        System.out.println("callback imp : " + clientId);
        ChatDto newGrp =  contactService.createNewGroup(clientId, selected, text);
        clients.get(clientId).updateGroupList(newGrp);
    }

}
