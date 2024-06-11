import javax.swing.*;
//vključuje razrede JFrame,JButton in JLabel
import java.awt.*;
//vključuje razrede Color, Font, Graphics, in Dimension
import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;

import java.util.Random;


public class Igra extends JFrame {

    private static final int DOLŽINA_KODE = 4; //Določa dolžino rešitve, ki jo mora igralec uganiti
    private static final int MAKS_POSKUSOV = 10;
    private static final Color[] BARVE = {Color.PINK, Color.GREEN, Color.CYAN, Color.YELLOW, Color.ORANGE, Color.RED}; //Definira polje barv, ki se lahko uporabijo v igri
    private static final String[] IMENA_BARV = {"P", "G", "B", "Y", "O", "R"};

    private char[] skrivnaKoda;
    private int stevec;
    //Števec, ki sledi številu poskusov, ki jih je igralec že porabil
    private JPanel vnosniPanel;
    private JPanel ugibniPanel;
    private JPanel feedbackPanel;
    private JComboBox<MožnostiBarve>[] poljaUgiba;
    //Barve na vrhu panela
    //Polje kombiniranih barv, kjer bo igralec izbral svoje ugibanje.
    private JButton gumbSubmit;
    private JButton[] barvniGumbi;
    private Color[] ugibneBarve;
    //Barve na dnu panela
    //Objekt tipa Color, ki shranjuje trenutno izbrane barve

    public Igra() {
        setTitle("Igra Mastermind");
        setSize(500, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        //metoda za postavitev komponent v cone (sever,jug,...)

        skrivnaKoda = generirajSkrivnoKodo();
        stevec = 0;

        //Vnosni panel za izbiranje barv
        vnosniPanel = new JPanel();
        vnosniPanel.setLayout(new GridLayout(1, DOLŽINA_KODE + 1));
        //vnosna vrstica s petimi stolpci(barve + submit)
        //barve na izbiro
        
        poljaUgiba = new JComboBox[DOLŽINA_KODE];
        //JComboBox omogoča uporabnikom izbiranje ene možnosti iz seznama
        for (int i = 0; i < DOLŽINA_KODE; i++) {
            poljaUgiba[i] = new JComboBox<>(MožnostiBarve.values());
          //JComboBox bo vseboval vse barvne možnosti, ki so določene v ColorOption
            poljaUgiba[i].setRenderer(new BarvniRender()); 
          //Renderer določa, kako bodo elementi znotraj JComboBox-a vizualno predstavljeni
            vnosniPanel.add(poljaUgiba[i]);
        }

        // Gumb za oddajo ugiba
        gumbSubmit = new JButton("Submit");
        gumbSubmit.addActionListener(new SubmitButtonListener());
        vnosniPanel.add(gumbSubmit);

        // Panel za prikaz ugibov
        ugibniPanel = new JPanel();
        ugibniPanel.setLayout(new GridLayout(MAKS_POSKUSOV, DOLŽINA_KODE));

        // Panel za prikaz povratnih informacij
        feedbackPanel = new JPanel();
        feedbackPanel.setLayout(new GridLayout(MAKS_POSKUSOV, DOLŽINA_KODE));

        JPanel glavniPanel = new JPanel(new BorderLayout());
        glavniPanel.add(ugibniPanel, BorderLayout.CENTER);
        glavniPanel.add(feedbackPanel, BorderLayout.EAST);

        // Spodnji panel z barvnimi gumbi
        JPanel spodnjiPanel = new JPanel();
        spodnjiPanel.setLayout(new GridLayout(1, BARVE.length));

        barvniGumbi = new JButton[BARVE.length];
        ugibneBarve = new Color[DOLŽINA_KODE];
        for (int i = 0; i < BARVE.length; i++) {
        	barvniGumbi[i] = new JButton();
        	barvniGumbi[i].setBackground(BARVE[i]);
        	barvniGumbi[i].setOpaque(true);
        	barvniGumbi[i].setBorderPainted(false);
            int končniI = i;
            barvniGumbi[i].addActionListener(e -> {
                for (int j = 0; j < DOLŽINA_KODE; j++) {
                    if (ugibneBarve[j] == null) {
                        ugibneBarve[j] = BARVE[končniI];
                        poljaUgiba[j].setSelectedItem(MožnostiBarve.izBarve(BARVE[končniI]));
                        break;
                    }
                }
            });
            spodnjiPanel.add(barvniGumbi[i]);
        }
      //Panel barv na spodnjem delu okna (barve samo na klik)

        add(vnosniPanel, BorderLayout.NORTH);
        add(glavniPanel, BorderLayout.CENTER);
        add(spodnjiPanel, BorderLayout.SOUTH);
    }

    private char[] generirajSkrivnoKodo() {
        char[] koda = new char[DOLŽINA_KODE];
        Random random = new Random();
        for (int i = 0; i < DOLŽINA_KODE; i++) {
            koda[i] = IMENA_BARV[random.nextInt(IMENA_BARV.length)].charAt(0);
          //generira naključne znake (zaporedje barv)
        }
        return koda;
    }

    private int[] getFeedback(char[] ugib) {
        int[] feedback = new int[2]; // feedback[0] so črne pike, feedback[1] so bele pike
        boolean[] kodaUporabljena = new boolean[DOLŽINA_KODE];
        boolean[] ugibUporabljen = new boolean[DOLŽINA_KODE];
      //primerja uporabnikov ugib s skrivno rešitvijo in vrača povratne informacije
        //črni krogi=pravilna barva na pravilnem mestu, beli krogi=pravilna barva na napačnem mestu

        // stetje crnih krogov (koliko barv je na pravih mestih)
        for (int i = 0; i < DOLŽINA_KODE; i++) {
            if (ugib[i] == skrivnaKoda[i]) {
                feedback[0]++;
                kodaUporabljena[i] = true;
                ugibUporabljen[i] = true;
            }
        }

     // stetje belih krogov (koliko barv je pravilnih)
        for (int i = 0; i < DOLŽINA_KODE; i++) {
            if (ugibUporabljen[i]) continue;
            for (int j = 0; j < DOLŽINA_KODE; j++) {
                if (kodaUporabljena[j]) continue;
                if (ugib[i] == skrivnaKoda[j]) {
                    feedback[1]++;
                    kodaUporabljena[j] = true;
                    break;
                }
            }
          //Če element ugiba ni bil uporabljen za črne kroge, preveri, ali se ujema z elementi v rešitvi, ki niso na pravilnih mestih.
        }

        return feedback;
    }

    private class SubmitButtonListener implements ActionListener { 
    	// preverja barve, ki jih je uporabnik izbral ter prikaže povratne informacije glede ujemanja z resitvo
        @Override
        public void actionPerformed(ActionEvent e) {
            char[] tabelaUgiba = new char[DOLŽINA_KODE]; // hrani barvne znake trenutnega ugiba
            for (int i = 0; i < DOLŽINA_KODE; i++) {
                MožnostiBarve izbranaBarva = (MožnostiBarve) poljaUgiba[i].getSelectedItem(); 
                if (izbranaBarva != null) { 
                    tabelaUgiba[i] = izbranaBarva.vrniBarvniZnak();
                } else { 	//primer ko uporabnik ne izbere barve
                    JOptionPane.showMessageDialog(null, "Please, choose color for every box!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            int[] feedback = getFeedback(tabelaUgiba); 
            //metoda pridobi povratne informacije o ugibanju
            // vrne tabelo povratnih info, ki opisuje pravilno mesto in barvo
            stevec++;

            JPanel enojniUgibniPanel = new JPanel(); // prikaz trenutnega panela
            enojniUgibniPanel.setLayout(new GridLayout(1, DOLŽINA_KODE));
            for (int i = 0; i < DOLŽINA_KODE; i++) {
                MožnostiBarve izbranaBarva = (MožnostiBarve) poljaUgiba[i].getSelectedItem();
                enojniUgibniPanel.add(new KrogPanel(izbranaBarva.vrniBarvo()));
            } // prikaže ustrezne barvne kroge, ki ustrezajo izbranim barvam

            JPanel enojniPovratniPanel = new JPanel(); //panel za feedback
            enojniPovratniPanel.setLayout(new GridLayout(2, 2));
            for (int i = 0; i < feedback[0]; i++) { // dodaja črne kroge (pravilna barva in mesto)
                enojniPovratniPanel.add(new KrogPanel(Color.BLACK));
            }
            for (int i = 0; i < feedback[1]; i++) { // dodaja bele kroge (pravilne barve napačna mesta)
                enojniPovratniPanel.add(new KrogPanel(Color.WHITE));
            }

            ugibniPanel.add(enojniUgibniPanel); //prikaze trenutni ugib
            feedbackPanel.add(enojniPovratniPanel); // prikaze feedback
            //posodabljanje, da lajko prikažemo nove ugibe in feedback
            ugibniPanel.revalidate();
            feedbackPanel.revalidate();
            

            if (feedback[0] == DOLŽINA_KODE) { //če je ugib pravilen, se prikaze zmagovalno sporočilo
                JOptionPane.showMessageDialog(null, "Congratulations! You've guessed the secret code.");
                for (JComboBox<MožnostiBarve> poljeUgiba : poljaUgiba) { // polja in gum se onemogočita
                    poljeUgiba.setEnabled(false);
                }
                gumbSubmit.setEnabled(false);
            } else if (stevec >= MAKS_POSKUSOV) { //če porabimo vse poskuse, se prikaže sporočilo za konec igre, ter rešitev
                JOptionPane.showMessageDialog(null, "You've run out of attempts! The secret code was: " + String.valueOf(skrivnaKoda));
                for (JComboBox<MožnostiBarve> poljeUgiba : poljaUgiba) {
                    poljeUgiba.setEnabled(false);
                }
                gumbSubmit.setEnabled(false);
            }

            // Popravljanje vnosnoh polj za naslednji poskus
            ugibneBarve = new Color[DOLŽINA_KODE];
            for (JComboBox<MožnostiBarve> poljeUgiba : poljaUgiba) {
                poljeUgiba.setSelectedItem(null);
            }
        }
    }

    private enum MožnostiBarve { //bolje berljiva, manj napak
    	//definira barve in njihove pripadajoče znake
        ROZA(Color.PINK, 'P'),
        ZELENA(Color.GREEN, 'G'),
        MODRA(Color.CYAN, 'B'),
        RUMENA(Color.YELLOW, 'Y'),
        ORANŽNA(Color.ORANGE, 'O'),
        RDEČA(Color.RED, 'R');

        private final Color barva;
        private final char barvniZnak;

        MožnostiBarve(Color barva, char barvniZnak) {
            this.barva = barva;
            this.barvniZnak = barvniZnak;
        }

        public Color vrniBarvo() {
            return barva; 
        }

        public char vrniBarvniZnak() {
            return barvniZnak;
        }

        @Override
        public String toString() { //preoblikuje konstanto v niz, ki prestavlja njen znak
            return String.valueOf(barvniZnak);
        }

        public static MožnostiBarve izBarve(Color barva) { // staticna metoda, ki poišče konstanto eunma na podlagi podane barve
            for (MožnostiBarve možnost : values()) {
                if (možnost.vrniBarvo().equals(barva)) {
                    return možnost;
                }
            } //vrne konstanto, ki ustreza barvi, ali null, če taksne barve ni med konstantami
            return null;
        }
    }

    private static class BarvniRender extends JPanel implements ListCellRenderer<MožnostiBarve> { // ListCellRenderer<MožnostiBarve> vmesnik
    	//uporabljamo v primerih, ko želimo prilagoditi prikaz elementov v JList, ki vsebuje elemente iz MožnostiBarve
    	
        private final JLabel label;

        public BarvniRender() {
            setLayout(new BorderLayout());
            label = new JLabel(); //prikazal bo barvo in ga dodal na JPanel
            label.setOpaque(true);
            add(label, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends MožnostiBarve> list, MožnostiBarve vrednost, int index, boolean jeIzbran, boolean imaFokus) {
        	// metoda se kliče ko se vrednost v JList spremeni
        	// JList omogoča ustvarjanje seznamov elementov
            if (vrednost != null) {
                label.setBackground(vrednost.vrniBarvo());
                label.setText(vrednost.toString());
            }
            return this;
        }
    }

    private static class KrogPanel extends JPanel { //KrogPanel prikaže krog določene barve
        private final Color barva;

        public KrogPanel(Color barva) { // prejme barvo, ki jo želimo uporabiti za krog, in nastavi željeno velikost območja
            this.barva = barva;
            setPreferredSize(new Dimension(30, 30));
        }

        @Override
        protected void paintComponent(Graphics g) { //uporablja za risanje kroglice znotraj Panela
            super.paintComponent(g); // pravilno risanje osnovnih komponent (ozadje, robovi)
            g.setColor(barva);
            g.fillOval(5, 5, getWidth() - 10, getHeight() - 10);
        }
    }

    public static void main(String[] args) { //predstavlja začetno točko programa
        SwingUtilities.invokeLater(() -> { 	 // zagotovi dosledno in pravilno obnašanje aplikacije
            Igra igra = new Igra(); 
            igra.setVisible(true);
        });
    }
}
