package com.jmzc.snmp.westhawk.helper;

import java.util.Observable;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.jmzc.snmp.Variable;

import uk.co.westhawk.snmp.stack.AsnObject;
import uk.co.westhawk.snmp.stack.GetNextPdu;
import uk.co.westhawk.snmp.stack.varbind;


public class GetNextRequest implements Observer
{

	private Variable v;
	
	private boolean state = false;
	
	private final static Log log = LogFactory.getLog(GetNextRequest.class);
	
	public void update(Observable obs, Object ov)
	{
		GetNextPdu pdu = (GetNextPdu) obs;
		log.debug("Response with code:" + pdu.getErrorStatus() + ";Message:" + pdu.getErrorStatusString());
	    if (pdu.getErrorStatus() == AsnObject.SNMP_ERR_NOERROR)
	    {
	        try
	        {
	            varbind[] vars = pdu.getResponseVarbinds();

	            if ( vars != null && vars.length > 0 && vars[0] != null)
	            {	     
	            	if (Variable.ASN_INTEGER.equals(vars[0].getValue().getRespTypeString()))
            		{
	            		v =  new Variable(vars[0].getOid().toString(),Integer.valueOf(vars[0].getValue().toString()));	
            		}
            		else
            		{
            			v = new Variable(vars[0].getOid().toString(),vars[0].getValue().toString());	
            		}
	            }  
	        
	        }
	        catch(uk.co.westhawk.snmp.stack.PduException exc)
	        {
	        	log.debug("SNMP getrequest exception"  + exc.getMessage());
	            
	            state = true;
	        }

	    }
	    else
	    {
	    	log.debug("Response code error SNMP = " + pdu.getErrorStatusString());

	    }
	    
	    state = true;
	
	}


	public Variable getVariable()
	{
		return v;
	}

	
	public boolean state()
	{
		return state;
	}
	
	
	
}
