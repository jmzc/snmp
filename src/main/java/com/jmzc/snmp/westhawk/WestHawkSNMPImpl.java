
package com.jmzc.snmp.westhawk;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.westhawk.snmp.stack.*;

import com.jmzc.snmp.SNMP;
import com.jmzc.snmp.Variable;
import com.jmzc.snmp.westhawk.helper.*;

public class WestHawkSNMPImpl implements SNMP
{

	private final String 	SNMP_VERSION_2c = "2c";
	private final String 	SNMP_VERSION_1  = "1";
	private final int 		SMNP_PORT 		=  161;
	private final int 		TIMEOUT	 		=  20;
	
	
	private String 			SNMP_VERSION = SNMP_VERSION_1;
	

	
	private SnmpContextFace context;
	private String host;
	
	private String rw = "private";
	private String ro = "public";
	
	private final static Log log = LogFactory.getLog(WestHawkSNMPImpl.class);
	
	public WestHawkSNMPImpl(String host) 
	{
		this.host = host;
	}
	
	public WestHawkSNMPImpl(String host, String version) 
	{
		this.host = host;
		this.SNMP_VERSION = version;
	}
	
	public void connect(String rw, String ro) throws Exception
	{

		if (connected())
			throw new Exception();
		
		try 
		{
			// If bind address is null, then the system will pick up a valid local address to bind the socket.
			if (SNMP_VERSION.equals(SNMP_VERSION_2c))
			{
				context = new SnmpContextv2c(host, SMNP_PORT);
			}
			else if (SNMP_VERSION.equals(SNMP_VERSION_1))
			{
				context = new SnmpContext(host, SMNP_PORT);
			}
			
			if (rw != null)
				this.rw = rw;
			if (ro != null)
				this.ro = ro;
			
			log.debug("======== Initialized SNMP context on " + this.host + " version:" + SNMP_VERSION + "========");
			
			
			
		}
		catch (Exception e )
		{
			log.error("Error initializating SNMP context:" + e.getMessage());
			throw e;

		}
	}
	
	public void disconnect()
	{
		if (context != null && !context.isDestroyed())
		{
			context.destroy();
		}
		
		log.debug("======== SNMP context destroyed on " + this.host + "========");
	}
	
	
	/***
	 * 	Realiza un get_next_request para obtener una lista valores de una columna
	 * 
	 */
	public List<Map<String,Variable>> getVariableColumnMap(String t, int f)  throws Exception
	{
		if (!connected())
		{
			log.error("SNMP context not initialized");
			throw new Exception();
		}
		
		List<Map<String,Variable>> l = new ArrayList<Map<String,Variable>>();
		
		try
		{
			
			context.setCommunity(this.ro);

			String oid = t + "." + f;
			while(true)
			{
				
				GetNextPdu pdu = new GetNextPdu(context);
				
				
				//varbind class represents the variable bindings to a PDU. A variable consists of a name (an AsnObjectId) and a value (an AsnObject)
				//The varbind is usually passed to the Observers of the PDU when notifying them. 
				

				GetNextRequest getNextRequest = new GetNextRequest();
				pdu.addObserver(getNextRequest);
				
			    // Thread [localhost_161_null_v1_Trans0] 
			    // Thread [localhost_161_null_v1_Receive] 
				pdu.addOid(new varbind(oid));
				
			    pdu.send();

			    // Sleep, or do something else. Your update method will get called automatically when the data has arrived
			    // Both the context and PDU objects spawn their own threads, so there is nothing further to do.

			    for (int i=0; !getNextRequest.state() && i < TIMEOUT; i++)
			    {
			    	Thread.sleep(1000);
			    	if ( i == TIMEOUT - 1)
			    		throw new Exception();
			    }

			   
			    Variable v = getNextRequest.getVariable();
			    
			    if (v == null || v.getOID() == null || v.getValue() == null)
			    {
			    	continue;
			    }
			    else
			    {
			    	Pattern p = Pattern.compile(t + "." + f + "\\.(.+)$");
			    	Matcher m =  p.matcher(v.getOID().toString());
			    	if (m.find())
			    	{
			    		Map<String,Variable> h = new HashMap<String,Variable>();
			    		h.put(m.group(1), v);
			    		l.add(h);
			    		
			    		oid = v.getOID();
			    	}
			    	else
			    	{
			    	
			    		break;
			    	}
			    	
			    }

			}
		   
		    return l;
		    
		}
		catch (Exception e)
		{
			throw e;
		}
		
	}
	
	/***
	 * 	Realiza un get_request para la lista de campos f y la clave o
	 * 
	 */
	public Map<Integer,Variable> getVariableRowMap(String t, String o, int[] f)  throws Exception
	{
		
		Map<Integer,Variable> h = new HashMap<Integer,Variable>();
		
		/*
		if ( f.length > N )
		{
			log.debug("Divide et vinces");
			int[] a = new int[N];
			for (int i=0; i<N ; i++)
			{
				a[i] = f[i];
			}
			
			int[] b = new int[f.length - N];
			for (int i=0; i< f.length - N ; i++)
			{
				b[i] = f[N + i];
			}
			
			h.putAll(this.getVariableRowMap(t, o, a));
			h.putAll(this.getVariableRowMap(t, o, b));
			
			return h;
			
		}
		*/
				
				
		if (!connected())
		{
			log.error("SNMP context not initialized");
			throw new Exception();
		}
	
		
		
		//Packet maximum size (1300)
		try
		{
			String[] a = new String[f.length];
			for (int i=0; i<f.length ; i++)
			{
				a[i] = t + "." + f[i] + "." + o;
			}
			
			List<Variable> l = this.getVariableList(a);
			if (l != null )
			{
				
				Pattern p = Pattern.compile(t.replace(".","\\.") + "\\.(\\d+?)\\." + o.replace(".","\\.") + "$");
				for (Variable v:l)
				{
					Matcher m = p.matcher(v.getOID());
					if (m.find())
					{
						h.put(Integer.valueOf(m.group(1)), v);
						
					}
					else
					{
						log.error("Not match error.OID:" + v.getOID());
					}
					
				}
			}
			else
			{
				// Plan B
				log.error("Plan B");
				return this._getVariableRowMap(t, o, f);
			}

		   return h;

		}
		catch (EncodingException e)
		{
			log.error("Error getting data [" + e.getMessage() + "]");
			log.error("Plan B");
			return this._getVariableRowMap(t, o, f);
			
		}
		catch (Exception e)
		{
			log.error("Error getting data from oid:" + t + ";index:" + o);
			throw e;

			
		}
		
	}

	/***
	 * 	Realiza un get_request campo x campo 
	 * 
	 */
	private Map<Integer,Variable> _getVariableRowMap(String t, String o, int[] f)  throws Exception
	{
		
		if (!connected())
		{
			log.error("SNMP context not initialized");
			throw new Exception();
		}
			
	
		Map<Integer,Variable> h = new HashMap<Integer,Variable>();
		
		
		try
		{

			
			context.setCommunity(this.ro);

			for (int c: f)
			{
				
				GetPdu pdu = new GetPdu(context);				
				
				//varbind class represents the variable bindings to a PDU. A variable consists of a name (an AsnObjectId) and a value (an AsnObject)
				//The varbind is usually passed to the Observers of the PDU when notifying them. 
				

				GetRequest getRequest = new GetRequest();
				pdu.addObserver(getRequest);
				
			    // Thread [localhost_161_null_v1_Trans0] 
			    // Thread [localhost_161_null_v1_Receive] 
				pdu.addOid(new varbind(t + "." + c + "." + o));
			    pdu.send();

			    // Sleep, or do something else. Your update method will get called automatically when the data has arrived
			    // Both the context and PDU objects spawn their own threads, so there is nothing further to do.

			    for (int i=0; !getRequest.state() && i < TIMEOUT; i++)
			    {
			    	Thread.sleep(1000);
			    	
			    	if ( i == TIMEOUT - 1)
			    		throw new Exception();
			    }

			  
			    List<Variable> l = getRequest.getLVariable();
			    if (l!= null && l.size() > 0)
			    {
			    	Variable v = l.get(0);
			    	h.put(c,v);
			    	

			    }
			   
  
			}    
			    	
			    return h;

		}
		catch (Exception e)
		{
			log.error("Error getting data from oid:" + t + ";index:" + o);
			throw e;
		}
		
	}
	
	
	
	public Variable getVariable(String o) throws Exception
	{
		List<Variable> l = this.getVariableList(o);
		if ( l != null && l.size() > 0 )
		{
			return l.get(0);
		}
		else
		{
			return null;
		}
		
	}
	
	public List<Variable> getVariableList(String ... l) throws Exception
	{
		
		if (!connected())
		{
			log.error("SNMP context not initialized");
			throw new Exception();
		}
			
		try
		{
			
			context.setCommunity(this.ro);
			
			
			GetPdu pdu = new GetPdu(context);
			for (String oid: l)				
			{
				
				//varbind class represents the variable bindings to a PDU. A variable consists of a name (an AsnObjectId) and a value (an AsnObject)
				//The varbind is usually passed to the Observers of the PDU when notifying them. 
				
				pdu.addOid(new varbind(oid));
			}
			
			GetRequest getRequest = new GetRequest();
		    pdu.addObserver(getRequest);
		    
		    // Thread [localhost_161_null_v1_Trans0] 
		    // Thread [localhost_161_null_v1_Receive] 
		    pdu.send();

		    // Sleep, or do something else. Your update method will get called automatically when the data has arrived
		    // Both the context and PDU objects spawn their own threads, so there is nothing further to do.

		    for (int i=0; !getRequest.state() && i < TIMEOUT; i++)
		    {
		    	Thread.sleep(1000);
		    	if ( i == TIMEOUT - 1)
		    		throw new Exception();
		    }

		    return getRequest.getLVariable();
		    
		}
		catch (Exception e)
		{
			throw e;
		}
	}
	
	
	
	public void setVariableList(Variable ... l) throws Exception
	{
		
		if (!connected())
			throw new Exception();
			
		try
		{

			
			context.setCommunity(this.rw);
			
			
			SetPdu pdu = new SetPdu(context);
			for (Variable v: l)				
			{
				
				//varbind class represents the variable bindings to a PDU. A variable consists of a name (an AsnObjectId) and a value (an AsnObject)
				//The varbind is usually passed to the Observers of the PDU when notifying them. 
				
				AsnObject o;
				if (v.getValue() instanceof Integer )
				{
					o = new AsnInteger((Integer)v.getValue());
				}
				else if (v.getValue() instanceof String )
				{
					o = new AsnOctets((String)v.getValue());
				}
				else
				{
					throw new Exception();
				}
				
				pdu.addOid(v.getOID(),o);
	
	
			}
			
			
			SetRequest setRequest = new SetRequest();
		    pdu.addObserver(setRequest);
		    
		    
		    // Thread [localhost_161_null_v1_Trans0] 
		    // Thread [localhost_161_null_v1_Receive] 
		    pdu.send();

		    // Sleep, or do something else. Your update method will get called automatically when the data has arrived
		    // Both the context and PDU objects spawn their own threads, so there is nothing further to do.

		    for (int i=0; !setRequest.state() && i < TIMEOUT; i++)
		    {
		    	Thread.sleep(1000);
		    	if ( i == TIMEOUT - 1)
		    		throw new Exception();
		    }
		    

		    
		}
		catch (Exception e)
		{
			throw e;
		}
		
	}
	
	public boolean connected()
	{
		
		return (context != null);
	}

	
	
}
