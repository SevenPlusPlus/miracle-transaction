package com.miracle.common.transaction.tcc.coordinator;

import java.io.Serializable;

import com.miracle.common.transaction.api.TccTransaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class CoordinatorAction implements Serializable{

	private static final long serialVersionUID = 4048225106338968210L;
	
	private CoordinatorActionType actionType;
	
	private TccTransaction tccTransaction;

}
