package com.xxb.mybatis;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.xxb.base.OptimisticLockingFailureException;

public class MyExecutor implements Executor {

	private Executor executor;

	public MyExecutor(Executor executor) {
		this.executor = executor;
	}

	@Override
	public int update(MappedStatement ms, Object parameter) throws SQLException {
		if (StringUtils.endsWith(ms.getId(), "update")) {
			// version lock
			Field verField = PluginUtil.findVersionField(parameter);
			if (verField != null) {
				int result = executor.update(ms, parameter);
				if (result != 1) {
					throw new OptimisticLockingFailureException("optimistic lock failed.");
				}
				return result;
			}
		}
		return executor.update(ms, parameter);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
			CacheKey cacheKey, BoundSql boundSql) throws SQLException {
		List<E> rows = executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
		if (rows.isEmpty()) {
			return rows;
		}
		E obj = rows.get(0);
		// page implement
		if (obj instanceof MybatisPage) {
			MappedStatement listms = ms.getConfiguration().getMappedStatement(ms.getId()+MybatisPage.PageQuerySuffix);
			MybatisPage page = (MybatisPage) obj;
			CacheKey listCacheKey = executor.createCacheKey(listms, parameter, rowBounds, listms.getBoundSql(parameter));
			List<?> content = executor.query(listms, parameter, rowBounds, resultHandler, listCacheKey, listms.getBoundSql(parameter));
			PageRequest pageRequest = null;
			if (parameter instanceof Pageable) {
				pageRequest = (PageRequest) parameter;
			}
			if (parameter instanceof Map) {
				Map<String, Object> paraMap = (Map<String, Object>) parameter;
				pageRequest = (PageRequest) paraMap.get("pageRequest");
			}
			page.build(pageRequest, content);
		}
		return rows;
	}

	@Override
	public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler)
			throws SQLException {
		BoundSql boundSql = ms.getBoundSql(parameter);
		return query(ms, parameter, rowBounds, resultHandler,
				executor.createCacheKey(ms, parameter, rowBounds, boundSql), boundSql);
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
	public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
		executor.queryCursor(ms, parameter, rowBounds);
		return null;
	}

	@Override
	public void setExecutorWrapper(Executor executor) {
		executor.setExecutorWrapper(executor);
	}

}
