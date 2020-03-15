package org.peng.icu.rabbitmq.tran;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import org.peng.icu.rabbitmq.utils.RabbitUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @ClassName Send
 * @Date 2020/3/15 17:46
 * @Author pengyifu
 */
public class Send {
    static String EXCHANGE_NAME = "logs.topic";
    static String TYPE = "topic";

    private static byte[] getFileBytes(String filePath) {
        File file = new File(filePath);
        try {
            FileInputStream in = new FileInputStream(file);
            int FILENAME_SIZE = 1024;
            int fileSize = in.available();
            byte[] data = new byte[FILENAME_SIZE + fileSize];

            byte[] fileName = file.getName().getBytes();
            System.arraycopy(fileName, 0, data, 0, fileName.length);

            in.read(data,FILENAME_SIZE,fileSize);
            in.close();

            return data;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        Channel channel = RabbitUtil.buildChannel();

        String queueName = channel.queueDeclare().getQueue();
        channel.exchangeDeclare(EXCHANGE_NAME, TYPE);
        String[] routingKeys = {"file.jpg"};
        for (String routingKey : routingKeys) {
            channel.queueBind(queueName, EXCHANGE_NAME, routingKey);
        }

        String fileName = "C:\\Users\\Hypers\\Documents\\PROGRAM\\IdeaProjects\\icu-rabbitmq.rar";
        byte[] data = getFileBytes(fileName);
        for (String routingKey : routingKeys) {
            String massage = "hello who are you";
            channel.basicPublish(
                    EXCHANGE_NAME,
                    routingKey,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    data);
            System.out.println("sent " + routingKey + " : " + massage);
        }
        System.out.println("over");
    }
}

