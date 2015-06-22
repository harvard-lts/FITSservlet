package edu.harvard.hul.ois.fits.service.config;

import java.io.IOException;
import java.util.Properties;

public class ReadPropertiesFile {
	
	private static String fitsHome = "";

	public void main() {
		
		  Properties properties = new Properties();
		  try {

			properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("project.properties"));
			fitsHome = System.getProperty("fits.home");

		  } catch (IOException e) {
			e.printStackTrace();
		  }

		  if (fitsHome == null) {
	      	System.out.println("**** ERROR **** MISSING CONFIGURATION DATA!");
		  } else {
		      	System.out.println("CONFIGURATION DATA READ! fitsHome="+fitsHome);
		  }
		  
		  System.out.println("fitsHome="+fitsHome);
		  

//		try {
//			File file = new File("test.properties");
//			FileInputStream fileInput = new FileInputStream(file);
//			Properties properties = new Properties();
//			properties.load(fileInput);
//			fileInput.close();
//	
//			Enumeration enuKeys = properties.keys();
//			while (enuKeys.hasMoreElements()) {
//				String key = (String) enuKeys.nextElement();
//				String value = properties.getProperty(key);
//				System.out.println(key + ": " + value);
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
}
