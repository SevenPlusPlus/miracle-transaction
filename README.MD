 

## 1.环境条件
### 引入依赖包

&ensp;&ensp;使用版本管理器melot-common-parent 1.1.3以上，单独引用的话需要
kkrpc 1.1.8以上，kk-transaction-hist  2.0.3以上。


```
<dependency>
	<groupId>com.melot</groupId>
	<artifactId>melot-transaction-log</artifactId>
	<version>1.0.0</version>
</dependency>
```
### spring环境

&ensp;&ensp;需要使用```spring-boot```启动，底层mq使用了```melot-mq-starter```。另外，启动类需要设置扫描```@com.melot```，启用切面类。(使用传统```aop:aspect```开启也是可以的，这对所有切面通用)

## 2.注解使用

&ensp;&ensp;注解分为```@ClassTransactional```和```@LogTransactional```,```@ClassTransactional```用作表示类下的所有方法开启事务埋点，```@LogTransactional```则针对具体方法开启事务埋点。两个注解的配置属性一致，

```
@Component
@ClassTransactional(module="echoService")
public class CglibImpl {
	
	@Autowired
	private DsDao dsDao;
	
	@LogTransactional(propagation=Propagation.PROPAGATION_SUPPORTS, module="remote")
	public void updateUserInfo(UserInfo userInfo) {
		dsDao.updateUserInfo(BeanUtils.beanToMap(userInfo));
	}
	
	public UserInfo getUserInfo(int userId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId", userId);
		return dsDao.getUserInfo(map);
	}
	
}
```
propagation表示事务传播属性，其值使用枚举类Propagation，含义与spring```@Transactional```一致，默认使用```PROPAGATION_REQUIRED```。module表示模块名称，埋点mq默认使用module+className+module来体现模块，类，方法。

## 3.其他配置项

&ensp;&ensp;类似spring事务，可以在spring-boot的application.properties设置方法事务属性管理。
```
melot.transaction.attributes.set*=PROPAGATION_REQUIRED
melot.transaction.attributes.get*=PROPAGATION_NEVER
```
根据方法名匹配Propagation，对于一个类，同时使用了@ClassTransactional和@LogTransactional，其事务传播属性使用优先级如下
```
@LogTransactional > attributes > @ClassTransactional
```

对于jdk动态代理生成的实现类，由于注解是无法被接口实现类继承，重写的方法也无法继承注解，aop是拦截不到的，例如mybatis mapper。对于以上情况，如果需要添加埋点，可以使用```LogAopMaker```,让接口继承这个标记类，其所有的实现类，子类都能被aop所拦截。
```
import com.melot.common.transaction.log.aop.LogAopMaker;

@ClassTransactional(module="mybatis")
public interface DsDao extends LogAopMaker{
		
	public UserInfo getUserInfo(Map<String, Object> map);
	
	public void updateUserInfo(Map<String, Object> map);
}
```

## 4.对账中心

&ensp;&ensp;启动正常即可在```http://10.0.3.28:10001/kk-transaction-console```查看流水记录
```帐号密码:admin 12345```

![img](http://10.0.16.6/qijia.wang/melot-transaction/raw/master/sources/b7GPyQrCjO.gif)