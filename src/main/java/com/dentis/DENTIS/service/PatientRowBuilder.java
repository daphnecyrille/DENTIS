package com.dentis.DENTIS.service;

import com.dentis.DENTIS.model.Patient;
import com.dentis.DENTIS.model.User;

import java.util.List;
import java.util.Map;

/** Builds the patient-list rows (one per patient, a status tag per chart form) used by the clinician and faculty lists. */
@SuppressWarnings("unchecked")
public final class PatientRowBuilder {

    private PatientRowBuilder() { }

    static Map<String, String> tag(String code, String title, String status, String css) {
        return Map.of("code", code, "title", title + " \u2014 " + status, "status", status, "css", css);
    }

    /**
     * One row per patient, with a status tag for each of their charts.
     * Row keys: "patient", "tags", "lastUpdated" (latest save of any of their charts), "sections" (e.g. "Perio Endo"),
     * and the flags "revise", "toSubmit", "awaiting", "done" (every chart approved) used by the filters.
     * Order: patients with a chart for revision first, fully approved patients last, otherwise most recently updated first.
     */
    public static List<Map<String, Object>> build(List<com.dentis.DENTIS.model.OralSurgeryChart> os,
                                                  List<com.dentis.DENTIS.model.EndodonticsChart> endo,
                                                  List<com.dentis.DENTIS.model.PeriodonticChart> perio,
                                                  List<com.dentis.DENTIS.model.OperativeChart> operative,
                                                  List<com.dentis.DENTIS.model.ProsthodonticsChart> prostho) {
        Map<Long, Map<String, Object>> rows = new java.util.LinkedHashMap<>();
        // add(patient, section, last saved, tag, status name)
        class Adder {
            void add(Patient p, String section, java.time.LocalDateTime touched, Map<String, String> t, String status) {
                add(p, section, touched, t, status, null, null);
            }
            void add(Patient p, String section, java.time.LocalDateTime touched, Map<String, String> t, String status, User clin, User fac) {
                if (p == null) return;
                Map<String, Object> row = rows.computeIfAbsent(p.getId(), k -> {
                    Map<String, Object> r = new java.util.HashMap<>();
                    r.put("patient", p);
                    r.put("tags", new java.util.ArrayList<Map<String, String>>());
                    r.put("sectionSet", new java.util.LinkedHashSet<String>());
                    r.put("statuses", new java.util.ArrayList<String>());
                    r.put("clinicianSet", new java.util.LinkedHashSet<String>());
                    r.put("facultySet", new java.util.LinkedHashSet<String>());
                    r.put("facultyIds", new java.util.HashSet<Long>());
                    return r;
                });
                @SuppressWarnings("unchecked") List<Map<String, String>> tags = (List<Map<String, String>>) row.get("tags");
                @SuppressWarnings("unchecked") java.util.Set<String> secs = (java.util.Set<String>) row.get("sectionSet");
                @SuppressWarnings("unchecked") List<String> sts = (List<String>) row.get("statuses");
                tags.add(t); secs.add(section); sts.add(status);
                if (clin != null) ((java.util.Set<String>) row.get("clinicianSet")).add(clin.getFullName());
                if (fac != null) { ((java.util.Set<String>) row.get("facultySet")).add(fac.getFullName()); ((java.util.Set<Long>) row.get("facultyIds")).add(fac.getId()); }
                java.time.LocalDateTime last = (java.time.LocalDateTime) row.get("lastUpdated");
                if (touched != null && (last == null || touched.isAfter(last))) row.put("lastUpdated", touched);
            }
        }
        Adder a = new Adder();
        for (var c : os) {
            boolean f2 = c.getChartType() != null && c.getChartType().name().equals("OSF2");
            a.add(c.getPatient(), "OS", touched(c.getUpdatedAt(), c.getCreatedAt()),
                  tag(f2 ? "OSF2" : "OSF1", f2 ? "Oral Surgery \u2013 Odontectomy" : "Oral Surgery \u2013 Patient Workup",
                      c.getStatus().getDisplayName(), c.getStatus().getCssClass()), c.getStatus().name(), c.getClinician(), c.getFaculty());
        }
        for (var c : endo) {
            var t = touched(c.getUpdatedAt(), c.getCreatedAt());
            a.add(c.getPatient(), "Endo", t, tag("ENDO1", "Endodontics \u2013 Patient Workup", c.getStatus().getDisplayName(), c.getStatus().getCssClass()), c.getStatus().name(), c.getClinician(), c.getFaculty());
            a.add(c.getPatient(), "Endo", t, tag("ENDO4", "Endodontics \u2013 Informed Consent", c.getForm4Label(), c.getForm4Css()), c.getForm4Status().name(), c.getClinician(), c.getFaculty());
        }
        for (var c : perio) {
            var t = touched(c.getUpdatedAt(), c.getCreatedAt());
            a.add(c.getPatient(), "Perio", t, tag("PF1", "Periodontics \u2013 Periodontal Chart", c.getStatus().getDisplayName(), c.getStatus().getCssClass()), c.getStatus().name(), c.getClinician(), c.getFaculty());
            a.add(c.getPatient(), "Perio", t, tag("PF2", "Periodontics \u2013 PPD/CAL/BOP Chart", c.getFormCStatus().getDisplayName(), c.getFormCStatus().getCssClass()), c.getFormCStatus().name(), c.getClinician(), c.getFaculty());
            a.add(c.getPatient(), "Perio", t, tag("PF3", "Periodontics \u2013 Gingivitis Reevaluation Chart", c.getPf3Status().getDisplayName(), c.getPf3Status().getCssClass()), c.getPf3Status().name(), c.getClinician(), c.getFaculty());
        }
        for (var c : operative) {
            var t = touched(c.getUpdatedAt(), c.getCreatedAt());
            a.add(c.getPatient(), "Resto", t, tag("RESTO10", "Operative \u2013 Assessment Form for Carious Lesions", c.getStatus().getDisplayName(), c.getStatus().getCssClass()), c.getStatus().name(), c.getClinician(), c.getFaculty());
            a.add(c.getPatient(), "Resto", t, tag("RESTO6", "Operative \u2013 Caries Risk Assessment", c.getForm2Status().getDisplayName(), c.getForm2Status().getCssClass()), c.getForm2Status().name(), c.getClinician(), c.getFaculty());
        }
        for (var c : prostho) {
            a.add(c.getPatient(), "Prostho", touched(c.getUpdatedAt(), c.getCreatedAt()),
                  tag("PROSTHO", "Prosthodontics" + (c.getCaseType() != null ? " (" + c.getCaseType() + ")" : ""),
                      c.getStatus().getDisplayName(), c.getStatus().getCssClass()), c.getStatus().name(), c.getClinician(), c.getFaculty());
        }
        List<Map<String, Object>> out = new java.util.ArrayList<>(rows.values());
        for (Map<String, Object> r : out) {
            @SuppressWarnings("unchecked") List<String> sts = (List<String>) r.get("statuses");
            @SuppressWarnings("unchecked") java.util.Set<String> secs = (java.util.Set<String>) r.get("sectionSet");
            r.put("revise", sts.contains("REVISE"));
            r.put("toSubmit", sts.contains("CREATED") || sts.contains("IN_PROGRESS"));
            r.put("awaiting", sts.contains("SUBMITTED"));
            r.put("done", !sts.isEmpty() && sts.stream().allMatch("APPROVED"::equals));
            r.put("sections", String.join(" ", secs));
            r.put("clinicians", String.join(", ", (java.util.Set<String>) r.get("clinicianSet")));
            r.put("faculty", String.join(", ", (java.util.Set<String>) r.get("facultySet")));
        }
        java.util.Comparator<Map<String, Object>> order = java.util.Comparator
            .comparing((Map<String, Object> r) -> !(Boolean) r.get("revise"))
            .thenComparing(r -> (Boolean) r.get("done"))
            .thenComparing((Map<String, Object> r) -> (java.time.LocalDateTime) r.get("lastUpdated"),
                           java.util.Comparator.nullsLast(java.util.Comparator.<java.time.LocalDateTime>reverseOrder()));
        out.sort(order);
        return out;
    }

    private static java.time.LocalDateTime touched(java.time.LocalDateTime updated, java.time.LocalDateTime created) {
        return updated != null ? updated : created;
    }
}
