package package1;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;

public class ChatRoomList extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private DataInputStream is = Client.is;
	private DataOutputStream os = Client.os;

	private ArrayList<ChattingRoom> chattingRoomList = new ArrayList<>(); // 내가 참여중인 채팅방 목록

	private DefaultListModel listModel;
	private JList list;
	protected JLabel labelUserName;

	public ChatRoomList() throws IOException {
		setBackground(new Color(2, 210, 255));

		setSize(386, 563);
		setLayout(null);

		JLabel label1 = new JLabel("채팅방 리스트");
		label1.setBounds(34, 34, 89, 32);
		add(label1);

		listModel = new DefaultListModel();
		list = new JList(listModel);
		list.setFont(new Font("나눔고딕", Font.PLAIN, 18));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setBounds(34, 149, 316, 377);
		add(list);

		//방 만들기 버튼 이미지 버튼으로 생성
		ImageIcon createRoomIcon = new ImageIcon("data/chat.png");
		
		JButton btnCreateRoom = new JButton(createRoomIcon);
		btnCreateRoom.addActionListener(this);
		
		//테두리 없애는 코드
		btnCreateRoom.setBorderPainted(false);
		
		//이미지에 크기를 맞추는 코드
        btnCreateRoom.setBounds(280, 37, createRoomIcon.getIconWidth(), createRoomIcon.getIconHeight());
		add(btnCreateRoom);

		// 채팅창 항목을 더블클릭했을때
		// 채팅창을 켜고
		// 서버에게 방에 접속했음을 알려주고
		// chattingRoomList에 추가한다.
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = list.locationToIndex(e.getPoint());
					String roomName = (String) listModel.getElementAt(index);
					System.out.println("Double-clicked on: " + "방이름 : " + listModel.getElementAt(index) + " 방 인덱스 : " + index);
					try {
						os.writeInt(Function.JOINROOM);
						os.writeInt(index);
						os.flush();
						ChattingRoom cr = new ChattingRoom(index, roomName); // 채팅방 생성 + 열기
						chattingRoomList.add(cr); // 내가 참여중인 채팅방 목록에 추가한다.
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		labelUserName = new JLabel();
		labelUserName.setBackground(new Color(128, 255, 255));
		labelUserName.setBounds(49, 92, 89, 23);
		add(labelUserName);

		// 채팅창 생성 여부, 채팅 메시지를 받아서 처리하는 스레드
		Thread thread = new Thread(new Runnable() {
			boolean active = true;
			@Override
			public void run() {
				while (active) {	// active가 false가 되면 스레드 종료.
					try {
						int protocol = is.readInt();
						switch (protocol) {
						case Function.CREATEROOM: { // 다른 클라이언트가 방을 생성했다고 한다.
							String roomName = is.readUTF(); // 채팅방 이름을 받고
							listModel.addElement(roomName); // JList에 채팅방 목록을 추가하자.
							break;
						}
						case Function.JOINROOM: {	//클라이언트에게 어느 사용자가 접속했는지 표시
							int roomId = is.readInt();
							String userName = is.readUTF();
							
							for (ChattingRoom chatRoom : chattingRoomList) {
								if (chatRoom.getChatRoomId() == roomId) {
									
									SimpleAttributeSet style = new SimpleAttributeSet();
							        StyleConstants.setAlignment(style, StyleConstants.ALIGN_CENTER);
							        StyleConstants.setBackground(style, new Color(164, 190, 215));
							        chatRoom.doc.setParagraphAttributes(chatRoom.doc.getLength(), 1, style, false);
									try {
										chatRoom.doc.insertString(chatRoom.doc.getLength(), userName + "님이 입장하였습니다.\n", style);
									} catch (BadLocationException e) {
										e.printStackTrace();
									}
									chatRoom.textPaneChatView
											.setCaretPosition(chatRoom.textPaneChatView.getDocument().getLength());
								}
							}
							break;
						}
						case Function.CHAT: {	//다른 클라이언트가 보낸 메세지를 채팅방에 참여한 모든 클라이언트에게 표시
							int roomId = is.readInt();
							String userName = is.readUTF();
							String msg = is.readUTF();
							String time = is.readUTF();

							for (ChattingRoom chatRoom : chattingRoomList) {
								if (chatRoom.getChatRoomId() == roomId) {
									
									//남이 쓴 채팅 흰색배경처리
									SimpleAttributeSet style = new SimpleAttributeSet();
							        StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);
							        StyleConstants.setBackground(style, Color.WHITE);
							        StyleConstants.setFontSize(style, 14);
							        
							        //아이디, 시간은 배경없이 보내기 위한 구분
							        SimpleAttributeSet infoStyle = new SimpleAttributeSet();
							        StyleConstants.setAlignment(infoStyle, StyleConstants.ALIGN_LEFT);
							        StyleConstants.setFontSize(infoStyle, 10);
							        
							        chatRoom.doc.setParagraphAttributes(chatRoom.doc.getLength(), 1, style, false);
									try {
										chatRoom.doc.insertString(chatRoom.doc.getLength(), userName + "    " + time + "\n", infoStyle);
										chatRoom.doc.insertString(chatRoom.doc.getLength(), " " + msg + " " + "\n", style);
									} catch (BadLocationException e) {
										e.printStackTrace();
									}
									chatRoom.textPaneChatView
											.setCaretPosition(chatRoom.textPaneChatView.getDocument().getLength());
								}
							}
							break;
						}
						case Function.IMAGE: { 					// 클라이언트에게 사진을 저장하도록 하고, 클라이언트가 저장중인 사진을 출력하도록.
							int roomId = is.readInt();
							String userName = is.readUTF();
							String imagePathName = is.readUTF();
							String time = is.readUTF();
							int imageSize = is.readInt();
							
							byte[] inputBuffer = new byte[imageSize];
				            is.readFully(inputBuffer, 0, imageSize);
				            
				            FileOutputStream fos = new FileOutputStream(imagePathName);
				            fos.write(inputBuffer, 0, imageSize);
				            fos.close();

				            
				            File imageFile = new File(imagePathName);
							
				            for (ChattingRoom chatRoom : chattingRoomList) {
								if (chatRoom.getChatRoomId() == roomId) {
									chatRoom.displayImage(imageFile, "LEFT", userName, time);
									chatRoom.textPaneChatView.setCaretPosition(chatRoom.doc.getLength());
									try {
										chatRoom.doc.insertString(chatRoom.doc.getLength(), "\n", null);
									} catch (BadLocationException e) {
										e.printStackTrace();
									}
								}
							}
							break;
						}
						case Function.EMOT: { 					// 클라이언트에게 이모티콘 출력
							int roomId = is.readInt();
							String userName = is.readUTF();
							String EmotPathName = is.readUTF();
							String SoundPathName = is.readUTF();
							String time = is.readUTF();
							
				            for (ChattingRoom chatRoom : chattingRoomList) {
								if (chatRoom.getChatRoomId() == roomId) {
									
									chatRoom.displayEmoticon(EmotPathName, "LEFT", userName, time);
									chatRoom.playSound(SoundPathName);
									chatRoom.textPaneChatView.setCaretPosition(chatRoom.doc.getLength());
									try {
										chatRoom.doc.insertString(chatRoom.doc.getLength(), "\n", null);
									} catch (BadLocationException e) {
										e.printStackTrace();
									}
								}
							}
							break;
						}
						case Function.WHISPER: {				// 지정한 클라이언트에게만 메세지를 보내는 귓속말 출력
							int roomId = is.readInt();
							String userName = is.readUTF();
							String msg = is.readUTF();
							String time = is.readUTF();
							String targetName = is.readUTF();

							for (ChattingRoom chatRoom : chattingRoomList) {
								if (chatRoom.getChatRoomId() == roomId) {
									
									SimpleAttributeSet style = new SimpleAttributeSet();
							        StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);
							        StyleConstants.setBackground(style, Color.WHITE);
							        StyleConstants.setFontSize(style, 14);
							        
							        SimpleAttributeSet infoStyle = new SimpleAttributeSet();
							        StyleConstants.setAlignment(infoStyle, StyleConstants.ALIGN_LEFT);
							        StyleConstants.setFontSize(infoStyle, 10);
							        
							        chatRoom.doc.setParagraphAttributes(chatRoom.doc.getLength(), 1, style, false);
									try {
										chatRoom.doc.insertString(chatRoom.doc.getLength(), userName + " -> "  + targetName + "  :  " + time + "\n", infoStyle);
										chatRoom.doc.insertString(chatRoom.doc.getLength(), " " + msg + " " + "\n", style);
									} catch (BadLocationException e) {
										e.printStackTrace();
									}
									chatRoom.textPaneChatView
											.setCaretPosition(chatRoom.textPaneChatView.getDocument().getLength());
								}
							}
							break;
						}
						case Function.LIST: {					//전체 사용자 목록을 표시하는 기능
							int roomId = is.readInt();
						    String roomName = is.readUTF();
						    String time = is.readUTF();
						    int numUsers = is.readInt(); // 사용자 수를 받음

						    // 사용자 목록을 전달할 리스트 생성
						    List<String> userList = new ArrayList<>();
						    for (int i = 0; i < numUsers; i++) {
						        String userName = is.readUTF(); // 사용자 이름을 받음
						        userList.add(userName);
						    }
						    
						    String userL = null;;
						    for (int i = 0; i < userList.size(); i++) {
						        userL = userList.get(i);
						    }
						    
						    for (ChattingRoom chatRoom : chattingRoomList) {
								if (chatRoom.getChatRoomId() == roomId) {
									
									SimpleAttributeSet style = new SimpleAttributeSet();
							        StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);
							        chatRoom.doc.setParagraphAttributes(chatRoom.doc.getLength(), 1, style, false);
									try {
										chatRoom.doc.insertString(chatRoom.doc.getLength(), "<System> : " + time + "\n", style);
										
										for (int i = 0; i < userList.size(); i++) {
											chatRoom.doc.insertString(chatRoom.doc.getLength(), (i+1) + ". " + userList.get(i) + "\n", style);
										}
										
									} catch (BadLocationException e) {
										e.printStackTrace();
									}
									chatRoom.textPaneChatView
											.setCaretPosition(chatRoom.textPaneChatView.getDocument().getLength());
								}
							}

						    // userList를 활용하여 사용자 목록을 표시하는 코드를 작성
						    
						    
						    break;
						}
						case Function.CLOSE: {
							active = false;
							break;
						}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		thread.start();

	}

	// 방만들기 버튼 눌렀을때
	public void actionPerformed(ActionEvent e) {
		String chatRoomName = JOptionPane.showInputDialog(null, "방 제목 입력", "입력", JOptionPane.PLAIN_MESSAGE);
		int jListSize = listModel.getSize();
		try {
			os.writeInt(Function.CREATEROOM);
			os.writeInt(jListSize); // 현재 JList의 크기가 0이면 생성할 방의 id를 0으로 설정하는 것.
			os.writeUTF(chatRoomName);
			os.flush();
			if (chatRoomName != null) {
				listModel.addElement(chatRoomName);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
