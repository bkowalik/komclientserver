package client.gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
        talkTabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if(talkTabs.getTabCount() == 0) TalkDialog.this.setVisible(false);
                else {
                    notification(Notifications.UNNOTIFY, talkTabs.getSelectedIndex());
                }
            }
        });
    }
    
    public void addTalk(TalkPanel p) {
        if(talkTabs.indexOfTabComponent(p) == -1) {
            talkTabs.add(p.getTalkWithName(), p);
        }
    }

    public void notify(TalkPanel p) {
        int i = talkTabs.indexOfComponent(p);
        notification(Notifications.NOTIFY, i);
    }

    private void notification(Notifications n, int i) {
        switch (n) {
            case NOTIFY:
                if(i != talkTabs.getSelectedIndex()) {
                    talkTabs.setBackgroundAt(i, Color.BLACK);
                    talkTabs.setForegroundAt(i, Color.WHITE);
                }
                break;
            case UNNOTIFY:
                talkTabs.setBackgroundAt(i, new Color(238, 238, 238));
                talkTabs.setForegroundAt(i, Color.BLACK);
                break;
        }
    }

    private enum Notifications {
        NOTIFY, UNNOTIFY;
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
