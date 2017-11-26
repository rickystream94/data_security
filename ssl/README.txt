Just because of test purposes, the two .JKS files have been created with password "password".
1) keystore-server.jks represents server's private key from which the X509 Certificate has been generated;
2) truststore-client.jks is the client's truststore, containing all certificates he should consider trusted. For this purpose, the server's X509 certificate has been imported into this truststore.

--------------

Keytool commands to generate the files:

# Run following command to generate a keystore for the server:
keytool -genkeypair -alias certificatekey -keyalg RSA -validity 360 -keystore keystore-server.jks

# Run following command to generate a truststore for the client:
keytool -genkeypair -alias certificatekey -keyalg RSA -validity 360 -keystore truststore-client.jks

# Run following command to generate a certificate from the previously generated server's keystore:
keytool -export -alias certificatekey -keystore keystore-server.jks -rfc -file X509_serverCertificate.cer

# Import server's certificate into client's trust-store:
keytool -import -alias certificatekey -file X509_serverCertificate.cer -keystore truststore-client.jks