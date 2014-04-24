gasp-encrypt
============

Simple example of how to encrypt and base64-encode iOS Push Notification certificates/keys (in PEM format).  
See [gasp-push-server](https://github.com/mqprichard/gasp-push-server) for an example of how to decrypt cert/key files encypted with this utility. 

In this exmaple, we use the GCM API Key as the encryption key; the utiltity will output the AES Init Vector and Salt (both in base64 format), make a note of these as you will need them to decrypt the ciphertext.

To run:

1. copy your cert/key files (gasp-cert.pem and gasp-key.pem) to the project directory 

2. do `mvn clean install`

3. mkdir libs; copy commons-codec-1.9.jar and commons-lang-2.6.jar to libs

4. run the utility
`java -cp target/gasp-encrypt-certs-1.0-SNAPSHOT.jar:libs/commons-codec-1.9.jar:libs/commons-lang-2.6.jar -DGCM_API_KEY=<your-API-key> com.cloudbees.gasp.EncryptCerts`

5. make a note of the output AES IV and Salt; encrypted files are in gasp-cert.b64 and gasp-key.b64
