
package org.alfresco.integrations.google.glass.webscripts;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.integrations.google.glass.service.GoogleGlassService;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;


public class AuthUrl
    extends DeclarativeWebScript
{
    private static final Log    log                 = LogFactory.getLog(AuthUrl.class);

    private final static String MODEL_AUTHURL       = "authURL";
    private final static String MODEL_AUTHENTICATED = "authenticated";

    private final static String PARAM_STATE         = "state";
    private final static String PARAM_OVERRIDE      = "override";


    private GoogleGlassService  googleGlassService;


    public void setGoogleGlassService(GoogleGlassService googleGlassService)
    {
        this.googleGlassService = googleGlassService;
    }


    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        boolean authenticated = false;

        if (!Boolean.valueOf(req.getParameter(PARAM_OVERRIDE)))
        {
            if (googleGlassService.isAuthenticated())
            {
                authenticated = true;
            }
            else
            {
                try
                {
                    model.put(MODEL_AUTHURL, googleGlassService.getAuthenticateUrl(req.getParameter(PARAM_STATE)));
                    
                    log.debug("Authenticated: " + authenticated + "; AuthUrl: "
                            + ((model.containsKey(MODEL_AUTHURL)) ? model.get(MODEL_AUTHURL) : ""));
                }
                catch (IOException ioe)
                {
                    throw new WebScriptException(HttpStatus.SC_INTERNAL_SERVER_ERROR, ioe.getMessage());
                }
            }


        }
        else
        {
            try
            {
                model.put(MODEL_AUTHURL, googleGlassService.getAuthenticateUrl(req.getParameter(PARAM_STATE)));
                
                authenticated = googleGlassService.isAuthenticated();
                log.debug("Forced AuthURL. AuthUrl: " + model.get(MODEL_AUTHURL) + "; Authenticated: " + authenticated);
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
