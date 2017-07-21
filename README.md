MQTT(Mosquitto) with SSL/TLS

My environment of developement is 
secure mosquitto server as a message queue broker with mqtt protocol
and the eclipse paho client with openssl.


Here are my steps to achieve the system setup.

Making a your own certificate file by script(OwnTracks) with openssl library.

Download a generate-CA.sh script using wget command as follows

	mkdir CA
	chmod 700 CA
	cd CA

	wget https://github.com/owntracks/tools/raw/master/TLS/generate-CA.sh .
	./generate-CA.sh

You can check the all 6 files are created while executing the script which are : 

 ca.crt(certificates), ca.key(keys), ca.srl(serial number record), localhost.crt, localhost.csr(request), localhost.key

	sudo cp ca.crt /etc/mosquitto/ca
	sudo cp localhost.crt localhost.key /etc/mosquitto/crt/

configuration with mosquitto.conf 

	vi /usr/local/mosquitto/mosquitto.conf 

Put the lines below at the end the file

	listener 8883
	protocol mqtt

	cafile /etc/mosquitto/ca/ca.crt
	certfile /etc/mosquitto/crt/localhost.crt
	keyfile /etc/mosquitto/crt/localhost.key

	require_certificate false   

	#listener 1883  => all refuse except TLS secure connection.
	#protocol mqtt
 


You can see now what you have with mosquitto_sub and mosquitto_pub executable in the bin folder

Start Broker
	mosquitto -c mosquitto.conf 

start Subscribe  
	mosquitto_sub -h localhost -p 8883 --cafile /etc/mosquitto/ca_certficates/ca.crt -t hello

start Publish 
	mosquitto_pub -h localhost -p 8883 --cafile /etc/mosquitto/ca_certficates/ca.crt -t hello -m "Test is Test"  

Now, It's time to ready with java client.

Getting java client from the eclipse paho site

https://github.com/eclipse/paho.mqtt.java

I decided to make my own paho client with ant project.
I also use the apache launcher project.

	- apache-ant-1.9.4
	- apache launcher

You should have some openssl java library like bouncycastle from the site
https://www.bouncycastle.org/latest_releases.html

	- bcpkix-jdk15on-157.jar
	- bcprov-ext-jdk15on-157.jar
	- bcprov-jdk15on-156.jar

After all this setup done, I've got a sslsocket factory named SslUtil.java.
https://gist.github.com/rohanag12/07ab7eb22556244e9698

This is latest version source. you can check old version of source from 
https://gist.github.com/sharonbn/4104301

After that, I have to modify the Sample.java.
There are more options than the original one.

The output when enter the help command.


	Syntax:

    Sample [-h] [-a publish|subscribe] [-t <topic>] [-m <message text>]
            [-s 0|1|2] -b <hostname|IP address>] [-p <brokerport>] [-i <clientID>]

    -h  Print this help text and quit
    -q  Quiet mode (default is false)
    -a  Perform the relevant action (default is publish)
    -v  TLS/SSL enabled; true - (default is false)
    -e  Path of ca certification file if v option turns on
    -f  Path of client certification file if v option turns on
    -y  Path of client key file if v option turns on
    -t  Publish/subscribe to <topic> instead of the default
            (publish: "Sample/Java/v3", subscribe: "Sample/#")
    -m  Use <message text> instead of the default
            ("Message from MQTTv3 Java client")
    -s  Use this QoS instead of the default (2)
    -b  Use this name/IP address instead of the default (m2m.eclipse.org)
    -p  Use this port instead of the default (1883)

    -i  Use this client ID instead of SampleJavaV3_<action>
    -c  Connect to the server with a clean session (default is false)


	Security Options
	 -u Username
	 -z Password
	
	
	TLS Options
	-v  TLS/SSL enabled; true - (default is false)     -e  CA certification file with openssl generally
	-f  Client certification file
	-y  Client key file
	
	
	SSL Options
	-l  SSL enabled; true - (default is false)     -k  Use this JKS format key store to verify the clie
	-w  Passpharse to verify certificates in the keys store
	-r  Use this JKS format keystore to verify the server
	If javax.net.ssl properties have been set only the -l flag needs to be set
	Delimit strings containing spaces with ""
	
	Publishers transmit a single message then disconnect from the server.
	Subscribers remain connected to the server and receive appropriate
	messages until <enter> is pressed.

You can then execute mqtt client using apache launcher.
See the launcher.xml and several batch scripts.
I assume your os must be windows, but it's not that difficult to find compatible command on your os out there.

This is jvm argument in my launcher.xml file.

subscribe:
	-a subscribe -b 192.9.112.155 -p 8883 -e resources/ca/ca.crt -f resources/ca/wap1.crt -y resources/ca/wap1.key -v true -z secret
	
batch file: run_secu_sub.bat

![subscrbe command script](https://raw.githubusercontent.com/tommybee-dev/tls-paho-mosquitto/master/screenshot/command_sub.png?raw=true "subscrbe")
	
publish:
	-a publish -b 192.9.112.155 -p 8883 -e resources/ca/ca.crt -f resources/ca/wap1.crt -y resources/ca/wap1.key -v true -z secret -m 'this is a message from me'

batch file: run_secu_pub.bat

![publish command script](https://raw.githubusercontent.com/tommybee-dev/tls-paho-mosquitto/master/screenshot/command_pub.png?raw=true "publish")


# note: 
Check exec directory if all certificate files in the ca directory in the resources folder


