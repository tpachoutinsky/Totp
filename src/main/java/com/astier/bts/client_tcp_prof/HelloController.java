package com.astier.bts.client_tcp_prof;

import com.astier.bts.client_tcp_prof.aes.Aes_cbc;
import com.astier.bts.client_tcp_prof.aes.Connexion;
import Outils.Outils;
import com.astier.bts.client_tcp_prof.tcp.TCPBin;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.shape.Circle;
import modeles.Ipv4;

import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import static javafx.scene.paint.Color.*;

public class HelloController implements Initializable {


    public Button button;
    public Button connecter;
    public Button deconnecter;
    public TextField TextFieldIP;
    public TextField TextFieldPort;
    public TextField TextFieldRequette;
    public Circle voyant;
    public TextArea TextAreaReponses;
    static public TCPBin tcp;
    static boolean enRun = false;
    public ChoiceBox choicebox;
    public Aes_cbc aes;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        voyant.setFill(RED);

        try {
            chargerConfigAES();
        } catch (Exception e) {
            TextAreaReponses.appendText("Erreur config AES : " + e.getMessage() + "\n");
        }


        try {
            ArrayList<Ipv4> interfaces = ScanInterfaces.getSystemIP();
            interfaces.forEach(ip -> {
                choicebox.getItems().add(ip.nominterfacename() + " (" + ip.ip() + ")");
            });
        } catch (Exception e) {
            TextAreaReponses.appendText("Erreur scan interfaces : " + e.getMessage() + "\n");
        }

        connecter.setOnAction(event -> {
            try{
                connecter();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        deconnecter.setOnAction(event -> {
            try{
                deconnecter();
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        });

        button.setOnAction(event -> {
            try{
                envoyer();
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        });
    }

    private void chargerConfigAES() {
        try {
            InputStream is = HelloController.class.getResourceAsStream("/configuration_json.json");

            if (is == null) {
                TextAreaReponses.appendText("Erreur : configuration_json.json introuvable dans /resources\n");
                return;
            }

            String json = new String(is.readAllBytes());

            String keyStr = json.split("\"motDePasse\"")[1]
                    .split(":")[1]
                    .replace("\"", "")
                    .replace(",", "")
                    .trim();

            String ivStr = json.split("\"iv\"")[1]
                    .split(":")[1]
                    .replace("\"", "")
                    .replace("}", "")
                    .trim();

            if (keyStr.isEmpty() || ivStr.isEmpty()) {
                TextAreaReponses.appendText("Erreur : motDePasse ou iv manquant dans le JSON\n");
                return;
            }

            byte[] key = Outils.normalizeChaine(keyStr, 16);
            byte[] iv  = Outils.normalizeChaine(ivStr, 16);


            aes = new Aes_cbc(key, iv);

        } catch (Exception e) {
            TextAreaReponses.appendText("Erreur config AES : " + e.getMessage() + "\n");
        }
    }

    private void envoyer() {
        String requette = TextFieldRequette.getText();

        if (requette.isEmpty()) {
            TextAreaReponses.appendText("Requête vide\n");
            return;
        }

        if (!enRun || tcp == null) {
            TextAreaReponses.appendText("Non connecté\n");
            return;
        }

        try {
            tcp.requette(requette);
            TextAreaReponses.appendText("Requête envoyée : " + requette + "\n");

        } catch (Exception e) {
            TextAreaReponses.appendText("Erreur lors de l'envoi : " + e.getMessage() + "\n");
        }
    }



    private void deconnecter() throws InterruptedException {
        try {
            if (tcp != null && enRun) {
                tcp.deconnection();
                enRun = false;
                voyant.setFill(RED);
                TextAreaReponses.appendText("Déconnexion effectuée\n");
            }
        } catch (Exception e) {
            TextAreaReponses.appendText("Erreur lors de la déconnexion : " + e.getMessage() + "\n");
        }
    }

    private void connecter() {
        try {
            String interfaceChoisie = (String) choicebox.getValue();

            if (interfaceChoisie == null) {
                TextAreaReponses.appendText("Choisissez une interface réseau\n");
                return;
            }

            Multicast mc = new Multicast(interfaceChoisie);
            List<Connexion> serveurs = mc.discoverAll();

            if (serveurs.isEmpty()) {
                TextAreaReponses.appendText("Aucun serveur trouvé\n");
                return;
            }

            // On prend le premier serveur trouvé
            Connexion connexion = serveurs.get(0);

            TextAreaReponses.appendText("Serveur trouvé : "
                    + connexion.addressAsString() + ":" + connexion.portTCP() + "\n");

            tcp = new TCPBin(connexion.adresseServer(), connexion.portTCP(), this);
            tcp.connection();
            tcp.start();

            enRun = true;
            voyant.setFill(GREEN);

        } catch (Exception e) {
            TextAreaReponses.appendText("Erreur connexion : " + e.getMessage() + "\n");
        }
    }




}