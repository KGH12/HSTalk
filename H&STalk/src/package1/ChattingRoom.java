package package1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.text.*;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.print.attribute.AttributeSet;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.border.LineBorder;

public class ChattingRoom extends JFrame implements ActionListener {

	private DataInputStream is = Client.is;
	private DataOutputStream os = Client.os;

	private int chatRoomId;
	private String roomName;

	private JScrollPane scrollPane;
	protected JTextField textFieldInputMessage;
	protected JTextPane textPaneChatView;
	Login login = new Login();

	StyledDocument doc;

	public int getChatRoomId() {
		return chatRoomId;
	}

	//채팅방 화면, 기능구현
	public ChattingRoom(int chatRoomId, String roomName) throws IOException {
		getContentPane().setBackground(new Color(255, 255, 255)); // 현재 채팅창의 id를 담는다.
		this.chatRoomId = chatRoomId;
		this.roomName = roomName;

		JLabel lbRoomName = new JLabel(roomName);
		
		setTitle(roomName);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(400, 600);
		getContentPane().setLayout(null);

		textPaneChatView = new JTextPane();
		textPaneChatView.setBackground(new Color(186, 206, 224));
		textPaneChatView.setEditable(false);

		//	스타일드 문서 가져오기
		doc = textPaneChatView.getStyledDocument();
		
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setAlignment(style, StyleConstants.ALIGN_CENTER);
        StyleConstants.setFontFamily(style, "나눔고딕");
        
        //	스타일 배경 음영처리
        StyleConstants.setBackground(style, new Color(164, 190, 215));
        
        doc.setParagraphAttributes(doc.getLength(), 1, style, false);
		try {
			doc.insertString(doc.getLength(), "입장 완료\n", style);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
		scrollPane = new JScrollPane(textPaneChatView);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(12, 45, 362, 357);
		getContentPane().add(scrollPane);

		textFieldInputMessage = new JTextField();
		textFieldInputMessage.setBounds(12, 412, 362, 88);
		getContentPane().add(textFieldInputMessage);
		textFieldInputMessage.setColumns(10);
		textFieldInputMessage.addActionListener(this);

		JButton btnSend = new JButton("전송");
		btnSend.setBounds(311, 510, 63, 43);
		btnSend.addActionListener(this);
		btnSend.setBackground(Color.YELLOW);
		btnSend.setBorder(new LineBorder(new Color(255, 230, 0), 2));
		getContentPane().add(btnSend);

		
		//이모티콘 이미지 버튼 구현
		ImageIcon ImageIcon = new ImageIcon("data/emotImage.png");
		JButton btnEmoticon = new JButton(ImageIcon);
		btnEmoticon.setBorderPainted(false);
		btnEmoticon.setBounds(57, 510, ImageIcon.getIconWidth(), ImageIcon.getIconHeight());
		
		//이모티콘 버튼 기능 구현
		btnEmoticon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame emoticonFrame = new JFrame();
				emoticonFrame.setTitle("이모티콘 선택");
				emoticonFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				for (int i = 1; i <= 16; i++) {
			        String emoticonPath = "emoticon/emot" + i + ".png";  //이모티콘 경로 설정
			        String soundPath = "emoticon/emot" + i + ".wav";  //사운드 효과 경로 설정

			        //이모티콘 이미지 파일을 크기에 맞춰 스케일링
			        ImageIcon emoticonIcon = new ImageIcon(new ImageIcon(emoticonPath).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
			        JButton emoticonButton = new JButton(emoticonIcon);

			        emoticonButton.addActionListener(new ActionListener() {
			            @Override
			            public void actionPerformed(ActionEvent e) {
			                try {
			                	LocalDateTime currentTime = LocalDateTime.now();
			            		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a h:mm");
			            		String time = currentTime.format(formatter);
			            		
			                	displayEmoticon(emoticonPath, "RIGHT", "나", time);
			                	playSound(soundPath);
			                	sendEmoticon(emoticonPath, soundPath);
			                	
							} catch (Exception e1) {
								e1.printStackTrace();
							}

			                emoticonFrame.dispose();
			            }
			        });

			        emoticonFrame.getContentPane().add(emoticonButton);
			    }
				// 이모티콘창 인터페이스 설정
		        emoticonFrame.getContentPane().setLayout(new GridLayout(4, 4));
		        emoticonFrame.setSize(400, 400);
		        emoticonFrame.setLocationRelativeTo(null);
		        emoticonFrame.setVisible(true);
			}
		});
		getContentPane().add(btnEmoticon);
		

		//	이미지 보내는 기능 구현
		ImageIcon FileIcon = new ImageIcon("data/fileImage.png");
		JButton btnUploadImage = new JButton(FileIcon);
		btnUploadImage.setBorderPainted(false);
		btnUploadImage.setBounds(12, 510, FileIcon.getIconWidth(), FileIcon.getIconHeight());
		
		btnUploadImage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 파일 선택 다이얼로그 생성
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter("이미지 파일", "jpg", "jpeg", "png", "gif"));

				int result = fileChooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					// 선택된 파일 처리 (예: 이미지 표시)
					File selectedFile = fileChooser.getSelectedFile();
					try {
						LocalDateTime currentTime = LocalDateTime.now();
	            		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a h:mm");
	            		String time = currentTime.format(formatter);
	            		
						displayImage(selectedFile, "RIGHT", "나", time);
						sendImage(selectedFile);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		getContentPane().add(btnUploadImage);

		setLocationRelativeTo(null);
		setVisible(true);

	}

	//	메세지 전송 기능 구현
	public void actionPerformed(ActionEvent evt) {
		String message = textFieldInputMessage.getText();
		textFieldInputMessage.setText("");
		LocalDateTime currentTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a h:mm");
		String formattedTime = currentTime.format(formatter);
		
		String targetUser = null;
		String whisperMessage = null;

		try {
	        if (message.startsWith("/w")) {
	            // 사용자가 귓속말 명령어를 입력한 경우
	            String[] parts = message.split(" ", 3);
	            if (parts.length == 3) {
	                targetUser = parts[1];
	                whisperMessage = parts[2];

	                os.writeInt(Function.WHISPER);
	                os.writeInt(chatRoomId);
	                os.writeUTF(roomName);
	                os.writeUTF(targetUser);
	                os.writeUTF(whisperMessage);
	                os.writeUTF(formattedTime);
	                os.flush();
	            } else {
	                // 잘못된 명령어 형식일 경우 처리 ( 기능 제대로 작동 안하는중 수정 예정 ) 
	            	SimpleAttributeSet style = new SimpleAttributeSet();
	                StyleConstants.setAlignment(style, StyleConstants.ALIGN_CENTER);
	                doc.setParagraphAttributes(doc.getLength(), 1, style, false);
	                try {
	        			doc.insertString(doc.getLength(), "잘못된 입력입니다.", style);
	        		} catch (BadLocationException e) {
	        			e.printStackTrace();
	        		}
	            }
	        } else if (message.equals("/list")) {
	        	// 사용자가 전체 유저 확인 명령어를 입력한 경우
	        	os.writeInt(Function.LIST);
                os.writeInt(chatRoomId);
                os.writeUTF(roomName);
                os.writeUTF(formattedTime);
                os.flush();
	        } else {
	            // 귓속말이 아닌 경우 일반 채팅 메시지를 보냅니다.
	            os.writeInt(Function.CHAT);
	            os.writeInt(chatRoomId);
	            os.writeUTF(roomName);
	            os.writeUTF(message);
	            os.writeUTF(formattedTime);
	            os.flush();
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		
		
		
		
		//	채팅창에 메세지 표시, 자신이 보낸 메세지 노란색 배경으로
		SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setAlignment(style, StyleConstants.ALIGN_RIGHT);
        StyleConstants.setBackground(style, Color.YELLOW);
        StyleConstants.setFontSize(style, 14);
        doc.setParagraphAttributes(doc.getLength(), 1, style, false);
        
        //	시간, 자신 or 다른 클라이언트 정보만 담긴 메세지 작게
        SimpleAttributeSet infoStyle = new SimpleAttributeSet();
        StyleConstants.setAlignment(infoStyle, StyleConstants.ALIGN_RIGHT);
        StyleConstants.setFontSize(infoStyle, 10);
        doc.setParagraphAttributes(doc.getLength(), 1, infoStyle, false);
        
        
		try {
			if (message.startsWith("/w")) { // 귓속말시 본인에게 보여야 되는 상태 구분
				doc.insertString(doc.getLength(), formattedTime + "    " + "나" + " -> " + targetUser + "\n", infoStyle);
				doc.insertString(doc.getLength(), whisperMessage + "\n", style);
			} else {
				doc.insertString(doc.getLength(), formattedTime + "    " + "나" + "\n", infoStyle);
				doc.insertString(doc.getLength(), " " + message + " " + "\n", style);
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		textPaneChatView.setCaretPosition(textPaneChatView.getDocument().getLength());
	}

	//	이미지를 채팅창 화면에 표시하는 함수
	public void displayImage(File imageFile, String align, String userName, String time) {
		
		try {
				// 이미지를 표시하는 코드 작성
				// 여기서는 JLabel을 사용하여 이미지를 표시하는 예제를 보여줍니다.
				ImageIcon originalIcon = new ImageIcon(imageFile.getAbsolutePath());
				Image scaledImage = originalIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
				ImageIcon scaledIcon = new ImageIcon(scaledImage);

				// 스타일을 생성하고 정렬을 설정
				SimpleAttributeSet style = new SimpleAttributeSet();
				StyleConstants.setAlignment(style, "RIGHT".equals(align) ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);

				// 텍스트를 삽입하고 스타일을 설정
				doc.setParagraphAttributes(doc.getLength(), 1, style, false);
				textPaneChatView.setCaretPosition(doc.getLength());
				String text = ("RIGHT".equals(align) ? time + "    나" : userName + "  :  " + time) + "\n";
				doc.insertString(doc.getLength(), text, style);
	           
				// 이미지를 삽입하고 바로 개행을 추가하여 텍스트가 다음 줄에 오도록 함
				// 이미지를 삽입하기 전에 문단 스타일을 설정
				doc.setParagraphAttributes(doc.getLength(), 1, style, false);
				textPaneChatView.setCaretPosition(doc.getLength()); // 커서 위치를 설정
				textPaneChatView.insertComponent(new JLabel(scaledIcon));
				doc.insertString(doc.getLength(), "\n", null); // 이모티콘 후에 개행 문자를 삽입

				// 컴포넌트와 텍스트 삽입 후 문단 스타일을 리셋
				SimpleAttributeSet resetStyle = new SimpleAttributeSet();
				StyleConstants.setAlignment(resetStyle, StyleConstants.ALIGN_LEFT);
				doc.setParagraphAttributes(doc.getLength(), 1, resetStyle, false);
	           
				// 스크롤을 최신 위치로 이동
				textPaneChatView.setCaretPosition(doc.getLength());
	       } catch (BadLocationException e) {
	           	e.printStackTrace();
	       }
	}
	
	//	서버에 이미지정보를 보내는 함수
	private void sendImage(File imageFile) throws Exception {
		FileInputStream fis = new FileInputStream(imageFile);
		byte[] buffer = new byte[(int) imageFile.length()];
        fis.read(buffer);
        
        LocalDateTime currentTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a h:mm");
		String formattedTime = currentTime.format(formatter);
		
		try {
			os.writeInt(Function.IMAGE);
			os.writeInt(chatRoomId);
			os.writeUTF(roomName);
			os.writeUTF(formattedTime);
			os.writeInt(buffer.length);
            os.write(buffer, 0, buffer.length);
            os.flush();		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//	채팅창에 이모티콘을 표시하는 함수
	public void displayEmoticon(String emotPath, String align, String userName, String time) { //이모티콘 화면제어 함수
		try {
	           ImageIcon emot = new ImageIcon(emotPath);
	           Image scaledImage = emot.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
	           ImageIcon scaledIcon = new ImageIcon(scaledImage);

	           SimpleAttributeSet style = new SimpleAttributeSet();
	           StyleConstants.setAlignment(style, "RIGHT".equals(align) ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);

	           doc.setParagraphAttributes(doc.getLength(), 1, style, false);
	           textPaneChatView.setCaretPosition(doc.getLength());
	           String text = ("RIGHT".equals(align) ? time + "    나" : userName + "  :  " + time) + "\n";
	           doc.insertString(doc.getLength(), text, style);
	           
	           doc.setParagraphAttributes(doc.getLength(), 1, style, false);
	           textPaneChatView.setCaretPosition(doc.getLength());
	           textPaneChatView.insertComponent(new JLabel(scaledIcon));
	           doc.insertString(doc.getLength(), "\n", null);

	           SimpleAttributeSet resetStyle = new SimpleAttributeSet();
	           StyleConstants.setAlignment(resetStyle, StyleConstants.ALIGN_LEFT);
	           doc.setParagraphAttributes(doc.getLength(), 1, resetStyle, false);
	           
	           textPaneChatView.setCaretPosition(doc.getLength());
	       } catch (BadLocationException e) {
	           e.printStackTrace();
	       }
	}
	
	//	서버에 선택한 이모티콘 정보를 보내는 함수
	private void sendEmoticon(String emoticonPath, String soundPath) { //서버에게 선택한 이모티콘 전달함수
		String emotPath = emoticonPath; // 이모티콘 이미지 파일 경로 설정
		
		LocalDateTime currentTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a h:mm");
		String formattedTime = currentTime.format(formatter);
		
		try {
			os.writeInt(Function.EMOT);
			os.writeInt(chatRoomId);
			os.writeUTF(roomName);
			os.writeUTF(formattedTime);
			os.writeUTF(emotPath);
			os.writeUTF(soundPath);
            os.flush();		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	//	이모티콘의 소리기능 추가 함수(소리티콘)
	public void playSound(String soundPath) {
		File soundFile = new File(soundPath);
		
		// 해당 파일이 존재하지 않으면 함수 실행안함
	    if (!soundFile.exists()) {
	        return;
	    }
		
		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		} catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}

}
