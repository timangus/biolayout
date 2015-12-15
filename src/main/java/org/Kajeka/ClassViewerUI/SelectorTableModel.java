/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.Kajeka.ClassViewerUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import org.Kajeka.Analysis.AnnotationTypeManagerBG;
import org.Kajeka.CoreUI.LayoutClasses;

/**
 *
 * @author seb
 */
public class SelectorTableModel extends AbstractTableModel {
    public String[] columnNames;
    public Object[][] data;
    
    public SelectorTableModel(Object[][] indata, String[] incolumnNames){
        data = indata;
        columnNames = incolumnNames;
        
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public int getColumnCount() {
        if (data.length != 0)
            return data[0].length;
        else
            return 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        
        return data[rowIndex][columnIndex];
    }
    
    public boolean isCellEditable(int row, int col) {
        if (col < 1) {
            return false;
        } else {
            return true;
        }
    }
    
    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }
    
    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
    
    @Override
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }
    
    public HashSet<String> getSelectedClasses(){
        HashSet<String> selectedClassess = new HashSet<>();
        for (int i = 0; i < this.getRowCount(); i++) {
            if ((Boolean)this.getValueAt(i, 1)){
                selectedClassess.add((String)getValueAt(i, 0));
            }
        }
        return selectedClassess;
    }

    public void refreshContent(ArrayList<LayoutClasses> classList){
        data = new Object[classList.size()][2];
        int i = 0;
        for(LayoutClasses obj : classList)
        {
            data[i][0] = obj.getClassSetName();
            for (int j = 0; j < 1; j++) {
                data[i][1+j] = new Boolean(false);
            }
          i++;
        }
    }
}
