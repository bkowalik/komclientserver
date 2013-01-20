package client.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

public class AuthDialog extends JDialog {
    private static final int WIDTH = 240;
    private static final int HEIGHT = 180;
    
    private JTextField username;
    private JTextField password;

    public AuthDialog(Frame parent) {
        super(parent, true);
        setAlwaysOnTop(true);
        setTitle("Logowanie");
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        JPanel panel = new JPanel();
        getContentPane().add(panel, BorderLayout.CENTER);
        
        JLabel lblLogin = new JLabel("Login:");
        lblLogin.setFont(new Font("Tahoma", Font.BOLD, 14));
        
        JLabel lblHaso = new JLabel("Hasło:");
        lblHaso.setFont(new Font("Tahoma", Font.BOLD, 14));
        
        username = new JTextField();
        username.setColumns(10);
        
        password = new JTextField();
        password.setColumns(10);
        
        JButton btnZaloguj = new JButton("Zaloguj");
        
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
    }
    
    public String getUsername() {
        return username.getText();
    }
    
    public String getPassword() {
        return password.getText();
    }
}
