/**
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 * 
 * This file is part of Alfresco
 * 
 * Alfresco is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * Alfresco is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Google Glass Document Library actions. Defines JS actions for documents, as well as for folders via
 * the Create Content menu.
 * 
 * @author jottley
 * @author wabson
 */
(function() {
   
   /*
    * YUI aliases
    */
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event,
      KeyListener = YAHOO.util.KeyListener;
   
   /**
    * Forward the browser to the editing page for the specified repository nodeRef
    * 
    * @param nodeRef {String} NodeRef of the item being edited
    * @returns null
    */
   var navigateToEditorPage = function GGA_navigateToEditorPage(nodeRef)
   {
      var returnPath = location.pathname.replace(Alfresco.constants.URL_PAGECONTEXT, "") + location.search + location.hash;
      Alfresco.util.navigateTo(Alfresco.util.siteURL("googledocsEditor?nodeRef=" + encodeURIComponent(nodeRef) + "&return=" + encodeURIComponent(returnPath), {
         site: Alfresco.constants.SITE
      }, true));
   };
   


   /**
    * Publish an existing document to Google Glass
    *
    * @method onGoogleglassActionPublish
    * @param record {object} Object literal representing the file or folder on which the work should be performed
    */
   YAHOO.Bubbling.fire("registerAction", {
      actionName : "onGoogleglassActionPublish",
      fn : function dlA_onGoogleglassActionPublish(record) {
         
         var me = this;
         
         Alfresco.GoogleGlass.showMessage({
            text: this.msg("googleglass.actions.publishing"), 
            displayTime: 0,
            showSpinner: true
         });
         
         var publishDocument = function Googleglass_publishDocument() {
            Alfresco.GoogleGlass.showMessage({
               text: this.msg("googleglass.actions.publishing"), 
               displayTime: 0,
               showSpinner: true
            });

            Alfresco.GoogleGlass.request.call(this, {
               url: Alfresco.constants.PROXY_URI + 'googleglass/uploadContent?nodeRef=' + record.nodeRef,
               method: "POST",
               requestContentType: Alfresco.util.Ajax.JSON,
               successCallback:
               {
                  fn : function(response)
                  {
                	  Alfresco.GoogleGlass.hideMessage();
                  },
                  scope : this
               },
               failureCallback:
               {
                  fn: function(response)
                  {
                     if (response.serverResponse.status == 503)
                     {
                        Alfresco.util.PopupManager.displayPrompt(
                        {   
                           title: this.msg("googleglass.disabled.title"),
                   	       text: this.msg("googleglass.disabled.text"),
                   	       noEscape: true,
                   	       buttons: [
                   	       {
                              text: this.msg("button.ok"),
                   	          handler: function submitDiscard()
                   	          {
                   	  		     // Close the confirmation pop-up
                   	  			 Alfresco.GoogleGlass.hideMessage();
                   	  			 this.destroy();
                   	  		   },
                   	  		   isDefault: true
                   	  		}]
                   	    }); 
                     }
                     else
                     {
                	    Alfresco.GoogleGlass.showMessage({
                           text: this.msg("googleglass.actions.publishing.failure"), 
                           displayTime: 2.5,
                           showSpinner: false
                        });
                     }
                  },
                  scope: this
               }
            });
         };
         
         Alfresco.GoogleGlass.requestOAuthURL.call(this, {
            nodeRef: record.nodeRef,
            onComplete: {
               fn: function(authResp) { // Auth resp contains the OAuth URL to use for publishing (not needed here) and the permissions
                  Alfresco.GoogleGlass.checkGoogleLogin.call(this, {
                     onLoggedIn: {
                        fn: function() {
                        	 publishDocument.call(me);
                        },
                        scope: this
                     }
                  });
               },
               scope: this
            }
         });
      }
   })
   
})();