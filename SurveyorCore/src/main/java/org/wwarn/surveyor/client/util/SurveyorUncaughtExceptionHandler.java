package org.wwarn.surveyor.client.util;

/*
 * #%L
 * SurveyorCore
 * %%
 * Copyright (C) 2013 - 2015 University of Oxford
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the University of Oxford nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 */
public class SurveyorUncaughtExceptionHandler implements GWT.UncaughtExceptionHandler
{
    private static Logger logger = Logger.getLogger("Surveyor.SurveyorUncaughtExceptionHandler");

    private static final String newline = " ";//System.getProperty("line.separator").toCharArray()[0];//"&lt;br /&gt;"

    private static final String POP_UP_ERROR_MESSAGE = "An unexpected error has occurred;" +
            " reloading the page should fix the problem.";
    private static final String TIMEOUT_ERROR_MESSAGE = "A timeout error has occurred. Repeating your last action should fix this." +
            " If the problem persists, please check your internet connection.";
    private static final String POP_UP_TECHNICAL_ERROR_MESSAGE_1 = "An unexpected error has occurred." +
            newline + "Exception message:" + newline;
    private static final String POP_UP_TECHNICAL_ERROR_MESSAGE_2 = " Stack trace as follows:" + newline;
    public static boolean alertMsg = true;

    @Override
    public void onUncaughtException(Throwable e)
    {
        // a user friendly error message to display if errors are encountered
        String errorMessage = POP_UP_ERROR_MESSAGE + e.getLocalizedMessage();

        // if GWT is not in script mode, it is in hosted mode - i.e. it is running via the GWT Dev Manager
        // - so can display more technical error info
        if (! GWT.isScript())
        {
            errorMessage = new StringBuilder().append(POP_UP_TECHNICAL_ERROR_MESSAGE_1).
                    append(e.getMessage()).append(POP_UP_TECHNICAL_ERROR_MESSAGE_2).
                    append(printStackTrace(e.getStackTrace())).toString();
        }

        // If an IllegalStateException is thrown, the app is now in an unstable state: show the error message in the loading screen - to
        // block any access to functionality until a page reload is done
        if (e instanceof IllegalStateException)
        {

            if (e.getCause().getClass() == TimedOutException.class)
            {

                errorMessage = TIMEOUT_ERROR_MESSAGE;

            }
            else
            {
                final String loading_screen = "loading_screen";
                DOM.setStyleAttribute(RootPanel.get(loading_screen).getElement(), "display", "block");
                DOM.setInnerHTML(RootPanel.get(loading_screen).getElement(), errorMessage);
            }

        }

        // log the error to the GWT log
            logger.log(Level.SEVERE, errorMessage);
        if (alertMsg) {
            Window.alert(errorMessage);
        }
    }


    /**
     * Given a stack trace, turn it into a HTML formatted string - to improve its display
     *
     * @param stackTrace - stack trace to convert to string
     * @return String with stack trace formatted with HTML line breaks
     */
    private String printStackTrace(Object[] stackTrace) {
        StringBuilder output = new StringBuilder();
        for (Object line : stackTrace) {
            output.append(line);
            output.append(newline);
        }
        return output.toString();
    }

}
