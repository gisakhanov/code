package com.availity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Utility {

	/**
	 * Convert the content of the file to the list of strings.
	 * @param path
	 * @return
	 */
	public static List<String> fileToList(Path path) {
		List<String> list=new ArrayList<String>();
		try (BufferedReader br=new BufferedReader(new FileReader(path.toFile()));) {
			list=br.lines().collect(Collectors.toList());
		}
		catch (IOException e) {
			System.err.println(e.getStackTrace());
		}
		return list;
	}

	/**
	 * Delete directory with content
	 * @param path
	 * @return
	 */
	public static void deleteDirectory(Path path) throws IOException {
		for (Path dir:Files.walk(path).sorted(Comparator.reverseOrder()).toArray(Path[]::new))
		{
		    Files.delete(dir);
		}
	}

}
