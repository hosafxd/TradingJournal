package com.tradingjournal;

import java.io.Serializable;

public class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String id;
    
   public Account(String name) {
    this.name = name;
    // Create a simpler, more predictable ID
    this.id = name.toLowerCase().replaceAll("[^a-z0-9]", "_");
}
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getId() {
        return id;
    }
    
    @Override
    public String toString() {
        return name;
    }
}