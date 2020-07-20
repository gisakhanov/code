/**
 * CSV Processor
 * 
 */
package com.availity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Represents CSV record
 * @author George
 *
 */
class CSVRecord implements Comparable<CSVRecord>{
	
	final private String id;
	final private String firstName;
	final private String lastName;
	final private Integer version;
	final private String insuranceCompany;

	public String getId() {return id;}
	public String getFirstName() {return firstName;}
	public String getLastName() {return lastName;}
	public Integer getVersion() {return version;}
	public String getInsuranceCompany() {return insuranceCompany;}
	
	public CSVRecord(String[] list) throws DataTranformationException {
		Objects.requireNonNull(list);
		if (list.length!=5) throw new DataTranformationException("Incorrect data for CSV record");
		Optional<String> idValue=Optional.ofNullable(list[0]);
		if (idValue.isEmpty()) throw new DataTranformationException("ID is undefined");
		else id=idValue.get();
		Optional<String> idLastName=Optional.ofNullable(list[1]);
		if (idLastName.isEmpty()) throw new DataTranformationException("Last Name is undefined");
		else lastName=idLastName.get();
		Optional<String> idFirstName=Optional.ofNullable(list[2]);
		if (idFirstName.isEmpty()) throw new DataTranformationException("First Name is undefined");
		else firstName=idFirstName.get();
		Optional<String> idVersion=Optional.ofNullable(list[3]);
		if (idVersion.isEmpty()) throw new DataTranformationException("Version is undefined");
		else {
			try {
				version=Integer.valueOf(idVersion.get()).intValue();
			}
			catch (NumberFormatException e) {
				throw new DataTranformationException("Version is not a numeric value");
			}
		}
		Optional<String> idInsuranceCompany=Optional.ofNullable(list[4]);
		if (idInsuranceCompany.isEmpty()) throw new DataTranformationException("Insurance Company is undefined");
		else insuranceCompany=idInsuranceCompany.get();
	}
	
	/**
	 * Convert to CVS record
	 * @return
	 */
	public String toCVSRecord() {return id+","+lastName+","+firstName+","+version+","+insuranceCompany;}
	
	@Override
	public int compareTo(CSVRecord o) {
		int ret=this.lastName.compareTo(o.getLastName());
		if (ret==0) ret=this.firstName.compareTo(o.getFirstName());
		return ret;
	}


}

/**
 * @author George
 *
 */
public class CSVProcessor {
	
	/**
	 * This method extracts data from CSV file, cleanse them and group by data per insurance company in own file.
	 * The data in each file will be sorted by last name and first name.
	 * If there are duplicate User IDs for the same Insurance Company, then only the record with the highest version should be included. 
	 * @param csvDataFile Path to the CSV file.
	 * @param badDataFile Path where rejected data will be stored.
	 * @param workingDir Path where CSV files with sorted data per insurance company will be created.
	 */
	public void process(String csvDataFile, String badDataFile, String workingDir) {
		Hashtable<String,HashMap<String,CSVRecord>> tab=new Hashtable<>();
		try (BufferedReader br=new BufferedReader(new FileReader(csvDataFile));
			 BufferedWriter bw=new BufferedWriter(new FileWriter(badDataFile));) {
			br.lines().filter(p->p.length()>0).forEach(line->{
				String[] dataList=line.split(",");
				try {
					CSVRecord rec=new CSVRecord(dataList);
					// if an entry is not found create a new one
					if (!tab.containsKey(rec.getInsuranceCompany())) {
						HashMap<String,CSVRecord> map=new HashMap<>();
						map.put(rec.getId(),rec);
						tab.put(rec.getInsuranceCompany(),map);
					}
					// update the existing entry
					else {
						// if such entry exists and current version is higher then update it
						tab.get(rec.getInsuranceCompany()).compute(rec.getId(), (key,value)-> (value==null || value!=null && rec.getVersion()>=value.getVersion())?rec:value);
					}
				}
				//save bad records in own file for further review
				catch (DataTranformationException e) {
					try {
						bw.write(line);
						bw.newLine();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
			//sort and save cleansed records per Insurance company
			tab.forEach((key,value)-> {
				//sort data
				TreeMap<String,CSVRecord> sortingTree=new TreeMap<>(value);
				//create own file per Insurance company 
				try (BufferedWriter cfw=new BufferedWriter(new FileWriter(Paths.get(workingDir,key+".csv").toFile()));) {
					sortingTree.forEach((sortkey,sortvalue)->{
						try {
							cfw.write(sortvalue.toCVSRecord());
							cfw.newLine();
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
				}
				catch (IOException e) {
					System.err.println(e.getStackTrace());
				}
			});
		}
		catch (IOException e) {
			System.err.println(e.getStackTrace());
		}
	}

}
