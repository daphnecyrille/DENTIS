package com.dentis.DENTIS.model;

import java.util.List;

/**
 * Operative Dentistry evaluation forms, graded by the faculty and sent to the clinician:
 * RESTO1 – Clinical Operative Dentistry Evaluation Form (direct restorations) – RESTO FORM 1
 * RESTO2 – Clinical Operative Dentistry Evaluation Form (indirect restorations) – RESTO FORM 2
 * NOC    – Clinical Operative Dentistry Assessment Form: Pit & Fissure Sealant Application
 * Rubric text is from the paper forms.
 */
public enum RestoEvalFormType {
    RESTO1, RESTO2, NOC;

    /** One line of a part's rubric: the rating it stands for and its description. */
    public record Line(String rating, String text) { }

    /** A graded part: number, name, what was done, max points (Forms 1-2) or weight in % (NOC), rubric. */
    public record Part(String no, String name, String did, double max, List<Line> rubric) {
        /** "15" or "2.5" (no trailing .0) for labels */
        public String maxLabel() { return max == Math.floor(max) ? String.valueOf((int) max) : String.valueOf(max); }
    }

    public String getTitle() {
        return this == NOC ? "Clinical Operative Dentistry Assessment Form" : "Clinical Operative Dentistry Evaluation Form";
    }

    public String getDisplayName() {
        return switch (this) {
            case RESTO1 -> "Evaluation Form – Direct Restorations";
            case RESTO2 -> "Evaluation Form – Indirect Restorations";
            case NOC -> "NOC: Pit & Fissure Sealant Application";
        };
    }

    /** Short code used in chart numbers and lists. */
    public String getCode() { return this == NOC ? "NOC" : name(); }

    /** Ratings the faculty can pick. Forms 1-2 rate 1-5 (with 4.5 and 3.5); NOC rates 3.0-5.0. */
    public List<String> getRatings() {
        return this == NOC ? List.of("5.0", "4.5", "4.0", "3.5", "3.0") : List.of("5", "4.5", "4", "3.5", "3", "2", "1");
    }

    /** Forms 1-2: each part's faculty name and date are recorded (paper: "Faculty Signature/date" per part). */
    public boolean isStampedPerPart() { return this != NOC; }

    /** NOC: one shared scale ("The procedure was accomplished:") instead of a rubric per part. */
    public boolean isSharedScale() { return this == NOC; }

    private static final List<Line> DIAG = List.of(
            new Line("5", "Independently utilizing appropriate diagnostic tools"),
            new Line("4", "With minimal guidance from instructor as to diagnosis & diagnostic tools"),
            new Line("3", "With significant guidance of instructor as to diagnosis & diagnostic tools"),
            new Line("2", "Much guidance from the instructor as to diagnosis & diagnostic tools"),
            new Line("1", "Incorrect diagnosis even after adequate guidance from the instructor"));
    private static final List<Line> PREP = List.of(
            new Line("5", "Is ready to receive a restoration, did it excellently and independently"),
            new Line("4", "Has slight deviations from acceptable that required one modification"),
            new Line("3", "Requires some amount of modification done with help of faculty"),
            new Line("2", "Needed to be modified with faculty help, some compromise was made to an acceptable prep"),
            new Line("1", "Has poor quality; damage is irreversible, faculty had to modify"));
    private static final List<Line> PULP = List.of(
            new Line("5", "Excellently & independently; knew the indication, used proper armamentarium & technique"),
            new Line("4", "Properly and with minimal faculty guidance"),
            new Line("3", "Adequately but with adequate faculty help"),
            new Line("2", "Adequately only with the instruction and assistance from faculty"),
            new Line("1", "Pulp protection done by faculty; student did not recognize indication as needed"));
    private static final List<Line> GUIDED = List.of(   // Polishing (Form 1) / Fitting & Cementation (Form 2)
            new Line("5", "Excellently and independently"),
            new Line("4", "Properly with very minimal faculty guidance"),
            new Line("3", "Adequately but with adequate faculty guidance"),
            new Line("2", "To an acceptable level with a lot of faculty guidance"),
            new Line("1", "To an unacceptable level; faculty had to take over"));
    private static final List<Line> DEPORT = List.of(
            new Line("5", "With diligence, proper time & patient management, neatness, completeness & appropriateness of armamentarium"),
            new Line("4–2", "With increasing levels of deviations from above"),
            new Line("1", "Without exercising the requisite traits as above"));

    public List<Part> getParts() {
        return switch (this) {
            case RESTO1 -> List.of(
                    new Part("I", "Diagnosis", "Arrived at a correct diagnosis", 15, DIAG),
                    new Part("II", "Cavity Preparation", "Prepared cavity that:", 35, PREP),
                    new Part("III", "Pulp Protection", "Performed pulp protection", 5, PULP),
                    new Part("IV", "Filling", "Restored the prepared cavity", 30, List.of(
                            new Line("5", "Excellently and independently meeting all requirements of marginal integrity, restored anatomy, proximal contact, surface finish & occlusion"),
                            new Line("4.5", "Properly but with slight deviations from the ideal that will not affect its quality & longevity"),
                            new Line("4", "Acceptably after minor modifications done with minimal faculty guidance"),
                            new Line("3.5", "That required several modifications with adequate faculty guidance"),
                            new Line("3", "Need further modification during polishing/ another appointment"),
                            new Line("2", "Inadequately with deviations that required faculty assistance to correct"),
                            new Line("1", "Poorly cannot be improved during polishing; errors would result in failure of restoration"))),
                    new Part("V", "Polishing", "Polished restoration", 10, GUIDED),
                    new Part("VI", "Deportment", "Accomplished work", 5, DEPORT));
            case RESTO2 -> List.of(
                    new Part("I", "Diagnosis", "Arrived at a correct diagnosis", 15, DIAG),
                    new Part("II", "Cavity Preparation", "Prepared cavity that:", 35, PREP),
                    new Part("III", "Pulp Protection", "Performed pulp protection", 5, PULP),
                    new Part("IV", "Restoration", "Performed impression taking, bite registration & temporization", 30, List.of(
                            new Line("5", "Excellently & independently using proper armamentarium & technique"),
                            new Line("4.5", "Properly & independently but with slight deviations from the ideal"),
                            new Line("4", "Acceptably after minor modifications done with minimal faculty guidance"),
                            new Line("3.5", "That required several modifications with adequate faculty guidance"),
                            new Line("3", "Need further modification during polishing/ another appointment"),
                            new Line("2", "Inadequately with deviations that required faculty assistance to correct"),
                            new Line("1", "Poorly & faculty needed to take over to complete procedures"))),
                    new Part("V", "Fitting & Cementation", "Tried in & cemented final onlay", 10, GUIDED),
                    new Part("VI", "Deportment", "Accomplished work", 5, DEPORT));
            case NOC -> {
                List<Line> scale = List.of(
                        new Line("5.0", "Excellently and independently"),
                        new Line("4.5", "Properly but with slight deviation from excellent; done independently"),
                        new Line("4.0", "Properly but with some deviations that required 1 modification; done with very little faculty supervision"),
                        new Line("3.5", "Adequately completed after 2 modifications with some faculty supervision"),
                        new Line("3.0", "Acceptably after much guidance from or with demonstration by faculty"));
                yield List.of(
                        new Part("I", "Diagnosis", "Determined The Indication for PFS", 15, scale),
                        new Part("II", "Tooth Cleaning & Isolation", "Surface Adequately Cleaned & Isolated with Rubber Dam (Multiple Teeth) / Cotton Roll (Single Tooth)", 15, scale),
                        new Part("III", "Surface Coverage, Occlusion & Retention of PFS", "Pits & fissures Adequately Filled; Smooth Adaptation; Not Too Thick; Not Easily Dislodged", 60, scale),
                        new Part("IV", "Deportment", "Proper Armamentarium & Technique; Time Management, Neatness", 10, scale));
            }
        };
    }
}
