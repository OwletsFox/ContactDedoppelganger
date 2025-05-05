package com.example.contactdedoppelganger;

import com.example.contactdedoppelganger.data.dto.ContactParcelable;
import java.util.List;

interface IContactService {
    /** Возвращает список всех контактов */
    List<ContactParcelable> getAllContacts();
}
