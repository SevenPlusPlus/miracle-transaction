package com.miracle.common.transaction.api;

public enum TccRole {
	  /**
     * Start tcc role enum.
     */
    STARTER(1, "发起者"),


    /**
     * Consumer tcc role enum.
     */
    CONSUMER(2, "消费者"),


    /**
     * Provider tcc role enum.
     */
    PROVIDER(3, "提供者"),


    /**
     * Local tcc role enum.
     */
    LOCAL(4,"本地调用");


    private int role;

    private String desc;

    TccRole(int role, String desc) {
        this.role = role;
        this.desc = desc;
    }


    /**
     * Acquire by code tcc action enum.
     *
     * @param code the code
     * @return the tcc action enum
     */
    public static TccRole valueOf(int role) {
    	switch(role)
    	{
    	case 1:
    		return STARTER;
    	case 2:
    		return CONSUMER;
    	case 3:
    		return PROVIDER;
    	default:
    		return LOCAL;
    	}
    }


    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
