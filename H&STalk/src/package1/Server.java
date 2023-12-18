package package1;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Server  extends JFrame  {
	
	private JTextField portTextField;
	private JButton startButton;
	private JButton stopButton;
	private JTextArea logTextArea;
	private ServerSocket ssocket;

	static ArrayList<ServerThread> allServerList = new ArrayList<>();
	static ArrayList<ChatRoomInfo> chatRoomList = new ArrayList<>();
	static ArrayList<String> userList = new ArrayList<>();
	
	static int imageCount = 0;
	
	
	
	public Server() {
        // GUI 구성 요소 초기화
        portTextField = new JTextField("12345");
        startButton = new JButton("서버 시작");
        stopButton = new JButton("서버 종료");
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);

        // 레이아웃 설정
        JPanel panel = new JPanel();
        panel.add(new JLabel("포트:"));
        panel.add(portTextField);
        panel.add(startButton);
        panel.add(stopButton);

        add(panel, "North");
        add(new JScrollPane(logTextArea), "Center");

        // 액션 리스너 설정
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopServer();
            }
        });

        // 프레임 속성 설정
        setTitle("채팅 서버");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void startServer() {
        try {
            int port = Integer.parseInt(portTextField.getText());
            ssocket = new ServerSocket(port);

            log("서버가 포트 " + port + "에서 시작되었습니다.");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Socket s = ssocket.accept();
                            log("클라이언트가 연결되었습니다!");

                            
                            // 클라이언트 처리를 위한 나머지 서버 코드
                			DataInputStream is = new DataInputStream(s.getInputStream());
                			DataOutputStream os = new DataOutputStream(s.getOutputStream());

                			String userName = is.readUTF();
                			Server.userList.add(userName);
//                			System.out.println("Server : hello " + userName);
                			log(userName + "님이 접속하였습니다!");

                			ServerThread thread = new ServerThread(s, userName, is, os);
                			allServerList.add(thread);
                			thread.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        } catch (IOException | NumberFormatException ex) {
            log("서버 시작 중 오류 발생: " + ex.getMessage());
        }
    }

    private void stopServer() {
        try {
            if (ssocket != null && !ssocket.isClosed()) {
                ssocket.close();
                log("서버가 중지되었습니다.");
            } else {
                log("서버가 실행 중이 아닙니다.");
            }
        } catch (IOException e) {
            log("서버 중지 중 오류 발생: " + e.getMessage());
        }
    }

    private void log(String message) {
        logTextArea.append(message + "\n");
    }
    
    
    
    	
	
	public static void main(String[] args) throws IOException {
		new Server();
	}
}

class ServerThread extends Thread {
	private String userName;
	final DataInputStream is;
	final DataOutputStream os;
	Socket s;
	boolean active = true;

	public ServerThread(Socket s, String userName, DataInputStream is, DataOutputStream os) {
		this.is = is;
		this.os = os;
		this.userName = userName;
		this.s = s;
	}

	@Override
	public void run() {
		while (active) { // active가 false가 되면 스레드 끝.
			try {
				int protocol = is.readInt();
				switch (protocol) {
				case Function.CREATEROOM: {
					int roomId = is.readInt();
					String chatRoomName = is.readUTF();
					Server.chatRoomList.add(new ChatRoomInfo(roomId, chatRoomName));

					for (ServerThread t : Server.allServerList) { // 모든 클라이언트에게 채팅방이 생성되었음을 알려주자.
						if (!t.equals(this)) { // 본인에게는 알려줄 필요 없으니
							t.os.writeInt(Function.CREATEROOM);
							t.os.writeUTF(chatRoomName);
							t.os.flush();
						}
					}
					break;
				}
				case Function.JOINROOM: {
					int chatRoomId = is.readInt(); // 몇번방으로 들어갔는지 받아주고
					for (ChatRoomInfo cri : Server.chatRoomList) { // 모든 채팅방중에서
						if (cri.getId() == chatRoomId) { // 들어간 채팅방을 찾아서
							cri.getChatRoomList().add(this); // 채팅방의 서버 스레드 array에 넣어주자
							
							for (ServerThread t : cri.getChatRoomList()) { // 그 채팅방의 서버스레드들에게
								if (!t.equals(this)) { 			// 본인에게는 보내줄 필요 없으니..
									t.os.writeInt(Function.JOINROOM);
									t.os.writeInt(chatRoomId); // 해당 채팅방 id와
									t.os.writeUTF(this.userName); // 유저 이름을 보내줌
									t.os.flush();
								}
							}
						}
					}
					
					break;
				}
				case Function.CHAT: {
					int chatRoomId = is.readInt(); // 메시지 보낸 채팅방의 id를 받고
					String roomName = is.readUTF();	// 방제목 받고
					String message = is.readUTF(); // 메시지도 받고
					String time = is.readUTF(); // 보낸 시간도 받고
					
					// 추가한 부분
					try (BufferedWriter writer = new BufferedWriter(new FileWriter("txts/" + roomName + "_" + chatRoomId + ".txt", true))) {
			            writer.write("[" + time + "] " + this.userName + " :" + message);
			            writer.newLine();
			        } catch (IOException e) {
			            e.printStackTrace();
			        }

					for (ChatRoomInfo cri : Server.chatRoomList) { // 채팅방들 중에서
						if (cri.getId() == chatRoomId) { // 메시지를 보낸 채팅방의 id와 같은 채팅방을 찾고
							for (ServerThread t : cri.getChatRoomList()) { // 그 채팅방의 서버스레드들에게
								if (!t.equals(this)) { // 본인에게는 보내줄 필요 없으니..
									t.os.writeInt(Function.CHAT);
									t.os.writeInt(chatRoomId); // 해당 채팅방 id와
									t.os.writeUTF(this.userName); // 유저 이름과
									t.os.writeUTF(message); // 메시지와
									t.os.writeUTF(time); // 메시지 보낸시간을 보내준다.
									t.os.flush();
								}
							}
						}
					}
					break;
				}
				case Function.IMAGE: {
					int chatRoomId = is.readInt();
					String roomName = is.readUTF();	// 추가함
					String time = is.readUTF(); // 추가함
					int imageSize = is.readInt();
					byte[] inputBuffer = new byte[imageSize];
		            is.readFully(inputBuffer, 0, imageSize);
		            
		            // 추가한 부분
					try (BufferedWriter writer = new BufferedWriter(new FileWriter("txts/" + roomName + "_" + chatRoomId + ".txt", true))) {
			            writer.write("[" + time + "] " + this.userName + " : (이미지)");
			            writer.newLine();
			        } catch (IOException e) {
			            e.printStackTrace();
			        }

		            
		            String imagePathName = "images/image" + Server.imageCount + ".jpg";
		            Server.imageCount++;
		            FileOutputStream fos = new FileOutputStream(imagePathName);
		            fos.write(inputBuffer, 0, imageSize);
		            fos.close();
			        
			        
		            File imageFile = new File(imagePathName);
		            
		            FileInputStream fis = new FileInputStream(imageFile);
		            byte[] outputBuffer = new byte[(int) imageFile.length()];
		            fis.read(outputBuffer);
		            
					for (ChatRoomInfo cri : Server.chatRoomList) { // 채팅방들 중에서
						if (cri.getId() == chatRoomId) { // 메시지를 보낸 채팅방의 id와 같은 채팅방을 찾고
							for (ServerThread t : cri.getChatRoomList()) { // 그 채팅방의 서버스레드들에게
								if (!t.equals(this)) { // 본인에게는 보내줄 필요 없으니..
									t.os.writeInt(Function.IMAGE);
									t.os.writeInt(chatRoomId);
									t.os.writeUTF(this.userName);
									t.os.writeUTF(imagePathName);
									t.os.writeUTF(time);
									t.os.writeInt(outputBuffer.length);
									t.os.write(outputBuffer, 0, outputBuffer.length);
									t.os.flush();
								}
							}
						}
					}
					break;
				}
				case Function.EMOT: {
					int chatRoomId = is.readInt();
					String roomName = is.readUTF();	// 추가함
					String time = is.readUTF(); // 추가함
					String emotPath = is.readUTF(); //이모티콘 경로 받음
					String soundPath = is.readUTF(); //소리기능 포함된 이모티콘일 경우 소리파일경로까지 받음
					
					try (BufferedWriter writer = new BufferedWriter(new FileWriter("txts/" + roomName + "_" + chatRoomId + ".txt", true))) {
			            writer.write("[" + time + "] " + this.userName + " :" + "(이모티콘)");
			            writer.newLine();
			        } catch (IOException e) {
			            e.printStackTrace();
			        }
					
					for (ChatRoomInfo cri : Server.chatRoomList) { 
						if (cri.getId() == chatRoomId) { 
							for (ServerThread t : cri.getChatRoomList()) { 
								if (!t.equals(this)) { 
									t.os.writeInt(Function.EMOT);
									t.os.writeInt(chatRoomId);
									t.os.writeUTF(this.userName);
									t.os.writeUTF(emotPath);
									t.os.writeUTF(soundPath);
									t.os.writeUTF(time);
									t.os.flush();
								}
							}
						}
					}
					break;
				}
				case Function.WHISPER: {
					int chatRoomId = is.readInt(); 
					String roomName = is.readUTF();
					String targetName = is.readUTF(); // 대상 아이디 수신
					String wMessage = is.readUTF(); // 귓속말 수신
					String time = is.readUTF();
					
					try (BufferedWriter writer = new BufferedWriter(new FileWriter("txts/" + roomName + "_" + chatRoomId + ".txt", true))) {
			            writer.write("[" + time + "] " + this.userName + " -> " + targetName + "  :  " + wMessage);
			            writer.newLine();
			        } catch (IOException e) {
			            e.printStackTrace();
			        }

					for (ChatRoomInfo cri : Server.chatRoomList) {
						if (cri.getId() == chatRoomId) {
							for (ServerThread t : cri.getChatRoomList()) {
								if (!t.equals(this)) { // 본인에게는 보내줄 필요 없음
									if(t.userName.equals(targetName)) {// 대상 아이디(유저)에게만 메세지 보내게끔 설정
										t.os.writeInt(Function.WHISPER);
										t.os.writeInt(chatRoomId);
										t.os.writeUTF(this.userName);
										t.os.writeUTF(wMessage);
										t.os.writeUTF(time);
										t.os.writeUTF(targetName);
										t.os.flush();
									}
								}
							}
						}
					}
					break;
				}
				case Function.LIST: {
					int chatRoomId = is.readInt();
					String roomName = is.readUTF();
					String time = is.readUTF();

					// 사용자 목록을 전달할 리스트 생성
				    List<String> userList = new ArrayList<>();
				    
					for (ChatRoomInfo cri : Server.chatRoomList) {
						if (cri.getId() == chatRoomId) {
							for (ServerThread t : cri.getChatRoomList()) {
								userList.add(t.userName); // 채팅방에 들어와 있는 사용자를 리스트에 추가
				            }
						}
					}
					
					// 사용자 목록을 전송
				    os.writeInt(Function.LIST);
				    os.writeInt(chatRoomId);
				    os.writeUTF(roomName);
				    os.writeUTF(time);
				    os.writeInt(userList.size()); // 사용자 수를 전송
				    for (String user : userList) {
				        os.writeUTF(user); // 각 사용자 이름을 전송
				    }
				    os.flush();
				    
					break;
				}

				case Function.CLOSE: {
					active = false;
					os.writeInt(Function.CLOSE);
					s.close();
					is.close();
					os.close();
					break;
				}
				}
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}
}
