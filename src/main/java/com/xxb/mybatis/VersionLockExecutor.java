package com.xxb.mybatis;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import com.xxb.base.OptimisticLockingFailureException;

public class VersionLockExecutor implements Executor {
	
	private Executor executor;
	
	public VersionLockExecutor(Executor executor) {
		this.executor = executor;
	}

	@Override
	public int update(MappedStatement ms, Object parameter) throws SQLException {
		if (StringUtils.endsWith(ms.getId(), "update")) {
			Field verField = PluginUtil.findVersionField(parameter);
			if (verField != null) {
				int result = executor.update(ms, parameter);
				if (result != 1) {
					throw new OptimisticLockingFailureException("optimistic lock failed.");
				}
			}
		}
		return executor.update(ms, parameter);
	}

	@Override
	public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
			CacheKey cacheKey, BoundSql boundSql) throws SQLException {
		return executor.query(ms, parameter, rowBounds, resultHandler);
	}

	@Override
	public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler)
			throws SQLException {
		return executor.query(ms, parameter, rowBounds, resultHandler);
	}

	@Override
	public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
		return executor.queryCursor(ms, parameter, rowBounds);
	}

	@Override
	public List<BatchResult> flushStatements() throws SQLException {
		return executor.flushStatements();
	}

	@Override
	public void commit(boolean required) throws SQLException {
		executor.commit(required);
	}

	@Override
	public void rollback(boolean required) throws SQLException {
		executor.rollback(required);
	}

	@Override
	public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
		return executor.createCacheKey(ms, parameterObject, rowBounds, boundSql);
	}

	@Override
	public boolean isCached(MappedStatement ms, CacheKey key) {
		return executor.isCached(ms, key);
	}

	@Override
	public void clearLocalCache() {
		executor.clearLocalCache();
	}

	@Override
	public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key,
			Class<?> targetType) {
		executor.deferLoad(ms, resultObject, property, key, targetType);
	}

	@Override
	public Transaction getTransaction() {
		return executor.getTransaction();
	}

	@Override
	public void close(boolean forceRollback) {
		executor.close(forceRollback);
	}

	@Override
	public boolean isClosed() {
		return executor.isClosed();
	}

	@Override
	public void setExecutorWrapper(Executor executor) {
		executor.setExecutorWrapper(executor);
	}

}
