package com.example.dolarya.Firebase;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    private String id;
    private String email;
    private double cuentaPesos;
    private double cuentaDolares;
    private ArrayList<String> aliasPesos = new ArrayList<>();
    private ArrayList<String> aliasDolares = new ArrayList<>();

    public User(){
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getCuentaPesos() {
        return cuentaPesos;
    }

    public void setCuentaPesos(double cuentaPesos) {
        this.cuentaPesos = cuentaPesos;
    }

    public double getCuentaDolares() {
        return cuentaDolares;
    }

    public void setCuentaDolares(double cuentaDolares) {
        this.cuentaDolares = cuentaDolares;
    }

    public ArrayList<String> getAliasPesos() {
        return aliasPesos;
    }

    public void setAliasPesos(ArrayList<String> aliasPesos) {
        this.aliasPesos = aliasPesos;
    }

    public ArrayList<String> getAliasDolares() {
        return aliasDolares;
    }

    public void setAliasDolares(ArrayList<String> aliasDolares) {
        this.aliasDolares = aliasDolares;
    }

    public void acreditarPesos(Double importe){
        setCuentaPesos(cuentaPesos + importe);
    }

    public void debitarPesos(Double importe){
        setCuentaPesos(cuentaPesos - importe);
    }

    public void acreditarDolares(Double importe){
        setCuentaDolares(cuentaDolares + importe);
    }

    public void debitarDolares(Double importe){
        setCuentaDolares(cuentaDolares - importe);
    }

    public boolean hasPesosSuficientes(Double importe){
        if(cuentaPesos >= importe)
            return  true;
        else
            return false;
    }

    public boolean hasDolaresSuficientes(Double importe){
        if(cuentaDolares >= importe)
            return  true;
        else
            return false;
    }

    public boolean existeAlias(String nombreAlias){
        for(String aliasExistente : aliasPesos){
            if(aliasExistente.equals(nombreAlias))
                return true;
        }
        for(String aliasExistente : aliasDolares){
            if(aliasExistente.equals(nombreAlias))
                return true;
        }
        return false;
    }

    public void agregarAlias(String alias, String moneda){
        if(moneda.equals("pesos")){
            aliasPesos.add(alias);
        }
        else if (moneda.equals("dolares")){
            aliasDolares.add(alias);
        }
    }
}
