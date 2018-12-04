package com.miracle.common.transaction.util;

import java.lang.reflect.Proxy;
import java.util.Map;

import com.miracle.common.miracle_utils.StringUtils;
import com.miracle.common.miracle_utils.TypeUtils;
import com.miracle.common.mq.utils.TransactionConstrants;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import org.aspectj.lang.ProceedingJoinPoint;
import com.google.common.collect.Maps;


public class LogUtils {

	private static final String DEFAULT_ARGS_NAME_PREFIX = "args";
	
	public static Map<String, Object> createMethodParameterMap(
			ProceedingJoinPoint joinPoint) throws Throwable {
		Object target = joinPoint.getTarget();
		if (target instanceof Proxy) {
			return getUnkownFieldsParameterMap(joinPoint.getArgs());
		}
		String classType = joinPoint.getTarget().getClass().getName();
		Class<?> clazz = Class.forName(classType);
		String clazzName = clazz.getName();
		String methodName = joinPoint.getSignature().getName(); // 获取方法名称
		Object[] args = joinPoint.getArgs();// 参数
		return getFieldsParameterMap(
				LogUtils.class.getClass(), clazzName, methodName, args);
	}

	private static Map<String, Object> getUnkownFieldsParameterMap(Object[] args) {
		Map<String, Object> paramMap = Maps.newHashMap();
		if(args != null && args.length > 0)
		{
			int idx = 0;
			for (Object arg : args) {
				paramMap.put(DEFAULT_ARGS_NAME_PREFIX + idx, arg);
				idx++;
			}
		}
		return paramMap;
	}

	/**
	 * java7 根据类名方法名称获取参数名称map的方法。
	 * 
	 * @param cls
	 * @param clazzName
	 * @param methodName
	 * @param args
	 *            参数值数组
	 * @return
	 * @throws NotFoundException
	 */
	private static Map<String, Object> getFieldsParameterMap(
			@SuppressWarnings("rawtypes") Class cls, String clazzName,
			String methodName, Object[] args) throws NotFoundException {
		Map<String, Object> paramMap = Maps.newHashMap();
		ClassPool pool = ClassPool.getDefault();
		ClassClassPath classPath = new ClassClassPath(cls);
		pool.insertClassPath(classPath);

		CtClass cc = pool.get(clazzName);
		CtMethod cm = cc.getDeclaredMethod(methodName);
		MethodInfo methodInfo = cm.getMethodInfo();
		CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
		LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute
				.getAttribute(LocalVariableAttribute.tag);
		if (attr == null) {
		}
		int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
		for (int i = 0; i < cm.getParameterTypes().length; i++) {
			paramMap.put(attr.variableName(i + pos), args[i]);// paramNames即参数名
		}
		return paramMap;
	}
	
	public static String getNextEventId(String eventId)
	{
		if(StringUtils.isEmpty(eventId))
			return TransactionConstrants.DEFAULT_VALID_EVENT;
		
		String[] stepArr = eventId.split("\\.");
		int[] intArr = new int[stepArr.length];
		int i = 0;
		for(String str : stepArr)
		{
			intArr[i++] = TypeUtils.stringToIntegerSafe(str, 1);
		}
		intArr[intArr.length-1]++;
		StringBuilder nextEventId = new StringBuilder();
		for(int idx = 0; idx < intArr.length; idx++)
		{
			if(idx != 0)
			{
				nextEventId.append(".");
			}
			nextEventId.append(intArr[idx]);
		}
		return nextEventId.toString();
	}
	
}
