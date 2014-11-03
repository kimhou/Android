package com.tencent.cos.bean;

public class CosBucket {

	private String name;
	private String referer;
	private int acl;
	private int type;
	private long buildtime;
	private long updatetime;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getReferer() {
		return referer;
	}

	public void setReferer(String referer) {
		this.referer = referer;
	}

	public int getAcl() {
		return acl;
	}

	public void setAcl(int acl) {
		this.acl = acl;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getBuildtime() {
		return buildtime;
	}

	public void setBuildtime(long buildtime) {
		this.buildtime = buildtime;
	}

	public long getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(long updatetime) {
		this.updatetime = updatetime;
	}

}
