/*
*    DelianCubeEngine. A simple cube query engine.
*    Copyright (C) 2018  Panos Vassiliadis
*
*    This program is free software: you can redistribute it and/or modify
*    it under the terms of the GNU Affero General Public License as published
*    by the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*
*/


package client.naiveJavaClient;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.io.Serializable;

import client.ClientRMITransferer;
import cubemanager.cubebase.QueryHistoryManager;
//import mainengine.Foo;
import mainengine.IMainEngine;
import mainengine.ResultFileMetadata;
import mainengine.Session;
/**
 * A simple client that 
 * (a) locates an RMI server in the HOST at PORT
 * (b) issues queries against a specific cubebase and a specific cube
 * 
 * @author pvassil
 *
 */
public class NaiveJavaClient implements Serializable{
	private static final long serialVersionUID = 4390482518182625971L;

	// Host or IP of Server
	private static final String HOST = "localhost";
	private static final int PORT = 2020;
	private static Registry registry;

	public static void main(String[] args) throws Exception {
		// Search the registry in the specific Host, Port.
		registry = LocateRegistry.getRegistry(HOST, PORT);
		// LookUp for MainEngine on the registry
		IMainEngine service = (IMainEngine) registry.lookup(IMainEngine.class
				.getSimpleName());
		if(service == null) {
			System.out.println("Unable to commence server, exiting");
			System.exit(-100);
		}
		// The first arg is audio; the second is for Word
		//service.optionsChoice(false, false);
		
		
		// Cube ADULT and queries
		/*service.initializeConnection("adult_no_dublic", "CinecubesUser",
				"Cinecubes", "adult", "adult");
		File f = new File("InputFiles/cubeQueries.ini");
		service.answerCubeQueriesFromFile(f);/**/
				
		// Cube LOAN and queries
		service.initializeConnection("pkdd99", "CinecubesUser",
				"Cinecubes", "pkdd99", "loan");
		System.out.println("Completed connection initialization");


		
		//CleanUp client Cache
		File resultFolder = new File("ClientCache");
		deleteAllFilesOfFolder(resultFolder);
		
		
		//Run queries
		//File f2 = new File("InputFiles/cubeQueriesloan.ini");
		File f2 = new File("InputFiles/pkdd99/Queries/loanQueries.txt");
		//File f2 = new File("InputFiles/try.txt");
		ArrayList<String> fileLocations = service.answerCubeQueriesFromFile(f2);
		
		// Cube ORDERS and queries
		/*service.initializeConnection("pkdd99", "CinecubesUser",
				 "Cinecubes", "pkdd99", "orders");
		File f4 = new File("InputFiles/cubeQueriesorder.ini");
		service.answerCubeQueriesFromFile(f4);/**/
		

		
		for(String s: fileLocations) {
			System.out.println("Find the next result at " + s);
			File remote = new File(s);
			String sep = "\\" + File.separator;	//Java idioms. You need to add the "\\" before!
			String[] array = s.split(sep);
			String localName = "NoName";
			if (array.length > 0)
				localName = array[array.length-1].trim();

			ClientRMITransferer.download(service, remote, new File("ClientCache" + File.separator + localName));
			
		}
		
		/* ******************* now for the models **************/
	
		String queryForModels11 =		"CubeName:loan" + " \n" +
				"Name: CubeQueryLoan11_Prague" + " \n" +
				"AggrFunc:Sum" + " \n" +
				"Measure:amount" + " \n" +
				"Gamma:account_dim.lvl1,date_dim.lvl2" + " \n" +
				"Sigma:account_dim.lvl2='Prague'";
		String queryName11 = "CubeQueryLoan11_Prague";
		
		
		String queryForModels12 =		"CubeName:loan" + " \n" +
				"Name: CubeQueryLoan12_Sum1998" + " \n" +
				"AggrFunc:Sum" + " \n" +
				"Measure:amount" + " \n" +
				"Gamma:account_dim.lvl1,status_dim.lvl1" + " \n" +
				"Sigma:date_dim.lvl2 = '1998-01'";
		String queryName12 = "CubeQueryLoan12_Sum1998";
		
		
		String queryFired = queryForModels12;
		String queryName = queryName12;
		String [] modelsToGenerate = {"Rank","Outlier", "KMeansApache", "KPIMedianBased"};
		//String [] modelsToGenerate = {"Outlier"};
		//String [] modelsToGenerate = {};
		ResultFileMetadata resMetadata = service.answerCubeQueryFromStringWithModels(queryFired, modelsToGenerate);
		
		QueryHistoryManager queryHistory = service.getQueryHistoryMng();
		service.rollUp(queryHistory.getQueryByName("LoanQuery11_S1_CG-Prtl"), "account_dim", "lvl3");
		
		service.slice(queryHistory.getQueryByName("LoanQuery11_S1_CG-Prtl"), "account_dim", "lvl1", "=", "'Sumperk'");

		List<String> dimensions = Collections.unmodifiableList(Arrays.asList("account_dim", "date_dim"));
		List<String> levels = Collections.unmodifiableList(Arrays.asList("lvl1", "lvl3"));
		List<String> operators = Collections.unmodifiableList(Arrays.asList("=", "="));
		List<String> values = Collections.unmodifiableList(Arrays.asList("'Sumperk'", "1998"));
		
		service.dice(queryHistory.getQueryByName("LoanQuery11_S1_CG-Prtl"), dimensions, levels, operators, values);
		
		
		String remoteFolder = resMetadata.getLocalFolder();
		String remoteResultsFile = resMetadata.getResultFile();
		String remoteInfoFile = resMetadata.getResultInfoFile();
		ArrayList<String> models = resMetadata.getComponentResultFiles();
		ArrayList<String> modelInfos = resMetadata.getComponentResultInfoFiles();
		
		System.out.println("\nRES\t" + remoteResultsFile + "\nINFO\t" + remoteInfoFile + "\nCOMP\t" + models.get(0));
		
		String localFolder = "ClientCache" + File.separator;
		File remoteRes = new File(remoteResultsFile);
		ClientRMITransferer.download(service, remoteRes, new File( localFolder + queryName + ".tab"));
		File remoteIRes = new File(remoteInfoFile);
		ClientRMITransferer.download(service, remoteIRes, new File(localFolder + queryName + "_Info.txt"));
		
		if(models.size() > 0) {	
			for(String model: models){
				String sep = "\\" + File.separator;	//Java idioms. You need to add the "\\" before!
				String [] array = model.split(sep);
				String localModelName = "NoName";
				if (array.length > 0)
					localModelName = array[array.length-1].trim();
				File remote = new File(model);
				ClientRMITransferer.download(service, remote, new File("ClientCache"+ File.separator  + localModelName));
			}//end for
			for(String modelInfo: modelInfos){
				String sep = "\\" + File.separator;	//Java idioms. You need to add the "\\" before!
				String [] array = modelInfo.split(sep);
				String localModelName = "NoName";
				if (array.length > 0)
					localModelName = array[array.length-1].trim();
				File remote = new File(modelInfo);
				ClientRMITransferer.download(service, remote, new File("ClientCache"+ File.separator  + localModelName));
			}//end for

		
		}//end if
		
		
		System.out.println("Execution of client is complete");
	}//end main

	public static int deleteAllFilesOfFolder(File dir) {
		if(!dir.isDirectory())
			return -1;
		int i = 0;
		for(File file: dir.listFiles()) { 
		    if (!file.isDirectory() && !file.getName().equals("README.txt")) {
		        file.delete();
		        i++;
		    }
		}
		return i;
	}//end method
	
}//end class

//Foo fooService = (Foo) registry.lookup(Foo.class.getSimpleName());
//if(fooService == null) {
//	System.out.println("Unable to commence server, exiting");
//	System.exit(-100);
//}
//fooService.foo();
