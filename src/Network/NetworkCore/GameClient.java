package Network.NetworkCore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.swing.JOptionPane;

import Global.Constants;
import Network.InnerData.WaitingRoom.WaitingRoomInfo;
import Network.Objects.User;

public class GameClient {
	private Socket socket;
	private PrintWriter writer;
	private User user;
	
	private WaitingRoomInfo RoomInfo = new WaitingRoomInfo("","");
	
	
	public Socket getSocket() {
		return socket;
	}
	
	public WaitingRoomInfo getRoomInfo() {
		return this.RoomInfo;
	}
	
	public void connect(User user, String IP, String password) {
		socket = new Socket();
		
		this.user = user;
		try {
			socket.connect(new InetSocketAddress(IP, NetworkTag.DEFAULT_SERVER_PORT));
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			System.out.println("CLIENT: CONNECT SUCCESS");
			
			if(password.equals(""))password = NetworkTag.EMPTY_STRING;
			sendMessageToServer(NetworkTag.PARTICIPATE+"|"+user.toString()+"|"+password);
			
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			//CLIENT <- SERVER
			new Thread(new Runnable() {

				@Override
				public void run() {
					String response = null;
					try {
						while(true){
							 response = reader.readLine();
							 Constants.ff.cprint("CLIENT <- SERVER : "+response);
							
							 String[] tokens = response.split("\\|");
							 
							 String tag = tokens[0];
							 String[] msg = Arrays.copyOfRange(tokens, 1, tokens.length);
							 
							 if(tag.equals(NetworkTag.WAITING_ROOM)) {
								 RoomInfo.fromMsg(msg);
								 
							 }else if(tag.equals(NetworkTag.PASSWORD_NOT_CORRECT)) {
								 JOptionPane.showMessageDialog(null, "패스워드가 틀렸습니다.");
							     System.exit(0);
							 }
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					try {
						reader.close();
						writer.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
			}).start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, "해당 ip에 접속할 수 없습니다.");
			e.printStackTrace();
			return;
			
		}
	}
	
	public void sendMessageToServer(String msg){
		//클라이언트 -> 서버
		msg = user.getUserName()+"|" +msg;
		Constants.ff.cprint("CLIENT -> SERVER : "+msg);
		writer.println(msg);
		writer.flush();
	}
}
