package com.miracle.common.transaction.api;

public enum RepositoryType {
	DB("db");
	
	private String type;
	
	RepositoryType(String type)
	{
		this.type = type;
	}
	
	public String getType()
	{
		return this.type;
	}

	public static RepositoryType fromString(String type)
	{
		if(type.equalsIgnoreCase("db"))
		{
			return DB;
		}
		
		return DB;
	}
	
}
