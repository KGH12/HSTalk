package package1;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Window.Type;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;


public class Client extends JFrame implements ActionListener {
	final static int ServerPort = 12345;

	private Socket socket = null;
	
	static public DataInputStream is;
	static public DataOutputStream os;

	private Login loginPanel;
	private ChatRoomList chatRoomListPanel;
	private String userName;
	
	public Client() throws IOException {
	
		setupConnection();
		
		addWindowListener(new WindowAdapter() {
			@Override
            public void windowClosing(WindowEvent e) {
                try {
					os.writeInt(Function.CLOSE);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
                dispose();
                System.exit(0); // 프로그램 종료
            }
		});
		
		setTitle("HSTalk");
		setSize(400,600);
		setResizable(false);
		getContentPane().setLayout(null);
		
		
		Container c = getContentPane();
	
		chatRoomListPanel = new ChatRoomList();
		chatRoomListPanel.setBounds(0, 0, 386, 563);
		chatRoomListPanel.setVisible(false);
		c.add(chatRoomListPanel);

		loginPanel = new Login();
		loginPanel.setBounds(0, 0, 386, 563);
		loginPanel.btnLogin.addActionListener(this);
		loginPanel.setVisible(true);
		c.add(loginPanel);
		
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void setupConnection() throws IOException {
		socket = new Socket("localhost", ServerPort);
		System.out.println("Client 연결 완료");
		is = new DataInputStream(socket.getInputStream());
		os = new DataOutputStream(socket.getOutputStream());
	}
	
	public void actionPerformed(ActionEvent e) {
		loginPanel.setVisible(false);
		chatRoomListPanel.setVisible(true);
		userName = loginPanel.textFieldUserName.getText();
		chatRoomListPanel.labelUserName.setText(userName);
		try {
			os.writeUTF(userName);
			os.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println("Client : " + userName);
	}
	
	public static void main(String[] args) throws IOException {
		new Client();
	}
}