package jm.task.core.jdbc.dao;

import jm.task.core.jdbc.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static jm.task.core.jdbc.util.Util.getConnect;

public class UserDaoJDBCImpl implements UserDao {


    private static final UserDaoJDBCImpl INSTANCE = new UserDaoJDBCImpl();
    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS user (
              id INT NOT NULL AUTO_INCREMENT,
              name VARCHAR(128) NOT NULL,
              lastname VARCHAR(128) NOT NULL,
              age INT NOT NULL,
              PRIMARY KEY (id),
              UNIQUE INDEX id_UNIQUE (id ASC) VISIBLE)
            """;
    private static final String DROP_TABLE_SQL = """
            DROP TABLE IF EXISTS user
            """;
    private static final String SAVE_USER_SQL = """
            INSERT INTO user (name, lastname, age)
            VALUES (?, ?, ?)
            """;
    private static final String REMOVE_USER_SQL = """
            DELETE FROM user
            WHERE id =?
            """;
    private static final String GET_ALL_USER_SQL = """
            SELECT id, name, lastname, age
            FROM user
            """;
    private static final String CLEAN_ALL_USER_SQL = """
            DELETE FROM user
            """;

    private UserDaoJDBCImpl() {
    }

    public static UserDaoJDBCImpl getInstance() {
        return INSTANCE;
    }

    public void createUsersTable() throws SQLException {
        try (Connection connection = getConnect();
             PreparedStatement preparedStatement = connection.prepareStatement(CREATE_TABLE_SQL)) {
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void dropUsersTable() {
        try (Connection connection = getConnect();
             PreparedStatement preparedStatement = connection.prepareStatement(DROP_TABLE_SQL)) {
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveUser(String name, String lastName, byte age) {
        try (Connection connection = getConnect();
             PreparedStatement preparedStatement = connection.prepareStatement(SAVE_USER_SQL)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, lastName);
            preparedStatement.setByte(3, age);
            preparedStatement.executeUpdate();
            System.out.println("User с именем – " + name + " добавлен в базу данных");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeUserById(long id) {
        try (Connection connection = getConnect();
             PreparedStatement preparedStatement = connection.prepareStatement(REMOVE_USER_SQL)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<User> getAllUsers() {
        try (Connection connection = getConnect();
             PreparedStatement preparedStatement = connection.prepareStatement(GET_ALL_USER_SQL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<User> userList = new ArrayList<>();
            while (resultSet.next()) {
                userList.add(getUser(resultSet));
            }
            return userList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private User getUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getString("name"),
                resultSet.getString("lastname"),
                resultSet.getByte("age")
        );
    }

    public void cleanUsersTable() {
        try (Connection connection = getConnect();
             PreparedStatement preparedStatement = connection.prepareStatement(CLEAN_ALL_USER_SQL)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
