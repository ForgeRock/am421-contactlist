package com.forgerock.edu.contactlist.dao;

import com.forgerock.edu.contactlist.entity.Contact;
import com.forgerock.edu.contactlist.entity.ContactGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactListDAO {

    // Initialization-on-demand holder idiom
    private static class LazyHolder {

        private static final ContactListDAO INSTANCE = new ContactListDAO();
    }

    public static ContactListDAO getInstance() {
        return LazyHolder.INSTANCE;
    }

    private Map<String,ContactGroup>contactGroupsByUID= new HashMap<>();
            
    private List<Contact> pbel = new ArrayList<>();

    private ContactListDAO() {
        
//        pbel.add(new Contact("jg", "James Gosling", "james@phoney.com", "+1 555 1234567", "management"));
//        pbel.add(new Contact("jd", "John Doe", "doe@phoney.com", "+1 555 7654321", "hr"));
//        pbel.add(new Contact("rs", "Roger Steels", "steels@phoney.com", "+1 555 7555321", "engineering"));
//        pbel.add(new Contact("jl", "Jane light", "light@phoney.com", "1 555 1112233", "management"));
//        pbel.add(new Contact("vrg", "Péter Varga", "vrg@vrg.hu", "+36 33 2509778", "training"));
//        pbel.add(new Contact("mf", "Ferenc Martin", "ferenc.martin@dpc.hu", "+36 44 2509779", "training"));
//        pbel.add(new Contact("hga", "Gábor Hollósi", "gabor.hollosi@dpc.hu", "+36 22 2509780", "training"));
    }

//    public Contact findByUid(String uid) {
//        for (Contact pbe : pbel) {
//            if (uid.equals(pbe.getUid())) {
//                return pbe;
//            }
//        }
//        // shall it be an exception??
//        return null;
//    }
//
//    public void add(Contact pbe) {
//        if (pbe != null) {
//            pbel.add(pbe);
//        }
//    }
//
//    public List<Contact> findAll() {
//        return pbel;
//    }
//
//    public boolean modify(Contact pbe) {
//        Contact found = findByUid(pbe.getUid());
//        if (found != null) {
//            pbel.remove(found);
//            pbel.add(pbe);
//            return true;
//        } else {
//            return false;
//        }
//    }
//    
//    public Contact removeByUid(String uid) {
//        Contact found = findByUid(uid);
//        if (found != null) {
//            pbel.remove(found);
//            return found;
//        }
//        return null;
//    }
//
//    public boolean modifyByUid(String uid, Contact pbe) {
//        Contact found = findByUid(uid);
//        if (found != null && pbe != null && uid.equals(pbe.getUid())) {
//            pbel.remove(found);
//            pbel.add(pbe);
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    public List<Contact> search(String name, String email, String phone) {
//        List<Contact> result = new ArrayList<>();
//        for (Contact pbe : pbel) {
//            // case insensitive substring match
//            // null will allways match
//            if ((name == null || pbe.getName().toLowerCase().contains(name.toLowerCase()))
//                    && (email == null || pbe.getEmail().toLowerCase().contains(email.toLowerCase()))
//                    && (phone == null || pbe.getPhoneNumber().toLowerCase().contains(phone.toLowerCase()))) {
//                result.add(pbe);
//            }
//        }
//        return result;
//    }
}
