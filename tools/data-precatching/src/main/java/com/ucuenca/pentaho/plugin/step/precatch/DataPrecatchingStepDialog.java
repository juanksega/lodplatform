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

package com.ucuenca.pentaho.plugin.step.precatch;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;

import com.ucuenca.misctools.DatabaseLoader;

/**
 * This class is part of the demo step plug-in implementation.
 * It demonstrates the basics of developing a plug-in step for PDI. 
 * 
 * The demo step adds a new string field to the row stream and sets its
 * value to "Hello World!". The user may select the name of the new field.
 *   
 * This class is the implementation of StepDialogInterface.
 * Classes implementing this interface need to:
 * 
 * - build and open a SWT dialog displaying the step's settings (stored in the step's meta object)
 * - write back any changes the user makes to the step's meta object
 * - report whether the user changed any settings when confirming the dialog 
 * 
 */
public class DataPrecatchingStepDialog extends BaseStepDialog implements StepDialogInterface {

	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	private static Class<?> PKG = DataPrecatchingStepMeta.class; // for i18n purposes

	// this is the object the stores the step's settings
	// the dialog reads the settings from it when opening
	// the dialog writes the settings to it when confirmed 
	private DataPrecatchingStepMeta meta;

	// text field holding the name of the field to add to the row stream
	private Text wHelloFieldName, wTableNameField; 
	
	private String dataStepName;

	/**
	 * The constructor should simply invoke super() and save the incoming meta
	 * object to a local variable, so it can conveniently read and write settings
	 * from/to it.
	 * 
	 * @param parent 	the SWT shell to open the dialog in
	 * @param in		the meta object holding the step's settings
	 * @param transMeta	transformation description
	 * @param sname		the step name
	 */
	public DataPrecatchingStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		meta = (DataPrecatchingStepMeta) in;

	}

	/**
	 * This method is called by Spoon when the user opens the settings dialog of the step.
	 * It should open the dialog and return only once the dialog has been closed by the user.
	 * 
	 * If the user confirms the dialog, the meta object (passed in the constructor) must
	 * be updated to reflect the new step settings. The changed flag of the meta object must 
	 * reflect whether the step configuration was changed by the dialog.
	 * 
	 * If the user cancels the dialog, the meta object must not be updated, and its changed flag
	 * must remain unaltered.
	 * 
	 * The open() method must return the name of the step after the user has confirmed the dialog,
	 * or null if the user cancelled the dialog.
	 */
	public String open() {

		// store some convenient SWT variables 
		Shell parent = getParent();
		Display display = parent.getDisplay();

		// SWT code for preparing the dialog
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, meta);
		
		// Save the value of the changed flag on the meta object. If the user cancels
		// the dialog, it will be restored to this saved value.
		// The "changed" variable is inherited from BaseStepDialog
		changed = meta.hasChanged();
		
		// The ModifyListener used on all controls. It will update the meta object to 
		// indicate that changes are being made.
		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				meta.setChanged();
			}
		};
		
		// ------------------------------------------------------- //
		// SWT code for building the actual settings dialog        //
		// ------------------------------------------------------- //
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "DataPrecatching.Shell.Title")); 

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName")); 
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
		
		// output field value
		Label wlValName = new Label(shell, SWT.RIGHT);
		wlValName.setText(BaseMessages.getString(PKG, "DataPrecatching.FieldName.Label")); 
		props.setLook(wlValName);
		FormData fdlValName = new FormData();
		fdlValName.left = new FormAttachment(0, 0);
		fdlValName.right = new FormAttachment(middle, -margin);
		fdlValName.top = new FormAttachment(wStepname, margin);
		wlValName.setLayoutData(fdlValName);

		wHelloFieldName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wHelloFieldName);
		wHelloFieldName.setEditable(false);
		wHelloFieldName.setText(DatabaseLoader.SQL_URI_CONNECTION + DatabaseLoader.SQL_SCHEMA);
		wHelloFieldName.addModifyListener(lsMod);
		FormData fdValName = new FormData();
		fdValName.left = new FormAttachment(middle, 0);
		fdValName.right = new FormAttachment(100, 0);
		fdValName.top = new FormAttachment(wStepname, margin);
		wHelloFieldName.setLayoutData(fdValName);
		
		// Database table name
		Label wlTableName = new Label(shell, SWT.RIGHT);
		wlTableName.setText(BaseMessages.getString(PKG, "DataPrecatching.TableName.Label")); 
		props.setLook(wlTableName);
		FormData fdlTableName = new FormData();
		fdlTableName.left = new FormAttachment(0, 0);
		fdlTableName.right = new FormAttachment(middle, -margin);
		fdlTableName.top = new FormAttachment(wHelloFieldName, margin);
		wlTableName.setLayoutData(fdlTableName);

		wTableNameField = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wTableNameField);
		wTableNameField.addModifyListener(lsMod);
		FormData fdTableName = new FormData();
		fdTableName.left = new FormAttachment(middle, 0);
		fdTableName.right = new FormAttachment(100, 0);
		fdTableName.top = new FormAttachment(wHelloFieldName, margin);
		wTableNameField.setLayoutData(fdTableName);
		      
		// OK and cancel buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); 
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); 

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wTableNameField);

		// Add listeners for cancel and OK
		lsCancel = new Listener() {
			public void handleEvent(Event e) {cancel();}
		};
		lsOK = new Listener() {
			public void handleEvent(Event e) {ok();}
		};

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);

		// default listener (for hitting "enter")
		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {ok();}
		};
		wStepname.addSelectionListener(lsDef);
		wHelloFieldName.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window and cancel the dialog properly
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {cancel();}
		});
		
		// Set/Restore the dialog size based on last position on screen
		// The setSize() method is inherited from BaseStepDialog
		setSize();

		// populate the dialog with the values from the meta object
		populateDialog();
		
		// restore the changed flag to original value, as the modify listeners fire during dialog population 
		meta.setChanged(changed);

		// open dialog and enter event loop 
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		// at this point the dialog has closed, so either ok() or cancel() have been executed
		// The "stepname" variable is inherited from BaseStepDialog
		return stepname;
	}
	
	/**
	 * This helper method puts the step configuration stored in the meta object
	 * and puts it into the dialog controls.
	 */
	private void populateDialog() {
		wStepname.selectAll();
		String[] dbResult = new String[2]; 
		try {
			dbResult =  this.getDBTableNameFromPreviousSteps(stepMeta);
                        if (dbResult.length==2 && dbResult[0]==null && dbResult[1]==null){
                            dbResult[0]="DEFAULTDATA";
                            dbResult[1]="DEFAULTSTEP";
                            changed=true;
                        }
		}catch(KettleException e) {
			
		}
		wTableNameField.setText( Const.nullToEmpty(dbResult[0]) );
		wTableNameField.setEnabled( dbResult[0] == null );
		this.dataStepName = dbResult[1];	
	}

	/**
	 * Called when the user cancels the dialog.  
	 */
	private void cancel() {
		// The "stepname" variable will be the return value for the open() method. 
		// Setting to null to indicate that dialog was cancelled.
		stepname = null;
		// Restoring original "changed" flag on the met aobject
		meta.setChanged(changed);
		// close the SWT dialog window
		dispose();
	}
	
	/**
	 * Called when the user confirms the dialog
	 */
	private void ok() {
		// The "stepname" variable will be the return value for the open() method. 
		// Setting to step name from the dialog control
		stepname = wStepname.getText();
		if(changed) {
			meta.setDbTable( wTableNameField.getText() );
		}
		meta.setChanged(changed);
		//meta.setDataStepName(this.dataStepName);
		meta.setDataStepName(stepname);
		dispose();
	}
	
	/**
	 * Lookup for the DB table name across the hop grid 
	 * @param stepMeta base step meta
	 * @return table name
	 * @throws KettleException
	 */
	private String[] getDBTableNameFromPreviousSteps(StepMeta stepMeta) throws KettleException{
		String [] dbResult = new String[2];
		for(StepMeta step: stepMeta.getParentTransMeta().findPreviousSteps(stepMeta)) { 
			dbResult[0] = this.lookupGetterMethod(step.getName(), step.getStepMetaInterface().getStepData());
			if(dbResult[0] != null) {
				dbResult[1] = step.getName();
				logBasic("DBTABLE FIELD FOUND ON " + dbResult[1] + " STEP DATA CLASS. VALUE ==> " + dbResult[0]);
				break;
			}
			if(step.getParentTransMeta().findPreviousSteps(step).size() > 0) {
				dbResult = this.getDBTableNameFromPreviousSteps(step);
				if(dbResult[0] != null) break;
			}
		}
		return dbResult;
	}
	
	/**
	 * Method in charge to lookup and execute the step getter method for DB table name
	 * @param stepName Name of step involved
	 * @param stepData step data interface
	 * @return table name
	 */
	private String lookupGetterMethod(String stepName, StepDataInterface stepData) {
		String value = null;
		try {
			value = (String)stepData.getClass().getField("DBTABLE").get(stepData);
		}catch(NoSuchFieldException ne) {
			logDebug("NO 'DBTABLE' FIELD FOUND ON " + stepName + " STEP DATA CLASS");
		}catch(SecurityException se) {
			logDebug("NO 'DBTABLE' PUBLIC FIELD FOUND ON " + stepName + " STEP DATA CLASS");
		}catch(IllegalAccessException ae) {
			logDebug(ae.getMessage());
		}
		return value;
	}
}
