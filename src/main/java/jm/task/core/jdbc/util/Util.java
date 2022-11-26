package jm.task.core.jdbc.util;

import jm.task.core.jdbc.model.User;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class Util {

    private static final String DRIVER_KEY = "db.driver";
    private static final String URL_KEY = "db.url";
    private static final String USERNAME_KEY = "db.username";
    private static final String PASSWORD_KEY = "db.password";
    private static final String POOL_SIZE_KEY = "db.pool.size";
    private static final Integer DEFAULT_POOL_SIZE = 5;
    private static BlockingQueue<Connection> pool;
    private static List<Connection> connectionList;

    private static SessionFactory sessionFactory;


    static {
//        loadDriver();
//        try {
//            initConnectionPool();
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
    }

    private Util() {
    }

    private static void loadDriver() {
        try {
            Class.forName(PropertiesUtil.get(DRIVER_KEY));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initConnectionPool() throws SQLException {
        String poolSize = PropertiesUtil.get(POOL_SIZE_KEY);
        int guaranteedSize = poolSize == null ? DEFAULT_POOL_SIZE : Integer.parseInt(poolSize);
        pool = new ArrayBlockingQueue<>(guaranteedSize);
        connectionList = new ArrayList<>(guaranteedSize);
        for (int i = 0; i < guaranteedSize; i++) {
            Connection connection = openConnection();
            Connection proxyConnection = (Connection) Proxy.newProxyInstance(
                    Util.class.getClassLoader(),
                    new Class[]{Connection.class},
                    (proxy, method, args) ->
                            method.getName().equals("close") ?
                                    pool.add((Connection) proxy) :
                                    method.invoke(connection, args));
            pool.add(proxyConnection);
            connectionList.add(connection);
        }
    }

    public static void closePool() {
        for (Connection connection : connectionList) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Connection getConnect() {
        try {
            return pool.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Connection openConnection() throws SQLException {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(
                    PropertiesUtil.get(URL_KEY),
                    PropertiesUtil.get(USERNAME_KEY),
                    PropertiesUtil.get(PASSWORD_KEY));
            connection.setAutoCommit(false);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();

                Properties properties = new Properties();
                properties.put(AvailableSettings.DRIVER, "com.mysql.cj.jdbc.Driver");
                properties.put(AvailableSettings.URL, "jdbc:mysql://localhost:3306/user_repository");  //?useSSL=false
                properties.put(AvailableSettings.USER, "root");
                properties.put(AvailableSettings.PASS, "root");
                properties.put(AvailableSettings.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
                properties.put(AvailableSettings.SHOW_SQL, "true");
                properties.put(AvailableSettings.FORMAT_SQL, "true");
                properties.put(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "thread");
                properties.put(AvailableSettings.HBM2DDL_AUTO, "");

                configuration.setProperties(properties);
                configuration.addAnnotatedClass(User.class);    //сущность, которую отслеживает SessionFactory

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties()).build();

                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sessionFactory;
    }

    public static void closeSessionFactory() {
        if (sessionFactory != null) {
            try {
                sessionFactory.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}