package package1;

import java.util.ArrayList;

public class ChatRoomInfo {
	private int id;
	private String chatRoomName;
	private ArrayList<ServerThread> chatRoomList = new ArrayList<>();
	
	public ChatRoomInfo(int id, String chatRoomName) {
		this.id = id;
		this.chatRoomName = chatRoomName;
	}
	
	public int getId() {
		return id;
	}
	
	public ArrayList<ServerThread> getChatRoomList() {
		return chatRoomList;
	}
}
