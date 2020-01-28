package org.cysdigital.proxydb.master;

import org.cysdigital.proxydb.master.queue.BlockingSystemLinkedBlockingQueue;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Storage {

    private Connection connection;
    private final BlockingQueue<Proxy> injectQueue = new LinkedBlockingQueue<Proxy>();
    private final BlockingQueue<Proxy> afterQueue = new LinkedBlockingQueue<Proxy>();
    private final ArrayList<String> duplicationList = new ArrayList<String>();
    private boolean isRunPush = false;

    public Storage() {
        connect();
    }

    private void connect() {
        try {
            Class.forName("org.postgresql.Driver");//185.157.247.142
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/proxydb", "proxydb", "");
            System.out.println("[   OK  ] Database connected.");
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS proxy_storage (\n" +
                    "  proxy VARCHAR(21) NOT NULL,\n" +
                    "  isValid BOOLEAN NOT NULL,\n" +
                    "  isSock BOOLEAN NOT NULL,\n" +
                    "  isHttp BOOLEAN NOT NULL,\n" +
                    "  lastCheck TIMESTAMP NOT NULL,\n" +
                    "  PRIMARY KEY (proxy)\n" +
                    ");");

            System.out.println("[   OK  ] Create or init default table.");
        } catch (Exception e) {
            System.out.println("[   ERROR  ] Connection timeout. Please retry ...");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void refillQueue(BlockingSystemLinkedBlockingQueue queue) {
        queue.setBlocked(true);
        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSetValid = statement.executeQuery("SELECT * FROM proxy_storage WHERE isValid=true AND lastCheck < now() - interval '1 hours' ORDER BY lastCheck ASC LIMIT 10000 OFFSET 0");
            parseProxyPull(queue, resultSetValid);

            ResultSet resultSetNotValid = statement.executeQuery("SELECT * FROM proxy_storage WHERE isValid=false AND lastCheck < now() - interval '3 days' ORDER BY lastCheck ASC LIMIT 5000 OFFSET 0");
            parseProxyPull(queue, resultSetNotValid);

            for (int i = 0; i < 10000; i++) {
                if(injectQueue.size() == 0)
                    break;
                addQueue(queue, injectQueue.take());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        queue.setBlocked(false);
    }

    private void parseProxyPull(BlockingSystemLinkedBlockingQueue queue, ResultSet resultSetNotValid) throws SQLException {
        String[] proxy;
        int port;
        while (resultSetNotValid.next()){
            proxy = resultSetNotValid.getString("proxy").split(":");
            try {
                port = Integer.parseInt(proxy[1]);
                addQueue(queue, new Proxy(proxy[0], port));
            } catch (Exception e) {
            }
        }
    }

    public void pushQueue(BlockingQueue<Proxy> afterQueue) {

        if(isRunPush)
            return;

        isRunPush = true;
        int size;
        if((size = afterQueue.size()) == 0) {
            isRunPush = false;
            return;
        }

        System.out.println("[   OK  ] Push proxy on database (count="+size+")");

        List<String> proxies = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        try {

            preparedStatement = getConnection().prepareStatement("INSERT INTO proxy_storage (proxy, isValid, isSock, isHttp, lastCheck) VALUES (?, ?, ?, ?, NOW()) " +
                    "ON CONFLICT (proxy) DO UPDATE SET  " +
                    "proxy=EXCLUDED.proxy," +
                    "isValid=EXCLUDED.isValid," +
                    "isSock=EXCLUDED.isSock," +
                    "isHttp=EXCLUDED.isHttp," +
                    "lastCheck=EXCLUDED.lastCheck;");

            Proxy proxy;
            for (int i = 0; i < size; i++) {
                proxy = afterQueue.take();
               // System.out.println(i+"/"+size+" ->"+proxy.toString());
                proxies.add(proxy.toString());
                preparedStatement.setString(1, proxy.toString());
                preparedStatement.setBoolean(2, proxy.isValid());
                preparedStatement.setBoolean(3, proxy.isSocks());
                preparedStatement.setBoolean(4, proxy.isHttp());

                preparedStatement.addBatch();

                if ((i % 1000) == 0)
                    preparedStatement.executeBatch();

            }
            preparedStatement.executeBatch();

        } catch(BatchUpdateException e) {
            e.printStackTrace();
        } catch(SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null)
                    preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            proxies.forEach(p -> {
                getDuplicationList().remove(p);
            });
            isRunPush = false;
        }


    }

    public void addQueue(BlockingSystemLinkedBlockingQueue queue, Proxy proxy) {
        if(!getDuplicationList().contains(proxy.toString())){
            getDuplicationList().add(proxy.toString());
            queue.add(proxy);
        }
    }


    public ArrayList<String> getDuplicationList() {
        return duplicationList;
    }

    public synchronized BlockingQueue<Proxy> getInjectQueue() {
        return injectQueue;
    }

    public synchronized BlockingQueue<Proxy> getAfterQueue() {
        return afterQueue;
    }

    public List<String> getProxiesQuantity(int quantity) {
        List<String> lists = new ArrayList<>();
        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM proxy_storage WHERE isValid=true LIMIT "+quantity+" OFFSET 0");
            while (resultSet.next())
                lists.add(resultSet.getString("proxy"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lists;
    }

    public List<String> getProxiesQuantityWithType(int quantity, String type) {
        List<String> lists = new ArrayList<>();
        if(type.equals("socks"))
            type = "issock";
        else if (type.equals("http"))
            type = "ishttp";
        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM proxy_storage WHERE isValid=true AND "+type+"=true LIMIT "+quantity+" OFFSET 0");
            while (resultSet.next())
                lists.add(resultSet.getString("proxy"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lists;
    }
}
