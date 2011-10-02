package spider.prototype.services.modules.connection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import spider.prototype.services.Controller;
import spider.prototype.services.yellowpage.ServiceDescription;
import spider.prototype.services.yellowpage.YellowPage;
import spider.prototype.utils.BinaryTransformer;

public class XMLConnectionModule extends ConnectionModule {

	private List<YellowPage> values = new ArrayList<YellowPage>();
	
	// received answers
	int recv = 0;
	
	// xml
    Document doc;
    Element name;
    Element root;

	
	
	boolean isCollecting = false;
	
	
	public XMLConnectionModule(int serviceId) {
		super(serviceId);
		
		createXMLDoc();
		
		
	}
	
	public void start() {
		autoCollect(1);
		autoSend(5);
	}
	
	public void sendDataRequests() {
		
		// collecting == true && send data requests to all used services
		startXMLCollection();
		
		for (YellowPage yp : values) {
			
			Controller.getInstance().sendDataRequest(this, yp.getAddress(), yp.getServiceDescription(), yp.getServiceId());
		}
		
		
	}
	
	public void sendDataRequests(boolean toAll) {
	
		
		
		if (toAll == false) {
			
			sendDataRequests();
		} else {
			
			// collecting == true && send data requests to ALL services
			values = new ArrayList<YellowPage>();
			for (ServiceDescription sd : ServiceDescription.values()) {

				for (YellowPage yp : Controller.getInstance().getYellowPage(sd)) {
					
					values.add(yp);
				}
			}
			
			sendDataRequests();
			
			
		}
	}
	
	
	public void saveData() {
		
		saveXMLToFile();
	}
	
	public void sendData() {
		
		  String response = "";
		  URL url;
		try {
			url = new URL(getServer());
		
		  URLConnection conn = url.openConnection();
		  
		  // Set connection parameters.
		  conn.setDoInput (true);
		  conn.setDoOutput (true);
		  conn.setUseCaches (false);
		  
		  // Make server believe we are form data...
		  conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		  DataOutputStream out = new DataOutputStream (conn.getOutputStream ());
		  
		  // Write out the bytes of the content string to the stream.
		  out.writeBytes("data=" + getXMLAsString());
		  out.flush ();
		  out.close ();
		  
		  // Read response from the input stream.
		  BufferedReader in = new BufferedReader (new InputStreamReader(conn.getInputStream ()));
		  String temp;
		  while ((temp = in.readLine()) != null){
		    response += temp + "\n";
		   }
		  temp = null;
		  in.close ();
		  
		  // TODO what shall we do with the response?
		  //System.out.println("Server response:\n'" + response + "'");
		} catch (Exception e) {
			// TODO handle this fkn exception server shitty stuff
			e.printStackTrace();
		}
		
		createXMLDoc();
	}

	@Override
	public void use(byte[] parameters) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDataResponse(ServiceDescription serviceDesc, int serviceId, byte[] response) {
		
		if (isCollecting == true) {
			
			Element node = doc.createElement("sensors");
			if (serviceDesc.equals(ServiceDescription.Temperature)) {
				node.setAttribute("value", new Double(BinaryTransformer.toDouble(response)).toString());
			} else {
				node.setAttribute("value", BinaryTransformer.toString(response).toString());
			}
			node.setAttribute("sedesc", new Integer(serviceDesc.ordinal()).toString());
			node.setAttribute("sid", new Integer(serviceId).toString());
	        root.appendChild(node);
	        
	        recv++;
	        if (recv >= values.size()) {
	        	isCollecting = false;
	        	saveData();
	        }
		}
		
		
	}

	@Override
	public void onObservableResponse(byte[] value) {
		// TODO Auto-generated method stub
		
	}
	
	
	private void startXMLCollection() {
		
		isCollecting = true;
		recv = 0;
		createXMLTimeArea();

		// stop collector & save info in 5 seks
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(5 * 1000);
					if (isCollecting == true) {
						isCollecting = false;
						saveData();
					}
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}).start();
	}
	
	private void createXMLDoc() {
		
		// create XML document
        try {
 			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
 		} catch (ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		name = doc.createElement("spider");
 		name.setAttribute("sid", Integer.valueOf(getServiceId()).toString());
		doc.appendChild(name);
	}
	
	private void createXMLTimeArea() {
		
		// create timestamps
		root = doc.createElement("date");
		root.setAttribute("value", Calendar.getInstance().getTime().toString());
		name.appendChild(root);
	}
	
	private void saveXMLToFile() {
		
		// for line spaces in file
		
		
		// Prepare the DOM document for writing
        Source source = new DOMSource(doc);

        // Prepare the output file
        File file = new File("info.xml");
        Result result = new StreamResult(file);

        // Write the DOM document to the file
        Transformer xformer;
		try {
			xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	      
	}
	
	private String getXMLAsString() {
		
	    BufferedReader reader = null;
		try {
			reader = new BufferedReader( new FileReader ("info.xml"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    String line  = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    String ls = System.getProperty("line.separator");
	    try {
			while( ( line = reader.readLine() ) != null ) {
			    stringBuilder.append( line );
			    stringBuilder.append( ls );
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return stringBuilder.toString();

	}

	private void autoSend(final int minutes) {
		
new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					while (true) {
						Thread.sleep(minutes * 100 * 60);
						sendData();
					}
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}).start();
	}
	
	private void autoCollect(final int minutes) {
		
new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					while (true) {
						Thread.sleep(minutes * 100 * 60);
						sendDataRequests(true);
					}
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}).start();
	}
}
