/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.siri.core.handlers;

import org.onebusaway.siri.core.SiriServer;

import uk.org.siri.siri.SubscriptionRequest;

/**
 * Interface for handling an incoming SIRI {@link SubscriptionRequest}, allowing
 * the implementor to take special action.
 * 
 * @author bdferris
 * @see SiriServer#addSubscriptionRequestHandler(SiriSubscriptionRequestHandler)
 */
public interface SiriSubscriptionRequestHandler {

  public void handleSubscriptionRequest(SubscriptionRequest request);
}
