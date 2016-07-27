/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.siri.core;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.siri.core.exceptions.SiriException;
import org.onebusaway.siri.core.exceptions.SiriMissingArgumentException;
import org.onebusaway.siri.core.exceptions.SiriUnknownVersionException;
import org.onebusaway.siri.core.versioning.ESiriVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.siri.siri.AbstractServiceRequestStructure;
import uk.org.siri.siri.AbstractSubscriptionStructure;
import uk.org.siri.siri.CheckStatusRequestStructure;
import uk.org.siri.siri.DirectionRefStructure;
import uk.org.siri.siri.EstimatedTimetableRequestStructure;
import uk.org.siri.siri.EstimatedTimetableSubscriptionStructure;
import uk.org.siri.siri.LineRefStructure;
import uk.org.siri.siri.MessageQualifierStructure;
import uk.org.siri.siri.MonitoringRefStructure;
import uk.org.siri.siri.ProductionTimetableRequestStructure;
import uk.org.siri.siri.ProductionTimetableSubscriptionRequest;
import uk.org.siri.siri.ServiceRequest;
import uk.org.siri.siri.Siri;
import uk.org.siri.siri.SituationExchangeRequestStructure;
import uk.org.siri.siri.SituationExchangeSubscriptionStructure;
import uk.org.siri.siri.StopMonitoringRequestStructure;
import uk.org.siri.siri.StopMonitoringSubscriptionStructure;
import uk.org.siri.siri.StopTimetableRequestStructure;
import uk.org.siri.siri.StopTimetableSubscriptionStructure;
import uk.org.siri.siri.SubscriptionQualifierStructure;
import uk.org.siri.siri.SubscriptionRequest;
import uk.org.siri.siri.TerminateSubscriptionRequestStructure;
import uk.org.siri.siri.VehicleMonitoringRefStructure;
import uk.org.siri.siri.VehicleMonitoringRequestStructure;
import uk.org.siri.siri.VehicleMonitoringSubscriptionStructure;
import uk.org.siri.siri.VehicleRefStructure;

public class SiriClientRequestFactory {

  private static Logger _log = LoggerFactory.getLogger(SiriClientRequestFactory.class);

  public static final String ARG_URL = "Url";
  public static final String ARG_MANAGE_SUBSCRIPTION_URL = "ManageSubscriptionUrl";
  public static final String ARG_CHECK_STATUS_URL = "CheckStatusUrl";

  public static final String ARG_VERSION = "Version";
  public static final String ARG_MODULE_TYPE = "ModuleType";
  public static final String ARG_SUBSCRIBE = "Subscribe";
  public static final String ARG_POLL_INTERVAL = "PollInterval";

  public static final String ARG_RECONNECTION_ATTEMPTS = "ReconnectionAttempts";
  public static final String ARG_RECONNECTION_INTERVAL = "ReconnectionInterval";

  public static final String ARG_HEARTBEAT_INTERVAL = "HeartbeatInterval";
  public static final String ARG_CHECK_STATUS_INTERVAL = "CheckStatusInterval";
  public static final String ARG_INITIAL_TERMINATION_TIME = "InitialTerminationTime";

  public static final String ARG_MESSAGE_IDENTIFIER = "MessageIdentifier";
  public static final String ARG_SUBSCRIPTION_IDENTIFIER = "SubscriptionIdentifier";
  public static final String ARG_MAXIMUM_VEHICLES = "MaximumVehicles";
  public static final String ARG_VEHICLE_REF = "VehicleRef";
  public static final String ARG_LINE_REF = "LineRef";
  public static final String ARG_DIRECTION_REF = "DirectionRef";
  public static final String ARG_VEHICLE_MONITORING_REF = "VehicleMonitoringRef";

  public static final String ARG_CHANGE_BEFORE_UPDATES = "ChangeBeforeUpdates";
  public static final String ARG_INCREMENTAL_UPDATES = "IncrementalUpdates";
  public static final String ARG_PREVIEW_INTERVAL = "PreviewInterval";

  public static final String ARG_MONITORING_REF = "MonitoringRef";

  private static final DatatypeFactory _dataTypeFactory = SiriTypeFactory.createDataTypeFactory();

  public SiriClientRequest createRequest(Map<String, String> args) {
    SiriClientRequest request = new SiriClientRequest();
    processCommonArgs(args, request);
    if (request.isSubscribe()) {
      processSubscriptionRequestArgs(args, request);
    } else {
      processServiceRequestArgs(args, request);
    }
    return request;
  }

  public SiriClientRequest createServiceRequest(Map<String, String> args) {
    SiriClientRequest request = new SiriClientRequest();
    processCommonArgs(args, request);
    processServiceRequestArgs(args, request);
    return request;
  }

  public SiriClientRequest createSubscriptionRequest(Map<String, String> args) {
    SiriClientRequest request = new SiriClientRequest();
    processCommonArgs(args, request);
    processSubscriptionRequestArgs(args, request);
    return request;
  }

  public SiriClientRequest createCheckStatusRequest(Map<String, String> args) {

    SiriClientRequest request = new SiriClientRequest();
    processCommonArgs(args, request);

    CheckStatusRequestStructure checkStatusRequest = new CheckStatusRequestStructure();

    Siri payload = new Siri();
    payload.setCheckStatusRequest(checkStatusRequest);
    request.setPayload(payload);

    return request;
  }

  public SiriClientRequest createTerminateSubscriptionRequest(
      Map<String, String> args) {

    SiriClientRequest request = new SiriClientRequest();
    processCommonArgs(args, request);

    TerminateSubscriptionRequestStructure terminateRequest = new TerminateSubscriptionRequestStructure();
    Siri payload = new Siri();
    payload.setTerminateSubscriptionRequest(terminateRequest);
    request.setPayload(payload);

    String messageIdentifierValue = args.get(ARG_MESSAGE_IDENTIFIER);
    if (messageIdentifierValue != null) {
      MessageQualifierStructure messageIdentifier = new MessageQualifierStructure();
      messageIdentifier.setValue(messageIdentifierValue);
      terminateRequest.setMessageIdentifier(messageIdentifier);
    }

    String subscriptionIdentifierValue = args.get("SubscriptionIdentifier");
    if (subscriptionIdentifierValue != null) {
      SubscriptionQualifierStructure value = new SubscriptionQualifierStructure();
      value.setValue(subscriptionIdentifierValue);
      terminateRequest.getSubscriptionRef().add(value);
    } else {
      terminateRequest.setAll("true");
    }

    return request;
  }

  /****
   * Private Methods
   ****/

  private void processCommonArgs(Map<String, String> args,
      SiriClientRequest request) {

    String url = args.get(ARG_URL);
    if (url == null)
      throw new SiriMissingArgumentException(ARG_URL);
    request.setTargetUrl(url);

    String manageSubscriptionUrl = args.get(ARG_MANAGE_SUBSCRIPTION_URL);
    request.setManageSubscriptionUrl(manageSubscriptionUrl);

    String checkStatusUrl = args.get(ARG_CHECK_STATUS_URL);
    request.setCheckStatusUrl(checkStatusUrl);

    String versionId = args.get(ARG_VERSION);
    if (versionId != null) {
      ESiriVersion version = ESiriVersion.getVersionForVersionId(versionId);
      if (version == null) {
        throw new SiriUnknownVersionException(versionId);
      }
      request.setTargetVersion(version);
    } else {
      request.setTargetVersion(ESiriVersion.V1_3);
    }

    String subscribeValue = args.get(ARG_SUBSCRIBE);
    if (subscribeValue != null) {
      boolean subscribe = Boolean.parseBoolean(subscribeValue);
      request.setSubscribe(subscribe);
    }

    String pollIntervalValue = args.get(ARG_POLL_INTERVAL);
    if (pollIntervalValue != null) {
      int pollInterval = Integer.parseInt(pollIntervalValue);
      request.setPollInterval(pollInterval);
    }

    String initialTerminationTime = args.get(ARG_INITIAL_TERMINATION_TIME);
    if (initialTerminationTime != null) {
      if (initialTerminationTime.startsWith("P")) {
        Duration duration = _dataTypeFactory.newDuration(initialTerminationTime);
        request.setInitialTerminationDuration(duration.getTimeInMillis(new Date()));
      } else {
        try {
          Date time = getIso8601StringAsTime(initialTerminationTime,
              TimeZone.getDefault());
          request.setInitialTerminationDuration(time.getTime()
              - System.currentTimeMillis());
        } catch (ParseException e) {
          throw new SiriException(
              "error parsing initial termination time (ISO 8601)");
        }
      }
    } else {
      /**
       * By default, expire in 24 hours
       */
      Calendar c = Calendar.getInstance();
      c.add(Calendar.DAY_OF_YEAR, 1);
      request.setInitialTerminationDuration(c.getTimeInMillis()
          - System.currentTimeMillis());
    }

    String reconnectionAttempts = args.get(ARG_RECONNECTION_ATTEMPTS);
    if (reconnectionAttempts != null) {
      int attempts = Integer.parseInt(reconnectionAttempts);
      request.setReconnectionAttempts(attempts);
    }

    String reconnectionInterval = args.get(ARG_RECONNECTION_INTERVAL);
    if (reconnectionInterval != null) {
      int interval = Integer.parseInt(reconnectionInterval);
      request.setReconnectionInterval(interval);
    }

    String checkStatusIntervalValue = args.get(ARG_CHECK_STATUS_INTERVAL);
    if (checkStatusIntervalValue != null) {
      int checkStatusInterval = Integer.parseInt(checkStatusIntervalValue);
      request.setCheckStatusInterval(checkStatusInterval);
    }

    String heartbeatIntervalValue = args.get(ARG_HEARTBEAT_INTERVAL);
    if (heartbeatIntervalValue != null) {
      int heartbeatInterval = Integer.parseInt(heartbeatIntervalValue);
      request.setHeartbeatInterval(heartbeatInterval);
    }
  }

  private void processServiceRequestArgs(Map<String, String> args,
      SiriClientRequest request) {
    ServiceRequest serviceRequest = new ServiceRequest();
    Siri payload = new Siri();
    payload.setServiceRequest(serviceRequest);
    request.setPayload(payload);

    String messageIdentifierValue = args.get(ARG_MESSAGE_IDENTIFIER);
    if (messageIdentifierValue != null) {
      MessageQualifierStructure messageIdentifier = new MessageQualifierStructure();
      messageIdentifier.setValue(messageIdentifierValue);
      serviceRequest.setMessageIdentifier(messageIdentifier);
    }

    String moduleTypeValue = args.get(ARG_MODULE_TYPE);

    if (moduleTypeValue != null) {

      ESiriModuleType moduleType = ESiriModuleType.valueOf(moduleTypeValue.toUpperCase());
      AbstractServiceRequestStructure moduleRequest = createServiceRequestForModuleType(moduleType);

      handleModuleServiceRequestSpecificArguments(moduleType, moduleRequest,
          args);

      List<AbstractServiceRequestStructure> moduleRequests = SiriLibrary.getServiceRequestsForModule(
          serviceRequest, moduleType);
      moduleRequests.add(moduleRequest);
    }
  }

  private AbstractServiceRequestStructure createServiceRequestForModuleType(
      ESiriModuleType moduleType) {

    switch (moduleType) {
      case PRODUCTION_TIMETABLE:
        return new ProductionTimetableRequestStructure();
      case ESTIMATED_TIMETABLE:
        return new EstimatedTimetableRequestStructure();
      case STOP_TIMETABLE:
        return new StopTimetableRequestStructure();
      case STOP_MONITORING:
        return new StopMonitoringRequestStructure();
      case VEHICLE_MONITORING:
        return new VehicleMonitoringRequestStructure();
      case SITUATION_EXCHANGE:
        return new SituationExchangeRequestStructure();
      default:
        throw new UnsupportedOperationException();
    }
  }

  private void processSubscriptionRequestArgs(Map<String, String> args,
      SiriClientRequest request) {
    SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
    Siri payload = new Siri();
    payload.setSubscriptionRequest(subscriptionRequest);
    request.setPayload(payload);

    String messageIdentifierValue = args.get(ARG_MESSAGE_IDENTIFIER);
    if (messageIdentifierValue != null) {
      MessageQualifierStructure messageIdentifier = new MessageQualifierStructure();
      messageIdentifier.setValue(messageIdentifierValue);
      subscriptionRequest.setMessageIdentifier(messageIdentifier);
    }

    String moduleTypeValue = args.get(ARG_MODULE_TYPE);

    if (moduleTypeValue != null) {

      ESiriModuleType moduleType = ESiriModuleType.valueOf(moduleTypeValue.toUpperCase());
      AbstractSubscriptionStructure moduleSubscription = createSubscriptionForModuleType(moduleType);

      String subscriptionIdentifierValue = args.get(ARG_SUBSCRIPTION_IDENTIFIER);
      if (subscriptionIdentifierValue != null) {
        SubscriptionQualifierStructure value = new SubscriptionQualifierStructure();
        value.setValue(subscriptionIdentifierValue);
        moduleSubscription.setSubscriptionIdentifier(value);
      }

      handleModuleSubscriptionSpecificArguments(moduleType, moduleSubscription,
          args);

      List<AbstractSubscriptionStructure> moduleSubscriptions = SiriLibrary.getSubscriptionRequestsForModule(
          subscriptionRequest, moduleType);
      moduleSubscriptions.add(moduleSubscription);
    }
  }

  private AbstractSubscriptionStructure createSubscriptionForModuleType(
      ESiriModuleType moduleType) {

    switch (moduleType) {
      case PRODUCTION_TIMETABLE:
        return new ProductionTimetableSubscriptionRequest();
      case ESTIMATED_TIMETABLE:
        return new EstimatedTimetableSubscriptionStructure();
      case STOP_TIMETABLE:
        return new StopTimetableSubscriptionStructure();
      case STOP_MONITORING:
        return new StopMonitoringSubscriptionStructure();
      case VEHICLE_MONITORING:
        return new VehicleMonitoringSubscriptionStructure();
      case SITUATION_EXCHANGE:
        return new SituationExchangeSubscriptionStructure();
      default:
        throw new UnsupportedOperationException();
    }
  }

  private void handleModuleServiceRequestSpecificArguments(
      ESiriModuleType moduleType,
      AbstractServiceRequestStructure moduleServiceRequest,
      Map<String, String> args) {

    switch (moduleType) {
      case VEHICLE_MONITORING:
        applyArgsToVehicleMonitoringRequest(
            (VehicleMonitoringRequestStructure) moduleServiceRequest, args);
        break;
      case SITUATION_EXCHANGE:
        applyArgsToSituationExchangeRequest(
            (SituationExchangeRequestStructure) moduleServiceRequest, args);
        break;
      case STOP_MONITORING:
          applyArgsToStopMonitoringRequest(
              (StopMonitoringRequestStructure) moduleServiceRequest, args);
          break;
    }
  }



private void handleModuleSubscriptionSpecificArguments(
      ESiriModuleType moduleType,
      AbstractSubscriptionStructure moduleSubscription, Map<String, String> args) {

    switch (moduleType) {
      case VEHICLE_MONITORING:
        handleVehicleMonitoringSubscriptionSpecificArguments(
            (VehicleMonitoringSubscriptionStructure) moduleSubscription, args);
        break;
      case SITUATION_EXCHANGE:
        handleSituationExchangeSubscriptionSpecificArguments(
            (SituationExchangeSubscriptionStructure) moduleSubscription, args);
        break;
      case STOP_MONITORING:
          handleStopMonitoringSubscriptionSpecificArguments(
              (StopMonitoringSubscriptionStructure) moduleSubscription, args);
          break;
    }
  }


private void handleVehicleMonitoringSubscriptionSpecificArguments(
      VehicleMonitoringSubscriptionStructure moduleSubscription,
      Map<String, String> args) {

      VehicleMonitoringRequestStructure vmr = new VehicleMonitoringRequestStructure();
      moduleSubscription.setVehicleMonitoringRequest(vmr);

      String changeBeforeUpdates = args.get(ARG_CHANGE_BEFORE_UPDATES);
	  if (changeBeforeUpdates != null) {
		  if (changeBeforeUpdates.startsWith("PT")) {
			  moduleSubscription.setChangeBeforeUpdates(_dataTypeFactory.newDuration(changeBeforeUpdates));
		  } else {
			  if(StringUtils.isNumeric(changeBeforeUpdates)) {
				  moduleSubscription.setChangeBeforeUpdates(_dataTypeFactory.newDuration("P" + changeBeforeUpdates + "S"));
			  } else {
				  _log.warn("value for " + ARG_CHANGE_BEFORE_UPDATES + " must be either numeric (seconds) "
				  		+ "or in duration format, but is " + changeBeforeUpdates);
			  }
		  }
	  }

	  String incrementalUpdates = args.get(ARG_INCREMENTAL_UPDATES);
	  if (incrementalUpdates != null) {
		  if (incrementalUpdates.toLowerCase().trim().equals("true")) {
			  moduleSubscription.setIncrementalUpdates(true);
		  } else if (incrementalUpdates.toLowerCase().trim().equals("false")) {
			  moduleSubscription.setIncrementalUpdates(false);
		  } else {
			  _log.warn("value for " + ARG_INCREMENTAL_UPDATES + " must be either true or false, but is " + incrementalUpdates);
		  }
	  }

      applyArgsToVehicleMonitoringRequest(vmr, args);
  }

  private void applyArgsToVehicleMonitoringRequest(
      VehicleMonitoringRequestStructure vmr, Map<String, String> args) {

    String vehicleMonitoringRefValue = args.get(ARG_VEHICLE_MONITORING_REF);

    if (vehicleMonitoringRefValue != null) {
      VehicleMonitoringRefStructure vehicleMonitoringRef = new VehicleMonitoringRefStructure();
      vehicleMonitoringRef.setValue(vehicleMonitoringRefValue);
      vmr.setVehicleMonitoringRef(vehicleMonitoringRef);
    }

    String directionRefValue = args.get(ARG_DIRECTION_REF);
    if (directionRefValue != null) {
      DirectionRefStructure directionRef = new DirectionRefStructure();
      directionRef.setValue(directionRefValue);
      vmr.setDirectionRef(directionRef);
    }

    String lineRefValue = args.get(ARG_LINE_REF);
    if (lineRefValue != null) {
      LineRefStructure lineRef = new LineRefStructure();
      lineRef.setValue(lineRefValue);
      vmr.setLineRef(lineRef);
    }

    String vehicleRefValue = args.get(ARG_VEHICLE_REF);
    if (vehicleRefValue != null) {
      VehicleRefStructure vehicleRef = new VehicleRefStructure();
      vehicleRef.setValue(vehicleRefValue);
      vmr.setVehicleRef(vehicleRef);
    }

    String maximumVehiclesValue = args.get(ARG_MAXIMUM_VEHICLES);
    if (maximumVehiclesValue != null) {
      vmr.setMaximumVehicles(new BigInteger(maximumVehiclesValue));
    }

  }

  private void handleSituationExchangeSubscriptionSpecificArguments(
      SituationExchangeSubscriptionStructure moduleSubscription,
      Map<String, String> args) {

    SituationExchangeRequestStructure request = new SituationExchangeRequestStructure();
    moduleSubscription.setSituationExchangeRequest(request);

    applyArgsToSituationExchangeRequest(request, args);
  }

  private void applyArgsToSituationExchangeRequest(
      SituationExchangeRequestStructure request, Map<String, String> args) {

  }

  private void handleStopMonitoringSubscriptionSpecificArguments(StopMonitoringSubscriptionStructure moduleSubscription,
			Map<String, String> args) {
	  StopMonitoringRequestStructure smr = new StopMonitoringRequestStructure();
	    moduleSubscription.setStopMonitoringRequest(smr);

	  String changeBeforeUpdates = args.get(ARG_CHANGE_BEFORE_UPDATES);
	  if (changeBeforeUpdates != null) {
		  if (changeBeforeUpdates.startsWith("PT")) {
			  moduleSubscription.setChangeBeforeUpdates(_dataTypeFactory.newDuration(changeBeforeUpdates));
		  } else {
			  if(StringUtils.isNumeric(changeBeforeUpdates)) {
				  moduleSubscription.setChangeBeforeUpdates(_dataTypeFactory.newDuration("P" + changeBeforeUpdates + "S"));
			  } else {
				  _log.warn("value for " + ARG_CHANGE_BEFORE_UPDATES + " must be either numeric (seconds) "
				  		+ "or in duration format, but is " + changeBeforeUpdates);
			  }
		  }
	  }

	  String incrementalUpdates = args.get(ARG_INCREMENTAL_UPDATES);
	  if (incrementalUpdates != null) {
		  if (incrementalUpdates.toLowerCase().trim().equals("true")) {
			  moduleSubscription.setIncrementalUpdates(true);
		  } else if (incrementalUpdates.toLowerCase().trim().equals("false")) {
			  moduleSubscription.setIncrementalUpdates(false);
		  } else {
			  _log.warn("value for " + ARG_INCREMENTAL_UPDATES + " must be either true or false, but is " + incrementalUpdates);
		  }
	  }

	    applyArgsToStopMonitoringRequest(smr, args);
	}

private void applyArgsToStopMonitoringRequest(StopMonitoringRequestStructure smr, Map<String, String> args) {

	String monitoringRefValue = args.get(ARG_MONITORING_REF);
    if (monitoringRefValue != null) {
      MonitoringRefStructure monitoringRef = new MonitoringRefStructure();
      monitoringRef.setValue(monitoringRefValue);
      smr.setMonitoringRef(monitoringRef);
    }

	String previewInterval = args.get(ARG_PREVIEW_INTERVAL);
	if (previewInterval != null) {
		  if (previewInterval.startsWith("PT")) {
			  smr.setPreviewInterval(_dataTypeFactory.newDuration(previewInterval));
		  } else {
			  if(StringUtils.isNumeric(previewInterval)) {
				  smr.setPreviewInterval(_dataTypeFactory.newDuration("P" + previewInterval + "M"));
			  } else {
				  _log.warn("value for " + ARG_PREVIEW_INTERVAL + " must be either numeric (minutes) "
				  		+ "or in duration format, but is " + previewInterval);
			  }
		  }
	}
}

  private static Date getIso8601StringAsTime(String value, TimeZone timeZone)
      throws ParseException {

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    format.setTimeZone(timeZone);

    int n = value.length();

    if (n > 6) {
      char c1 = value.charAt(n - 6);
      char c2 = value.charAt(n - 3);
      if ((c1 == '-' || c1 == '+') && c2 == ':')
        value = value.substring(0, n - 3) + value.substring(n - 2);
    }

    return format.parse(value);
  }
}
