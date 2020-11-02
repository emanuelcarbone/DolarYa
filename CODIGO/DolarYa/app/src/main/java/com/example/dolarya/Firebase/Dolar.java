package com.example.dolarya.Firebase;

import java.io.Serializable;

public class Dolar implements Serializable {
    private double precio;

    public Dolar(){
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }
}
