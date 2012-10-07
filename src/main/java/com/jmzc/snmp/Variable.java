package com.jmzc.snmp;

public class Variable
{

	public static final String ASN_OCTET_STR = "ASN_OCTET_STR";
	public static final String ASN_INTEGER = "ASN_INTEGER";
	

	
	
	private String oID = null;
	private Object value = null;

	
	
	public Variable(String oid,Object value)
	{
		super();
		this.oID = oid;
		this.value = value;
		
	}
	

	public void setOID(String oID)
	{
		this.oID = oID;
	}



	public void setValue(Object value)
	{
		this.value = value;
	}

	public String getOID()
	{
		return oID;
	}
	public Object getValue()
	{
		return value;
	}
	
	
}
