package com.jmzc.snmp.westhawk.helper;

import java.util.Observable;
import java.util.Observer;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jmzc.snmp.Variable;

import uk.co.westhawk.snmp.stack.AsnObject;
import uk.co.westhawk.snmp.stack.GetPdu;
import uk.co.westhawk.snmp.stack.varbind;


public class GetRequest implements Observer
{

	private List<Variable> l;
	
	private boolean state = false;
	
	private final static Log log = LogFactory.getLog(GetRequest.class);
	
	
	public void update(Observable obs, Object ov)
	{
		GetPdu pdu = (GetPdu) obs;	
		
		log.debug("Response with code:" + pdu.getErrorStatus() + ";Message:" + pdu.getErrorStatusString());
		
	    if (pdu.getErrorStatus() == AsnObject.SNMP_ERR_NOERROR)
	    {
	        try
	        {
	        	
	            varbind[] vars = pdu.getResponseVarbinds();
	            if ( vars != null && vars.length > 0 )
	            {
	            	l = new ArrayList<Variable>();
	            	
		            for (varbind var: vars)
		            {
	
		            	if (var != null && var.getOid() != null && var.getValue() != null )
		            	{
		            		if (Variable.ASN_INTEGER.equals(var.getValue().getRespTypeString()))
		            		{
		            			l.add(new Variable(var.getOid().toString(), Integer.valueOf(var.getValue().toString())));
		            		}
		            		else
		            		{
		            			l.add(new Variable(var.getOid().toString(), var.getValue().toString()));
		            		}

		            	}
	
		            }
		            
		           
	            }  
	        }
	        catch(uk.co.westhawk.snmp.stack.PduException exc)
	        {
	        	log.debug("SNMP getrequest exception"  + exc.getMessage());
	        }
	    }
	    else
	    {
	    	log.debug("Response code error SNMP = " + pdu.getErrorStatusString());

	    }
	    
	    state = true;
	
	}


	public List<Variable> getLVariable()
	{
		return l;
	}

	
	public boolean state()
	{
		return state;
	}
	
	
	
}
