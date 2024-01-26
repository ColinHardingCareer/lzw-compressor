/*************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt   (compress)
 *  Execution:    java LZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STAARTING WIITH ORACCLE JAVA 6, UPDATE 7 the SUBBSTRRING
 *  METHOD TAKESS TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 *************************************************************************/

public class MyLZW {
    private static int R = 256;        // number of input chars
    private static int L = 512;       // number of codewords = 2^W
    private static int W = 9;         // codeword width
	private static final double threshold = 1.1; //defined by assignment

    public static void compress(String a) { 
		
		char mode = a.charAt(0);
		boolean monitor= false;
		double ratioOld = 1;	
        double ratioNew = 0;	
		long uncompressed = 0;	
        long compressed = 0;
		
		
		if("nrm".indexOf(mode) < 0){
        	 	System.out.println("\nInvalid Mode; Try N, R, M : do nothing, Reset, Monitor respectively");
				return;
		}
		
	
		BinaryStdOut.write(mode);
		String input = BinaryStdIn.readString();
		TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        int code = R+1;  // R is codeword for EOF
		
		
        while (input.length() > 0) {
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
            int t = s.length();
			
            
            //compression ratio current/new calculation
			uncompressed+=t*16;
			compressed += W;	
			ratioNew=((double)uncompressed/(double)compressed);
			//compression ratio current/new calculation end
			
			
			if ( t < input.length() && code >= L && W < 16 )//if codebook is full and not max width ?  increase codeword width : continue 
				L = (int)Math.pow(2,W++);	
			
			if ( code >= L && W == 16 ) { // if complete max is reached 
			
			
			switch(mode){//mode decider
			
				case 'r':
					st = new TST<Integer>();
		        	for (int i = 0; i < R; i++)
		            	st.put("" + (char) i, i);
					
					/*reset globals
						|||
						vvv
					*/
		         	W = 9;								
		         	L = 512;							
		         	code = R+1;
			
					break;//end r mode
					
				case 'm':
					
					
					
					if(monitor){//is it needed to monitor what the ratio is ? reset if needed : make a new old ratio
					
						if ( ( ratioOld / ratioNew ) >= threshold ) {//reset
							st = new TST<Integer>();		
							for (int i = 0; i < R; i++)
								st.put("" + (char) i, i);
							
							W = 9;							
							L = 512;						
							code = R+1;						
							monitor = false;								
						}
					
					
					}else{
						ratioOld=ratioNew;
						monitor=true;
					}
				
				
					break;//end m mode
			}//end switch
			
			}// end if complete max is reached
			
			
			
            if (t < input.length() && code < L)    // Add s to symbol table.
                st.put(input.substring(0, t + 1), code++);
            input = input.substring(t);            // Scan past s in input.
            
        }//end while
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } //end compress


    public static void expand() {
		
		char mode = BinaryStdIn.readChar();	
		boolean monitor= false;
		double ratioOld = 1;	
        double ratioNew = 0;	
		long uncompressed = 0;	
        long compressed = 0;	//being read in
		int i; // next available codeword value
        String[] st = new String[(int) Math.pow(2,16)];//just give it enough space to begin with 
       
		

        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";                        // (unused) lookahead for EOF
		
		

        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;           // expanded message is empty string
        String val = st[codeword];
		//re arrange order
		compressed +=W;
		uncompressed += (val.length()*16);
		
		

        while (true) {
			ratioNew = (double)uncompressed/(double)compressed;
			
			
			if (i >= L && W < 16 ) //if codebook is full but not max width ? increase width and amount of codewords : continue
				L = (int)Math.pow(2,W++);//reverse ++
			
			
			if ( i >= L && W == 16 ) {  //if max everything 
			
				switch(mode){
					
				case 'r':
					st = new String[(int) Math.pow(2,16)];
		        	for (int j = 0; i < R; j++)
		            	st[j]=("" + (char) j);
					
					/*reset everyhting
						|||
						vvv
					*/
		         	W = 9;								
		         	L = 512;							
		         	i = R+1;
			
					break;//end r mode
					
				case 'm':
					if(monitor){ //is it needed to monitor what the ratio is ? reset if needed : make a new old ratio
					
						if ( ( ratioOld / ratioNew ) >= threshold ) {//if needs reset
							st = new String[(int) Math.pow(2,16)];
				        	for (int j = 0; j < R; j++)
				            	st[j]=("" + (char) j);
							
							/*reset everyhting
								|||
								vvv
							*/
				         	W = 9;								
				         	L = 512;							
				         	i = R+1;			
							monitor = false;								
						}
					
					
					}else{
						ratioOld=ratioNew;
						monitor=true;
					}
				
				
					break;//end m mode
			}//end switch
			
			
			
			
			}//end if max everything
			
			
			
            BinaryStdOut.write(val);
            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) break;
            String s = st[codeword];
            if (i == codeword) s = val + val.charAt(0);   // special case hack
            if (i < L) st[i++] = val + s.charAt(0);
            val = s;
			compressed +=W;
			uncompressed += (val.length()*16);
        }
        BinaryStdOut.close();
    }



    public static void main(String[] args) {
		/*
		pass args1 into compress
		*/
        if      (args[0].equals("-")) compress(args[1]);
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }

}
