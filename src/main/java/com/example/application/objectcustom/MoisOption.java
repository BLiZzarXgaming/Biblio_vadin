package com.example.application.objectcustom;

public class MoisOption {

    private String numero;
    private String nom;

    public MoisOption(String numero, String nom) {
        this.numero = numero;
        this.nom = nom;
    }

    public String getNumero() {
        return numero;
    }

    public String getNom() {
        return nom;
    }

    @Override
    public String toString() {
        return getNumero();
    }
}
