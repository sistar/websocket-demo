websocket-demo
=================
Der Server kann via mvn gestartet werden. Hierbei kann -DserverPort= verwendet werden um den 
Port zu spezifizieren.

\# mvn -Pserver exec:exec -DserverPort=8080


Starten des Cients
=================
Der Client kann via mvn gestartet werden. Hierbei kann -DserverAddress und -DserverPort= verwendet werden um die 
IP-Addresse bzw. den Port des Server zu spezifizieren der die Echo Nachricht entegen nehmen soll. 

\# mvn -Pclient exec:exec -DserverAddress=127.0.0.1 -DserverPort=8080
