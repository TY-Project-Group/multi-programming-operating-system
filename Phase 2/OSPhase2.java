import java.io.*;
import java.util.*;

public class OSPhase2
{
    public static final String INPUT_FILE = "Input.txt";    // Input File
    public static final String OUTPUT_FILE = "Output.txt";  // Output File

    public static char[][] M;                   // Memory
    public static char[] R;                     // General Purpose Register
    public static char[] IR;                    // Instruction Register
    public static int IC;                       // Instrucation Counter
    public static boolean C;                    // Toggle Register
    public static int SI;                       // Service Interrupt
    public static int TI;                       // Time Limit Interrupt
    public static int PI;                       // Line Limit Interrupt
    public static int EM;                       // End Message
    public static int PTR;                      // Page Table Register
    public static int PTO;                      // Page Table Offset
    public static int VA;                       // Virtual Address
    public static int RA;                       // Real Address

    public static boolean[] AllocateArray;      // Allocation monitoring array
    public static PCB PCB;                      // Process Control Block

    public static int m;                        // Memory Used
    public static char[] buffer;                // Card Storage Buffer
    
    // File read and write operators
    public static FileReader input;
    public static FileWriter output;
    public static BufferedReader in;    
    public static BufferedWriter out;

    public static void Initialization() throws IOException
    {
        M = new char[300][4];
        R = new char[4];
        IR = new char[4];
        IC = 0;
        C = false;   
        SI = 0;
        PI = 0;
        TI = 0;
        EM = 0;
        PTO = 0;
        VA = 0;
        buffer = new char[40]; 
        input = new FileReader(INPUT_FILE);
        output = new FileWriter(OUTPUT_FILE);
        in = new BufferedReader(input);
        out = new BufferedWriter(output);
    }

    public static void READ() throws IOException
    {
        //IR[3] = '0';
        //int loc = Integer.parseInt(""+IR[2]+IR[3]);
        int loc = RA;
        String card = in.readLine();
        System.out.println("IN READ: ");
        System.out.println("Card: " + card);

        if (card.length() >= 4)
        {             
            if (card.substring(0,4).equals("$END"))
            {     
                TERMINATE(1, -1);
                return;
            }
        }

        buffer = card.toCharArray();

        for (int i = 0; i < card.length(); i++)
        {
            M[loc][i%4] = buffer[i];
            if ((i+1)%4 == 0)
            {
                loc++;
            }
        }        
    }

    public static void WRITE() throws IOException
    {
        System.out.println("In WRITE:");
        //IR[3] = '0';
        PCB.TLC++;
        if (PCB.TLC > PCB.TLL)
        {
            TERMINATE(2, -1);
            return;
        }
        //int loc = Integer.parseInt(""+IR[2]+IR[3]);
        int loc = RA;
        String data = "";
        for (int i = 0; i < 10; i++)
        {
            data = data + new String(M[loc + i]);   
        }
        System.out.println("Card: " + data);
        output.write(data);
        output.write("\n");       
    }

    public static int Allocate()
    {
        int temp = (int)(Math.random() * 30);
        while(AllocateArray[temp] == true)
        {
            temp = (int)(Math.random() * 30);
        } 
        AllocateArray[temp] = true;
        return temp;         
    }

    public static void LOAD() throws IOException
    {        
        String card;

        while((card = in.readLine())!=null) 
        {
            //System.out.print("IN LOAD: ");
            //System.out.println(card);
            buffer = card.toCharArray();

            if(buffer[0] == '$')
            {            
                // Control Cards
                if ((""+buffer[0]+buffer[1]+buffer[2]+buffer[3]).equals("$AMJ"))
                {
                    PCB = new PCB();
                    PCB.Job_ID = Integer.parseInt(""+buffer[4]+buffer[5]+buffer[6]+buffer[7]);
                    PCB.TTL = Integer.parseInt(""+buffer[8]+buffer[9]+buffer[10]+buffer[11]);
                    PCB.TLL = Integer.parseInt(""+buffer[12]+buffer[13]+buffer[14]+buffer[15]);
                    PCB.TTC = 0;
                    PCB.TLC = 0;
                    IC = 0;   
                    EM = 0;             

                    // TRACE
                    System.out.println("Job " + PCB.Job_ID + ": PCB Initialized");

                    // MEMORY INITIALIZATION
                    M = null;
                    M = new char[300][4];
                    m = 0;
                    for(int i = 0; i < M.length; i++)
                    {
                        for(int j = 0; j < M[0].length; j++)
                        {
                            M[i][j] = ' ';
                        }
                    }

                    AllocateArray = new boolean[30];   
                    PTR = Allocate() * 10;
                    PTO = 0;
                    //System.out.println(PTR);
                    continue;
                }

                else if((""+buffer[0]+buffer[1]+buffer[2]+buffer[3]).equals("$DTA"))
                {
                    STARTEXECUTION();
                    continue;
                }

                else if((""+buffer[0]+buffer[1]+buffer[2]+buffer[3]).equals("$END"))
                {
                    continue;
                } 
            }
            
            else
            {
                int loc = Allocate();
                M[PTR + PTO][3] = (char)(loc % 10 + '0');           
                M[PTR + PTO++][2] = (char)((loc/10) % 10 + '0');
                PrintPTR();
                System.out.println("Card: " + card);

                loc *= 10;
                for (int i = 0, j = 0; i < card.length() && i < 40; i++, j++)
                {                
                    M[loc][j%4] = buffer[i];
                    if(buffer[i] == 'H')
                    {
                        j+=3;                    
                    }                
                    if ((j + 1)%4 == 0)
                    {
                        loc++;
                    }               
                }
            }
        }  
        output.close();               
    }

    public static void STARTEXECUTION() throws IOException
    {
        IC = 0;
        System.out.println("\nJob " + PCB.Job_ID + ": Execution Commenced");
        EXECUTEUSERPROGRAM();
    }

    public static void TERMINATE(int EM1, int EM2) throws IOException
    {
        System.out.println("Job " + PCB.Job_ID + " Terminated: ");
        EM = 1;
        output.write("\n\n");
        if (EM1 == 0)
        {
            System.out.println("No error");
        }
        else if (EM1 == 1)
        {
            System.out.println("Out of Data");
        }
        else if (EM1 == 2)
        {
            System.out.println("Line Limit Exceeded");
        }
        else if (EM1 == 3)
        {
            System.out.println("Time Limit Exceeded");
            if (EM2 == 4)
            {
                System.out.println("Operation Code Error");
            }
            else if (EM2 == 5)
            {
                System.out.println("Operand Error");
            }
        }
        else if (EM1 == 4)
        {
            System.out.println("Operation Code Error");
        }
        else if (EM1 == 5)
        {
            System.out.println("Operand Error");
        }
        else if (EM1 == 6)
        {
            System.out.println("Invalid Page Fault");
        }
        PrintTerminationStatus();
        PrintMemory();

        // if(EM1 != 0)
        // {
        //     String card;
        //     while((card = in.readLine())!=null)
        //     {
        //         if(card.charAt(0) == '$')
        //         {
        //             return;
        //         }
        //     }
        // }

    }

    public static void PrintTerminationStatus()
    {
        System.out.print("IC: " + IC);
        System.out.print("\tIR: " + String.valueOf(IR));
        System.out.print("\nTTC: " + PCB.TTC);
        System.out.println("\tTLC: " + PCB.TLC);
        System.out.println("\n------------------------------------------\n");
    }

    public static void MOS() throws IOException
    {
        if (TI == 0)
        {
            if (SI == 1)
            {
                READ();
            }            
            else if (SI == 2)
            {
                WRITE();
            }
            else if (SI == 3)
            {
                TERMINATE(0, -1);
            }

            else if(PI == 1)
            {
                TERMINATE(4, -1);
            }
            else if(PI == 2)
            {
                TERMINATE(5, -1);
            }
            else if(PI == 3)
            {
                String instruction = ""+IR[0]+IR[1];
                if(instruction.equals("GD") || instruction.equals("SR"))
                {
                    // System.out.println("IN MOS: " + String.valueOf(IR));
                    int loc = Allocate();
                    M[PTR + PTO][3] = (char)(loc % 10 + '0');           
                    M[PTR + PTO++][2] = (char)((loc/10) % 10 + '0');  
                    PrintPTR();  
                    IC--;
                    PI = 0;
                    // EXECUTEUSERPROGRAM(); 
                }
                else
                {
                    TERMINATE(6, -1);                                  
                }
            }
        }
        else if (TI == 2)
        {
            if (SI == 1)
            {
                TERMINATE(3, -1);
            } 
            else if (SI == 2)
            {
                WRITE();
                TERMINATE(3, -1);
            }
            else if (SI == 3)
            {
                TERMINATE(0, -1);
            }
            else if (PI == 1)
            {
                TERMINATE(3, 4);
            }  
            else if (PI == 2)
            {
                TERMINATE(3, 5);
            }   
            else if (PI == 3)
            {
                TERMINATE(3, -1);
            }       
        }
        SI = 0;
        PI = 0;
        TI = 0;
    }

    public static void ADDRESSMAP(int VA)
    {
        if (VA >= 100)
        {
            PI = 2;
            return;
        }
        int pte = PTR + (int)VA/10;
        int fNo;

        if (M[pte][3] == ' ')
        {
            PI = 3;
            return;
        }
        else
        {
            fNo = Character.getNumericValue(M[pte][2]) * 10 
            + Character.getNumericValue(M[pte][3]);                 
        }
        RA = ((fNo * 10) + (VA % 10));
        //System.out.print("IN ADDRESSMAP: ");
        //System.out.print(VA +" " + RA + "\n");
    }

    public static void EXECUTEUSERPROGRAM() throws IOException
    {
        while(true)
        {
            if(EM == 1)
            {
                return;                                
            }

            ADDRESSMAP(IC);
            if (PI != 0)
            {
                MOS();
                break;                                            
            }

            System.out.print("IN EXECUTE: ");
            IR = M[RA];
            System.out.println(String.valueOf(IR));
            IC = IC + 1;
            if (IR[0] == 'H')
            {
                PCB.TTC++;
                if (PCB.TTC > PCB.TTL)
                {
                    TI = 2;
                }
                SI = 3;
                MOS();
                // H();
                break;
            }
            if(IR[2] < '0' || IR[2] > '9' || IR[3] < '0' || IR[3] > '9')
            {
                PI = 2;   
                MOS();
                break;            
            }
            else
            {
                ADDRESSMAP(Integer.parseInt(""+IR[2]+IR[3]));
            }
            if (PI != 0)
            {
                MOS();
                continue;       // *** //                                     
            }


            if ((""+IR[0]+IR[1]).equals("LR"))
            {
                int loc = RA;
                R = M[loc];
                // LR(loc);
            }

            else if ((""+IR[0]+IR[1]).equals("SR"))
            {
                int loc = RA;
                M[loc] = R;
                PCB.TTC++;
                if(PCB.TTC > PCB.TTL)
                {
                    TI = 2;
                }
                // SR(loc);            
            }

            else if ((""+IR[0]+IR[1]).equals("CR"))
            {
                int loc = RA;
                if(Arrays.equals(M[loc], R))
                {
                    C = true;
                }
                else
                {
                    C = false;
                }                
                // CR(loc);
            }

            else if ((""+IR[0]+IR[1]).equals("BT"))
            {
                int loc = Integer.parseInt("" + IR[2] + IR[3]);
                if (C)
                {
                    IC = loc;
                    C = false;            
                }
                // BT(loc);
            }

            else if ((""+IR[0]+IR[1]).equals("GD"))
            {
                SI = 1;
                PCB.TTC++;
                if(PCB.TTC > PCB.TTL)
                {
                    TI = 2;
                }
                // GD();
            }

            else if ((""+IR[0]+IR[1]).equals("PD"))
            {
                SI = 2; 
                // PD();
            }
            else 
            {
                PI = 1;
            }

            PCB.TTC++;
            if (PCB.TTC > PCB.TTL)
            {
                TI = 2;
            }
            if (SI!=0 || PI!=0 || TI!=0)
            {
                MOS();
            }
        }
    }

    public static void PrintPTR()
    {
        System.out.println("\nPTR Updated to: ");
        for(int i = 0; i < PTO; i++)
        {
            System.out.println(String.valueOf(M[PTR + i]));
        }
        System.out.println("");        
    }

    // public static void GD() throws IOException
    // {
    //     SI = 1;
    //     PCB.TTC++;
    //     if(PCB.TTC > PCB.TTL)
    //     {
    //         TI = 2;
    //     }
    //     //MOS();
    // }

    // public static void PD() throws IOException
    // {
    //     SI = 2;  
    //     //MOS();
    // }

    // public static void H() throws IOException
    // {
    //     SI = 3;
    //     MOS();
    // }

    // public static void LR(int loc)
    // {
    //     R = M[loc];             
    // }

    // public static void SR(int loc)
    // {
    //     M[loc] = R;
    //     PCB.TTC++;
    //     if(PCB.TTC > PCB.TTL)
    //     {
    //         TI = 2;
    //     }
    // }

    // public static void CR(int loc)
    // {
    //     if(Arrays.equals(M[loc], R))
    //     {
    //         C = true;
    //         return;
    //     }
    //     C = false;
    // }

    // public static void BT(int loc)
    // {
    //     if (C)
    //     {
    //         IC = loc;
    //         C = false;            
    //     }
    // }

    public static void PrintMemory()
    {
        for(int i = 0; i < M.length; i++)
        {
            System.out.printf("%3d   ",i);
            for(int j = 0; j < M[0].length; j++)
            {
                System.out.print(M[i][j] + " ");
            }
            System.out.print("   ");
            if(i % 10 == 9)
            {
                System.out.println();
            }
        }
        System.out.println("\n------------------------------------------\n");
    }
    public static void main(String[] args) throws IOException
    {
        Initialization();
        LOAD();
        //PrintMemory();
    }
}
