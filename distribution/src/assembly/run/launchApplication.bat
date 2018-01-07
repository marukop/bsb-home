set ORACLE_HOME=D:/QuantumDB/product/11.2.0/client_1

java -cp "lib/*;conf/*;." -Dlog4j.configurationFile=./conf/log4j2.xml -Doracle.net.tns_admin=%ORACLE_HOME%/network/admin com.bsb.avionics.data.ImporterEntryPoint -f %1

pause
