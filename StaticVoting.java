import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.logging.*;

/************************************************************************************************88
 * 
 * Main class to run the StaticVoting algorithm
 * 
 *
 */
public class StaticVoting {

	
	public static String hostName;
	public static int read_quorum_size;
	public static int write_quorum_size;
	public static int version;
	public static int votes;
	public static LockManager lock;
	public static Logger mylogger;
	public static SyncMsgs smsg;
	public static HashMap<String,Integer> fileListW=new HashMap<String,Integer>();
	public static HashMap<String,Integer> fileListR=new HashMap<String,Integer>();
	public static HashMap<String,Integer> fileVersion=new HashMap<String,Integer>();
	public static String[] file;
	
	//Clear all text files
	public static void ClearFiles(int no_of_files)
	{
		try
		{ 
			for(int i=1; i<=no_of_files; i++)
			{
				String fName = hostName+"/file"+i+".txt";
				File myfile = new File(fName);
				if(myfile.exists())
				{
					PrintWriter writer = new PrintWriter(fName);
					writer.print("");
					//writer.print("This is the demo file"+i+" in "+hostName);
					writer.println();
					writer.close();
				}	
			}

		}
		catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	//Get the port number of receiving machine
	public static int CreateReceiverPort(ArrayList<Node>list)
	{
		//ArrayList<Node> newlist = new ArrayList<Node>();
		int portNo = 0;
		for(Node item : list)
		{
			if(item.hostname.equals(hostName))
			{
				portNo = item.port_no;
			}
		}
		return portNo;
	}

	//List of other machines in the system
	public static ArrayList<Node> CreateSenderList(ArrayList<Node>list)
	{
		ArrayList<Node> newlist = new ArrayList<Node>();
		for(Node item : list)
		{

			if(!(item.hostname.equals(hostName)))
			{
				newlist.add(item);
			}
		}
		return newlist;
	}

	//Read the quorum size
	public static void ReadQuorumSize()
	{
		String temp = new String();
		String tempstring;
		temp = FileOperations.read_file("read quorum","config_file.txt");
		tempstring = temp.substring(temp.indexOf(':')+2);
		read_quorum_size = Integer.parseInt(tempstring);

		temp = "";
		tempstring = "";
		temp = FileOperations.read_file("write quorum","config_file.txt");
		tempstring = temp.substring(temp.indexOf(':')+2);
		write_quorum_size = Integer.parseInt(tempstring);	

		mylogger.info("read quorum size : " + read_quorum_size+ " write quorum size is : "+write_quorum_size);
	}

	//Get the number of votes the machine has from the config file
	public static void ReadRequest(ArrayList<Node>list) throws IOException
		//public static String ReadRequest(MessageObject msg) throws IOException
	{
		for(Node item : list)
		{
			if(item.hostname.equals(hostName))
			{
				votes = item.votes;
			}
		}
		mylogger.info("My votes : "+votes);
	}

	//Serialize the sending bytes
	public static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ObjectOutputStream o = new ObjectOutputStream(b);
		o.writeObject(obj);
		return b.toByteArray();
	}

	//Deserialize the received bytes
	public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream b = new ByteArrayInputStream(bytes);
		ObjectInputStream o = new ObjectInputStream(b);
		return o.readObject();
	}
	
	//Perform read operation
	public static void read_file(String updatedMachine, String fileName){
		if(hostName.equals(updatedMachine))
		{
			FileOperations.PerformRead(hostName+"/"+fileName);
		}
		else
		{
			FileOperations.CopyFileContents(updatedMachine+"/"+fileName, hostName+"/"+fileName);
			FileOperations.PerformRead(hostName+"/"+fileName);

		}

	}
	//Perform write operation
	public static void write_file(String updatedMachine, String fileName){
		if(hostName.equals(updatedMachine))
		{
			FileOperations.PerformWrite(hostName+"/"+fileName);
			write_updated_file(hostName+"/"+fileName);			
		}
		else
		{
			FileOperations.CopyFileContents(updatedMachine+"/"+fileName, hostName+"/"+fileName);
			FileOperations.PerformWrite(hostName+"/"+fileName);
			write_updated_file(hostName+"/"+fileName);

		}

	}
	
	//Update all the machines who sent the vote replies
	public static void write_updated_file(String filepath)
	{
		MessageObject msg;
		if(smsg.writercvqueue.isEmpty())
			StaticVoting.mylogger.info("No replies for write operation");
		while(smsg.writercvqueue.peek() != null)
		{
			msg = (MessageObject) smsg.writercvqueue.poll();
			FileOperations.CopyFileContents(filepath, msg.sender_name+"/"+msg.file_name);
		}
	}
	
	static class logging extends SimpleFormatter {

		public String format(LogRecord record){
			if(record.getLevel() == Level.INFO){
				return record.getMessage() + "\r\n";
			}else{
				return super.format(record);
			}
		}
	}

	public static void set_logging(String hostName) {
		mylogger = Logger.getLogger("MyLog");
		FileHandler fh;
		try {
			fh = new FileHandler("logs/logfile_"+hostName+".log");
			mylogger.addHandler(fh);
			logging formatter = new logging();
			fh.setFormatter(formatter);
			mylogger.setUseParentHandlers(false);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}



	public static void main(String args []) throws IOException, InterruptedException {

		int port;
		ArrayList<Node> list = new ArrayList<Node>();
		ArrayList<Node> listSend = new ArrayList<Node>();
		Random randomGenerator = new Random();
		int failint = randomGenerator.nextInt(100);
		File fileDir = new File("net01.utdallas.edu");
		
		file = fileDir.list();
		FileOperations rc = new FileOperations();
		InetAddress addr = InetAddress.getLocalHost();
		hostName = addr.getHostName();
		set_logging(hostName);
		ClearFiles(file.length);
		mylogger.info("my hostname "+hostName);
		list = rc.find_machine_details();
		listSend = CreateSenderList(list);
		port = CreateReceiverPort(list);
		ReadRequest(list);
		ReadQuorumSize();
		lock = new LockManager();
		lock.readers = 0;
		lock.writers = 0;
		for(int i=0; i<file.length;i++){
			fileListW.put(file[i], lock.writers);
			fileListR.put(file[i], lock.readers);
			fileVersion.put(file[i],1);
		}							
		
		try{

			MessageReceiver R1 = new MessageReceiver(port);
			Thread Receiver = new Thread(R1);
			Receiver.start();
			MessageSender S1 = new MessageSender(listSend, hostName);
			Thread Send = new Thread(S1);
			if(!(failint>50 && failint<55)){
				Send.start();
			}
			else 
				System.out.println("Staticvoting : "+hostName+" had a machine failure, stopping its send operations");
			FileManager F = new FileManager(fileListW,fileListR,file);
			Thread FM = new Thread(F);
			FM.start();
			Receiver.join();
			Send.join();
			FM.join();
			mylogger.info("Program Terminated");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}catch(Exception e){
		}		
	}

}
