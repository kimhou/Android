package com.tencent.cos.bean;

public class CosFile {
	
	private String md5;
	private String name;
	private long crttime;
	private long uptime;
	private long fszie;
	private int type;
	private boolean finishFlag;

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getCrttime() {
		return crttime;
	}

	public void setCrttime(long crttime) {
		this.crttime = crttime;
	}

	public long getUptime() {
		return uptime;
	}

	public void setUptime(long uptime) {
		this.uptime = uptime;
	}

	public long getFszie() {
		return fszie;
	}

	public void setFszie(long fszie) {
		this.fszie = fszie;
	}


	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isFinishFlag() {
		return finishFlag;
	}

	public void setFinishFlag(boolean finishFlag) {
		this.finishFlag = finishFlag;
	}



}
