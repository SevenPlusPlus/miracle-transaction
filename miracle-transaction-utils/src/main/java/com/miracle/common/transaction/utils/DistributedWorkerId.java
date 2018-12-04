package com.miracle.common.transaction.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class DistributedWorkerId {

	@Value("${melot.distributed.workerid.server}")
	private String fetchServer;
	
	@Value("${melot.distributed.workerid.backupserver}")
	private String backupServer;
	
	@Value("${melot.distributed.workerid.name}")
	private String workerName;
	
	private final String fetchUrl = "/api/workerId/";
	
	private final long INITIAL_DELAY = 60;
	
	private final long UPDATE_PERIOD = 60;
	
	private final int MAX_FAILED_COUNT = 3;
	
	@Getter
	private volatile int curWorkerId = -1;
	
	private volatile int failedCount = 0;
	
	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory(){

		private AtomicInteger idx = new AtomicInteger(0);
		@Override
		public Thread newThread(Runnable r) {
			String groupName = "DistributedWorkerIdUpdater_";
			Thread newThread = new Thread(r);
			newThread.setDaemon(true);
			newThread.setName(groupName + idx.getAndAdd(1));
			return newThread;
		}
		
	});
	
	private int fetchWorkerId()
	{
		int workerId = -1;
		
		final String fullUrl = fetchServer + fetchUrl + workerName;		
		RestTemplate restTemplate = new RestTemplate();
		try{
			workerId = restTemplate.getForObject(fullUrl, Integer.class);
		}
		catch(Exception e)
		{
			if(!StringUtils.isEmpty(backupServer))
			{
				final String backupUrl = backupServer + fetchUrl + workerName;
				workerId = restTemplate.getForObject(backupUrl, Integer.class);
			}
			else
			{
				throw e;
			}
		}
		
		return workerId;
	}
	
	private int renewWorkerId(int workerId)
	{
		int newWorkerId = -1;
		
		final String fullUrl = fetchServer + fetchUrl + workerName;
		
		RestTemplate restTemplate = new RestTemplate();
		try{
			newWorkerId = restTemplate.postForObject(fullUrl, workerId, Integer.class);
		}
		catch(Exception e)
		{
			if(!StringUtils.isEmpty(backupServer))
			{
				final String backupUrl = backupServer + fetchUrl + workerName;
				newWorkerId = restTemplate.postForObject(backupUrl, workerId, Integer.class);
			}
			else
			{
				throw e;
			}
		}
		return newWorkerId;
	}
	
	@PostConstruct
	private void init() {
		curWorkerId = fetchWorkerId();
		
		executor.scheduleAtFixedRate(new Runnable(){

			@Override
			public void run() {
				try {
					DistributedWorkerId.this.curWorkerId = 
							renewWorkerId(curWorkerId);
					failedCount = 0;
				}
				catch(Exception e)
				{
					log.error("Renew workerId {} for {} failed.", 
							DistributedWorkerId.this.curWorkerId, DistributedWorkerId.this.workerName, e);
					failedCount++;
				}
				if(failedCount >= MAX_FAILED_COUNT)
				{
					DistributedWorkerId.this.curWorkerId = -1;
				}
			}
			
		}, INITIAL_DELAY, UPDATE_PERIOD, TimeUnit.SECONDS);
	}
}
