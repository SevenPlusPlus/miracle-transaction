package com.miracle.common.transaction.annotation.api;


public enum Propagation {
	 /**
     * PropagationEnum required propagation.
     */
    PROPAGATION_REQUIRED(0),

    /**
     * PropagationEnum supports propagation.
     */
    PROPAGATION_SUPPORTS(1),

    /**
     * PropagationEnum mandatory propagation.
     */
    PROPAGATION_MANDATORY(2),

    /**
     * PropagationEnum requires new propagation.
     */
    PROPAGATION_REQUIRES_NEW(3);


    private final int value;

    Propagation(int value) {
        this.value = value;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public int getValue() {
        return this.value;
    }
    
    public static Propagation parsePropaByString(String propStr)
	{
    	Propagation type = null;
		switch(propStr)
		{
		case "PROPAGATION_REQUIRED":
			type = PROPAGATION_REQUIRED;
			break;
		case "PROPAGATION_MANDATORY":
			type = PROPAGATION_MANDATORY;
			break;
		case "PROPAGATION_REQUIRES_NEW":
			type = PROPAGATION_REQUIRES_NEW;
			break;
		case "PROPAGATION_SUPPORTS":
			type = PROPAGATION_SUPPORTS;
			break;
		default:
			break;
		}
		return type;
	}
}
