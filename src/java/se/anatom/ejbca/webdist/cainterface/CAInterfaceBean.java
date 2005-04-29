/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
 
package se.anatom.ejbca.webdist.cainterface;

import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;

import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.jce.PKCS10CertificationRequest;

import se.anatom.ejbca.ca.crl.ICreateCRLSessionHome;
import se.anatom.ejbca.apply.RequestHelper;
import se.anatom.ejbca.authorization.IAuthorizationSessionLocal;
import se.anatom.ejbca.authorization.IAuthorizationSessionLocalHome;
import se.anatom.ejbca.ca.caadmin.CAInfo;
import se.anatom.ejbca.ca.caadmin.ICAAdminSessionLocal;
import se.anatom.ejbca.ca.caadmin.ICAAdminSessionLocalHome;
import se.anatom.ejbca.ca.crl.RevokedCertInfo;
import se.anatom.ejbca.ca.publisher.IPublisherSessionLocal;
import se.anatom.ejbca.ca.publisher.IPublisherSessionLocalHome;
import se.anatom.ejbca.ca.sign.ISignSessionLocal;
import se.anatom.ejbca.ca.sign.ISignSessionLocalHome;
import se.anatom.ejbca.ca.store.CRLInfo;
import se.anatom.ejbca.ca.store.ICertificateStoreSessionLocal;
import se.anatom.ejbca.ca.store.ICertificateStoreSessionLocalHome;
import se.anatom.ejbca.ca.store.certificateprofiles.CertificateProfile;
import se.anatom.ejbca.hardtoken.IHardTokenSessionLocal;
import se.anatom.ejbca.hardtoken.IHardTokenSessionLocalHome;
import se.anatom.ejbca.log.Admin;
import se.anatom.ejbca.ra.IUserAdminSessionLocal;
import se.anatom.ejbca.ra.IUserAdminSessionLocalHome;
import se.anatom.ejbca.ra.raadmin.IRaAdminSessionLocal;
import se.anatom.ejbca.ra.raadmin.IRaAdminSessionLocalHome;
import se.anatom.ejbca.util.Base64;
import se.anatom.ejbca.util.CertTools;
import se.anatom.ejbca.util.ServiceLocator;
import se.anatom.ejbca.webdist.rainterface.CertificateView;
import se.anatom.ejbca.webdist.rainterface.RevokedInfoView;
import se.anatom.ejbca.webdist.webconfiguration.EjbcaWebBean;
import se.anatom.ejbca.webdist.webconfiguration.InformationMemory;


/**
 * A class used as an interface between CA jsp pages and CA ejbca functions.
 *
 * @author  Philip Vendil
 * @version $Id: CAInterfaceBean.java,v 1.28 2005-04-29 10:34:01 anatom Exp $
 */
public class CAInterfaceBean   {


    /** Creates a new instance of CaInterfaceBean */
    public CAInterfaceBean() {
    }

    // Public methods
    public void initialize(HttpServletRequest request, EjbcaWebBean ejbcawebbean) throws  Exception{

      if(!initialized){
        administrator = new Admin(((X509Certificate[]) request.getAttribute( "javax.servlet.request.X509Certificate" ))[0]);
        ServiceLocator locator = ServiceLocator.getInstance();
        ICertificateStoreSessionLocalHome certificatesessionhome = (ICertificateStoreSessionLocalHome) locator.getLocalHome(ICertificateStoreSessionLocalHome.COMP_NAME);
        certificatesession = certificatesessionhome.create();
        
        ICAAdminSessionLocalHome caadminsessionhome = (ICAAdminSessionLocalHome) locator.getLocalHome(ICAAdminSessionLocalHome.COMP_NAME);
        caadminsession = caadminsessionhome.create();
        
        IAuthorizationSessionLocalHome authorizationsessionhome = (IAuthorizationSessionLocalHome) locator.getLocalHome(IAuthorizationSessionLocalHome.COMP_NAME);
        authorizationsession = authorizationsessionhome.create();
        
        IUserAdminSessionLocalHome adminsessionhome = (IUserAdminSessionLocalHome) locator.getLocalHome(IUserAdminSessionLocalHome.COMP_NAME);
        adminsession = adminsessionhome.create();

        IRaAdminSessionLocalHome raadminsessionhome = (IRaAdminSessionLocalHome) locator.getLocalHome(IRaAdminSessionLocalHome.COMP_NAME);
        raadminsession = raadminsessionhome.create();               
        
		ISignSessionLocalHome home = (ISignSessionLocalHome)locator.getLocalHome(ISignSessionLocalHome.COMP_NAME );
	    signsession = home.create();
	    
	    IHardTokenSessionLocalHome hardtokensessionhome = (IHardTokenSessionLocalHome)locator.getLocalHome(IHardTokenSessionLocalHome.COMP_NAME);
	    hardtokensession = hardtokensessionhome.create();               
	    
	    IPublisherSessionLocalHome publishersessionhome = (IPublisherSessionLocalHome) locator.getLocalHome(IPublisherSessionLocalHome.COMP_NAME);
	    publishersession = publishersessionhome.create();               
	    
	    
        this.informationmemory = ejbcawebbean.getInformationMemory();
          
        certificateprofiles = new CertificateProfileDataHandler(administrator, certificatesession, authorizationsession, informationmemory);
        cadatahandler = new CADataHandler(administrator, caadminsession, adminsession, raadminsession, certificatesession, authorizationsession, signsession, ejbcawebbean);
        publisherdatahandler = new PublisherDataHandler(administrator, publishersession, authorizationsession, 
        		                                        caadminsession, certificatesession,  informationmemory);
        initialized =true;
      }
    }

    public CertificateView[] getCACertificates(int caid) {
      CertificateView[] returnval = null;      
      
      Collection chain = signsession.getCertificateChain(administrator, caid);
      
      returnval = new CertificateView[chain.size()];
      Iterator iter = chain.iterator();
      int i=0;
      while(iter.hasNext()){
        Certificate next = (Certificate) iter.next();  
        RevokedInfoView revokedinfo = null;
        RevokedCertInfo revinfo = certificatesession.isRevoked(administrator, CertTools.getIssuerDN((X509Certificate) next), ((X509Certificate) next).getSerialNumber());
        if(revinfo != null && revinfo.getReason() != RevokedCertInfo.NOT_REVOKED)
          revokedinfo = new RevokedInfoView(revinfo);
        returnval[i] = new CertificateView((X509Certificate) next, revokedinfo,null);
        i++;
      }

      return returnval;
    }
    
    /**
     * Method that returns a HashMap connecting available CAIds (Integer) to CA Names (String).
     *
     */ 
    
    public HashMap getCAIdToNameMap(){
      return informationmemory.getCAIdToNameMap();      
    }

    /**
     * Return the name of the CA based on its ID
     * @param caId the ca ID
     * @return the name of the CA or null if it does not exists.
     */
    public String getName(Integer caId) {
        return (String)informationmemory.getCAIdToNameMap().get(caId);
    }

    public Collection getAuthorizedCAs(){
      return informationmemory.getAuthorizedCAIds();
    }  
      
      
    public TreeMap getEditCertificateProfileNames() {
      return informationmemory.getEditCertificateProfileNames();
    }

    /** Returns the profile name from id proxied */
    public String getCertificateProfileName(int profileid) {
      return this.informationmemory.getCertificateProfileNameProxy().getCertificateProfileName(profileid);
    }
    
    public int getCertificateProfileId(String profilename){
      return certificateprofiles.getCertificateProfileId(profilename);
    }


    public CertificateProfile getCertificateProfile(String name)  throws Exception{
      return certificateprofiles.getCertificateProfile(name);
    }

    public CertificateProfile getCertificateProfile(int id)  throws Exception{
      return certificateprofiles.getCertificateProfile(id);
    }

    public void addCertificateProfile(String name) throws Exception{
       CertificateProfile profile = new CertificateProfile();
       profile.setAvailableCAs(informationmemory.getAuthorizedCAIds());
       
       certificateprofiles.addCertificateProfile(name, profile);
              
    }

   
    public void changeCertificateProfile(String name, CertificateProfile profile) throws Exception {
       certificateprofiles.changeCertificateProfile(name, profile);
    }
    
    /** Returns false if certificate type is used by any user or in profiles. */
    public boolean removeCertificateProfile(String name) throws Exception{

        boolean certificateprofileused = false;
        int certificateprofileid = certificatesession.getCertificateProfileId(administrator, name);        
        CertificateProfile certprofile = this.certificatesession.getCertificateProfile(administrator, name);
        
        if(certprofile.getType() == CertificateProfile.TYPE_ENDENTITY){
          // Check if any users or profiles use the certificate id.
          certificateprofileused = adminsession.checkForCertificateProfileId(administrator, certificateprofileid)
                                || raadminsession.existsCertificateProfileInEndEntityProfiles(administrator, certificateprofileid)
								|| hardtokensession.existsCertificateProfileInHardTokenProfiles(administrator, certificateprofileid);
        }else{
           certificateprofileused = caadminsession.exitsCertificateProfileInCAs(administrator, certificateprofileid);
        }
            
          
        if(!certificateprofileused){
          certificateprofiles.removeCertificateProfile(name);
        }

        return !certificateprofileused;
    }

    public void renameCertificateProfile(String oldname, String newname) throws Exception{
       certificateprofiles.renameCertificateProfile(oldname, newname);
    }

    public void cloneCertificateProfile(String originalname, String newname) throws Exception{
      certificateprofiles.cloneCertificateProfile(originalname, newname);
    }    
      
    public void createCRL(String issuerdn)  throws RemoteException, NamingException, CreateException  {      
      InitialContext jndicontext = new InitialContext();
      ICreateCRLSessionHome home  = (ICreateCRLSessionHome)javax.rmi.PortableRemoteObject.narrow( jndicontext.lookup("CreateCRLSession") , ICreateCRLSessionHome.class );
      home.create().run(administrator, issuerdn);
    }

    public int getLastCRLNumber(String  issuerdn) {
      return certificatesession.getLastCRLNumber(administrator, issuerdn);      
    }
    
    public CRLInfo getLastCRLInfo(String issuerdn) {
      return certificatesession.getLastCRLInfo(administrator,  issuerdn);          
    }

    /* Returns certificateprofiles as a CertificateProfiles object */
    public CertificateProfileDataHandler getCertificateProfileDataHandler(){
      return certificateprofiles;
    }
    
    public HashMap getAvailablePublishers() {
      return publishersession.getPublisherIdToNameMap(administrator);
    }
    
    public PublisherDataHandler getPublisherDataHandler() {    
    	return this.publisherdatahandler;
    }
    
    public CADataHandler getCADataHandler(){
      return cadatahandler;   
    }
    
    public CAInfoView getCAInfo(String name) throws Exception{
      return cadatahandler.getCAInfo(name);   
    }

    public CAInfoView getCAInfo(int caid) throws Exception{
      return cadatahandler.getCAInfo(caid);   
    }    
    
    public void saveRequestInfo(CAInfo cainfo){
    	this.cainfo = cainfo;
    }
    
    public CAInfo getRequestInfo(){
    	return this.cainfo;
    }
    
	public void savePKCS10RequestData(PKCS10CertificationRequest request){
		this.request = request;
	}
    
	public PKCS10CertificationRequest getPKCS10RequestData(){
		return this.request;
	}    
	
	public String getPKCS10RequestDataAsString() throws Exception{
	  String returnval = null;	
	  if(request != null ){
	  						  				  
 	    ByteArrayOutputStream bOut = new ByteArrayOutputStream();
	    DEROutputStream dOut = new DEROutputStream(bOut);
	    dOut.writeObject(request);
	    dOut.close();
	      	  
	    returnval = RequestHelper.BEGIN_CERTIFICATE_REQUEST_WITH_NL
	                   + new String(Base64.encode(bOut.toByteArray()))
                       + RequestHelper.END_CERTIFICATE_REQUEST_WITH_NL;  
	    
	  }      
	  return returnval;
   }
    
   public void saveProcessedCertificate(Certificate cert){
	   this.processedcert =cert;
   }
    
   public Certificate getProcessedCertificate(){
	   return this.processedcert;
   }    
	
   public String getProcessedCertificateAsString() throws Exception{
	 String returnval = null;	
	 if(request != null ){
		byte[] b64cert = se.anatom.ejbca.util.Base64.encode(this.processedcert.getEncoded());
		returnval = RequestHelper.BEGIN_CERTIFICATE_WITH_NL;
		returnval += new String(b64cert);
		returnval += RequestHelper.END_CERTIFICATE_WITH_NL;  	    
	 }      
	 return returnval;
  }
    
    // Private methods

    // Private fields
    private ICertificateStoreSessionLocal      certificatesession;
    private ICAAdminSessionLocal               caadminsession;
    private IAuthorizationSessionLocal         authorizationsession;
    private IUserAdminSessionLocal             adminsession;
    private IRaAdminSessionLocal               raadminsession;
    private ISignSessionLocal                  signsession;
    private IHardTokenSessionLocal             hardtokensession;
    private IPublisherSessionLocal             publishersession;
    private CertificateProfileDataHandler      certificateprofiles;
    private CADataHandler                      cadatahandler;
    private PublisherDataHandler               publisherdatahandler;
    private boolean                            initialized;
    private Admin                              administrator;
    private InformationMemory                  informationmemory;
    private CAInfo                                      cainfo;
    private PKCS10CertificationRequest       request;
    private Certificate	                             processedcert;
    
}
