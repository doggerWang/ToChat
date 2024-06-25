package Swing;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;


public class ChatRoom {
  public  String ip = "47.115.213.200";
    //服务端端口
    public  int port = 5000;
    boolean isFilter=false;

    public DataOutputStream output = null;
    public  Socket socket = null;
    public DataInputStream input = null;

    private JPanel panel1;
    private JTextField text;
    private JButton sendButton;
    private JList chatList;
    private JTextField name;
    private JButton quit;
    private JLabel num;
    private JRadioButton day1;
    private JRadioButton day2;
    private JRadioButton day3;
    private JRadioButton day7;
    private JCheckBox filter;
    private JButton saveSettingsButton;
    private JLabel history;
    private DefaultListModel messageListModel;

    public ChatRoom() {
    {
            panel1 = new JPanel();
            GroupLayout layout = new GroupLayout(panel1);
            panel1.setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            chatList = new JList();
            JScrollPane scrollPane = new JScrollPane(chatList);

            text = new JTextField();
            sendButton = new JButton("send");
            name = new JTextField();
            quit = new JButton("quit");
            num = new JLabel("在线人数： 人");
            day1 = new JRadioButton("1day");
            day2 = new JRadioButton("2day");
            day3 = new JRadioButton("3day");
            day7 = new JRadioButton("a week");
            filter = new JCheckBox("显示进入/离开");
            saveSettingsButton = new JButton("Save Settings");
            saveSettingsButton.setForeground(Color.RED);
            JLabel usernameLabel = new JLabel("username");

            ButtonGroup dayGroup = new ButtonGroup();
            dayGroup.add(day1);
            dayGroup.add(day2);
            dayGroup.add(day3);
            dayGroup.add(day7);

            layout.setHorizontalGroup(
                    layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(scrollPane)
                                    .addGroup(layout.createSequentialGroup()
                                            .addComponent(text)
                                            .addComponent(sendButton)))
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(num)
                                    .addGroup(layout.createSequentialGroup()
                                            .addComponent(usernameLabel)
                                            .addComponent(name))
                                    .addGroup(layout.createSequentialGroup()
                                            .addComponent(day1)
                                            .addComponent(day2)
                                            .addComponent(day3)
                                            .addComponent(day7))
                                    .addComponent(filter)
                                    .addComponent(saveSettingsButton)
                                    .addComponent(quit))
            );

            layout.setVerticalGroup(
                    layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(scrollPane)
                                    .addGroup(layout.createSequentialGroup()
                                            .addComponent(num)
                                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                    .addComponent(usernameLabel)
                                                    .addComponent(name))
                                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                    .addComponent(day1)
                                                    .addComponent(day2)
                                                    .addComponent(day3)
                                                    .addComponent(day7))
                                            .addComponent(filter)
                                            .addComponent(saveSettingsButton)
                                            .addComponent(quit)))
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(text)
                                    .addComponent(sendButton))
            );

            // 设置窗口首选大小
            panel1.setPreferredSize(new Dimension(800, 600));  // 根据实际需要调整大小
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ChatRoom chatRoom=new ChatRoom();
        chatRoom.run();
    }
    public void run() throws IOException, InterruptedException {
        JFrame frame = new JFrame("ChatRoom");
        ButtonGroup group = new ButtonGroup();
        group.add(day1);
        group.add(day2);
        group.add(day3);
        group.add(day7);


        // 添加一个按钮来显示当前选择的 JRadioButton
        JButton showSelectionButton = new JButton("Show Selection");
        frame.add(showSelectionButton);

        JLabel selectionLabel = new JLabel();
        frame.add(selectionLabel);
        frame.setContentPane(panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringBuilder stringBuilder=new StringBuilder();
                stringBuilder.append("GHW&").append("Message&").append(name.getText()).append("&").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("&").append(text.getText());
                String send=stringBuilder.toString();
                try {
                    encryptWrite(send,output);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        quit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    encryptWrite(proxyUtil.make("Quit",name.getText(),LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))),output);
                    input.close();
                    output.close();
                    socket.close();
                    System.exit(0);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        saveSettingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int cnt=0;
                if(!filter.isSelected()) cnt+=10;
                if(day1.isSelected()) cnt+=1;
                else if(day2.isSelected()) cnt+=2;
                else if(day3.isSelected()) cnt+=3;
                else if(day7.isSelected()) cnt+=7;
                isFilter=!filter.isSelected();
                messageListModel.clear();
                try {
                    encryptWrite(proxyUtil.make("Ask",name.getText(),LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), String.valueOf(cnt)),output);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });
        showSelectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (day1.isSelected()) {
                    selectionLabel.setText("Selected: Day 1");
                } else if (day2.isSelected()) {
                    selectionLabel.setText("Selected: Day 2");
                } else if (day3.isSelected()) {
                    selectionLabel.setText("Selected: Day 3");
                } else if (day7.isSelected()) {
                    selectionLabel.setText("Selected: Day 7");
                }
            }
        });
        day1.setSelected(true);
        filter.setSelected(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    encryptWrite(proxyUtil.make("Quit",name.getText(),LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))),output);
                    input.close();
                    output.close();
                    socket.close();
                    System.exit(0);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }


            }
        });
        socket=new Socket(ip,port);
        output=new DataOutputStream(socket.getOutputStream());
        input = new DataInputStream(socket.getInputStream());
        messageListModel=new DefaultListModel();
        chatList.setCellRenderer(new CustomListCellRenderer());
        chatList.setModel(messageListModel);
        num.setText("在线人数： 人");
        name.setText("新用户"+new Random().nextInt(1000));
        try {
            encryptWrite(proxyUtil.make("Ask",name.getText(),LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),"0"),output);
            encryptWrite(proxyUtil.make("Enter",name.getText(),LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))),output);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        while(true){
            String receive = readDecrypt(input);
            String[] param = receive.split("&");
            if(param.length<3) continue;
            if(param[1].equals("Enter")&&!isFilter) messageListModel.addElement(param[2]+"进入聊天室");
            else if(param[1].equals("Quit")&&!isFilter) messageListModel.addElement(param[2]+"离开聊天室");
            else if(param[1].equals("Data")) num.setText("在线人数："+param[2]+"人");
            else if(param[1].equals("Message"))messageListModel.addElement(MessageFormat.format("{0}({1}): {2}",param[2],param[3],param[4]));
            System.out.println(receive);
            Thread.sleep(20);
        }

    }

    public static void encryptWrite(String src,DataOutputStream output)throws IOException {
        //将一个字符串转化为字符数组
        //System.out.println(src);
        char[] char_arr = src.toCharArray();
        //加密操作
        for(int i = 0;i<char_arr.length;i++){
            output.writeChar(char_arr[i]+13);
        }
        //用作结束标志符
        output.writeChar(2333);
        output.flush();
    }

    public static String readDecrypt(DataInputStream input)throws IOException{
        String rtn="";
        while(true){
            int char_src =input.readChar();
            if(char_src!=2333){
                rtn=rtn+(char)(char_src-13);
            }else{
                break;
            }
        }
        return rtn;
    }

}


