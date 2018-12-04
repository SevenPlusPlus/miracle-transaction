package com.miracle.common.transaction.rpc.cluster.retry;

import java.lang.reflect.Method;

import com.miracle.common.transaction.annotation.TccTransactional;
import com.miracle.common.transaction.api.TccTransactionContext;
import com.miracle.common.transaction.api.TccTransactionContextLocal;
import com.miracle.module.rpc.cluster.ClusterRetryStrategy;
import com.miracle.module.rpc.core.api.CglibProxyFactory;
import com.miracle.module.rpc.core.api.Invoker;
import com.miracle.module.rpc.core.api.RpcException;
import com.miracle.module.rpc.core.api.RpcRequest;

public class RpcTransactionClusterRetryStrategy implements ClusterRetryStrategy {


	@Override
	public <T> boolean isRetriable(Invoker<T> invoker, RpcRequest request,
								   RpcException lastException) {
		if(lastException != null)
		{
			if(lastException.isTimeout() || lastException.isSerialization())
			{
				TccTransactionContext tccTransactionContext = TccTransactionContextLocal.getInstance().get();
				if(tccTransactionContext != null)
				{
					Class<T> cls = invoker.getInterface();
					Method method = CglibProxyFactory.getOnlyMethodByName(cls, request.getMethodName());
					TccTransactional tcc = method.getAnnotation(TccTransactional.class);
					if(tcc != null)
					{
						return false;
					}
				}
			}
		}
		return true;
	}

}
