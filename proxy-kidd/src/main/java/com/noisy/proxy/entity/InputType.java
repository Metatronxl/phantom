package com.noisy.proxy.entity;

/**
 * Created by kevin on 8/22/16.
 */
public enum InputType {
    IP_LIST("ip_list"),
    IP_SEGMENTS("ip_segments");

    InputType(String type){
        this.type = type;
    }

    private String type;

    public String getType() {
        return type;
    }

    public boolean isThisType(String type){
        return this.type.equals(type);
    }
}
