/*
 * Generated by XDoclet - Do not edit!
 */
package se.anatom.ejbca.authorization;

/**
 * Local interface for AuthorizationTreeUpdateData.
 */
public interface AuthorizationTreeUpdateDataLocal
   extends javax.ejb.EJBLocalObject
{
   /**
    * Method returning the newest authorizationtreeupdatenumber. Should be used after each time the authorization tree is built.
    * @return the newest accessruleset number.
    */
   public int getAuthorizationTreeUpdateNumber(  ) ;

   /**
    * Method used check if a reconstruction of authorization tree is needed in the authorization beans. It is used to avoid desyncronisation of authorization structures in a distibuted environment.
    * @param currentauthorizationtreeupdatenumber indicates which authorizationtreeupdatenumber is currently used.
    * @return true if update is needed.
    */
   public boolean updateNeccessary( int currentauthorizationtreeupdatenumber ) ;

   /**
    * Method incrementing the authorizationtreeupdatenumber and thereby signaling to other beans that they should reconstruct their accesstrees.
    */
   public void incrementAuthorizationTreeUpdateNumber(  ) ;

}
