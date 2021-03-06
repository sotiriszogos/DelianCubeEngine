package mainengine.nlq;

import java.util.ArrayList;
import java.util.HashMap;



/**
 * This class is used for the error checking of natural language queries.
 * 
 * @author DimosGkitsakis 
 *
 */

public class NLQValidator  {
	
	
	public HashMap<String, String> incomingQueries = new HashMap<String, String>();
	public HashMap<String, String> errorDetails = new HashMap<String, String>();

	private ArrayList<String> cubeNames;
	private ArrayList<String> aggrFunctions;
	private ArrayList<String> measures;

	
	public NLQValidator(ArrayList<String> cubeNames,  ArrayList<String> aggrFunctions, ArrayList<String> measures) {
		this.cubeNames = cubeNames;
		this.aggrFunctions = aggrFunctions;
		this.measures = measures;
	}
	
	
	/**
	 * This method is responsible for the pre-processing of the natural language query.
	 * It takes as an input the natural language query in its cube query form, analyzes it and produces a set of errors if any found.
	 * 
	 * @param cubeQuery A QueryForm object with the cube query in its fields.
	 * @param gammaErrorHandling An ArrayList of Strings with information about the gamma component of the cube query.
	 * @param sigmaErrorHandling An ArrayList of Strings with information about the sigma component of the cube query.
	 * @return A NLQValidationResults object which contains information about the error checking process, which are used for
	 * the production of error messages to be presented to the end-user in the front-end.
	 * @author DimosGkitsakis
	 */
	public NLQValidationResults prepareCubeQuery(CubeQueryForm cubeQuery, ArrayList<String> gammaErrorHandling, ArrayList<String> sigmaErrorHandling) {
		
		//1. Produce cube query string
		String cubeQueryString = cubeQuery.toString();
		String queryName = cubeQuery.queryName;
		
		
		//2. Save it to the hashmap
		saveCubeQuery(queryName, cubeQueryString);
		
		//3. Check for errors		
		fillErrorHashMap();
		String errorCode = errorChecking(cubeQuery, gammaErrorHandling, sigmaErrorHandling);

		if (errorCode.equals("No Error Found")) {
			return new NLQValidationResults(cubeQuery.queryName, false, null, null);
		}else {
			return new NLQValidationResults(cubeQuery.queryName, true, errorCode, errorDetails.get(errorCode));
		}
		
	}
	
	
	private void saveCubeQuery(String queryName, String cubeQuery) {
		incomingQueries.put(queryName, cubeQuery);
	}
	
	private void fillErrorHashMap() {
		errorDetails.put("Cube Name Error", "Cube Name Error found. The given cube name was not recognized. "
				+ "Please check again. \n "
				+ "The query should look like: \n'Describe the *aggregate function* of *cube name* *measure name* per *grouper* for *selection* as *query name*' " );
		
		errorDetails.put("Aggregate Function Error", "Aggregate Function Error found. The given aggregate function was not recognized. "
				+ "Please check again. \n "
				+ "The query should look like: \n'Describe the *aggregate function* of *cube name* *measure name* per *grouper* for *selection* as *query name*' " );
		
		errorDetails.put("Measure Error", "Measure Name Error found. The given measure name was not recognized. "
				+ "Please check again. \n "
				+ "The query should look like: \n'Describe the *aggregate function* of *cube name* *measure name* per *grouper* for *selection* as *query name*' " );

		//gamma errors
		errorDetails.put("Gamma Field Error: Level Name Not Found", "Gamma Field: One or more of the dimension level names given were not found. "
				+ "Please check again. \n "
				+ "The query should look like: \n'Describe the *aggregate function* of *cube name* *measure name* per *grouper* for *selection* as *query name*' "
				+ "and the groupers should be split by 'and' ");
		
		errorDetails.put("Gamma Field Error: Dimension Name Not Found", "Gamma Field: One or more of the dimension names given were not found. "
				+ "Please check again. \n "
				+ "The query should look like: \n'Describe the *aggregate function* of *cube name* *measure name* per *grouper* for *selection* as *query name*' "
				+ "and the groupers should be split by 'and' ");
		
		errorDetails.put("Gamma Field Error: Many Same Level Names", "Gamma Field: More than 1 dimensions with the same level were found. "
				+ "Please check again. \n "
				+ "The query should look like: \n'Describe the *aggregate function* of *cube name* *measure name* per *grouper* for *selection* as *query name*' "
				+ "and the groupers should be split by 'and' ");

		//sigma errors
		errorDetails.put("Sigma Field Error: More levels than values", "Sigma Field: More levels than the equivalent given values were found. "
				+ "Please check again. \n "
				+ "The query should look like: \n'Describe the *aggregate function* of *cube name* *measure name* per *grouper* for *selection* as *query name*' "
				+ "and the sigma selections should be split by 'and' ");

		errorDetails.put("Sigma Field Error: More values than levels", "Sigma Field: More values than the equivalent given levels were found. "
				+ "Please check again. \n "
				+ "The query should look like: \n'Describe the *aggregate function* of *cube name* *measure name* per *grouper* for *selection* as *query name*' "
				+ "and the sigma selections should be split by 'and' ");

		errorDetails.put("Sigma Field Error: Level Name Not Found", "Sigma Field: One or more of the dimension level names given were not found. "
				+ "Please check again. \n "
				+ "The query should look like: \n'Describe the *aggregate function* of *cube name* *measure name* per *grouper* for *selection* as *query name*' "
				+ "and the sigma selections should be split by 'and' ");

		errorDetails.put("Sigma Field Error: Dimension Name Not Found", "Sigma Field: One or more of the dimension names given were not found. "
				+ "Please check again. \n "
				+ "The query should look like: \n'Describe the *aggregate function* of *cube name* *measure name* per *grouper* for *selection* as *query name*' "
				+ "and the sigma selections should be split by 'and' ");

		errorDetails.put("Sigma Field Error: Many Same Level Names", "Sigma Field: More than 1 dimensions with the same level were found. "
				+ "Please check again. \n "
				+ "The query should look like: \n'Describe the *aggregate function* of *cube name* *measure name* per *grouper* for *selection* as *query name*' "
				+ "and the sigma selections should be split by 'and' ");
	}
	
	private String errorChecking(CubeQueryForm query, ArrayList<String> gammaErrorHandling, ArrayList<String> sigmaErrorHandling) {
		String cubeName = query.cubeName.split(":")[1] + "_cube";
		String aggrFunc = query.aggregateFunction.split(":")[1];
		String measure = query.cubeName.split(":")[1] + "." + query.measure.split(":")[1];
		

		if(!(cubeNames.contains(cubeName))) {
			return "Cube Name Error";
		}else if(!(aggrFunctions.contains(aggrFunc))) {
			return "Aggregate Function Error";
		}else if (!(measures.contains(measure))){
			return "Measure Error";
		}else if (!(gammaErrorHandling.get(0).equals("Null"))) {
			return gammaErrorHandling.get(0);
		}else if (!(sigmaErrorHandling.get(0).equals("Null"))) {
			return sigmaErrorHandling.get(0);
		}
		
		return "No Error Found";
	}
	
	public String getQuery(String hashKey) {
		return incomingQueries.get(hashKey);
	}
	
	public ArrayList<String> getCubeNames(){
		return cubeNames;
	}
}
