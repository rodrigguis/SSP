package org.jasig.ssp.service.impl;

import com.google.common.collect.Lists;
import org.jasig.ssp.model.EarlyAlert;
import org.jasig.ssp.model.Person;
import org.jasig.ssp.model.WatchStudent;
import org.jasig.ssp.model.reference.Campus;
import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.service.PersonService;
import org.jasig.ssp.transferobject.messagetemplate.EarlyAlertMessageTemplateTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EarlyAlertProcessesInclusion {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(EarlyAlertProcessesInclusion.class);

    private PersonService personService;

    public EarlyAlertProcessesInclusion(PersonService personService) {
        this.personService = personService;
    }

    public void checkTypeAlertInclusion(Map<UUID, List<EarlyAlertMessageTemplateTO>> easByCoach, Map<UUID, Person> coaches,
                                         boolean includeCoachAsRecipient, boolean includeEarlyAlertCoordinatorAsRecipient,
                                         boolean includeEarlyAlertCoordinatorAsRecipientOnlyIfStudentHasNoCoach,
                                         EarlyAlert earlyAlert) {
        final Set<Person> recipients = new HashSet<Person>();
        Person coach = earlyAlert.getPerson().getCoach();

        if (includeCoachAsRecipient) {
            checkAddCoach(earlyAlert, recipients, coach);
        }
        if (includeEarlyAlertCoordinatorAsRecipient ||
                (coach == null && includeEarlyAlertCoordinatorAsRecipientOnlyIfStudentHasNoCoach)) {
            checkInclusionAlertStudentHasNoCoach(earlyAlert, recipients);
        }
        LOGGER.debug("Early Alert: {}; Recipients: {}", earlyAlert.getId(), recipients);
        if (recipients.isEmpty()) {
            return;
        } else {
            for (Person person : recipients) {
                // We've definitely got a coach by this point
                addMessageTemplate(easByCoach, coaches, earlyAlert, person);
            }
        }

        List<WatchStudent> watchers = earlyAlert.getPerson().getWatchers();
        addEarlyAlertCoaches(easByCoach, coaches, earlyAlert, watchers);
    }

    private void addMessageTemplate(Map<UUID, List<EarlyAlertMessageTemplateTO>> easByCoach, Map<UUID, Person> coaches, EarlyAlert earlyAlert, Person person) {
        if (easByCoach.containsKey(person.getId())) {
            final List<EarlyAlertMessageTemplateTO> coachEarlyAlerts = easByCoach.get(person.getId());
            coachEarlyAlerts.add(createEarlyAlertTemplateTO(earlyAlert));
        } else {
            coaches.put(person.getId(), person);
            final ArrayList<EarlyAlertMessageTemplateTO> messageTemplateTOS = Lists.newArrayList();
            messageTemplateTOS.add(createEarlyAlertTemplateTO(earlyAlert)); // add separately from newArrayList() call else list will be sized to 1
            easByCoach.put(person.getId(), messageTemplateTOS);
        }
    }

    private void addEarlyAlertCoaches(Map<UUID, List<EarlyAlertMessageTemplateTO>> easByCoach, Map<UUID, Person> coaches, EarlyAlert earlyAlert, List<WatchStudent> watchers) {
        for (WatchStudent watcher : watchers) {
            addMessageTemplate(easByCoach, coaches, earlyAlert, watcher.getPerson());
        }
    }

    private void checkInclusionAlertStudentHasNoCoach(EarlyAlert earlyAlert, Set<Person> recipients) {
        final Campus campus = earlyAlert.getCampus();
        if (campus == null) {
            LOGGER.error("Early Alert with id: {} does not have valid a campus, so skipping email to EAC.", earlyAlert.getId());
        } else {
            final UUID earlyAlertCoordinatorId = campus.getEarlyAlertCoordinatorId();
            if ( earlyAlertCoordinatorId == null ) {
                LOGGER.error("Early Alert with id: {} has campus with no early alert coordinator, so skipping email to EAC.", earlyAlert.getId());
            } else {
                try {
                    final Person earlyAlertCoordinator = personService.get(earlyAlertCoordinatorId);
                    if (earlyAlertCoordinator == null) { // guard against change in behavior where ObjectNotFoundException is not thrown (which we've seen)
                        LOGGER.error("Early Alert with id: {} has campus with an early alert coordinator with a bad ID ({}), so skipping email to EAC.", earlyAlert.getId(), earlyAlertCoordinatorId);
                    } else {
                        recipients.add(earlyAlertCoordinator);
                    }
                } catch(ObjectNotFoundException exp){
                    LOGGER.error("Early Alert with id: {} has campus with an early alert coordinator with a bad ID ({}), so skipping email to coach because no coach can be resolved.", new Object[] { earlyAlert.getId(), earlyAlertCoordinatorId, exp });
                }
            }
        }
    }

    private EarlyAlertMessageTemplateTO createEarlyAlertTemplateTO(EarlyAlert earlyAlert){
        Person creator = null;
        try{
            creator = personService.get(earlyAlert.getCreatedBy().getId());
        }catch(ObjectNotFoundException exp){
            LOGGER.error("Early Alert with id: " + earlyAlert.getId() + " does not have valid creator: " + earlyAlert.getCreatedBy(), exp);
        }
        return new EarlyAlertMessageTemplateTO(earlyAlert, creator,earlyAlert.getPerson().getWatcherEmailAddresses());
    }

    private void checkAddCoach(EarlyAlert earlyAlert, Set<Person> recipients, Person coach) {
        if (coach == null) {
            LOGGER.warn("Early Alert with id: {} is associated with a person without a coach, so skipping email to coach.", earlyAlert.getId());
        } else {
            recipients.add(coach);
        }
    }
}
