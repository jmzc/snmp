package com.jmzc.client;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jmzc.snmp.exception.ConnectionException;
import com.jmzc.snmp.exception.OperationException;
import com.jmzc.snmp.SNMP;
import com.jmzc.snmp.Variable;
import com.jmzc.snmp.westhawk.*;


public class Client
{

	protected String host;
	protected String name;
	
	protected SNMP snmp;
	
	protected String rW;
	protected String rO;
	
	private final static Log log = LogFactory.getLog(Client.class);
	
	protected Client(String host)
	{
		this.host = host;
	}
	
	
	public String getHost()
	{
		return this.host;
	}
	
	

	public String getRW()
	{
		return rW;
	}



	public String getRO()
	{
		return rO;
	}


	


	public void connect(String rw, String ro) throws ConnectionException
	{
		if (connected())
			return;
		
		try
		{
			snmp = new WestHawkSNMPImpl(host);
			snmp.connect(rw,ro);
		}
		catch(Exception e)
		{
			throw new ConnectionException(e);
		}

	}
	
	
	public void disconnect()
	{
		
		if(connected())
			snmp.disconnect();
		
		 this.snmp = null;

	}
	

	public boolean connected()
	{
		return ( snmp != null );
	}
	
	public String getName() throws ConnectionException,OperationException
	{
		if (this.name == null)
		{
			if ( !connected())
			{
				log.error("No connected to " + this.host);
				throw new ConnectionException();
			}
			
			try
			{
				Variable v = this.snmp.getVariable("1.3.6.1.2.1.1.5.0");
				
				if (v != null && v.getValue() != null)
				{
					this.name = (String)v.getValue();
				}
				else
				{
					throw new Exception("Host name is NULL");
				}
			}
			catch (Exception e )
			{
				log.error("Operation failed when getting host name [" + this.host + "][" +  e.getMessage() + "]");
				throw new OperationException(e);

			}
			
			
		}
		
		return this.name;
		
		

	}


	
	

	
	
	
}
