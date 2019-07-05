package com.xxb.mybatis.test.controller;

import java.util.Date;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xxb.base.MessResult;
import com.xxb.mybatis.MapperRefresh;
import com.xxb.mybatis.test.entity.TestEntity;
import com.xxb.mybatis.test.repository.TestEntityMapper;

@RestController
public class TestController {
	
	@Autowired
	private MapperRefresh mapperRefresh;
	
	@Autowired
	private TestEntityMapper mapper;
	
	@GetMapping(value = "/test")
	public MessResult test() {
		TestEntity t = new TestEntity();
		t.setFid("1");
        t.setFname("4321");
        t.setFdate(new Date());
        t.setVersion(0);
//		int result = mapper.save(t);
//        try {
//        	int result = mapper.update(t);
//		} catch (Exception e) {
//			if (e.getCause() instanceof com.xxb.base.OptimisticLockingFailureException) {
//				return new MessResult(false, "OptimisticLockingFailureException");
//			}
//		}
		return new MessResult("ok", mapper.selectAll());
	}
	
	@GetMapping(value = "/xmlrefresh")
	public MessResult xmlrefresh() throws Exception {
		String path = TestEntityMapper.class.getResource("").getFile();
		mapperRefresh.refresh(path);
		return new MessResult("refresh ok");
	}

}
