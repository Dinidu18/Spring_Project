package lk.chapterhouse.repository;

import java.util.List;
import java.util.Optional;

import lk.chapterhouse.model.ContactMessage;

public interface ContactRepository {
    List<ContactMessage> findAll();

    Optional<ContactMessage> findById(String id);

    ContactMessage create(ContactMessage message);
}
