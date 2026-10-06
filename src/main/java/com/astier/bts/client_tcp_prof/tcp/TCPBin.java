package com.astier.bts.client_tcp_prof.tcp;

import com.astier.bts.client_tcp_prof.HelloController;
import com.astier.bts.client_tcp_prof.aes.Aes_cbc;
import javafx.application.Platform;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class TCPBin extends Thread {
    int port;
    InetAddress serveur;
    Socket socket;
    boolean marche = false;
    public boolean connection = false;
    OutputStream out;
    InputStream  in;
    HelloController fxmlCont;


    public TCPBin(InetAddress serveur, int port, HelloController fxmlCont) {
        this.port = port;
        this.serveur = serveur;
        this.fxmlCont = fxmlCont;
        System.out.println("@ serveur: " + serveur + " port: " + port);
    }

    public void connection() {
        try {
            socket = new Socket(serveur, port);
            out = socket.getOutputStream();
            in = socket.getInputStream();
            marche = true;
            connection = true;
            System.out.println("Connexion TCP OK");
        } catch (Exception e) {
            connection = false;
            marche = false;
            System.out.println("Erreur connexion TCP : " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void deconnection() throws InterruptedException {
        try {
            marche = false;
            connection = false;

            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            System.out.println("Déconnexion TCP OK");

        } catch (Exception e) {
            System.out.println("Erreur déconnexion TCP : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void requette(String laRequette) throws IOException {
        byte[] clair = laRequette.getBytes();
        byte[] crypte = fxmlCont.aes.cryptage(clair);

        out.write(crypte);
        out.flush();

        System.out.println("Requête cryptée envoyée (" + crypte.length + " octets)");
    }

    @Override
    public void run() {
        try {
            while (marche) {

                byte[] buffer = new byte[65535];
                int lu = in.read(buffer);

                if (lu == -1) {
                    marche = false;
                    break;
                }

                byte[] crypte = Arrays.copyOf(buffer, lu);


                byte[] clair = fxmlCont.aes.decryptage(crypte);

                String message = new String(clair);

                updateMessage("Message serveur : " + message);
            }

        } catch (Exception e) {
            updateMessage("Erreur dans run() : " + e.getMessage());
        }

        marche = false;
        connection = false;

        Platform.runLater(() ->
                fxmlCont.TextAreaReponses.appendText("Connexion perdue\n")
        );
    }





    /*
    Pour déclencher une opération graphique en dehors du thread graphique  utiliser
    javafx.application.Platform.runLater(java.lang.Runnable)
    Cette méthode permet d'éxécuter le code du runnable par le thread graphique de JavaFX.
    */
    protected void updateMessage(String message) {
        Platform.runLater(() -> fxmlCont.TextAreaReponses.appendText("    MESSAGE SERVEUR >  \n      " + message + "\n"));
    }
}
