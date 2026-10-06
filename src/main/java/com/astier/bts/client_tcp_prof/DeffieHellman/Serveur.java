package com.astier.bts.client_tcp_prof.DeffieHellman;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

public class Serveur {

    public static void main(String[] args) throws Exception {

        ServerSocket ss = new ServerSocket(5555);
        System.out.println("Serveur en attente...");
        Socket client = ss.accept();
        System.out.println("Client connecté : " + client);

        InputStream in = client.getInputStream();
        OutputStream out = client.getOutputStream();

        BigInteger p = readBigInteger(in);
        System.out.println("p = " + p);

        BigInteger g = readBigInteger(in);
        System.out.println("g = " + g);

        BigInteger A = readBigInteger(in);
        System.out.println("A = " + A);

        BigInteger b = new BigInteger(128, new SecureRandom());
        System.out.println("b = " + b);

        BigInteger B = g.modPow(b, p);
        System.out.println("B = " + B);


        sendBigInteger(out, B);
        out.flush();

        BigInteger K = A.modPow(b, p);
        System.out.println("Clé secrète K = " + K);

        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] cleAES = Arrays.copyOf(sha.digest(K.toByteArray()), 16);
        System.out.println("Clé AES (hex) = " + toHex(cleAES));

        client.close();
        ss.close();
    }

    private static void sendBigInteger(OutputStream out, BigInteger x) throws Exception {
        byte[] data = x.toByteArray();
        if (data.length > 255) {
            throw new IllegalArgumentException("BigInteger trop grand pour ce protocole");
        }
        out.write(data.length);
        out.write(data);
    }

    private static BigInteger readBigInteger(InputStream in) throws Exception {
        int size = in.read();
        if (size == -1) {
            throw new IllegalStateException("Flux fermé avant lecture de la taille");
        }
        byte[] data = new byte[size];
        int read = 0;
        while (read < size) {
            int r = in.read(data, read, size - read);
            if (r == -1) {
                throw new IllegalStateException("Flux fermé pendant lecture des données");
            }
            read += r;
        }
        return new BigInteger(data);
    }

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }
}
