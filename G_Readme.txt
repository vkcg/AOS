There are 3 Java files included, one for client, other two are for servers group server and shopping server. G_TCPCClient.java, G_TCPCServer.java and G_Shop.java are the java files. There is a serializable file named menu.ser and couple of text files login.txt and users.txt used for authenticating and storing details of users.

In command prompt, we need to go to the path where the files are located using cd command.

We need to compile the G_Shop.java and run it by providing port number as command line argument. We need to run three instances of shopping server each with unique port number each for silver, gold and platinum users.

Compiling G_Shop.java
>javac G_Shop.java

Running G_Shop.java
>java -cp [class path] G_Shop [port number]
>java -cp c:\aos\p1 G_Shop 1111


We need to compile the G_TCPCServer.java and run it by providing port number and ip address of shopping server as command line arguments. It is a concurrent server, one instance of server is needed to be run.

Compiling G_TCPCServer.java
>javac G_TCPCServer.java

Running G_TCPCServer.java
>java -cp [class path] G_TCPCServer [port number] [ip address]
>java -cp c:\aos\p1 G_TCPCServer 6666 localhost

We need to compile the G_TCPCServer.java and run it by providing port number and ip address of group server as command line arguments.

Compiling G_TCPCClient.java
>javac G_TCPCClient.java

Running G_TCPCClient.java
>java -cp [class path] G_TCPCClient [port number] [ip address]
>java -cp c:\aos\p1 G_TCPCClient 6666 localhost

login.txt file is accessed by the group server which contains all the details like id, username, password and points of users. This is used to authenticate the users.

users.txt files is accessed by the client, it contains details like usernames and points of the users.

 
