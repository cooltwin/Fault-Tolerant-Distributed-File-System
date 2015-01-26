import java.io.*;
import java.util.*;
import java.net.*;

/**********************************************************************************************
 * 
 * Perform Read and Write operations by check-out and check-in
 *
 */
public class FileManager implements Runnable {
	private Thread t;
	private String threadName = "FileManager"; 
	public static HashMap<String,Integer> fileWriterLock=new HashMap<String,Integer>();
	public static HashMap<String,Integer> fileReaderLock=new HashMap<String,Integer>();
	public static String[] fileList;
	public static String fileName;
	public static LockManager lock;
	int fint;

	FileManager(HashMap<String,Integer> fileListW,HashMap<String,Integer> fileListR,String[] file) throws IOException{
		this.fileWriterLock=fileListW;
		this.fileReaderLock=fileListR;
		this.fileList=file;
		this.lock = new LockManager(fileWriterLock,fileReaderLock);
	}

	public void run() {
		int randomint =0,j=0, rint=0,opno=0;
		boolean exec_state;
		String lock_type;
		//Randomize the operations 
		Random randomGenerator = new Random();
		try{
			t.sleep(10000);
			rint = randomGenerator.nextInt(25);

			if(rint > 0 && rint <= 5)
				t.sleep(1000);
			else if(rint > 5 && rint <= 10)
				t.sleep(10000);
			else if (rint > 10 && rint <=15)
				t.sleep(20000);
			else if (rint > 15 && rint <= 20)
				t.sleep(30000);
			else if (rint > 15 && rint <=25)
				t.sleep(40000);
			StaticVoting.mylogger.info("FileManager : My rint value was : "+rint);

			while(j<2) {
				opno = j+1;
				//Randomly select a file to perform the operation
				fint=randomGenerator.nextInt(fileList.length);
				fileName=fileList[fint];
				randomint = randomGenerator.nextInt(100);
				//if random value is less than 60, then perform read operation, else perform write operation
				if(randomint < 60) {
					StaticVoting.mylogger.info("FileManager : Got a read request on file : "+fileName);
					System.out.println("FileManager : Got a read request on file : "+fileName);
					exec_state = process_request("read");
					lock_type = "read";
				}
				else {
					StaticVoting.mylogger.info("FileManager :Got a write request on file : "+fileName);
					System.out.println("FileManager :Got a write request on file : "+fileName);
					exec_state = process_request("write");
					lock_type = "write";
				}
				if(exec_state == false) { 
					StaticVoting.mylogger.info("FileManager :Stopping execution for :"+opno+" th execution, operation was: "+lock_type);
					System.out.println("FileManager :Stopping execution for :"+opno+" th execution, operation was: "+lock_type);
				}
				else {
					StaticVoting.mylogger.info("FileManager :Successfully completed execution for :"+opno+" th execution, operation was: "+lock_type);
					System.out.println("FileManager :Successfully completed execution for :"+opno+" th execution, operation was: "+lock_type);
				}

				j++;
				t.sleep(5000);
				StaticVoting.mylogger.info("====================================================================================");
				StaticVoting.mylogger.info("====================================================================================");
				System.out.println("====================================================================================");
				System.out.println("====================================================================================");
			}			
			
		}
		catch(Exception e)
		{
			System.out.println("System error");
		}

	}
	
	//Call LockManager to lock the requested file and implements Static Voting algorithm 
	public boolean process_request(String lock_type) throws FileNotFoundException, InterruptedException {
		int  lock_value;
		long startTime, endTime, op_start_time=0, op_end_time=0;
		int max_version=0, total_votes=StaticVoting.votes, flag =0, file_version_no=0;
		String latest_version_holder = new String();
		SyncMsgs syncmsg = new SyncMsgs();

		MessageObject msgobj= null;

		file_version_no = StaticVoting.fileVersion.get(fileName);

		lock_value = lock.TestLock(lock_type,"lock", fileName);
		
		if(lock_value == 1) {
			StaticVoting.mylogger.info("FileManager :Got lock from lock manager for file : "+fileName);
			System.out.println("FileManager :Got lock from lock manager for file : "+fileName);
			msgobj = new MessageObject(StaticVoting.hostName,null, lock_type, 0, file_version_no,  "Vote_Request", true, fileName);	
			syncmsg.set_msg(msgobj);			
		}
		else {
			StaticVoting.mylogger.info("FileManager : Request for lock failed for file : "+fileName+" , didn't get lock from lock manager");
			System.out.println("FileManager : Request for lock failed for file : "+fileName+" , didn't get lock from lock manager");
			//add backoff time code here
			return false;
		}
		startTime = System.currentTimeMillis();
		endTime = System.currentTimeMillis() + 10000;
		msgobj = null;
		
		//Receive the replies from the processes till 10 secs
		while (System.currentTimeMillis() < endTime) {
			msgobj = syncmsg.get_vote_reply_msg();
			if(msgobj != null && msgobj.isBusy == false) {
				if(max_version < msgobj.version_no) {
					max_version = msgobj.version_no;
					latest_version_holder = msgobj.sender_name;
				}
				StaticVoting.mylogger.info("FileManager : Got vote for file : "+fileName+" from host : "+msgobj.sender_name);
				System.out.println("FileManager : Got vote for file : "+fileName+" from host : "+msgobj.sender_name);
				total_votes = total_votes + msgobj.vote;
				StaticVoting.mylogger.info("===============================================================================");
				System.out.println("===============================================================================");
			}
		}
		StaticVoting.mylogger.info("FileManager :max_version is : "+max_version+" and machine with latest file version is : "+latest_version_holder);
		StaticVoting.mylogger.info("FileManager :Total votes received is : "+total_votes);
		System.out.println("FileManager :max_version is : "+max_version+" and machine with latest file version is : "+latest_version_holder);
		System.out.println("FileManager :Total votes received is : "+total_votes);
		//Check if the process gets the read quorum
		if(lock_type.equals("read")) {
			if(total_votes >= StaticVoting.read_quorum_size) {
				StaticVoting.mylogger.info("FileManager :got quorum , now reading the file : "+fileName);
				System.out.println("FileManager :got quorum , now reading the file : "+fileName);
				op_start_time = System.currentTimeMillis();
				StaticVoting.read_file(latest_version_holder, fileName);
			}
			else {
				StaticVoting.mylogger.info("FileManager :Didn't get quorum , failed reading the file : "+fileName);
				System.out.println("FileManager :Didn't get quorum , failed reading the file : "+fileName);
				flag++;
			}
		}
		//Check if the process gets the write quorum
		else {
			if(total_votes >= StaticVoting.write_quorum_size) {
				StaticVoting.mylogger.info("FileManager :got quorum , now writing the file : "+fileName);
				System.out.println("FileManager :got quorum , now writing the file : "+fileName);
				StaticVoting.write_file(latest_version_holder, fileName);
				file_version_no++;
				StaticVoting.fileVersion.put(fileName, file_version_no);
				op_start_time = System.currentTimeMillis();
			}
			else {
				StaticVoting.mylogger.info("FileManager :Didn't get quorum , failed writing the file : "+fileName);
				System.out.println("FileManager :Didn't get quorum , failed writing the file : "+fileName);
				flag++;
			}
		}
		lock_value = lock.TestLock(lock_type, "unlock", fileName);
		//Release lock
		if(lock_value == 1) {
			StaticVoting.mylogger.info("FileManager :Released "+lock_type+" lock to lock Manager for file : "+fileName);
			System.out.println("FileManager :Released "+lock_type+" lock to lock Manager for file : "+fileName);
		}
		else {
			StaticVoting.mylogger.info("FileManager :Released "+lock_type+" lock to lock Manager for file : "+fileName);
			System.out.println("FileManager :Released "+lock_type+" lock to lock Manager for file : "+fileName);
		}
		op_end_time = System.currentTimeMillis();
		if(flag == 0)
			StaticVoting.mylogger.info("Status : Filename : "+fileName+" Operation : "+lock_type+" Start Time : "+op_start_time+" End Time : "+op_end_time);
		MessageObject releasemsg = new MessageObject(StaticVoting.hostName, null, lock_type, 0, file_version_no,  "Vote_Release", false, fileName);	
		syncmsg.set_msg(releasemsg);
		t.sleep(5000);

		if (flag > 0)
			return false;
		else
			return true;
	}


	public void start ()
	{
		if (t == null)
		{
			t = new Thread (this, threadName);
			t.start ();
		}
	}



}
