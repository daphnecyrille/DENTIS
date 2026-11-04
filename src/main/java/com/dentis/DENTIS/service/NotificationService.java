package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.User;
import org.springframework.stereotype.Service;

/** Counts for the clinician's "new results" badge: evaluation forms sent + Prostho cases cleared, not opened yet. */
@Service
public class NotificationService {

    private final PerioEvaluationService perioEvaluationService;
    private final ProsthodonticsChartService prosthodonticsChartService;

    public NotificationService(PerioEvaluationService perioEvaluationService,
                               ProsthodonticsChartService prosthodonticsChartService) {
        this.perioEvaluationService = perioEvaluationService;
        this.prosthodonticsChartService = prosthodonticsChartService;
    }

    public int newEvaluationCount(User clinician) {
        if (clinician == null) return 0;
        return perioEvaluationService.unreadCount(clinician)
                + prosthodonticsChartService.newClearedForClinician(clinician).size();
    }
}
