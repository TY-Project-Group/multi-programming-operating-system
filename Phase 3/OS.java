import java.io.*;
import java.util.*;

public class OS
{
    public static final String INPUT_FILE = "Input.txt";    // Input File
    public static final String OUTPUT_FILE = "Output.txt";  // Output File

    public static FileReader input;
    public static FileWriter output;
    public static BufferedReader in;
    public static BufferedWriter out;

    public static PCB[] processArray;
    public static int universalTimer;

    public static ArrayDeque<Integer> RQ; 
    public static ArrayDeque<Integer> IOQ; 
    public static ArrayDeque<Integer> LQ; 
    public static ArrayDeque<Integer> TQ;

    public static ArrayDeque<Buf> ifbQ;
    public static ArrayDeque<String> ofbQ;
    public static ArrayDeque<Buf> ebQ;

    public static char[][] M;
    public static char[][] AuxMemory;
    public static boolean[] AllocateArray;
    public static char[] R;
    public static char[] IR;
    public static boolean C;
    public static int auxOffset;
    public static int RA;
    public static int m;

    public static int SI;                       
    public static int TI;                       
    public static int PI;
    public static int IOI;

    public static Channel [] Ch;

    public static char F;
    public static int currentPCB;
    public static int currentISPCB;
    public static int currentOSPCB;

    public static String task;

    public static void ClearProgramMemory(int indexPCB)
    {
        PCB tempPCB = processArray[indexPCB];
        for(int i = 0; i < tempPCB.PTO; i++)
        {
            int loc = Character.getNumericValue(M[tempPCB.PTR+i][2]) * 10 
            + Character.getNumericValue(M[tempPCB.PTR+i][3]);
            AllocateArray[loc] = false;
            m-=10;
            loc*=10;

            for(int j = 0; j < 10; j++)
            {
                for(int k = 0; k < 4; k++)
                {
                    M[loc+j][k] = ' ';
                }
            }
            M[tempPCB.PTR + i][2] = ' ';
            M[tempPCB.PTR + i][3] = ' ';
        }
        AllocateArray[tempPCB.PTR/10] = false;
        m-=10;

    }

    public static void TERMINATE(int indexPCB) throws IOException
    {
        PCB tempPCB = processArray[indexPCB];
        int EM1 = tempPCB.EM1;
        int EM2 = tempPCB.EM2;

        System.out.println("\n------------------------------------------\n");

        System.out.println("Job " + processArray[indexPCB].Job_ID + " Terminated: ");
        tempPCB.EM = 1;
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
        PrintPCB(tempPCB);
        System.out.println("\n------------------------------------------\n");
    }

    public static void InitializeMainMemory()
    {
        M = null;
        M = new char[300][4];
        for(int i = 0; i < M.length; i++)
        {
            for(int j = 0; j < M[0].length; j++)
            {
                M[i][j] = ' ';
            }
        }
    }

    public static void InitializeAuxMemory()
    {
        AuxMemory = null;
        AuxMemory = new char[500][4];
        for(int i = 0; i < AuxMemory.length; i++)
        {
            for(int j = 0; j < AuxMemory[0].length; j++)
            {
                AuxMemory[i][j] = ' ';
            }
        }
    }

    public static int Allocate()
    {
        int temp = (int)(Math.random() * 30);
        if(m <= 290)
        {
            while(AllocateArray[temp] == true)
            {
                temp = (int)(Math.random() * 30);
            } 
            AllocateArray[temp] = true;
        }
        m += 10;
        return temp;         
    }

    public static void ADDRESSMAP(int va, int ptr)
    {
        if (va >= 100)
        {
            PI = 2;
            return;
        }
        int pte = ptr + (int)va/10;
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
        RA = ((fNo * 10) + (va % 10));
    }
   
    public static void StoreToAuxiliaryMemory(Buf buffer)
    {
        if(buffer.flag == 'D')
        {
            for(int i = 0, k = 0; i < 10; i++)
            {
                for(int j = 0; j < 4; j++, k++)
                {
                    if(k >= buffer.card.length())
                    {
                        AuxMemory[auxOffset + i][j] = ' ';
                        continue;
                    }
                    AuxMemory[auxOffset + i][j] = buffer.card.charAt(k);
                }
            }      
        }
        
        else
        {

            int loc = auxOffset;
            for (int i = 0, j = 0; i < buffer.card.length(); i++, j++)
            {                
                AuxMemory[loc][j%4] = buffer.card.charAt(i);
                if(buffer.card.charAt(i) == 'H')
                {
                    j+=3;                    
                }                
                if ((j + 1)%4 == 0)
                {
                    loc++;
                }               
            }
        }

        auxOffset = auxOffset + 10;              
    }

    public static void PrintMemory()
    {
        System.out.println("\n------------------------------------------\n");
        System.out.println("MAIN MEMORY");
        System.out.println("\n------------------------------------------\n");
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

    public static void PrintAuxMemory()
    {
        System.out.println("\n------------------------------------------\n");
        System.out.println("AUXILIARY DRUM MEMORY");
        System.out.println("\n------------------------------------------\n");
        for(int i = 0; i < AuxMemory.length; i++)
        {
            System.out.printf("%3d   ",i);
            for(int j = 0; j < AuxMemory[0].length; j++)
            {
                System.out.print(AuxMemory[i][j] + " ");
            }
            System.out.print("   ");
            if(i % 10 == 9)
            {
                System.out.println();
            }
        }
        System.out.println("\n------------------------------------------\n");
    }

    public static void PrintInterrupts()
    {
        System.out.print("SI: " + SI);
        System.out.print("\tPI: " + PI);
        System.out.print("\tTI: " + TI);
        System.out.println("\tIOI: " + IOI);
    }

    public void PrintBufferQueue(ArrayDeque <Buf> Q)
    {
        System.out.println("Contents of BufferQueue:");
        for(Buf element : Q)
        {
            System.out.println(element.card + "\t" + element.flag);
        }
    }

    public void PrintPCBQueue(ArrayDeque <Integer> Q)
    {
        System.out.println("Contents of PCBQueue:");
        for(int element : Q)
        {
            System.out.println("Job " + element);
        }
    }

    public static void PrintProcessArray()
    {
        for(int i = 1; i < currentPCB + 1; i++) 
        {
            System.out.println(processArray[i].Job_ID);
        }       
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

    public static void STARTCHi(int channel)
    {
        IOI = IOI - channel;
        if(channel == 3)
        {
            IOI = IOI - 1;
        }
        Ch[channel - 1].timer = 0;
        Ch[channel - 1].flag = true;  
    }

    public static void IR1() throws IOException
    {
        //System.out.println("In IR1");
        String card = "";
        if(!ebQ.isEmpty())
        {      
            if((card = in.readLine()) != null)
            {
                //System.out.println("Card: " + card); 
                Buf temp = new Buf(card);
                ebQ.pollFirst();
                ifbQ.addLast(temp);
                STARTCHi(1);
                if(IOI < 4)
                {
                    IOI = IOI + 4;
                }            

                if(card.charAt(0) == '$')
                {
                    if(card.substring(0, 4).equals("$AMJ"))
                    {
                        char[] buffer = card.toCharArray();
                        PCB tempPCB = new PCB(buffer);
                        currentPCB = tempPCB.Job_ID;
                        processArray[currentPCB] = tempPCB;
                        F = 'P';

                        temp = ifbQ.pollLast();
                        temp.card = "";
                        ebQ.addLast(temp);                     
                    }
                
                    else if(card.substring(0, 4).equals("$DTA"))
                    {
                        F = 'D';
                        temp = ifbQ.pollLast();
                        temp.card = "";
                        ebQ.addLast(temp);
                    }
                    else if(card.substring(0, 4).equals("$END"))
                    {
                        // LQ.addLast(currentPCB);
                        // temp = ifbQ.pollLast();
                        // temp.card = "";
                        // ebQ.addLast(temp);
                    }
                }
                else
                {
                    temp.flag = F;
                    ifbQ.pollLast();
                    ifbQ.addLast(temp);                                                
                }
            }   
        }                   
    }

    public static void IR2() throws IOException
    {
        //System.out.println("In IR2");
        Buf temp = new Buf("");
        if(!ofbQ.isEmpty())
        {
            temp.card = ofbQ.pollFirst(); 
            //System.out.println(temp.card);
            output.write(temp.card);
            output.write("\n");
            temp.card = "";
            ebQ.addLast(temp);
            STARTCHi(2);
        }
    }

    public static void IR3() throws IOException
    {
        //System.out.println("In IR3");
        
        if(!IOQ.isEmpty())
        {
            if("" + IR[0] + IR[1] == "GD")
            {
                if(processArray[IOQ.peekFirst()].DCount == 0) 
                {
                    TQ.addLast(IOQ.pollFirst());
                }
                else
                {
                   task = "RD";  
                   STARTCHi(3);                   
                }

            }
            else if("" + IR[0] + IR[1] == "PD")
            {
                PCB tempPCB = processArray[IOQ.peekFirst()];
                if(tempPCB.TLC >= tempPCB.TLL)
                {
                    int indexPCB = IOQ.pollFirst();
                    processArray[indexPCB].EM1 = 2;
                    TQ.addLast(indexPCB);                    
                }
                else
                {
                    if(auxOffset <= 490)
                    {
                        task = "WT";
                        STARTCHi(3);                                                
                    }
                }
            }
        }
        else if(!TQ.isEmpty() && !ebQ.isEmpty())
        {
            task = "OS";  
            STARTCHi(3);                
        }
        else if(!ifbQ.isEmpty() && auxOffset <= 490)
        {        
            task = "IS"; 
            STARTCHi(3);
        }        
        else if(!LQ.isEmpty() && m <= 290)
        {
            task = "LD";
            STARTCHi(3);            
        }

        

        switch(task)
        {
            case "IS":
                if(!ifbQ.isEmpty()) 
                {
                    Buf temp = ifbQ.pollFirst();
                    //System.out.println("Card: " + temp.card);
                    PCB tempPCB = processArray[currentISPCB];
                    // if(auxOffset <= 490)
                    // {
                    //     STARTCHi(3);
                    // }
                    if(temp.card.charAt(0) == '$')
                    {       
                        tempPCB.OutStart = auxOffset;
                        auxOffset = auxOffset + (tempPCB.TLL * 10);                
                        LQ.addLast(currentISPCB);
                        currentISPCB++;                                           
                    }
                    else
                    {
                        if(temp.flag == 'P')
                        {
                            if(tempPCB.PFirst)
                            {
                                tempPCB.P = auxOffset;
                                tempPCB.PFirst = false;
                            }
                            tempPCB.PCount++;
                        }
                        else
                        {
                            if(tempPCB.DFirst)
                            {                        
                                tempPCB.D = auxOffset;
                                tempPCB.DFirst = false;
                            }
                            tempPCB.DCount++;
                        }
                        StoreToAuxiliaryMemory(temp);
                    }                    
                     
                    temp.card = "";
                    ebQ.addLast(temp);
                } 
                task = "";
                break;   
                
            case "OS":
                if(!ebQ.isEmpty())
                {
                    PCB tempPCB = processArray[currentOSPCB];
                    Buf temp = ebQ.pollFirst();
                    String buffer = "";
                    // PrintPCB(tempPCB);
                    if(tempPCB.outLineCount == 0)
                    {      
                        /* Remove from terminate queue*/

                        Buf x1 = ebQ.pollFirst();
                        if(!ebQ.isEmpty())
                        {
                            ebQ.pollFirst();
                            ofbQ.addLast("                                        ");
                            ofbQ.addLast("                                        ");
                            PrintMemory();
                            ClearProgramMemory(currentOSPCB);
                            PrintMemory();
                            TERMINATE(currentOSPCB);
                            currentOSPCB++;
                            TQ.pollFirst();
                        }
                        else
                        {
                            ebQ.addLast(x1);                      
                        }
                    }
                    else
                    {
                        for(int i = 0; i < 10; i++)
                        {
                            for(int j = 0; j < 4; j++)
                            {
                                buffer = buffer + AuxMemory[tempPCB.OutStart+i][j];  
                                // AuxMemory[tempPCB.OutStart+i][j] = ' ';                  
                            }
                        }
                        tempPCB.OutStart = tempPCB.OutStart + 10;
                        temp.card = buffer;
                        //System.out.println("Card: " + temp.card);
                        ofbQ.addLast(temp.card);
                        tempPCB.outLineCount--;
                    }  

                    if(IOI < 2 || IOI == 5 || IOI == 4)
                    {
                        IOI = IOI + 2;                        
                    }
                }
                task = "";
                break;

            case "LD":
                if(!LQ.isEmpty())
                {
                    int indexPCB = LQ.peekFirst();
                    PCB tempPCB = processArray[indexPCB];
                    if(tempPCB.PTR == -1)
                    {
                        tempPCB.PTR = Allocate() * 10;
                    }
                    int loc = Allocate();
                    M[tempPCB.PTR + tempPCB.PTO][3] = (char)(loc % 10 + '0'); 
                    M[tempPCB.PTR + tempPCB.PTO++][2] = (char)((loc/10) % 10 + '0');
                    loc *= 10;
                    // System.out.println("loc:" + loc);
                    // PrintPCB(tempPCB);
                    for(int i = 0; i < 10; i++)
                    {
                        for(int j = 0; j < 4; j++)
                        {
                            M[loc+i][j] = AuxMemory[tempPCB.P + i][j]; 
                        }
                    }
                    tempPCB.P = tempPCB.P + 10;                    
                    tempPCB.PCount--;
                    if(tempPCB.PCount == 0)
                    {
                        LQ.pollFirst();
                        RQ.addLast(indexPCB);
                    }               
                }
                task = "";
                break;

            case "RD":
                if(!IOQ.isEmpty())
                {
                    int indexPCB = IOQ.pollFirst();
                    PCB tempPCB = processArray[indexPCB];
                    int loc = RA;
                    for(int i = 0; i < 10; i++)
                    {
                        for(int j = 0; j < 4; j++)
                        {
                            M[RA+i][j] = AuxMemory[tempPCB.D+i][j]; 
                        }
                    } 
                    tempPCB.D = tempPCB.D + 10;                    
                    tempPCB.DCount--;
                }
                task = "";
                break;

            case "WT":
                if(!IOQ.isEmpty())
                {
                    int indexPCB = IOQ.pollFirst();
                    PCB tempPCB = processArray[indexPCB];
                    int loc = RA;
                    tempPCB.TLC++;
                    // if(tempPCB.TLC > tempPCB.TLL)
                    // {
                    //     TQ.addLast(indexPCB);
                    // }
                    
                    for(int i = 0; i < 10; i++)
                    {
                        for(int j = 0; j < 4; j++)
                        {
                            AuxMemory[tempPCB.OutStart + tempPCB.OutOffset + i][j] = M[RA+i][j];
                            if(tempPCB.OFirst)
                            {
                                tempPCB.OFirst = false;
                            }
                        }
                    }    
                    tempPCB.OutOffset += 10;                    
                    
                }
                task = "";
                break; 

            default:
                break;         
        }       
    }

    public static void MOS() throws IOException
    {        
        //PrintInterrupts();
        if (TI == 2)
        {
            if (SI == 1)
            {
                int indexPCB = RQ.pollFirst();
                processArray[indexPCB].EM1 = 3;
                TQ.addLast(indexPCB);
                SI = 0;
                // TERMINATE(3, -1);
            } 
            else if (SI == 2)
            {
                int indexPCB = RQ.pollFirst();
                IOQ.addLast(indexPCB);
                processArray[indexPCB].EM1 = 3;
                TQ.addLast(indexPCB);
                SI = 0;

                // WRITE();
                // TERMINATE(3, -1);
            }
            else if (SI == 3)
            {
                int indexPCB = RQ.pollFirst();
                processArray[indexPCB].EM1 = 0;
                TQ.addLast(indexPCB);
                SI = 0;
                // TERMINATE(0, -1);
            }
            else if (PI == 1)
            {
                int indexPCB = RQ.pollFirst();
                processArray[indexPCB].EM1 = 3;
                processArray[indexPCB].EM2 = 4;
                TQ.addLast(indexPCB);
                PI = 0;
                // TERMINATE(3, 4);
            }  
            else if (PI == 2)
            {
                int indexPCB = RQ.pollFirst();
                processArray[indexPCB].EM1 = 3;
                processArray[indexPCB].EM2 = 5;
                TQ.addLast(indexPCB);
                PI = 0;
                // TERMINATE(3, 5);
            }   
            else if (PI == 3)
            {
                int indexPCB = RQ.pollFirst();
                processArray[indexPCB].EM1 = 3;
                TQ.addLast(indexPCB);
                PI = 0;
                // TERMINATE(3, -1);
            } 
            TI = 0;      
        }
        else if (TI == 0 || TI == 1)
        {
            if(PI == 1)
            {
                int indexPCB = RQ.pollFirst();
                processArray[indexPCB].EM1 = 4;
                TQ.addLast(indexPCB);
                PI = 0;
                // TERMINATE(4, -1);
            }
            else if(PI == 2)
            {
                int indexPCB = RQ.pollFirst();
                processArray[indexPCB].EM1 = 5;
                TQ.addLast(indexPCB);
                PI = 0;
                // TERMINATE(5, -1);
            }
            else if(PI == 3)
            {
                String instruction = ""+IR[0]+IR[1];
                if(instruction.equals("PD") || instruction.equals("LR") || instruction.equals("CR"))
                {
                    int indexPCB = RQ.pollFirst();
                    processArray[indexPCB].EM1 = 6;
                    TQ.addLast(indexPCB);
                    PI = 0;
                    // TERMINATE(6, -1);
                }
                else
                {
                    // System.out.println("IN MOS: " + String.valueOf(IR));
                    int indexPCB = RQ.peekFirst();
                    PCB tempPCB = processArray[indexPCB];
                    int loc = Allocate();
                    M[tempPCB.PTR + tempPCB.PTO][3] = (char)(loc % 10 + '0');           
                    M[tempPCB.PTR + tempPCB.PTO++][2] = (char)((loc/10) % 10 + '0');  
                    tempPCB.IC--;
                    PI = 0;
                    // EXECUTEUSERPROGRAM();               
                }
            }
            else if (SI == 1)
            {
                int indexPCB = RQ.peekFirst();
                IOQ.addLast(indexPCB);
                task = "RD";
                SI = 0;

                // READ();
            }            
            else if (SI == 2)
            {
                int indexPCB = RQ.peekFirst();
                IOQ.addLast(indexPCB);
                task = "WT";
                SI = 0;

                // WRITE();
            }
            else if (SI == 3)
            {
                int indexPCB = RQ.pollFirst();
                processArray[indexPCB].EM1 = 0;
                TQ.addLast(indexPCB);
                SI = 0;
                // TERMINATE(0, -1);
            }
        }

        switch(IOI)
        {
            case 0: 
                break;
            
            case 1:
                IR1();
                break;

            case 2:
                IR2();
                break;
            
            case 3:
                IR2();
                IR1();
                break;

            case 4:
                IR3();
                break;
            
            case 5:
                IR1();
                IR3();                
                break;

            case 6:
                IR3();
                IR2();
                break;

            case 7:
                IR2();
                IR1();
                IR3();
                break;

        }
    }

    public static void EXECUTEUSERPROGRAM() throws IOException
    {
        //PrintMemory();
        if(!RQ.isEmpty())
        {
            int indexPCB = RQ.peekFirst();
            PCB tempPCB = processArray[indexPCB];
            if(tempPCB.EM == 1)
            {
                /// kuch to karna padega idhar
                return;                                
            }

            ADDRESSMAP(tempPCB.IC, tempPCB.PTR);
            if (PI != 0)
            {
                // MOS();
                //break;                                            
            }

            IR = M[RA];
            // System.out.println(String.valueOf(IR));
            tempPCB.IC = tempPCB.IC + 1;
            if (IR[0] == 'H')
            {                
                SI = 3;
                // H();
                //break;
            }

            if(!(SI == 3 || (!(PI == 0))))
            { 
                if(IR[2] < '0' || IR[2] > '9' || IR[3] < '0' || IR[3] > '9')
                {
                    PI = 2;   
                    // MOS();
                    // break;            
                }
                else
                {
                    ADDRESSMAP(Integer.parseInt(""+IR[2]+IR[3]), tempPCB.PTR);
                }
                if (PI == 0)
                {
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
                            tempPCB.IC = loc;
                            C = false;            
                        }
                        // BT(loc);
                    }

                    else if ((""+IR[0]+IR[1]).equals("GD"))
                    {
                        SI = 1;                    
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
                    //MOS();
                    //continue;  
                }
            }

            tempPCB.TTC++;
            if (tempPCB.TTC > tempPCB.TTL)
            {
                TI = 2;
            }
        }
    }

    public static void Initialization() throws IOException
    {
        input = new FileReader(INPUT_FILE);
        output = new FileWriter(OUTPUT_FILE);
        in = new BufferedReader(input);
        out = new BufferedWriter(output);

        processArray = new PCB[10];
        universalTimer = 0;

        LQ =    new ArrayDeque<Integer>();
        RQ =    new ArrayDeque<Integer>();
        TQ =    new ArrayDeque<Integer>();
        IOQ =   new ArrayDeque<Integer>();

        ifbQ =  new ArrayDeque<Buf>();
        ofbQ =  new ArrayDeque<String>();
        ebQ =   new ArrayDeque<Buf>();
        for(int i = 0; i < 10; i++)
        {
            ebQ.addLast(new Buf(""));
        }

        InitializeMainMemory();
        m=0;
        InitializeAuxMemory();
        AllocateArray = new boolean[30];
        R = new char[4];
        IR = new char[4];
        C = false; 
        auxOffset = 0;

        SI = 0;
        TI = 0;
        PI = 0;
        IOI = 1;

        Ch = new Channel[3];
        Ch[0] = new Channel(5);
        Ch[1] = new Channel(5);
        Ch[2] = new Channel(2);
        
        currentISPCB = 1;
        currentOSPCB = 1;

        task = "";

    }

    public static void SIMULATION()
    {
        System.out.println("Universal Timer: " + universalTimer);
        // PrintInterrupts();
        universalTimer++;
        for(int chi = 1; chi < 4; chi++)
        {
            if(Ch[chi - 1].flag)
            {
                Ch[chi - 1].timer++;
                if(Ch[chi - 1].timer == Ch[chi - 1].TotalTime)
                {
                    if(chi == 1)
                    {
                        if(IOI == 0 || IOI == 2 || IOI == 4 || IOI == 6)
                        {
                            IOI = IOI + chi;
                        }
                    }
                    if(chi == 2)
                    {
                        if(IOI < 2 || IOI == 4 || IOI == 5)
                        {
                            IOI = IOI + chi;
                        }
                    }
                    if(chi == 3)
                    {
                        if(IOI < 4)
                        {
                            IOI = IOI + chi + 1;
                        }
                    }                    
                    Ch[chi - 1].flag = false;
                }
            }
        }
    }
    public static void main(String[] args) throws IOException
    {
        Initialization();  
        while(universalTimer < 500)
        {
            if(!RQ.isEmpty() && IOQ.isEmpty())
            {
                // System.out.println("In EXECUTE");
                EXECUTEUSERPROGRAM();
            }
            SIMULATION();
            if (SI!=0 || PI!=0 || TI!=0 || IOI!=0)
            {
                // System.out.println("In MOS");
                MOS();
            }            
        }
        PrintMemory();
        PrintAuxMemory();

        output.close();        
    }
}