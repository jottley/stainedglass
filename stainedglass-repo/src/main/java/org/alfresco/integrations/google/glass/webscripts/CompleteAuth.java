
package org.alfresco.integrations.google.glass.webscripts;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.integrations.google.glass.exceptions.GoogleGlassServiceException;
import org.alfresco.integrations.google.glass.service.GoogleGlassService;
import org.apache.commons.httpclient.HttpStatus;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;


public class CompleteAuth
    extends DeclarativeWebScript
{
    private GoogleGlassService  googleGlassService;

    private final static String PARAM_ACCESS_TOKEN  = "access_token";

    private final static String MODEL_AUTHENTICATED = "authenticated";


    public void setGoogleGlassService(GoogleGlassService googleGlassService)
    {
        this.googleGlassService = googleGlassService;
    }


    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        boolean authenticated = false;

        if (req.getParameter(PARAM_ACCESS_TOKEN) != null)
        {
            try
            {
                authenticated = googleGlassService.completeAuthentication(req.getParameter(PARAM_ACCESS_TOKEN));
            }
            catch (GoogleGlassServiceException gdse)
            {
                throw new WebScriptException(HttpStatus.SC_INTERNAL_SERVER_ERROR, gdse.getMessage());
            }
            catch (IOException ioe)
            {
                throw new WebScriptException(HttpStatus.SC_INTERNAL_SERVER_ERROR, ioe.getMessage());
            }
        }

        model.put(MODEL_AUTHENTICATED, authenticated);

        return model;
    }
}
