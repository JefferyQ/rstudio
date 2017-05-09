/*
 * EventTracker.java
 *
 * Copyright (C) 2009-17 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client;

import org.rstudio.core.client.JsVector;
import org.rstudio.core.client.widget.MiniPopupPanel;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Singleton;

// This class maintains a simple JavaScript event history, primarily to be used
// by classes that need to know what sequence of unfortunate events might have
// led to the current application state (whatever that might be).

@Singleton
public class JavaScriptEventHistory
{
   public static class EventData extends JavaScriptObject
   {
      protected EventData()
      {
      }
      
      public static final native EventData create(NativeEvent event)
      /*-{
         return {
            "type"   : event.type,
            "button" : event.button
         };
      }-*/;
      
      public final native String getType() /*-{ return this["type"];   }-*/;
      public final native int getButton()  /*-{ return this["button"]; }-*/;
   }
   
   public interface Predicate
   {
      public boolean accept(EventData event);
   }
   
   public JavaScriptEventHistory()
   {
      queue_ = JavaScriptObject.createArray().cast();
      registerEventListeners();
   }
   
   private final native void registerEventListeners()
   /*-{
      var self = this;
      
      // get reference to document body
      var document = $doc;
      var body = document.body;
      console.log(body);
      
      // define our handler (we can just use a single one)
      var handler = $entry(function(event) {
         self.@org.rstudio.studio.client.JavaScriptEventHistory::onEvent(Lcom/google/gwt/dom/client/NativeEvent;)(event);
      });
      
      // iterate through all keys on body object, and
      // register handlers for any prefixed with 'on'
      for (var key in body) {
         
         // skip keys that don't have an 'on' prefix
         if (key.indexOf("on") != 0)
            continue;
            
         // extract event name and attach handler
         var event = key.slice(2);
         body.addEventListener(event, handler)
      }
      
   }-*/;
   
   private void onEvent(NativeEvent event)
   {
      EventData eventData = EventData.create(event);
      queue_.unshift(eventData);
      queue_.setLength(QUEUE_LENGTH);

      // toggle this if you want to see events as they're emitted real-time
      debugHistory();
   }
   
   public EventData findEvent(Predicate predicate)
   {
      for (int i = 0, n = queue_.length(); i < n; i++)
         if (predicate.accept(queue_.get(i)))
            return queue_.get(i);
      return null;
   }
   
   // primarily for debug use (see what events are being emitted over time)
   private MiniPopupPanel historyPanel_ = null;
   public void debugHistory()
   {
      if (historyPanel_ == null)
         historyPanel_ = new MiniPopupPanel(false, false);
      
      VerticalPanel contentPanel = new VerticalPanel();
      contentPanel.add(new HTML("<h4 style='margin: 0;'>JavaScript Event History</h2><hr />"));
      for (int i = 0, n = Math.min(10, queue_.length()); i < n; i++)
      {
         EventData event = queue_.get(i);
         contentPanel.add(new HTML("Event: " + event.getType()));
      }
      
      historyPanel_.setWidget(contentPanel);
      if (!historyPanel_.isShowing())
      {
         historyPanel_.setPopupPosition(10, 10);
         historyPanel_.show();
      }
   }
   
   private final JsVector<EventData> queue_;
   
   private static final int QUEUE_LENGTH = 10;
}
