package com.jmzc.snmp;

import java.util.List;
import java.util.Map;

public interface SNMP
{

	public void connect(String rw, String ro) throws Exception;

	public List<Variable> getVariableList(String ... l) throws Exception;
	
	public Variable getVariable(String o) throws Exception;
	
	public void setVariableList(Variable ... l) throws Exception;
	
	public List<Map<String,Variable>> getVariableColumnMap(String t, int f)  throws Exception;

	public Map<Integer,Variable> getVariableRowMap(String t, String o, int[] f)  throws Exception;


	public void disconnect();
	
	
}
