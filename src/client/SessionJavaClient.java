package client;

import java.io.File;
import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cubemanager.cubebase.CubeQuery;
import cubemanager.cubebase.QueryHistoryManager;
import mainengine.IMainEngine;
import mainengine.ResultFileMetadata;

public class SessionJavaClient  implements Serializable{
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
		
		QueryHistoryManager queryHistory = service.getQueryHistoryMng();
		CubeQuery cb = queryHistory.getQueryByName("LoanQuery11_S1_CG-Prtl");
		ResultFileMetadata resMetadata = service.rollUp(cb, "account_dim", "lvl3");
		
		String remoteResultsFile = resMetadata.getResultFile();
		String remoteInfoFile = resMetadata.getResultInfoFile();
				
		String localFolder = "ClientCache" + File.separator;
		File remoteRes = new File(remoteResultsFile);
		ClientRMITransferer.download(service, remoteRes, new File( localFolder + cb.getName() + "_t.tab"));
		File remoteIRes = new File(remoteInfoFile);
		ClientRMITransferer.download(service, remoteIRes, new File(localFolder + cb.getName() + "_Info.txt"));
		
		resMetadata = service.drillDown(cb, "account_dim", "lvl1");

		localFolder = "ClientCache" + File.separator;
		remoteRes = new File(remoteResultsFile);
		ClientRMITransferer.download(service, remoteRes, new File( localFolder + cb.getName() + "_t.tab"));
		remoteIRes = new File(remoteInfoFile);
		ClientRMITransferer.download(service, remoteIRes, new File(localFolder + cb.getName() + "_Info.txt"));

		
		resMetadata = service.slice(queryHistory.getQueryByName("LoanQuery11_S1_CG-Prtl"), "account_dim", "lvl1", "=", "'Sumperk'");

		localFolder = "ClientCache" + File.separator;
		remoteRes = new File(remoteResultsFile);
		ClientRMITransferer.download(service, remoteRes, new File( localFolder + cb.getName() + "_t.tab"));
		remoteIRes = new File(remoteInfoFile);
		ClientRMITransferer.download(service, remoteIRes, new File(localFolder + cb.getName() + "_Info.txt"));
		
		
		List<String> dimensions = Collections.unmodifiableList(Arrays.asList("account_dim", "date_dim"));
		List<String> levels = Collections.unmodifiableList(Arrays.asList("lvl1", "lvl3"));
		List<String> operators = Collections.unmodifiableList(Arrays.asList("=", "="));
		List<String> values = Collections.unmodifiableList(Arrays.asList("'Sumperk'", "1998"));
		
		service.dice(queryHistory.getQueryByName("LoanQuery11_S1_CG-Prtl"), dimensions, levels, operators, values);
		
		localFolder = "ClientCache" + File.separator;
		remoteRes = new File(remoteResultsFile);
		ClientRMITransferer.download(service, remoteRes, new File( localFolder + cb.getName() + "_t.tab"));
		remoteIRes = new File(remoteInfoFile);

		
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

}
