import java.net.*;
import java.nio.ByteBuffer;
import java.io.*;
import java.util.*;
import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;

/*****
 * Thread to send the control messages to other machines
 * 
 * 
 */
public class MessageSender implements Runnable {
	private Thread t;
	private String threadName = "Client";
	public static final int MESSAGE_SIZE = 1000;
	public static int timestamp = 0;
	public static int status=0;
	int port = 9658;
	//List<String> hosts = new ArrayList<String>();
	ByteBuffer byteBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
	byte[] byteArray;
	String hostName;
	int clientProcessID = 0;
	int countMulticast=0;
	ArrayList<Node> broadcast = new ArrayList<Node>();
	public SctpChannel sctpChannel;
	MessageObject message;

	MessageSender(ArrayList<Node> list, String myHost) throws IOException{
		this.broadcast= list;
		this.hostName = myHost;
	}

	public void run() {		
		SyncMsgs syncmsg = new SyncMsgs();
		try
		{ 
			int newPort = 0;
			while(true) {
				message = syncmsg.get_msg();
				while(message != null){
					if(message.msg_type.equals("Vote_Request"))
					{
						StaticVoting.mylogger.info("MessageSender : Sending vote request msg for file : "+message.file_name);
						byteArray = StaticVoting.serialize(message);
						for(Node item: broadcast){
							if(item != null){
								message.receiver_name = item.hostname;
								ClientSend(item.hostname, item.port_no, byteArray);
							}
						}
						StaticVoting.mylogger.info("===============================================================================");
					}
					else if(message.msg_type.equals("Vote_Reply"))
					{
						StaticVoting.mylogger.info("MessageSender : Sending vote reply msg for file : "+message.file_name);
						byteArray = StaticVoting.serialize(message);
						for(Node item: broadcast){
							if(item.hostname.equals(message.receiver_name)){
								newPort = item.port_no;
							}			   				
						}
						ClientSend(message.receiver_name, newPort, byteArray);
						StaticVoting.mylogger.info("===============================================================================");

					}
					else if(message.msg_type.equals("Vote_Release")) {

						if(SyncMsgs.reprcvqueue.isEmpty())
							StaticVoting.mylogger.info("MessageSender : Error reprcvqueue is empty");
						StaticVoting.mylogger.info("MessageSender : Sending Vote Release msg for file : "+message.file_name);
						while(SyncMsgs.reprcvqueue.peek() != null) {
							MessageObject msgObj = (MessageObject) SyncMsgs.reprcvqueue.poll();
							byteArray = StaticVoting.serialize(message);
							for(Node item: broadcast){
								if(item.hostname.equals(msgObj.sender_name)){
									newPort = item.port_no;
								}
							}
							message.receiver_name = msgObj.sender_name;
							ClientSend(message.receiver_name, newPort, byteArray);
						}
						StaticVoting.mylogger.info("===============================================================================");
					}
					else
						StaticVoting.mylogger.info("MessageSender : Message type is wrong");
					message = syncmsg.get_msg();
				}
			}
		}
		catch(IOException ex)
		{
			System.out.println("Error : Some System failed!!!! ");
		
		}


	}
	public void ClientSend(String host, int port, byte[] msgArray)
	{
		try{
			SocketAddress socketAddress = new InetSocketAddress(host,port);
			StaticVoting.mylogger.info("MessageSender : Sending message to host : "+host+" on port : "+port);
			sctpChannel = SctpChannel.open();
			sctpChannel.bind(new InetSocketAddress(port));
			sctpChannel.connect(socketAddress);
			MessageInfo messageInfo = MessageInfo.createOutgoing(null,0);
			byteBuffer.put(msgArray);
			byteBuffer.flip();
			sctpChannel.send(byteBuffer,messageInfo);
			byteBuffer.clear();
			sctpChannel.close();
			StaticVoting.mylogger.info("MessageSender : Message sent successfully"); 
		}
		catch (IOException e) {
			System.out.println("Error : Some System failed!!!! ");
		}

	}
	public void start ()
	{
		if (t == null)
		{
			StaticVoting.mylogger.info("MessageSender :Starting sender thread" +  threadName );
			t = new Thread (this, threadName);
			t.start ();
		}
	}

}
