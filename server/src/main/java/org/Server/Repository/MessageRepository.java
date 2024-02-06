package org.Server.Repository;
import org.Server.RepoInterfaces.MessageRepoInterface;
import org.Server.ServerModels.ServerEntities.Message;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;

public class MessageRepository implements MessageRepoInterface {
    private final Connection connection;
    public MessageRepository() {
        this.connection = DatabaseConnectionManager.getInstance().getMyConnection();
    }
    @Override
    public Integer save(Message message) {
        String query = "INSERT INTO Messages (SenderID, ReceiverID, MessageContent, IsAttachement) " +
                "VALUES (?, ?, ?, ?);";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, message.getSenderID());
            preparedStatement.setInt(2, message.getReceiverID());
            preparedStatement.setString(3, message.getMessageContent());
            preparedStatement.setInt(4, message.isAttachment() ? 1 : 0);

            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    System.out.println("Generated MessageID: " + generatedKeys.getInt(1));
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating attachment failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
     public Integer getLastInsertedId() throws SQLException {
        int lastInsertedId = -1;
        String query = "SELECT LAST_INSERT_ID()";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                lastInsertedId = resultSet.getInt(1);
            }
        }
        return lastInsertedId;
    }



    @Override
    public Message findById(Integer id) {
        String query = "select * from Messages where MessageID = ?";
        Message message = new Message();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                mapToMessage(message, resultSet);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return message;

    }

    private void mapToMessage(Message message, ResultSet resultSet) throws SQLException {
        message.setMessageID(resultSet.getInt("MessageID"));
        message.setSenderID(resultSet.getInt("SenderID"));
        message.setReceiverID(resultSet.getInt("ReceiverID"));
        message.setMessageContent(resultSet.getString("MessageContent"));
        message.setMessageTimestamp(resultSet.getTimestamp("MessageTimestamp"));
        message.setAttachment(resultSet.getInt("IsAttachement") == 1 ? true : false);

    }

    @Override
    public List<Message> findAll() {
        List<Message> messagesList = new ArrayList<>();
        String query = "select * from Messages";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Message message = new Message();
                mapToMessage(message, resultSet);
                messagesList.add(message);

            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messagesList;
    }

    @Override
    public void deleteById(Integer id) {

        String query = "DELETE FROM Messages " +
                "WHERE MessageID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public List<Message> getPrivateChatMessages(Integer chatID) {
        List<Message> messagesList = new ArrayList<>();
        String query = "select * from Messages where ReceiverID=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, chatID);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Message message = new Message();
                mapToMessage(message, resultSet);
                System.out.println("message" + message.getMessageContent());
                messagesList.add(message);
            }
            resultSet.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messagesList;
    }
}



