# SEG-2425

Desarrollo de una aplicación cliente-servidor segura utilizando el protocolo SSL/TLS, implementada con el paquete Java JSSE, JCA, y JCE. 
Los objetivos principales son familiarizarse con librerías criptográficas modernas, la gestión de claves y certificados, y el uso de herramientas como OpenSSL.

## Ejecución ⚙️

### Cliente sin autenticar
java ClassFileServer 2080 /home/user/SEG-2425/textosPrueba TLS
java ClienteNoAutenticado 127.0.0.1 2080 textoclaro.txt

### Cliente Autenticado
java ClassFileServer 2080 /home/user/SEG-2425/textosPrueba TLS true
java ClienteAutenticado 127.0.0.1 2080 textoclaro.txt 
