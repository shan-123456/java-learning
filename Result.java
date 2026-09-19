import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Result {

    // ============================================================
    // ENUMS
    // ============================================================

    enum Grade {
        A_PLUS, A, B, C, D, F
    }

    enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    // ============================================================
    // CUSTOM EXCEPTION
    // ============================================================

    static class InvalidAcademicDataException extends Exception {
        public InvalidAcademicDataException(String message) {
            super(message);
        }
    }

    // ============================================================
    // SUBJECT CLASS
    // ============================================================

    static class Subject {

        private String name;
        private double marks;
        private double attendance;

        public Subject(String name, double marks, double attendance)
                throws InvalidAcademicDataException {

            if (marks < 0 || marks > 100) {
                throw new InvalidAcademicDataException(
                        "Marks must be between 0 and 100.");
            }

            if (attendance < 0 || attendance > 100) {
                throw new InvalidAcademicDataException(
                        "Attendance must be between 0 and 100.");
            }

            this.name = name;
            this.marks = marks;
            this.attendance = attendance;
        }

        public String getName() {
            return name;
        }

        public double getMarks() {
            return marks;
        }

        public double getAttendance() {
            return attendance;
        }

        public boolean isFailed() {
            return marks < 40;
        }

        public boolean hasLowAttendance() {
            return attendance < 75;
        }
    }

    // ============================================================
    // STUDENT CLASS
    // ============================================================

    static class Student {

        private int id;
        private String name;
        private String department;
        private int semester;
        private List<Subject> subjects;

        public Student(int id, String name, String department, int semester) {
            this.id = id;
            this.name = name;
            this.department = department;
            this.semester = semester;
            this.subjects = new ArrayList<>();
        }

        public void addSubject(Subject subject) {
            subjects.add(subject);
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDepartment() {
            return department;
        }

        public int getSemester() {
            return semester;
        }

        public List<Subject> getSubjects() {
            return subjects;
        }

        public double getTotalMarks() {
            return subjects.stream()
                    .mapToDouble(Subject::getMarks)
                    .sum();
        }

        public double getAverage() {
            return subjects.stream()
                    .mapToDouble(Subject::getMarks)
                    .average()
                    .orElse(0.0);
        }

        public double getAverageAttendance() {
            return subjects.stream()
                    .mapToDouble(Subject::getAttendance)
                    .average()
                    .orElse(0.0);
        }

        public long getFailedSubjects() {
            return subjects.stream()
                    .filter(Subject::isFailed)
                    .count();
        }

        public long getLowAttendanceSubjects() {
            return subjects.stream()
                    .filter(Subject::hasLowAttendance)
                    .count();
        }
    }

    // ============================================================
    // PERFORMANCE ANALYZER
    // ============================================================

    static class PerformanceAnalyzer {

        public static Grade calculateGrade(double average) {

            if (average >= 90)
                return Grade.A_PLUS;
            else if (average >= 80)
                return Grade.A;
            else if (average >= 70)
                return Grade.B;
            else if (average >= 60)
                return Grade.C;
            else if (average >= 50)
                return Grade.D;
            else
                return Grade.F;
        }

        public static Optional<Subject> findStrongestSubject(Student student) {

            return student.getSubjects()
                    .stream()
                    .max(Comparator.comparingDouble(Subject::getMarks));
        }

        public static Optional<Subject> findWeakestSubject(Student student) {

            return student.getSubjects()
                    .stream()
                    .min(Comparator.comparingDouble(Subject::getMarks));
        }

        public static List<Subject> findStrongSubjects(Student student) {

            return student.getSubjects()
                    .stream()
                    .filter(s -> s.getMarks() >= 75)
                    .sorted(Comparator.comparingDouble(Subject::getMarks).reversed())
                    .collect(Collectors.toList());
        }

        public static List<Subject> findWeakSubjects(Student student) {

            return student.getSubjects()
                    .stream()
                    .filter(s -> s.getMarks() < 60)
                    .sorted(Comparator.comparingDouble(Subject::getMarks))
                    .collect(Collectors.toList());
        }
    }

    // ============================================================
    // RISK PREDICTOR
    // ============================================================

    static class RiskPredictor {

        public static int calculateRiskScore(Student student) {

            int score = 0;

            double average = student.getAverage();

            // Academic performance
            if (average < 40)
                score += 50;
            else if (average < 50)
                score += 40;
            else if (average < 60)
                score += 25;
            else if (average < 70)
                score += 10;

            // Failed subjects
            score += student.getFailedSubjects() * 15;

            // Attendance
            if (student.getAverageAttendance() < 60)
                score += 30;
            else if (student.getAverageAttendance() < 75)
                score += 20;
            else if (student.getAverageAttendance() < 85)
                score += 5;

            // Low attendance subjects
            score += student.getLowAttendanceSubjects() * 5;

            return Math.min(score, 100);
        }

        public static RiskLevel predictRisk(Student student) {

            int score = calculateRiskScore(student);

            if (score >= 80)
                return RiskLevel.CRITICAL;
            else if (score >= 60)
                return RiskLevel.HIGH;
            else if (score >= 30)
                return RiskLevel.MEDIUM;
            else
                return RiskLevel.LOW;
        }

        public static List<String> generateRiskReasons(Student student) {

            List<String> reasons = new ArrayList<>();

            if (student.getAverage() < 60) {
                reasons.add(
                        "Overall academic average is below the recommended level.");
            }

            student.getSubjects()
                    .stream()
                    .filter(Subject::isFailed)
                    .forEach(s ->
                            reasons.add(
                                    "Failed/low score detected in " + s.getName() + "."));

            student.getSubjects()
                    .stream()
                    .filter(Subject::hasLowAttendance)
                    .forEach(s ->
                            reasons.add(
                                    "Attendance below 75% in " + s.getName() + "."));

            if (reasons.isEmpty()) {
                reasons.add(
                        "No major academic risk indicators detected.");
            }

            return reasons;
        }
    }

    // ============================================================
    // REPORT GENERATOR
    // ============================================================

    static class ReportGenerator {

        public static String generateReport(Student student) {

            StringBuilder report = new StringBuilder();

            Grade grade =
                    PerformanceAnalyzer.calculateGrade(student.getAverage());

            RiskLevel risk =
                    RiskPredictor.predictRisk(student);

            int riskScore =
                    RiskPredictor.calculateRiskScore(student);

            Optional<Subject> strongest =
                    PerformanceAnalyzer.findStrongestSubject(student);

            Optional<Subject> weakest =
                    PerformanceAnalyzer.findWeakestSubject(student);

            report.append("\n");
            report.append("============================================================\n");
            report.append("              PERSONALIZED PERFORMANCE REPORT\n");
            report.append("============================================================\n");

            report.append("Generated On : ")
                    .append(LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern(
                                    "dd-MM-yyyy HH:mm:ss")))
                    .append("\n\n");

            report.append("STUDENT INFORMATION\n");
            report.append("------------------------------------------------------------\n");
            report.append("Student ID     : ").append(student.getId()).append("\n");
            report.append("Student Name   : ").append(student.getName()).append("\n");
            report.append("Department     : ").append(student.getDepartment()).append("\n");
            report.append("Semester       : ").append(student.getSemester()).append("\n");

            report.append("\nEXAMINATION & MARKS\n");
            report.append("------------------------------------------------------------\n");

            for (Subject s : student.getSubjects()) {

                report.append(String.format(
                        "%-25s : %6.2f / 100%n",
                        s.getName(),
                        s.getMarks()));
            }

            report.append("\nATTENDANCE ANALYSIS\n");
            report.append("------------------------------------------------------------\n");

            for (Subject s : student.getSubjects()) {

                String status =
                        s.getAttendance() >= 75
                                ? "GOOD"
                                : "WARNING";

                report.append(String.format(
                        "%-25s : %6.2f%%   [%s]%n",
                        s.getName(),
                        s.getAttendance(),
                        status));
            }

            report.append("\nRESULT SUMMARY\n");
            report.append("------------------------------------------------------------\n");

            report.append("Total Marks       : ")
                    .append(String.format("%.2f", student.getTotalMarks()))
                    .append(" / ")
                    .append(student.getSubjects().size() * 100)
                    .append("\n");

            report.append("Average Marks     : ")
                    .append(String.format("%.2f", student.getAverage()))
                    .append("%\n");

            report.append("Average Attendance: ")
                    .append(String.format("%.2f", student.getAverageAttendance()))
                    .append("%\n");

            report.append("Grade             : ")
                    .append(grade)
                    .append("\n");

            report.append("Failed Subjects   : ")
                    .append(student.getFailedSubjects())
                    .append("\n");

            report.append("\nSUBJECT-WISE PERFORMANCE ANALYSIS\n");
            report.append("------------------------------------------------------------\n");

            if (strongest.isPresent()) {

                report.append("Strongest Subject : ")
                        .append(strongest.get().getName())
                        .append(" (")
                        .append(String.format(
                                "%.2f",
                                strongest.get().getMarks()))
                        .append("%)\n");
            }

            if (weakest.isPresent()) {

                report.append("Weakest Subject   : ")
                        .append(weakest.get().getName())
                        .append(" (")
                        .append(String.format(
                                "%.2f",
                                weakest.get().getMarks()))
                        .append("%)\n");
            }

            report.append("\nSTRONG SUBJECTS\n");
            report.append("------------------------------------------------------------\n");

            List<Subject> strongSubjects =
                    PerformanceAnalyzer.findStrongSubjects(student);

            if (strongSubjects.isEmpty()) {

                report.append("No subject currently falls in the strong-performance category.\n");

            } else {

                strongSubjects.forEach(s ->
                        report.append("✓ ")
                                .append(s.getName())
                                .append(" - ")
                                .append(String.format("%.2f", s.getMarks()))
                                .append("%\n"));
            }

            report.append("\nWEAK SUBJECTS\n");
            report.append("------------------------------------------------------------\n");

            List<Subject> weakSubjects =
                    PerformanceAnalyzer.findWeakSubjects(student);

            if (weakSubjects.isEmpty()) {

                report.append("No major weak subject detected.\n");

            } else {

                weakSubjects.forEach(s ->
                        report.append("⚠ ")
                                .append(s.getName())
                                .append(" - ")
                                .append(String.format("%.2f", s.getMarks()))
                                .append("%\n"));
            }

            report.append("\nACADEMIC RISK PREDICTION\n");
            report.append("------------------------------------------------------------\n");

            report.append("Risk Score : ")
                    .append(riskScore)
                    .append(" / 100\n");

            report.append("Risk Level : ")
                    .append(risk)
                    .append("\n");

            report.append("\nEARLY-WARNING INSIGHTS\n");
            report.append("------------------------------------------------------------\n");

            RiskPredictor.generateRiskReasons(student)
                    .forEach(reason ->
                            report.append("⚠ ")
                                    .append(reason)
                                    .append("\n"));

            report.append("\nFACULTY SUPPORT RECOMMENDATIONS\n");
            report.append("------------------------------------------------------------\n");

            generateRecommendations(student, risk)
                    .forEach(r ->
                            report.append("→ ")
                                    .append(r)
                                    .append("\n"));

            report.append("\n============================================================\n");
            report.append("          END OF PERSONALIZED REPORT\n");
            report.append("============================================================\n");

            return report.toString();
        }

        private static List<String> generateRecommendations(
                Student student,
                RiskLevel risk) {

            List<String> recommendations = new ArrayList<>();

            if (risk == RiskLevel.CRITICAL) {

                recommendations.add(
                        "Faculty should conduct immediate academic counselling.");

                recommendations.add(
                        "Create an individual improvement plan for the student.");

            } else if (risk == RiskLevel.HIGH) {

                recommendations.add(
                        "Faculty should closely monitor academic progress.");

                recommendations.add(
                        "Provide additional learning support for weak subjects.");

            } else if (risk == RiskLevel.MEDIUM) {

                recommendations.add(
                        "Monitor the student's performance in upcoming assessments.");

                recommendations.add(
                        "Encourage additional practice in weak subjects.");

            } else {

                recommendations.add(
                        "Continue regular academic monitoring.");

                recommendations.add(
                        "Encourage the student to maintain current performance.");
            }

            student.getSubjects()
                    .stream()
                    .filter(Subject::hasLowAttendance)
                    .forEach(s ->
                            recommendations.add(
                                    "Improve attendance in " + s.getName() + "."));

            student.getSubjects()
                    .stream()
                    .filter(Subject::isFailed)
                    .forEach(s ->
                            recommendations.add(
                                    "Arrange remedial support for " + s.getName() + "."));

            return recommendations;
        }
    }

    // ============================================================
    // FILE REPORT 
    // ============================================================

    public static void saveReportToFile(String report) {

        try {

            FileWriter writer =
                    new FileWriter("Student_Performance_Report.txt");

            writer.write(report);

            writer.close();

            System.out.println(
                    "\n✓ Report successfully saved as:");
            System.out.println(
                    "  Student_Performance_Report.txt");

        } catch (IOException e) {

            System.out.println(
                    "Unable to save report: " + e.getMessage());
        }
    }

    // ============================================================
    // DISPLAY SYSTEM HEADER
    // ============================================================

    public static void displayHeader() {

        System.out.println();
        System.out.println("============================================================");
        System.out.println("        SMART EXAMINATION, PERFORMANCE & ACADEMIC");
        System.out.println("                    RISK PREDICTION SYSTEM");
        System.out.println("============================================================");
        System.out.println();
        System.out.println("Problem Domain:");
        System.out.println("Student Examination & Result Management");
        System.out.println("Academic Performance Analysis");
        System.out.println("Academic Risk Prediction");
        System.out.println("Early-Warning & Faculty Support System");
        System.out.println();
    }

    // ============================================================
    // MAIN METHOD
    // ============================================================

    public static void main(String[] args) {
        displayHeader();

        try {
            Student student = new Student(101, "Alex Mercer", "Computer Science", 4);

            student.addSubject(new Subject("Data Structures", 85.0, 90.0));
            student.addSubject(new Subject("Algorithms", 42.0, 70.0));
            student.addSubject(new Subject("Database Systems", 35.0, 65.0));
            student.addSubject(new Subject("Operating Systems", 78.0, 88.0));
		System.out.println("\u26A0");

            String report = ReportGenerator.generateReport(student);
            System.out.println(report);

            saveReportToFile(report);

        } catch (InvalidAcademicDataException e) {
            System.out.println("Error creating student record: " + e.getMessage());
        }
    }
}