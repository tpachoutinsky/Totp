package com.astier.bts.client_tcp_prof;

import com.astier.bts.client_tcp_prof.aes.Connexion;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Multicast {

    private static final String GROUPE_MULTICAST = "224.0.0.250";
    private static final int PORT_MULTICAST = 5555;
    private static final int PORT_REPONSE = 5556;
    private static final int TIMEOUT_MS = 2000; // 2 secondes pour scanner plusieurs serveurs

    private final String nomInterface;

    public Multicast(String nomInterface) {
        this.nomInterface = nomInterface;
    }

    /**
     * Scan multicast : retourne TOUS les serveurs trouvés
     */
    public List<Connexion> discoverAll() throws IOException {

        NetworkInterface ni = NetworkInterface.getByName(nomInterface);
        if (ni == null) {
            throw new IOException("Interface réseau introuvable : " + nomInterface);
        }

        InetAddress groupe = InetAddress.getByName(GROUPE_MULTICAST);
        List<Connexion> serveursTrouves = new ArrayList<>();

        try (MulticastSocket ms = new MulticastSocket(PORT_MULTICAST);
             DatagramSocket dsReponse = new DatagramSocket(PORT_REPONSE)) {

            ms.setNetworkInterface(ni);
            ms.setTimeToLive((byte) 60);
            ms.joinGroup(groupe);

            dsReponse.setSoTimeout(TIMEOUT_MS);

            byte[] data = "Tu es qui?".getBytes(StandardCharsets.UTF_8);
            DatagramPacket demande = new DatagramPacket(data, data.length, groupe, PORT_MULTICAST);
            ms.send(demande);

            long start = System.currentTimeMillis();

            while (System.currentTimeMillis() - start < TIMEOUT_MS) {

                byte[] buffer = new byte[256];
                DatagramPacket reponse = new DatagramPacket(buffer, buffer.length);

                try {
                    dsReponse.receive(reponse);
                } catch (SocketTimeoutException e) {
                    break;
                }

                String message = new String(reponse.getData(), 0, reponse.getLength(), StandardCharsets.UTF_8);
                String[] parts = message.split(";");

                if (parts.length == 3) {
                    InetAddress ipServeur = InetAddress.getByName(parts[0].trim());
                    int portTCP = Integer.parseInt(parts[1].trim());
                    int portUDP = Integer.parseInt(parts[2].trim());
                    serveursTrouves.add(new Connexion(ipServeur, portTCP, portUDP));
                }
            }

            ms.leaveGroup(groupe);
        }

        return serveursTrouves;
    }

}
