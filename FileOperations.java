import java.io.*;
import java.util.*;

/********************************************************************************
 * 
 * Perform all the File operations
 *
 */
class FileOperations {
	
	//Read the files
	public static String read_file(String search_string, String filename){
		String currentLine = null;
		try {
			File config_file = new File(filename);
			Scanner scanner = new Scanner(config_file);
			while(scanner.hasNextLine()) {
				currentLine = scanner.nextLine();
				if(currentLine.indexOf(search_string) >= 0) {
					return currentLine ;
				}
			}
			scanner.close();
		}catch(FileNotFoundException e)  {
			e.printStackTrace();
		}
		return null;
	}
	
	//Read all the processes
	public ArrayList<Node> find_machine_details() {
		ArrayList<Node> list = new ArrayList<Node>();
		String temp, tempstring, hostname;
		int no_of_process, port, votes;
		String fileName = "config_file.txt";
		String temparr[] = new String[3];
		Node machine_detail = null;		
		temp = read_file("Total no of process", fileName);
		tempstring = temp.substring(temp.indexOf(':')+2);
		no_of_process = Integer.parseInt(tempstring);
		for(int j = 1; j<=no_of_process; j++) {
			if(j<10)
				temp = read_file("net0"+j, fileName);
			else 
				temp = read_file("net"+j, fileName);
			temparr = temp.split("\t");
			hostname = temparr[0];
			port = Integer.parseInt(temparr[1]);
			votes = Integer.parseInt(temparr[2]);
			machine_detail = new Node(hostname, port, votes);
			list.add(machine_detail);
						
		}
		return list;
	}
	
	//Copy the contents of file to another
	public static void CopyFileContents(String source, String destination)
	{
		try {

			InputStream in = new FileInputStream(new File(source));
			OutputStream out = new FileOutputStream(new File(destination));
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//File Read operation
	public static void PerformRead(String fileName)
	{		
		String str;
		StaticVoting.mylogger.info("Reading file ");
		StaticVoting.mylogger.info("*************************************************************************************");
		System.out.println("Reading file ");
		System.out.println("*************************************************************************************");
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			while ((str = in.readLine()) != null) {
				StaticVoting.mylogger.info(str);
				System.out.println(str);
			}
			in.close();
		StaticVoting.mylogger.info("*************************************************************************************");
		System.out.println("*************************************************************************************");
		} catch (IOException e) {

			e.printStackTrace();
		}

	}
	
	//File Write operation
	public static void PerformWrite(String fileName)
	{
		Random randomGenerator = new Random();
		int rint = randomGenerator.nextInt(100);
		StaticVoting.mylogger.info("Writing file");
		StaticVoting.mylogger.info("*************************************************************************************");
		StaticVoting.mylogger.info("This is host : "+StaticVoting.hostName+" writing value : "+rint+" to file ");
		System.out.println("Writing file");
		System.out.println("*************************************************************************************");
		System.out.println("This is host : "+StaticVoting.hostName+" writing value : "+rint+" to file ");
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
			out.println("This is host : "+StaticVoting.hostName+" writing value : "+rint+" to file");
			out.close();
		StaticVoting.mylogger.info("*************************************************************************************");
		System.out.println("*************************************************************************************");
		} catch (IOException e) {

			e.printStackTrace();
		}

	}
}
