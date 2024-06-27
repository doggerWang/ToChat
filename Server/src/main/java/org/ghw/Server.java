package org.ghw;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


@EnableScheduling
@SpringBootApplication
public class Server{
    public static ServerSocketChannel   serverSocketChannel;
    public static ArrayList<SocketChannel> socketChannels=new ArrayList<>();
    public static ServerSocket server_socket;
    public static ArrayList<Socket> socketList=new ArrayList<Socket>();
    public static ReentrantReadWriteLock readWriteLock=new ReentrantReadWriteLock();
    public static Lock readLock=readWriteLock.readLock();
    public static Lock writeLock=readWriteLock.writeLock();
    public static LinkedBlockingQueue<Pair<String, SocketChannel>> messageQueue=new LinkedBlockingQueue<>();
    public static MessageDao messageDao;
    public static void main(String []args) throws SocketException {

        ApplicationContext applicationContext=SpringApplication.run(Server.class, args);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Pair<String,SocketChannel> message = Server.messageQueue.take();
                        encryptWrite(message.getLeft(), message.getRight());
                    } catch (SocketException e) {
                        try {
                            messageQueue.take().getRight().close();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();

        messageDao= (MessageDao) applicationContext.getBean("messageDao");
        try{
            serverSocketChannel=ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(5000));
            serverSocketChannel.configureBlocking(false);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while(true){
                SocketChannel   socketChannel = serverSocketChannel.accept();
                if(socketChannel!=null){
                    writeLock.lock();
                    socketChannels.add(socketChannel);
                    writeLock.unlock();
                }
                StringBuilder stringBuilder=new StringBuilder();
                for(SocketChannel s:socketChannels){
                    if(!s.isOpen()) continue;
                    if (s.read(buffer) >0) {
                        buffer.flip();
                        String message = StandardCharsets.UTF_8.decode(buffer).toString();
                        ArrayList<String>messageList=parseMessages(message);
                        for(String m:messageList){
                            analyze(m,s);
                        }
                       buffer.clear();
                    }
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }finally{
            try{
                if(server_socket!=null){
                    server_socket.close();
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }

    public  static void analyze(String receive,SocketChannel socketChannel) throws IOException {
        String[] param = receive.split("&");
        DataOutputStream output;
        DataInputStream input;
        if(param.length<4||!param[0].equals("GHW")) return ;
        Message message=new Message(null,param[1],param[2],param[4],param[3]);
        if(!message.getType().equals("Ask")){
            broadcast(receive);
            messageDao.insert(message);
        }
        if(message.getType().equals("Quit")){
                socketChannel.close();
        }
        if(message.getType().equals("Ask")){
            int days=Integer.parseInt(message.getText());
            ArrayList<Message>messages=messageDao.select();
            Iterator<Message> iterator=messages.iterator();
            if(days>=10)
                while(iterator.hasNext()){
                    if(!iterator.next().getType().equals("Message")) iterator.remove();
                }

            if(days==0) days=7;
            LocalDateTime start=LocalDateTime.now().minusDays(days);
            for(Message m:messages){
                if(start.isBefore(LocalDateTime.parse(m.getTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                    singlecast(proxyUtil.make(m.getType(),m.getName(),m.getTime(),m.getText()),socketChannel);
            }
        }


    }
    public static void broadcast(String receive) throws IOException {
        Server.readLock.lock();
        for(SocketChannel socketChannel: Server.socketChannels){ //遍历socke集合
            //把读取到的消息发送给各个客户端
            if(socketChannel.isOpen()){
                Server.messageQueue.add(new MutablePair<>(receive,socketChannel));
            }
        }
        Server.readLock.unlock();
    }

    public static void singlecast(String receive,SocketChannel socketChannel) throws IOException {
        Server.readLock.lock();
        Server.messageQueue.add(new MutablePair<>(receive,socketChannel));

        Server.readLock.unlock();
    }

    public static void encryptWrite(String src,SocketChannel socketChannel)throws IOException {
        StringBuilder encryptedMessage = new StringBuilder();
        // 对字符串中的每个字符进行加密操作
        for (int i = 0; i < src.length(); i++) {
            encryptedMessage.append((char) (src.charAt(i) + 13));
        }

        // 添加结束标志符
        encryptedMessage.append((char) 2333);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(encryptedMessage.toString());
        socketChannel.write(byteBuffer);
    }

    public static void readDecrypt(CharBuffer charBuffer,SocketChannel socketChannel)throws IOException{
        StringBuilder stringBuilder=new StringBuilder();
        while (true){
            Character c=charBuffer.get();
            if(c!=2333) stringBuilder.append(c-13);
            else analyze(stringBuilder.toString(),socketChannel);
        }
    }
    public static ArrayList<String> parseMessages(String input) {
        ArrayList<String> messages = new ArrayList<>();
        StringBuilder currentMessage = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            if (currentChar == 2333) {
                messages.add(currentMessage.toString());
                currentMessage.setLength(0); // 清空当前报文
            } else {
                currentMessage.append((char)(currentChar - 13)); // 解密操作
            }
        }

        return messages;
    }
}

