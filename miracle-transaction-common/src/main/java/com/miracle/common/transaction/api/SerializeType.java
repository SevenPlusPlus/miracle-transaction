package com.miracle.common.transaction.api;


public enum SerializeType {

    /**
     * Jdk serialize protocol enum.
     */
    JDK("jdk"),

    /**
     * Fastjson serialize protocol enum
     */
    FASTJSON("fastjson"),

    /**
     * Hessian serialize protocol enum.
     */
    HESSIAN("hessian"),

    /**
     * Protostuff serialize protocol enum.
     */
    PROTOSTUFF("protostuff");

    private String type;

    private SerializeType(String type) {
        this.type = type;
    }

    public static SerializeType fromString(String type)
	{
		if(type.equalsIgnoreCase("jdk"))
		{
			return JDK;
		}
		else if(type.equalsIgnoreCase("fastjson"))
		{
			return FASTJSON;
		}
		else if(type.equalsIgnoreCase("hessian"))
		{
			return HESSIAN;
		}
		else if(type.equalsIgnoreCase("protostuff"))
		{
			return PROTOSTUFF;
		}
		
		return PROTOSTUFF;
	}

    /**
     * Get serialize type.
     *
     * @return the serialize type
     */
    public String getType() {
        return this.type;
    }

}
