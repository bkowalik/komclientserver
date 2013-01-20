package client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import client.gui.ContactListModel.Contact;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {
    protected static String contactsFile = "contacts.xml";
    protected static String myId = "Klient";
    public static final String APP_NAME = "Bartek";
    public static final int HEIGHT = 400;
    public static final int WIDTH = 250;
    private State state;
    private AuthDialog authDialog = new AuthDialog(this);
    private final TalkDialog talkDialog = new TalkDialog(this);
    private final ContactPopup contactPopup = new ContactPopup();
    private final ContactListModel contactsListModel = new ContactListModel(contactsFile);
    private final ListPopup listPopup = new ListPopup();
    private final JList contactsList;
    private final Map<String, WeakReference<TalkPanel>> talks = new HashMap<String, WeakReference<TalkPanel>>();
    
    private enum State {
        AUTHORIZED, NOT_AUTHORIZED;
    }
    
    public MainWindow() {
        setTitle(APP_NAME + " - " + myId);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JScrollPane scrollPane = new JScrollPane();
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        
        contactsList = new JList();
        scrollPane.setViewportView(contactsList);
        contactsList.setModel(contactsListModel);
        contactsList.addMouseListener(new ListMouseEvents());
        
        JMenuBar menuBar = new JMenuBar();
        getContentPane().add(menuBar, BorderLayout.NORTH);
        
        JMenu mnClinet = new JMenu("Clinet");
        menuBar.add(mnClinet);
        
        JMenuItem mntmZaloguj = new JMenuItem("Logowanie");
        mntmZaloguj.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                authDialog.setVisible(true);
            }
        });
        mnClinet.add(mntmZaloguj);
        
        JMenuItem mntmWyloguj = new JMenuItem("Wyloguj");
        mntmWyloguj.setEnabled(false);
        mnClinet.add(mntmWyloguj);
        
        JMenu mnKontakty = new JMenu("Kontakty");
        mnClinet.add(mnKontakty);
        
        JMenuItem mntmEksportDoPliku = new JMenuItem("Eksport do pliku");
        mntmEksportDoPliku.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                MainWindow.this.exportContacts();
            }
        });
        mnKontakty.add(mntmEksportDoPliku);
        
        JMenuItem mntmImportZPliku = new JMenuItem("Import z pliku");
        mntmImportZPliku.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                MainWindow.this.importContacts();
            }
        });
        mnKontakty.add(mntmImportZPliku);
        
        JSeparator separator = new JSeparator();
        mnClinet.add(separator);
        
        JMenuItem mntmZamknij = new JMenuItem("Zamknij");
        mnClinet.add(mntmZamknij);
        
        JMenu mnPomoc = new JMenu("Pomoc");
        menuBar.add(mnPomoc);
        
        JMenuItem mntmOProgramie = new JMenuItem("O programie");
        mnPomoc.add(mntmOProgramie);
        
        addWindowListener(new WindowEvents());
        setCenter();
        setGUIState(State.NOT_AUTHORIZED);
        if(new File(contactsFile).exists())
            contactsListModel.loadFromFile(contactsFile);
    }
    
    private void exportContacts() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setApproveButtonText("Export");
        int returnVal = fileChooser.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            if(f.exists()) {
                int val = JOptionPane.showConfirmDialog(
                        fileChooser,
                        "Czy chcesz nadpisać plik?",
                        "Ostrzeżenie",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null
                );
                if(val == JOptionPane.YES_OPTION) {
                    contactsListModel.saveToFile(f.getAbsolutePath());
                }
            }
        }
    }
    
    private void importContacts() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setApproveButtonText("Import");
        int returnVal = fc.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if(!f.exists() || !f.getName().toLowerCase().endsWith(".xml")) {
                JOptionPane.showMessageDialog(
                        fc,
                        "Niepoprawny plik",
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE,
                        null
                );
                return;
            }
            contactsListModel.loadFromFile(f.getAbsolutePath());
        }
    }
    
    private void setGUIState(State s) {
        switch(s) {
        case AUTHORIZED:
            contactsList.setEnabled(true);
            state = State.AUTHORIZED;
            break;
        case NOT_AUTHORIZED:
//            contactsList.setEnabled(false);
            state = State.NOT_AUTHORIZED;
            break;
        }
    }
    
    private void setCenter() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width - WIDTH)/2;
        int y = (screen.height - HEIGHT)/2;
        
        setBounds(x, y, WIDTH, HEIGHT);
    }
     
    private class WindowEvents extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            contactsListModel.saveToFile(contactsFile);
        }
    }
    
    private class ListMouseEvents extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if((e.getClickCount() == 2) && SwingUtilities.isLeftMouseButton(e)) {
                int i = contactsList.locationToIndex(e.getPoint());
                Contact c = (Contact)contactsListModel.getElementAt(i);
                WeakReference<TalkPanel> tp = talks.get(c.getId());
                if(tp == null) { 
                    tp = new WeakReference<TalkPanel>(new TalkPanel(c));
                    talks.put(c.getId(), tp);
                }
                talkDialog.addTalk(tp.get());
                if(!talkDialog.isVisible()) talkDialog.setVisible(true);
            } else if((e.getClickCount() == 1) && SwingUtilities.isRightMouseButton(e)) {
                int index = contactsList.locationToIndex(e.getPoint());
                if(contactsList.getCellBounds(index, index).contains(e.getPoint())) {
                    contactsList.setSelectedIndex(index);
                    contactPopup.show(MainWindow.this.contactsList, e.getX(), e.getY());
                } else {
                    listPopup.show(MainWindow.this.contactsList, e.getX(), e.getY());
                }
            }
        } 
    }
    
    private class ContactPopup extends JPopupMenu {
        public ContactPopup() {
            add(new JMenuItem("Istniejący kontakt"));
        }
    }
    
    private class ListPopup extends JPopupMenu {
        public ListPopup() {
            add(new JMenuItem("Nowy kontakt"));
        }
    }
}
