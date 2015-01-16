import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.io.*;
import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;

/***
 * Thread to receive the initial control message
 * 
 * 
 *
 */

public class MessageReceiver implements Runnable {
	private Thread t;
	private String threadName = "Server1";
	public static final int MESSAGE_SIZE = 1000;
	int port = 0;
	int serverProcessID = 0;
	MessageObject msgObj;
	public static SctpChannel sctpChannel;
	public static SctpServerChannel sctpServerChannel;
	ArrayList<Node> list;


	MessageReceiver(int portNo) throws IOException{

		this.port = portNo;
	}


	public void run()
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
		int lock_value = 0;
		MessageObject voteReplymsg = null;
		SyncMsgs syncmsg = new SyncMsgs();

		try
		{
			FileManager fm = new FileManager(StaticVoting.fileListW, StaticVoting.fileListR, StaticVoting.file);
			int version_no=0;
			sctpServerChannel = SctpServerChannel.open();
			InetSocketAddress serverAddr = new InetSocketAddress(port);
			sctpServerChannel.bind(serverAddr);

			while(true)
			{
				//i--;
				sctpChannel = sctpServerChannel.accept();
				MessageInfo messageInfo = sctpChannel.receive(byteBuffer,null,null);
				byte[] byteArray = new byte[byteBuffer.capacity()];
				byteBuffer.rewind();
				byteBuffer.get(byteArray, 0, byteArray.length);;
				msgObj = (MessageObject) StaticVoting.deserialize(byteArray);
				byteBuffer.clear();
				if(msgObj.msg_type.equals("Vote_Request"))
				{
					StaticVoting.mylogger.info("===============================================================================");
					StaticVoting.mylogger.info("MessageReceiver : Received Vote Request Message from : "+msgObj.sender_name+" for file : "+msgObj.file_name) ;
					System.out.println("===============================================================================");
					System.out.println("MessageReceiver : Received Vote Request Message from : "+msgObj.sender_name+" for file : "+msgObj.file_name) ;
					lock_value = fm.lock.TestLock(msgObj.lock_type, "lock", msgObj.file_name);
					if(lock_value == 1) {
						StaticVoting.mylogger.info("MessageReceiver :Received "+msgObj.lock_type+" lock from lock manager, now sending vote reply");
						System.out.println("MessageReceiver :Received "+msgObj.lock_type+" lock from lock manager, now sending vote reply");
						version_no = StaticVoting.fileVersion.get(msgObj.file_name); 
						voteReplymsg = new MessageObject(StaticVoting.hostName, msgObj.sender_name, msgObj.lock_type, StaticVoting.votes, version_no, "Vote_Reply", false, msgObj.file_name);
						syncmsg.set_msg(voteReplymsg);
					}
					else {
						StaticVoting.mylogger.info("MessageReceiver : Failed to send vote reply, didn't get "+msgObj.lock_type+" lock for file : "+msgObj.file_name);
						StaticVoting.mylogger.info("===============================================================================");
						System.out.println("MessageReceiver : Failed to send vote reply, didn't get "+msgObj.lock_type+" lock for file : "+msgObj.file_name);
						System.out.println("===============================================================================");
					}
					//t.sleep(5000);

				}
				if(msgObj.msg_type.equals("Vote_Reply")) {

					StaticVoting.mylogger.info("===============================================================================");
					StaticVoting.mylogger.info("MessageReceiver :Received Vote Reply Message from :"+msgObj.sender_name+" for file : "+msgObj.file_name);
					syncmsg.set_vote_reply_msg(msgObj);
					//t.sleep(5000);
				}
				if(msgObj.msg_type.equals("Vote_Release")) {
					StaticVoting.mylogger.info("===============================================================================");
					StaticVoting.mylogger.info("MessageReceiver : Got a Vote Release msg, from host : "+msgObj.sender_name+" for file : "+msgObj.file_name);
					System.out.println("===============================================================================");
					System.out.println("MessageReceiver : Got a Vote Release msg, from host : "+msgObj.sender_name+" for file : "+msgObj.file_name);	
					if(msgObj.lock_type.equals("write"))
						StaticVoting.fileVersion.put(msgObj.file_name, msgObj.version_no);
					lock_value = fm.lock.TestLock(msgObj.lock_type, "unlock", msgObj.file_name);
					if(lock_value == 1) {
						StaticVoting.mylogger.info("MessageReceiver : Successfully Released "+msgObj.lock_type+" lock for file : "+msgObj.file_name);
						System.out.println("MessageReceiver : Successfully Released "+msgObj.lock_type+" lock for file : "+msgObj.file_name);
					}
					else { 
						StaticVoting.mylogger.info("MessageReceiver : Error Couldn't release : "+msgObj.lock_type+" lock for file : "+msgObj.file_name);
						System.out.println("MessageReceiver : Successfully Released "+msgObj.lock_type+" lock for file : "+msgObj.file_name);
					}
					StaticVoting.mylogger.info("===============================================================================");
					System.out.println("===============================================================================");

				}

			}
		}

		catch(IOException ex)
		{
			ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 

	}

	public void TerminateConnection() throws IOException
	{
		sctpServerChannel.close();
		sctpChannel.close();
	}
	public void start ()
	{
		if (t == null)
		{
			StaticVoting.mylogger.info("MessageReceiver :Starting receiver thread" +  threadName );
			t = new Thread (this, threadName);
			t.start ();
		}
	}

}
