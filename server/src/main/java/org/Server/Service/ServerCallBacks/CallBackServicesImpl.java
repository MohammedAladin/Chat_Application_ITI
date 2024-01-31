package org.Server.Service.ServerCallBacks;

import Interfaces.CallBacks.Client.CallBackServicesClient;
import Interfaces.CallBacks.Server.CallBackServicesServer;
import Model.DTO.ContactDto;
import Model.DTO.MessageDTO;
import Model.DTO.NotificationDTO;
import javafx.application.Platform;
import org.Server.Repository.ContactsRepository;
import org.Server.Repository.UserRepository;
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
        try {
            clientId = userRepository.findByPhoneNumber(clientphone).getUserID();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ArrayList<NotificationDTO> notificationDTOS = getNotificationList(clientId);

        clients.put(clientId, client);
        client.setClientId(clientId);
        try {
            client.setNotificationList(notificationDTOS);
            client.setContactList(new ContactService().getContacts(clientId));
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

    public void unRegister(Integer clientId) {
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

    @Override
    public void unRegister(CallBackServicesClient client) throws RemoteException {

    }

    public void sendAnnouncement(String announcement) throws RemoteException {

    }

    public void addContact(Integer clientId, String contactPhoneNumber) throws RemoteException {
        CallBackServicesClient client = clients.get(clientId);
        boolean exists = new ContactService().addContact(clientId, contactPhoneNumber);
        Platform.runLater(() -> {
            try {
                client.contactExists(exists);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });


    }

    @Override
    public void acceptInvitation(Integer clientId, Integer acceptedUserID) throws RemoteException {
        CallBackServicesClient client = clients.get(clientId);
        new ContactService().acceptInvitation(clientId, acceptedUserID);
        System.out.println("accepted client" + acceptedUserID);
        Platform.runLater(() -> {
            try {
                client.deleteNotification(acceptedUserID);
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

    public void changeStatus(Integer ID, String status) throws RemoteException{
        List<Integer> contacts = ContactsRepository.getInstance().getContacts(ID);
        for (Integer contact : contacts) {
            if(!clients.containsKey(contact))
                continue;
            CallBackServicesClient client = clients.get(contact);
            Platform.runLater(() -> {
                try {
                    client.changeStatus(ID, status);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            });
        }

    }

    @Override
    public ContactDto searchForContact(String phoneNumber) throws RemoteException {
        try {
            User user =new UserRepository().findByPhoneNumber(phoneNumber);
            if(user == null)
                return null;
            else return new ContactService().mapUserToContactDto(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


}
