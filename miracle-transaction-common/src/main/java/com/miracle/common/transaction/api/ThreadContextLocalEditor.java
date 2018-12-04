package com.miracle.common.transaction.api;

import java.util.Map;

import com.google.common.collect.Maps;

public class ThreadContextLocalEditor {

	public Map<String, String> getLocalContextAttachments()
	{
		Map<String, String> attachments = Maps.newHashMap();
		//do something here:
		return attachments;
	}
	
	public void setLocalContextFromAttachments(Map<String, String> attachments)
	{
		if(attachments != null)
		{
			//do something here:
		}
	}
}
