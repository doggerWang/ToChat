package org.ghw;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;

import static org.ghw.ServerThread.broadcast;
import static org.ghw.ServerThread.encryptWrite;

@Component
public class MyTask {
    @Scheduled(fixedDelay = 5000)
    public static  void task() throws IOException {
        int num=0;
        Server.writeLock.lock();
            Iterator<Socket>iterator=Server.socketList.iterator();
            while(iterator.hasNext()){
                Socket s=iterator.next();
                if(s.isClosed()) iterator.remove();
                else num++;
            }
        Server.socketList.trimToSize();
      Server.writeLock.unlock();
      if(num!=0)
      broadcast(proxyUtil.make("Data",num));
    }
}
