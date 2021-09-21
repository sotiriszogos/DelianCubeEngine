/**
 * 
 */
package cubemanager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import cubemanager.cubebase.BasicStoredCube;
import cubemanager.cubebase.CubeQuery;
import cubemanager.cubebase.Dimension;
import cubemanager.cubebase.Hierarchy;
import cubemanager.cubebase.Level;
import cubemanager.cubebase.LinearHierarchy;
import cubemanager.cubebase.Measure;
import exctractionmethod.ExtractionMethod;

/**
 * @author pvassil
 *
 */
public class CubeQueryTranslatorToSQL implements ICubeQueryTranslator {

	public CubeQueryTranslatorToSQL() {
		//System.out.println("Created CubeQueryTranslatorToSQL");
		;
	}
	
	/* (non-Javadoc)
	 * @see cubemanager.ICubeQueryTranslator#produceExtractionMethod(cubemanager.cubebase.CubeQuery)
	 */
	@Override
	public String produceExtractionMethod(CubeQuery cubeQuery) {
		//TODO: God method, super long.
		//CONSIDER: refactoring to smaller parts, but do it without errors
		//ATTN: ordering of the steps is sensitive, do NOT change unless you know what you are doing.
		List<Measure> measure;
		String aggregateFunction;
		ArrayList<String[]> gammaExpressions;
		ArrayList<String[]> sigmaExpressions;
		BasicStoredCube referCube;
		ExtractionMethod extractionMethod;  
		
		measure = cubeQuery.getMsr();
		aggregateFunction = cubeQuery.getAggregateFunction();
		gammaExpressions = cubeQuery.getGammaExpressions();
		sigmaExpressions = cubeQuery.getSigmaExpressions();
		referCube = cubeQuery.getReferCube();
		extractionMethod = cubeQuery.getExtractionMethod(); //must have been "new" before though
		
		if(measure.get(0).getAttribute() !=null ) 
			extractionMethod.addReturnedFields(aggregateFunction,measure.get(0).getAttribute().getName());
		else
			extractionMethod.addReturnedFields(aggregateFunction,"");
		
		HashSet<String> FromTables=new HashSet<String>();

		/*Create WhereClausse */
		for(String[] sigmaExpr: sigmaExpressions){
			for(int i = 0;i < referCube.getListDimension().size(); i++) {
				Dimension dimension = referCube.getListDimension().get(i);
				String[] tmp=sigmaExpr[0].split("\\.");
				if(dimension.hasSameName(tmp[0].trim())){
					/* FOR JOIN WITH Basic CUBE*/
					String toaddJoin[]=new String[3];
					toaddJoin[0] = referCube.getDimensionRefField().get(i);
					toaddJoin[1] = "=";
					toaddJoin[2] = dimension.getTableName()+"."+((LinearHierarchy)dimension.getHier().get(0)).getLevels().get(0).getAttributeName(0);
					extractionMethod.addFilter(toaddJoin);

					FromTables.add(dimension.getTableName());

					/* Add the Sigma Expression */
					ArrayList<Hierarchy> current_hierachy=dimension.getHier();
					
					List<String> taS = new ArrayList<>();
					String zero = dimension.getTableName()+".";
					
					for(int k=0;k<current_hierachy.size();k++){//for each hierarchy of dimension
						List<Level> current_lvls=current_hierachy.get(k).getLevels();
						for(int l=0;l<current_lvls.size();l++){							
							if(current_lvls.get(l).getName().equals(tmp[1].trim())){
								zero+=current_lvls.get(l).getAttributeName(0);
							}
						}
					}
					taS.add( zero );
					taS.add( sigmaExpr[1] );
					taS.add( sigmaExpr[2] );
					
					if( sigmaExpr.length>3 ) {
						taS.add( "OR" );
						for(int j=1; j<sigmaExpr.length/3; j++) {
							int k=0;
							if(sigmaExpr[3*j] != null) {
								k++;
								for(int o = 0;o < referCube.getListDimension().size(); o++) {
									Dimension dim = referCube.getListDimension().get(o);
									String[] t=sigmaExpr[3*j].split("\\.");
									if(dim.hasSameName(t[0].trim())){
										String z = dim.getTableName()+".";
										for(int m=0;m<current_hierachy.size();m++){//for each hierarchy of dimension
											List<Level> current_lvls=current_hierachy.get(m).getLevels();
											for(int l=0;l<current_lvls.size();l++){							
												if(current_lvls.get(l).getName().equals(t[1].trim())){
													z+=current_lvls.get(l).getAttributeName(0);
												}
											}
										}
										taS.add(z);
									}
								}
								taS.add(sigmaExpr[3*j+1]);	
								taS.add(sigmaExpr[3*j+2]);	

								if(k%3==0) {
									taS.add( "OR" );
								}
							}
						}
					}
					
					String[] array = taS.toArray(new String[0]);
					extractionMethod.addFilter(array);						 
				}
			}
		} //end for of WhereClasue

		/*Create From clause */
		String[] tbl_tmp = new String[1];
		tbl_tmp[0] = "";
		if(referCube != null) 
			tbl_tmp[0] = referCube.FactTable().getTableName();
		extractionMethod.addSourceCube(tbl_tmp);

		for(int i=0;i<FromTables.size();i++){
			String[] toAdd=new String[1];
			toAdd[0]=(String) FromTables.toArray()[i];
			extractionMethod.addSourceCube(toAdd);
		}
		
		/*Create groupClausse*/
		for(String[] gammaExpr: gammaExpressions){
			if(gammaExpr[0].length()==0) {
				String[] toadd=new String[1];
				toadd[0]=gammaExpr[1];
				extractionMethod.addGroupers(toadd);
			}
			else{
				for(int i=0;i<referCube.getListDimension().size();i++){
					Dimension dimension= referCube.getListDimension().get(i);
					if(dimension.hasSameName(gammaExpr[0])){
						String[] toadd=new String[1];
						toadd[0]=dimension.getTableName()+".";
						ArrayList<Hierarchy> current_hierachy=dimension.getHier();
						for(int k=0;k<current_hierachy.size();k++){//for each hierarchy of dimension
							List<Level> current_lvls=current_hierachy.get(k).getLevels();
							for(int l=0;l<current_lvls.size();l++){
								if(current_lvls.get(l).getName().equals(gammaExpr[1])){
									/* FOR JOIN WITH Basic CUBE*/
									String toaddJoin[]=new String[3];
									toaddJoin[0]=referCube.getDimensionRefField().get(i);
									toaddJoin[1]="=";
									toaddJoin[2]=dimension.getTableName()+"."+((LinearHierarchy)dimension.getHier().get(0)).getLevels().get(0).getAttributeName(0);
									extractionMethod.addFilter(toaddJoin);
									String[] toAddfrom=new String[1];
									toAddfrom[0]=dimension.getTableName();
									if(FromTables.contains(dimension.getTableName())==false) 
										extractionMethod.addSourceCube(toAddfrom);

									toadd[0]+=current_lvls.get(l).getAttributeName(0);
								}
							}
						}

						extractionMethod.addGroupers(toadd);
					}
				}
			}
		}
		
		return extractionMethod.toString();
	}//end method produceExtractionMethod(CubeQuery)


}
