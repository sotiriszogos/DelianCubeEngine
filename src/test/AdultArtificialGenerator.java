package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;

public class AdultArtificialGenerator {

	static final int NUMROWS = 10000000;
	static final int ID_OFFSET = 10000;
	static final String DELIM = ";";
	static final int NUMMILLIONROWS = (int)(NUMROWS / 1000000);
	static int c = 1;
	
	private AdultArtificialGenerator() {
		
	}
	
	public static void main(String[] args) {



		String filePrefix = "Adult";
		String outputFolder = "OutputFiles//Datagen//";
		String outputFileName = outputFolder + filePrefix + NUMMILLIONROWS + "M.csv";

		System.out.println("Starting generation of " + NUMROWS + " rows for file:\t" + outputFileName);
		
		File file=new File(outputFileName);
		FileOutputStream fileOutputStream=null;
		PrintStream printStream=null;
		try {
			fileOutputStream=new FileOutputStream(file);
			printStream=new PrintStream(fileOutputStream);
			String header[] = {"id","age","work_class","education","marital_status","occupation","race","gender", "hours_per_week", "native_country", "salary" };
			addToTabTextFile(printStream, header);
			
			for (int i=0; i < NUMROWS; i++) {		
				String [] nextRow = generateNextRow(i);
				addToTabTextFile(printStream, nextRow);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(fileOutputStream!=null){
					fileOutputStream.close();
				}
				if(printStream!=null){
					printStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}//end finally try
		}//end finally

		System.out.println("DONE");
	}//end main

	public static String [] generateNextRow(int rowCnt) {
		String [] edu = {"10th","11th","12th","1st-4th","5th-6th","7th-8th","9th","Assoc-acdm","Assoc-voc","Bachelors","Doctorate","HS-grad","Masters","Preschool","Prof-school","Some-college"};
		String [] gend = {"Male","Female"};
		String [] mar = {"Divorced","Married-AF-spouse","Married-civ-spouse","Married-spouse-absent","Never-married","Separated","Widowed"};
		String [] coun = {"Cambodia","Canada","China","Columbia","Cuba","Dominican-Republic","Ecuador","El-Salvador","England","France","Germany","Greece","Guatemala","Haiti","Holand-Netherlands","Honduras","Hong","Hungary","India","Iran","Ireland","Italy","Jamaica","Japan","Laos","Mexico","Nicaragua","Outlying-US(Guam-USVI-etc)","Peru","Philippines","Poland","Portugal","Puerto-Rico","Scotland","South","Taiwan","Thailand","Trinadad&Tobago","United-States","Vietnam","Yugoslavia"};
		String [] occu = {"Adm-clerical","Armed-Forces","Craft-repair","Exec-managerial","Farming-fishing","Handlers-cleaners","Machine-op-inspct","Other-service","Priv-house-serv","Prof-specialty","Protective-serv","Sales","Tech-support","Transport-moving"};
		String [] rac = {"Amer-Indian-Eskimo","Asian-Pac-Islander","Black","Other","White"};
		String [] work = {"Federal-gov","Local-gov","Private","Self-emp-inc","Self-emp-not-inc","State-gov","Without-pay"};
		String [] sal = {"<=50K",">50K"};
		
		String [] nextLine = new String[11] ;		

		nextLine[0] = Integer.toString(c);
		
		UniformIntegerDistribution age = new UniformIntegerDistribution(18, 70);
		nextLine[1]=Integer.toString(age.sample());
		
		UniformIntegerDistribution w = new UniformIntegerDistribution(0, 6);
		nextLine[2]=work[w.sample()];
		
		UniformIntegerDistribution e = new UniformIntegerDistribution(0, 15);
		nextLine[3]=edu[e.sample()];

		UniformIntegerDistribution m = new UniformIntegerDistribution(0, 6);
		nextLine[4]=mar[m.sample()];
		
		UniformIntegerDistribution o = new UniformIntegerDistribution(0, 13);
		nextLine[5]=occu[o.sample()];
		
		UniformIntegerDistribution r = new UniformIntegerDistribution(0, 4);
		nextLine[6]=rac[r.sample()];

		UniformIntegerDistribution g = new UniformIntegerDistribution(0, 1);
		nextLine[7]=gend[g.sample()];

		UniformIntegerDistribution hou = new UniformIntegerDistribution(1, 80);
		nextLine[8]=Integer.toString(hou.sample());

		UniformIntegerDistribution n = new UniformIntegerDistribution(0, 40);
		nextLine[9]=coun[n.sample()];
		
		UniformIntegerDistribution s = new UniformIntegerDistribution(0, 1);
		nextLine[10]=sal[s.sample()];
		
		c = c + 1;
	
		return nextLine;
	}//end method
		
	public static void addToTabTextFile(PrintStream printStream, String[] line) {

		
		for (int i =0; i< line.length;i++)
			printStream.print(line[i]+DELIM);
		printStream.println();
	}
}
