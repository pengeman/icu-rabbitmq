package org.peng.icu.rabbitmq.tran;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.MessageProperties;
import org.apache.commons.lang3.StringUtils;
//import utils.RabbitUtil;
import org.peng.Parse;
import org.peng.Protocol;
import org.peng.icu.rabbitmq.utils.RabbitUtil;

import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import static java.nio.charset.StandardCharsets.UTF_8;
/**
 * @ClassName Send
 * @Date 2020/3/15 17:46
 * @Author pengyifu
 */
public class TokU {
    static String EXCHANGE_NAME = "logs.topic";
    static String TYPE = "topic";
    static String sendqueueName = RabbitUtil.getsendQueueName();
    static String recqueueName = RabbitUtil.getrecQueueName();
    static String routingKey = "file.jpg";

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


    static private byte[] getFileBytes(String filePath) {
        File file = new File(filePath);
        try {
            FileInputStream in = new FileInputStream(file);
            int FILENAME_SIZE = 1024;
            int fileSize = in.available();
            byte[] data = new byte[FILENAME_SIZE + fileSize];

            byte[] fileName = file.getName().getBytes();
            System.arraycopy(fileName, 0, data, 0, fileName.length);

            in.read(data, FILENAME_SIZE, fileSize);
            in.close();

            return data;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 发送文本字符串
     *
     * @param constr
     */
    static private void sendMSG(String constr) {
        Protocol protocol = new Protocol();
        protocol.setFlagmsg("MD".getBytes());
        protocol.setContent(constr.getBytes());

        //send(constr.getBytes());
        send(protocol);
    }

    /**
     * 发送文档对象
     *
     * @param filename
     */
    static private void sendDOC(String filename) {
        Protocol protocol = new Protocol();
        byte[] bytefile = getFileBytes(filename);
        protocol.setFlagmsg("DD".getBytes());
        protocol.setContent(bytefile);
        send(protocol);
    }

    static private void send(Protocol protocol){
        send(protocol.toBytes());
    }
    static private void send(byte[] constr) {
        try {
            Channel channel = RabbitUtil.buildChannel();

            channel.exchangeDeclare(EXCHANGE_NAME, TYPE);


            channel.queueBind(sendqueueName, EXCHANGE_NAME, routingKey);

            byte[] data = constr;

            channel.basicPublish(
                    EXCHANGE_NAME,
                    routingKey,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    data);
        } catch (TimeoutException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }



    static private void rec() {

        Channel channel = null;
        try {
            channel = RabbitUtil.buildChannel();

            boolean durable = true;
            channel.queueDeclare(recqueueName, durable, false, false, null);

            Channel finalChannel = channel;
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                Protocol protocol = null;
                Parse parse = new Parse();
                //String message = new String(delivery.getBody(), "UTF-8");
                byte[] msg = delivery.getBody();
                parse.setProtos(msg);
                protocol = parse.check();
                if  (protocol == null) return;
                String message = null;
                String flag = new String(protocol.getFlagmsg());

                if (flag.equals("MD")){
                    message = new String(protocol.getContent());
                }else if (flag.equals("DD")){
                    String savePath = "out";
                    byte[] filecontext = protocol.getContent();
                    saveFile(filecontext,savePath);
                }


                System.out.println("--> ↘ " + message);

                finalChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };

            boolean autoAck = false;
            channel.basicConsume(
                    recqueueName,
                    autoAck,
                    deliverCallback,
                    consumerTag -> {
                        System.out.println("what.........................");
                    });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    static private String date(){
        return new java.text.SimpleDateFormat("yyyy/MM/dd").format(new Date());
    }

    public static void main(String[] args) {
        System.out.printf("rec启动");
        // 接受
        new Thread(){
            @Override
            public void run(){
              rec();
            }
        }.run();

        System.out.printf("send 启动");
        // 发送
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        byte by[] = new byte[10];
                        System.out.print("> ");
                        Scanner sc = new Scanner(System.in);
                        String bbs = sc.nextLine();
                        System.out.println(bbs);
                        if (bbs.equals("exit")) {
                            System.exit(0);
                            break;
                        }
                        String subbbs = "";
                        String[] subbs = bbs.split(":");
                        subbbs = subbs[0];
                        String substr = subbs[1];
                        if (StringUtils.equals(subbbs, "f") || StringUtils.equals(subbbs, "file")) {
                            // 发送文档
                            sendDOC(substr);
                        } else if (StringUtils.equals(subbbs, "m") || StringUtils.equals(subbbs, "msg")) {
                            // 发送字符串
                            sendMSG(substr);
                        } else if (StringUtils.equals(subbbs, ":file")) {
                            // 进入发送文档模式
                        } else if (StringUtils.equals(subbbs, ":message")) {
                            // 进入字符串聊天模式
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.run();
    }
}

