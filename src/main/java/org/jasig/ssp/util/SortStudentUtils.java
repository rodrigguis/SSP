package org.jasig.ssp.util;

import org.jasig.ssp.transferobject.reports.JournalCaseNotesStudentReportTO;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortStudentUtils {

    public static void sortByStudentName(List<JournalCaseNotesStudentReportTO> toSort) {
        Collections.sort(toSort, new Comparator<JournalCaseNotesStudentReportTO>() {
            public int compare(JournalCaseNotesStudentReportTO p1, JournalCaseNotesStudentReportTO p2) {
                int value = p1.getLastName().compareToIgnoreCase(p2.getLastName());
                if (value != 0)
                    return value;

                value = p1.getFirstName().compareToIgnoreCase(
                        p2.getFirstName());
                if (value != 0)
                    return value;
                if (p1.getMiddleName() == null && p2.getMiddleName() == null)
                    return 0;
                if (p1.getMiddleName() == null)
                    return -1;
                if (p2.getMiddleName() == null)
                    return 1;
                return p1.getMiddleName().compareToIgnoreCase(
                        p2.getMiddleName());
            }
        });
    }
}
