package jm.task.core.jdbc;

import jm.task.core.jdbc.dao.UserDaoHibernateImpl;

import static jm.task.core.jdbc.util.Util.closeSessionFactory;

public class Main {

    public static void main(String[] args) {

        try {
            UserDaoHibernateImpl userDao = UserDaoHibernateImpl.getInstance();
            userDao.createUsersTable();

            userDao.saveUser("Ivan", "Ivanov", (byte) 20);
            userDao.saveUser("Petr", "Petrov", (byte) 25);
            userDao.saveUser("Sidor", "Sidorov", (byte) 30);
            userDao.saveUser("Foma", "Fomin", (byte) 35);

            userDao.removeUserById(3);

            System.out.println(userDao.getUser(3));

            System.out.println(userDao.getAllUsers());

            userDao.cleanUsersTable();
            userDao.dropUsersTable();
        } finally {
            closeSessionFactory();
        }
    }
}