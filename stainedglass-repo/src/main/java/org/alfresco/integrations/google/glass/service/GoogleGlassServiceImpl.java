
package org.alfresco.integrations.google.glass.service;


import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.alfresco.integrations.google.glass.GoogleGlassConstants;
import org.alfresco.integrations.google.glass.exceptions.GoogleGlassAuthenticationException;
import org.alfresco.integrations.google.glass.exceptions.GoogleGlassRefreshTokenException;
import org.alfresco.integrations.google.glass.exceptions.GoogleGlassServiceException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.oauth2.OAuth2CredentialsStoreService;
import org.alfresco.service.cmr.remotecredentials.OAuth2CredentialsInfo;
import org.alfresco.service.cmr.remoteticket.NoSuchSystemException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.mirror.Mirror;
import com.google.api.services.mirror.Mirror.Timeline.Insert;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;


public class GoogleGlassServiceImpl
    implements GoogleGlassService
{

    private static final Log              log = LogFactory.getLog(GoogleGlassServiceImpl.class);

    private FileFolderService             fileFolderService;
    private OAuth2CredentialsStoreService oauth2CredentialsStoreService;

    private HttpTransport                 httpTransport;
    private JacksonFactory                jsonFactory;

    private GoogleClientSecrets           clientSecrets;


    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }


    public void setOauth2CredentialsStoreService(OAuth2CredentialsStoreService oauth2CredentialsStoreService)
    {
        this.oauth2CredentialsStoreService = oauth2CredentialsStoreService;
    }


    public void init()
        throws IOException
    {
        httpTransport = new NetHttpTransport();
        jsonFactory = new JacksonFactory();

        clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(GoogleGlassServiceImpl.class.getResourceAsStream("client_secrets.json")));
    }


    private GoogleAuthorizationCodeFlow getFlow()
        throws IOException
    {
        return new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, GoogleGlassConstants.SCOPES).setAccessType("offline").setApprovalPrompt("force").build();
    }


    /**
     * Get a connection to the Google APIs. Will attempt to refresh tokens if they are invalid. If unable to refresh return a
     * GoogleGlassRefreshTokenException.
     * 
     * @return
     * @throws GoogleGlassAuthenticationException
     * @throws GoogleGlassRefreshTokenException
     * @throws GoogleGlassServiceException
     * @throws IOException
     */
    public Credential getCredential()
        throws GoogleGlassAuthenticationException,
            GoogleGlassRefreshTokenException,
            GoogleGlassServiceException,
            IOException
    {
        Credential credential = null;

        // OAuth credentials for the current user, if the exist
        OAuth2CredentialsInfo credentialInfo = oauth2CredentialsStoreService.getPersonalOAuth2Credentials(GoogleGlassConstants.REMOTE_SYSTEM);

        if (credentialInfo != null)
        {
            log.debug("OAuth Access Token Exists: " + credentialInfo.getOAuthAccessToken());

            credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod()).setJsonFactory(jsonFactory).setTransport(httpTransport).setClientAuthentication(new ClientParametersAuthentication(clientSecrets.getDetails().getClientId(), clientSecrets.getDetails().getClientSecret())).setTokenServerEncodedUrl(clientSecrets.getDetails().getTokenUri()).build();
            credential.setAccessToken(credentialInfo.getOAuthAccessToken()).setRefreshToken(credentialInfo.getOAuthRefreshToken()).setExpirationTimeMilliseconds(credentialInfo.getOAuthTicketExpiresAt().getTime());

            try
            {
                log.debug("Attempt to create OAuth Credentials");
                testConnection(credential);
            }
            catch (HttpClientErrorException hcee)
            {
                log.debug(hcee.getResponseBodyAsString());
                if (hcee.getStatusCode().value() == HttpStatus.SC_UNAUTHORIZED)
                {
                    try
                    {
                        credential = refreshAccessToken();
                        testConnection(credential);
                        // credential = new
                        // Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(credentialInfo.getOAuthAccessToken()).setRefreshToken(credentialInfo.getOAuthRefreshToken()).setExpirationTimeMilliseconds(credentialInfo.getOAuthTicketExpiresAt().getTime());
                    }
                    catch (GoogleGlassRefreshTokenException ggrte)
                    {
                        throw ggrte;
                    }
                    catch (GoogleGlassServiceException ggse)
                    {
                        throw ggse;
                    }
                }
                else
                {
                    throw new GoogleGlassServiceException(hcee.getMessage(), hcee, hcee.getStatusCode().value());
                }
            }
            catch (HttpServerErrorException hsee)
            {
                throw new GoogleGlassServiceException(hsee.getMessage(), hsee, hsee.getStatusCode().value());
            }
        }

        log.debug("Credentials Created");
        return credential;
    }


    /**
     * Has the current user authenticated to Google Drive?
     * 
     * @return
     */
    public boolean isAuthenticated()
    {
        boolean authenticated = false;

        OAuth2CredentialsInfo credentialInfo = oauth2CredentialsStoreService.getPersonalOAuth2Credentials(GoogleGlassConstants.REMOTE_SYSTEM);

        if (credentialInfo != null)
        {
            authenticated = true;
        }

        log.debug("Authenticated: " + authenticated);
        return authenticated;
    }


    /**
     * The oauth authentication url
     * 
     * @param state the value of the oauth state parameter to be passed in the authentication url
     * @return The complete oauth authentication url
     * @throws IOException
     */
    public String getAuthenticateUrl(String state)
        throws IOException
    {
        String authenticateUrl = null;

        if (state != null)
        {
            GoogleAuthorizationCodeRequestUrl urlBuilder = getFlow().newAuthorizationUrl().setRedirectUri(GoogleGlassConstants.REDIRECT_URI).setState(state);

            authenticateUrl = urlBuilder.build();
        }

        log.debug("Authentication URL: " + authenticateUrl);
        return authenticateUrl;
    }


    public boolean completeAuthentication(String authorizationCode)
        throws GoogleGlassServiceException,
            IOException
    {
        boolean authenticationComplete = false;

        GoogleTokenResponse response = getFlow().newTokenRequest(authorizationCode).setRedirectUri(GoogleGlassConstants.REDIRECT_URI).execute();

        try
        {
            // If this is a reauth....we may not get back the refresh token. We
            // need to make sure it is persisted across the "refresh".
            if (response.getRefreshToken() == null)
            {
                log.debug("Missing Refresh Token");

                OAuth2CredentialsInfo credentialInfo = oauth2CredentialsStoreService.getPersonalOAuth2Credentials(GoogleGlassConstants.REMOTE_SYSTEM);
                // In the "rare" case that no refresh token is returned and the
                // users credentials are no longer there we need to skip this
                // next check
                if (credentialInfo != null)
                {
                    // If there is a persisted refresh ticket...add it to the
                    // accessGrant so that it is persisted across the update
                    if (credentialInfo.getOAuthRefreshToken() != null)
                    {
                        response.setRefreshToken(credentialInfo.getOAuthRefreshToken());

                        log.debug("Persisting Refresh Token across reauth");
                    }
                }
            }

            oauth2CredentialsStoreService.storePersonalOAuth2Credentials(GoogleGlassConstants.REMOTE_SYSTEM, response.getAccessToken(), response.getRefreshToken(), new Date(response.getExpiresInSeconds()), new Date());

            authenticationComplete = true;
        }
        catch (NoSuchSystemException nsse)
        {
            throw new GoogleGlassServiceException(nsse.getMessage());
        }

        log.debug("Authentication Complete: " + authenticationComplete);

        return authenticationComplete;
    }


    private Credential refreshAccessToken()
        throws GoogleGlassAuthenticationException,
            GoogleGlassRefreshTokenException,
            GoogleGlassServiceException,
            IOException
    {
        log.debug("Refreshing Access Token for " + AuthenticationUtil.getRunAsUser());
        OAuth2CredentialsInfo credentialInfo = oauth2CredentialsStoreService.getPersonalOAuth2Credentials(GoogleGlassConstants.REMOTE_SYSTEM);

        if (credentialInfo.getOAuthRefreshToken() != null)
        {
            Credential credential = null;
            boolean success = false;
            try
            {
                credential = new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(credentialInfo.getOAuthAccessToken()).setRefreshToken(credentialInfo.getOAuthRefreshToken()).setExpirationTimeMilliseconds(credentialInfo.getOAuthTicketExpiresAt().getTime());

                success = credential.refreshToken();
            }
            catch (HttpClientErrorException hcee)
            {
                if (hcee.getStatusCode().value() == HttpStatus.SC_BAD_REQUEST)
                {
                    throw new GoogleGlassAuthenticationException(hcee.getMessage());
                }
                else if (hcee.getStatusCode().value() == HttpStatus.SC_UNAUTHORIZED)
                {
                    throw new GoogleGlassAuthenticationException("Token Refresh Failed.");
                }
                else
                {
                    throw new GoogleGlassServiceException(hcee.getMessage(), hcee.getStatusCode().value());
                }

            }

            if (credential != null && success)
            {
                Date expiresIn = null;

                if (credential.getExpirationTimeMilliseconds() != null)
                {
                    if (credential.getExpirationTimeMilliseconds() > 0L)
                    {
                        expiresIn = new Date(new Date().getTime() + credential.getExpirationTimeMilliseconds());
                    }
                }

                try
                {
                    oauth2CredentialsStoreService.storePersonalOAuth2Credentials(GoogleGlassConstants.REMOTE_SYSTEM, credential.getAccessToken(), credential.getRefreshToken(), expiresIn, new Date());
                }
                catch (NoSuchSystemException nsse)
                {
                    throw nsse;
                }
            }
            else
            {
                throw new GoogleGlassAuthenticationException("No Access Grant Returned.");
            }

            log.debug("Access Token Refreshed");
            return credential;

        }
        else
        {
            throw new GoogleGlassRefreshTokenException("No Refresh Token Provided for " + AuthenticationUtil.getRunAsUser());
        }
    }


    private void testConnection(Credential credential)
        throws GoogleGlassServiceException
    {
        Oauth2 userInfoService = new Oauth2.Builder(new NetHttpTransport(), new JacksonFactory(), credential).build();
        Userinfoplus userInfo = null;
        try
        {
            userInfo = userInfoService.userinfo().get().execute();
        }
        catch (IOException e)
        {
            throw new GoogleGlassServiceException("Error creating Connection: " + e.getMessage());
        }
        if (userInfo == null || userInfo.getId() == null)
        {
            throw new GoogleGlassServiceException("Error creating Connection: No user");
        }
    }


    private Mirror getMirrorApi(Credential credential)
    {
        log.debug("Initiating Google Mirror Service");
        return new Mirror.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName(GoogleGlassConstants.APPLICATION_NAME).build();
    }


    public void sendMessage(Credential credential, String message)
        throws GoogleGlassAuthenticationException,
            GoogleGlassRefreshTokenException,
            GoogleGlassServiceException,
            IOException
    {
        if (credential == null)
        {
            credential = getCredential();
        }

        Mirror mirror = getMirrorApi(credential);

        TimelineItem item = new TimelineItem();
        item.setSpeakableText(message);

        Insert insert = mirror.timeline().insert(item);
        insert.execute();
    }


    public void uploadContent(Credential credential, NodeRef nodeRef)
        throws GoogleGlassAuthenticationException,
            GoogleGlassRefreshTokenException,
            GoogleGlassServiceException,
            IOException
    {
        if (credential == null)
        {
            credential = getCredential();
        }

        Mirror mirror = getMirrorApi(credential);

        TimelineItem item = new TimelineItem();
        item.setTitle(fileFolderService.getFileInfo(nodeRef).getName());

        Insert insert = mirror.timeline().insert(item, new InputStreamContent(fileFolderService.getFileInfo(nodeRef).getContentData().getMimetype(), fileFolderService.getReader(nodeRef).getContentInputStream()));
        insert.execute();

    }
}
