package client.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.Xpp3Driver;

public class ContactList extends AbstractListModel {
    private List<ContactList.Contact> contacts = new ArrayList<ContactList.Contact>();    
    private XStream xstream = new XStream(new Xpp3Driver());
    
    public ContactList() {
        for(int i = 0; i < 10; i++) {
            contacts.add(new Contact("Użytkownik " + i, String.valueOf(i)));
        }
    }
    
    private class ConstactLoader implements Runnable {
        @Override
        public void run() {
            
        }
    }
    
    private class ContactSaver implements Runnable {
        @Override
        public void run() {
            
        }
    }
    
    private class Contact implements Serializable {
        private String name;
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

    @Override
    public Object getElementAt(int index) {
        return contacts.get(index);
    }

    @Override
    public int getSize() {
        return contacts.size();
    }
}
