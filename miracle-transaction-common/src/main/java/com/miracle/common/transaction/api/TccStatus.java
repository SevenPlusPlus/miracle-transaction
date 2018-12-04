package com.miracle.common.transaction.api;

public enum TccStatus {
	TRYING(0,"开始执行try"),

	TRY_FAIL(1, "try异常完成"),
   
    TRY_DONE(2, "try阶段完成"),

    CONFIRMING(3, "confirm阶段"),

    CANCELING(4, "cancel阶段");


    private int status;

    private String desc;

    TccStatus(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }


    public static TccStatus valueOf(int status) {
    	 switch (status) {
    	 case 0:
    		 return TRYING;
         case 1:
        	 return TRY_FAIL;
         case 2:
             return TRY_DONE;
         case 3:
             return CONFIRMING;
         default:
             return CANCELING;
     }
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
