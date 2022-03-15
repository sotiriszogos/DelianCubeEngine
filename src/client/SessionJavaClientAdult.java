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

public class SessionJavaClientAdult implements Serializable{

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

		// Cube ADULT and queries
		service.initializeConnection("adult_no_dublic_10m", "CinecubesUser",
						"Cinecubes", "adult", "adult");
		System.out.println("Completed connection initialization");
		
		
		//CleanUp client Cache
		File resultFolder = new File("ClientCache");
		deleteAllFilesOfFolder(resultFolder);
		
		
		//Run queries
		File f2 = new File("InputFiles/adult/Adult/exp2.txt");
		ArrayList<String> fileLocations = service.answerCubeQueriesFromFile(f2);
		
		
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
		CubeQuery cb = queryHistory.getQueryByName("CubeQuery0");
		
		long startTime = 0;
		long stopTime = 0;
		long elapsedTime = 0;
		double elapsedTimeInSecond = 0;
		ResultFileMetadata resMetadata = null;
		
		//roll-up
		for(int i = 0; i < 5; i++) {
			startTime = System.nanoTime();
			resMetadata = service.rollUp(cb, "occupation_dim", "lvl2");
			stopTime = System.nanoTime();
			elapsedTime= stopTime - startTime;
			elapsedTimeInSecond = (double) elapsedTime / 1_000_000_000;
			System.out.println("Rollup: " + elapsedTimeInSecond + " seconds");
		}
        
		String remoteResultsFile = resMetadata.getResultFile();
		String remoteInfoFile = resMetadata.getResultInfoFile();
		
		
		String localFolder = "ClientCache" + File.separator;
		File remoteRes = new File(remoteResultsFile);
		ClientRMITransferer.download(service, remoteRes, new File( localFolder + cb.getName() + "_t.tab"));
		File remoteIRes = new File(remoteInfoFile);
		ClientRMITransferer.download(service, remoteIRes, new File(localFolder + cb.getName() + "_Info.txt"));
		
		//drill-down
		for(int i = 0; i < 5; i++) {
			startTime = System.nanoTime();
			resMetadata = service.drillDown(cb, "education_dim", "lvl1");
			stopTime = System.nanoTime();
			elapsedTime= stopTime - startTime;
			elapsedTimeInSecond = (double) elapsedTime / 1_000_000_000;
			System.out.println("Drilldpwm: " + elapsedTimeInSecond + " seconds");
		}
		
		localFolder = "ClientCache" + File.separator;
		remoteRes = new File(remoteResultsFile);
		ClientRMITransferer.download(service, remoteRes, new File( localFolder + cb.getName() + "_t.tab"));
		remoteIRes = new File(remoteInfoFile);
		ClientRMITransferer.download(service, remoteIRes, new File(localFolder + cb.getName() + "_Info.txt"));

		//slice
		for(int i = 0; i < 5; i++) {
			startTime = System.nanoTime();
			resMetadata = service.slice(queryHistory.getQueryByName("CubeQuery0"), "marital_dim", "lvl2", "=", "'Married'");
			stopTime = System.nanoTime();
			elapsedTime= stopTime - startTime;
			elapsedTimeInSecond = (double) elapsedTime / 1_000_000_000;
			System.out.println("Slice: "+ elapsedTimeInSecond + " seconds");
		}
		
		localFolder = "ClientCache" + File.separator;
		remoteRes = new File(remoteResultsFile);
		ClientRMITransferer.download(service, remoteRes, new File( localFolder + cb.getName() + "_t.tab"));
		remoteIRes = new File(remoteInfoFile);
		ClientRMITransferer.download(service, remoteIRes, new File(localFolder + cb.getName() + "_Info.txt"));
		
		//dice
		List<String> dimensions = Collections.unmodifiableList(Arrays.asList("marital_dim", "education_dim"));
		List<String> levels = Collections.unmodifiableList(Arrays.asList("lvl2", "lvl3"));
		List<String> operators = Collections.unmodifiableList(Arrays.asList("=", "="));
		List<String> values = Collections.unmodifiableList(Arrays.asList("'Married'", "'Post-Secondary'"));
		
		for(int i = 0; i < 5; i++) {
			startTime = System.nanoTime();
			service.dice(queryHistory.getQueryByName("CubeQuery0"), dimensions, levels, operators, values);
			stopTime = System.nanoTime();
			elapsedTime= stopTime - startTime;
			elapsedTimeInSecond = (double) elapsedTime / 1_000_000_000;
			System.out.println("Dice: "+ elapsedTimeInSecond + " seconds");
		}
		
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
