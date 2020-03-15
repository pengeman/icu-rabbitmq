package org.peng.icu.rabbitmq.talk;

import com.rabbitmq.client.*;
import org.peng.icu.rabbitmq.utils.RabbitUtil;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @ClassName Main
 * @Date 2020/2/13 18:47
 */
public class Tran {
    static String queueName = RabbitUtil.getQueueName();
    static String exchangeName = "logs";

//    static private Channel buildChannel() throws IOException, TimeoutException {
//        ConnectionFactory factory = RabbitUtil.buildFactory();
//        Connection connection = factory.newConnection();
//        Channel channel = connection.createChannel();
//        return channel;
//    }

    static class Send {

        public static void main(String[] args) throws IOException, TimeoutException {
            Channel channel = RabbitUtil.buildChannel();
//            String queueName = channel.queueDeclare().getQueue();
//            String queueName = RabbitUtil.getQueueName();
            boolean durable = true;
            channel.queueDeclare(queueName, durable, false, false, null);
//            queueName = channel.queueDeclare().getQueue();
//            channel.queueDeclare(queueName, false, false, false, null);
//            channel.exchangeDeclare(exchangeName, "fanout");
//            channel.queueBind(queueName, exchangeName, "");

            String massage = "hello who are you";
            channel.basicPublish(
                    "",
                    queueName,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    massage.getBytes());
            System.out.println("sent " + massage);

            channel.basicPublish(
                    "",
                    queueName,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    massage.getBytes());
            System.out.println("sent " + massage);
        }
    }

    static class Rec {
        public static void main(String[] args) throws IOException, TimeoutException {
            Channel channel = RabbitUtil.buildChannel();
//            String queueName = channel.queueDeclare().getQueue();
//            String queueName = RabbitUtil.getQueueName();

            boolean durable = true;
            channel.queueDeclare(queueName, durable, false, false, null);
//            channel.basicQos(1); // accept only one unack-ed message at a time (see below)
//            channel.exchangeDeclare(exchangeName, "fanout");
//            channel.queueBind(queueName, exchangeName, "");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

            };

            boolean autoAck = false;
            channel.basicConsume(
                    queueName,
                    autoAck,
                    deliverCallback,
                    consumerTag -> {
                        System.out.println("what");
                    });
        }
    }
}
