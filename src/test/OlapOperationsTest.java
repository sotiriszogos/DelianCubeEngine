package test;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import cubemanager.cubebase.CubeQuery;
import cubemanager.cubebase.QueryHistoryManager;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;

import mainengine.ResultFileMetadata;
import mainengine.SessionQueryProcessorEngine;

public class OlapOperationsTest {

	private static SessionQueryProcessorEngine queryEngine;
	private static QueryHistoryManager historyMng;

	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		queryEngine = new SessionQueryProcessorEngine(); 
		
		queryEngine.initializeConnection("pkdd99", "CinecubesUser",
				"Cinecubes", "pkdd99","loan");
		
		historyMng = queryEngine.getQueryHistoryMng();
		
		queryEngine.answerCubeQueryFromString("CubeName:loan\n" + 
				"Name: LoanQuery11_S1_CG-Prtl\n" + 
				"AggrFunc:Avg\n" + 
				"Measure:amount\n" + 
				"Gamma:account_dim.lvl2,date_dim.lvl2\n" + 
				"Sigma:account_dim.lvl2='north Moravia'");
		queryEngine.answerCubeQueryFromString("CubeName:loan\n" + 
				"Name: LoanQuery31_S3_CG-Prtl\n" + 
				"AggrFunc:Sum\n" + 
				"Measure:amount\n" + 
				"Gamma:account_dim.lvl2,date_dim.lvl3\n" + 
				"Sigma:account_dim.lvl2='west Bohemia',status_dim.lvl0='Contract Finished/No Problems', date_dim.lvl3 = '1996'");

	}
	
	@Test
	public void testRollUpHappyPath() throws IOException {
		CubeQuery query = historyMng.getQueryByName("LoanQuery11_S1_CG-Prtl");
		queryEngine.rollUp(query, "account_dim", "lvl3");
		CubeQuery rollUpQuery = historyMng.getLastQuery();
		String[] gamma0 = rollUpQuery.getGammaExpressions().get(0);
		Assert.assertEquals(gamma0[0], "account_dim");
		Assert.assertEquals(gamma0[1], "lvl3");	
		File fileProduced = new File("OutputFiles/Roll-up_LoanQuery11_S1_CG-Prtl.tab");
		File fileReference = new File("src/test/OutputFiles/pkdd99/Reference_Roll-up_LoanQuery11_S1_CG-Prtl.tab");
		Assert.assertTrue(FileUtils.contentEquals(fileProduced, fileReference));
	}
	
	@Test
	public void testRollUpDimensionDoesntExist() throws RemoteException {
		CubeQuery query = historyMng.getQueryByName("LoanQuery11_S1_CG-Prtl");
		ResultFileMetadata res = queryEngine.rollUp(query, "accnt_dim", "lvl3");
		Assert.assertEquals(res.getErrorCheckingFile(), "The dimension does not exist");
		
	}
	
	@Test
	public void testRollUpLevelDoesntExist() throws RemoteException {
		CubeQuery query = historyMng.getQueryByName("LoanQuery11_S1_CG-Prtl");
		ResultFileMetadata res = queryEngine.rollUp(query, "account_dim", "lvl5");
		Assert.assertEquals(res.getErrorCheckingFile(), "The level does not exist");
	}
	
	@Test
	public void testRollUpLevelIsLowerThanBefore() throws RemoteException {
		CubeQuery query = historyMng.getQueryByName("LoanQuery11_S1_CG-Prtl");
		ResultFileMetadata res = queryEngine.rollUp(query, "account_dim", "lvl1");
		Assert.assertEquals(res.getErrorCheckingFile(), "You can't roll up to a lower or equal level");
	}
	
	@Test
	public void testDrillDownHappyPath() throws IOException {
		CubeQuery query = historyMng.getQueryByName("LoanQuery11_S1_CG-Prtl");
		queryEngine.drillDown(query, "account_dim", "lvl1");
		CubeQuery drillDownQuery = historyMng.getLastQuery();
		String[] gamma0 = drillDownQuery.getGammaExpressions().get(0);
		Assert.assertEquals(gamma0[0], "account_dim");
		Assert.assertEquals(gamma0[1], "lvl1");	
		File fileProduced = new File("OutputFiles/Drill-down_LoanQuery11_S1_CG-Prtl.tab");
		File fileReference = new File("src/test/OutputFiles/pkdd99/Reference_Drill-down_LoanQuery11_S1_CG-Prtl.tab");
		Assert.assertTrue(FileUtils.contentEquals(fileProduced, fileReference));
	}
	
	@Test
	public void testDrillDownDimensionDoesntExist() throws RemoteException {
		CubeQuery query = historyMng.getQueryByName("LoanQuery11_S1_CG-Prtl");
		ResultFileMetadata res = queryEngine.drillDown(query, "accnt_dim", "lvl3");
		Assert.assertEquals(res.getErrorCheckingFile(), "The dimension does not exist");
	}
	
	@Test
	public void testDrillDownLevelDoesntExist() throws RemoteException {
		CubeQuery query = historyMng.getQueryByName("LoanQuery11_S1_CG-Prtl");
		ResultFileMetadata res = queryEngine.drillDown(query, "account_dim", "lvl5");
		Assert.assertEquals(res.getErrorCheckingFile(), "The level does not exist");
	}
	
	@Test
	public void testDrillDownLevelIsLowerThanBefore() throws RemoteException {
		CubeQuery query = historyMng.getQueryByName("LoanQuery11_S1_CG-Prtl");
		ResultFileMetadata res = queryEngine.drillDown(query, "account_dim", "lvl3");
		Assert.assertEquals(res.getErrorCheckingFile(), "You can't drill down to an upper or equal level");
	}
	
	@Test
	public void testSliceHappyPath() throws IOException {
		CubeQuery query = historyMng.getQueryByName("LoanQuery11_S1_CG-Prtl");
		queryEngine.slice(query, "account_dim", "lvl1", "=", "'Sumperk'");
		CubeQuery sliceQuery = historyMng.getLastQuery();
		String[] sigma1 = sliceQuery.getSigmaExpressions().get(1);
		Assert.assertEquals(sigma1[0], "account_dim.lvl1");
		Assert.assertEquals(sigma1[1], "=");	
		Assert.assertEquals(sigma1[2], "'Sumperk'");
		File fileProduced = new File("OutputFiles/Slice_LoanQuery11_S1_CG-Prtl.tab");
		File fileReference = new File("src/test/OutputFiles/pkdd99/Reference_Slice_LoanQuery11_S1_CG-Prtl.tab");
		Assert.assertTrue(FileUtils.contentEquals(fileProduced, fileReference));
	}
	
	@Test
	public void testSliceDimensionDoesntExist() throws RemoteException {
		CubeQuery query = historyMng.getQueryByName("LoanQuery11_S1_CG-Prtl");
		ResultFileMetadata res = queryEngine.slice(query, "accnt_dim", "lvl1", "=", "'Sumperk'");
		Assert.assertEquals(res.getErrorCheckingFile(), "The dimension does not exist");
	}
	
	@Test
	public void testSliceLevelDoesntExist() throws RemoteException {
		CubeQuery query = historyMng.getQueryByName("LoanQuery11_S1_CG-Prtl");
		ResultFileMetadata res = queryEngine.slice(query, "account_dim", "lvl5", "=", "'Sumperk'");
		Assert.assertEquals(res.getErrorCheckingFile(), "The level does not exist");
	}
	
	@Test
	public void testDiceHappyPath() throws IOException {
		CubeQuery query = historyMng.getQueryByName("LoanQuery11_S1_CG-Prtl");
		List<String> dimensions = Collections.unmodifiableList(Arrays.asList("account_dim", "date_dim"));
		List<String> levels = Collections.unmodifiableList(Arrays.asList("lvl1", "lvl3"));
		List<String> operators = Collections.unmodifiableList(Arrays.asList("=", "="));
		List<String> values = Collections.unmodifiableList(Arrays.asList("'Sumperk'", "1998"));
		queryEngine.dice(query, dimensions, levels, operators, values);
		CubeQuery diceQuery = historyMng.getLastQuery();
		String[] sigma1 = diceQuery.getSigmaExpressions().get(1);
		Assert.assertEquals(sigma1[0], "account_dim.lvl1");
		Assert.assertEquals(sigma1[1], "=");	
		Assert.assertEquals(sigma1[2], "'Sumperk'");
		Assert.assertEquals(sigma1[3], "date_dim.lvl3");
		Assert.assertEquals(sigma1[4], "=");	
		Assert.assertEquals(sigma1[5], "1998");
		File fileProduced = new File("OutputFiles/Dice_LoanQuery11_S1_CG-Prtl.tab");
		File fileReference = new File("src/test/OutputFiles/pkdd99/Reference_Dice_LoanQuery11_S1_CG-Prtl.tab");
		Assert.assertTrue(FileUtils.contentEquals(fileProduced, fileReference));
	}
	
	@Test
	public void testDiceDimensionDoesntExist() throws RemoteException {
		CubeQuery query = historyMng.getQueryByName("LoanQuery11_S1_CG-Prtl");
		List<String> dimensions = Collections.unmodifiableList(Arrays.asList("account_dim", "de_dim"));
		List<String> levels = Collections.unmodifiableList(Arrays.asList("lvl1", "lvl3"));
		List<String> operators = Collections.unmodifiableList(Arrays.asList("=", "="));
		List<String> values = Collections.unmodifiableList(Arrays.asList("'Sumperk'", "1998"));
		ResultFileMetadata res = queryEngine.dice(query, dimensions, levels, operators, values);
		Assert.assertEquals(res.getErrorCheckingFile(), "The dimension does not exist");
	}
	
	@Test
	public void testDiceLevelDoesntExist() throws RemoteException {
		CubeQuery query = historyMng.getQueryByName("LoanQuery11_S1_CG-Prtl");
		List<String> dimensions = Collections.unmodifiableList(Arrays.asList("account_dim", "date_dim"));
		List<String> levels = Collections.unmodifiableList(Arrays.asList("lvl1", "lvl5"));
		List<String> operators = Collections.unmodifiableList(Arrays.asList("=", "="));
		List<String> values = Collections.unmodifiableList(Arrays.asList("'Sumperk'", "1998"));
		ResultFileMetadata res = queryEngine.dice(query, dimensions, levels, operators, values);
		Assert.assertEquals(res.getErrorCheckingFile(), "The level does not exist");
	}
}
