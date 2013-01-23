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
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import client.gui.ContactListModel.Contact;
import client.logic.Connection;
import client.logic.events.LogicEvent;
import client.logic.events.LogicEventListener;
import client.logic.events.MessageEvent;
import client.logic.events.MessageEventListener;
import common.protocol.ComObject;
import common.protocol.Message;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {
//    protected static String contactsFile = "/home/bartek/git/komunikator2/contacts.xml";
//    protected static String contactsFile = "C:\\Users\\Bartek\\Documents\\git_repo\\komunikator2\\contacts.xml";
    protected static String contactsFile = "contacts.xml";
    protected static String myId = "Klient";
    public static final String APP_NAME = "Clinet";
    public static final int HEIGHT = 400;
    public static final int WIDTH = 250;
    private State state;
    Connection connection;
    private AuthDialog authDialog;
    private final TalkDialog talkDialog = new TalkDialog(this);
    private final ContactPopup contactPopup = new ContactPopup();
    private final ContactListModel contactsListModel = new ContactListModel(contactsFile);
    private final ListPopup listPopup = new ListPopup();
    private final JList contactsList;
    private final Map<String, WeakReference<TalkPanel>> talks = new HashMap<String, WeakReference<TalkPanel>>();
    private JMenuItem btnLogin;

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
        
        btnLogin = new JMenuItem("Logowanie");
        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                authDialog.setVisible(true);
            }
        });
        mnClinet.add(btnLogin);
        
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
        mntmZamknij.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                connection.disconnect();
                System.exit(0);
            }
        });
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
        try {
            connection = new Connection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.addLogicEventListener(new LogicEvents());
        connection.addMessageEventListener(new MessageEvents());
        authDialog = new AuthDialog(this, "Logowanie");
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
                    contactsListModel.saveToFileThread(f.getAbsolutePath());
                }
            } else {
                contactsListModel.saveToFileThread(f.getAbsolutePath());
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
            btnLogin.setEnabled(false);
            contactsList.setEnabled(true);
            state = State.AUTHORIZED;
            setTitle(APP_NAME + " - " + connection.getID());
            break;
        case NOT_AUTHORIZED:
            btnLogin.setEnabled(true);
            contactsList.setEnabled(false);
            state = State.NOT_AUTHORIZED;
            setTitle(APP_NAME);
            break;
        }
    }
    
    private void setCenter() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width - WIDTH)/2;
        int y = (screen.height - HEIGHT)/2;
        
        setBounds(x, y, WIDTH, HEIGHT);
    }

    private TalkPanel talk(Contact c) {
        WeakReference<TalkPanel> tpw = talks.get(c.getId());
        if(tpw == null) {
            tpw = new WeakReference<TalkPanel>(new TalkPanel(c, connection));
            talks.put(c.getId(), tpw);
        }
        TalkPanel p = tpw.get();
        talkDialog.addTalk(p);
        if(!talkDialog.isVisible()) talkDialog.setVisible(true);
        return p;
    }

    private class LogicEvents implements LogicEventListener {
        @Override
        public void onLogicEvent(LogicEvent e) {
            switch (e.type) {
                case AUTH_SUCCESS:
                    setGUIState(State.AUTHORIZED);
                    break;
                case AUTH_FAILURE:
                    setGUIState(State.NOT_AUTHORIZED);
                    break;
                case DISCONNECT:
                    setGUIState(State.NOT_AUTHORIZED);
                    break;
                case CONNECT:
                    setGUIState(State.AUTHORIZED);
                    break;
            }
        }
    }

    private class MessageEvents implements MessageEventListener {
        @Override
        public void onMessageIncomming(MessageEvent e) {
            String from = e.stream.from;
            ComObject obj = e.stream.obj;
            if(obj instanceof Message) {
                Message msg = (Message)obj;
                TalkPanel p = talk(new Contact(from, from));
                p.addMessage(msg.date, from, msg.body);
                talkDialog.notify(p);
            }
        }
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
            if(state == State.NOT_AUTHORIZED) return;
            if((e.getClickCount() == 2) && SwingUtilities.isLeftMouseButton(e)) {
                int i = contactsList.locationToIndex(e.getPoint());
                Contact c = (Contact)contactsListModel.getElementAt(i);
                talk(c);
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
            JMenuItem delete = new JMenuItem("Skasuj");
            add(delete);
            delete.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int choice = JOptionPane.showConfirmDialog(
                            MainWindow.this,
                            "Czy chcesz skasować kontakt?",
                            "Uwaga",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null
                    );
                    if(choice == JOptionPane.OK_OPTION) {
                        contactsListModel.remove(contactsList.getSelectedIndex());
                    }
                }
            });
            
            JMenuItem edit = new JMenuItem("Edytuj");
            add(edit);
            edit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    ContactPanel cpanel = new ContactPanel();
                    Contact c = (Contact) contactsListModel.getElementAt(contactsList.getSelectedIndex());
                    cpanel.setConId(c.getId());
                    cpanel.setConName(c.getName());
                    int choice = JOptionPane.showConfirmDialog(
                            MainWindow.this,
                            cpanel,
                            "Edycja",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE,
                            null
                    );
                    if(choice == JOptionPane.OK_OPTION) {
                        if(!cpanel.getConId().equals("") && !cpanel.getConName().equals("")) {
                            contactsListModel.edit(contactsList.getSelectedIndex(), new Contact(cpanel.getConName(), cpanel.getConId()));                            
                        }
                    }
                }
            });
        }
    }
    
    private class ListPopup extends JPopupMenu {
        public ListPopup() {
            JMenuItem newOne = new JMenuItem("Nowy kontakt");
            add(newOne);
            newOne.addActionListener(new ActionListener() {
                
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    ContactPanel panel = new ContactPanel();
                    int choice = JOptionPane.showConfirmDialog(
                            MainWindow.this,
                            panel,
                            "Nowy kontakt",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE,
                            null
                    );
                    if(choice == JOptionPane.OK_OPTION) {
                        Contact con = new Contact(panel.getConName(), panel.getConId());
                        contactsListModel.add(con);
                    }
                }
            });
        }
    }
}
