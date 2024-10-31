# SEG-2425

Desarrollo de una aplicación cliente-servidor segura utilizando el protocolo SSL/TLS, implementada con el paquete Java JSSE, JCA, y JCE. 
Los objetivos principales son familiarizarse con librerías criptográficas modernas, la gestión de claves y certificados, y el uso de herramientas como OpenSSL.

## Ejecución ⚙️

### Cliente sin autenticar
```
java ClassFileServer 2080 /home/alba/24-25/SEG/SEG-2425/textosPrueba TLS
```
```
java ClienteNoAutenticado 127.0.0.1 2080 textoclaro.txt
```

### Cliente Autenticado
```
java ClassFileServer 2080 /home/alba/24-25/SEG/SEG-2425/textosPrueba TLS true
```
```
java ClienteAutenticado 127.0.0.1 2080 textoclaro.txt 
```
#### Abrir canal SSL para cliente autenticado
```
openssl s_client -connect 127.0.0.1:2080 
```

### Cliente Autenticado con OCSP

```
openssl ocsp -port 9080 -index db/index -rsigner root-ocsp.crt -rkey private/root-ocsp.key -CA root-ca.crt -text
```

```
java ClassFileServer 8080 .\textosPrueba\ TLS true
```

(Del cliente hay que cambiar la IP del OCSP en Java a la que corresponda)
```
java SSLSocketClientWithClientAuthOCSP 127.0.0.1 8080 textoclaro.txt
```





