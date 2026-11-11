package lk.chapterhouse.service;

import java.util.List;

import lk.chapterhouse.model.ContactMessage;
import lk.chapterhouse.repository.ContactRepository;
import lk.chapterhouse.util.AppClock;
import lk.chapterhouse.util.Ids;

public class ContactService {
    private final ContactRepository contacts;

    public ContactService(ContactRepository contacts) {
        this.contacts = contacts;
    }

    public ContactMessage submit(String userId, String name, String email, String subject, String message) {
        String owner = userId == null ? "" : userId;
        ContactMessage contact = new ContactMessage(
                Ids.newId(), owner, name, email, subject, message, AppClock.now());
        return contacts.create(contact);
    }

    public List<ContactMessage> listAllForAdmin() {
        return contacts.findAll();
    }
}
