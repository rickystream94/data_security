Just because of test purposes, the two .JKS files have been created with password "password".
1) keystore-server.jks represents server's private key from which the X509 Certificate has been generated;
2) truststore-client.jks is the client's truststore, containing all certificates he should considered trusted. For this purpose, the server's X509 certificate has been imported into this truststore.

--------------

Keytool commands to generate the files: