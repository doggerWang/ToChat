package org.ghw;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
@EnableScheduling
@SpringBootApplication
public class Server{
    public static ServerSocket server_socket;
    public static ArrayList<Socket> socketList=new ArrayList<Socket>();
    public static ReentrantReadWriteLock readWriteLock=new ReentrantReadWriteLock();
    public static Lock readLock=readWriteLock.readLock();
    public static Lock writeLock=readWriteLock.writeLock();
    public static LinkedBlockingQueue<Pair<String, Socket>> messageQueue=new LinkedBlockingQueue<>();
    public static void main(String []args){
        ApplicationContext applicationContext=SpringApplication.run(Server.class, args);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Pair<String,Socket> message = Server.messageQueue.take();
                        ServerThread.encryptWrite(message.getLeft(), new DataOutputStream( message.getRight().getOutputStream()));
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
        try{
            server_socket = new ServerSocket(5000);
            while(true){
                Socket socket = server_socket.accept();
                writeLock.lock();
                    socketList.add(socket); //把sock对象加入sock集合
                writeLock.unlock();
                ServerThread ServerThread=new ServerThread(socket);
                applicationContext.getAutowireCapableBeanFactory().autowireBean(ServerThread);//初始化多线程
                ServerThread.start();//启动多线程
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

    //读取并解密

}

