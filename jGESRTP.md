# jGESRTP

**jGESRTP** è una libreria Open Source che permette la comunicazione tra client e PLC General Elettric o Emerson.

GE utilizza il protocollo SRTP (Secure Real-time Transport Protocol) adattato alle esigenze dei PLC creando un protocollo proprietario e disponibile solo per VB, C++ e C#.

Per questioni lavorative ho la necessità di riuscire ad implementare questa libreria per creare software SCADA per il monitoraggio di impianti industriali in Java e per questo motivo ho decido di creare questa liberia che è ancora in fase di sviluppo e che può essere implementata dalla community.

## Creazione e Test

Per creare la libreria ho utilizzato una documentazione [PDF]([(PDF) Leveraging the SRTP protocol for over-the-network memory acquisition of a GE Fanuc Series 90-30](https://www.researchgate.net/publication/318925679_Leveraging_the_SRTP_protocol_for_over-the-network_memory_acquisition_of_a_GE_Fanuc_Series_90-30)) trovata in rete che spiega a grandi linee la struttura dei dati da passare per instaurare la connessione e per lo scambio di dati.

Successivamente ho trovato un programma in VB6 che mi consentiva di scrivere e leggere molteplici memorie contemporaneamente e grazie all'utilizzo di WireShark sono risalito alla struttura dei dati completa.

Questa libreria è stata testata su PLC:

- GE 90-30

- GE 90-70

- GE RX3i

- Emerson RSTi

## Struttura dati

In generale possiamo dire che la comunicazione avviene passando 56 byte dove solo alcuni sono rilevanti.

#### Connessione

Per prima cosa bisogna creare una matrice di 56 byte tutti 0x00 che sara il messaggio da inviare al PLC.

```java
private static byte[] msg_init = new byte[56];
```

Attraverso la funzione **initConnection** e passando una stringa contente l'indirizzo ip del PLC potremo instaurare la connessione sulla porta 18245 impostata di default.

```java
public static int initConnection(String ip){
        byte[] data = new byte[1024];
        try{
            client = new Socket(InetAddress.getByName(ip), PLC_PORT);
            client.setSoTimeout(3000);
            os = client.getOutputStream();
            is = client.getInputStream();

            if (client.isConnected()){
                os.write(msg_init, 0, msg_init.length);
                is.read(data, 0, data.length);
                if (data[0] == 0x01){
                    connected = true;
                    return 0;
                }else{
                    connected = false;
                    os.close();
                    is.close();
                    return -2;
                }
            }else{
                return -1;
            }
        }catch (Exception ex){
            return -1;
        }
    }
```

Una volta effettuata la connessione attraverso il Socket dobbiamo inviare la matriche creata in precedenza e la risposta del PLC deve essere **0x01** per avere la conferma che può iniziare lo scambio di dati.

#### Abilitazione SCADA

Per abilitare il monitoraggio SCADA e di conseguenza la simultaneità di più client che leggono/scrivono i dati bisogna usare questa struttura dati:

```java
private static byte[] msg_scada = { 
            0x08,        // 00
            0x00,        // 01
            0x01,        // 02
            0x00,        // 03
            0x00,        // 04
            0x00,        // 05
            0x00,        // 06
            0x00,        // 07
            0x00,        // 08
            0x01,        // 09
            0x00,        // 10
            0x00,        // 11
            0x00,        // 12
            0x00,        // 13
            0x00,        // 14
            0x00,        // 15
            0x00,        // 16
            0x01,        // 17
            0x00,        // 18
            0x00,        // 19
            0x00,        // 20
            0x00,        // 21
            0x00,        // 22
            0x00,        // 23
            0x00,        // 24
            0x00,        // 25
            0x00,        // 26
            0x00,        // 27
            0x00,        // 28
            0x00,        // 29
            0x01,        // 30
            (byte)0xc0,  // 31
            0x00,        // 32
            0x00,        // 33
            0x00,        // 34
            0x00,        // 35
            0x10,        // 36
            0x0e,        // 37
            0x00,        // 38
            0x00,        // 39
            0x01,        // 40
            0x01,        // 41
            0x4f,        // 42
            0x01,        // 43
            0x00,        // 43
            0x00,        // 44
            0x00,        // 45
            0x00,        // 46
            0x00,        // 47
            0x00,        // 48
            0x00,        // 49
            0x00,        // 50
            0x00,        // 51
            0x00,        // 52
            0x00,        // 53
            0x00,        // 54
            0x00         // 55
        };
```

Con la funzione **initScada** possiamo abilitare la connessione SCADA con il PLC:

```java
public static int initScada(){
        byte[] data = new byte[1024];
        try{
            if (client.isConnected()){
                os.write(msg_scada, 0, 56);
                is.read(data, 0, data.length);
                if (data[0] == 0x03){
                    connected = true;
                    return 0;
                }else{
                    connected = false;
                    os.close();
                    is.close();
                    return -2;
                }
            }else{
                return -1;
            }
        }catch (Exception ex){
            return -1;
        }
    }
```

L'abilitazione avviene solo se il PLC risponde nel primo byte con **0x03**

#### Disconnessione

Per quanto riguarda la disconnessione è molto simile al processo di connessione, bisogna inviare tramite socket l'array **msg_init** e chiudere il socket.

```java
public static int closeConnection(){
        byte[] data = new byte[1024];
        try{
            if (client.isConnected()){
                os.write(msg_init, 0, msg_init.length);
                is.read(data, 0, data.length);
                connected = false;
                os.close();
                is.close();
                client.close();
                n_transfer = 0;
                return 0;
            }
            return -1;
        }catch (Exception ex){
            connected = false;
            return -1;
        }
    }
```

#### Lettura

Questa è la struttura base dei dati per la lettura di memorie.

```java
private static byte[] msg_read = {
            0x02,        // 00
            0x00,        // 01
            0x00,        // 02 - Seq Number
            0x00,        // 03
            0x00,        // 04
            0x00,        // 05
            0x00,        // 06
            0x00,        // 07
            0x00,        // 08
            0x01,        // 09
            0x00,        // 10
            0x00,        // 11
            0x00,        // 12
            0x00,        // 13
            0x00,        // 14
            0x00,        // 15
            0x00,        // 16
            0x01,        // 17
            0x00,        // 18
            0x00,        // 19
            0x00,        // 20
            0x00,        // 21
            0x00,        // 22
            0x00,        // 23
            0x00,        // 24
            0x00,        // 25
            0x00,        // 26
            0x00,        // 27
            0x00,        // 28
            0x00,        // 29
            0x00,        // 30 - Seq Number
            (byte)0xc0,  // 31
            0x00,        // 32
            0x00,        // 33
            0x00,        // 34
            0x00,        // 35
            0x10,        // 36
            0x0e,        // 37
            0x00,        // 38
            0x00,        // 39
            0x01,        // 40
            0x01,        // 41
            0x00,        // 42 - Service Request Code
            0x00,        // 43 - Memory Type Code
            0x00,        // 44 - Address:LSB
            0x00,        // 45 - Address:MSB
            0x00,        // 46 - Data Size Words:LSB
            0x00,        // 47 - Data Size Words:MSB
            0x00,        // 48
            0x00,        // 49
            0x00,        // 50
            0x00,        // 51
            0x00,        // 52
            0x00,        // 53
            0x00,        // 54 
            0x00         // 55
        };
```

Questa è la funzione di esempio che in questo caso va a leggere le memorie %R ma il ragionamento vale per ogni tipo di memoria che vogliamo leggere.

```java
public static int[] read_R_WORD(int address, int number){   
        //preparazione varibaili
        byte[] msg_send = new byte[56];
        byte[] msg_response = new byte[56];
        byte[] data = new byte[number * 2];
        int[] val = new int[number];
        msg_send = msg_read;

        msg_send[3] = (byte) n_transfer;
        msg_send[30] = (byte) n_transfer;
        n_transfer++;
        if(n_transfer > 255){n_transfer = 0;}

        msg_send[42] = (byte)SERVICE_REQUEST.READ_SYS_MEMORY;

        msg_send[43] = (byte)MEMORY_TYPE.R;

        address = address - 1;
        msg_send[44] = (byte)(address & 0xFF);
        msg_send[45] = (byte)(address >> 8);

        msg_send[46] = (byte) number;

        int c = 0;
        try{
            if (client.isConnected()){   
                os.write(msg_send, 0, msg_send.length);
                is.read(msg_response, 0, msg_response.length);
                if (msg_response[0] == 0x03){
                    if(number > 3){
                        is.read(data, 0, data.length);
                        for(int i = 0; i < data.length; i+=2){
                            val[c] = (data[i + 1] << 8) + data[i];
                            c++;
                        }
                    }else{
                        for(int i = 44; i < 50; i+=2){
                            val[c] = (msg_response[i + 1] << 8) + msg_response[i];
                            c++;
                        }
                    }
                    return val;
                }
            }
        }catch (Exception ex){
            return null;
        }

        return null;
    }
```

Dati da inserire:

- **msg_send[3]**: indice che si incrementa ad ogni invio di dati da parte del client.

- **msg_send[30]**: indice che si incrementa ad ogni invio di dati da parte del client.

- **msg_send[42]**: byte che corrisponde al codice per la lettura di memorie.

- **msg_send[43]**: byte che corrisponde al codice per il tipo di memoria.

- **msg_send[44]**: numero della memoria iniziale (LSB)

- **msg_send[45]**: numero della memoria iniziale (MSB)

- **msg_send[46]**: numero di memorie da leggere (LSB)

- **msg_send[47]**: numero di memorie da leggere  (MSB)

Una volta inviato il pacchetto con tutti i dati il PLC invierà due risposte: la prima è solo di conferma (se il primo byte è **0x03** vuol dire che la richiesta è andata a buon fine) e la seconda che contiene effettivamente i dati che ci interessano.

###### Nota bene

Se il numero di memorie da leggere è minore o uguale a 3 allora il PLC invierà solo una risposta e i dati che abbiamo richiesto li troveremo a partire dal byte numero 44.

#### Scrittura

Questa è la struttura dati per la scrittura di memorie.

```java
private static byte[] msg_write = {
            0x02,        // 00
            0x00,        // 01
            0x00,        // 02 - Seq Number
            0x00,        // 03
            0x00,        // 04 - Number of bytes to write LSB
            0x00,        // 05 - Number of bytes to write MSB
            0x00,        // 06
            0x00,        // 07
            0x00,        // 08
            0x02,        // 09
            0x00,        // 10
            0x00,        // 11
            0x00,        // 12
            0x00,        // 13
            0x00,        // 14
            0x00,        // 15
            0x00,        // 16
            0x02,        // 17
            0x00,        // 18
            0x00,        // 19
            0x00,        // 20
            0x00,        // 21
            0x00,        // 22
            0x00,        // 23
            0x00,        // 24
            0x00,        // 25
            0x00,        // 26
            0x00,        // 27
            0x00,        // 28
            0x00,        // 29
            0x00,        // 30 - Seq Number
            (byte)0x80,  // 31
            0x00,        // 32
            0x00,        // 33
            0x00,        // 34
            0x00,        // 35
            0x10,        // 36
            0x0e,        // 37
            0x00,        // 38
            0x00,        // 39
            0x01,        // 40
            0x01,        // 41
            0x32,        // 42
            0x00,        // 43
            0x00,        // 44
            0x00,        // 45
            0x00,        // 46
            0x00,        // 47
            0x01,        // 48
            0x01,        // 49
            0x00,        // 50 - Service Request Code
            0x00,        // 51 - Memory Type Code
            0x00,        // 52
            0x00,        // 53
            0x00,        // 54
            0x00         // 55
        };
```

Questa è la funzione di esempio che in questo caso va a scrivere le memorie %R ma il ragionamento vale per ogni tipo di memoria che vogliamo scrivere.

```java
public static boolean write_R_WORD(int address, int[] valori){
        byte[] msg_send = new byte[56];
        byte[] msg_read = new byte[56];
        byte[] data = new byte[valori.length * 2];
        msg_send = msg_write;

        msg_send[3] = (byte) n_transfer;
        msg_send[30] = (byte) n_transfer;
        n_transfer++;
        if(n_transfer > 255){n_transfer = 0;}

        int n_bytes = valori.length * 2;
        msg_send[4] = (byte)(n_bytes & 0xFF);
        msg_send[5] = (byte)(n_bytes >> 8);

        msg_send[50] = (byte)SERVICE_REQUEST.WRITE_SYS_MEMORY;

        msg_send[51] = (byte)MEMORY_TYPE.R;

        address = address - 1;
        msg_send[52] = (byte)(address & 0xFF);
        msg_send[53] = (byte)(address >> 8);

        msg_send[54] = (byte) valori.length;

        int c = 0;
        for(int i = 0; i < valori.length; i++){
            data[c] = (byte)(valori[i] & 0xFF);
            data[c + 1] = (byte)(valori[i] >> 8);
            c += 2;
        }

        try{
            if (client.isConnected())
            {   
                os.write(msg_send, 0, msg_send.length);
                os.write(data, 0, data.length);
                is.read(msg_read, 0, msg_read.length);
                if (msg_read[0] == 0x03){
                    return true;
                }
            }
        }catch (Exception ex){
            return false;
        }

        return false;
    }
```

Dati da inserire:

- **msg_send[3]**: indice che si incrementa ad ogni invio di dati da parte del client.

- **msg_send[30]**: indice che si incrementa ad ogni invio di dati da parte del client.

- **msg_send[4]**: numero di byte che vogliamo scrivere (LSB)

- **msg_send[5]**: numero di byte che vogliamo scrivere (MSB)

- **msg_send[50]**: byte che corrisponde al codice per la lettura di memorie.

- **msg_send[51]**: byte che corrisponde al codice per il tipo di memoria.

- **msg_send[52]**: numero della memoria iniziale (LSB)

- **msg_send[53]**: numero della memoria iniziale (MSB)

- **msg_send[54]**: numero di memorie da leggere (LSB)

- **msg_send[55]**: numero di memorie da leggere (MSB)

In questo caso dobbiamo inviare due pacchetti: il primo che è **msg_send** e il secondo è un array che deve avere le dimensioni specificate in precedenza e contiene i valori che effettivamente vogliamo scrivere, una volta inviati il PLC risponderà con **0x03** se tutto è andato a buon fine.

#### Memory Type Code

Questi sono tutti i byte che corrispondono al tipo di memoria che si vuole leggere o scrivere.

In questa libreria per il momento sono implementate solo le AI, AQ e R per motivi strettamente lavorativi ma in futuro ho l'obbiettivo di implementare tutti i tipi di memorie in modo da renderlo più completo possibile.

```java
class MEMORY_TYPE{
    public static byte R = 0x08;    // Register (Word)
    public static byte AI = 0x0a;   // Analog Input (Word)
    public static byte AQ = 0x0c;   // Analog Output (Word)
    public static byte I_BYTE = 0x10;    // Descrete Inputs (Byte)
    public static byte Q_BYTE = 0x12;    // Descrete Outputs (Byte)
    public static byte T_BYTE = 0x14;    // Descrete Temporary Bits (Byte)
    public static byte M_BYTE = 0x16;    // Descrete Markers (Byte)
    public static byte SA_BYTE = 0x18;   // System Bits A-part (Byte)
    public static byte SB_BYTE = 0x20;   // System Bits B-part (Byte)
    public static byte SC_BYTE = 0x22;   // System Bits C-part (Byte)
    public static byte G_BYTE = 0x38;    // Genius Global (Byte)
    public static byte I_BIT = 0x46;    // Descrete Input (Bit)
    public static byte Q_BIT = 0x48;    // Descrete Output (Bit)
    public static byte T_BIT = 0x4a;    // Descrete Temporary (Bit)
    public static byte M_BIT = 0x4c;    // Descrete Marker (Bit)
    public static byte SA_BIT = 0x4e;   // System Bit A-part (Bit)
    public static byte SB_BIT = 0x50;   // System Bit B-part (Bit)
    public static byte SC_BIT = 0x52;   // System Bit C-part (Bit)
    public static byte G_BIT = 0x56;    // Genius Global (Bit)
}
```

#### Service Request Code

Questi sono tutti i tipi di richiesta che possono essere fatti al PLC, per il momento sono implementati solo **READ_SYS_MEMORY** e **WRITE_SYS_MEMORY**.

In futuro penso di implementare solo il **PLC_STATUS** perchè a livello di software SCADA non vedo la necessità di altri tipi di richieste.

```java
class SERVICE_REQUEST{
    public static byte PLC_STATUS = 0x00;
    public static byte RETURN_PROG_NAME = 0x03;
    public static byte READ_SYS_MEMORY = 0x04;
    public static byte READ_TASK_MEMORY = 0x05;
    public static byte READ_PROG_MEMORY = 0x06;
    public static byte WRITE_SYS_MEMORY = 0x07;
    public static byte WRITE_TASK_MEMORY = 0x08;
    public static byte WRITE_PROG_MEMORY = 0x09;
    public static byte RETURN_DATETIME = 0x25;
    public static byte RETURN_CONTROLLER_TYPE = 0x43;
}
```

## Esempi

Per chi non volesse modificare la libreria ma solo utilizzarla, ecco un esempio di lettura:

```java
jGESRTP plc = new jGESRTP();
int status_connection = plc.initConnetion("192.168.1.100");
if(status_connection == 0){
    int status_scada = plc.initScada();
    if(status_scada == 0){
        int[] analog_input = plc.read_AI_WORD(1, 100); //Read 100AI from 1AI
        int[] analog_output = plc.read_AQ_WORD(1, 100); //Read 100AQ from 1AQ
        int[] register = plc.read_R_WORD(1, 100); //Read 100R from 1R
        plc.closeConnection();
    }else{
        plc.closeConnection();
    }
}else{
    plc.closeConnection();
}
```

E un esempio di scrittura:

```java
jGESRTP plc = new jGESRTP();
int status_connection = plc.initConnetion("192.168.1.100");
if(status_connection == 0){
    int status_scada = plc.initScada();
    if(status_scada == 0){
        int[] values = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9}
        boolean analog_input = plc.write_AI_WORD(1, values); //Write from 1AI
        boolean analog_output = plc.write_AQ_WORD(1, values); //Write from 1AQ
        boolean register = plc.write_R_WORD(1, values); //Write from 1R
        plc.closeConnection();
    }else{
        plc.closeConnection();
    }
}else{
    plc.closeConnection();
}
```

## Importante

Questa libreria è ancora in versione beta anche se è stata testata molte volte, consiglio di controllare su banco di prova che tutto funzioni correttamente prima di implementarla sel software di supervisione.

Come già detto la libreria è Open Source quindi per chiunque volesse contribuire allo sviluppo può benissimo farlo e lo invito a condividere gli aggiornamenti in modo da aggiornare anche la versione ufficiale o anche solo comunicare eventuali bug in modo da risolverli.

## TODO

- [ ] Sviluppare la lettura e la scrittura per tutti i tipi di memoria.

- [ ] Sviluppare tutti i tipi di request.

- [ ] Verificare che effettivamente l'abilitazione SCADA permette in monitoraggio di più client.

- [ ] Sviluppare la lettura e la scrittura anche per DWORD e FLOAT
