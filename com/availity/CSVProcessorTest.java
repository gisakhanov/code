package com.availity;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;

class CSVProcessorTest {
	
	final static String testDir="C:\\!\\test";
	final static String outputDir="output";
	final static String cvsFile="test.csv";
	final static String rejectFile="reject.csv";
	final static String insuranceCompany1="IC1";
	final static String insuranceCompany2="IC2";
	final static String correctRecId1_1="id1,Last1.1,First1.1,1,"+insuranceCompany1;//ID1 correct record version 1 - will be excluded from the final file
	final static String correctRecId1_2="id1,Last1.2,First1.2,2,"+insuranceCompany1;//ID1 correct record version 2 - will be included into the final file
	final static String correctRecId2="id2,Last2,First2,2,"+insuranceCompany2;//ID2
	final static String correctRecId3="id3,Last3,First3,3,"+insuranceCompany2;//ID3
	final static String badRecId1="fid1,L,F,a,I2";//Bad record 1
	final static String badRecId2="fid1,L,F,,I2";//Bad record 2
	final static String badRecId3="fid2,Trash2,1";//Bad record 3
	
	/**
	 * Get the (n) record from the file
	 * @param path
	 * @return
	 */
	public Optional<String> getNRecord(Path path, long l) {
		Optional<String> s= Optional.empty();
		try (BufferedReader br=new BufferedReader(new FileReader(path.toFile()));) {
			s=br.lines().skip(l-1).findFirst();
		}
		catch (IOException e) {
			System.err.println(e.getStackTrace());
		}
		return s;
	}

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		//clean up and recreate CSV data file
		Path dataFile=Paths.get(testDir,cvsFile);
		Files.deleteIfExists(dataFile);
		Files.deleteIfExists(Paths.get(testDir,rejectFile));
		Path outDirPath=Paths.get(testDir,outputDir);
		if (Files.exists(outDirPath)) Utility.deleteDirectory(outDirPath);
		Files.createDirectories(outDirPath);
		//create CSV file 
		List<String> list= List.of( correctRecId1_1,//ID1 correct record version 1 - will be excluded from the final file
									correctRecId1_2,//ID1 correct record version 2 - will be included into the final file
									badRecId1,		//junk record should be rejected due to incorrect version data type
									badRecId2,		//junk record should be rejected due to null value of version
									"",				//will be ignored
									badRecId3,		//junk record should be rejected due to incorrect data
									correctRecId3,	//ID3 
									correctRecId2);	//ID2
		try (BufferedWriter bw=new BufferedWriter(new FileWriter(dataFile.toFile()));) {
			for (String s:list) {
				bw.write(s);
				bw.newLine();
			}
		}
	}

	@Test
	void test() {
		CSVProcessor processor=new CSVProcessor();
		processor.process(Paths.get(testDir,cvsFile).toString(), Paths.get(testDir,rejectFile).toString(), Paths.get(testDir,outputDir).toString());
		//test that junk records has been properly rejected
		List<String> rejectList=Utility.fileToList(Paths.get(testDir,rejectFile));
		assertLinesMatch(List.of(badRecId1,badRecId2,badRecId3),rejectList);
		//test that we took the record with the highest version for the same insurance company if IDs are the same
		List<String> insuranceCompanyList1=Utility.fileToList(Paths.get(testDir,outputDir,insuranceCompany1+".csv"));
		assertLinesMatch(List.of(correctRecId1_2),insuranceCompanyList1);
		//test that records have been sorted properly
		List<String> insuranceCompanyList2=Utility.fileToList(Paths.get(testDir,outputDir,insuranceCompany2+".csv"));
		assertLinesMatch(List.of(correctRecId2,correctRecId3),insuranceCompanyList2);
	}

}
