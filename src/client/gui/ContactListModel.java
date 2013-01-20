package client.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractListModel;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.io.xml.Xpp3Driver;

public class ContactListModel extends AbstractListModel {
    private static final Logger logger = Logger.getLogger(ContactListModel.class.getName());
    private ArrayList<ContactListModel.Contact> contactsList = new ArrayList<ContactListModel.Contact>(); 
    private XStream xstream = new XStream(new Xpp3Driver());
    private String contactFile;
    
    public ContactListModel(String contactsFile) {
        this.contactFile = contactsFile;
        configureXStream();
    }
    
    private void configureXStream() {
        xstream.alias("contact", ContactListModel.Contact.class);
    }
    
    @Override
    public Object getElementAt(int index) {
        return contactsList.get(index);
    }

    @Override
    public int getSize() {
        return contactsList.size();
    }
    
    public void saveToFile(String file) {
        new Thread(new ContactSaver(file)).start();
    }
    
    public void loadFromFile(String file) {
        new Thread(new ConstactLoader(file)).start();
    }
    
    private class ConstactLoader implements Runnable {
        private String file;
        
        public ConstactLoader(String file) {
            this.file = file;
        }
        
        @Override
        public void run() {
            synchronized (contactsList) {
                ArrayList<Contact> con = null;
                
                try {
                    FileReader fr = new FileReader(file);
                    BufferedReader reader = new BufferedReader(fr);
                    con = (ArrayList<ContactListModel.Contact>)xstream.fromXML(reader);
                    reader.close();
                    fr.close();
                } catch(IOException e) {
                    logger.log(Level.SEVERE, "Failure", e);
                }
                
                contactsList = con;
            }
        }
    }

    private class ContactSaver implements Runnable {
        private String file;
        
        public ContactSaver(String file) {
            this.file = file;
        }
        
        @Override
        public void run() {
            synchronized (contactsList) {
                try {
                    FileWriter fw = new FileWriter(file);
                    BufferedWriter writer = new BufferedWriter(fw);
                    xstream.toXML(contactsList, writer);
                    writer.close();
                    fw.close();
                } catch(IOException e) {
                    logger.log(Level.SEVERE, "Failure", e);
                }
            }
        }
    }
    
    public static class Contact {
        private String name;
        @XStreamAsAttribute
        private String id;
        
        public Contact(String name, String id) {
            this.name = name;
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
}
