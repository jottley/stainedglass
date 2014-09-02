
package org.alfresco.integrations.google.glass.service;


import java.io.IOException;

import org.alfresco.integrations.google.glass.exceptions.GoogleGlassAuthenticationException;
import org.alfresco.integrations.google.glass.exceptions.GoogleGlassRefreshTokenException;
import org.alfresco.integrations.google.glass.exceptions.GoogleGlassServiceException;
import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;

import com.google.api.client.auth.oauth2.Credential;


public interface GoogleGlassService
{
    @Auditable
    public Credential getCredential()
        throws GoogleGlassAuthenticationException,
            GoogleGlassRefreshTokenException,
            GoogleGlassServiceException,
            IOException;


    /**
     * Has the current user completed OAuth2 authentication against Google Docs
     * 
     * @return
     */
    @Auditable
    public boolean isAuthenticated();


    /**
     * Build the OAuth2 URL needed to authenticate the current user against Google Docs
     * 
     * @param state
     * @return
     */
    @Auditable(parameters = { "state" })
    public String getAuthenticateUrl(String state)
        throws IOException;


    /**
     * Complete the OAuth2 Dance for the current user, persisting the OAuth2 Tokens.
     * 
     * @param access_token
     * @return
     */
    @Auditable
    public boolean completeAuthentication(String access_token)
        throws GoogleGlassServiceException,
            IOException;


    @Auditable(parameters = { "message" })
    public void sendMessage(Credential credential, String message)
        throws GoogleGlassAuthenticationException,
            GoogleGlassRefreshTokenException,
            GoogleGlassServiceException,
            IOException;
    
    @Auditable(parameters = { "nodeRef" })
    public void uploadContent(Credential credential, NodeRef nodeRef)
            throws GoogleGlassAuthenticationException,
                GoogleGlassRefreshTokenException,
                GoogleGlassServiceException,
                IOException;

}
