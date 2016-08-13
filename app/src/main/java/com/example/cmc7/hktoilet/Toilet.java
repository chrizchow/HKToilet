package com.example.cmc7.hktoilet;

/**
 * Created by cmc7 on 2/27/2016.
 */

//public class Toilet implements Comparable<Toilet> {
public class Toilet {
    public String name;
    public String address;
    public String distance;

    public Toilet(){

    }

    public Toilet(String name, String address, String distance){
        this.name = name;
        this.address = address;
        this.distance = distance;
    }

    /*
    @Override
    public int compareTo(Toilet t){
        int compare = (int)(distance - t.distance);
        return compare;
    }
    */

}
