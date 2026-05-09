package com.dentis.DENTIS.repository;

import com.dentis.DENTIS.model.ChartRequest;
import com.dentis.DENTIS.model.ChartRequestStatus;
import com.dentis.DENTIS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChartRequestRepository extends JpaRepository<ChartRequest, Long> {
    List<ChartRequest> findAllByOrderByCreatedAtDesc();
    List<ChartRequest> findByStatusOrderByCreatedAtDesc(ChartRequestStatus status);
    List<ChartRequest> findByClinicianAndStatusInOrderByCreatedAtDesc(User clinician, List<ChartRequestStatus> statuses);

    @Query("SELECT r FROM ChartRequest r WHERE r.patient.id = :patientId AND r.status = :status AND (r.clinician = :clinician OR r.clinician IS NULL) ORDER BY r.createdAt DESC")
    List<ChartRequest> findApprovedForClinicianAndPatient(@Param("clinician") User clinician, @Param("patientId") Long patientId, @Param("status") ChartRequestStatus status);
}
