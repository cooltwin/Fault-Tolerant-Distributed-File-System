import java.io.*;
import java.util.*;

/****************************************************************************************************8
 * 
 * Class to create a type of Message
 *
 */
class MessageObject implements Serializable{
	String msg_type;
	String sender_name;
	String receiver_name;
	String lock_type;
	int vote;
	int version_no;
	boolean isBusy;
	String file_name;

	
	public MessageObject(String machine_name, String receiver_name, String lock_type, int vote, int version_no, String msg_type, boolean isBusy, String file_name) {
		this.sender_name = machine_name;
		this.receiver_name = receiver_name;
		this.lock_type = lock_type;
		this.vote = vote;
		this.version_no = version_no;
		this.msg_type = msg_type;
		this.isBusy = isBusy;
		this.file_name = file_name;
	}

}

