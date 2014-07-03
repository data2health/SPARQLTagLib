package edu.uiowa.slis.SPARQLTagLib.util;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.jsp.jstl.sql.Result;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class ResultImplementation implements Result {
	static Logger logger = Logger.getLogger(ResultImplementation.class);
	ResultSet rs = null;
	
    private List<SortedMap<String,Object>> rowMap = new ArrayList<SortedMap<String,Object>>();
    private List<Object[]> rowByIndex = new ArrayList<Object[]>();
	String[] columnNames = null;
	int columnCount = 0;
	int rowCount = 0;
	
	public ResultImplementation(ResultSet rs) {
		this.rs = rs;
		columnNames = getColumnNames();
		columnCount = columnNames.length;
		
		while (rs.hasNext()) {
			QuerySolution solution = rs.next();

	           Object[] columns = new Object[columnCount];
	            SortedMap<String,Object> columnMap = 
	                new TreeMap<String,Object>(String.CASE_INSENSITIVE_ORDER);

	            // JDBC uses 1 as the lowest index!
	            for (int i = 0; i < columnCount; i++) {
	            	RDFNode node = solution.get(columnNames[i]);
	                Object value =  node.isLiteral() ? node.asLiteral().getString() : node.toString();
	                logger.trace("row: " + rowCount + "\tcolumn: " + columnNames[i] + " : " + value);
	                columns[i] = value;
	                columnMap.put(columnNames[i], value);
	            }
	            rowMap.add(columnMap);
	            rowByIndex.add(columns);

			
			rowCount++;
		}
	}

	@Override
	public String[] getColumnNames() {
		if (columnNames != null)
			return columnNames;
		
		String[] result = new String[rs.getResultVars().size()];
		int fence = 0;
		for (String var : rs.getResultVars()) {
			logger.debug("var: " + var);
			result[fence++] = var;
		}
		return result;
	}

	@Override
	public int getRowCount() {
		return rowCount;
	}

	@Override
	public SortedMap[] getRows() {
		if (rowMap == null)
			return null;

        //should just be able to return SortedMap[] object
		return (SortedMap[]) rowMap.toArray(new SortedMap[0]);
	}

	@Override
	public Object[][] getRowsByIndex() {
		if (rowByIndex == null)
			return null;

        //should just be able to return Object[][] object
		return (Object[][]) rowByIndex.toArray(new Object[0][0]);
	}

	@Override
	public boolean isLimitedByMaxRows() {
		return false;
	}

}