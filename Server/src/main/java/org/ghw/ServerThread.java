//BIO时为每个连接分配的线程。在NIO模式下不用。
//package org.ghw;
//
//import org.apache.commons.lang3.tuple.MutablePair;
//import org.apache.ibatis.javassist.compiler.ast.Pair;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.stereotype.Component;
//
//import javax.lang.model.type.ArrayType;
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.net.Socket;
//import java.net.SocketException;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.Iterator;
//
//@Component
//class ServerThread extends Thread{
//    @Autowired
//    MessageDao messageDao;
//    Socket socket = null;
//    ServerThread(){}
//
//    ServerThread(Socket s){//初始化
//        this.socket=s;
//    }
//    public void run() {
//        DataInputStream input = null;
//        DataOutputStream output = null;
//        try {
//            if(socket.isClosed()) return;
//            input = new DataInputStream(socket.getInputStream());
//            String receive = null;
//            String send = null;
//            while (true) {//监视当前客户端有没有发来消息
//                if (!socket.isClosed()) {
//                    try {
//                        receive = readDecrypt(input);
//                    }
//                    catch (SocketException s){
//                        socket.close();
//                    }
//                    String[] param = receive.split("&");
//                   if(analyze(receive,param))break;
//                }
//            }
//        } catch (IOException e) {
//              e.printStackTrace();
//        }
//    }
//
//    public static void broadcast(String receive) throws IOException {
//        Server.readLock.lock();
//        for(Socket socket: Server.socketList){ //遍历socke集合
//            //把读取到的消息发送给各个客户端
//            if(!socket.isClosed()){
//                Server.messageQueue.add(new MutablePair<>(receive,socket));
//            }
//        }
//        Server.readLock.unlock();
//    }
//
//    public  void singlecast(String receive) throws IOException {
//        Server.readLock.lock();
//        Server.messageQueue.add(new MutablePair<>(receive,socket));
//
//        Server.readLock.unlock();
//    }
//    public  boolean analyze(String receive,String []param) throws IOException {
//        DataOutputStream output;
//        DataInputStream input;
//        if(param.length<5||!param[0].equals("GHW")) return false;
//        Message message=new Message(null,param[1],param[2],param[4],param[3]);
//        if(!message.getType().equals("Ask")){
//            broadcast(receive);
//            messageDao.insert(message);
//        }
//                if(message.getType().equals("Quit")){
//                   try {
//                       input = new DataInputStream(socket.getInputStream());
//                       output = new DataOutputStream(socket.getOutputStream());
//                       input.close();
//                       output.close();
//                       socket.close();
//                       return true;
//                   }
//                   catch (SocketException s){
//                       socket.close();
//                   }
//
//                }
//                if(message.getType().equals("Ask")){
//                int days=Integer.parseInt(message.getText());
//                ArrayList<Message>messages=messageDao.select();
//                Iterator<Message> iterator=messages.iterator();
//                if(days>=10)
//                    while(iterator.hasNext()){
//                        if(!iterator.next().getType().equals("Message")) iterator.remove();
//                    }
//
//                if(days==0) days=7;
//                LocalDateTime start=LocalDateTime.now().minusDays(days);
//                    for(Message m:messages){
//                        if(start.isBefore(LocalDateTime.parse(m.getTime(),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
//                        singlecast(proxyUtil.make(m.getType(),m.getName(),m.getTime(),m.getText()));
//                    }
//                }
//
//
//            return false;
//        }
//
//
//    public static void encryptWrite(String src,DataOutputStream output)throws IOException {
//        //将一个字符串转化为字符数组
//        //System.out.println(src);
//        char[] char_arr = src.toCharArray();
//        //加密操作
//        for(int i = 0;i<char_arr.length;i++){
//            output.writeChar(char_arr[i]+13);
//        }
//        //用作结束标志符
//        output.writeChar(2333);
//        output.flush();
//    }
//
//
//    public static String readDecrypt(DataInputStream input)throws IOException{
//        String rtn="";
//        while(true){
//            int char_src =input.readChar();
//            if(char_src!=2333){
//                rtn=rtn+(char)(char_src-13);
//            }else{
//                break;
//            }
//        }
//        return rtn;
//    }
//}