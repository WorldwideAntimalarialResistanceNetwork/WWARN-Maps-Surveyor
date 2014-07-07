package org.wwarn.mapcore.client.utils;

/*
 * #%L
 * MapCore
 * %%
 * Copyright (C) 2013 - 2014 University of Oxford
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

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;import java.lang.String;

/**
 * Event logger relies on an external javascript implementation to track events across application,
 * in order to use this functionality the following script must be included in the html page
 * <pre>
 * {@code
 * <script type="text/javascript" language="javascript">
      var eventBuffer = [];
      var eventTracker = [];

      window.__gwtStatsEvent = function(event) {
          bufferEvent(event);
          writeBufferedEvents();
          return true;
      };

      function bufferEvent (event) {
          eventBuffer[eventBuffer.length] = event;

          if (event.type == 'begin') {
              var key = event.moduleName + event.subSystem + event.evtGroup;
              eventTracker[key] = event.millis;
          }
      }

      function writeBufferedEvents () {
          if (!isDebugDisplayReady()) return;

          var event = eventBuffer.shift();
          while (event) {
              appendToDebugDisplay(event);
              event = eventBuffer.shift();
          }
      }

      function isDebugDisplayReady () {
          return true;
      }

      function appendToDebugDisplay (event) {
          if (window.console)
              console.log(eventToString(event));
      }

      function eventToString (event) {
          var key = event.moduleName + event.subSystem + event.evtGroup;
          var beginTime = eventTracker[key];
          var ms = '';

          if (event.type != 'begin' && beginTime) {
              ms = ' [' + (event.millis - beginTime) + 'ms]';
          }

          return '[' + event.moduleName + '] ' + event.subSystem + ' - '
                  + event.evtGroup + ' - ' + event.type + ' | '
                  + event.millis + ms;
      }
    </script>
 * }
 * </pre>
 */
public class EventLogger
{
    public static void logEvent (String subsys, String grp, String type)
    {
        logEvent(GWT.getModuleName(), subsys, grp, Duration.currentTimeMillis(), type);
    }

    public static native void logEvent (String module, String subsys,
                                        String grp, double millis, String type)
    /*-{
        if ($wnd.__gwtStatsEvent) {
            $wnd.__gwtStatsEvent({
                'moduleName' : module,
                'subSystem' : subsys,
                'evtGroup' : grp,
                'millis' : millis,
                'type' : type
            });
        }
    }-*/;
}
