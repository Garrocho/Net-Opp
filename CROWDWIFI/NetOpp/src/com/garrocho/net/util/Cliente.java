package com.garrocho.net.util;

import java.io.Serializable;
import java.util.Comparator;

public class Cliente implements Serializable, Comparator<Cliente>, Comparable<Cliente>{
	private static final long serialVersionUID = 8156011550117514471L;
	public String name;
	public int bateria;

	public Cliente(String n, int a){
		name = n;
		bateria = a;
	}

    @Override
    public int compareTo(Cliente cli){
        if (this.bateria < cli.bateria)
            return 1;
        else if (this.bateria == cli.bateria)
            return 0;
        else 
            return -1;
    }

	public int compare(Cliente d, Cliente d1){
		return d.bateria - d1.bateria;
	}
}
