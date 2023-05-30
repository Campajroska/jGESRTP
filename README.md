# jGESRTP

**jGESRTP** it is an open-source library that enables communication between clients and General Electric or Emerson PLCs.

GE utilizes the Secure Real-time Transport Protocol (SRTP) adapted to the needs of PLCs, creating a proprietary protocol that is available only for VB, C++, and C#.

For work-related reasons, I have the need to be able to implement this library to create SCADA software for monitoring industrial plants in Java. That's why I have decided to create this library, which is still in the development phase and can be implemented by the community.

## Creation and Testing

To create the library, I used this documentation. [PDF]([(PDF) Leveraging the SRTP protocol for over-the-network memory acquisition of a GE Fanuc Series 90-30](https://www.researchgate.net/publication/318925679_Leveraging_the_SRTP_protocol_for_over-the-network_memory_acquisition_of_a_GE_Fanuc_Series_90-30)) I found online documentation that explains the basic structure of the data to be passed for establishing the connection and for data exchange.

Later, I found a VB6 program that allowed me to write and read multiple memories simultaneously, and using WireShark, I was able to determine the complete data structure.

This library has been tested on PLCs:

- GE 90-30

- GE 90-70

- GE RX3i

- Emerson RSTi

## Data structure

In general, we can say that communication occurs by passing 56 bytes where only some are relevant.

#### Connection

First, we need to create a 56-byte matrix filled with **0x00**, which will be the message to be sent to the PLC.

```java
private static byte[] msg_init = new byte[56];
```

Through the **initConnection** function, by passing a string containing the IP address of the PLC, we can establish the connection on the default port 18245.

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

Once the connection is established through the socket, we need to send the previously created matrix, and the response from the PLC should be **0x01** to confirm that data exchange can begin.

#### Enable SCADA

To enable SCADA monitoring and, consequently, the simultaneous access of multiple clients for reading/writing data, you need to use this data structure:

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

With the **initScada** function, we can enable the SCADA connection with the PLC.

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

The enablement only occurs if the PLC responds with **x03** in the first byte.

#### Disconnect

As for the disconnection, it is very similar to the connection process. You need to send the **msg_init** array via the socket and then close the socket.

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

#### Reading

This is the basic data structure for reading memories.

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

This is an example function that, in this case, reads %R memories, but the logic applies to any type of memory we want to read.

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

Data to be inserted:

- **msg_send[30]**: An index that increments with each data transmission by the client.

- **msg_send[42]**: A byte that corresponds to the code for reading memories.

- **msg_send[43]**: A byte that corresponds to the code for the memory type.

- **msg_send[44]**: Start memory (LSB).

- **msg_send[45]**: Start memory (MSB).

- **msg_send[46]**: Number of memory (LSB).

- **msg_send[47]**: Number of memory (MSB).

Once the packet with all the data is sent, the PLC will send two responses: the first one is just a confirmation (if the first byte is 0x03, it means the request was successful), and the second one actually contains the data of interest.

###### Important

If the number of memories to be read is less than or equal to 3, the PLC will only send one response, and the requested data can be found starting from byte number 44.

#### Writing

This is the basic data structure for writing memories.

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

This is an example function that, in this case, writes to %R memories, but the logic applies to any type of memory we want to write to.

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

Data to be inserted:

- **msg_send[30]**: An index that increments with each data transmission by the client.

- **msg_send[4]**: Number of bytes to write (LSB)

- **msg_send[5]**: Number of bytes to write (MSB)

- **msg_send[50]**: A byte that corresponds to the code for writing memories.

- **msg_send[51]**: A byte that corresponds to the code for the memory type.

- **msg_send[52]**: Start memory (LSB)

- **msg_send[53]**: Start memory (MSB)

- **msg_send[54]**: Number of memory (LSB)

- **msg_send[55]**: Number of memory (MSB)

In this case, we need to send two packets: the first one is **msg_send**, and the second one is an array that should have the specified dimensions and contains the values we actually want to write. Once sent, the PLC will respond with **0x03** if everything went well.

#### Memory Type Code

These are all the bytes that correspond to the memory type you want to read or write.

In this library, for now, only **AI**, **AQ**, and **R** memories are implemented due to work-related reasons. However, in the future, my goal is to implement all types of memories to make it as comprehensive as possible.

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

These are all the types of requests that can be made to the PLC. Currently, only **READ_SYS_MEMORY** and **WRITE_SYS_MEMORY** are implemented.

In the future, I plan to implement only the **PLC_STATUS** request because, at the SCADA software level, I don't see the need for other types of requests.

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

## Examples

For those who don't want to modify the library but only use it, here is an example of reading:

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

Here's an example of writing using the library:

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

## Important

This library is still in the beta version, even though it has been tested multiple times. I recommend performing thorough testing on a test bench to ensure that everything functions correctly before implementing it in the supervision software. It's always good practice to validate the library's performance and stability in a controlled environment before deploying it in a production system.

As mentioned earlier, the library is open source, so anyone who wants to contribute to its development is welcome to do so. I encourage you to share your updates and improvements so that the official version can be updated accordingly. Additionally, please feel free to report any bugs you encounter, as this will help in resolving them. Collaboration and community involvement are key to the success and improvement of open-source projects.

## TODO

- [ ] Developing read and write capabilities for all types of memory.

- [ ] Developing all types of requests.

- [ ] Verifying that SCADA enablement indeed allows monitoring by multiple clients.

- [ ] Developing read and write capabilities for DWORD and FLOAT data types as well.
