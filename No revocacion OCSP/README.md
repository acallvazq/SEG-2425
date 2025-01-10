# 3 Verificacion de no revocacion OCSP

## Ejecucion

### 1. Servidor

```
java ClassFileServer 8080 ../textosPrueba TLS true
```

### 2. Cliente

```
java ClienteAutenticadoOCSP 192.168.1.138 8080 textoclaro.txt
```
Si estamos en el portatil, cambiar ip por 192.168.10.10

### 3. Iniciar OCSP responder

1. cd root-ca

```
openssl ocsp -port 9080 -index db/index -rsigner root-ocsp.crt -rkey private/root-ocsp.key -CA root-ca.crt -text
```

## Testear la revocacion/no-revocacion OCSP del certificado del lado del servidor

### 1. Servidor

```
java ClassFileServer_con_OCSPStapling 8080 ../textosPrueba TLS true
```

### 2. Cliente

```
java Cliente_autenticado_con_OCSPStapling 192.168.1.138 8080 textoclaro.txt
```

### 3. Iniciar OCSP responder

1. cd root-ca

```
openssl ocsp -port 9080 -index db/index -rsigner root-ocsp.crt -rkey private/root-ocsp.key -CA root-ca.crt -text
```