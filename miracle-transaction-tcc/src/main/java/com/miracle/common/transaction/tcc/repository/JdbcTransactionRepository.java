package com.miracle.common.transaction.tcc.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import com.miracle.common.transaction.api.Participant;
import com.miracle.common.transaction.api.TccTransaction;
import com.miracle.common.transaction.exception.TccException;
import com.miracle.common.transaction.serializer.ObjectSerializer;
import com.miracle.common.transaction.serializer.ObjectSerializerFactory;
import com.miracle.common.transaction.tcc.TccSpringBeanFactory;
import com.miracle.common.transaction.tcc.config.TccConfig;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


@Component
@Slf4j
public class JdbcTransactionRepository implements TransactionRepository, ApplicationContextAware {

	@Autowired
    private TccConfig tccConfig;
	
	private DataSource dataSource;

    private String tableName;

    private ObjectSerializer serializer;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		TccSpringBeanFactory.getInstance().setApplicationContext(applicationContext);
	}
    
    @PostConstruct
    public void init() {
    	String modelName = tccConfig.getModuleName();
    	@SuppressWarnings("unchecked")
		Map<String, DataSource> datesouceMap = (Map<String, DataSource>) TccSpringBeanFactory.getInstance().getBean("getDataSourceMap");
    	if(datesouceMap != null)
    	{
    		this.dataSource = datesouceMap.get(tccConfig.getDatasourceName());
    	}
    	if(this.dataSource == null)
    	{
    		throw new IllegalStateException("No datasouce available for JdbcTransactionRepository: " + 
    				tccConfig.getDatasourceName());
    	}
    	this.serializer = ObjectSerializerFactory.createSerializerByName(tccConfig.getSerializer());
    	this.tableName = "tcc_" + modelName.replaceAll("-", "_");
        executeUpdate(SqlHelper.buildCreateTableSql(tccConfig.getDbType(), tableName));
    }


    @Override
    public int create(TccTransaction tccTransaction) {
        String sql = "insert into " + tableName + "(transaction_id,status,role,mode,target_class,target_method,retried_count,create_time,last_update_time,version,invocation)" +
                " values(?,?,?,?,?,?,?,?,?,?,?)";
        try {

        	Timestamp createTime = new Timestamp(tccTransaction.getCreateTime());
        	Timestamp lastUpdateTime = new Timestamp(tccTransaction.getLastUpdateTime());
            final byte[] serialize = serializer.serialize(tccTransaction.getParticipants());
            return executeUpdate(sql, tccTransaction.getTransactionId(), tccTransaction.getStatus(), tccTransaction.getRole(), 
            		tccTransaction.getMode(), tccTransaction.getTargetClass(), tccTransaction.getTargetMethod(),
                    tccTransaction.getRetriedCount(), createTime, lastUpdateTime,
                    tccTransaction.getVersion(), serialize);

        } catch (TccException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int remove(Long id) {
        String sql = "delete from " + tableName + " where transaction_id = ? ";
        return executeUpdate(sql, id);
    }

    /**
     * 更新数据
     *
     * @param tccTransaction 事务对象
     * @return rows 1 成功 0 失败 失败需要抛异常
     */
    @Override
    public int update(TccTransaction tccTransaction) {

        final Integer currentVersion = tccTransaction.getVersion();
        tccTransaction.setLastUpdateTime(System.currentTimeMillis());
        tccTransaction.setVersion(tccTransaction.getVersion() + 1);

        String sql = "update " + tableName +
                " set last_update_time = ?,version =?,retried_count =?,invocation=?,status=? ,confirm_method=?,cancel_method=? ,mode=? where transaction_id = ? and version=? ";
        String confirmMethod = "";
        String cancelMethod = "";
        try {
            final byte[] serialize = serializer.serialize(tccTransaction.getParticipants());

            if (CollectionUtils.isNotEmpty(tccTransaction.getParticipants())) {
                final Participant participant = tccTransaction.getParticipants().get(0);
                if(participant.getConfirmTccInvocation() != null)
                {
                	confirmMethod = participant.getConfirmTccInvocation().getMethodName();
                }
                if(participant.getCancelTccInvocation() != null)
                {
                	cancelMethod = participant.getCancelTccInvocation().getMethodName();
                }
            }
            Timestamp lastUpdateTime = new Timestamp(tccTransaction.getLastUpdateTime());
            return executeUpdate(sql, lastUpdateTime,
                    tccTransaction.getVersion(), tccTransaction.getRetriedCount(), serialize,
                    tccTransaction.getStatus(), confirmMethod, cancelMethod, tccTransaction.getMode(),
                    tccTransaction.getTransactionId(), currentVersion);

        } catch (TccException e) {
            e.printStackTrace();
            return 0;
        }


    }

    /**
     * 更新 List<Participant>  只更新这一个字段数据
     *
     * @param tccTransaction 实体对象
     */
    @Override
    public int updateParticipant(TccTransaction tccTransaction) {

        String sql = "update " + tableName +
                " set invocation=?  where transaction_id = ?  ";

        try {
            final byte[] serialize = serializer.serialize(tccTransaction.getParticipants());

            return executeUpdate(sql, serialize,
                    tccTransaction.getTransactionId());

        } catch (TccException e) {
            e.printStackTrace();
            return 0;
        }


    }

    /**
     * 更新补偿数据状态
     *
     * @param id     事务id
     * @param status 状态
     * @return rows 1 成功 0 失败
     */
    @Override
    public int updateStatus(Long id, Integer status) {
        String sql = "update " + tableName +
                " set status=?  where transaction_id = ?  ";
        return executeUpdate(sql, status, id);


    }


    /**
     * 根据id获取对象
     *
     * @param id 主键id
     * @return TccTransaction
     */
    @Override
    public TccTransaction findById(Long id) {
        String selectSql = "select * from " + tableName + " where transaction_id=?";
        List<Map<String, Object>> list = executeQuery(selectSql, id);
        if (CollectionUtils.isNotEmpty(list))
        {
        	for(Map<String, Object> item : list)
        	{
        		return this.buildByResultMap(item);
        	}
        }
        return null;
    }

    /**
     * 获取需要提交的事务
     *
     * @return List<TransactionRecover>
     */
    @Override
    public List<TccTransaction> listAll() {
        String selectSql = "select * from " + tableName;
        List<Map<String, Object>> list = executeQuery(selectSql);
        if (CollectionUtils.isNotEmpty(list)) {
        	List<TccTransaction> retList = Lists.newArrayList();
        	for(Map<String, Object> item : list)
        	{
        		retList.add(this.buildByResultMap(item));
        	}
            return retList;
        }
        return null;
    }

    /**
     * 获取延迟多长时间后的事务信息,只要为了防止并发的时候，刚新增的数据被执行
     *
     * @param date 延迟后的时间
     * @return List<TccTransaction>
     */
    @Override
    public List<TccTransaction> listAllByDelay(Long timestamp) {
        String sb = "select * from " +
                tableName +
                " where last_update_time < ?";

        Timestamp delyTm = new Timestamp(timestamp);
        List<Map<String, Object>> list = executeQuery(sb, delyTm);

        if (CollectionUtils.isNotEmpty(list)) {
        	List<TccTransaction> retList = Lists.newArrayList();
        	for(Map<String, Object> item : list)
        	{
        		retList.add(this.buildByResultMap(item));
        	}
            return retList;
        }

        return null;
    }


    private TccTransaction buildByResultMap(Map<String, Object> map) {
        TccTransaction tccTransaction = new TccTransaction();
        tccTransaction.setTransactionId((Long) map.get("transaction_id"));
        tccTransaction.setRetriedCount((Integer) map.get("retried_count"));
        tccTransaction.setCreateTime(((Timestamp) map.get("create_time")).getTime());
        tccTransaction.setLastUpdateTime(((Timestamp) map.get("last_update_time")).getTime());
        tccTransaction.setVersion((Integer) map.get("version"));
        tccTransaction.setStatus((Integer) map.get("status"));
        tccTransaction.setRole((Integer) map.get("role"));
        tccTransaction.setMode((Integer) map.get("mode"));
        byte[] bytes = (byte[]) map.get("invocation");
        try {
            @SuppressWarnings("unchecked")
			final List<Participant> participants = serializer.deSerialize(bytes, CopyOnWriteArrayList.class);
            tccTransaction.setParticipants(participants);
        } catch (TccException e) {
            e.printStackTrace();
        }
        return tccTransaction;
    }

    private int executeUpdate(String sql, Object... params) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject((i + 1), params[i]);
                }
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("executeUpdate-> " + e.getMessage());
        } finally {
            close(connection, ps, null);
        }
        return 0;
    }

    private List<Map<String, Object>> executeQuery(String sql, Object... params) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> list = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject((i + 1), params[i]);
                }
            }
            rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> rowData = Maps.newHashMap();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), rs.getObject(i));
                }
                list.add(rowData);
            }
        } catch (SQLException e) {
            log.error("executeQuery-> " + e.getMessage());
        } finally {
            close(connection, ps, rs);
        }
        return list;
    }

    private void close(Connection connection,
                       PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
