package org.jasig.ssp.service;

import org.jasig.ssp.model.EarlyAlert;

import javax.validation.constraints.NotNull;
import java.util.Map;

public interface SendMessageEarlyAlert {

    void sendMessageToAdvisor(@NotNull final EarlyAlert earlyAlert, final String emailCC) throws ObjectNotFoundException;

    void sendMessageToStudent(@NotNull final EarlyAlert earlyAlert);

    void sendConfirmationMessageToFaculty(final EarlyAlert earlyAlert);

    void sendAllEarlyAlertReminderNotifications();

    Map<String, Object> fillTemplateParameters(@NotNull final EarlyAlert earlyAlert);

}