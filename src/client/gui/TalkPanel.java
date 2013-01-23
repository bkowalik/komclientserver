package client.gui;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import client.gui.ContactListModel.Contact;
import client.logic.Connection;
import com.sun.jmx.snmp.SnmpStringFixed;
import common.exceptions.UnauthorizedException;
import common.protocol.ComStream;
import common.protocol.Message;

@SuppressWarnings("serial")
public class TalkPanel extends JPanel {
    private JTextArea chatArea;
    private final JTextArea msgArea;
    private final Contact talkWith;
    private final Connection con;
    private boolean notified;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-mm-yy hh:mm:ss");

    public TalkPanel(Contact talkWith, Connection con) {
        super(new BorderLayout());
        this.talkWith = talkWith;
        this.con = con;
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
    }

    public String getTalkWithName() {
        return talkWith.getName();
    }
    
    private void performSending() {
        String msg = msgArea.getText();
        if(msg.equals("")) return;
        msgArea.setText("");
        chatArea.append(sdf.format(new Date()) + " " + con.getID() + ": " + msg + '\n');
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
        try {
            con.sendStream(new ComStream(con.getID(), talkWith.getId(), new Message(msg)));
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        }
    }

    public void addMessage(Date date, String from, String msg) {
        chatArea.append(sdf.format(date) + " " + from + ": " + msg + '\n');
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    public String getHistory() {
        return chatArea.getText();
    }

    public void setNotified() {
        notified = true;
    }

    public boolean isNotified() {
        return notified;
    }
}
