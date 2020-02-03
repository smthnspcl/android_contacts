package io.eberlein.contacts.objects;

import io.realm.RealmObject;

// https://github.com/realm/realm-java/issues/761
// v5, 6 & 7 don't support inheritance
// error: Valid model classes must either extend RealmObject or implement RealmModel.

public class DBObject /* extends RealmObject */ {}
