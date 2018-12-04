package com.miracle.common.transaction.tcc;

import javax.annotation.PostConstruct;

import com.miracle.common.transaction.tcc.config.TccConfig;
import com.miracle.common.transaction.tcc.coordinator.CoordinatorService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class TccTransactionBoostrap{

	@Autowired
	private TccConfig tccConfig;
	
	@Autowired
	private CoordinatorService coordinatorService;
	
	@PostConstruct
	private void init()
	{
		try {
			coordinatorService.start();
		} catch (Exception e) {
			log.error("Tcc transaction init failed", e);
		}
		log.info("Tcc transaction init successfully！");
	}
}
