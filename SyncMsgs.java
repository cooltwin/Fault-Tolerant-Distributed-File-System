import java.io.*;
import java.util.*;

/***********************************************************************************************************
 * 
 * Class to synchronize threads
 *
 */
class SyncMsgs {
	public static boolean msg_lock = true;
	public static boolean vote_reply_msg_lock = true;
	public static Queue sndqueue = new LinkedList();
	public static Queue rcvqueue = new LinkedList();
	public static Queue reprcvqueue = new LinkedList();
	public static Queue writercvqueue = new LinkedList();
	
	public synchronized void set_msg(MessageObject msgObj) {
		if(msgObj.msg_type.equals("Vote_Request") || msgObj.msg_type.equals("Vote_Reply") || msgObj.msg_type.equals("Vote_Release")) {
			sndqueue.add(msgObj);
			msg_lock = false;
		}
	}
	public synchronized void set_vote_reply_msg(MessageObject msgObj) {
		rcvqueue.add(msgObj);
		reprcvqueue.add(msgObj);
		writercvqueue.add(msgObj);
		
		vote_reply_msg_lock = false;
	}

	public synchronized MessageObject get_vote_reply_msg() {
		MessageObject msgObj;
		if(vote_reply_msg_lock == false) {
			msgObj = (MessageObject)rcvqueue.poll();
			if(rcvqueue.isEmpty())
				vote_reply_msg_lock = true;
			return msgObj;
		}
		return null;
	}
	public synchronized MessageObject get_msg() {
		MessageObject msgObj;
		if(msg_lock == false) {
			msgObj = (MessageObject) sndqueue.poll();
			if(sndqueue.isEmpty())
				msg_lock = true;
			return msgObj;
		}
		return null;
	}
}
