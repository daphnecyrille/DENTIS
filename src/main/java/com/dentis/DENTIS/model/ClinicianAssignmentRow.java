package com.dentis.DENTIS.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClinicianAssignmentRow {
    private User clinician;
    private User osFaculty;
    private User endoFaculty;
    private User perioFaculty;
    private User restoFaculty;
}
