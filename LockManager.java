import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;

/**********************************************************************************************************
 * 
 * Create locks on the file  
 *
 */
public class LockManager {
	public static RandomAccessFile file = null;
	public static int readers;
	public static int writers;
	public static HashMap<String,Integer> fileWriter=new HashMap<String,Integer>();
	public static HashMap<String,Integer> fileReader=new HashMap<String,Integer>();
	public static String fileN;

        public LockManager() {
	}

	public LockManager(HashMap<String,Integer> fileWriterLock,HashMap<String,Integer> fileReaderLock){
		this.fileWriter=fileWriterLock;
		this.fileReader=fileReaderLock;
	}

	//Locks for read operation
	public static synchronized int lockRead(String fileN) {
		if(fileWriter.get(fileN) > 0 ){
			StaticVoting.mylogger.info("LockManager : Failed to get read lock, no of readers are : "+readers);
			return 0;
		}
		int s=fileReader.get(fileN);
		s++;
		fileReader.put(fileN, s);

		StaticVoting.mylogger.info("LockManager: Received read lock successfully, no of readers: "+readers);
		return 1;
	}

	public static synchronized void unlockRead(String fileN){
		StaticVoting.mylogger.info("LockManager: Released read lock successfully, no of readers: "+readers);
		int s=fileReader.get(fileN);
		s--;
		fileReader.put(fileN, s);


	}
//Locks for write operations
	public synchronized static int lockWrite(String fileN) {

		if(fileReader.get(fileN) > 0 || fileWriter.get(fileN) > 0){
			StaticVoting.mylogger.info("LockManager : Failed to get write lock, no of writers are : "+readers);
			return 0;
		}
		int s=fileWriter.get(fileN);
		s++;
		fileWriter.put(fileN, s);

		StaticVoting.mylogger.info("LockManager: Received write lock successfully, no of readers: "+readers);
		return 1;
	}

	public static synchronized void unlockWrite(String fileN) {
		StaticVoting.mylogger.info("LockManager: Released write lock successfully, no of readers: "+readers);
		int s=fileWriter.get(fileN);
		s--;
		fileWriter.put(fileN, s);


	}

	// Check for Read-Read, Read-Write and Write-Write Locks
	static public int TestLock(String operation,String request, String fName) throws FileNotFoundException{
		int flag=0;
		if(operation.equals("read")){
			if(request.equals("lock")){
				int l=lockRead(fName);
				if(l==1){
					StaticVoting.mylogger.info("read");
					flag++;
				}

			}
			else if(request.equals("unlock")){
				unlockRead(fName);
				flag = 1;

			}
		}

		else if(operation.equals("write"))
		{
			if(request.equals("lock")){
				int l=lockWrite(fName);
				if(l==1){
					StaticVoting.mylogger.info("write");
					flag++;

				}

			}
			else if(request.equals("unlock")){
				unlockWrite(fName);
				flag=1;
			}
		}
		return(flag);
	}
}



