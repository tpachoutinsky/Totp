package DeffieHellman;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Client {

    public static void main(String[] args) throws Exception {

        BigInteger a = new BigInteger(128, new SecureRandom());
        System.out.println("a = " + a);

        BigInteger p = BigInteger.probablePrime(128, new SecureRandom());
        System.out.println("p = " + p);

        BigInteger g;
        do {
            g = new BigInteger(128, new SecureRandom());
        } while (g.compareTo(BigInteger.ONE) <= 0 || g.compareTo(p) >= 0);

        System.out.println("g = " + g);


        BigInteger A = g.modPow(a, p);
        System.out.println("A = " + A);

        Socket client = new Socket(InetAddress.getByName("127.0.0.1"), 5555);
        InputStream in = client.getInputStream();
        OutputStream out = client.getOutputStream();

        out.write(p.toByteArray());
        out.write(g.toByteArray());
        out.write(A.toByteArray());
        out.flush();

        byte[] bufB = new byte[65535];
        int taille = in.read(bufB);
        BigInteger B = new BigInteger(Arrays.copyOfRange(bufB, 0, taille));
        System.out.println("Serveur : B = " + B);

        BigInteger K = B.modPow(a, p);
        byte[] byteArray = K.toByteArray();

        System.out.println("""
                Clé privée du cryptage symétrique   ----> %s
                En héxadecimal                      ----> %s
                En Utf-8 (si possible)              ----> %s
                """.formatted(
                K,
                toHex(byteArray),
                new String(byteArray, StandardCharsets.UTF_8)
        ));

        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] cleAES = Arrays.copyOf(sha.digest(K.toByteArray()), 16);
        System.out.println("Clé AES (hex) = " + toHex(cleAES));

        client.close();
    }

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }
}
