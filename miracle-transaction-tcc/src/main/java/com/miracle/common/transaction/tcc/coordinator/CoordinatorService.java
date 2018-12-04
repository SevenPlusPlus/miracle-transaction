package com.miracle.common.transaction.tcc.coordinator;

import com.miracle.common.transaction.api.TccTransaction;

public interface CoordinatorService {
	/**
     * 启动本地补偿事务，根据配置是否进行补偿
     *
     * @throws Exception 异常
     */
    void start() throws Exception;

    /**
     * 保存补偿事务信息
     *
     * @param tccTransaction 实体对象
     * @return 主键id
     */
    Long save(TccTransaction tccTransaction);

    /**
     * 根据事务id获取TccTransaction
     *
     * @param transactionId 事务id
     * @return TccTransaction
     */
    TccTransaction findByTransId(Long transactionId);


    /**
     * 删除补偿事务信息
     *
     * @param transactionId 主键id
     * @return true成功 false 失败
     */
    boolean remove(Long transactionId);


    /**
     * 更新
     *
     * @param tccTransaction 实体对象
     */
    void update(TccTransaction tccTransaction);


    /**
     * 更新 List<Participant>  只更新这一个字段数据
     * @param tccTransaction  实体对象
     * @return rows
     */
    int updateParticipant(TccTransaction tccTransaction);


    /**
     * 更新补偿数据状态
     * @param id  事务id
     * @param status  状态
     * @return  rows 1 成功 0 失败
     */
    int updateStatus(Long transactionId, Integer status);

    /**
     * 提交补偿操作
     *
     * @param coordinatorAction 执行动作
     * @return true 成功
     */
    Boolean submit(CoordinatorAction coordinatorAction);
}
