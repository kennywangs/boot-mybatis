package com.xxb.test;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.xxb.mybatis.test.entity.TestEntity;
import com.xxb.mybatis.test.repository.TestEntityMapper;


@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class TestMapperTest {

	@Autowired
	private TestEntityMapper mapper;
	
	@Test
    public void save() {
		TestEntity t = new TestEntity();
		t.setFid("1");
        t.setFname("zzzz");
        t.setFdate(new Date());
        // 返回插入的记录数 ，期望是1条 如果实际不是一条则抛出异常
        Assert.assertEquals(1,mapper.save(t));
    }

    @Test
    public void update() {
    	TestEntity t = new TestEntity();
        t.setFid("1");
        t.setFname("z1");
        // 返回更新的记录数 ，期望是1条 如果实际不是一条则抛出异常
        Assert.assertEquals(1,mapper.update(t));
    }
    
    @Test
    public void findAll() {
        Assert.assertNotNull(mapper.selectAll());
    }
}
