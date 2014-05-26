//package DefenseTimetabling;

/**
 *
 * @author hiepmutesz
 */
public class SubjectMatch {

    private int student;
    private int professor;
    private int match;

    public SubjectMatch(int student, int professor, int match) {
        this.student = student;
        this.professor = professor;
        this.match = match;
    }

    public int getStudent() {
        return student;
    }

    public void setStudent(int student) {
        this.student = student;
    }

    public int getProfessor() {
        return professor;
    }

    public void setProfessor(int professor) {
        this.professor = professor;
    }

    public int getMatch() {
        return match;
    }

    public void setMatch(int match) {
        this.match = match;
    }

    @Override
    public String toString() {
        return "SubjectMatch{" + "student=" + student + ", professor=" + professor + ", match=" + match + '}';
    }
}
