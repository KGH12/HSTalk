package package1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class Login extends JPanel  {

	private static final long serialVersionUID = 1L;
	JTextField textFieldUserName;
	JButton btnLogin;
	private Image backgroundImage;
	

	public Login() {
		
		
		setSize(386, 563);
		setLayout(null);
		
		textFieldUserName = new JTextField();
		
		//추가
		textFieldUserName.setHorizontalAlignment(SwingConstants.CENTER);
		
		textFieldUserName.setBounds(146, 369, 96, 21);
		add(textFieldUserName);
		textFieldUserName.setColumns(10);
		
		
		JLabel labelUserName = new JLabel("대화명");
		labelUserName.setBounds(87, 369, 47, 20);
		add(labelUserName);
		
		btnLogin = new JButton("접속");
		btnLogin.setBounds(146, 420, 96, 23);
		add(btnLogin);
		
		//배경 이미지 설정
		backgroundImage = new ImageIcon("data/Login.png").getImage();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int imgWidth = backgroundImage.getWidth(null);
        int imgHeight = backgroundImage.getHeight(null);

        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), 0, 0, imgWidth, imgHeight, null);
    }
}
