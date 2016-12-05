::echo using Java %JAVA_HOME%\bin\java
::set path=G:\Program Files\Java\jdk1.6.0_16\bin
::set CLASS=E:\work\offline_server\bin\classes
set "CLASS=%cd%"
set CLASS_PATH=.;%CLASS%
set LIB=%CLASS%\..\lib
set CLASS_PATH=%CLASS_PATH%;%LIB%\commons-collections-3.1.jar
set CLASS_PATH=%CLASS_PATH%;%LIB%\commons-lang-2.6.jar
set CLASS_PATH=%CLASS_PATH%;%LIB%\commons-logging-1.0.3.jar
set CLASS_PATH=%CLASS_PATH%;%LIB%\fastjson-1.1.26.jar
set CLASS_PATH=%CLASS_PATH%;%LIB%\log4j-1.2.16.jar
set CLASS_PATH=%CLASS_PATH%;%LIB%\mina-core-2.0.7.jar
set CLASS_PATH=%CLASS_PATH%;%LIB%\mina-filter-compression-2.0.7.jar
set CLASS_PATH=%CLASS_PATH%;%LIB%\mongo-java-driver-2.11.3.jar
set CLASS_PATH=%CLASS_PATH%;%LIB%\quartz-all-1.8.6.jar
set CLASS_PATH=%CLASS_PATH%;%LIB%\slf4j-api-1.6.6.jar
set CLASS_PATH=%CLASS_PATH%;%LIB%\slf4j-nop-1.6.6.jar
set CLASS_PATH=%CLASS_PATH%;%LIB%\xpp3_min-1.1.4c.jar
set CLASS_PATH=%CLASS_PATH%;%LIB%\xstream-1.3.1.jar
 
java -classpath %CLASS_PATH% cn.jj.offline.server.net.SocketServer
@pause
