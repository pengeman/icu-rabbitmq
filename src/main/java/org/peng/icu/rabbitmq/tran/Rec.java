package org.peng.icu.rabbitmq.tran;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import org.peng.icu.rabbitmq.utils.RabbitUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @ClassName Rec
 * @Date 2020/3/15 15:58
 * @Author pengyifu
 */
public class Rec {
    static String EXCHANGE_NAME = "logs.topic";
    static String TYPE = "topic";

    static private void saveFile(byte[] fileByte, String savePath) {
        try {
            int FILENAME_SIZE = 1024;
            byte[] f = Arrays.copyOfRange(fileByte, 0, 1024);
            byte[] fileData = Arrays.copyOfRange(fileByte, 1024, fileByte.length);
            String fileName = new String(f, UTF_8).trim();
            System.out.println(fileName);
            File saveDir = new File(savePath);
            if(!saveDir.exists()){
                saveDir.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(new File(savePath + "/" + fileName));

            out.write(fileData);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        Channel channel = RabbitUtil.buildChannel();
        String queueName = channel.queueDeclare().getQueue();

//            boolean durable = true;
//            channel.queueDeclare(queueName, durable, false, false, null);
        channel.exchangeDeclare(EXCHANGE_NAME, TYPE);
        String[] routingKeys = {"file.jpg"};
        for (String routingKey : routingKeys) {
            channel.queueBind(queueName, EXCHANGE_NAME, routingKey);
        }

        channel.basicQos(1); // accept only one unack-ed message at a time (see below)
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            byte[] data = delivery.getBody();
            String message = "data";
            System.out.println(" [x] Received '" + delivery.getEnvelope().getRoutingKey() + " : " + message + "'");
            //开启这行 表示使用手动确认模式
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

            saveFile(data, "out");
            System.out.println("over");
        };

        // 监听队列，false表示手动返o回完成状态，true表示自动
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