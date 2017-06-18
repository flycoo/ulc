package com.czp.ulc.common.dao;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;

import com.czp.ulc.common.bean.IndexMeta;
import com.czp.ulc.common.mybatis.BaseDao;
import com.czp.ulc.common.mybatis.DynamicSql;

/**
 * @dec Function
 * @author coder_czp@126.com
 * @date 2017年5月26日/下午9:39:00
 * @copyright coder_czp@126.com
 *
 */
public interface IndexMetaDao extends BaseDao<IndexMeta> {

	@SelectProvider(type = DynamicSql.class, method = "countIndexMeta")
	IndexMeta count(Integer shardId);
	
	@InsertProvider(type = DynamicSql.class, method = "addIndexMeta")
	int add(IndexMeta meta);

}
