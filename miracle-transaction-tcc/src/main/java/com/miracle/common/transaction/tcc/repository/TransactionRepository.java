package com.miracle.common.transaction.tcc.repository;

import java.util.List;

import com.miracle.common.transaction.api.TccTransaction;

public interface TransactionRepository {
	 /**
     * 创建本地事务对象
     *
     * @param tccTransaction 事务对象
     * @return rows
     */
    int create(TccTransaction tccTransaction);

    /**
     * 删除对象
     *
     * @param transactionId 事务对象id
     * @return rows
     */
    int remove(Long transactionId);


    /**
     * 更新数据
     *
     * @param tccTransaction 事务对象
     * @return rows 1 成功 0 失败 失败需要抛异常
     */
    int update(TccTransaction tccTransaction);


    /**
     * 更新 List<Participant>  只更新这一个字段数据
     *
     * @param tccTransaction 实体对象
     * @return rows 1 成功 0 失败
     */
    int updateParticipant(TccTransaction tccTransaction);


    /**
     * 更新补偿数据状态
     * @param transactionId  事务id
     * @param status  状态
     * @return  rows 1 成功 0 失败
     */
    int updateStatus(Long transactionId, Integer status);

    /**
     * 根据id获取对象
     *
     * @param transactionId 主键id
     * @return TccTransaction
     */
    TccTransaction findById(Long transactionId);

    /**
     * 获取需要提交的事务
     *
     * @return List<TccTransaction>
     */
	List<TccTransaction> listAll();


    /**
     * 获取延迟多长时间后的事务信息,只要为了防止并发的时候，刚新增的数据被执行
     *
     * @param timestamp 延迟后的时间
     * @return List<TccTransaction>
     */
    List<TccTransaction> listAllByDelay(Long timestamp);

}
