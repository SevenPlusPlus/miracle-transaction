package com.miracle.common.transaction.log.cluster.retry;

import java.lang.reflect.Method;

import com.miracle.common.transaction.annotation.LogTransactional;

import com.miracle.common.transaction.api.LogTransactionContext;
import com.miracle.common.transaction.api.LogTransactionContextLocal;
import com.miracle.module.rpc.cluster.ClusterRetryStrategy;
import com.miracle.module.rpc.core.api.CglibProxyFactory;
import com.miracle.module.rpc.core.api.Invoker;
import com.miracle.module.rpc.core.api.RpcException;
import com.miracle.module.rpc.core.api.RpcRequest;

public class LogTransactionClusterRetryStrategy implements ClusterRetryStrategy {


	@Override
	public <T> boolean isRetriable(Invoker<T> invoker, RpcRequest request,
								   RpcException lastException) {
		if(lastException != null)
		{
			if(lastException.isTimeout() || lastException.isSerialization())
			{
				LogTransactionContext tccTransactionContext = LogTransactionContextLocal.getInstance().get();
				if(tccTransactionContext != null)
				{
					Class<T> cls = invoker.getInterface();
					Method method = CglibProxyFactory.getOnlyMethodByName(cls, request.getMethodName());
					LogTransactional logTransaction = method.getAnnotation(LogTransactional.class);
					if(logTransaction != null)
					{
						return false;
					}
				}
			}
		}
		return true;
	}

}
