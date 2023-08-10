/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package com.gesrtp.jgesrtp;

import java.io.OutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.net.InetAddress;

/**
 *
 * @author Matteo
 */
public class JGESRTP {

    private static byte[] msg_init = new byte[56]; //messaggio per la connessione e disconnessione con il PLC

    private static byte[] msg_scada = {  //messaggio per l'abilitazione al monitoraggio SCADA
            0x08,        // 00 - Myst be 08 - Unknown Type (used by SCADAs)
            0x00,        // 01 - Unknown
            0x01,        // 02 - Seq Number
            0x00,        // 03 - Unknown
            0x00,        // 04 - Text Length
            0x00,        // 05 - Unknown / Text character?
            0x00,        // 06 - Unknown / Text character?
            0x00,        // 07 - Unknown / Text character?
            0x00,        // 08 - Unknown / Text character?
            0x01,        // 09 - Unknown / Text character?
            0x00,        // 10 - Unknown / Text character?
            0x00,        // 11 - Unknown / Text character?
            0x00,        // 12 - Unknown / Text character?
            0x00,        // 13 - Unknown / Text character?
            0x00,        // 14 - Unknown / Text character?
            0x00,        // 15 - Unknown / Text character?
            0x00,        // 16 - Unknown / Text character?
            0x01,        // 17 - Unknown / Always x01
            0x00,        // 18 - Unknown
            0x00,        // 19 - Unknown
            0x00,        // 20 - Unknown
            0x00,        // 21 - Unknown
            0x00,        // 22 - Unknown
            0x00,        // 23 - Unknown
            0x00,        // 24 - Unknown
            0x00,        // 25 - Unknown
            0x00,        // 26 - Time Seconds
            0x00,        // 27 - Time Minutes
            0x00,        // 28 - Time Hours
            0x00,        // 29 - Reserved / Always x01
            0x01,        // 30 - Seq Number (Repeated) (0x06) - meaning in responces from PLC
            (byte)0xc0,  // 31 - Message Type
            0x00,        // 32 - Mailbox Source
            0x00,        // 33 - Mailbox Source
            0x00,        // 34 - Mailbox Source
            0x00,        // 35 - Mailbox Source
            0x10,        // 36 - Mailbox Destination // dec: 3600
            0x0e,        // 37 - Mailbox Destination
            0x00,        // 38 - Mailbox Destination
            0x00,        // 39 - Mailbox Destination
            0x01,        // 40 - Packet Number // to check
            0x01,        // 41 - Total Packet Number
            0x4f,        // 42 - Service Request Code - (Some optional SERVICE_REQUEST_CODE - unknown)
            0x01,        // 43 - Request Dependent Space (0x01 must be for 0x4f Service Request)
            0x00,        // 43 - Dependent (0x00 here)
            0x00,        // 44 - Dependent (0x00 here)
            0x00,        // 45 - Dependent (0x00 here)
            0x00,        // 46 - Dependent (0x00 here)
            0x00,        // 47 - Dependent (0x00 here)
            0x00,        // 48 - Dependent (0x00 here)
            0x00,        // 49 - Dependent (0x00 here)
            0x00,        // 50 - Dependent (0x00 here)
            0x00,        // 51 - Dependent (0x00 here)
            0x00,        // 52 - Dependent (0x00 here)
            0x00,        // 53 - Dependent (0x00 here)
            0x00,        // 54 - Dependent (0x00 here)
            0x00         // 55 - Dependent (0x00 here)
        };
    
    private static byte[] msg_read = { //messaggio base per la lettura
            0x02,        // 00 - Type (x03:=ReceiveOK, x02:=Transmit, x08:=Something just after msg_init - maybe kind of exception interruption?)
            0x00,        // 01 - Unknown
            0x00,        // 02 - Seq Number (x00 works, x06 also works, x05 also according referenced document)
            0x00,        // 03 - Unknown
            0x00,        // 04 - Text Length
            0x00,        // 05 - Unknown / Text character?
            0x00,        // 06 - Unknown / Text character?
            0x00,        // 07 - Unknown / Text character?
            0x00,        // 08 - Unknown / Text character?
            0x01,        // 09 - Unknown / Text character?
            0x00,        // 10 - Unknown / Text character?
            0x00,        // 11 - Unknown / Text character?
            0x00,        // 12 - Unknown / Text character?
            0x00,        // 13 - Unknown / Text character?
            0x00,        // 14 - Unknown / Text character?
            0x00,        // 15 - Unknown / Text character?
            0x00,        // 16 - Unknown / Text character?
            0x01,        // 17 - Unknown / Always x01
            0x00,        // 18 - Unknown
            0x00,        // 19 - Unknown
            0x00,        // 20 - Unknown
            0x00,        // 21 - Unknown
            0x00,        // 22 - Unknown
            0x00,        // 23 - Unknown
            0x00,        // 24 - Unknown
            0x00,        // 25 - Unknown
            0x00,        // 26 - Time Seconds
            0x00,        // 27 - Time Minutes
            0x00,        // 28 - Time Hours
            0x00,        // 29 - Reserved / Always x01
            0x00,        // 30 - Seq Number (Repeated) (0x06) - meaning in responces from PLC
            (byte)0xc0,  // 31 - Message Type
            0x00,        // 32 - Mailbox Source
            0x00,        // 33 - Mailbox Source
            0x00,        // 34 - Mailbox Source
            0x00,        // 35 - Mailbox Source
            0x10,        // 36 - Mailbox Destination // dec: 3600
            0x0e,        // 37 - Mailbox Destination
            0x00,        // 38 - Mailbox Destination
            0x00,        // 39 - Mailbox Destination
            0x01,        // 40 - Packet Number // to check
            0x01,        // 41 - Total Packet Number
            0x00,        // 42 - Service Request Code - (Operation Type SERVICE_REQUEST_CODE)
            0x00,        // 43 - Request Dependent Space (Ex. MEMORY_TYPE_CODE)
            0x00,        // 44 - Request Dependent Space (Ex. Address:LSB)
            0x00,        // 45 - Request Dependent Space (Ex. Address:MSB)
            0x00,        // 46 - Request Dependent Space (Ex. Data Size Words:LSB)
            0x00,        // 47 - Request Dependent Space (Ex. Data Size Words:MSB)
            0x00,        // 48 - Request Dependent Space (Ex. Write Value:LSB)
            0x00,        // 49 - Request Dependent Space (Ex. Write Value:MSB)
            0x00,        // 50 - Request Dependent Space (Ex. Write Value Part 2 for LONG:LSB)
            0x00,        // 51 - Request Dependent Space (Ex. Write Value Part 2 for LONG:MSB)
            0x00,        // 52 - Dependent of "Data Size" - byte 46, 47 / PLC status in other Service Request
            0x00,        // 53 - Dependent of "Data Size" - byte 46, 47 / PLC status in other Service Request
            0x00,        // 54 - Dependent of "Data Size" - byte 46, 47 / PLC status in other Service Request
            0x00         // 55 - Dependent of "Data Size" - byte 46, 47 / PLC status in other Service Request
        };

    private static byte[] msg_write = { //messaggio base per la scrittura
            0x02,        // 00 - Type (x03:=ReceiveOK, x02:=Transmit, x08:=Something just after msg_init - maybe kind of exception interruption?)
            0x00,        // 01 - Unknown
            0x00,        // 02 - Seq Number
            0x00,        // 03 - Unknown
            0x00,        // 04 - Number of bytes to write LSB
            0x00,        // 05 - Number of bytes to write MSB
            0x00,        // 06 - Unknown / Text character?
            0x00,        // 07 - Unknown / Text character?
            0x00,        // 08 - Unknown / Text character?
            0x02,        // 09 - Unknown / Text character?
            0x00,        // 10 - Unknown / Text character?
            0x00,        // 11 - Unknown / Text character?
            0x00,        // 12 - Unknown / Text character?
            0x00,        // 13 - Unknown / Text character?
            0x00,        // 14 - Unknown / Text character?
            0x00,        // 15 - Unknown / Text character?
            0x00,        // 16 - Unknown / Text character?
            0x02,        // 17 - Unknown / Always x01
            0x00,        // 18 - Unknown
            0x00,        // 19 - Unknown
            0x00,        // 20 - Unknown
            0x00,        // 21 - Unknown
            0x00,        // 22 - Unknown
            0x00,        // 23 - Unknown
            0x00,        // 24 - Unknown
            0x00,        // 25 - Unknown
            0x00,        // 26 - Time Seconds
            0x00,        // 27 - Time Minutes
            0x00,        // 28 - Time Hours
            0x00,        // 29 - Reserved / Always x01
            0x00,        // 30 - Seq Number (Repeated) (0x06) - meaning in responces from PLC
            (byte)0x80,  // 31 - Message Type
            0x00,        // 32 - Mailbox Source
            0x00,        // 33 - Mailbox Source
            0x00,        // 34 - Mailbox Source
            0x00,        // 35 - Mailbox Source
            0x10,        // 36 - Mailbox Destination // dec: 3600
            0x0e,        // 37 - Mailbox Destination
            0x00,        // 38 - Mailbox Destination
            0x00,        // 39 - Mailbox Destination
            0x01,        // 40 - Packet Number // to check
            0x01,        // 41 - Total Packet Number
            0x32,        // 42 - Service Request Code - (Operation Type SERVICE_REQUEST_CODE)
            0x00,        // 43 - Request Dependent Space (Ex. MEMORY_TYPE_CODE)
            0x00,        // 44 - Request Dependent Space (Ex. Address:LSB)
            0x00,        // 45 - Request Dependent Space (Ex. Address:MSB)
            0x00,        // 46 - Request Dependent Space (Ex. Data Size Words:LSB)
            0x00,        // 47 - Request Dependent Space (Ex. Data Size Words:MSB)
            0x01,        // 48 - Request Dependent Space (Ex. Write Value:LSB)
            0x01,        // 49 - Request Dependent Space (Ex. Write Value:MSB)
            0x00,        // 50 - Request Dependent Space (Ex. Write Value Part 2 for LONG:LSB)
            0x00,        // 51 - Request Dependent Space (Ex. Write Value Part 2 for LONG:MSB)
            0x00,        // 52 - Dependent of "Data Size" - byte 46, 47 / PLC status in other Service Request
            0x00,        // 53 - Dependent of "Data Size" - byte 46, 47 / PLC status in other Service Request
            0x00,        // 54 - Dependent of "Data Size" - byte 46, 47 / PLC status in other Service Request
            0x00         // 55 - Dependent of "Data Size" - byte 46, 47 / PLC status in other Service Request
        };

    private static final int PLC_PORT = 18245;
    private static Socket client;
    private static OutputStream os;
    private static InputStream is;
    private static int n_transfer = 0;
    private static boolean connected; 
    
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
                    os.close();
                    is.close();
                    connected = false;
                    return -2;
                }
            }else{
                connected = false;
                return -1;
            }
        }catch (Exception ex){
            connected = false;
            return -1;
        }
    }

    public static int closeConnection(){
        byte[] data = new byte[1024];
        try{
            if (client.isConnected()){
                os.write(msg_init, 0, msg_init.length);
                is.read(data, 0, data.length);
                os.close();
                is.close();
                client.close();
                n_transfer = 0;
                connected = false;
                return 0;
            }
            connected = false;
            return -1;
        }catch (Exception ex){
            connected = false;
            return -1;
        }
    }

    public static int initScada(){
        byte[] data = new byte[1024];
        try{
            if (client.isConnected()){
                os.write(msg_scada, 0, 56);
                is.read(data, 0, data.length);
                if (data[0] == 0x03){
                    return 0;
                }else{
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
    
    public boolean connected(){
        return connected;
    }

    public static int[] read_R_WORD(int address, int number){   
        //preparazione varibaili
        byte[] msg_send = new byte[56]; //messaggio che invio al plc
        byte[] msg_response = new byte[56]; //prima risposta del plc
        byte[] data = new byte[number * 2]; //seconda risposta dal plc con gli effettivi dati
        int[] val = new int[number];
        msg_send = msg_read;

        //numero di transazione
        msg_send[3] = (byte) n_transfer;
        msg_send[30] = (byte) n_transfer;
        n_transfer++;
        if(n_transfer > 255){n_transfer = 0;}

        //tipo di comando
        msg_send[42] = (byte)SERVICE_REQUEST.READ_SYS_MEMORY;

        //tipo di memoria
        msg_send[43] = (byte)MEMORY_TYPE.R;

        //registro iniziale
        address = address - 1;
        msg_send[44] = (byte)(address & 0xFF);    // Get LSB of Word
        msg_send[45] = (byte)(address >> 8);      // Get MSB of Word
            
        //numero di registri
        msg_send[46] = (byte)(number & 0xFF);    // Get LSB of Word
        msg_send[47] = (byte)(number >> 8);      // Get MSB of Word

        int c = 0;
        try{
            if (client.isConnected()){   
                os.write(msg_send, 0, msg_send.length);
                is.read(msg_response, 0, msg_response.length);
                if (msg_response[0] == 0x03){
                    if(number > 3){
                        is.read(data, 0, data.length);
                        for(int i = 0; i < data.length; i+=2){
                            //val[c] = (data[i + 1] << 8) + data[i];
                            val[c] = byte_to_word(data[i], data[i + 1]);
                            c++;
                        }
                    }else{
                        for(int i = 44; i < 50; i+=2){
                            //val[c] = (msg_response[i + 1] << 8) + msg_response[i];
                            val[c] = byte_to_word(msg_response[i], msg_response[i + 1]);
                            c++;
                        }
                    }
                    return val;
                }else{
                    System.out.println("Error on response.");
                }
            }else{
                System.out.println("Not connected");
            }
        }catch (Exception ex){
            return null;
        }

        return null;
    }

    public static int[] read_AI_WORD(int address, int number){   
        //preparazione varibaili
        byte[] msg_send = new byte[56]; //messaggio che invio al plc
        byte[] msg_response = new byte[56]; //prima risposta del plc
        byte[] data = new byte[number * 2]; //seconda risposta dal plc con gli effettivi dati
        int[] val = new int[number];
        msg_send = msg_read;

        //numero di transazione
        msg_send[3] = (byte) n_transfer;
        msg_send[30] = (byte) n_transfer;
        n_transfer++;
        if(n_transfer > 255){n_transfer = 0;}

        //tipo di comando
        msg_send[42] = (byte)SERVICE_REQUEST.READ_SYS_MEMORY;

        //tipo di memoria
        msg_send[43] = (byte)MEMORY_TYPE.AI;

        //registro iniziale
        address = address - 1;
        msg_send[44] = (byte)(address & 0xFF);    // Get LSB of Word
        msg_send[45] = (byte)(address >> 8);      // Get MSB of Word
            
        //numero di registri
        msg_send[46] = (byte)(number & 0xFF);    // Get LSB of Word
        msg_send[47] = (byte)(number >> 8);      // Get MSB of Word

        int c = 0;
        try{
            if (client.isConnected()){   
                os.write(msg_send, 0, msg_send.length);
                is.read(msg_response, 0, msg_response.length);
                if (msg_response[0] == 0x03){
                    if(number > 3){
                        is.read(data, 0, data.length);
                        for(int i = 0; i < data.length; i+=2){
                            //val[c] = (data[i + 1] << 8) + data[i];
                            val[c] = byte_to_word(data[i], data[i + 1]);
                            c++;
                        }
                    }else{
                        for(int i = 44; i < 50; i+=2){
                            //val[c] = (msg_response[i + 1] << 8) + msg_response[i];
                            val[c] = byte_to_word(msg_response[i], msg_response[i + 1]);
                            c++;
                        }
                    }
                    return val;
                }else{
                    System.out.println("Error on response.");
                }
            }else{
                System.out.println("Not connected");
            }
        }catch (Exception ex){
            return null;
        }

        return null;
    }

    public static int[] read_AQ_WORD(int address, int number){   
        //preparazione varibaili
        byte[] msg_send = new byte[56]; //messaggio che invio al plc
        byte[] msg_response = new byte[56]; //prima risposta del plc
        byte[] data = new byte[number * 2]; //seconda risposta dal plc con gli effettivi dati
        int[] val = new int[number];
        msg_send = msg_read;

        //numero di transazione
        msg_send[3] = (byte) n_transfer;
        msg_send[30] = (byte) n_transfer;
        n_transfer++;
        if(n_transfer > 255){n_transfer = 0;}

        //tipo di comando
        msg_send[42] = (byte)SERVICE_REQUEST.READ_SYS_MEMORY;

        //tipo di memoria
        msg_send[43] = (byte)MEMORY_TYPE.AQ;

        //registro iniziale
        address = address - 1;
        msg_send[44] = (byte)(address & 0xFF);    // Get LSB of Word
        msg_send[45] = (byte)(address >> 8);      // Get MSB of Word
            
        //numero di registri
        msg_send[46] = (byte)(number & 0xFF);    // Get LSB of Word
        msg_send[47] = (byte)(number >> 8);      // Get MSB of Word

        int c = 0;
        try{
            if (client.isConnected()){   
                os.write(msg_send, 0, msg_send.length);
                is.read(msg_response, 0, msg_response.length);
                if (msg_response[0] == 0x03){
                    if(number > 3){
                        is.read(data, 0, data.length);
                        for(int i = 0; i < data.length; i+=2){
                            //val[c] = (data[i + 1] << 8) + data[i];
                            val[c] = byte_to_word(data[i], data[i + 1]);
                            c++;
                        }
                    }else{
                        for(int i = 44; i < 50; i+=2){
                            //val[c] = (msg_response[i + 1] << 8) + msg_response[i];
                            val[c] = byte_to_word(msg_response[i], msg_response[i + 1]);
                            c++;
                        }
                    }
                    return val;
                }else{
                    System.out.println("Error on response.");
                }
            }else{
                System.out.println("Not connected");
            }
        }catch (Exception ex){
            return null;
        }

        return null;
    }

    public static boolean write_R_WORD(int address, int[] valori){
        byte[] msg_send = new byte[56]; //messaggio che invio al plc
        byte[] msg_read = new byte[56]; //risposta dal plc
        byte[] data = new byte[valori.length * 2]; //seconda messaggio con i dati effettivi
        msg_send = msg_write;

        //numero di transazione
        msg_send[3] = (byte) n_transfer;
        msg_send[30] = (byte) n_transfer;
        n_transfer++;
        if(n_transfer > 255){n_transfer = 0;}

        //numero di byte che nel secondo pacchetto trasmetto
        int n_bytes = valori.length * 2;
        msg_send[4] = (byte)(n_bytes & 0xFF);    // Get LSB of Word
        msg_send[5] = (byte)(n_bytes >> 8);      // Get MSB of Word

        //tipo di comando
        msg_send[50] = (byte)SERVICE_REQUEST.WRITE_SYS_MEMORY;

        //tipo di memoria
        msg_send[51] = (byte)MEMORY_TYPE.R;

        //registro iniziale
        address = address - 1;
        msg_send[52] = (byte)(address & 0xFF);    // Get LSB of Word
        msg_send[53] = (byte)(address >> 8);      // Get MSB of Word
        
        //numero di registri
        msg_send[54] = (byte)(valori.length & 0xFF);    // Get LSB of Word
        msg_send[55] = (byte)(valori.length >> 8);      // Get MSB of Word

        //preparazione dei dati da scrivere
        int c = 0;
        for(int i = 0; i < valori.length; i++){
            data[c] = (byte)(valori[i] & 0xFF);    // Get LSB of Word
            data[c + 1] = (byte)(valori[i] >> 8);      // Get MSB of Word
            c += 2;
        }

        try{
            if (client.isConnected()){   
                //this.stream.Write(null);
                os.write(msg_send, 0, msg_send.length);
                os.write(data, 0, data.length);
                is.read(msg_read, 0, msg_read.length);
                if (msg_read[0] == 0x03){
                    return true;
                }else{
                    System.out.println("Error on response.");
                }
            }else{
                System.out.println("Not connected.");
            }
        }catch (Exception ex){
            return false;
        }

        return false;
    }

    public static boolean write_AI_WORD(int address, int[] valori){
        byte[] msg_send = new byte[56]; //messaggio che invio al plc
        byte[] msg_read = new byte[56]; //risposta dal plc
        byte[] data = new byte[valori.length * 2]; //seconda messaggio con i dati effettivi
        msg_send = msg_write;

        //numero di transazione
        msg_send[3] = (byte) n_transfer;
        msg_send[30] = (byte) n_transfer;
        n_transfer++;
        if(n_transfer > 255){n_transfer = 0;}

        //numero di byte che nel secondo pacchetto trasmetto
        int n_bytes = valori.length * 2;
        msg_send[4] = (byte)(n_bytes & 0xFF);    // Get LSB of Word
        msg_send[5] = (byte)(n_bytes >> 8);      // Get MSB of Word

        //tipo di comando
        msg_send[50] = (byte)SERVICE_REQUEST.WRITE_SYS_MEMORY;

        //tipo di memoria
        msg_send[51] = (byte)MEMORY_TYPE.AI;

        //registro iniziale
        address = address - 1;
        msg_send[52] = (byte)(address & 0xFF);    // Get LSB of Word
        msg_send[53] = (byte)(address >> 8);      // Get MSB of Word
        
        //numero di registri
        msg_send[54] = (byte)(valori.length & 0xFF);    // Get LSB of Word
        msg_send[55] = (byte)(valori.length >> 8);      // Get MSB of Word

        //preparazione dei dati da scrivere
        int c = 0;
        for(int i = 0; i < valori.length; i++){
            data[c] = (byte)(valori[i] & 0xFF);    // Get LSB of Word
            data[c + 1] = (byte)(valori[i] >> 8);      // Get MSB of Word
            c += 2;
        }

        try{
            if (client.isConnected())
            {   
                //this.stream.Write(null);
                os.write(msg_send, 0, msg_send.length);
                os.write(data, 0, data.length);
                is.read(msg_read, 0, msg_read.length);
                if (msg_read[0] == 0x03){
                    return true;
                }else{
                    System.out.println("Error on response.");
                }
            }else{
                System.out.println("Not connected.");
            }
        }catch (Exception ex){
            return false;
        }

        return false;
    }

    public static boolean write_AQ_WORD(int address, int[] valori){
        byte[] msg_send = new byte[56]; //messaggio che invio al plc
        byte[] msg_read = new byte[56]; //risposta dal plc
        byte[] data = new byte[valori.length * 2]; //seconda messaggio con i dati effettivi
        msg_send = msg_write;

        //numero di transazione
        msg_send[3] = (byte) n_transfer;
        msg_send[30] = (byte) n_transfer;
        n_transfer++;
        if(n_transfer > 255){n_transfer = 0;}

        //numero di byte che nel secondo pacchetto trasmetto
        int n_bytes = valori.length * 2;
        msg_send[4] = (byte)(n_bytes & 0xFF);    // Get LSB of Word
        msg_send[5] = (byte)(n_bytes >> 8);      // Get MSB of Word

        //tipo di comando
        msg_send[50] = (byte)SERVICE_REQUEST.WRITE_SYS_MEMORY;

        //tipo di memoria
        msg_send[51] = (byte)MEMORY_TYPE.AQ;

        //registro iniziale
        address = address - 1;
        msg_send[52] = (byte)(address & 0xFF);    // Get LSB of Word
        msg_send[53] = (byte)(address >> 8);      // Get MSB of Word
        
        //numero di registri
        msg_send[54] = (byte)(valori.length & 0xFF);    // Get LSB of Word
        msg_send[55] = (byte)(valori.length >> 8);      // Get MSB of Word

        //preparazione dei dati da scrivere
        int c = 0;
        for(int i = 0; i < valori.length; i++){
            data[c] = (byte)(valori[i] & 0xFF);    // Get LSB of Word
            data[c + 1] = (byte)(valori[i] >> 8);      // Get MSB of Word
            c += 2;
        }

        try{
            if (client.isConnected())
            {   
                //this.stream.Write(null);
                os.write(msg_send, 0, msg_send.length);
                os.write(data, 0, data.length);
                is.read(msg_read, 0, msg_read.length);
                if (msg_read[0] == 0x03){
                    return true;
                }else{
                    System.out.println("Error on response.");
                }
            }else{
                System.out.println("Not connected.");
            }
        }catch (Exception ex){
            return false;
        }

        return false;
    }
    
    public static int byte_to_word(byte lsb, byte msb){
        int l = lsb;
        if(l < 0){l += 256;}
        int m = msb;
        if(m < 0){m += 256;}
        int val = (m * 256) + l;
        if(val > 32767){val -= 65536;}
        return val;
    }

    /*public static void main(String[] args) throws Exception {

        int status_connection = initConnection("10.100.100.80");
        if(status_connection == 0){
            int status_scada = initScada();
            if(status_scada == 0){
                /*int[] register = read_R_WORD(5001, 250); //Read 100R from 1R
                for(int i = 0; i < register.length; i++){
                    System.out.println(register[i]);
                }*/
                /*int[] valori = {-32767};
                boolean w = write_R_WORD(1, valori);
                System.out.println(w);
                closeConnection();
            }else{
                closeConnection();
            }
        }else{
            closeConnection();
        }
    }*/

}

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

class SERVICE_REQUEST{
    public static byte PLC_STATUS = 0x00;
    public static byte RETURN_PROG_NAME = 0x03;
    public static byte READ_SYS_MEMORY = 0x04;    // Used to read general memory (Example: %R123)
    public static byte READ_TASK_MEMORY = 0x05;
    public static byte READ_PROG_MEMORY = 0x06;
    public static byte WRITE_SYS_MEMORY = 0x07;   // Used to write general memory
    public static byte WRITE_TASK_MEMORY = 0x08;
    public static byte WRITE_PROG_MEMORY = 0x09;
    public static byte RETURN_DATETIME = 0x25;
    public static byte RETURN_CONTROLLER_TYPE = 0x43;
}
