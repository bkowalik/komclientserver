package client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MainWindow extends JFrame {
    private State state;
    private AuthDialog authDialog = new AuthDialog(this);
    public static final String APP_NAME = "Clinet";
    public static final int HEIGHT = 400;
    public static final int WIDTH = 250;
    
    private enum State {
        AUTHORIZED, NOT_AUTHORIZED;
    }
    
    public MainWindow() {
        setTitle(APP_NAME);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JScrollPane scrollPane = new JScrollPane();
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        
        JList list = new JList();
        scrollPane.setViewportView(list);
        list.setModel(new ContactList());
        
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
        
        JSeparator separator = new JSeparator();
        mnClinet.add(separator);
        
        JMenuItem mntmZamknij = new JMenuItem("Zamknij");
        mnClinet.add(mntmZamknij);
        
        JMenu mnPomoc = new JMenu("Pomoc");
        menuBar.add(mnPomoc);
        
        JMenuItem mntmOProgramie = new JMenuItem("O programie");
        mnPomoc.add(mntmOProgramie);
        setCenter();
        setGUIState(State.NOT_AUTHORIZED);
    }
    
    private void setGUIState(State s) {
        switch(s) {
        case AUTHORIZED:
            state = State.AUTHORIZED;
            break;
        case NOT_AUTHORIZED:
            
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
}
