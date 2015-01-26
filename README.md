# Fault-Tolerant-Distributed-File-System
This project is an implementation of how a distributed file system can be made fault tolerant. To tolerate failures, there is a replica of each file on every machine in the system. All replicas of a file are kept consistent using static voting protocol. 

The file system supports two types of operation on a file: read and write. Before an application can execute an operation on a file, it has to first check out of the file. Inorder to check out the file, it should first request other machines for votes and should be able to gather the desired number votes (read/write quorum size). After executing the operation, the applicstion has to check in the file, which means it has to return the updated copy of the file system to all the machines that has voted for it initially.

Testing of the system is done by simulating the machine failures. While a machine is down, it does not change its state. Also it discards any message it receives during this time. 


--------------------------------------------------------------------------------------------------------
COMPILING THE PROGRAM 
--------------------------------------------------------------------------------------------------------
   cd StaticVoting/src
   (Here all the source files are present along with Config file)
   
   Compilation:
   javac StaticVoting.java

--------------------------------------------------------------------------------------------------------
RUNNING THE PROGRAM 
--------------------------------------------------------------------------------------------------------

1) In the src directory class files are generated for all the classes
2) Check the entries in the Config file and open all the machines mentioned in the config file
3) Do:
   java StaticVoting machine no
   eg java StsticVoting net01
   
4) The program must be run simultaneously on all the machines mentioned in the Config file

--------------------------------------------------------------------------------------------------------
OUTPUT
--------------------------------------------------------------------------------------------------------

1) Every machine has its replica of file it its directory
2) If a process generates a read request, it reads from the most updated file and prints the text on the screen
3) If a process generates a write request, it writes to the file and sends the updated file to the members of its quorum 
4) The sequence of operations are logged in Log files

--------------------------------------------------------------------------------------------------------
TESTING THE RESULTS
--------------------------------------------------------------------------------------------------------

1) Verify if the text files are generated
2) Verify if the log files are created
2) Compile the TestCode file present in the test directory
3) Execute the TestCode file as java TestCode
4) The results will be displayed on the terminal
