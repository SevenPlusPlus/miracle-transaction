package com.miracle.common.transaction.threadpool;



public enum RejectedPolicyType {
    /**
     * Abort policy rejected policy type enum.
     */
    ABORT_POLICY("Abort"),
    /**
     * Blocking policy rejected policy type enum.
     */
    BLOCKING_POLICY("Blocking"),
    /**
     * Caller runs policy rejected policy type enum.
     */
    CALLER_RUNS_POLICY("CallerRuns"),
    /**
     * Discarded policy rejected policy type enum.
     */
    DISCARDED_POLICY("Discarded"),
    /**
     * Rejected policy rejected policy type enum.
     */
    REJECTED_POLICY("Rejected");

    private String type;

    RejectedPolicyType(String type) {
        this.type = type;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public String getType() {
        return type;
    }

    /**
     * From string rejected policy type enum.
     *
     * @param value the value
     * @return the rejected policy type enum
     */
    public static RejectedPolicyType fromString(String type) {
    	if(type.equalsIgnoreCase("Abort"))
    	{
    		return ABORT_POLICY;
    	}
    	else if(type.equalsIgnoreCase("Blocking"))
    	{
    		return BLOCKING_POLICY;
    	}
    	else if(type.equalsIgnoreCase("CallerRuns"))
    	{
    		return CALLER_RUNS_POLICY;
    	}
    	else if(type.equalsIgnoreCase("Discarded"))
    	{
    		return DISCARDED_POLICY;
    	}
    	else if(type.equalsIgnoreCase("Rejected"))
    	{
    		return REJECTED_POLICY;
    	}
    	
    	return ABORT_POLICY;
    }

    @Override
    public String toString() {
        return type;
    }
}

