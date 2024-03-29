package edu.uiowa.slis.SPARQLTagLib.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.jsp.jstl.sql.Result;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResultImplementation implements Result {
	static Logger logger = LogManager.getLogger(ResultImplementation.class);
    ResultSet rs = null;
    boolean rawMode = false;

    private List<SortedMap<String, Object>> rowMap = new ArrayList<SortedMap<String, Object>>();
    private List<Object[]> rowByIndex = new ArrayList<Object[]>();
    String[] columnNames = null;
    int columnCount = 0;
    int rowCount = 0;
    
    int blankNodeCount = 0;
    Hashtable<String,String> blankNodeHash = new Hashtable<String,String> ();

    public ResultImplementation(ResultSet rs, boolean rawMode) {
	this.rs = rs;
	this.rawMode = rawMode;
	columnNames = getColumnNames();
	columnCount = columnNames.length;

	while (rs.hasNext()) {
	    QuerySolution solution = rs.next();
	    
	    logger.debug("solution: " + solution);

	    Object[] columns = new Object[columnCount];
	    SortedMap<String, Object> columnMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);

	    // JDBC uses 1 as the lowest index!
	    for (int i = 0; i < columnCount; i++) {
		RDFNode node = solution.get(columnNames[i]);
		logger.debug("var " + columnNames[i] + ": " + node + "\tliteral: " + node.isLiteral() + "\tanon: " + node.isAnon());
		Object value = node == null ? null
				: rawMode ? ( node.isLiteral() ? "\""+node.asLiteral().getString().replace("\"", "\\\"").replace("\n", "\\n")+"\""+(node.asLiteral().getLanguage() == null || node.asLiteral().getLanguage().trim().length() == 0 ? "" : "@"+node.asLiteral().getLanguage().toLowerCase())
							       : (node.isAnon() ? getBlankNodeLabel(node.toString())
								       		: "<"+node+">" ))
					  : ( node.isLiteral() ? node.asLiteral().getString() : node.toString() );
		logger.trace("row: " + rowCount + "\tcolumn: " + columnNames[i] + " : " + value);
		columns[i] = value;
		columnMap.put(columnNames[i], value);
	    }
	    rowMap.add(columnMap);
	    rowByIndex.add(columns);

	    rowCount++;
	}
    }
    
    private String getBlankNodeLabel(String nodeLabel) {
	String blankLabel = blankNodeHash.get(nodeLabel);
	
	if (blankLabel == null) {
	    blankLabel = "_:b" + blankNodeCount++;
	    blankNodeHash.put(nodeLabel, blankLabel);
	}
	
	return blankLabel;
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

    @SuppressWarnings("rawtypes")
    @Override
    public SortedMap[] getRows() {
	if (rowMap == null)
	    return null;

	// should just be able to return SortedMap[] object
	return (SortedMap[]) rowMap.toArray(new SortedMap[0]);
    }

    @Override
    public Object[][] getRowsByIndex() {
	if (rowByIndex == null)
	    return null;

	// should just be able to return Object[][] object
	return (Object[][]) rowByIndex.toArray(new Object[0][0]);
    }

    @Override
    public boolean isLimitedByMaxRows() {
	return false;
    }

}
