package com.miracle.common.transaction.tcc;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;


@SuppressWarnings("rawtypes")
public class TccSpringBeanFactory{

    private ConfigurableApplicationContext cfgContext;

    private static final TccSpringBeanFactory INSTANCE = new TccSpringBeanFactory();

    private TccSpringBeanFactory() {
    }

    public static TccSpringBeanFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 防止序列化产生对象
     *
     * @return 防止序列化
     */
    private Object readResolve() {
        return INSTANCE;
    }

    /**
     * 获取一个Bean信息
     *
     * @param type 类型
     * @param <T>  泛型
     * @return 对象
     */
    public <T> T getBean(Class<T> type) {
        return cfgContext.getBean(type);
    }
    
    public <T> T getBean(String beanName, Class<T> requiredType)
    {
    	return cfgContext.getBean(beanName, requiredType);
    }

    /**
     * 获取bean的名字
     *
     * @param type 类型
     * @return bean名字
     */
    public String getBeanName(Class type) {
        return cfgContext.getBeanNamesForType(type)[0];
    }

    public Object getBean(String beanName)
    {
    	return cfgContext.getBean(beanName);
    }
    /**
     * 判断一个bean是否存在Spring容器中.
     *
     * @param type 类型
     * @return 成功 true 失败 false
     */
    public boolean exitsBean(Class type) {
        return cfgContext.containsBean(type.getName());
    }

    /**
     * 动态注册一个Bean动Spring容器中
     *
     * @param beanName  名称
     * @param beanClazz 定义bean
     */

	public void registerBean(String beanName, Class beanClazz, Map<String, Object> propertys) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClazz);
        if (propertys != null) {
        	for(Map.Entry<String, Object> entry : propertys.entrySet())
        	{
        		builder.addPropertyValue(entry.getKey(), entry.getValue());
        	}
        }
        builder.setScope(BeanDefinition.SCOPE_SINGLETON);
        registerBean(beanName, builder.getBeanDefinition());

    }

    public void registerBean(String beanName, Object obj) {
        cfgContext.getBeanFactory().registerSingleton(beanName, obj);
    }

    /**
     * 注册Bean信息
     *
     * @param beanDefinition
     */
    public void registerBean(String beanName, BeanDefinition beanDefinition) {
        BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) cfgContext.getBeanFactory();
        beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinition);
    }

    /**
     * 根据枚举类型获取Spring注册的Bean
     *
     * @param annotationType 枚举
     * @return
     */
    public Map<String, Object> getBeanWithAnnotation(Class<? extends Annotation> annotationType) {
        return cfgContext.getBeansWithAnnotation(annotationType);
    }

    /**
     * 动态注册一个Bean动Spring容器中
     *
     * @param beanName  名称
     * @param beanClazz 定义bean
     */
    public void registerBean(String beanName, Class beanClazz) {
        registerBean(beanName, beanClazz, null);
    }

	public void setApplicationContext(ApplicationContext applicationContext)
	{
		this.cfgContext = (ConfigurableApplicationContext)applicationContext;	
	}
}
