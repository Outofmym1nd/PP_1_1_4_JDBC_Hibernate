package jm.task.core.jdbc;

import jm.task.core.jdbc.dao.UserDaoJDBCImpl;
import jm.task.core.jdbc.util.Util;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {

        try {
            UserDaoJDBCImpl userDaoJDBC = UserDaoJDBCImpl.getInstance();
            userDaoJDBC.createUsersTable();
            userDaoJDBC.saveUser("Ivan", "Ivanov", (byte) 20);
            userDaoJDBC.saveUser("Petr", "Petrov", (byte) 25);
            userDaoJDBC.saveUser("Sidor", "Sidorov", (byte) 30);
            userDaoJDBC.saveUser("Foma", "Fomin", (byte) 35);
            userDaoJDBC.removeUserById(3);
            System.out.println(userDaoJDBC.getAllUsers());
            userDaoJDBC.cleanUsersTable();
            userDaoJDBC.dropUsersTable();
        } finally {
            Util.closePool();
        }
    }
}
