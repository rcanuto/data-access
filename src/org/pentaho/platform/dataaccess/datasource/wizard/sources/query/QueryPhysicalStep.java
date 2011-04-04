/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created April 21, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.sources.query;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.pentaho.platform.dataaccess.datasource.DatasourceType;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.AbstractWizardStep;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.ConnectionController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.WizardConnectionController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.WizardRelationalDatasourceController;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IWizardModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.csv.CsvDatasource;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.stereotype.Bindable;

@SuppressWarnings("unchecked")
public class QueryPhysicalStep extends AbstractWizardStep {

  public static final int DEFAULT_RELATIONAL_TABLE_ROW_COUNT = 5;
  private DatasourceModel datasourceModel;
  private IXulAsyncDatasourceService datasourceService;
  XulTextbox datasourceNameTextBox = null;
  XulButton okButton = null;
  XulButton cancelButton = null;
  private XulTree csvDataTable = null;
  private WizardConnectionController connectionController;
  private ConnectionController databaseConnectionController;
  private IXulAsyncConnectionService connectionService;
  private boolean isFinishable = false;
  private IConnection connection;

  public QueryPhysicalStep(DatasourceModel datasourceModel, IWizardDatasource parentDatasource, boolean isFinishable) {
	  super(parentDatasource);
	  this.datasourceModel = datasourceModel;
	  this.datasourceService = new DatasourceServiceGwtImpl();
	  this.connectionService = new ConnectionServiceGwtImpl();
	  this.isFinishable = isFinishable;
  }
  
  public QueryPhysicalStep(DatasourceModel datasourceModel, IWizardDatasource parentDatasource) {
	  this(datasourceModel, parentDatasource, true);
  }
  
  @Override
  public void activating() {
    //To change body of implemented methods use File | Settings | File Templates.
	XulVbox vbox = (XulVbox) document.getElementById("queryBox");
	vbox.setVisible(true);
  }
  
  @Override
  public XulComponent getUIComponent() {
    return document.getElementById("queryDeckPanel");
  }

  @Bindable
  public void init(IWizardModel wizardModel) throws XulException {

    datasourceNameTextBox = (XulTextbox) document.getElementById("datasourceName"); //$NON-NLS-1$
    
    connectionController = new WizardConnectionController(document);
    connectionController.setDatasourceModel(datasourceModel);
    getXulDomContainer().addEventHandler(connectionController);
    connectionController.init();

    databaseConnectionController = new ConnectionController();
    getXulDomContainer().addEventHandler(databaseConnectionController);
    databaseConnectionController.setDatasourceModel(datasourceModel);
    databaseConnectionController.setService(connectionService);
    databaseConnectionController.init();
    databaseConnectionController.reloadConnections();

    WizardRelationalDatasourceController relationalDatasourceController = new WizardRelationalDatasourceController();

    relationalDatasourceController.setService(datasourceService);
    getXulDomContainer().addEventHandler(relationalDatasourceController);
    relationalDatasourceController.init(datasourceModel);


    initialize();

    datasourceModel.clearModel();
    super.init(wizardModel);
  }

  public void initialize() {
  }

  public IXulAsyncConnectionService getConnectionService() {
    return connectionService;
  }

  public void setConnectionService(IXulAsyncConnectionService connectionService) {
    this.connectionService = connectionService;
  }

  public String getName() {
    return "datasourceController"; //$NON-NLS-1$
  }

  @Bindable
  public void selectCsv() {
    csvDataTable.update();
    datasourceModel.setDatasourceType(DatasourceType.CSV);
  }

  
  @Bindable
  public void selectSql() {
    datasourceModel.setDatasourceType(DatasourceType.SQL);
  }

  public IXulAsyncDatasourceService getDatasourceService() {
    return datasourceService;
  }

  public void setDatasourceService(IXulAsyncDatasourceService datasourceService) {
    this.datasourceService = datasourceService;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.wizard.steps.IWizardStep#getStepName()
   */
  public String getStepName() {
    return MessageHandler.getString("wizardStepName.SOURCE"); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.wizard.steps.IWizardStep#setBindings()
   */
  public void setBindings() {

    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(wizardModel, "selectedDatasource", datasourceModel, "datasourceType", new BindingConvertor<IWizardDatasource, DatasourceType>(){
      @Override
      public DatasourceType sourceToTarget(IWizardDatasource iWizardDatasource) {
        if(iWizardDatasource instanceof QueryDatasource){
          return DatasourceType.SQL;
        } else if( iWizardDatasource instanceof CsvDatasource){
          return DatasourceType.CSV;
        }
        return DatasourceType.NONE;
      }

      @Override
      public IWizardDatasource targetToSource(DatasourceType datasourceType) {
        return null;
      }
    });
    
    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(datasourceModel, "datasourceName", datasourceNameTextBox, "value"); //$NON-NLS-1$ //$NON-NLS-2$

    // create a binding from the headerRows property of the CsvFileInfo to the first-row-is-header check box

    
    datasourceModel.addPropertyChangeListener("datasourceName", new QueryAndDatasourceNamePropertyChangeListener()); //$NON-NLS-1$
    datasourceModel.addPropertyChangeListener("query", new QueryAndDatasourceNamePropertyChangeListener()); //$NON-NLS-1$

    bf.setBindingType(Binding.Type.ONE_WAY);

    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(datasourceModel, "datasourceName", datasourceModel.getModelInfo(), "stageTableName");

    bf.createBinding(wizardModel, "editing", datasourceNameTextBox, "disabled");
    bf.createBinding(datasourceModel, "selectedRelationalConnection", this, "connection");


  }

  public void setFocus() {
    datasourceNameTextBox.setFocus();
    setStepImageVisible(true);
  }

  public void reloadConnections() {
    databaseConnectionController.reloadConnections();
  }

  public void selectConnectionByName(String name) {
    connection = databaseConnectionController.getDatasourceModel().getGuiStateModel().getConnectionByName(name);
    databaseConnectionController.getDatasourceModel().setSelectedRelationalConnection(connection);
  }

  private class QueryAndDatasourceNamePropertyChangeListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
      String newValue = (String) evt.getNewValue();
      if(newValue == null || newValue.trim().length() == 0) {
        parentDatasource.setFinishable(false);
      } else {
    	if(isFinishable) {
    		parentDatasource.setFinishable(true);	
    	} else {
    		parentDatasource.setFinishable(false);
    		setValid(true);
    	}
      }
    }
  }
  
  
  public boolean stepDeactivatingForward(){
	return super.stepDeactivatingForward();
  }

  public WizardConnectionController getConnectionController() {
    return connectionController;
  }

  public void setConnectionController(WizardConnectionController connectionController) {
    this.connectionController = connectionController;
  }

  @Bindable
  public IConnection getConnection() {
    return connection;
  }

  @Bindable
  public void setConnection(IConnection connection) {
    Object prevVal = this.connection == null ? new Object() : null;
    this.connection = connection;
    firePropertyChange("connection", prevVal, connection);
  }
}