package org.onebusaway.siri.core.subscriptions;

import java.util.List;

import org.onebusaway.siri.core.ESiriModuleType;
import org.onebusaway.siri.core.SiriClient;
import org.onebusaway.siri.core.SiriClientRequest;
import org.onebusaway.siri.core.SiriTypeFactory;
import org.onebusaway.siri.core.exceptions.SiriMissingArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.siri.siri.AbstractServiceDeliveryStructure;
import uk.org.siri.siri.AbstractSubscriptionStructure;
import uk.org.siri.siri.CheckStatusResponseStructure;
import uk.org.siri.siri.ErrorCodeStructure;
import uk.org.siri.siri.ParticipantRefStructure;
import uk.org.siri.siri.ServiceDeliveryErrorConditionStructure;
import uk.org.siri.siri.Siri;
import uk.org.siri.siri.StatusResponseStructure;
import uk.org.siri.siri.SubscriptionQualifierStructure;
import uk.org.siri.siri.SubscriptionRequest;
import uk.org.siri.siri.SubscriptionResponseStructure;
import uk.org.siri.siri.TerminateSubscriptionRequestStructure;
import uk.org.siri.siri.TerminateSubscriptionResponseStructure;
import uk.org.siri.siri.CheckStatusResponseBodyStructure.ErrorCondition;
import uk.org.siri.siri.TerminateSubscriptionResponseStructure.TerminationResponseStatus;

/**
 * This is a support class for {@link SiriClient} so we can off-load some
 * support methods and keep {@link SiriClient} from growing too large.
 * 
 * @author bdferris
 * 
 */
class ClientSupport {

  private static final Logger _log = LoggerFactory.getLogger(ClientSupport.class);

  public SubscriptionId getSubscriptionIdForSubscriptionRequest(
      SubscriptionRequest subscriptionRequest,
      AbstractSubscriptionStructure functionalSubscriptionRequest) {

    ParticipantRefStructure subscriberRef = subscriptionRequest.getRequestorRef();
    SubscriptionQualifierStructure subscriptionRef = functionalSubscriptionRequest.getSubscriptionIdentifier();

    return getSubscriptionId(subscriberRef, subscriptionRef);
  }

  public SubscriptionId getSubscriptionIdForStatusResponse(
      StatusResponseStructure status) {

    ParticipantRefStructure subscriberRef = status.getSubscriberRef();
    SubscriptionQualifierStructure subscriptionRef = status.getSubscriptionRef();

    return getSubscriptionId(subscriberRef, subscriptionRef);
  }

  public SubscriptionId getSubscriptionIdForTerminationStatusResponse(
      TerminationResponseStatus status) {

    ParticipantRefStructure subscriberRef = status.getSubscriberRef();
    SubscriptionQualifierStructure subscriptionRef = status.getSubscriptionRef();

    return getSubscriptionId(subscriberRef, subscriptionRef);
  }

  public SubscriptionId getSubscriptionIdForModuleDelivery(
      AbstractServiceDeliveryStructure moduleDelivery) {

    ParticipantRefStructure subscriberRef = moduleDelivery.getSubscriberRef();
    SubscriptionQualifierStructure subscriptionRef = moduleDelivery.getSubscriptionRef();

    return getSubscriptionId(subscriberRef, subscriptionRef);
  }

  public SubscriptionId getSubscriptionId(
      ParticipantRefStructure subscriberRef,
      SubscriptionQualifierStructure subscriptionRef) {

    if (subscriberRef == null || subscriberRef.getValue() == null)
      throw new SiriMissingArgumentException("SubscriberRef");

    if (subscriptionRef == null || subscriptionRef.getValue() == null)
      throw new SiriMissingArgumentException("SubscriptionRef");

    return new SubscriptionId(subscriberRef.getValue(),
        subscriptionRef.getValue());
  }

  public SiriClientRequest getTerminateSubscriptionRequestForSubscriptions(
      ClientSubscriptionChannel channel, String subscriberId,
      List<ClientSubscriptionInstance> subscriptionInstances) {

    TerminateSubscriptionRequestStructure terminateRequest = new TerminateSubscriptionRequestStructure();

    ParticipantRefStructure subscriberRef = SiriTypeFactory.particpantRef(subscriberId);
    terminateRequest.setSubscriberRef(subscriberRef);

    for (ClientSubscriptionInstance instance : subscriptionInstances) {

      SubscriptionId id = instance.getSubscriptionId();

      SubscriptionQualifierStructure value = new SubscriptionQualifierStructure();
      value.setValue(id.getSubscriptionId());
      terminateRequest.getSubscriptionRef().add(value);
    }

    Siri payload = new Siri();
    payload.setTerminateSubscriptionRequest(terminateRequest);

    SiriClientRequest request = new SiriClientRequest();
    request.setTargetUrl(channel.getAddress());
    request.setTargetVersion(channel.getTargetVersion());
    request.setPayload(payload);
    return request;
  }

  /****
   * 
   * @param subId
   * @param newModuleType
   * @param existingModuleType
   */

  public void logWarningAboutActiveSubscriptionsWithDifferentModuleTypes(
      SubscriptionId subId, ESiriModuleType newModuleType,
      ESiriModuleType existingModuleType) {

    _log.warn("An existing subscription ("
        + subId
        + ") already exists for module type "
        + existingModuleType
        + " but a new subscription has been requested for module type "
        + newModuleType
        + ".  Reuse of the same subscription id across different module types is not supported.");
  }

  public void logWarningAboutPendingSubscriptionsWithDifferentModuleTypes(
      SubscriptionId subId, ESiriModuleType moduleType,
      ClientPendingSubscription pending) {

    _log.warn("An existing pending subscription ("
        + subId
        + ") already exists for module type "
        + pending.getModuleType()
        + " but a new subscription has been requested for module type "
        + moduleType
        + ".  Reuse of the same subscription id across different module types is not supported.");
  }

  public void logUnknownSubscriptionResponse(
      SubscriptionResponseStructure response, SubscriptionId subId) {
    StringBuilder b = new StringBuilder();
    b.append("A <SubscriptionResponse/ResponseStatus/> was received with no pending <SubscriptionRequest/> having been sent:");
    if (response.getAddress() != null)
      b.append(" address=").append(response.getAddress());
    if (response.getSubscriptionManagerAddress() != null)
      b.append(" subscriptionManagerAddress=").append(
          response.getSubscriptionManagerAddress());
    if (response.getResponderRef() != null
        && response.getResponderRef().getValue() != null)
      b.append(" responderRef=" + response.getResponderRef().getValue());
    b.append(" subscriptionId=" + subId);
    _log.warn(b.toString());
  }

  public void logErrorInSubscriptionResponse(
      SubscriptionResponseStructure response, StatusResponseStructure status,
      SubscriptionId subId) {

    StringBuilder b = new StringBuilder();
    b.append("We received an error response for a subscription request:");
    if (response.getAddress() != null)
      b.append(" address=").append(response.getAddress());
    if (response.getSubscriptionManagerAddress() != null)
      b.append(" subscriptionManagerAddress=").append(
          response.getSubscriptionManagerAddress());
    if (response.getResponderRef() != null
        && response.getResponderRef().getValue() != null)
      b.append(" responderRef=" + response.getResponderRef().getValue());
    b.append(" subscriptionId=" + subId);
    ServiceDeliveryErrorConditionStructure error = status.getErrorCondition();

    if (error != null) {
      appendError(error.getAccessNotAllowedError(), b);
      appendError(error.getAllowedResourceUsageExceededError(), b);
      appendError(error.getCapabilityNotSupportedError(), b);
      appendError(error.getNoInfoForTopicError(), b);
      appendError(error.getOtherError(), b);

      if (error.getDescription() != null
          && error.getDescription().getValue() != null)
        b.append(" errorDescription=").append(error.getDescription().getValue());
    }

    _log.warn(b.toString());
  }

  public void logUnknownTerminateSubscriptionResponse(
      TerminateSubscriptionResponseStructure response, SubscriptionId subId) {
    StringBuilder b = new StringBuilder();
    b.append("A <TerminateSubscriptionResponse/> was received with no pending <TerminateSubscriptionRequest/> having been sent:");
    if (response.getAddress() != null)
      b.append(" address=").append(response.getAddress());
    if (response.getResponderRef() != null
        && response.getResponderRef().getValue() != null)
      b.append(" responderRef=" + response.getResponderRef().getValue());
    b.append(" subscriptionId=" + subId);
    _log.warn(b.toString());
  }

  public void logErrorInTerminateSubscriptionResponse(
      TerminateSubscriptionResponseStructure response,
      TerminationResponseStatus status, SubscriptionId subId) {

    StringBuilder b = new StringBuilder();
    b.append("We received an error response for a subscription request:");
    if (response.getAddress() != null)
      b.append(" address=").append(response.getAddress());
    if (response.getResponderRef() != null
        && response.getResponderRef().getValue() != null)
      b.append(" responderRef=" + response.getResponderRef().getValue());
    b.append(" subscriptionId=" + subId);
    TerminationResponseStatus.ErrorCondition error = status.getErrorCondition();

    if (error != null) {
      appendError(error.getCapabilityNotSupportedError(), b);
      appendError(error.getUnknownSubscriberError(), b);
      appendError(error.getUnknownSubscriptionError(), b);
      appendError(error.getOtherError(), b);

      if (error.getDescription() != null
          && error.getDescription().getValue() != null)
        b.append(" errorDescription=").append(error.getDescription().getValue());
    }

    _log.warn(b.toString());
  }

  public void logErrorInCheckStatusResponse(ClientSubscriptionChannel channel,
      CheckStatusResponseStructure response, boolean isNewer, boolean isInError) {

    StringBuilder b = new StringBuilder();
    b.append("check status failed for channel:");
    b.append(" address=").append(channel.getAddress());

    if (isNewer) {
      b.append(" prevServiceStartedTime=");
      b.append(channel.getLastServiceStartedTime());
      b.append(" newServiceStartedTime=");
      b.append(response.getServiceStartedTime());
    }

    ErrorCondition error = response.getErrorCondition();
    if (isInError && error != null) {
      appendError(error.getServiceNotAvailableError(), b);
      appendError(error.getOtherError(), b);
      if (error.getDescription() != null
          && error.getDescription().getValue() != null)
        b.append(" errorDescription=" + error.getDescription().getValue());
    }

    _log.warn(b.toString());
  }

  /****
   * Private Methods
   ****/

  private void appendError(ErrorCodeStructure code, StringBuilder b) {
    if (code == null)
      return;
    Class<? extends ErrorCodeStructure> clazz = code.getClass();
    String name = clazz.getName();
    int index = name.lastIndexOf('.');
    if (index != -1)
      name = name.substring(index + 1);
    b.append(" errorType=").append(name);
    if (code.getErrorText() != null)
      b.append(" errorText=").append(code.getErrorText());
  }

}