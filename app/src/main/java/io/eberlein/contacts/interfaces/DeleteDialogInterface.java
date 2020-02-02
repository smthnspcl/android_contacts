package io.eberlein.contacts.interfaces;

public interface DeleteDialogInterface<T> {
    void delete(T object);
    String getName();
}
