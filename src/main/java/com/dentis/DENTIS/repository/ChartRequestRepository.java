package com.dentis.DENTIS.repository;

import com.dentis.DENTIS.model.ChartRequest;
import com.dentis.DENTIS.model.ChartRequestStatus;
import com.dentis.DENTIS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChartRequestRepository extends JpaRepository<ChartRequest, Long> {
    List<ChartRequest> findAllByOrderByCreatedAtDesc();
    List<ChartRequest> findByStatusOrderByCreatedAtDesc(ChartRequestStatus status);
    List<ChartRequest> findByClinicianAndStatusInOrderByCreatedAtDesc(User clinician, List<ChartRequestStatus> statuses);
    List<ChartRequest> findByClinicianAndPatientIdAndStatus(User clinician, Long patientId, ChartRequestStatus status);
}
