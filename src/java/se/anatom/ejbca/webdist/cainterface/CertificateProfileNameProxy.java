package se.anatom.ejbca.webdist.cainterface;


import java.util.HashMap;

import se.anatom.ejbca.ca.store.ICertificateStoreSessionLocal;
import se.anatom.ejbca.log.Admin;
/**
 * A class used to improve performance by proxying certificateprofile id to certificate name mappings by minimizing the number of needed lockups over rmi.
 * 
 * @version $Id: CertificateProfileNameProxy.java,v 1.6 2003-09-04 09:46:43 herrvendil Exp $
 */
public class CertificateProfileNameProxy {
    
    /** Creates a new instance of ProfileNameProxy */
    public CertificateProfileNameProxy(Admin administrator, ICertificateStoreSessionLocal certificatestoresession){
      this.certificatestoresession = certificatestoresession;
      
      certificateprofilenamestore = new HashMap(); 
      this.admin= administrator;
        
    }
    
    /**
     * Method that first tries to find certificateprofile name in local hashmap and if it doesn't exists looks it up over RMI.
     *
     * @param certificateprofileid the certificateprofile id number to look up.
     * @return the certificateprofilename or null if no certificateprofilename is relatied to the given id
     */
    public String getCertificateProfileName(int certificateprofileid)  {
      String returnval = null;  
      // Check if name is in hashmap
      returnval = (String) certificateprofilenamestore.get(new Integer(certificateprofileid));
      
      if(returnval==null){
        // Retreive profilename 
        returnval = certificatestoresession.getCertificateProfileName(admin, certificateprofileid);
        if(returnval != null)
          certificateprofilenamestore.put(new Integer(certificateprofileid),returnval);
      }    
       
      return returnval;
    }
    
    // Private fields
    private HashMap                        certificateprofilenamestore;
    private ICertificateStoreSessionLocal  certificatestoresession;
    private Admin                          admin;

}
