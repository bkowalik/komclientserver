package client.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class TalkDialog extends JFrame {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;
    private JTabbedPane talkTabs = new JTabbedPane(JTabbedPane.BOTTOM);
    
    public TalkDialog(Frame parent) {
        setBounds(parent.getBounds());
        add(talkTabs, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        talkTabs.addMouseListener(new TalkTabsEvents());
    }
    
    public void addTalk(TalkPanel p) {
        if(talkTabs.indexOfTabComponent(p) == -1) {
            talkTabs.add(p.getTalkWithName(), p); 
        }
        talkTabs.setSelectedComponent(p);
    }
    
    private class TalkTabsEvents extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if(e.getClickCount() == 2) {
                int index = talkTabs.indexAtLocation(e.getPoint().x, e.getPoint().y);
                if(index > -1) {
                    talkTabs.remove(index);
                }
            }
        }
    }
}
