package org.jasig.ssp.service.impl;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.jasig.ssp.model.*;
import org.jasig.ssp.model.reference.MessageTemplate;
import org.jasig.ssp.service.EarlyAlertRoutingService;
import org.jasig.ssp.service.MessageService;
import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.service.SendMessageEarlyAlert;
import org.jasig.ssp.service.reference.MessageTemplateService;
import org.jasig.ssp.util.sort.PagingWrapper;
import org.jasig.ssp.util.sort.SortingAndPaging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SendMessageEarlyAlertImpl implements SendMessageEarlyAlert {

    private MessageService messageService;

    private MessageTemplateService messageTemplateService;

    private EarlyAlertRoutingService earlyAlertRoutingService;

    public SendMessageEarlyAlertImpl(MessageService messageService, MessageTemplateService messageTemplateService,
                                     EarlyAlertRoutingService earlyAlertRoutingService) {
        this.messageService = messageService;
        this.messageTemplateService = messageTemplateService;
        this.earlyAlertRoutingService = earlyAlertRoutingService;
    }

    @Override
    public void sendMessageToAdvisor(EarlyAlert earlyAlert, String emailCC) throws ObjectNotFoundException {
        if (earlyAlert == null) {
            throw new IllegalArgumentException("Early alert was missing.");
        }

        if (earlyAlert.getPerson() == null) {
            throw new IllegalArgumentException("EarlyAlert Person is missing.");
        }

        final Person person = earlyAlert.getPerson().getCoach();
        final SubjectAndBody subjAndBody = messageTemplateService
                .createEarlyAlertAdvisorConfirmationMessage(fillTemplateParameters(earlyAlert));

        Set<String> watcherEmailAddresses = new HashSet<String>(earlyAlert.getPerson().getWatcherEmailAddresses());
        if(emailCC != null && !emailCC.isEmpty())
        {
            watcherEmailAddresses.add(emailCC);
        }
        if ( person == null ) {
//            LOGGER.warn("Student {} had no coach when EarlyAlert {} was"
//                            + " created. Unable to send message to coach.",
//                    earlyAlert.getPerson(), earlyAlert);
        } else {
            // Create and queue the message
            final Message message = messageService.createMessage(person, org.springframework.util.StringUtils.arrayToCommaDelimitedString(watcherEmailAddresses
                            .toArray(new String[watcherEmailAddresses.size()])),
                    subjAndBody);
//            LOGGER.info("Message {} created for EarlyAlert {}", message, earlyAlert);
        }

        // Send same message to all applicable Campus Early Alert routing
        // entries
        final PagingWrapper<EarlyAlertRouting> routes = earlyAlertRoutingService
                .getAllForCampus(earlyAlert.getCampus(), new SortingAndPaging(
                        ObjectStatus.ACTIVE));

        if (routes.getResults() > 0) {
            final ArrayList<String> alreadySent = Lists.newArrayList();

            for (final EarlyAlertRouting route : routes.getRows()) {
                // Check that route applies
                if ( route.getEarlyAlertReason() == null ) {
                    throw new ObjectNotFoundException(
                            "EarlyAlertRouting missing EarlyAlertReason.", "EarlyAlertReason");
                }

                // Only routes that are for any of the Reasons in this EarlyAlert should be applied.
                if ( (earlyAlert.getEarlyAlertReasons() == null)
                        || !earlyAlert.getEarlyAlertReasons().contains(route.getEarlyAlertReason()) ) {
                    continue;
                }

                // Send e-mail to specific person
                final Person to = route.getPerson();
                if ( to != null && StringUtils.isNotBlank(to.getPrimaryEmailAddress()) ) {
                    //check if this alert has already been sent to this recipient, if so skip
                    if ( alreadySent.contains(route.getPerson().getPrimaryEmailAddress()) ) {
                        continue;
                    } else {
                        alreadySent.add(route.getPerson().getPrimaryEmailAddress());
                    }

                    final Message message = messageService.createMessage(to, null, subjAndBody);
//                    LOGGER.info("Message {} for EarlyAlert {} also routed to {}",
//                            new Object[]{message, earlyAlert, to}); // NOPMD
                }

                // Send e-mail to a group
                if ( !StringUtils.isEmpty(route.getGroupName()) && !StringUtils.isEmpty(route.getGroupEmail()) ) {
                    final Message message = messageService.createMessage(route.getGroupEmail(), null,subjAndBody);
//                    LOGGER.info("Message {} for EarlyAlert {} also routed to {}", new Object[] { message, earlyAlert, // NOPMD
//                            route.getGroupEmail() });
                }
            }
        }

    }

    @Override
    public void sendMessageToStudent(EarlyAlert earlyAlert) {

    }

    @Override
    public void sendConfirmationMessageToFaculty(EarlyAlert earlyAlert) {

    }

    @Override
    public void sendAllEarlyAlertReminderNotifications() {

    }

    @Override
    public Map<String, Object> fillTemplateParameters(EarlyAlert earlyAlert) {
        return null;
    }
}
