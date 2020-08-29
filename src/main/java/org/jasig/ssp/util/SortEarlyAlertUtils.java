package org.jasig.ssp.util;

import org.jasig.ssp.transferobject.EarlyAlertTO;
import org.jasig.ssp.transferobject.messagetemplate.EarlyAlertMessageTemplateTO;

import java.util.*;

public class SortEarlyAlertUtils {

    public static void sortEarly(Map<UUID, List<EarlyAlertMessageTemplateTO>> easByCoach,
                                         UUID coachId) {
        Collections.sort(easByCoach.get(coachId), new Comparator<EarlyAlertTO>() {
            @Override
            public int compare(EarlyAlertTO p1, EarlyAlertTO p2) {
                Date p1Date = p1.getLastResponseDate();
                if (p1Date == null)
                    p1Date = p1.getCreatedDate();
                Date p2Date = p2.getLastResponseDate();
                if (p2Date == null)
                    p2Date = p2.getCreatedDate();
                return p1Date.compareTo(p2Date);
            }
        });
    }
}
