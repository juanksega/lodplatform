/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.ucuenca.pentaho.plugin.step.ontologymapping;

import java.sql.ResultSet;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;

import com.ucuenca.misctools.DatabaseLoader;

/**
 * This class is part of the demo step plug-in implementation.
 * It demonstrates the basics of developing a plug-in step for PDI. 
 * 
 * The demo step adds a new string field to the row stream and sets its
 * value to "Hello World!". The user may select the name of the new field.
 *   
 * This class is the implementation of StepDataInterface.
 *   
 * Implementing classes inherit from BaseStepData, which implements the entire
 * interface completely. 
 * 
 * In addition classes implementing this interface usually keep track of
 * per-thread resources during step execution. Typical examples are:
 * result sets, temporary data, caching indexes, etc.
 *   
 * The implementation for the demo step stores the output row structure in 
 * the data class. 
 *   
 */
public class OntoMapData extends BaseStepData implements StepDataInterface {
	
	public Object[] ontologies, data;
	public RowMetaInterface ontologiesMeta, dataMeta;
    public RowSet ontologiesRowSet;
    public RowSet dataRowSet;

	public RowMetaInterface outputRowMeta;
	
	  	//must be included for DataBase Data Loading
		public static final String CLASSIFICATIONTABLE = "CLASSMAPPING";
		public static final String ANNOTATIONTABLE = "ANNOTATIONMAPPING";
		public static final String RELATIONTABLE = "RELATIONMAPPING";
		//End Database Data Loading attributes
		
		private String transName;
		private String stepName;
	
    public OntoMapData()
	{    	
		super();
	}

    public String getTransName() {
		return transName;
	}

	public void setTransName(String transName) {
		this.transName = transName;
	}

	public String getStepName() {
		return stepName;
	}

	public void setStepName(String stepName) {
		this.stepName = stepName;
	}

	/**
	 * Saves table data on DB Schema
	 * @param table Dialog TableView
	 * @param tableName DB table name
	 * @throws Exception
	 */
	public void saveTable(TableView table, String tableName)throws Exception {
    	ColumnInfo[] columns = table.getColumns();
    	Map<String, String> tableFields = new LinkedHashMap<String, String>();
    	tableFields.put("TRANSID", "VARCHAR(50)");
		tableFields.put("STEPID", "VARCHAR(50)");
    	for(ColumnInfo column:columns) tableFields.put(column.getName().toUpperCase().replaceAll(" ", "_"), "VARCHAR(100)");
    	tableFields.put("PRIMARY KEY", "(TRANSID, STEPID, "+ columns[0].getName().toUpperCase().replaceAll(" ", "_") + ")");
    	DatabaseLoader.createTable(tableName, tableFields);
    	Object[] pk = new Object[]{this.getTransName(), this.getStepName()};
    	DatabaseLoader.executeUpdate("DELETE FROM " + tableName + " WHERE TRANSID = ? AND STEPID = ?", pk);
    	for(int i=0;i<table.getItemCount();i++) {
    		Object[] tableValues = table.getItem(i);
    		Object[] values = pk;
    		values = ArrayUtils.addAll(values, tableValues);
    		if(((String)values[2]).length() > 0) {
	    		try {
	    			int totalFields = values.length;
	    			String sqlInsertion = "INSERT INTO " + tableName + " VALUES(";
	    			while(--totalFields >= 1) sqlInsertion += "?,";
	    			sqlInsertion += "?)";
	    			DatabaseLoader.executeUpdate(sqlInsertion, values);
	    		}catch(Exception e) {
	    			throw new KettleException("ERROR EXECUTING SQL INSERT: "+ e.getMessage());
	    		}
    		}
    	}
    }
	
	/**
	 * Query DB Table data and load into the TableView
	 * @param tableView Dialog TableView
	 * @param tableName DB table name
	 * @throws Exception
	 */
	public void queryTable(TableView tableView, String tableName)throws Exception {
    	ColumnInfo[] columns = tableView.getColumns();
    	List<String> tableFields = new ArrayList<String>();
    	for(ColumnInfo column:columns) tableFields.add(column.getName().toUpperCase().replaceAll(" ", "_"));
    	Object[] pk = new Object[]{this.getTransName(), this.getStepName()};
    	ResultSet rs = DatabaseLoader.executeQuery("SELECT " + 
    			tableFields.toString().substring(1, tableFields.toString().length()-1) + 
    			" FROM " + tableName + " WHERE TRANSID = ? AND STEPID = ?", pk);
    	tableView.removeAll();
    	int row = 0;
    	while(rs.next()) {
    		if(row == 0) {
	    		TableItem item = tableView.table.getItem( row );
	    		int count = 1;
	    		while(count <= tableFields.size()) {
	    			item.setText(count, rs.getString(count));
	    			count ++;
	    		}
    		} else {
    			List<String> values = new ArrayList<String>();
        		int count = 1;
        		while(count <= tableFields.size()) {
        			values.add(rs.getString(count));
        			count ++;
        		}
        		tableView.add(values.toArray(new String[values.size()]));
    		}
    		row++;
    	}
    	tableView.setRowNums();
        tableView.optWidth( true );
    }
	
	/**
	 * Delete DB table Data
	 * @param tableName DB table name
	 * @return Boolean.TRUE if the Deletion process succeeded
	 * @throws Exception
	 */
	public Boolean deleteTableRecords(String tableName) throws Exception {
    	Object[] pk = new Object[]{this.getTransName(), this.getStepName()};
    	return DatabaseLoader.executeUpdate("DELETE FROM " + tableName 
    			+ " WHERE TRANSID = ? AND STEPID = ?", pk);
	}
    
}
	
