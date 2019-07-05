package com.xxb.mybatis.test.entity;

import java.util.Date;

import com.xxb.mybatis.VersionLocker;

public class TestEntity {
	
	private String fid;
	
	private Date fdate;
	
	private String fname;
	
	@VersionLocker
	private Integer version;

	public String getFid() {
		return fid;
	}

	public void setFid(String fid) {
		this.fid = fid;
	}

	public Date getFdate() {
		return fdate;
	}

	public void setFdate(Date fdate) {
		this.fdate = fdate;
	}

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
	
}
