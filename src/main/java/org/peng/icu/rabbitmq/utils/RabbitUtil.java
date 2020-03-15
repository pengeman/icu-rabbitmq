package org.peng.icu.rabbitmq.utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

/**
 * Created with IntelliJ IDEA.
 * User: peng
 * Date: 2/17/20
 * Time: 4:08 PM
 * To change this template use File | Settings | File Templates.
 * Description:
 */
public class RabbitUtil {
    private static class Proper {
        private static Properties properties = null;
        private static volatile Proper proper = null;

        private Proper(Properties p) {
            if(proper!=null){
                throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
            }
            properties = p;
        }

        static public Proper getInstance() {
            if (proper == null) {
                synchronized (Proper.class) {
                    InputStream inputStream = ClassLoader.getSystemResourceAsStream("rabbitmq.properties");
                    properties = new Properties();
                    try {
                        properties.load(inputStream);
                        proper = new Proper(properties);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return proper;
        }

        public Properties getProperties() {
            return properties;
        }
    }

    static private Properties getProperties() {
        return Proper.getInstance().getProperties();
    }

    static public ConnectionFactory buildFactory() {
        Properties properties = getProperties();
        ConnectionFactory factory = new ConnectionFactory();
        String rabbitmqServer = properties.getProperty("rabbitmq.server");
        factory.setHost(properties.getProperty("rabbitmq.server." + rabbitmqServer + ".host"));
        factory.setUsername(properties.getProperty("rabbitmq.server." + rabbitmqServer + ".username"));
        factory.setPassword(properties.getProperty("rabbitmq.server." + rabbitmqServer + ".password"));
        return factory;
    }

    public static Channel buildChannel() throws IOException, TimeoutException {
        ConnectionFactory factory = RabbitUtil.buildFactory();
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        return channel;
    }

    static public String getQueueName() {
        return getProperties().getProperty("rabbitmq.queueName");
    }
}
