package org.Server.Service.Messages;

import Model.DTO.MessageDTO;
import org.Server.Repository.MessageRepository;
import org.Server.ServerModels.ServerEntities.Message;
import org.Server.Service.Chat.ChatServices;
import org.Server.Service.UserSession;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;

public class MessageServiceImpl {
    MessageRepository messageRepository;
    static MessageServiceImpl messageServiceImpl;

    private final ChatServices chatServices;
    private MessageServiceImpl() throws RemoteException {
        super();
        messageRepository = new MessageRepository();
        chatServices = ChatServices.getInstance();
    }

    public static MessageServiceImpl getInstance (){
        if (messageServiceImpl == null) {
            try {
                messageServiceImpl = new MessageServiceImpl();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
        return messageServiceImpl;
    }
    public void sendMessage(MessageDTO messageDTO) throws RemoteException {
        if (messageDTO.getIsAttachment() != 0){

        } else {
            Message message = messageDTOToMessage(messageDTO);
            messageRepository.save(message);
        }
    }

    private Message messageDTOToMessage (MessageDTO messageDTO){
        return new Message (
                UserSession.getCurrentUser().getUserID(),
                messageDTO.getChatID(),
                messageDTO.getContent(),
                new Timestamp(System.currentTimeMillis()),
                messageDTO.getIsAttachment() == 1
        );
    }
}