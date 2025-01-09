# 4 Jerarquia de autoridades de certificacion

## Ejecucion

El handshake inicial, es decir, el handshake sin contemplar la no-revocacion.

### 1. Servidor

```
java ClassFileServerSubCA 7080 ../textosPrueba TLS true
```

### 2. Cliente

```
java ClienteAutenticadoSubCAClient 127.0.0.1 7080 textoclaro.txt
```

---

El handshake junto con la verificacion de no-revocacion del certificado del servidor, usando
no-revocacion de cliente.


### 1. Servidor

```
java ClassFileServerSubCA 7080 ../textosPrueba TLS true
```

### 2. Cliente

```
java ClienteAutenticadoOCSPSubCA 127.0.0.1 7080 textoclaro.txt
```
### 3. OCSP Responder
1. cd sub-ca-server

```
openssl ocsp -port 9080 -index db/index -rsigner sub-ocsp.crt -rkey private/sub-ocsp.key -CA sub-ca-server.crt -text -validity_period 300
```


El handshake junto con la verificacion de no-revocacion del certificado del servidor, usando
no-revocacion de servidor, por uno cualquiera de los dos procedimientos descritos en el punto
1.2 para la no-revocacion de servidor.

### 1. Servidor

```
java ClassFileServer_con_OCSPStaplingSubCA 7080 ../textosPrueba TLS true
```

### 2. Cliente

```
java Cliente_autenticado_con_OCSPStaplingSubCA 127.0.0.1 7080 textoclaro.txt
```