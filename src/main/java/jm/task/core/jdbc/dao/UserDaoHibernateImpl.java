package jm.task.core.jdbc.dao;

import jm.task.core.jdbc.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

import static jm.task.core.jdbc.util.Util.getSessionFactory;

public class UserDaoHibernateImpl implements UserDao {


    private static final UserDaoHibernateImpl INSTANCE = new UserDaoHibernateImpl();
    private static final SessionFactory sessionFactory = getSessionFactory();
    private static Transaction transaction = null;
    private static final User user = new User();

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

    private static final String GET_ALL_USER_HQL = """
            FROM User
            """;

    private static final String CLEAN_ALL_USER_HQL = """
            DELETE FROM User
            """;

    private UserDaoHibernateImpl() {
    }

    public static UserDaoHibernateImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public void createUsersTable() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createSQLQuery(CREATE_TABLE_SQL).executeUpdate();
            session.getTransaction().commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    @Override
    public void dropUsersTable() {
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.createSQLQuery(DROP_TABLE_SQL).executeUpdate();
            session.getTransaction().commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    @Override
    public void saveUser(String name, String lastName, byte age) {
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.save(new User(name, lastName, age));
            session.getTransaction().commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    @Override
    public void removeUserById(long id) {
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            user.setId(id);
            session.delete(user);
            session.getTransaction().commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    @Override
    public List<User> getAllUsers() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(GET_ALL_USER_HQL).list();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public User getUser(long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(User.class, id);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void cleanUsersTable() {
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.createQuery(CLEAN_ALL_USER_HQL).executeUpdate();
            session.getTransaction().commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        }
    }
}
