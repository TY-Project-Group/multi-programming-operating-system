import java.io.*;
import java.util.*;

public class OSPhase1
{
    public static final String INPUT_FILE = "Input.txt";
    public static final String OUTPUT_FILE = "Output.txt";

    public static char[][] M;
    public static char[] R;
    public static char[] IR;
    public static int IC;
    public static boolean C;
    public static int SI;

    public static int m;
    public static char[] buffer;
    public static FileReader input;
    public static FileWriter output;
    public static BufferedReader in;
    public static BufferedWriter out;

    public static void Initialization() throws IOException
    {
        M = new char[100][4];
        R = new char[4];
        IR = new char[4];
        IC = 0;
        C = false;   
        SI = 0;
        
        buffer = new char[40]; 
        input = new FileReader(INPUT_FILE);
        output = new FileWriter(OUTPUT_FILE);
        in = new BufferedReader(input);
        out = new BufferedWriter(output);
    }

    public static void READ() throws IOException
    {
        //IR[3] = '0';
        int loc = Integer.parseInt(""+IR[2]+IR[3]);
        String card = in.readLine();
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
        //IR[3] = '0';
        int loc = Integer.parseInt(""+IR[2]+IR[3]);
        String data = "";
        for (int i = 0; i < 10; i++)
        {
            data = data + new String(M[loc + i]);   
        }
        output.write(data);
        output.write("\n");       
    }

    public static void TERMINATE() throws IOException
    {
        output.write("\n\n");     
    }

    public static void LOAD() throws IOException
    {        
        String card;

        while((card = in.readLine())!=null) 
        {
            buffer = card.toCharArray();

            // Control Cards
            if ((""+buffer[0]+buffer[1]+buffer[2]+buffer[3]).equals("$AMJ"))
            {
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

            // Program Card
            M = null;
            M = new char[100][4];
            m = 0;
            for(int i = 0; i < M.length; i++)
            {
                for(int j = 0; j < M[0].length; j++)
                {
                    M[i][j] = ' ';
                }
            }

            for (int i = 0, j = 0; i < card.length(); i++, j++)
            {
                if (m == 100)
                {
                    System.out.println("Memory Exhausted!");
                    break;
                }                 
                M[m][j%4] = buffer[i];
                if(buffer[i] == 'H')
                {
                    j+=3;                    
                }                
                if ((j + 1)%4 == 0)
                {
                    m++;
                }               
            }
        }  
        output.close();        
    }

    public static void EXECUTEUSERPROGRAM() throws IOException
    {
        while(true)
        {
            IR = M[IC];
            IC = IC + 1;

            if ((""+IR[0]+IR[1]).equals("LR"))
            {
                int loc = Integer.parseInt("" + IR[2] + IR[3]);
                LR(loc);
            }

            else if ((""+IR[0]+IR[1]).equals("SR"))
            {
                int loc = Integer.parseInt("" + IR[2] + IR[3]);
                SR(loc);            
            }

            else if ((""+IR[0]+IR[1]).equals("CR"))
            {
                int loc = Integer.parseInt("" + IR[2] + IR[3]);
                CR(loc);
            }

            else if ((""+IR[0]+IR[1]).equals("BT"))
            {
                int loc = Integer.parseInt("" + IR[2] + IR[3]);
                BT(loc);
            }

            else if ((""+IR[0]+IR[1]).equals("GD"))
            {
                GD();
            }

            else if ((""+IR[0]+IR[1]).equals("PD"))
            {
                PD();
            }

            else if (IR[0] == 'H')
            {
                H();
                break;
            }
        }
    }
    
    public static void MOS() throws IOException
    {
        if (SI == 1)
        {
            READ();
        }        
        else if(SI == 2)
        {
            WRITE();
        }
        else if(SI == 3)
        {
            TERMINATE();
        }
        SI = 0;
    }

    public static void STARTEXECUTION() throws IOException
    {
        IC = 0;
        EXECUTEUSERPROGRAM();
    }

    public static void GD() throws IOException
    {
        SI = 1;
        MOS();
    }

    public static void PD() throws IOException
    {
        SI = 2;
        MOS();
    }

    public static void H() throws IOException
    {
        SI = 3;
        MOS();
    }

    public static void LR(int loc)
    {
        R = M[loc];             
    }

    public static void SR(int loc)
    {
        M[loc] = R;
    }

    public static void CR(int loc)
    {
        if(Arrays.equals(M[loc], R))
        {
            C = true;
            return;
        }
        C = false;
    }

    public static void BT(int loc)
    {
        if (C)
        {
            IC = loc;
            C = false;            
        }
    }

    public static void PrintMemory()
    {
        for(int i = 0; i < M.length; i++)
        {
            System.out.printf("%2d   ",i);
            for(int j = 0; j < M[0].length; j++)
            {
                System.out.print(M[i][j] + " ");
            }
            System.out.println();
        }
    }
    public static void main(String[] args) throws IOException
    {
        Initialization();
        LOAD();
        PrintMemory();
    }
}