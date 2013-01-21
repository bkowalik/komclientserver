package client.gui;

import client.logic.Connection;
import client.logic.events.LogicEvent;
import client.logic.events.LogicEventListener;
import common.protocol.ComObject;
import common.protocol.request.Login;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.io.IOException;
import java.net.InetSocketAddress;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class AuthDialog extends JDialog {
    private static final int WIDTH = 240;
    private static final int HEIGHT = 180;
    private MainWindow mainWindow;
    private JTextField username;
    private JTextField password;

    public AuthDialog(MainWindow parent) {
        super(parent, true);
        mainWindow = parent;
        setAlwaysOnTop(true);
        setTitle("Logowanie");
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        
        JPanel panel = new JPanel();
        getContentPane().add(panel, BorderLayout.CENTER);
        
        JLabel lblLogin = new JLabel("Login:");
        lblLogin.setFont(new Font("Tahoma", Font.BOLD, 14));
        
        JLabel lblHaso = new JLabel("Hasło:");
        lblHaso.setFont(new Font("Tahoma", Font.BOLD, 14));
        
        username = new JTextField();
        username.setColumns(10);
        
        password = new JPasswordField();
        password.setColumns(10);
        
        JButton btnZaloguj = new JButton("Zaloguj");
        btnZaloguj.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                authenticate();
            }
        });
        
        JButton btnAnuluj = new JButton("Anuluj");
        GroupLayout gl_panel = new GroupLayout(panel);
        gl_panel.setHorizontalGroup(
                gl_panel.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_panel.createSequentialGroup()
                                .addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
                                        .addGroup(gl_panel.createSequentialGroup()
                                                .addGap(20)
                                                .addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
                                                        .addComponent(lblHaso)
                                                        .addComponent(lblLogin))
                                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                                .addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
                                                        .addComponent(username, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(password, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(gl_panel.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(btnZaloguj)
                                                .addGap(18)
                                                .addComponent(btnAnuluj)))
                                .addContainerGap(40, Short.MAX_VALUE))
        );
        gl_panel.setVerticalGroup(
                gl_panel.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_panel.createSequentialGroup()
                                .addGap(33)
                                .addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(lblLogin)
                                        .addComponent(username, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(lblHaso)
                                        .addComponent(password, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(18)
                                .addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(btnZaloguj)
                                        .addComponent(btnAnuluj))
                                .addContainerGap(16, Short.MAX_VALUE))
        );
        panel.setLayout(gl_panel);
        setSize(WIDTH, HEIGHT);
        mainWindow.connection.addLogicEventListener(new LogicEvents());
    }
    
    private void authenticate() {
        try {
            if(!mainWindow.connection.isConnected()) {
                mainWindow.connection.connect(new InetSocketAddress("localhost", 44321), 0);
            }
            Login log = new Login(username.getText(), password.getText());
            ComObject obj = mainWindow.connection.authenticate(log);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class LogicEvents implements LogicEventListener {
        @Override
        public void onLogicEvent(LogicEvent e) {
            switch (e.type) {
                case AUTH_SUCCESS:
                    JOptionPane.showMessageDialog(
                            AuthDialog.this,
                            "Zalogowano",
                            "Logowanie",
                            JOptionPane.INFORMATION_MESSAGE,
                            null
                    );
                    setVisible(false);
                    break;
                case AUTH_FAILURE:
                    JOptionPane.showMessageDialog(
                            AuthDialog.this,
                            "Błąd autoryzacji",
                            "Błąd",
                            JOptionPane.ERROR_MESSAGE,
                            null
                    );
                    break;
            }
        }
    }
}
