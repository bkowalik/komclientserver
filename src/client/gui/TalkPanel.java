package client.gui;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import client.gui.ContactListModel.Contact;

@SuppressWarnings("serial")
public class TalkPanel extends JPanel {
    private JTextArea chatArea;
    private final JTextArea msgArea;
    private final Contact talkWith;
    
    public TalkPanel(Contact talkWith) {
        super(new BorderLayout());
        this.talkWith = talkWith;
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        add(mainSplit, BorderLayout.CENTER);
        mainSplit.setResizeWeight(0.7);
        mainSplit.setEnabled(false);

        /*
         * Górna część okna rozmowy
         */
        JScrollPane scroll = new JScrollPane();
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        scroll.setViewportView(chatArea);
        mainSplit.setLeftComponent(scroll);
        
        /*
         * Dolna część okna rozmowy
         */
        JScrollPane bottomScroll = new JScrollPane();
        msgArea = new JTextArea();
        msgArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    performSending();
                }
            }
        });
        bottomScroll.setViewportView(msgArea);
        mainSplit.setRightComponent(bottomScroll);
        
        /*
         * DEMO
         */
        chatArea.append("Czat leci...\n");
        msgArea.append("Tu wpisz treść");
    }

    public String getTalkWithName() {
        return talkWith.getName();
    }
    
    private void performSending() {
        String msg = msgArea.getText();
        if(msg.equals("")) return;
        msgArea.setText("");
        chatArea.append(MainWindow.myId + ": " + msg + '\n');
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
        //TODO: wysłanie wiadomości
    }
    
    public String getHistory() {
        return chatArea.getText();
    }
}
