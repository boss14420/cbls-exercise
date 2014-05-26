//package DefenseTimetabling;

/**
 *
 * @author hiepmutesz
 */
public class Jury {

    private int studentID;            // ID of student
    private int supervisorID;         // ID of supervisor
    private int examiner1ID;          // ID of examiner 1
    private int examiner2ID;          // ID of examiner 2
    private int presidentID;          // ID of president
    private int secretaryID;          // ID of secretary
    private int additionalMemberID;   // ID of additional member
    private int slot;                 // ID of additional member
    private int room;                 // ID of additional member
    
    public Jury(int studentID, int supervisorID, int examiner1ID, 
            int examiner2ID, int presidentID, int secretaryID, 
            int additionalMemberID, int slot, int room) {
        this.studentID = studentID;
        this.supervisorID = supervisorID;
        this.examiner1ID = examiner1ID;
        this.examiner2ID = examiner2ID;
        this.presidentID = presidentID;
        this.secretaryID = secretaryID;
        this.additionalMemberID = additionalMemberID;
        this.slot = slot;
        this.room = room;
    }

    public int getStudentID() {
        return studentID;
    }

    public void setStudentID(int studentID) {
        this.studentID = studentID;
    }

    public int getSupervisorID() {
        return supervisorID;
    }

    public void setSupervisorID(int supervisorID) {
        this.supervisorID = supervisorID;
    }

    public int getExaminer1ID() {
        return examiner1ID;
    }

    public void setExaminer1ID(int examiner1ID) {
        this.examiner1ID = examiner1ID;
    }

    public int getExaminer2ID() {
        return examiner2ID;
    }

    public void setExaminer2ID(int examiner2ID) {
        this.examiner2ID = examiner2ID;
    }

    public int getPresidentID() {
        return presidentID;
    }

    public void setPresidentID(int presidentID) {
        this.presidentID = presidentID;
    }

    public int getSecretaryID() {
        return secretaryID;
    }

    public void setSecretaryID(int secretaryID) {
        this.secretaryID = secretaryID;
    }

    public int getAdditionalMemberID() {
        return additionalMemberID;
    }

    public void setAdditionalMemberID(int additionalMemberID) {
        this.additionalMemberID = additionalMemberID;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    @Override
    public String toString() {
        return "Jury{" + "studentID=" + studentID + ", "
            + "supervisorID=" + supervisorID + ", examiner1ID=" + examiner1ID 
            + ", examiner2ID=" + examiner2ID + ", presidentID=" + presidentID 
            + ", secretaryID=" + secretaryID + ", additionalMemberID=" + additionalMemberID 
            + ", slot=" + slot + ", room=" + room + '}';
    }
}
