package client.gui;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import client.gui.ContactListModel.Contact;

public class TalkPanel extends JPanel {
    private final Contact talkWith;
    
    public TalkPanel(Contact talkWith) {
        this.talkWith = talkWith;
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        add(mainSplit);
        mainSplit.setDividerLocation(0.5);
        
        /*
         * Górna część okna rozmowy
         */
        JScrollPane scroll = new JScrollPane();
        JTextArea chatArea = new JTextArea();
        scroll.add(chatArea);
        mainSplit.add(scroll, 0);
        
        /*
         * Dolna część okna rozmowy
         */
        JScrollPane bottomScroll = new JScrollPane();
        JTextArea msgArea = new JTextArea();
        bottomScroll.add(msgArea);
        mainSplit.add(bottomScroll, 1);
    }

    public String getTalkWithName() {
        return talkWith.getName();
    }
}
