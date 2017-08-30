JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = \
    HostInfo.java \
    MasterSlaveProtocol.java \
    MasterBot.java \
    SocketServer.java \
    MasterConsoleRunnable.java \
    MasterRunnable.java \
    SlaveBot.java \
    SlaveRunnable.java  

default: classes

classes: $(CLASSES:.java=.class)

clean:
		$(RM) *.class
