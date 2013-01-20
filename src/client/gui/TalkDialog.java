package client.gui;

import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import java.awt.BorderLayout;

public class TalkDialog extends JFrame {
    private static final int WIDTH = 350;
    private static final int HEIGHT = 250;
    private JTabbedPane talkTabs = new JTabbedPane(JTabbedPane.BOTTOM);
    
    public TalkDialog(Frame parent) {
        setBounds(parent.getBounds());
        add(talkTabs, BorderLayout.CENTER);
        setSize(WIDTH, HEIGHT);
    }
    
    public void addTalk(TalkPanel p) {
        if(talkTabs.indexOfTabComponent(p) == -1) {
            talkTabs.add(p.getTalkWithName(), p); 
        }
        talkTabs.setSelectedComponent(p);
    }
}
