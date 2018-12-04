package com.miracle.common.transaction.threadpool;



public enum BlockingQueueType {

    /**
     * Linked blocking queue blocking queue type enum.
     */
    LINKED_BLOCKING_QUEUE("Linked"),
    /**
     * Array blocking queue blocking queue type enum.
     */
    ARRAY_BLOCKING_QUEUE("Array"),
    /**
     * Synchronous queue blocking queue type enum.
     */
    SYNCHRONOUS_QUEUE("SynchronousQueue");

    private String type;

    BlockingQueueType(String type) {
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
     * From string blocking queue type enum.
     *
     * @param value the value
     * @return the blocking queue type enum
     */
    public static BlockingQueueType fromString(String type) {
    	if(type.equalsIgnoreCase("Linked"))
    	{
    		return LINKED_BLOCKING_QUEUE;
    	}
    	else if(type.equalsIgnoreCase("Array"))
    	{
    		return ARRAY_BLOCKING_QUEUE;
    	}
    	else if(type.equalsIgnoreCase("SynchronousQueue"))
    	{
    		return SYNCHRONOUS_QUEUE;
    	}
    	
    	return LINKED_BLOCKING_QUEUE;
    }

    @Override
    public String toString() {
        return type;
    }
}

