package com.xxb.mybatis;

import java.lang.reflect.Field;
import java.sql.Connection;
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

import com.xxb.base.OptimisticLockingFailureException;

@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
	@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class VersionInterceptor implements Interceptor {

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		String interceptMethod = invocation.getMethod().getName();
		if(!"prepare".equals(interceptMethod)) {
			return invocation.proceed();
		}
		StatementHandler handler = (StatementHandler) PluginUtil.processTarget(invocation.getTarget());
		MetaObject metaObject = SystemMetaObject.forObject(handler);
		MappedStatement ms = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
		if(ms.getSqlCommandType() != SqlCommandType.UPDATE || !StringUtils.endsWith(ms.getId(), "update")) {
			return invocation.proceed();
		}
		Object paraObj = handler.getBoundSql().getParameterObject();
		Field verField = PluginUtil.findVersionField(paraObj);
		if (verField == null) {
			return invocation.proceed();
		}
		Object originalVersion = metaObject.getValue("delegate.boundSql.parameterObject."+verField.getName());
		if (originalVersion == null || Integer.parseInt(originalVersion.toString()) < 0){
			throw new OptimisticLockingFailureException("optimistic lock failed. value of version field[" + verField.getName() + "] is not illegal");
		}
		
		return invocation.proceed();
	}

	

	@Override
	public Object plugin(Object target) {
		if (Executor.class.isAssignableFrom(target.getClass())) {
			MyExecutor executor = new MyExecutor((Executor) target);
			return Plugin.wrap(executor, this);
		}
		if (target instanceof StatementHandler) {
			return Plugin.wrap(target, this);
		}
		 return target;
	}

	@Override
	public void setProperties(Properties properties) {
		// TODO Auto-generated method stub
		
	}

}
