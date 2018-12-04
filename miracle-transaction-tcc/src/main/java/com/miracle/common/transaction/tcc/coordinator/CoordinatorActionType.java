package com.miracle.common.transaction.tcc.coordinator;

public enum CoordinatorActionType {
	 /**
     * Save coordinator action enum.
     */
    SAVE(0, "保存"),

    /**
     * Delete coordinator action enum.
     */
    DELETE(1, "删除"),

    /**
     * Update coordinator action enum.
     */
    UPDATE(2, "更新"),

    /**
     * Rollback coordinator action enum.
     */
    ROLLBACK(3, "回滚"),

    /**
     * Compensation coordinator action enum.
     */
    COMPENSATION(4, "补偿");

    private int type;

    private String desc;

    CoordinatorActionType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }
    
    public static CoordinatorActionType valueOf(int mtype)
    {
    	switch(mtype)
    	{
    	case 0:
    		return SAVE;
    	case 1:
    		return DELETE;
    	case 2:
    		return UPDATE;
    	case 3:
    		return ROLLBACK;
    	default:
    		return COMPENSATION;
    	}
    }

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
