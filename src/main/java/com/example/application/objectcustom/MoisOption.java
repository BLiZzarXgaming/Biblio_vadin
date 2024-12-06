package com.example.application.objectcustom;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public static List<MoisOption> getListeMois(){
        List<MoisOption> listeDesMois = IntStream.rangeClosed(1, 12)
                .mapToObj(i -> new MoisOption(
                        String.format("%02d", i),
                        Month.of(i).getDisplayName(TextStyle.FULL, Locale.FRENCH)))
                .collect(Collectors.toList());

        return listeDesMois;
    }
}
