//package DefenseTimetabling;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Random;
import localsearch.constraints.basic.Implicate;
import localsearch.constraints.basic.IsEqual;
import localsearch.constraints.basic.NotEqual;
import localsearch.constraints.basic.NotOverLap;
import localsearch.constraints.alldifferent.AllDifferent;
import localsearch.functions.basic.FuncMinus;
import localsearch.functions.occurrence.Occurrence;
import localsearch.functions.max_min.FMax;
import localsearch.functions.max_min.FMin;
import localsearch.model.ConstraintSystem;
import localsearch.model.IFunction;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;
import localsearch.search.TabuSearch;

/**
 *
 * @author hiepmutesz
 */
public class DefenseTimetablingOpenLocalSearch {

    String fileHeader;                  // Header of file name

    LocalSearchManager manager;         // For managing local search algorithm
    ConstraintSystem system;            // For manage constraints of problem

    private Jury[] juryList;            // Jury list
    private LinkedList<SubjectMatch> matchList;   // Subject match between professors and student
    private HashMap<Integer, Integer> professorMap; // Map from supervisorID to varint value
    
    private int matchs;                 // Number of subject matchs
    private int students;               // Number of students

    private int[] internal;             // ID of internal professor i
    private int[] external;             // ID of external professor i

    private int internals;              // Number of internal professors
    private int externals;              // Number of external professors
    private int professors;             // Number of professors
    
    private int slots;                  // Number of slots
    private int rooms;                  // Number of rooms

    // objective functions
    private IFunction diffFunc;         // Objective 1, = Max (occurence) - Min (occurence)

    private Random rand;

    // xp(s, 1):  examiner 1 of the jury of student s
    // xp(s, 2):  examiner 2 of the jury of student s
    // xp(s, 3):  president of the jury of student s
    // xp(s, 4):  secretary of the jury of student s
    // xp(s, 5):  additional member of the jury of student s
    private VarIntLS[][] solutionPro;
    private VarIntLS[] solutionSlot;
    private VarIntLS[] solutionRoom;
    
    IFunction[] occurence;               // 
    VarIntLS[][] professorRoom;         //
    VarIntLSArrayList[] slotTime;       // 
    VarIntLS[] minSlotTime;             //
    VarIntLS[] maxSlotTime;             //
    
    // Constructor
    public DefenseTimetablingOpenLocalSearch() {
        manager = new LocalSearchManager();
        system = new ConstraintSystem(manager);
        rand = new Random();

        while (!ReadData())
            ;
    }

    public void initSystem() {
        InitVariables();
        InitRandomSolution();
        PostConstraints();
        PostObjectives();

        system.close();
        manager.close();
    }

    // Read data from file
    public boolean ReadData() {
        try {
            // Read filename
            /*System.out.print("Type your header of TXT file: ");
             BufferedReader reader
             = new BufferedReader(new InputStreamReader(System.in));
             this.fileHeader = reader.readLine();
             if (fileHeader == null) {
             return false;
             }*/

            // Open file
            //Scanner scanner = new Scanner(new File(fileHeader + ".txt"));
            Scanner scanner = new Scanner(new File("data_SoICT/jury-data.txt"));
            System.out.print("Reading file... ");

            // Read jury list
            String str = scanner.next();
            students = Integer.parseInt(scanner.next());

            juryList = new Jury[students];
            
            for (int i = 0; i < 9; ++i) {
                str = scanner.next();
            }

            for (int i = 0; i < students; ++i) {
                juryList[i] = new Jury(
                        scanner.nextInt(), scanner.nextInt(), scanner.nextInt(), 
                        scanner.nextInt(), scanner.nextInt(), scanner.nextInt(), 
                        scanner.nextInt(), scanner.nextInt(), scanner.nextInt()
                );
            }

            // Read internal professors
            str = scanner.next();
            str = scanner.next();
            internals = Integer.parseInt(scanner.next());
            internal = new int[internals];

            professorMap = new HashMap<Integer, Integer>();
            for (int i = 0; i < internals; ++i) {
                internal[i] = scanner.nextInt();
                professorMap.put(internal[i], i);
            }

            // Read external professors
            str = scanner.next();
            str = scanner.next();
            externals = Integer.parseInt(scanner.next());
            external = new int[externals];

            for (int i = 0; i < externals; ++i) {
                external[i] = scanner.nextInt();
                professorMap.put(external[i], i + internals);
            }
            
            professors = internals + externals;

            // Read subject match list
            for (int i = 0; i < 6; ++i) {
                str = scanner.next();
            }

            matchList = new LinkedList<SubjectMatch>();
            
            try {
                do {
                    matchList.add(new SubjectMatch(scanner.nextInt(), 
                            scanner.nextInt(), scanner.nextInt()));
                } while (scanner.hasNext());
            } catch (java.util.InputMismatchException e) {

            }
            
            matchs = matchList.size();
            
            // Read number of slots and rooms
            for (int i = 0; i < 7; ++i) {
                str = scanner.next();
            }
            
            slots = scanner.nextInt();
            rooms = scanner.nextInt();

            System.out.println("Done!");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Print data
    public void PrintData()
    {
        // Print jury list
        System.out.printf("%10s %10s %10s %10s %10s %10s %10s %10s %10s\n", 
                "Student", "Supervisor", "Examiner1", "Examiner2", 
                "President", "Secretary", "Additional", "Slot", "Room");
        
        for (int i=0; i<students; ++i)
        {
            System.out.printf("%10d %10d %10d %10d %10d %10d %10d %10d %10d\n", 
                    juryList[i].getStudentID(), juryList[i].getSupervisorID(),
                    juryList[i].getExaminer1ID(), juryList[i].getExaminer2ID(), 
                    juryList[i].getPresidentID(), juryList[i].getSecretaryID(),
                    juryList[i].getAdditionalMemberID(), juryList[i].getSlot(),
                    juryList[i].getRoom());
        }
        
        // Print internal professors
        System.out.println();
        System.out.print("internal = ");
        for (int i=0; i<internals; ++i)
        {
            System.out.print(internal[i] + " ");
        }
        
        // Print external professors
        System.out.println();
        System.out.print("external = ");
        for (int i=0; i<externals; ++i)
        {
            System.out.print(external[i] + " ");
        }
        
        // Print subject matchs
        System.out.println();
        System.out.println();
        System.out.println("MatchList:");
        for (int i=0; i<matchs; ++i)
        {
            SubjectMatch tempMatch = matchList.get(i);
            
            System.out.println("match(" + tempMatch.getStudent() + ", " + 
                    tempMatch.getProfessor() + ") = " + tempMatch.getMatch());
        }
        
        // Print slots and rooms
        System.out.println();
        System.out.println("slots = " + slots + ", rooms = " + rooms);
    }
    
    // Initiate variables
    public void InitVariables() {
        solutionPro = new VarIntLS[students][5];
        
        for (int j = 0; j < students; ++j) {
            solutionPro[j][0] = new VarIntLS(manager, internals, professors  - 1);
            solutionPro[j][1] = new VarIntLS(manager, 0, internals - 1);
            solutionPro[j][2] = new VarIntLS(manager, 0, internals - 1);
            solutionPro[j][3] = new VarIntLS(manager, 0, internals - 1);
            solutionPro[j][4] = new VarIntLS(manager, internals, professors - 1);
        }

        /*
        solutionPro = new VarDiscreteIntLS[students][5];
        HashSet<Integer> internalProfs = new HashSet<Integer> (internals, 1.0f), internalMembers;
        for (int i : internal) internalProfs.add(i);

        HashSet<Integer> externalProfs = new HashSet<Integer> (externals, 1.0f), externalMembers;
        for (int i : external) externalProfs.add(i);
        for (int j = 0; j < students; ++j) {
            // jury members is external professor
            externalMembers = (HashSet<Integer>) externalProfs.clone();
            externalMembers.remove(juryList[j].getSupervisorID());
            solutionPro[j][0] = new VarDiscreteIntLS(manager, externalMembers);
            solutionPro[j][4] = new VarDiscreteIntLS(manager, externalMembers);

            // jury members is internal professor
            internalMembers = (HashSet<Integer>) internalProfs.clone();
            internalMembers.remove(juryList[j].getSupervisorID());
            solutionPro[j][1] = new VarDiscreteIntLS(manager, internalMembers);
            solutionPro[j][2] = new VarDiscreteIntLS(manager, internalMembers);
            solutionPro[j][3] = new VarDiscreteIntLS(manager, internalMembers);
        }
        */

        solutionRoom = new VarIntLS[students];
        solutionSlot = new VarIntLS[students];
        
        for (int j = 0; j < students; ++j) {
            solutionSlot[j] = new VarIntLS(manager, 1, slots);
            solutionRoom[j] = new VarIntLS(manager, 1, rooms);
        }

    }

    public void InitRandomSolution() {
        // int ss = students / slots;
        for (int s = 0; s < students; ++s)
        {
            solutionSlot[s].setValue(rand.nextInt(slots) + 1);
            solutionRoom[s].setValue(rand.nextInt(rooms) + 1);
            for (int i = 0; i < 5; ++i) {
                int diff = solutionPro[s][i].getMaxValue() - solutionPro[s][i].getMinValue() + 1;
                solutionPro[s][i].setValue(rand.nextInt(diff) + solutionPro[s][i].getMinValue());
            }
        }
    }

    // 
    public void PostObjectives()
    {
        // occ(p) = sum(xp(s, i) == p), s∈S, i∈{0,...,4}
        VarIntLS[] solutionPro1D = new VarIntLS[students * 5];
        for (int s= 0; s < students; ++s){
            for(int i = 0; i < 5; ++i)
                solutionPro1D[s*5+i] = solutionPro[s][i];
        }

        occurence = new IFunction[professors];
        for (int p=0; p<professors; ++p)
        {
            occurence[p] = new Occurrence(solutionPro1D, p);
        }
        
        // Minimize max{occ(p) : p ∈ P} - min{occ(p) : p ∈ P}
        IFunction minFunc = new FMin(occurence);
        IFunction maxFunc = new FMax(occurence);
        diffFunc = new FuncMinus(maxFunc, minFunc);
        
        /*
        // rp(p, t) ∈ R ∪ {0}, ∀p ∈ P, t ∈ SL
        professorRoom = new VarIntLS[professors][slots];
        for (int i=0; i<professors; ++i)
            for (int j=0; j<slots; ++j)
            {
                professorRoom[i][j] = new VarIntLS(manager, 0, rooms-1);
            }
        
        //
        int sum = 0;

        for (int s = 0; s < students; ++s) {
            for (int i = 0; i < 5; ++i) {
                sum += matchList.get(i)
                        
            }
        }

        
        // ts(p) = {t ∈ SL | rp(p, t) > 0}, ∀p ∈ P
        slotTime = new VarIntLSArrayList[professors];
        for (int p=0; p<professors; ++p)
        {
            slotTime[p] = new VarIntLSArrayList();
            
            for (int i=0; i<slots; ++i)
                if (professorRoom[p][i].getValue() > 0)
                {
                    VarIntLS temp = new VarIntLS(manager, 1, slots);
                    temp.setValue(i);
                    slotTime[p].add(temp);
                }
        }
        
        // 
        minSlotTime = new VarIntLS[professors];
        maxSlotTime = new VarIntLS[professors];
        
        for (int p=0; p<professors; ++p)
        {
            minSlotTime[p] = new VarIntLS(manager, 1, slots);
            maxSlotTime[p] = new VarIntLS(manager, 1, slots);
            
            if (slotTime[p].size() > 0)
            {
                minSlotTime[p].setValue(slotTime[p].get(0).getValue());
                maxSlotTime[p].setValue(slotTime[p].get(slotTime[p].size()-1).getValue());
            }
            else
            {
                minSlotTime[p].setValue(0);
                maxSlotTime[p].setValue(0);
            }
        }
        */

        // 
    }
    
    // Post constraint
    public void PostConstraints() {
        // all difference
        for (int s = 0; s < students; ++s) {
            int[] supervisor = { professorMap.get(juryList[s].getSupervisorID()) };
            system.post(new AllDifferentVarIntLSInt(solutionPro[s], supervisor));
        }


        // xp(s1, i) = xp(s2, j) ⇒ xs(s1) != xs(s2), ∀s1 != s2 ∈ S, i, j ∈ {0, . . . , 4}
        for (int s1 = 0; s1 < students - 1; s1++)   {
            for (int s2 = s1+1; s2 < students; s2++) {
                int[][] idss = { {0, 4}, {1, 2, 3} };

                for (int[] ids : idss) {
                    for (int i : ids) {
                        for (int j : ids) {
                            system.post(
                                new Implicate(
                                    new IsEqual(solutionPro[s1][i], solutionPro[s2][j]),
                                    new NotEqual(solutionSlot[s1], solutionSlot[s2])
                                )
                            );
                        }
                    }
                }

                /*
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 5; j++) {
                        system.post(
                            new Implicate(
                                new IsEqual(solutionPro[i][s1], solutionPro[j][s2]),
                                new NotEqual(solutionSlot[s1], solutionSlot[s2]
                                )
                            )
                        );
                    }
                }
                */
            }
        }
        
        // xr(s1) = xr(s2) ⇒ xs(s1) != xs(s2), ∀s1 != s2 ∈ S
        for (int s1 = 0; s1 < students - 1; s1++)   {
            for (int s2 = s1+1; s2 < students; s2++) {
                system.post(
                        new Implicate(
                                new IsEqual(solutionRoom[s1], solutionRoom[s2]),
                                new NotEqual(solutionSlot[s1], solutionSlot[s2])
                        )
                );
            }
        }
        
    }

    // number of occurrences of professor p, ∀p ∈ P
    private int getOccurence(int professor)
    {
        int counter = 0;
        
            for (int j=0; j<students; ++j)
            {
                for (int i=0; i<5; ++i)
                  if (solutionPro[j][i].getValue() == professor)
                    ++counter;
            }
        
        return counter;
    }
    
    
    // Print solution
    public void PrintSolution() {
        System.out.println("Violation = " + system.violations() + "\n\n");
        for (int sl = 1; sl <= slots; ++sl) {
            System.out.println("Slot " + sl);
            for (int j = 0; j < students; ++j)
            {
                if (solutionSlot[j].getValue() != sl) { continue; }

                System.out.printf("Student: %03d, supervisor %03d, examiner1 %03d, examiner2 %03d, president %03d, secretary %03d, extra member %03d, room %03d\n",
                                  j, professorMap.get(juryList[j].getSupervisorID()), 
                                  solutionPro[j][0].getValue(), solutionPro[j][1].getValue(), 
                                  solutionPro[j][2].getValue(), solutionPro[j][3].getValue(), 
                                  solutionPro[j][4].getValue(), solutionRoom[j].getValue());

            }
            System.out.println();
        }

        System.out.println("\n\nObjectives:");

        // diffFunc
        int minOcc = students+1, maxOcc = -1;
        int minOccProf = 0, maxOccProf = 0;
        for (int p = 0; p < professors; ++p) {
            int occ = getOccurence(p);
            if (occ > maxOcc) {
                maxOcc = occ;
                maxOccProf = p;
            } 
            if (occ < minOcc) {
                minOcc = occ;
                minOccProf = p;
            }
        }
        System.out.printf("occ(professors %02d) = %02d, occ(professors %02d) = %02d, diff = %02d\n",
                          maxOccProf, maxOcc, minOccProf, minOcc, maxOcc - minOcc);

    }

    // Apply local search to solve problem
    public void LocalSearch() {
        TabuSearch2 tab = new TabuSearch2();
        System.out.println("Init violation: " + system.violations());
        //tab.search(system, 300, 3000, 2000, 10);
        tab.searchMaintainConstraints(diffFunc, system, 300, 3000, 2000, 10);
    }

    // Main
    public static void main(String[] args) {
        DefenseTimetablingOpenLocalSearch algorithm
                = new DefenseTimetablingOpenLocalSearch();

        algorithm.initSystem();
        algorithm.LocalSearch();
        System.out.println("\n\n");
        algorithm.PrintSolution();
    }
}
