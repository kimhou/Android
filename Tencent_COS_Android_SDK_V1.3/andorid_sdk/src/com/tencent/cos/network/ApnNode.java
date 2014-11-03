package com.tencent.cos.network;


public class ApnNode {
	
	private static final String TAG = ApnNode.class.getName();
	String apn;
	String name;

	public ApnNode(String apn, String name, String type)
	{
		this.apn = apn;
		this.name = name;
	}

	public ApnNode()
	{

	}

	public String getApn()
	{
		return apn;
	}

	public String getName()
	{
		return name;
	}

	public void setApn(String apn)
	{
		this.apn = apn;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(" apn = " + apn);
		sb.append(" name = " + name);

		return sb.toString();
	}

}
