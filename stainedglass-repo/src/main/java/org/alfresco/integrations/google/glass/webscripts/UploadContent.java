/**
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 * 
 * This file is part of Alfresco
 * 
 * Alfresco is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with Alfresco. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package org.alfresco.integrations.google.glass.webscripts;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.integrations.google.glass.exceptions.GoogleGlassAuthenticationException;
import org.alfresco.integrations.google.glass.exceptions.GoogleGlassRefreshTokenException;
import org.alfresco.integrations.google.glass.exceptions.GoogleGlassServiceException;
import org.alfresco.integrations.google.glass.service.GoogleGlassService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * @author Jared Ottley <jared.ottley@alfresco.com>
 */
public class UploadContent
    extends DeclarativeWebScript
{
    private static final Log    log           = LogFactory.getLog(UploadContent.class);

    private GoogleGlassService  googleGlassService;

    private static final String PARAM_NODEREF = "nodeRef";
    private static final String MODEL_NODEREF = "nodeRef";


    public void setGoogleGlassService(GoogleGlassService googleGlassService)
    {
        this.googleGlassService = googleGlassService;
    }


    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        String param_nodeRef = req.getParameter(PARAM_NODEREF);
        NodeRef nodeRef = new NodeRef(param_nodeRef);

        try
        {
            googleGlassService.uploadContent(googleGlassService.getCredential(), nodeRef);
        }
        catch (GoogleGlassAuthenticationException ggae)
        {
            throw new WebScriptException(HttpStatus.SC_BAD_GATEWAY, ggae.getMessage());
        }
        catch (GoogleGlassServiceException ggse)
        {
            if (ggse.getPassedStatusCode() > -1)
            {
                throw new WebScriptException(ggse.getPassedStatusCode(), ggse.getMessage());
            }
            else
            {
                throw new WebScriptException(ggse.getMessage());
            }
        }
        catch (GoogleGlassRefreshTokenException ggrte)
        {
            throw new WebScriptException(HttpStatus.SC_BAD_GATEWAY, ggrte.getMessage());
        }
        catch (IOException ioe)
        {
            throw new WebScriptException(HttpStatus.SC_INTERNAL_SERVER_ERROR, ioe.getMessage(), ioe);
        }
        catch (Exception e)
        {
            throw new WebScriptException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }

        model.put(MODEL_NODEREF, nodeRef.toString());

        return model;
    }
}
