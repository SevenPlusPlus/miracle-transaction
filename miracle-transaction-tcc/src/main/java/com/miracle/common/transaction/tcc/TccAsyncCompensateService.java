package com.miracle.common.transaction.tcc;

import java.util.concurrent.ExecutorService;

import javax.annotation.PostConstruct;

import com.miracle.common.transaction.tcc.config.TccConfig;
import com.miracle.common.transaction.tcc.coordinator.TccTransactionThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class TccAsyncCompensateService {
	
	@Autowired
	private TccConfig tccConfig;
	
	private ExecutorService executorService;
	
	@PostConstruct
	private void init()
	{
		int coreSize = tccConfig.getAsyncCompensateThreadCore();
		int maxSize = tccConfig.getAsyncCompensateThreadMax();
		executorService = TccTransactionThreadPool.newCustomCacheableThreadPool(
 				tccConfig, "asyncCompensateExecutor", coreSize, maxSize);
	}
	
	public void execute(Runnable r)
	{
		executorService.submit(r);
	}
}
