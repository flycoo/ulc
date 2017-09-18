package com.czp.ulc.core.dao;

import java.util.List;

import org.apache.ibatis.annotations.SelectProvider;

import com.czp.ulc.core.bean.ProcessorBean;
import com.czp.ulc.core.mybatis.BaseDao;
import com.czp.ulc.core.mybatis.DynamicSql;

/**
 * function
 *
 * <li>创建人：Jeff.cao</li>
 * <li>创建时间：2017年9月18日-下午3:32:36</li>
 * 
 * @version 0.0.1
 */
public interface ProcessorDao extends BaseDao<ProcessorBean>{

	@SelectProvider(type = DynamicSql.class, method = "queryProc")
	List<ProcessorBean> query(ProcessorBean arg);

	@SelectProvider(type = DynamicSql.class, method = "getProcById")
	ProcessorBean get(Integer id);
}