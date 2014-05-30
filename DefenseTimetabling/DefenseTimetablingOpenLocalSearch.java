//package DefenseTimetabling;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import localsearch.constraints.basic.Implicate;
import localsearch.constraints.basic.IsEqual;
import localsearch.constraints.basic.NotEqual;
import localsearch.constraints.basic.NotOverLap;
import localsearch.constraints.alldifferent.AllDifferent;
import localsearch.functions.basic.FuncMinus;
import localsearch.functions.occurrence.Occurrence;
import localsearch.functions.max_min.FMax;
import localsearch.functions.max_min.FMin;
import localsearch.functions.sum.Sum;
import localsearch.model.ConstraintSystem;
import localsearch.model.IFunction;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;
import localsearch.model.IConstraint;
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
    //private LinkedList<SubjectMatch> matchList;   // Subject match between numProfessors and student
    private HashMap<Integer, Integer> subjectMatch;   // Subject match between numProfessors and student
    private HashMap<Integer, Integer> professorMap; // Map from supervisorID to varint value
    
    private int matchs;                 // Number of subject matchs
    private int numStudents;               // Number of numStudents

    private int[] internal;             // ID of internal professor i
    private int[] external;             // ID of external professor i
    private int[] professorList;             

    private int numInternals;              // Number of internal numProfessors
    private int numExternals;              // Number of external numProfessors
    private int numProfessors;             // Number of numProfessors
    
    private int numSlots;                  // Number of numSlots
    private int numRooms;                  // Number of numRooms


    private IFuncFactory funcFactory;
    // objective functions
    private IFunction diffFunc;         // Objective 1, = Max (occurence) - Min (occurence)
    private IFunction sumMatchFunc;     // Objective 2, = Sum(m(s, xp(s, {0, 1})))
    private FConsecutive[] consecutiveFuncs;   
    private IFunction sumConsecutive;   // Objective 3, = Sum(C(p))

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
            numStudents = Integer.parseInt(scanner.next());

            juryList = new Jury[numStudents];
            
            for (int i = 0; i < 9; ++i) {
                str = scanner.next();
            }

            for (int i = 0; i < numStudents; ++i) {
                juryList[i] = new Jury(
                        scanner.nextInt(), scanner.nextInt(), scanner.nextInt(), 
                        scanner.nextInt(), scanner.nextInt(), scanner.nextInt(), 
                        scanner.nextInt(), scanner.nextInt(), scanner.nextInt()
                );
            }

            // Read internal numProfessors
            str = scanner.next();
            str = scanner.next();
            numInternals = Integer.parseInt(scanner.next());
            internal = new int[numInternals];

            professorMap = new HashMap<Integer, Integer>();
            for (int i = 0; i < numInternals; ++i) {
                internal[i] = scanner.nextInt();
                professorMap.put(internal[i], i);
            }

            // Read external numProfessors
            str = scanner.next();
            str = scanner.next();
            numExternals = Integer.parseInt(scanner.next());
            external = new int[numExternals];

            for (int i = 0; i < numExternals; ++i) {
                external[i] = scanner.nextInt();
                professorMap.put(external[i], i + numInternals);
            }
            
            numProfessors = numInternals + numExternals;
            //professorList = new int[numProfessors];

            // Read subject match list
            for (int i = 0; i < 6; ++i) {
                str = scanner.next();
            }

            //matchList = new LinkedList<SubjectMatch>();
            subjectMatch = new HashMap<Integer, Integer>();
            
            try {
                do {
                    //matchList.add(new SubjectMatch(scanner.nextInt(), 
                    //        scanner.nextInt(), scanner.nextInt()));
                    subjectMatch.put(scanner.nextInt() * numProfessors + professorMap.get(scanner.nextInt()), scanner.nextInt());
                } while (scanner.hasNext());
            } catch (java.util.InputMismatchException e) {

            }
            
            //matchs = matchList.size();
            matchs = subjectMatch.size();
            
            // Read number of numSlots and numRooms
            for (int i = 0; i < 7; ++i) {
                str = scanner.next();
            }
            
            numSlots = scanner.nextInt();
            numRooms = scanner.nextInt();

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
        
        for (int i=0; i<numStudents; ++i)
        {
            System.out.printf("%10d %10d %10d %10d %10d %10d %10d %10d %10d\n", 
                    juryList[i].getStudentID(), juryList[i].getSupervisorID(),
                    juryList[i].getExaminer1ID(), juryList[i].getExaminer2ID(), 
                    juryList[i].getPresidentID(), juryList[i].getSecretaryID(),
                    juryList[i].getAdditionalMemberID(), juryList[i].getSlot(),
                    juryList[i].getRoom());
        }
        
        // Print internal numProfessors
        System.out.println();
        System.out.print("internal = ");
        for (int i=0; i<numInternals; ++i)
        {
            System.out.print(internal[i] + " ");
        }
        
        // Print external numProfessors
        System.out.println();
        System.out.print("external = ");
        for (int i=0; i<numExternals; ++i)
        {
            System.out.print(external[i] + " ");
        }
        
        // Print subject matchs
        System.out.println();
        System.out.println();
        System.out.println("MatchList:");
        for (int i=0; i<matchs; ++i)
        {
            /*
            SubjectMatch tempMatch = matchList.get(i);
            
            System.out.println("match(" + tempMatch.getStudent() + ", " + 
                    tempMatch.getProfessor() + ") = " + tempMatch.getMatch());
            */
        }
        
        // Print numSlots and numRooms
        System.out.println();
        System.out.println("numSlots = " + numSlots + ", numRooms = " + numRooms);
    }
    
    // Initiate variables
    public void InitVariables() {
        solutionPro = new VarIntLS[numStudents][5];
        
        for (int j = 0; j < numStudents; ++j) {
            solutionPro[j][0] = new VarIntLS(manager, numInternals, numProfessors  - 1);
            solutionPro[j][1] = new VarIntLS(manager, 0, numInternals - 1);
            solutionPro[j][2] = new VarIntLS(manager, 0, numInternals - 1);
            solutionPro[j][3] = new VarIntLS(manager, 0, numInternals - 1);
            solutionPro[j][4] = new VarIntLS(manager, numInternals, numProfessors - 1);
        }

        /*
        solutionPro = new VarDiscreteIntLS[numStudents][5];
        HashSet<Integer> internalProfs = new HashSet<Integer> (numInternals, 1.0f), internalMembers;
        for (int i : internal) internalProfs.add(i);

        HashSet<Integer> externalProfs = new HashSet<Integer> (numExternals, 1.0f), externalMembers;
        for (int i : external) externalProfs.add(i);
        for (int j = 0; j < numStudents; ++j) {
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

        solutionRoom = new VarIntLS[numStudents];
        solutionSlot = new VarIntLS[numStudents];
        
        for (int j = 0; j < numStudents; ++j) {
            solutionSlot[j] = new VarIntLS(manager, 1, numSlots);
            solutionRoom[j] = new VarIntLS(manager, 1, numRooms);
        }

    }

    public void InitRandomSolution() {
        // int ss = numStudents / numSlots;
        for (int s = 0; s < numStudents; ++s)
        {
            solutionSlot[s].setValue(rand.nextInt(numSlots) + 1);
            solutionRoom[s].setValue(rand.nextInt(numRooms) + 1);
            for (int i = 0; i < 5; ++i) {
                int diff = solutionPro[s][i].getMaxValue() - solutionPro[s][i].getMinValue() + 1;
                solutionPro[s][i].setValue(rand.nextInt(diff) + solutionPro[s][i].getMinValue());
            }
        }
    }

    // 
    public void PostObjectives()
    {
        //
        // occ(p) = sum(xp(s, i) == p), s∈S, i∈{0,...,4}
        //
        VarIntLS[] solutionPro1D = new VarIntLS[numStudents * 5];
        for (int s= 0; s < numStudents; ++s){
            for(int i = 0; i < 5; ++i)
                solutionPro1D[s*5+i] = solutionPro[s][i];
        }

        occurence = new IFunction[numProfessors];
        for (int p=0; p<numProfessors; ++p)
        {
            occurence[p] = new Occurrence(solutionPro1D, p);
        }
        
        // Minimize max{occ(p) : p ∈ P} - min{occ(p) : p ∈ P}
        IFunction minFunc = new FMin(occurence);
        IFunction maxFunc = new FMax(occurence);
        diffFunc = new FuncMinus(maxFunc, minFunc);
        

        //
        // Maximize match
        //

        IFunction[] fmatchs = new IFunction[numStudents * 2];
        for (int s = 0; s < numStudents; ++s) {
            fmatchs[s * 2]      = new FMatch(s, solutionPro[s][0], subjectMatch, numProfessors);
            fmatchs[s * 2 + 1]  = new FMatch(s, solutionPro[s][1], subjectMatch, numProfessors);
            //fmatchs[s * 5 + 2]  = new FMatch(s, solutionPro[s][2], subjectMatch, numProfessors);
            //fmatchs[s * 5 + 3]  = new FMatch(s, solutionPro[s][3], subjectMatch, numProfessors);
            //fmatchs[s * 5 + 4]  = new FMatch(s, solutionPro[s][4], subjectMatch, numProfessors);
        }
        sumMatchFunc = new Sum(fmatchs);
         

        //
        // Maximize consecutive
        //

        funcFactory = new IFuncFactory(solutionPro, solutionSlot, solutionRoom);
        consecutiveFuncs = new FConsecutive[numProfessors];
        for (int p = 0; p < numProfessors; ++p) 
            consecutiveFuncs[p] = funcFactory.getConsecutiveFunc(p);
        sumConsecutive = new Sum(consecutiveFuncs);

    }
    
    // Post constraint
    public void PostConstraints() {
        // all difference
        for (int s = 0; s < numStudents; ++s) {
            int[] supervisor = { professorMap.get(juryList[s].getSupervisorID()) };
            system.post(new AllDifferentVarIntLSInt(solutionPro[s], supervisor));
        }


        // xp(s1, i) = xp(s2, j) ⇒ xs(s1) != xs(s2), ∀s1 != s2 ∈ S, i, j ∈ {0, . . . , 4}
        for (int s1 = 0; s1 < numStudents - 1; s1++)   {
            for (int s2 = s1+1; s2 < numStudents; s2++) {
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
        for (int s1 = 0; s1 < numStudents - 1; s1++)   {
            for (int s2 = s1+1; s2 < numStudents; s2++) {
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
        
            for (int j=0; j<numStudents; ++j)
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

        // per student/slot
        for (int sl = 1; sl <= numSlots; ++sl) {
            System.out.println("Slot " + sl);
            for (int j = 0; j < numStudents; ++j)
            {
                if (solutionSlot[j].getValue() != sl) { continue; }

                System.out.printf("(Student: %03d, supervisor %03d), examiner1 %03d, examiner2 %03d, president %03d, secretary %03d, extra member %03d, room %03d, match = %03d\n",
                                  j, professorMap.get(juryList[j].getSupervisorID()), 
                                  solutionPro[j][0].getValue(), solutionPro[j][1].getValue(), 
                                  solutionPro[j][2].getValue(), solutionPro[j][3].getValue(), 
                                  solutionPro[j][4].getValue(), solutionRoom[j].getValue(),
                                  subjectMatch.get(j * numProfessors + solutionPro[j][0].getValue()) +
                                  subjectMatch.get(j * numProfessors + solutionPro[j][1].getValue())
                                  );

            }
            System.out.println();
        }

        // per professor
        System.out.println("\n\nProfessors:");
        String[] positions = { "examiner1", "examiner2", "president", "secretary", "extra member" };
        for (int p = 0; p < numProfessors; ++p) {
            System.out.printf("Professor: %03d, ID = %03d, %s professor\n", p, 
                              (p >= numInternals) ? external[p - numInternals] : internal[p],
                              ( (p >= numInternals) ? "external" : "internal" ) );
            int s, i;
            for (s = 0; s < numStudents; ++s) {
                for (i = 0; i < 5; ++i) {
                    if (solutionPro[s][i].getValue() == p)
                        break;
                }
                if (i < 5)
                    System.out.printf("%15s of student #%03d, slot %02d, room %02d, match = %02d\n",
                                      positions[i], s, solutionSlot[s].getValue(),
                                      solutionRoom[s].getValue(), subjectMatch.get(s * numProfessors + p));
            }
            System.out.printf("Consecutive = %02d\n\n", consecutiveFuncs[p].getValue());
        }

        System.out.println("\n\nObjectives:");

        // diffFunc
        int minOcc = numStudents+1, maxOcc = -1;
        int minOccProf = 0, maxOccProf = 0;
        for (int p = 0; p < numProfessors; ++p) {
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
        System.out.printf("occ(numProfessors %02d) = %02d, occ(numProfessors %02d) = %02d, diff = %02d\n",
                          maxOccProf, maxOcc, minOccProf, minOcc, maxOcc - minOcc);

        // sumMatchFunc
        System.out.printf("sum match = %03d\n", sumMatchFunc.getValue());

        // sumConsecutive
        System.out.printf("sum consecutive = %03d\n", sumConsecutive.getValue());

    }

    // Export result to HTML file
    public void exportHtml(String fileName) {
        StringBuilder content = new StringBuilder();
        content.append("<!doctype html>\n");
        content.append("<html>\n");
        content.append("<head>\n");
        content.append("<title>SoICT Master Thesis Defense Timetabling</title>\n");
        content.append("<style type=\"text/css\">\n");
        content.append("body");
        content.append("{\n");
        content.append("font-family: arial;\n");
        content.append("}\n");
        content.append("th,td\n");
        content.append("{\n");
        content.append("margin: 0;\n");
        content.append("text-align: center;\n");
        content.append("border-collapse: collapse;\n");
        content.append("outline: 1px solid #e3e3e3;\n");
        content.append("}\n");
        content.append("td\n");
        content.append("{\n");
        content.append("padding: 5px 10px;\n");
        content.append("}\n");

        content.append("th\n");
        content.append("{\n");
        content.append("background: #666;\n");
        content.append("color: white;\n");
        content.append("padding: 5px 10px;\n");
        content.append("}\n");

        content.append("td:hover\n");
        content.append("{\n");
        content.append("cursor: pointer;\n");
        content.append("background: #666;\n");
        content.append("color: white;\n");
        content.append("}\n");
        content.append("</style>\n");

        content.append("</head>\n");
        content.append("<body>\n");
        content.append("<h1 align=\"center\">SoICT Master Thesis Defense Timetabling</h2>\n");
        
        //   
        for (int i=1; i<=numSlots; ++i) {
            content.append("<h2 align=\"left\">Slot ").append(i).append("</h2>\n");
            content.append("<table align=\"center\" >\n");
                
            String[] colHeaders = {
                "Student", "Supervisor", "Examiner 1", "Examiner 2", 
                "President", "Secretary", "Extra member", "Room", "Match"};
                
            content.append("<tr>\n");
            for (String header : colHeaders)    {
                content.append("<th colspan=\"6\">").append(header).append("</th>\n");
            }
            content.append("</tr>\n");
            
            for (int j = 0; j < numStudents; ++j) {
                if (solutionSlot[j].getValue() != i) {
                    continue;
                }
                
                int[] colValues = {
                    j, professorMap.get(juryList[j].getSupervisorID()),
                    solutionPro[j][0].getValue(), solutionPro[j][1].getValue(),
                    solutionPro[j][2].getValue(), solutionPro[j][3].getValue(),
                    solutionPro[j][4].getValue(), solutionRoom[j].getValue(),
                    subjectMatch.get(j * numProfessors + solutionPro[j][0].getValue()) + 
                        subjectMatch.get(j * numProfessors + solutionPro[j][1].getValue())
                };
                        
                content.append("<tr>\n");
                for (int val : colValues)   {
                    content.append("<td colspan=\"6\">").append(val).append("</td>\n");
                }
                content.append("</tr>\n\n\n");
            }
            
            content.append("</table>\n");
        }
        
        //
        content.append("<h2 align=\"left\">Professors:").append("</h2>\n");

        String[] roles = {"Examiner 1", "Examiner 2", "President", "Secretary", "Extra member"};
        String[] colHeaders = {
            "Role", "Student", "Slot", "Room", "Match"
        };
                    
        for (int p = 0; p < numProfessors; ++p) {
            content.append("<h3 align=\"left\">Professors ").append(p).
                    append(", ID = ").append((p >= numInternals) ? external[p - numInternals] : internal[p]).
                    append(", ").append((p >= numInternals) ? "external" : "internal").append(" professor").
                    append("</h3>\n");
        
            content.append("<table align=\"center\" >\n");
            content.append("<tr>\n");
            for (String header : colHeaders) {
                content.append("<th colspan=\"6\">").append(header).append("</th>\n");
            }
            content.append("</tr>\n");

            int s, i;
            for (s = 0; s < numStudents; ++s) {
                for (i = 0; i < 5; ++i) {
                    if (solutionPro[s][i].getValue() == p) {
                        break;
                    }
                }
            
                if (i < 5) {
                    String[] colValues = {
                        roles[i], 
                        ((Integer)s).toString(),
                        ((Integer)solutionSlot[s].getValue()).toString(),
                        ((Integer)solutionRoom[s].getValue()).toString(),
                        (subjectMatch.get(s * numProfessors + p)).toString()
                    };
                    
                    content.append("<tr>\n");
                    for (String val : colValues) {
                        content.append("<td colspan=\"6\">").append(val).append("</td>\n");
                    }
                    content.append("</tr>\n\n\n");
                }
            }
            
            content.append("</table>\n");
            content.append("<h4 align=\"left\">Consecutive = ").append(consecutiveFuncs[p].getValue()).append("</h4>\n");
        }
        
        content.append("<h2 align=\"left\">Objectives:").append("</h2>\n");
        content.append("<table align=\"center\" >\n");
        content.append("<tr>\n");
        content.append("<th>Objectives</th><th>Value</th>");
        content.append("</tr>\n");

        content.append("<tr>\n");
        content.append("<td> Diff occurence </td><td> ").append(diffFunc.getValue()).append(" </td>");
        content.append("</tr>\n");
        
        content.append("<tr>\n");
        content.append("<td> Sum match </td><td> ").append(sumMatchFunc.getValue()).append(" </td>");
        content.append("</tr>\n");
        
        content.append("<tr>\n");
        content.append("<td> Sum consecutive </td><td> ").append(sumConsecutive.getValue()).append(" </td>");
        content.append("</tr>\n");
        
        content.append("</table>\n");
        
        content.append("</body>\n");
        content.append("</html>\n");

        // Print        
        try {
            try (PrintStream print = new PrintStream(new File(fileName))) {
                print.print(content.toString());
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DefenseTimetablingOpenLocalSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("Exported successful!!");
    }

    // Apply local search to solve problem
    public void LocalSearch(int objectives) {
        TabuSearch2 tab = new TabuSearch2();
        IFunction[] oldFunc;
        
        System.out.printf("Init violation: %d\ndiffFunc = %d\nsumMatchFunc = %d\n", 
            system.violations(), diffFunc.getValue(), sumMatchFunc.getValue());
        
        switch (objectives) {
            case 0:
                tab.search(system, 300, 3000, 500, 10);
                break;
                
            case 1:
                try {
                //LocalSearch(0);
                tab.searchMaintainConstraints(diffFunc, system, 300, 3000, 400, 10);
                //tab.searchMaintainConstraintFunction(sumMatchFunc, system, 500, 3000, 1000, 50);
                } catch (Exception e) {
                    PrintSolution();
                    //System.out.print("Occurences = [");
                    //for (IFunction f : occurence) {
                    //    System.out.printf("%d, ", f.getValue()); 
                    //}
                    //System.out.printf("], max = %d, min = %d\n", minOcc.getValue(), maxOcc.getValue());
                    //exportHtml("result.html");
                    throw e;
                }
                break;
                
            case 2:
                oldFunc = new IFunction[] {diffFunc};
                //oldFunc = new IFunction[] {sumMatchFunc};
                LocalSearch(1);
                tab.searchMaintainConstraintsFunction(sumMatchFunc, oldFunc, system, 700, 3000, 2000, 10);
                //tab.searchMaintainConstraintFunctionFuncArr(diffFunc, oldFunc, system, 300, 500, 500, 50);
                break;
               
            case 3:
                oldFunc = new IFunction[] {diffFunc, sumMatchFunc};
                LocalSearch(2);
                tab.searchMaintainConstraintsFunction(sumConsecutive, oldFunc, system, 300, 3000, 500, 10);
                break;
        }        
            
        System.out.printf("Consecutive = [");
        for (IFunction f : consecutiveFuncs) {
            System.out.printf("%d, ", f.getValue());
        }
        System.out.printf("], sum = %d\n", sumConsecutive.getValue());
    }

    public void test() {
        int s = 0, s2 = 1, s3 = 2;
        int prof = 1;
        System.out.println("Init");
        PrintSolution();
        System.out.printf("Consecutive = [");
        for (IFunction f : consecutiveFuncs) System.out.printf("%d, ", f.getValue());
        System.out.printf("], sum = %d\n", sumConsecutive.getValue());

        System.out.println();
        for (int i = 0; i < 5; ++i) solutionPro[s][i].setValuePropagate(i);
        FConsecutive fcons = consecutiveFuncs[prof];
        //fcons.debug();
        //PrintSolution();
        System.out.printf("Consecutive = [");
        for (IFunction f : consecutiveFuncs) System.out.printf("%d, ", f.getValue());
        System.out.printf("], sum = %d\n", sumConsecutive.getValue());
        System.out.println();

        solutionSlot[s].setValuePropagate(1);
        solutionPro[s][1].setValuePropagate(2);
        //fcons.propagateInt(solutionPro[s][1], 2);
        //fcons.debug();
        //PrintSolution();
        System.out.printf("Consecutive = [");
        for (IFunction f : consecutiveFuncs) System.out.printf("%d, ", f.getValue());
        System.out.printf("], sum = %d\n", sumConsecutive.getValue());
        System.out.println();

        solutionSlot[s2].setValuePropagate(3);
        solutionPro[s2][3].setValuePropagate(prof);
        //fcons.debug();
        //PrintSolution();
        System.out.printf("Consecutive = [");
        for (IFunction f : consecutiveFuncs) System.out.printf("%d, ", f.getValue());
        System.out.printf("], sum = %d\n", sumConsecutive.getValue());
        System.out.println();

        solutionSlot[s3].setValuePropagate(1);
        solutionPro[s3][2].setValuePropagate(prof);
        //fcons.debug();
        //PrintSolution();
        System.out.printf("Consecutive = [");
        for (IFunction f : consecutiveFuncs) System.out.printf("%d, ", f.getValue());
        System.out.printf("], sum = %d\n", sumConsecutive.getValue());
        System.out.println();

        solutionSlot[s].setValuePropagate(2);
        solutionPro[s][1].setValuePropagate(prof);
        //fcons.debug();
        //PrintSolution();
        System.out.printf("Consecutive = [");
        for (IFunction f : consecutiveFuncs) System.out.printf("%d, ", f.getValue());
        System.out.printf("], sum = %d\n", sumConsecutive.getValue());
        System.out.println();

        solutionSlot[s].setValuePropagate(4);
        //fcons.debug();
        //PrintSolution();
        System.out.printf("Consecutive = [");
        for (IFunction f : consecutiveFuncs) System.out.printf("%d, ", f.getValue());
        System.out.printf("], sum = %d\n", sumConsecutive.getValue());
        System.out.println();
    }

    // Main
    public static void main(String[] args) {
        DefenseTimetablingOpenLocalSearch algorithm
                = new DefenseTimetablingOpenLocalSearch();

        int objective = 3;
        if (args.length > 0) {
            objective = Integer.parseInt(args[0]);
        }

        algorithm.initSystem();
        algorithm.LocalSearch(3);
        System.out.println("\n\n");
        algorithm.PrintSolution();
        algorithm.exportHtml("result.html");
        //algorithm.test();
    }
}
