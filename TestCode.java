
import java.io.*;
import java.util.*;
import java.util.logging.*;

class OpDetail {
	public String FileName;
	public String Operation;
	public long startTime;
	public long endTime;
	public String logfile_name;
	public int opid ;

	public OpDetail(String File,String op,long sTime,long eTime, String logfile, int operation_id){
		FileName=File;
		Operation=op;
		startTime=sTime;
		endTime=eTime;
		logfile_name = logfile;
		opid= operation_id;

	}
}

public class TestCode {
	public static int tempid=0;
	public static int no_of_file = 0;
	public static String filename[];
	public static HashMap<String,ArrayList> fileOp=new HashMap<String,ArrayList>();


	public static void read_log_file(String logfile){
		String currentLine = null, op_type = null, file_name = null, start_time=null, end_time=null;
		OpDetail opdetail = null;
		ArrayList<OpDetail> temp_op = new ArrayList<OpDetail>();
		try {
			File config_file = new File("logs/"+logfile);
			Scanner scanner = new Scanner(config_file);
			while(scanner.hasNextLine()) {
				currentLine = scanner.nextLine();
				if(currentLine.indexOf("Status") >= 0) {
					file_name = currentLine.substring(currentLine.indexOf("Filename :")+11, currentLine.indexOf(" O"));
					op_type = currentLine.substring(currentLine.indexOf("Operation :")+12, currentLine.indexOf(" S"));
					start_time = currentLine.substring(currentLine.indexOf("Start Time :")+13, currentLine.indexOf(" E"));
					end_time = currentLine.substring(currentLine.indexOf("End Time :")+11);
					tempid++;
					opdetail = new OpDetail(file_name, op_type, Long.parseLong(start_time), Long.parseLong(end_time), logfile, tempid);
					temp_op = fileOp.get(file_name);
					temp_op.add(opdetail);
					fileOp.put(file_name, temp_op);

				}
			}
			scanner.close();
		}catch(FileNotFoundException e)  {
			e.printStackTrace();
		}
	}
	public static void print_hash_map() {
		ArrayList<OpDetail> temp_op = new ArrayList<OpDetail>();
		for(int i = 0; i< no_of_file; i++) {
			System.out.println("=========================================================================================================");
			System.out.println("Printing Operation Details for file : "+filename[i]);
			temp_op = fileOp.get(filename[i]);
			for(OpDetail op: temp_op) {
				System.out.println("Opeartion_id : "+op.opid);
				System.out.println("Filename : "+op.FileName);
				System.out.println("Operation : "+op.Operation);
				System.out.println("Start Time : "+op.startTime);
				System.out.println("End Time : "+op.endTime);
				System.out.println("Read from Logfile : "+op.logfile_name);
				System.out.println("=========================================================================================================");
			}
			System.out.println("=========================================================================================================");
		}
	}
	static ArrayList<Integer> TestOverlapActivity(ArrayList<OpDetail> FileOp, String file_name ){
		int i=0, j;
		boolean conflict = true;
		String temp = null,second_machine=null, first_machine=null;
		ArrayList<Integer> activity = new ArrayList<Integer>();

		activity.add(FileOp.get(0).opid);


		for (j = 1; j < FileOp.size(); j++){
			conflict = true;
			if(FileOp.get(j).Operation.equals("read") && FileOp.get(i).Operation.equals("read")){
				activity.add(FileOp.get(j).opid);
				conflict = false;

			}else if(FileOp.get(j).startTime>= FileOp.get(i).endTime) {

				activity.add(FileOp.get(j).opid);
				conflict = false;
			}
			if(conflict == true) {
				System.out.println("=========================================================================================================");
				temp = FileOp.get(j).logfile_name; 
				first_machine = temp.substring(temp.indexOf("net"), temp.indexOf('.'));
				temp = FileOp.get(i).logfile_name; 
				second_machine = temp.substring(temp.indexOf("net"), temp.indexOf('.'));
				System.out.println("Status for file : "+file_name);
				System.out.print("Operation id :"+FileOp.get(j).opid+", Operation Type: "+FileOp.get(j).Operation+"  of machine : "+first_machine+" is confilcting");
				System.out.println(" with Operation id :"+FileOp.get(i).opid+", Operation Type: "+FileOp.get(i).Operation+"  of machine : "+second_machine);
				System.out.println("=========================================================================================================");
			}
			i=j;
		}
		return activity;
	}

	public static class CompFinish implements Comparator<OpDetail> {
		public int compare(OpDetail arg0, OpDetail arg1) {
			return Long.valueOf(arg0.endTime).compareTo(Long.valueOf(arg1.endTime));
		}
	}


	public static void main(String args[]) {
		int no_of_logfiles =0;
		String logfilename[];
		ArrayList<OpDetail> op;
		ArrayList<OpDetail> temp_op;
		ArrayList<Integer> temp_opid;
		ArrayList<Integer> activities;
		boolean flag = false;

		File fileDir = new File("net01.utdallas.edu");
		filename = fileDir.list();
		no_of_file = filename.length;
		for(int i=0; i<no_of_file; i++){
			op = new ArrayList<OpDetail>();
			fileOp.put(filename[i], op);
		}

		File filelogDir = new File("logs");
		logfilename = filelogDir.list();
		no_of_logfiles = logfilename.length;
		for(int i = 0; i<no_of_logfiles; i++)	
			read_log_file(logfilename[i]);
		print_hash_map();

		for(int i=0;i<no_of_file;i++){
			temp_op = new ArrayList<OpDetail>();
			temp_opid = new ArrayList<Integer>();
			activities = new ArrayList<Integer>();
			temp_op = fileOp.get(filename[i]);

			if(!(temp_op.isEmpty())){
				Collections.sort(temp_op, new TestCode.CompFinish()); 
				activities = TestOverlapActivity(temp_op, filename[i]);
			}
			for(int j =0 ;j< temp_op.size(); j++)
				temp_opid.add(temp_op.get(j).opid);

			for(int p=0; p< temp_opid.size(); p++) {
				for(int q=0; q< activities.size(); q++) {
					if(temp_opid.get(p) == activities.get(q)){
						flag = true;
						break;
					}
					else{
						flag = false;
					}

				}
				if(flag == false) {
					break;
				}
			}

			if(flag == true) {
				System.out.println("=========================================================================================================");
				System.out.println("Status for file : "+filename[i]);
				System.out.println("No conflicting operations are executing concurrently");
				System.out.println("=========================================================================================================");
			}

		}	

	}
}





