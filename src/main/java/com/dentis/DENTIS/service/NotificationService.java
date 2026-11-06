package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.User;
import org.springframework.stereotype.Service;

/** Counts for the clinician's "new results" badge: evaluation forms sent (Perio, Endo) + Prostho cases cleared, not opened yet. */
@Service
public class NotificationService {

    private final PerioEvaluationService perioEvaluationService;
    private final ProsthodonticsChartService prosthodonticsChartService;
    private final EndoEvaluationService endoEvaluationService;
    private final RestoEvaluationService restoEvaluationService;

    public NotificationService(PerioEvaluationService perioEvaluationService,
                               ProsthodonticsChartService prosthodonticsChartService,
                               EndoEvaluationService endoEvaluationService,
                               RestoEvaluationService restoEvaluationService) {
        this.perioEvaluationService = perioEvaluationService;
        this.prosthodonticsChartService = prosthodonticsChartService;
        this.endoEvaluationService = endoEvaluationService;
        this.restoEvaluationService = restoEvaluationService;
    }

    public int newEvaluationCount(User clinician) {
        if (clinician == null) return 0;
        return perioEvaluationService.unreadCount(clinician)
                + prosthodonticsChartService.newClearedForClinician(clinician).size()
                + endoEvaluationService.unreadCount(clinician)
                + restoEvaluationService.unreadCount(clinician);
    }
}
