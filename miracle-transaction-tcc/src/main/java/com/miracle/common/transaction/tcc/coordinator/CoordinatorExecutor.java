package com.miracle.common.transaction.tcc.coordinator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CoordinatorExecutor {

	@Autowired
	private CoordinatorService coordinatorService;
	
	  /**
     * 执行协调命令接口
     *
     * @param coordinatorAction 协调数据
     */
    public void execute(CoordinatorAction coordinatorAction) {
        coordinatorService.submit(coordinatorAction);
    }
}
