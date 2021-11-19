// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.data.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.mitre.schemastore.model.DataType;
import org.mitre.schemastore.model.Function;
import org.mitre.schemastore.model.FunctionImp;

/**
 * Handles function data calls in the database
 * @author CWOLF
 */
public class FunctionDataCalls extends AbstractDataCalls
{	
	/** Retrieves the list of functions in the repository */
	private ArrayList<Function> retrieveFunctions(Integer functionID)
	{
		// Generate the SQL command
		String command = "SELECT id,name,description,expression,category,output_type FROM functions";
		if(functionID!=null) command += " WHERE id="+functionID;

		// Retrieve the list of functions
		ArrayList<Function> functions = new ArrayList<Function>();
		try {
			Statement stmt = connection.getStatement();
			Statement stmt2 = connection.getStatement();
			ResultSet rs = stmt.executeQuery(command);
			while(rs.next())
			{				
				// Retrieve the function info
				Integer id = rs.getInt("id");
				String name = rs.getString("name");
				String description = rs.getString("description");
				String expression = rs.getString("expression");
				String category = rs.getString("category");
				Integer outputType = rs.getInt("output_type");
				
				// Get the list of function input types
				ArrayList<Integer> inputTypes = new ArrayList<Integer>();
				ResultSet rs2 = stmt2.executeQuery("SELECT input_type FROM function_input WHERE function_id="+id+" ORDER BY input_loc");
				while(rs2.next()) inputTypes.add(rs2.getInt("input_type"));
				Integer inputTypeArray[] = inputTypes.toArray(new Integer[0]);
				
				// Store the function
				functions.add(new Function(id,name,description,expression,category,inputTypeArray,outputType));
			}
			stmt.close();
			stmt2.close();
		} catch(SQLException e) { System.out.println("(E) FunctionDataCalls:retrieveFunctions: "+e.getMessage()); }
		return functions;
	}
	
	/** Constructs the data call class */
	FunctionDataCalls(DatabaseConnection connection) { super(connection); }

	/** Retrieves the function validation number */
	public Integer getFunctionValidationNumber()
	{
		Integer validationNumber = 0;
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT sum(id) AS validation_number FROM functions");
			if(rs.next())
				validationNumber = rs.getInt("validation_number");
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) FunctionDataCalls:getFunctionValidationNumber: "+e.getMessage()); }
		return validationNumber;
	}
	
	//-----------------------------------
	// Handles Functions in the Database
	//-----------------------------------
	
	/** Retrieves the list of data types in the repository */
	public ArrayList<DataType> getDataTypes()
	{
		ArrayList<DataType> dataTypes = new ArrayList<DataType>();
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT id,type,description FROM data_type");
			while(rs.next())
			{				
				// Retrieve the function info
				Integer id = rs.getInt("id");
				String type = rs.getString("type");
				String description = rs.getString("description");
				
				// Store the function
				dataTypes.add(new DataType(id,type,description));
			}
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) FunctionDataCalls:getDataTypes: "+e.getMessage()); }
		return dataTypes;		
	}
	
	/** Retrieves the list of functions in the repository */
	public ArrayList<Function> getFunctions()
		{ return retrieveFunctions(null); }
	
	/** Retrieves the specified function in the repository */
	public Function getFunction(Integer functionID)
	{
		ArrayList<Function> functions = retrieveFunctions(functionID);
		return functions.size()==0 ? null : functions.get(0);
	}
	
	/** Adds the specified function */
	public Integer addFunction(Function function)
	{
		Integer functionID = 0;
		try {
			functionID = getUniversalIDs(1);
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("INSERT INTO functions(id,name,description,expression,category,output_type) VALUES("+functionID+",'"+scrub(function.getName(),50)+"','"+scrub(function.getDescription(),4096)+"','"+scrub(function.getExpression(),200)+"','"+scrub(function.getCategory(),100)+"',"+(function.getOutputType())+")");
			stmt.close();
			connection.commit();
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			functionID = 0;
			System.out.println("(E) FunctionDataCalls:addFunction: "+e.getMessage());
		}
		return functionID;
	}
	
	/** Returns the list of deletable functions */
	public ArrayList<Integer> getDeletableFunctions()
	{
		ArrayList<Integer> functions = new ArrayList<Integer>();
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT id FROM function " +
											 "EXCEPT SELECT function_id AS id FROM mapping_cell");
			while(rs.next())
				functions.add(rs.getInt("id"));
			stmt.close();
		}
		catch(SQLException e) { System.out.println("(E) FunctionDataCalls:getDeletableFunctions: "+e.getMessage()); }
		return functions;
	}

	/** Deletes the specified function */
	public boolean deleteFunction(int functionID)
	{
		boolean success = false;
		try {
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("DELETE FROM function_implementation WHERE function_id="+functionID);
			stmt.executeUpdate("DELETE FROM function_input WHERE function_id="+functionID);
			stmt.executeUpdate("DELETE FROM functions WHERE id="+functionID);
			stmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) FunctionDataCalls:deleteFunction: "+e.getMessage());
		}
		return success;
	}

	//--------------------------------------------------
	// Handles Function Implementations in the Database
	//--------------------------------------------------
	
	/** Retrieves the list of function implementations in the repository for the specified function */
	public ArrayList<FunctionImp> getFunctionImps(Integer functionID)
	{
		ArrayList<FunctionImp> functionImps = new ArrayList<FunctionImp>();
		try {
			Statement stmt = connection.getStatement();
			ResultSet rs = stmt.executeQuery("SELECT function_id,language,dialect,implementation FROM function_implementation"+(functionID==null?"":" WHERE function_id="+functionID));
			while(rs.next())
			{				
				// Retrieve the function info
				Integer id = rs.getInt("function_id");
				String language = rs.getString("language");
				String dialect = rs.getString("dialect");
				String implementation = rs.getString("implementation");
				
				// Store the function
				functionImps.add(new FunctionImp(id,language,dialect,implementation));
			}
			stmt.close();
		} catch(SQLException e) { System.out.println("(E) FunctionDataCalls:getFunctionImps: "+e.getMessage()); }
		return functionImps;
	}
	
	/** Retrieves the list of function implementations in the repository */
	public ArrayList<FunctionImp> getFunctionImps()
		{ return getFunctionImps(null); }
	
	/** Sets the specified function implementation */
	public boolean setFunctionImp(FunctionImp functionImp)
	{
		// Gather up the function info
		Integer functionID = functionImp.getFunctionID();
		String language = scrub(functionImp.getLanguage(),50);
		String dialect = scrub(functionImp.getDialect(),50);
		String implementation = scrub(functionImp.getImplementation(),500);
		
		// First, attempt to update function implementation
		try {
			Statement stmt = connection.getStatement();
			stmt.executeUpdate("UPDATE function_implementation SET implementation='"+implementation+"' WHERE function_id="+functionID+" AND language='"+language+"'"+(dialect==null ? "" : " AND dialog='"+dialect+"'"));
			stmt.close();
			connection.commit();
		}
		
		// If update failed, try to add function implementation
		catch(SQLException e)
		{
			try {
				Statement stmt = connection.getStatement();
				stmt.executeUpdate("INSERT INTO function_implementation (function_id,language,dialect,implementation) VALUES('"+functionID+"','"+language+"',"+(dialect==null?"NULL":"'"+dialect+"'")+",'"+implementation+"')");
				stmt.close();
				connection.commit();
			}

			// Indicate that the function implementation failed to be set
			catch(SQLException e2)
			{
				try { connection.rollback(); } catch(SQLException e3) {}
				System.out.println("(E) FunctionDataCalls:setFunctionImp: "+e2.getMessage());
				return false;
			}
		}
		return true;
	}
	
	/** Deletes the specified function implementation */
	public boolean deleteFunctionImp(FunctionImp functionImp)
	{
		// Generate the deletion command
		String command = "DELETE FROM function_implementation WHERE function_id="+functionImp.getFunctionID()+" AND language="+functionImp.getLanguage();
		if(functionImp.getDialect()!=null) command += " AND dialect="+functionImp.getDialect();

		// Delete the function implementation
		boolean success = false;
		try {
			Statement stmt = connection.getStatement();
			stmt.executeUpdate(command);
			stmt.close();
			connection.commit();
			success = true;
		}
		catch(SQLException e)
		{
			try { connection.rollback(); } catch(SQLException e2) {}
			System.out.println("(E) FunctionDataCalls:deleteFunction: "+e.getMessage());
		}
		return success;
	}
}