public class PCB
{
    public int Job_ID;                  // Job ID
    public int TTL;                     // Total Time Limit
    public int TLL;                     // Total Line Limit
    public int TTC;                     // Total Time Counter
    public int TLC;                     // Total Line Counter

    public int outLineCount;            // Number of Output Lines
    
    public int P;                       // Track number of Program Cards in Drum
    public int D;                       // Track number of Data Cards in Drum
    public int OutStart;                // Track number of Output in Drum
    public int OutOffset;               // Offset of output  

    public int PCount;                  // Program card count
    public int DCount;                  // Data card count

    public boolean PFirst;              // First Program Card
    public boolean DFirst;              // First Data Card
    public boolean OFirst;              // First Output Card

    public int IC;                      // Instruction Counter
    public int EM;                      // End message

    public int EM1;
    public int EM2;

    public int PTR;                     // PTR for the Job                    
    public int PTO;                     // Page Table Offset

    public PCB(char[] buffer)
    {
        Job_ID = Integer.parseInt(""+buffer[4]+buffer[5]+buffer[6]+buffer[7]);
        TTL = Integer.parseInt(""+buffer[8]+buffer[9]+buffer[10]+buffer[11]);
        TLL = Integer.parseInt(""+buffer[12]+buffer[13]+buffer[14]+buffer[15]);
        System.out.println("Initializing new PCB for JOB : " + Job_ID);
        outLineCount = TLL;
        TTC = 0;
        TLC = 0;
        D = 0;
        P = 0;
        OutStart = 0;
        OutOffset = 0;
        DCount = 0;
        PCount = 0;
        PFirst = true;
        DFirst = true;
        OFirst = true;
        IC = 0;   
        EM = 0;
        PTR = -1;
        PTO = 0;
        EM1 = -1;
        EM2 = -1;
        PrintPCB(this);
    }
    public static void PrintPCB(PCB tempPCB)
    {        
        System.out.println("\n------------------------------------------\n");
        System.out.println("JOB " + tempPCB.Job_ID);
        System.out.println("TTL " + tempPCB.TTL);
        System.out.println("TLL " + tempPCB.TLL);
        System.out.println("TTC " + tempPCB.TTC);
        System.out.println("TLC " + tempPCB.TLC);
        System.out.println("IC " + tempPCB.IC);
        System.out.println("PTR " + tempPCB.PTR);
        // System.out.println("PTO " + tempPCB.PTO);
        // System.out.println("PCount " + tempPCB.PCount);
        // System.out.println("PFirst " + tempPCB.PFirst);
        // System.out.println("DCount " + tempPCB.DCount);
        // System.out.println("DFirst " + tempPCB.DFirst);
        // System.out.println("OFirst " + tempPCB.OFirst);
        // System.out.println("OutStart" + tempPCB.OutStart);
        // System.out.println("outLineCount " + tempPCB.outLineCount);
        System.out.println("\n------------------------------------------\n");
    }
}