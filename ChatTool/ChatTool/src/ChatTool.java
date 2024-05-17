import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class ChatTool {
    private static String VERSION = "0.83";
    private JFrame frame;

    // MySQL数据库连接信息
    private String dbUrl = "jdbc:mysql://172.30.15.207:3306/chattool?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private String dbUser = "root";
    private String dbPassword = "root";
    //当前用户
    String userNow;

    // 登录窗口组件
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmpasswordField;
    private JButton loginButton;
    private JButton logoutButton;
    private JButton registButton;
    private JButton regButton;

    // 好友窗口组件
    private JList<String> friendList;
    private JButton openChatButton;

    // 聊天窗口组件
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    //private JButton leaveButton;
    private static final int DELAY = 0;  // 延迟时间，表示立即执行任务
    private static final int PERIOD = 1000;  // 每次执行任务的间隔时间（以毫秒为单位）

    private static int COUNT = 0;//计数 用于刷新记录

    //制表符用于分割消息
    private static final String TABLESPACE = "\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000";
    //用于获取时间
    private static String TIME = "";
    //用于读取账户
    private static String USERNAME = "";
    //用于读取密码
    private static String PASSWORD = "";
    //用于缩进长聊天绘画
    private static final int INDENTATION= 15;
    //退出聊天界面后选择的好友;控制监听 true 就会一直监听 false 会停止监听
    private static boolean ISEXIT = true;
    //定义全局定时任务 方便后期取消停止
    Timer timer;

    // 运行聊天工具
    public void run() {
        // 创建初始窗口
        creatInitWindow();
    }
    public void creatInitWindow() {
        frame = new JFrame("Let‘s Chat v"+ VERSION);
        createLoginWindow();
    }
    // 创建登录窗口
    private void createLoginWindow() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);

        JPanel panel = new JPanel();
        frame.setLocation(650,330);
        panel.setLayout(new GridLayout(3, 2));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        //从文件中获取信息 自动填写 账号密码
        getFile();
        usernameField.setText(USERNAME);
        passwordField.setText(PASSWORD);

        loginButton = new JButton("Login");
        registButton =new JButton("Register");
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();

                String password = new String(passwordField.getPassword());
                if (validateUser(username, password)) {
                    userNow = username;
                    frame.getContentPane().removeAll();
                    createFriendWindow();
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid username or password", "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        registButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.getContentPane().removeAll();
                createRegistWindow();
            }
        });
        panel.add(loginButton);
        panel.add(registButton);
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
    //创建注册窗口
    public void createRegistWindow()
    {
        frame.setTitle("Register v"+ VERSION);
        frame.setSize(300, 200);
        JPanel panel = new JPanel();
        //frame.setLocation(650,330);
        panel.setLayout(new GridLayout(4, 2));

        panel.add(new JLabel("Input Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Input Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        panel.add(new JLabel("Confirm Password"));
        confirmpasswordField = new JPasswordField();
        panel.add(confirmpasswordField);

        regButton = new JButton("Register");
        regButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Register(usernameField.getName(),new String(passwordField.getPassword()),new String(confirmpasswordField.getPassword()));
            }
        });
        panel.add(regButton);
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
    public boolean Register(String username, String password, String confirmpwd){
        if(username == null || password == null){
            JOptionPane.showMessageDialog(frame,"Please input username or password");
        }
       else
        {
            if(!password.equals(confirmpwd)){
            JOptionPane.showMessageDialog(frame,"Passwords does not match!");
        }
            else if(isExist(username)){
                JOptionPane.showMessageDialog(frame,"用户名已存在");
            }
        }
        return false;
    }

    public boolean isExist(String username){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            String query = "SELECT * FROM users WHERE username=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            boolean isExist = rs.next();
            rs.close();
            stmt.close();
            conn.close();
            return isExist;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    // 验证用户登录
    private boolean validateUser(String username, String password) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            String query = "SELECT * FROM users WHERE username=? and password=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            boolean isValid = rs.next();
            rs.close();
            stmt.close();
            conn.close();
            return isValid;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // 创建好友窗口
    private void createFriendWindow() {
        ArrayList<String> usernameList = new ArrayList<>();
        frame.setTitle("select friend v"+ VERSION);
        frame.setSize(300, 400);
        //获取好友列表
        try {
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            String query = "select username from users where username != '"+userNow+"'";
            //System.out.println(query);
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String username = rs.getString("username");
                usernameList.add(username);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // 好友列表
        DefaultListModel<String> friendListModel = new DefaultListModel<>();
        for (String username : usernameList) {
            friendListModel.addElement(username);
            //System.out.println(username);
        }
//        friendListModel.addElement("Friend 1");
//        friendListModel.addElement("Friend 2");
//        friendListModel.addElement("Friend 3");
        friendList = new JList<>(friendListModel);

        // 打开聊天窗口按钮
        openChatButton = new JButton("Chat it");
        logoutButton = new JButton("Log Out");
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int isLogout = JOptionPane.showConfirmDialog(frame,"Sure to Log out?","Logout",JOptionPane.YES_NO_OPTION );
                if(isLogout == 0) {
                    frame.getContentPane().removeAll();
                    createLoginWindow();
                }
            }
        });
        openChatButton.addActionListener(new ActionListener() {
                                             public void actionPerformed(ActionEvent e) {
                                                 String selectedFriend = friendList.getSelectedValue();
                                                 if (selectedFriend != null) {
                                                     frame.getContentPane().removeAll();
                                                     //night 1130 ok
                                                     //System.out.println(selectedFriend);
                                                     createChatWindow(selectedFriend);
                                                     ISEXIT = true;

                                                 } else {
                                                     JOptionPane.showMessageDialog(frame, "Select a friend to chat with", "Chat",
                                                             JOptionPane.INFORMATION_MESSAGE);
                                                 }
                                             }
                                         }
        );

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JScrollPane(friendList), BorderLayout.CENTER);
        panel.add(openChatButton, BorderLayout.NORTH);
        panel.add(logoutButton, BorderLayout.SOUTH);
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    // 创建聊天窗口
    private void createChatWindow(String friend) {
        //night 1130 ok
        //System.out.println("3"+friend);
        frame.setTitle("Chat with " + friend +" v"+ VERSION);
        frame.setSize(800, 700);
        frame.setLocation(400,80);
        // 聊天记录
        chatArea = new JTextArea();
        Font font = new Font("SimSun", Font.PLAIN, 14); // 设置字体为Arial，大小为14
        chatArea.setFont(font);
        chatArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(chatArea);

        // 消息输入框
        messageField = new JTextField();
        messageField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = messageField.getText();
                sendMessage(friend, message);
                messageField.setText("");
            }
        });

        // 发送按钮
        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = messageField.getText();
                sendMessage(friend, message);
                messageField.setText("");
            }
        });

        JPanel panel = new JPanel();
        //leaveButton = new JButton("leave");
        panel.setLayout(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(messageField, BorderLayout.NORTH);
        panel.add(sendButton, BorderLayout.SOUTH);
        //panel.add(leaveButton, BorderLayout.SOUTH);

        frame.getContentPane().add(panel);
        frame.setVisible(true);

        //Timer timer = new Timer();
        timer = new Timer();
        //new Timer().scheduleAtFixedRate(new TimerTask() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //TODO add if
                //timer.cancel();
                if(ISEXIT){
                    //System.out.println(friend);
                    getTime();
                    int count = 0;
                    String query = "select COUNT(*) from chathistory where sender = '"+friend+"' and receiver='"+userNow+"';";
                    //1130 night test
                    //System.out.println(query);
                    try (Connection connection = DriverManager.getConnection(dbUrl,dbUser , dbPassword);
                         Statement statement = connection.createStatement();
                         ResultSet resultSet = statement.executeQuery(query)) {

                        if (resultSet.next()) {
                            count = resultSet.getInt("count(*)");
                        }
                        connection.close();
                        resultSet.close();
                        statement.close();
                        //System.out.println(COUNT);
                        if (COUNT < count ) {
                            COUNT = count;
                            //System.out.println(COUNT);
                            //frame.setState(Frame.NORMAL);
                            // 添加窗口状态改变的监听器
                            frame.addWindowStateListener(new WindowStateListener() {
                                @Override
                                public void windowStateChanged(WindowEvent e) {
                                    // 获取窗口当前的状态
                                    int state = e.getNewState();

                                    // 判断窗口是否最小化 弹窗通知 未实现
//                                if ((state & Frame.ICONIFIED) != 0) {
//                                } else {
//                                    messageIsComing();
//                                }
                                }
                            });

                            sendMessage(friend, "");  // 调用发送消息的方法

                        }else{
                            COUNT = count;
                            //System.out.println(COUNT);
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    //sendMessage(friend, "");  // 调用发送消息的方法
                }

            }
        }, DELAY, PERIOD);

    }

    // 发送消息
    private void sendMessage(String friend, String message) {
        System.out.println(friend);
        long timestamp = System.currentTimeMillis();
        // 创建一个 SimpleDateFormat 对象，用于定义日期格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 将时间戳转换为 Date 对象
        Date date = new Date(timestamp);
        // 使用 SimpleDateFormat 格式化 Date 对象为字符串
        String formattedDate = dateFormat.format(date);
        // 这里可以将消息保存到数据库中，以便后续展示聊天记录等功能
        // 建立数据库连接
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            chatArea.setText("");
            //String sql = "SELECT * FROM chathistory WHERE sender = '"+userNow+"'and receiver = '"+friend+"';";
            String sql = "SELECT * FROM chathistory WHERE " +
                    "(sender = '"+userNow+"' AND receiver = '"+friend+"') OR (sender = '"+friend+"' AND receiver = '"+userNow+"')";

            // 创建查询语句
            try (Statement statement = connection.createStatement()) {
                // 执行查询并获取结果集
                try (ResultSet resultSet = statement.executeQuery(sql)) {
                    // 遍历结果集并输出字段值
                    while (resultSet.next()) {
                        String fieldValue1 = resultSet.getString("sender");
                        String fieldValue2 = resultSet.getString("receiver");
                        String fieldValue3 = resultSet.getString("message_content");
                        String fieldValue4 = resultSet.getString("message_time");
                        //String newString = fieldValue1+"->"+fieldValue2+":"+fieldValue3+"("+fieldValue4+")"+"\n";
                        if(friend.equals(fieldValue2)){
                            String newString = TABLESPACE+fieldValue1+":"+IndentationString(fieldValue3,INDENTATION)+"\n"+TABLESPACE+"("+fieldValue4+")"+"\n";
                            //String newString = fieldValue1+":"+fieldValue3+"\n("+fieldValue4+")"+"\n";
                            chatArea.append(newString);
                        }else{
                            String newString = fieldValue1+":"+IndentationString(fieldValue3,0)+"\n("+fieldValue4+")"+"\n";
                            chatArea.append(newString);
                        }
                        //System.out.println(fieldValue);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //chatArea.append("\t-------------以前的聊天记录-------------");
        chatArea.append(TABLESPACE);
        chatArea.append(userNow+":"+IndentationString(message,INDENTATION) + "\n"+TABLESPACE+"("+formattedDate+") " );
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
        // 建立数据库连接
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            // 创建 SQL 插入语句的 PreparedStatement 对象，并将参数占位符指定为 ?
            String sql = "INSERT INTO chathistory (sender,receiver,message_content,message_time) VALUES (?,?,?,?)";
            PreparedStatement statement = connection.prepareStatement(sql);

            // 设置参数值
            String sender = userNow;
            String receiver = friend;
            String message_conntent = message;
            //如果输入特定字符推出到上一级
            if("原神启动".equals(message_conntent)){
                if (frame != null) {
                    ISEXIT = false;
                    frame.getContentPane().removeAll();//移除当前窗口的内容面板上的所有组件
                    frame.dispose();// 关闭当前窗口
                    createFriendWindow(); // 调用 createFriendWindow() 方法
                    friend = "";
                    timer.cancel();
                    return ;
                }
            }
            if("".equals(message_conntent)){
                System.out.println(TIME+"来自"+friend+"的消息");
                int[] a = {1,5};
                PlaySound(a);
            }else{
                statement.setString(1, sender);
                statement.setString(2, receiver);
                statement.setString(3, message_conntent);
                statement.setString(4, formattedDate);
                // 执行更新操作
                int rowsInserted = statement.executeUpdate();

                if (rowsInserted > 0) {
                    System.out.println(TIME+"发送"+userNow+"的消息");
                    //System.out.println(userNow+"`s message is posted");
                } else {
                    System.out.println("fail to post");
                }
                statement.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    public static void messageIsComing(){
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(170, 100);
        frame.setAlwaysOnTop(true); // 设置窗口置顶
        // 获取屏幕的尺寸
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // 设置窗口的位置为屏幕右下角
        int x = screenSize.width - frame.getWidth();
        int y = screenSize.height - frame.getHeight();
        frame.setLocation(x, y+20);
        //frame.setLocationRelativeTo(null); // 居中显示

        JLabel label = new JLabel("You Have A Message！");
        label.setFont(new Font("Arial", Font.PLAIN, 15));

        //label.setHorizontalAlignment(SwingConstants.CENTER);

        frame.add(label);
        javax.swing.Timer timer = new javax.swing.Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // 关闭窗口
            }
        });
        timer.setRepeats(false); // 仅触发一次
        timer.start();

        frame.setVisible(true);
        //frame.setFocusableWindowState(false);
    }
    public static void getTime(){
        long timestamp = System.currentTimeMillis();
        // 创建一个 SimpleDateFormat 对象，用于定义日期格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 将时间戳转换为 Date 对象
        Date date = new Date(timestamp);
        // 使用 SimpleDateFormat 格式化 Date 对象为字符串
        TIME = dateFormat.format(date);
    }
    public static void getFile(){
        String filePath = "AccessFile";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            USERNAME = reader.readLine(); // Read the first line
            PASSWORD = reader.readLine(); // Read the second line
        } catch (IOException e) {
            // File reading error
            System.out.println("检测读取账户密码文件不存在，请创建文件以便存储账户密码");
        }
    }
    public static int[] ChangeSound(int[] input){
        int [] frequency = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            //int[] frequencys = {261,293,329,349,392,440,493,523};
            if(1==input[i]){
                frequency[i] = 261;
            }else if (2==input[i]) {
                frequency[i] = 293;
            }else if (3==input[i]) {
                frequency[i] = 329;
            }else if (4==input[i]) {
                frequency[i] = 349;
            }else if (5==input[i]) {
                frequency[i] = 392;
            }else if (6==input[i]) {
                frequency[i] = 440;
            }else if (7==input[i]) {
                frequency[i] = 493;
            }else if (11==input[i]) {
                frequency[i] = 523;
            } else if (0==input[i]) {
                frequency[i] = 0;
            }
        }
        return frequency;
    }
    public static void PlaySound(int[] soundNumber){
        //int a[] = {1,2,3,4,5,6,7,11};
        int[] frequency = ChangeSound(soundNumber);// 频率（赫兹）
        try {
            // 设置提示音的频率和持续时间
            int duration = 2000; // 持续时间（毫秒）
            //int repeatCount = 7; // 循环播放次数

            // 获取默认的音频输出设备
            Clip clip = AudioSystem.getClip();

            for (int i = 0; i < frequency.length; i++) {
//                System.out.print(i+"\t");
//                System.out.print(frequency[i]);
                // 创建一个简单的音频样式，使用正弦波作为提示音

                byte[] buffer = new byte[(int) clip.getFormat().getFrameRate() * duration / 1000];
                for (int j = 0; j < buffer.length; j++) {
                    double angle = 2.0 * Math.PI * frequency[i] * j / clip.getFormat().getFrameRate();
                    buffer[j] = (byte) (Math.sin(angle) * 127);
                }

                // 打开音频流并播放
                clip.open(clip.getFormat(), buffer, 0, buffer.length);
                clip.start();

                // 等待提示音播放完毕
                Thread.sleep(500);
                //System.out.println();
                // 关闭音频流
                clip.stop();
                clip.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String IndentationString(String str,int indentation){
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            stringBuilder.append(str.charAt(i));
            if(indentation==0){
                if ((i + 1) % INDENTATION == 0) {
                    stringBuilder.append("\n");
                }
            }else {
                if ((i + 1) % INDENTATION == 0) {
                    stringBuilder.append("\n"+TABLESPACE);
                }
            }

        }

        str = stringBuilder.toString();
        return str;
    }

    public static void main(String[] args) {
        System.out.println("ChatTool Test @ Version "+VERSION.toString());
        ChatTool chatTool = new ChatTool();
        chatTool.run();
    }
}