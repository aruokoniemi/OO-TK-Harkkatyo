package harkkatyo;

import java.io.IOException;
import java.io.InputStream;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class XMLParser {
    final private ProgressBar pb;
    DatabaseHandler db = DatabaseHandler.getInstance();
    
    public XMLParser() {
        pb = null;
    }
    
    public XMLParser(ProgressBar pb) {
        this.pb = pb;
    }
    
    
    //Get all smartpost data from XML to database
    public Task<Void> dataGetterTask = new Task<Void>() {
        @Override
        public Void call() {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            Document doc;
            String xmlURL = "http://smartpost.ee/fi_apt.xml";


            try {
                builder = factory.newDocumentBuilder();
                InputStream is = this.getClass().getResourceAsStream("fi_apt.xml");
                doc = builder.parse(is);
                Element root = doc.getDocumentElement();
                NodeList smartposts = root.getElementsByTagName("place");               
                
                for ( int i = 0; i < smartposts.getLength(); i++ ) {
                    Node smartpost = smartposts.item(i);
                    if ( smartpost.getNodeType() == Node.ELEMENT_NODE ) {
                        Element e = (Element) smartpost;
                        String address = e.getElementsByTagName("address").item(0).getTextContent();
                        String postalNumber = e.getElementsByTagName("code").item(0).getTextContent();
                        String city = e.getElementsByTagName("city").item(0).getTextContent();
                        String availability = e.getElementsByTagName("availability").item(0).getTextContent();
                        String postoffice = e.getElementsByTagName("postoffice").item(0).getTextContent();
                        String lat = e.getElementsByTagName("lat").item(0).getTextContent();
                        String lng = e.getElementsByTagName("lng").item(0).getTextContent();

                        
                        while(true) {
                            //Add cities
                            if ( !db.addCity(city) )
                                break;

                            if ( !db.addPostOffice(postoffice) )
                                break;

                            if  ( !db.addPostalNumber(postalNumber, city) ) 
                                break;

                            int locationID;
                            int addressID;
                            //Add smartpost only if address and location added successfuly
                            int SmartPostID = db.getNewSmartPostID();
                            SmartPost sPost = new SmartPost(SmartPostID, address, city, postalNumber,
                                    availability, postoffice, lat, lng);

                            if ( db.addAddress(address, city, postalNumber) 
                                    && (db.addLocation(lat, lng))) {                           
                                locationID = db.getLastID("locationid", "location");
                                addressID = db.getLastID("addressid", "address");
                                //add if got ids from db
                                if ( locationID != -1 && addressID != -1 ) {
                                    db.addSmartPost(sPost, locationID, addressID);
                                }
                            }
                            break;
                        }

                    }
                    if (!pb.isVisible()) {
                        pb.setVisible(true);
                    }
                    this.updateProgress(i, smartposts.getLength());
                } 
                this.updateProgress(1, 1);
                
            }
            catch (IOException | SAXException | ParserConfigurationException e) {
                System.out.println(e.getMessage());
            }
        
        return null;
        }
    };
    
    
}
