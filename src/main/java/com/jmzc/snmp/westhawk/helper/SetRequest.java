package com.jmzc.snmp.westhawk.helper;

import java.util.Observable;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.co.westhawk.snmp.stack.SetPdu;


public class SetRequest implements Observer
{

	private boolean state = false;
	
	private final static Log log = LogFactory.getLog(SetRequest.class);
	
	
	public void update(Observable obs, Object ov)
	{
		SetPdu pdu = (SetPdu) obs;	
		
		log.debug("Response with code:" + pdu.getErrorStatus() + ";Message:" + pdu.getErrorStatusString());

	    state = true;
	
	}

	
	public boolean state()
	{
		return state;
	}
	
	
	
}
