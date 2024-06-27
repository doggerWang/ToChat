package org.ghw;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.Iterator;



@Component
public class MyTask {
    @Scheduled(fixedDelay = 5000)
    public static  void task() throws IOException {
        int num=0;
        Server.writeLock.lock();
            Iterator<SocketChannel>iterator=Server.socketChannels.iterator();
            while(iterator.hasNext()){
                SocketChannel s=iterator.next();
                if(!s.isOpen()) iterator.remove();
                else num++;
            }
        Server.socketList.trimToSize();
      Server.writeLock.unlock();
      if(num!=0)
      Server.broadcast(proxyUtil.make("Data",num));
    }
}
