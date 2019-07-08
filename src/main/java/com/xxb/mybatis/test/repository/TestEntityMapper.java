package com.xxb.mybatis.test.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.xxb.mybatis.test.entity.TestEntity;

@Mapper
public interface TestEntityMapper {
	
	int save(TestEntity entity);
	
	int update(TestEntity entity);
	
	List<TestEntity> selectAll();
	
	Page<TestEntity> queryPagedEntityBy(Pageable pageable);

}
