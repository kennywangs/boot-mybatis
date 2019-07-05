package com.xxb.mybatis;

import java.sql.Connection;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

//import com.alibaba.druid.pool.DruidDataSource;
//import com.alibaba.druid.util.JdbcConstants;

@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
	@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class PageInterceptor implements Interceptor {

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		if (PluginUtil.processTarget(invocation.getTarget()) instanceof StatementHandler) {
			handleStatementHandler(invocation);
		}
		return invocation.proceed();
	}

	private void handleStatementHandler(Invocation invocation) {
		StatementHandler statementHandler = (StatementHandler) PluginUtil.processTarget(invocation.getTarget());
		MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
		MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
		if (!SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())) {
            return;
        }
		String mapId = mappedStatement.getId();
		boolean isPageable = mapId.contains(".queryPagedEntity") && mapId.endsWith(MybatisPage.PageQuerySuffix);
		if (isPageable) {
			Object paraObject = statementHandler.getBoundSql().getParameterObject();
			PageRequest pageRequest = null;
			if (paraObject instanceof Pageable) {
				pageRequest = (PageRequest) paraObject;
			}
			if (paraObject instanceof Map) {
				Map<String, Object> paraMap = (Map<String, Object>) paraObject;
				pageRequest = (PageRequest) paraMap.get("pageRequest");
			}
			/**
			 * 不需要分页的场合，如果 size 小于 0 返回结果集
			 */
			if (null == pageRequest || pageRequest.getPageSize() < 0) {
	            return;
	        }
			String sql = (String) metaObject.getValue("delegate.boundSql.sql");
			sql = sql.trim();
//			DruidDataSource ds = (DruidDataSource) SpringContextUtils.getBeanById("moduleDataSource");
//			String dbtype = ds.getDbType();
			String pageSql = sql;
			pageSql = sql + " LIMIT " + pageRequest.getPageSize() + " OFFSET " + pageRequest.getOffset();
//			if (StringUtils.equals(dbtype, JdbcConstants.POSTGRESQL) || StringUtils.equals(dbtype, JdbcConstants.MYSQL) || StringUtils.equals(dbtype, JdbcConstants.MARIADB)) {
//				pageSql = sql + " LIMIT " + pageRequest.getPageSize() + " OFFSET " + pageRequest.getOffset();
//			} else if (StringUtils.equals(dbtype, JdbcConstants.SQL_SERVER) || StringUtils.equals(dbtype, JdbcConstants.JTDS)){
//				pageSql = getSqlServersql(sql,pageRequest);
//			} else if (StringUtils.equals(dbtype, JdbcConstants.ORACLE)) {
//				pageSql = "SELECT * FROM ( SELECT TMP.*, ROWNUM ROW_ID FROM ( " +
//						sql + " ) TMP WHERE ROWNUM <=" + (pageRequest.getOffset()+pageRequest.getPageSize()) + ") WHERE ROW_ID > " + pageRequest.getOffset();
//			}
			
			metaObject.setValue("delegate.boundSql.sql", pageSql);
			metaObject.setValue("delegate.rowBounds.offset", RowBounds.NO_ROW_OFFSET);
			metaObject.setValue("delegate.rowBounds.limit", RowBounds.NO_ROW_LIMIT);
		}
	}

	@Override
	public Object plugin(Object target) {
		if (Executor.class.isAssignableFrom(target.getClass())) {
			PageExecutor executor = new PageExecutor((Executor)target);
			return Plugin.wrap(executor, this);
		}
		if (target instanceof StatementHandler) {
			return Plugin.wrap(target, this);
		}
		return target;
	}

	@Override
	public void setProperties(Properties properties) {
		
	}
	
	private String getSqlServersql(String originalSql, PageRequest pageRequest) {
		StringBuilder pagingBuilder = new StringBuilder();
		String loweredString = originalSql.toLowerCase();
		String orderby = getOrderByPart(originalSql);
		String distinctStr = StringUtils.EMPTY;
		String sqlPartString = originalSql;
		if (loweredString.startsWith("select")) {
            int index = 6;
            if (loweredString.startsWith("select distinct")) {
                distinctStr = "DISTINCT ";
                index = 15;
            }
            sqlPartString = sqlPartString.substring(index);
        }
		pagingBuilder.append(sqlPartString);
		// if no ORDER BY is specified use fake ORDER BY field to avoid errors
        if (StringUtils.isEmpty(orderby)) {
            orderby = "ORDER BY CURRENT_TIMESTAMP";
        }
        long firstParam = pageRequest.getOffset() + 1;
        long secondParam = pageRequest.getOffset() + pageRequest.getPageSize();
		String pageSql = "WITH selectTemp AS (SELECT " + distinctStr + "TOP 100 PERCENT " +
	            " ROW_NUMBER() OVER (" + orderby + ") as __row_number__, " + pagingBuilder +
	            ") SELECT * FROM selectTemp WHERE __row_number__ BETWEEN " +
	            //原因：mysql中limit 10(offset,size) 是从第10开始（不包含10）,；而这里用的BETWEEN是两边都包含，所以改为offset+1
	            firstParam + " AND " + secondParam + " ORDER BY __row_number__";
		return pageSql;
	}
	
	private static String getOrderByPart(String sql) {
		String loweredString = sql.toLowerCase();
		int orderByIndex = loweredString.indexOf("order by");
		if (orderByIndex != -1) {
			return sql.substring(orderByIndex);
		} else {
			return StringUtils.EMPTY;
		}
	}

}