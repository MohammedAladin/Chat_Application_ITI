package org.Server.Service.Messages;

import Model.DTO.AttachmentDto;
import Model.DTO.MessageDTO;
import org.Server.Repository.AttachmentReopsitory;
import org.Server.Repository.MessageRepository;
import org.Server.ServerModels.ServerEntities.Attachment;
import org.Server.ServerModels.ServerEntities.Message;
import org.Server.Service.Chat.ChatServices;
import org.Server.Service.UserSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MessageServiceImpl {
    MessageRepository messageRepository;
    AttachmentReopsitory attachmentReopsitory;
    static MessageServiceImpl messageServiceImpl;


    private final ChatServices chatServices;

    private MessageServiceImpl() throws RemoteException {
        super();
        messageRepository = new MessageRepository();
        chatServices = ChatServices.getInstance();
        attachmentReopsitory = AttachmentReopsitory.getInstance();
    }

    public static MessageServiceImpl getInstance() {
        if (messageServiceImpl == null) {
            try {
                messageServiceImpl = new MessageServiceImpl();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
        return messageServiceImpl;
    }

    public Integer sendMessage(MessageDTO messageDTO) {
        Message message = messageDTOToMessage(messageDTO);
        return messageRepository.save(message);
    }

    public void deleteMessage(MessageDTO messageDTO){
        messageRepository.deleteMessageByDTO(messageDTO);
    }

    public Integer getLastId() throws SQLException {
        return messageRepository.getLastInsertedId();
    }

    public Message messageDTOToMessage(MessageDTO messageDTO) {
        if (messageDTO.getStyle()==null){
            return new Message(
                    messageDTO.getSenderID(),
                    messageDTO.getChatID(),
                    messageDTO.getContent(),
                    new Timestamp(System.currentTimeMillis()),
                    messageDTO.getIsAttachment() == 1
            );
        }
        else {
            return new Message(
                    messageDTO.getSenderID(),
                    messageDTO.getChatID(),
                    messageDTO.getContent(),
                    new Timestamp(System.currentTimeMillis()),
                    messageDTO.getIsAttachment() == 1,
                    messageDTO.getStyle()
            );
        }

    }

    public List<MessageDTO> getPrivateChatMessages(Integer chatID) {
        List<Message> list = new MessageRepository().getPrivateChatMessages(chatID);
        ArrayList<MessageDTO> messageDTOS = new ArrayList<>();
        for (Message message : list) {
            messageDTOS.add(mapToMessageDTO(message));
        }
        return messageDTOS;
    }

    public MessageDTO mapToMessageDTO(Message message) {
        attachmentReopsitory = AttachmentReopsitory.getInstance();
        Attachment attachment = attachmentReopsitory.findByMessageId(message.getMessageID());
        if (message.isAttachment()) {
            return new MessageDTO(
                    message.getReceiverID(),
                    message.getMessageContent(),
                    message.isAttachment() ? 1 : 0,
                    message.getSenderID(),
                    attachment.getAttachmentID(),
                    message.getMessageTimestamp()
            );
        }
        if (message.getStyle() != null){
            return new MessageDTO(
                    message.getReceiverID(),
                    message.getMessageContent(),
                    message.isAttachment() ? 1 : 0,
                    message.getSenderID(),
                    message.getStyle(),
                    message.getMessageTimestamp()
            );
        }
        else {
            return new MessageDTO(
                    message.getReceiverID(),
                    message.getMessageContent(),
                    message.isAttachment() ? 1 : 0,
                    message.getSenderID(),
                    message.getMessageTimestamp()
            );
        }

    }


}
