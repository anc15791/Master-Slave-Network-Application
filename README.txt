
Master (MasterBot.java) will take the following command line argument:
-p PortNumber

Slave (SlaveBot.java) will take two arguments:
-h IPAddress|Hostname (of Master) -p (port where master is listening for connections)


Master accepts following commands:

* list - list all active slaves. 

connect (IPAddressOrHostNameOfSlave|all) (TargetHostName|IPAddress) TargetPortNumber [NumberOfConnections: 1 if not specified]
* connect 127.0.0.1 www.google.com 80 2 - instruct slave-ip to create 2 connections to google.com at port 80
* connect 127.0.0.1 www.google.com 80 - instruct slave-ip to create 1 connections to google.com at port 80

connect (IPAddressOrHostNameOfSlave|all) (TargetHostName|IPAddress) TargetPortNumber [NumberOfConnections: 1 if not specified] [url=path-to-be-provided-to-web-server]
* connect 127.0.0.1 www.google.com 80 2 url=https://www.google.com/#q=YowurRandomString - instruct slave-ip to create 2 connections to google.com at port 80 and send a random string. 

connect (IPAddressOrHostNameOfYourSlave|all) (TargetHostName|IPAddress) TargetPortNumber [NumberOfConnections: 1 if not specified] [keepalive]
* connect 127.0.0.1 www.google.com 80 2 keepalive - instruct slave to keep the connection alive.

disconnect (IPAddressOrHostNameOfSlave|all) (TargetHostName|IPAddress) [TargetPort:all if no port specified]
* disconnect 127.0.0.1 www.sjsu.edu 80 || disconnect 127.0.0.1 www.sjsu.edu

ipscan (IPAddressOrHostNameOfYourSlave|all) (IPAddressRange)
* ipscan localhost 127.0.0.1-127.0.0.5 || ipscan localhost 216.58.216.130-216.58.216.135

tcpportscan (IPAddressOrHostNameOfYourSlave|all) (TargetHostName|IPAddress) TargetPortNumberRange
* tcpportscan all www.google.com 1-100

geoipscan (IPAddressOrHostNameOfYourSlave|all) (IPAddressRange)
* geoipscan localhost 127.0.0.1-127.0.0.5 || geoipscan all 216.58.216.130-216.58.216.135

* exit - This command will stop the server and exit MasterBot.
 * exit all - This command closes the master server as well as all slave servers

