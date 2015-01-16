
/*****************************************************************************************
 * Class to create a type of Node
 *
 */
class Node {
	String hostname;
	int port_no;
	int votes;
	Node next;

	public Node() {
	}
	public Node(String hostname, int port_no, int votes) {
		this.port_no = port_no;
		this.hostname = hostname;
		this.votes = votes;
		next = null;
	}
}
