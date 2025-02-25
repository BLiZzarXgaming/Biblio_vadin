package com.example.application.form;

public interface DocumentForm<T extends Object> {
    void clear();

    T getDocumentData();

    void setDocumentData(T data);

    boolean isValid();

    void setReadOnly(boolean readOnly);
}
