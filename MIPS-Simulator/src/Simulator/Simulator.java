package Simulator;

import java.io.FileInputStream;  
import java.io.IOException;
import java.lang.String;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.util.Scanner;

public class Simulator{
	
	static String[] Memory = new String[50];

//------------------------------PR------------------------		
	public static String[] PR(String input) throws FileNotFoundException, EOFException, IOException{	  
		//System.out.print("Please input file path:");    //input file path
		//Scanner in = new Scanner(System.in);		
		//String pathname = "/Users/Terence/Desktop/fibonacci_bin.bin";
		String pathname = input;
		//String pathname1 = in.next();
		DataInputStream datastream = null;
		FileInputStream file = null;
		
		String a = "";
		String line ="";
		
		file = new FileInputStream(pathname);
		datastream = new DataInputStream(file);			
		byte[] buf = new byte[4];	
		
		for (int i = 0; i < 50; i++){
			Memory[i] = "0"; 
		}
		
		int count = 0;		//counter of instructions
		while ((datastream.read(buf, 0, buf.length) != -1) && (count < 29)){
			line = "";
			//total++;
			for (byte i : buf){				
				a = byte2bits(i);
				line = line + a;
			}
			Memory[count] = line;
			count = count + 1;
		}
		datastream.close();	
		return Memory;
	}//IS
	

	public static String byte2bits(byte b){		//byte to bit
		int z = b;
		z |= 256;
		String str = Integer.toBinaryString(z);
		int len = str.length();
		return str.substring(len-8, len);
	}//byte-bit
	
	
	
//------------------------------IF------------------------	
	static int[][] BTB = new int[10][3];		//BTB table(0-15)
	static boolean hit = false; 
	private static int IF (String instruction, int PC){
		//System.out.println("IQ:" + instruction);
		boolean flag = false;
		boolean flag1 = false;
		//检查是否为branch和jump		
		String opcode = instruction.substring(0,6);		
		String[] JB = {"000010","000100","000101","000001","000111","000110"};	//J type opcode
			int address = 0;
			for (String jb : JB){
				if (opcode.equals(jb)){
					flag = true;
					break;
				}
			}
			if (flag == true){
				
				int i = 0;
				for (i = 0; (i < 10) && (BTB[i][0] != 0); i++){
					if (PC == BTB[i][0]){
						address = BTB[i][1];		//hit
						flag1 = true;
						break;
					}
				}
				if (flag1){
					PC = address;
				}
				else{
					BTB[i][0] = PC;
					BTB[i][1] = PC+4;
					BTB[i][2] = 0;
					PC = PC + 4;
				}
			}
			else{
				PC = PC + 4;
			}
			
		return PC;
	}

	
	
//------------------------------ID------------------------
	static String[] I = {"101011","100011","000100","000101","000111","000110","001000","001001","001010","000001"};	//I type opcode
	static String[] J = {"000010"};	//J type opcode				
	static String[] R = {"000000"};	//R type opcode	
	
	static ReservationStation[] RS = new ReservationStation[10];		//RS(0-9)
	static ReoderBuffer[] ROB = new ReoderBuffer[10];		//Reorder Buffer(0-9)
	static RegisterStatus[] RegisterStat = new RegisterStatus[32];		//(0-32)

	static int a = 0;	
	static int b = 0;	
	static int ROBid = 1;
	
	private static Object[] ID (String instruction, int address, int clock){	
		Object type = new Object();
		Object[] rtype = new Object[10];
			
		int a = 0;
		int b = 0;

		for (int i = 0; i < 10; i++){
			if ((RS[i].Busy=="yes") || (RS[i].Busy=="no")){
				a = a+1;	
			}	
		}
		for (int i = 0; i < 10; i++){
			if ((ROB[i].Busy=="yes") || (ROB[i].Busy=="no")){
				b = b+1;
			}	
		}
		
		if ((a<10) && (b<10)){	
			String opcode = instruction.substring(0,6);  
		    String Category = "";
		    String function = "";
		    if ((opcode.equals("000000")==true)&&(instruction.substring(26,32).equals("001101")==true)){
            	Category = "BREAK"; 	//break
            	
            	BREAK br  = new BREAK();
            	br.Divide(instruction);
            	RS[a].Name = br.Name;	
            	RS[a].Busy = "yes";
        		RS[a].type = br;
        		RS[a].Exe = false;
        		RS[a].Instruction = instruction;
        		System.out.println(RS[a].Instruction);
            	RS[a].Qj = 0;
            	RS[a].Qk = 0;
        		RS[a].pInstruction = br.returnbuffer();
        		
            	ROB[b].ID = ROBid;
        		ROB[b].Busy = "yes";
        		ROB[b].Ready = false;
        		ROB[b].Instruction = instruction;
        		ROB[b].type = "BREAK";
        		ROB[b].pInstruction = br.returnbuffer();

            	
            }
            else if(instruction.equals("00000000000000000000000000000000")==true){		//identify "NOP"
            	Category = "NOP";
            	NOP nop = new NOP();
            	nop.Divide(instruction);
            	RS[a].Qj = 0;
            	RS[a].Qk = 0;
            	RS[a].Busy = "yes";
        		RS[a].type = nop;
        		RS[a].Exe = false;
        		RS[a].Instruction = instruction;
        		//RS[a].pInstruction = nop.returnbuffer();
            	
            	ROB[b].ID = ROBid;
        		ROB[b].Busy = "yes";
        		ROB[b].Ready = false;
        		ROB[b].Instruction = instruction;
        		ROB[b].type = "NOP";
        		//ROB[b].pInstruction = nop.returnbuffer();
        		
            }
            else
            {
            	 for(String i :I){
            		 if(opcode.equals(i) ==true){
                   		Category = "I";
                   		break;
                   	}
                   }
                   for(String j :J){
                   	if(opcode.equals(j) ==true){
                   		Category = "J";
                   		break;
                   	}
                   }
                   for(String r :R){
                   	if(opcode.equals(r) ==true){
                   		Category = "R";
                   		function = instruction.substring(26,32); 
                   		break;
                   	}
                   } 
              }
		    
		      if (Category.equals("I")==true){	    	  
		    		SW sw = new SW();
                	LW lw = new LW();
                	BEQ beq = new BEQ();
                	ADDI addi = new ADDI();
                	BNE bne = new BNE();
                	BGTZ bgtz = new BGTZ();
                	BLEZ blez = new BLEZ();
                	ADDIU addiu = new ADDIU();
                	SLTI slti = new SLTI();
                	BGEZ bgez = new BGEZ();
                	BLTZ bltz = new BLTZ();

                	
                	if (opcode.equals(sw.Op)==true){
                		sw.Divide(instruction);	
                
                		RS[a].Op = sw.Op;
                		RS[a].Name = sw.Name;		
                		if (RegisterStat[Integer.valueOf(sw.rs,2)].Busy=="yes"){    //base
                			int h = 0;	
                			for (int q = 0; q < 10; q++){
                				if (ROB[q].ID == RegisterStat[Integer.valueOf(sw.rs,2)].Reorder){
                					h = q;
                				}
                			}
                			
                			if (ROB[h].Ready){
                				RS[a].Vj = ROB[h].Value;
                				RS[a].Qj = 0;
                			}
                			else {
                				RS[a].Qj = RegisterStat[Integer.valueOf(sw.rs,2)].Reorder;
                			}
                		}
                		else {
                			RS[a].Vj = RegisterStat[Integer.valueOf(sw.rs,2)].Values;
                			RS[a].Qj = 0;
                		}
                		
                		if (RegisterStat[Integer.valueOf(sw.rt,2)].Busy=="yes"){
                			int h = 0;		
                			for (int q = 0; q < 10; q++){
                				if (ROB[q].ID == RegisterStat[Integer.valueOf(sw.rt,2)].Reorder){
                					h = q;
                				}
                			}
                			
                			if (ROB[h].Ready){
                				RS[a].Vk = ROB[h].Value;
                				RS[a].Qk = 0;
                			}
                			else {
                				RS[a].Qk = RegisterStat[Integer.valueOf(sw.rt,2)].Reorder;
                			}
                		}
                		else {
                			RS[a].Vk = RegisterStat[Integer.valueOf(sw.rt,2)].Values;
                			RS[a].Qk = 0;
                		}
                		
                		RS[a].Busy = "yes";
                		RS[a].type = sw;
                		RS[a].Exe = false;
                		RS[a].Instruction = instruction;
                		RS[a].PC = address-4;
                		RS[a].pInstruction = sw.returnbuffer();
                   		
                		ROB[b].ID = ROBid;
                		ROB[b].Busy = "yes";
                		ROB[b].Ready = false;
                		ROB[b].Instruction = instruction;
                		ROB[b].type = RS[a].Name;
                		ROB[b].pInstruction = sw.returnbuffer();
                		sw.RSnumber = a;
                		
                		
                	}
                	else if (opcode.equals(lw.Op)==true) {
                		
                		lw.Divide(instruction);
                		RS[a].Op = lw.Op;
                		RS[a].Name = lw.Name;
                		RS[a].Qk = 0;		
                		
                		if (RegisterStat[Integer.valueOf(lw.rs,2)].Busy=="yes"){
                			int h = 0;	
                			for (int q = 0; q < 10; q++){
                				if (ROB[q].ID == RegisterStat[Integer.valueOf(lw.rs,2)].Reorder){		//base
                					h = q;
                				}
                			}
                			if (ROB[h].Ready){
                				RS[a].Vj = ROB[h].Value;
                				RS[a].Qj = 0;
                			}
                			else {
                				RS[a].Qj = RegisterStat[Integer.valueOf(lw.rs,2)].Reorder;
                			}
                		}
                		else {
                			RS[a].Vj = RegisterStat[Integer.valueOf(lw.rs,2)].Values;
                			RS[a].Qj = 0;
                		}
                		
                		RS[a].Busy = "yes";
                		RS[a].Destination = ROBid;
                		RS[a].type = lw;
                		RS[a].Exe = false;
                		RS[a].Instruction = instruction;
                		RS[a].PC = address-4;
                		RS[a].pInstruction = lw.returnbuffer();                        	
                		ROB[b].ID = ROBid;
                		ROB[b].Busy = "yes";
                		ROB[b].Ready = false;
                		ROB[b].Instruction = instruction;
                		ROB[b].type = RS[a].Name;
                		ROB[b].Source = a;
                		ROB[b].Destination = Integer.valueOf(lw.rd,2);
                		ROB[b].pInstruction = lw.returnbuffer();
                		lw.RSnumber = a;	
                		
                		RegisterStat[Integer.valueOf(lw.rd,2)].Reorder = ROBid;
                		RegisterStat[Integer.valueOf(lw.rd,2)].Busy = "yes";
    				}
                	
                	else if (opcode.equals(beq.Op)==true) {
                		beq.Divide(instruction);
                		//beq.PrintInstruction();	    		
                		RS[a].Op = beq.Op;
                		RS[a].Name = beq.Name;
                		if (RegisterStat[Integer.valueOf(beq.rt,2)].Busy=="yes"){
                			int h = 0;
                			for (int q = 0; q < 10; q++){
                				if (ROB[q].ID == RegisterStat[Integer.valueOf(beq.rt,2)].Reorder){
                					h = q;
                				}
                			}
 	
                			if (ROB[h].Ready){
                				RS[a].Vj = ROB[h].Value;
                				RS[a].Qj = 0;
                			}
                			else {
                				RS[a].Qj = RegisterStat[Integer.valueOf(beq.rt,2)].Reorder;
                			}
                		}
                		else {
                			RS[a].Vj = RegisterStat[Integer.valueOf(beq.rt,2)].Values;
                			RS[a].Qj = 0;
                		}
                		if (RegisterStat[Integer.valueOf(beq.rs,2)].Busy=="yes"){
                			int h = 0;
                			for (int q = 0; q < 10; q++){
                				if (ROB[q].ID == RegisterStat[Integer.valueOf(beq.rs,2)].Reorder){
                					h = q;
                				}
                			}
 	
                			if (ROB[h].Ready){
                				RS[a].Vk = ROB[h].Value;
                				RS[a].Qk = 0;
                			}
                			else {
                				RS[a].Qk = RegisterStat[Integer.valueOf(beq.rs,2)].Reorder;
                			}
                		}
                		else {
                			RS[a].Vk = RegisterStat[Integer.valueOf(beq.rs,2)].Values;
                			RS[a].Qk = 0;
                		}
                		
                		RS[a].Busy = "yes";
                		RS[a].Destination = ROBid;
                		RS[a].type = beq;
                		RS[a].Exe = false;
                		RS[a].PC = address-4;
                		RS[a].Instruction = instruction;
                		RS[a].pInstruction = beq.returnbuffer();
                		ROB[b].ID = ROBid;
                		ROB[b].type = RS[a].Name;
                		ROB[b].Busy = "yes";
                		ROB[b].Ready = false;
                		ROB[b].Instruction = instruction;
                		ROB[b].Address = address;		
                		ROB[b].pInstruction = beq.returnbuffer();
                		beq.RSnumber = a;	
    				}
                	else if (opcode.equals(addi.Op)==true) {
                		addi.Divide(instruction);
                		RS[a].Op = addi.Op;
                		RS[a].Name = addi.Name;
                		RS[a].Qk = 0;		
                		RS[a].Vk = addi.num;	
                		if (RegisterStat[Integer.valueOf(addi.rs,2)].Busy=="yes"){
                			int h = 0;		
                			for (int q = 0; q < 10; q++){
                				if (ROB[q].ID == RegisterStat[Integer.valueOf(addi.rs,2)].Reorder){
                					h = q;
                				}
                			}
                			if (ROB[h].Ready){
                				RS[a].Vj = ROB[h].Value;
                				RS[a].Qj = 0;
                			}
                			else {
                				RS[a].Qj = RegisterStat[Integer.valueOf(addi.rs,2)].Reorder;
                			}
                		}
                		else {
                			RS[a].Vj = RegisterStat[Integer.valueOf(addi.rs,2)].Values;
                			RS[a].Qj = 0;
                		}
                			
                		RS[a].Busy = "yes";
                		RS[a].Destination = ROBid;
                		RS[a].type = addi;
                		RS[a].Exe = false;
                		RS[a].Instruction = instruction;
                		RS[a].PC = address-4;
                		RS[a].pInstruction = addi.returnbuffer();
                		
                		ROB[b].ID = ROBid;
                		ROB[b].type = RS[a].Name;
                		ROB[b].Busy = "yes";
                		ROB[b].Ready = false;
                		ROB[b].Instruction = instruction;
                		ROB[b].Destination = Integer.valueOf(addi.rd,2);
                		ROB[b].pInstruction = addi.returnbuffer();
                		
                		RegisterStat[Integer.valueOf(addi.rd,2)].Reorder = ROBid;
                		RegisterStat[Integer.valueOf(addi.rd,2)].Busy = "yes";
                	
                		addi.RSnumber = a;		
    				}
                	else if (opcode.equals(bne.Op)==true) {
                		bne.Divide(instruction);    		
                		RS[a].Op = bne.Op;
                		RS[a].Name = bne.Name;
                		if (RegisterStat[Integer.valueOf(bne.rt,2)].Busy=="yes"){
                			int h = 0;		
                			for (int q = 0; q < 10; q++){
                				if (ROB[q].ID == RegisterStat[Integer.valueOf(bne.rt,2)].Reorder){
                					h = q;
                				}
                			}
 	
                			if (ROB[h].Ready){
                				RS[a].Vj = ROB[h].Value;
                				RS[a].Qj = 0;
                			}
                			else {
                				RS[a].Qj = RegisterStat[Integer.valueOf(bne.rt,2)].Reorder;
                			}
                		}
                		else {
                			RS[a].Vj = RegisterStat[Integer.valueOf(bne.rt,2)].Values;
                			RS[a].Qj = 0;
                		}
                		if (RegisterStat[Integer.valueOf(bne.rs,2)].Busy=="yes"){
                			int h = 0;		
                			for (int q = 0; q < 10; q++){
                				if (ROB[q].ID == RegisterStat[Integer.valueOf(bne.rs,2)].Reorder){
                					h = q;
                				}
                			}
 	
                			if (ROB[h].Ready){
                				RS[a].Vk = ROB[h].Value;
                				RS[a].Qk = 0;
                			}
                			else {
                				RS[a].Qk = RegisterStat[Integer.valueOf(bne.rs,2)].Reorder;
                			}
                		}
                		else {
                			RS[a].Vk = RegisterStat[Integer.valueOf(bne.rs,2)].Values;
                			RS[a].Qk = 0;
                		}
                		
                		RS[a].Busy = "yes";
                		RS[a].Destination = ROBid;
                		RS[a].type = bne;	
                		RS[a].Exe = false;
                		RS[a].PC = address-4;
                		RS[a].Instruction = instruction;
                		RS[a].pInstruction = bne.returnbuffer();
                		
                		ROB[b].ID = ROBid;
                		ROB[b].type = RS[a].Name;
                		ROB[b].Busy = "yes";
                		ROB[b].Ready = false;
                		ROB[b].Instruction = instruction;
                		ROB[b].Address = address;			
                		ROB[b].pInstruction = bne.returnbuffer();
                		bne.RSnumber = a;	
    				}

                	else if (opcode.equals(bgtz.Op)==true) {
                		bgtz.Divide(instruction);	    		
                		RS[a].Op = bgtz.Op;
                		RS[a].Name = bgtz.Name;
                		if (RegisterStat[Integer.valueOf(bgtz.rs,2)].Busy=="yes"){
                			int h = 0;		
                			for (int q = 0; q < 10; q++){
                				if (ROB[q].ID == RegisterStat[Integer.valueOf(bgtz.rs,2)].Reorder){
                					h = q;
                				}
                			}
 	
                			if (ROB[h].Ready){
                				RS[a].Vk = ROB[h].Value;
                				RS[a].Qk = 0;
                			}
                			else {
                				RS[a].Qk = RegisterStat[Integer.valueOf(bgtz.rs,2)].Reorder;
                			}
                		}
                		else {
                			RS[a].Vk = RegisterStat[Integer.valueOf(bgtz.rs,2)].Values;
                			RS[a].Qk = 0;
                		}
                		
                		RS[a].Busy = "yes";
                		RS[a].Destination = ROBid;
                		RS[a].type = bgtz;	
                		RS[a].Exe = false;
                		RS[a].PC = address-4;
                		RS[a].Instruction = instruction;
                		RS[a].pInstruction = bgtz.returnbuffer();
                		
                		ROB[b].ID = ROBid;
                		ROB[b].type = RS[a].Name;
                		ROB[b].Busy = "yes";
                		ROB[b].Ready = false;
                		ROB[b].Instruction = instruction;
                		ROB[b].Address = address;			
                		ROB[b].pInstruction = bgtz.returnbuffer();
                		bgtz.RSnumber = a;	
    				}
                	else if (opcode.equals(blez.Op)==true) {
                		blez.Divide(instruction);    		
                		RS[a].Op = blez.Op;
                		RS[a].Name = blez.Name;
                		if (RegisterStat[Integer.valueOf(blez.rs,2)].Busy=="yes"){
                			int h = 0;	
                			for (int q = 0; q < 10; q++){
                				if (ROB[q].ID == RegisterStat[Integer.valueOf(blez.rs,2)].Reorder){
                					h = q;
                				}
                			}
 	
                			if (ROB[h].Ready){
                				RS[a].Vk = ROB[h].Value;
                				RS[a].Qk = 0;
                			}
                			else {
                				RS[a].Qk = RegisterStat[Integer.valueOf(blez.rs,2)].Reorder;
                			}
                		}
                		else {
                			RS[a].Vk = RegisterStat[Integer.valueOf(blez.rs,2)].Values;
                			RS[a].Qk = 0;
                		}
                		
                		RS[a].Busy = "yes";
                		RS[a].Destination = ROBid;
                		RS[a].type = blez;	
                		RS[a].Exe = false;
                		RS[a].PC = address-4;
                		RS[a].Instruction = instruction;
                		RS[a].pInstruction = blez.returnbuffer();
                		
                		ROB[b].ID = ROBid;
                		ROB[b].type = RS[a].Name;
                		ROB[b].Busy = "yes";
                		ROB[b].Ready = false;
                		ROB[b].Instruction = instruction;
                		ROB[b].Address = address;			
                		ROB[b].pInstruction = blez.returnbuffer();
                		blez.RSnumber = a;	
    				}
                	else if (opcode.equals(addiu.Op)==true) {
                		addiu.Divide(instruction);
                		//addi.PrintInstruction();
                		RS[a].Op = addiu.Op;
                		RS[a].Name = addiu.Name;
                		RS[a].Qk = 0;		
                		RS[a].Vk = addiu.num;	
                		if (RegisterStat[Integer.valueOf(addiu.rs,2)].Busy=="yes"){
                			int h = 0;		
                			for (int q = 0; q < 10; q++){
                				if (ROB[q].ID == RegisterStat[Integer.valueOf(addiu.rs,2)].Reorder){
                					h = q;
                				}
                			}
                			if (ROB[h].Ready){
                				RS[a].Vj = ROB[h].Value;
                				RS[a].Qj = 0;
                			}
                			else {
                				RS[a].Qj = RegisterStat[Integer.valueOf(addiu.rs,2)].Reorder;
                			}
                		}
                		else {
                			RS[a].Vj = RegisterStat[Integer.valueOf(addiu.rs,2)].Values;
                			RS[a].Qj = 0;
                		}
                			
                		RS[a].Busy = "yes";
                		RS[a].Destination = ROBid;
                		RS[a].type = addiu;
                		RS[a].Exe = false;
                		RS[a].Instruction = instruction;
                		RS[a].PC = address-4;
                		RS[a].pInstruction = addiu.returnbuffer();
                		
                		ROB[b].ID = ROBid;
                		ROB[b].type = RS[a].Name;
                		ROB[b].Busy = "yes";
                		ROB[b].Ready = false;
                		ROB[b].Instruction = instruction;
                		ROB[b].Destination = Integer.valueOf(addiu.rd,2);
                		ROB[b].pInstruction = addiu.returnbuffer();
                		
                		RegisterStat[Integer.valueOf(addiu.rd,2)].Reorder = ROBid;
                		RegisterStat[Integer.valueOf(addiu.rd,2)].Busy = "yes";
                	
                		addiu.RSnumber = a;		
    				}
                	else if (opcode.equals(slti.Op)==true) {
                		slti.Divide(instruction);    		
                		RS[a].Op = slti.Op;
                		RS[a].Name = slti.Name;
                		if (RegisterStat[Integer.valueOf(slti.rs,2)].Busy=="yes"){
                			int h = 0;		
                			for (int q = 0; q < 10; q++){
                				if (ROB[q].ID == RegisterStat[Integer.valueOf(slti.rs,2)].Reorder){
                					h = q;
                				}
                			}
 	
                			if (ROB[h].Ready){
                				RS[a].Vk = ROB[h].Value;
                				RS[a].Qk = 0;
                			}
                			else {
                				RS[a].Qk = RegisterStat[Integer.valueOf(slti.rs,2)].Reorder;
                			}
                		}
                		else {
                			RS[a].Vk = RegisterStat[Integer.valueOf(slti.rs,2)].Values;
                			RS[a].Qk = 0;
                		}
                		
                		RS[a].Busy = "yes";
                		RS[a].Destination = ROBid;
                		RS[a].type = slti;	
                		RS[a].Exe = false;
                		RS[a].PC = address-4;
                		RS[a].Instruction = instruction;
                		RS[a].pInstruction = slti.returnbuffer();
                		
                		ROB[b].ID = ROBid;
                		ROB[b].type = RS[a].Name;
                		ROB[b].Busy = "yes";
                		ROB[b].Ready = false;
                		ROB[b].Instruction = instruction;
                		ROB[b].Address = address;			
                		ROB[b].pInstruction = slti.returnbuffer();
                		slti.RSnumber = a;	
    				}
                 	else if (opcode.equals(bgez.Op)==true) {
                		bgez.Divide(instruction);	    		
                		RS[a].Op = bgez.Op;
                		RS[a].Name = bgez.Name;
                		if (RegisterStat[Integer.valueOf(bgez.rs,2)].Busy=="yes"){
                			int h = 0;		
                			for (int q = 0; q < 10; q++){
                				if (ROB[q].ID == RegisterStat[Integer.valueOf(bgez.rs,2)].Reorder){
                					h = q;
                				}
                			}
 	
                			if (ROB[h].Ready){
                				RS[a].Vk = ROB[h].Value;
                				RS[a].Qk = 0;
                			}
                			else {
                				RS[a].Qk = RegisterStat[Integer.valueOf(bgez.rs,2)].Reorder;
                			}
                		}
                		else {
                			RS[a].Vk = RegisterStat[Integer.valueOf(bgez.rs,2)].Values;
                			RS[a].Qk = 0;
                		}
                		
                		RS[a].Busy = "yes";
                		RS[a].Destination = ROBid;
                		RS[a].type = bgez;	
                		RS[a].Exe = false;
                		RS[a].PC = address-4;
                		RS[a].Instruction = instruction;
                		RS[a].pInstruction = bgez.returnbuffer();
                		
                		ROB[b].ID = ROBid;
                		ROB[b].type = RS[a].Name;
                		ROB[b].Busy = "yes";
                		ROB[b].Ready = false;
                		ROB[b].Instruction = instruction;
                		ROB[b].Address = address;			
                		ROB[b].pInstruction = bgez.returnbuffer();
                		bgez.RSnumber = a;	
    				}
                	
                 	else if (opcode.equals(bltz.Op)==true) {
                		bltz.Divide(instruction);	    		
                		RS[a].Op = bltz.Op;
                		RS[a].Name = bltz.Name;
                		if (RegisterStat[Integer.valueOf(bltz.rs,2)].Busy=="yes"){
                			int h = 0;		
                			for (int q = 0; q < 10; q++){
                				if (ROB[q].ID == RegisterStat[Integer.valueOf(bltz.rs,2)].Reorder){
                					h = q;
                				}
                			}
 	
                			if (ROB[h].Ready){
                				RS[a].Vk = ROB[h].Value;
                				RS[a].Qk = 0;
                			}
                			else {
                				RS[a].Qk = RegisterStat[Integer.valueOf(bltz.rs,2)].Reorder;
                			}
                		}
                		else {
                			RS[a].Vk = RegisterStat[Integer.valueOf(bltz.rs,2)].Values;
                			RS[a].Qk = 0;
                		}
                		
                		RS[a].Busy = "yes";
                		RS[a].Destination = ROBid;
                		RS[a].type = bltz;	
                		RS[a].Exe = false;
                		RS[a].PC = address-4;
                		RS[a].Instruction = instruction;
                		RS[a].pInstruction = bltz.returnbuffer();
                		
                		ROB[b].ID = ROBid;
                		ROB[b].type = RS[a].Name;
                		ROB[b].Busy = "yes";
                		ROB[b].Ready = false;
                		ROB[b].Instruction = instruction;
                		ROB[b].Address = address;			
                		ROB[b].pInstruction = bltz.returnbuffer();
                		bltz.RSnumber = a;	
    				}
                	
                	
                	
		      }//I
  
		      if (Category.equals("J")==true){
		    		J j = new J();
    				if (opcode.equals(j.Op)==true) {
    					j.Divide(instruction);
                		//j.PrintInstruction();
                		
                		RS[a].Op = j.Op;
                		RS[a].Name = j.Name;
                		RS[a].Qk = 0;		
                		RS[a].Qj = 0;		
                		
                		RS[a].Busy = "yes";
                		RS[a].Destination = ROBid;

                		RS[a].Address = j.address;		//	imm	
                		RS[a].type = j;	//保留类型
                		RS[a].Exe = false;
                		RS[a].PC = address-4;
                		RS[a].Instruction = instruction;
                		RS[a].pInstruction = j.returnbuffer();
                		
                		ROB[b].ID = ROBid;
                		ROB[b].type = RS[a].Name;
                		ROB[b].Busy = "yes";
                		ROB[b].Ready = false;
                		ROB[b].Instruction = instruction;
                		ROB[b].pInstruction = j.returnbuffer();
                		j.RSnumber = a;
                		
    				}
		      }
		      
		      if (Category.equals("R")==true) {
	                	ADD add = new ADD();
	                	SLT slt = new SLT();
	                	SLTU sltu = new SLTU();
	                	SLL sll = new SLL();
	                	SRL srl = new SRL();
	                	SRA sra = new SRA();
	                	SUB sub = new SUB();
	                	SUBU subu = new SUBU();
	                	ADDU addu = new ADDU();
	                	AND and = new AND();
	                	OR or = new OR();
	                	XOR xor = new XOR();
	                	NOR nor = new NOR();

	                	
	                	if (function.equals(add.function)==true){
	                		add.Divide(instruction);
	                		RS[a].Op = add.Op;
	                		RS[a].Name = add.Name;
	                		if (RegisterStat[Integer.valueOf(add.rt,2)].Busy=="yes"){
	                			int h = 0;	
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(add.rt,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vj = ROB[h].Value;
	                				RS[a].Qj = 0;
	                			}
	                			else {
	                				RS[a].Qj = RegisterStat[Integer.valueOf(add.rt,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vj = RegisterStat[Integer.valueOf(add.rt,2)].Values;
	                			RS[a].Qj = 0;
	                		}
	                		
	                		if (RegisterStat[Integer.valueOf(add.rs,2)].Busy=="yes"){
	                			int h = 0;		
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(add.rs,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vk = ROB[h].Value;
	                				RS[a].Qk = 0;
	                			}
	                			else {
	                				RS[a].Qk = RegisterStat[Integer.valueOf(add.rs,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vk = RegisterStat[Integer.valueOf(add.rs,2)].Values;
	                			RS[a].Qk = 0;
	                		}
	                		
	                		RS[a].Busy = "yes";
	                		RS[a].Destination = ROBid;
	                		RS[a].type = add;	
	                		RS[a].Exe = false;
	                		RS[a].PC = address-4;
	                		RS[a].Instruction = instruction;
	                		RS[a].pInstruction = add.returnbuffer();
	                		
	                		ROB[b].ID = ROBid;
	                		ROB[b].type = RS[a].Name;
	                		ROB[b].Busy = "yes";
	                		ROB[b].Ready = false;
	                		ROB[b].Instruction = instruction;
	                		ROB[b].Destination = Integer.valueOf(add.rd,2);
	                		ROB[b].pInstruction = add.returnbuffer();
	                		
	                		RegisterStat[Integer.valueOf(add.rd,2)].Reorder = ROBid;
	                		RegisterStat[Integer.valueOf(add.rd,2)].Busy = "yes";
	                		add.RSnumber = a;         		
	                	}
	                	else if (function.equals(slt.function)==true){
	                		slt.Divide(instruction);
	                		RS[a].Op = slt.Op;
	                		RS[a].Name = slt.Name;
	                		if (RegisterStat[Integer.valueOf(slt.rt,2)].Busy=="yes"){
	                			int h = 0;		
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(slt.rt,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vj = ROB[h].Value;
	                				RS[a].Qj = 0;
	                			}
	                			else {
	                				RS[a].Qj = RegisterStat[Integer.valueOf(slt.rt,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vj = RegisterStat[Integer.valueOf(slt.rt,2)].Values;
	                			RS[a].Qj = 0;
	                		}
	                		
	                		if (RegisterStat[Integer.valueOf(slt.rs,2)].Busy=="yes"){
	                			int h = 0;		
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(slt.rs,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vk = ROB[h].Value;
	                				RS[a].Qk = 0;
	                			}
	                			else {
	                				RS[a].Qk = RegisterStat[Integer.valueOf(slt.rs,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vk = RegisterStat[Integer.valueOf(slt.rs,2)].Values;
	                			RS[a].Qk = 0;
	                		}
	                		
	                		RS[a].Busy = "yes";
	                		RS[a].Destination = ROBid;
	                		RS[a].type = slt;	
	                		RS[a].Exe = false;
	                		RS[a].PC = address-4;
	                		RS[a].Instruction = instruction;
	                		RS[a].pInstruction = slt.returnbuffer();
	                		
	                		ROB[b].ID = ROBid;
	                		ROB[b].type = RS[a].Name;
	                		ROB[b].Busy = "yes";
	                		ROB[b].Ready = false;
	                		ROB[b].Instruction = instruction;
	                		ROB[b].Destination = Integer.valueOf(slt.rd,2);
	                		ROB[b].pInstruction = slt.returnbuffer();
	                		
	                		RegisterStat[Integer.valueOf(slt.rd,2)].Reorder = ROBid;
	                		RegisterStat[Integer.valueOf(slt.rd,2)].Busy = "yes";
	                		slt.RSnumber = a;         		
	                	}
	                	
	                	else if (function.equals(sltu.function)==true){
	                		sltu.Divide(instruction);
	                		RS[a].Op = sltu.Op;
	                		RS[a].Name = sltu.Name;
	                		if (RegisterStat[Integer.valueOf(sltu.rt,2)].Busy=="yes"){
	                			int h = 0;		
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(sltu.rt,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vj = ROB[h].Value;
	                				RS[a].Qj = 0;
	                			}
	                			else {
	                				RS[a].Qj = RegisterStat[Integer.valueOf(sltu.rt,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vj = RegisterStat[Integer.valueOf(sltu.rt,2)].Values;
	                			RS[a].Qj = 0;
	                		}
	                		
	                		if (RegisterStat[Integer.valueOf(sltu.rs,2)].Busy=="yes"){
	                			int h = 0;		
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(sltu.rs,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vk = ROB[h].Value;
	                				RS[a].Qk = 0;
	                			}
	                			else {
	                				RS[a].Qk = RegisterStat[Integer.valueOf(sltu.rs,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vk = RegisterStat[Integer.valueOf(sltu.rs,2)].Values;
	                			RS[a].Qk = 0;
	                		}
	                		
	                		RS[a].Busy = "yes";
	                		RS[a].Destination = ROBid;
	                		RS[a].type = sltu;	
	                		RS[a].Exe = false;
	                		RS[a].PC = address-4;
	                		RS[a].Instruction = instruction;
	                		RS[a].pInstruction = sltu.returnbuffer();
	                		
	                		ROB[b].ID = ROBid;
	                		ROB[b].type = RS[a].Name;
	                		ROB[b].Busy = "yes";
	                		ROB[b].Ready = false;
	                		ROB[b].Instruction = instruction;
	                		ROB[b].Destination = Integer.valueOf(sltu.rd,2);
	                		ROB[b].pInstruction = sltu.returnbuffer();
	                		
	                		RegisterStat[Integer.valueOf(sltu.rd,2)].Reorder = ROBid;
	                		RegisterStat[Integer.valueOf(sltu.rd,2)].Busy = "yes";
	                		sltu.RSnumber = a;         		
	                	}
	                	else if (function.equals(sll.function)==true){
	                		sll.Divide(instruction);
	                		RS[a].Op = sll.Op;
	                		RS[a].Name = sll.Name;
	                		if (RegisterStat[Integer.valueOf(sll.rs,2)].Busy=="yes"){
	                			int h = 0;		
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(sll.rs,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vk = ROB[h].Value;
	                				RS[a].Qk = 0;
	                			}
	                			else {
	                				RS[a].Qk = RegisterStat[Integer.valueOf(sll.rs,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vk = RegisterStat[Integer.valueOf(sll.rs,2)].Values;
	                			RS[a].Qk = 0;
	                		}
	                		
	                		RS[a].Busy = "yes";
	                		RS[a].Destination = ROBid;
	                		RS[a].type = sll;	
	                		RS[a].Exe = false;
	                		RS[a].PC = address-4;
	                		RS[a].Instruction = instruction;
	                		RS[a].pInstruction = sll.returnbuffer();
	                		
	                		ROB[b].ID = ROBid;
	                		ROB[b].type = RS[a].Name;
	                		ROB[b].Busy = "yes";
	                		ROB[b].Ready = false;
	                		ROB[b].Instruction = instruction;
	                		ROB[b].Destination = Integer.valueOf(sll.rd,2);
	                		ROB[b].pInstruction = sll.returnbuffer();
	                		
	                		RegisterStat[Integer.valueOf(sll.rd,2)].Reorder = ROBid;
	                		RegisterStat[Integer.valueOf(sll.rd,2)].Busy = "yes";
	                		sll.RSnumber = a;         		
	                	}
	                	else if (function.equals(srl.function)==true){
	                		srl.Divide(instruction);
	                		RS[a].Op = srl.Op;
	                		RS[a].Name = srl.Name;
	                		if (RegisterStat[Integer.valueOf(srl.rs,2)].Busy=="yes"){
	                			int h = 0;		
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(srl.rs,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vk = ROB[h].Value;
	                				RS[a].Qk = 0;
	                			}
	                			else {
	                				RS[a].Qk = RegisterStat[Integer.valueOf(srl.rs,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vk = RegisterStat[Integer.valueOf(srl.rs,2)].Values;
	                			RS[a].Qk = 0;
	                		}
	                		
	                		RS[a].Busy = "yes";
	                		RS[a].Destination = ROBid;
	                		RS[a].type = srl;	
	                		RS[a].Exe = false;
	                		RS[a].PC = address-4;
	                		RS[a].Instruction = instruction;
	                		RS[a].pInstruction = srl.returnbuffer();
	                		
	                		ROB[b].ID = ROBid;
	                		ROB[b].type = RS[a].Name;
	                		ROB[b].Busy = "yes";
	                		ROB[b].Ready = false;
	                		ROB[b].Instruction = instruction;
	                		ROB[b].Destination = Integer.valueOf(srl.rd,2);
	                		ROB[b].pInstruction = srl.returnbuffer();
	                		
	                		RegisterStat[Integer.valueOf(srl.rd,2)].Reorder = ROBid;
	                		RegisterStat[Integer.valueOf(srl.rd,2)].Busy = "yes";
	                		srl.RSnumber = a;         		
	                	}
	                	else if (function.equals(sra.function)==true){
	                		sra.Divide(instruction);
	                		RS[a].Op = sra.Op;
	                		RS[a].Name = sra.Name;
	                		if (RegisterStat[Integer.valueOf(sra.rs,2)].Busy=="yes"){
	                			int h = 0;
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(sra.rs,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vk = ROB[h].Value;
	                				RS[a].Qk = 0;
	                			}
	                			else {
	                				RS[a].Qk = RegisterStat[Integer.valueOf(sra.rs,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vk = RegisterStat[Integer.valueOf(sra.rs,2)].Values;
	                			RS[a].Qk = 0;
	                		}
	                		
	                		RS[a].Busy = "yes";
	                		RS[a].Destination = ROBid;
	                		RS[a].type = sra;	
	                		RS[a].Exe = false;
	                		RS[a].PC = address-4;
	                		RS[a].Instruction = instruction;
	                		RS[a].pInstruction = sra.returnbuffer();
	                		
	                		ROB[b].ID = ROBid;
	                		ROB[b].type = RS[a].Name;
	                		ROB[b].Busy = "yes";
	                		ROB[b].Ready = false;
	                		ROB[b].Instruction = instruction;
	                		ROB[b].Destination = Integer.valueOf(sll.rd,2);
	                		ROB[b].pInstruction = sra.returnbuffer();
	                		
	                		RegisterStat[Integer.valueOf(sra.rd,2)].Reorder = ROBid;
	                		RegisterStat[Integer.valueOf(sra.rd,2)].Busy = "yes";
	                		sra.RSnumber = a;         		
	                	}
	                	
	                	else if (function.equals(addu.function)==true){
	                		addu.Divide(instruction);
	                		RS[a].Op = addu.Op;
	                		RS[a].Name = addu.Name;
	                		if (RegisterStat[Integer.valueOf(addu.rt,2)].Busy=="yes"){
	                			int h = 0;		
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(addu.rt,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vj = ROB[h].Value;
	                				RS[a].Qj = 0;
	                			}
	                			else {
	                				RS[a].Qj = RegisterStat[Integer.valueOf(addu.rt,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vj = RegisterStat[Integer.valueOf(addu.rt,2)].Values;
	                			RS[a].Qj = 0;
	                		}
	                		
	                		if (RegisterStat[Integer.valueOf(addu.rs,2)].Busy=="yes"){
	                			int h = 0;		
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(add.rs,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vk = ROB[h].Value;
	                				RS[a].Qk = 0;
	                			}
	                			else {
	                				RS[a].Qk = RegisterStat[Integer.valueOf(addu.rs,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vk = RegisterStat[Integer.valueOf(addu.rs,2)].Values;
	                			RS[a].Qk = 0;
	                		}
	                		
	                		RS[a].Busy = "yes";
	                		RS[a].Destination = ROBid;
	                		RS[a].type = addu;	
	                		RS[a].Exe = false;
	                		RS[a].PC = address-4;
	                		RS[a].Instruction = instruction;
	                		RS[a].pInstruction = addu.returnbuffer();
	                		
	                		ROB[b].ID = ROBid;
	                		ROB[b].type = RS[a].Name;
	                		ROB[b].Busy = "yes";
	                		ROB[b].Ready = false;
	                		ROB[b].Instruction = instruction;
	                		ROB[b].Destination = Integer.valueOf(addu.rd,2);
	                		ROB[b].pInstruction = addu.returnbuffer();
	                		
	                		RegisterStat[Integer.valueOf(addu.rd,2)].Reorder = ROBid;
	                		RegisterStat[Integer.valueOf(addu.rd,2)].Busy = "yes";
	                		addu.RSnumber = a;         		
	                	}
	                	else if (function.equals(sub.function)==true){
	                		sub.Divide(instruction);
	                		RS[a].Op = sub.Op;
	                		RS[a].Name = sub.Name;
	                		if (RegisterStat[Integer.valueOf(sub.rt,2)].Busy=="yes"){
	                			int h = 0;	
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(sub.rt,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vj = ROB[h].Value;
	                				RS[a].Qj = 0;
	                			}
	                			else {
	                				RS[a].Qj = RegisterStat[Integer.valueOf(sub.rt,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vj = RegisterStat[Integer.valueOf(sub.rt,2)].Values;
	                			RS[a].Qj = 0;
	                		}
	                		
	                		if (RegisterStat[Integer.valueOf(sub.rs,2)].Busy=="yes"){
	                			int h = 0;	
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(sub.rs,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vk = ROB[h].Value;
	                				RS[a].Qk = 0;
	                			}
	                			else {
	                				RS[a].Qk = RegisterStat[Integer.valueOf(sub.rs,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vk = RegisterStat[Integer.valueOf(sub.rs,2)].Values;
	                			RS[a].Qk = 0;
	                		}
	                		
	                		RS[a].Busy = "yes";
	                		RS[a].Destination = ROBid;
	                		RS[a].type = sub;	
	                		RS[a].Exe = false;
	                		RS[a].PC = address-4;
	                		RS[a].Instruction = instruction;
	                		RS[a].pInstruction = sub.returnbuffer();
	                		
	                		ROB[b].ID = ROBid;
	                		ROB[b].type = RS[a].Name;
	                		ROB[b].Busy = "yes";
	                		ROB[b].Ready = false;
	                		ROB[b].Instruction = instruction;
	                		ROB[b].Destination = Integer.valueOf(sub.rd,2);
	                		ROB[b].pInstruction = sub.returnbuffer();
	                		
	                		RegisterStat[Integer.valueOf(sub.rd,2)].Reorder = ROBid;
	                		RegisterStat[Integer.valueOf(sub.rd,2)].Busy = "yes";
	                		sub.RSnumber = a;         		
	                	}
	                	else if (function.equals(subu.function)==true){
	                		subu.Divide(instruction);
	                		RS[a].Op = subu.Op;
	                		RS[a].Name = subu.Name;
	                		if (RegisterStat[Integer.valueOf(subu.rt,2)].Busy=="yes"){
	                			int h = 0;	
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(subu.rt,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vj = ROB[h].Value;
	                				RS[a].Qj = 0;
	                			}
	                			else {
	                				RS[a].Qj = RegisterStat[Integer.valueOf(subu.rt,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vj = RegisterStat[Integer.valueOf(subu.rt,2)].Values;
	                			RS[a].Qj = 0;
	                		}
	                		
	                		if (RegisterStat[Integer.valueOf(subu.rs,2)].Busy=="yes"){
	                			int h = 0;		
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(subu.rs,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vk = ROB[h].Value;
	                				RS[a].Qk = 0;
	                			}
	                			else {
	                				RS[a].Qk = RegisterStat[Integer.valueOf(subu.rs,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vk = RegisterStat[Integer.valueOf(subu.rs,2)].Values;
	                			RS[a].Qk = 0;
	                		}
	                		
	                		RS[a].Busy = "yes";
	                		RS[a].Destination = ROBid;
	                		RS[a].type = subu;	
	                		RS[a].Exe = false;
	                		RS[a].PC = address-4;
	                		RS[a].Instruction = instruction;
	                		RS[a].pInstruction = subu.returnbuffer();
	                		
	                		ROB[b].ID = ROBid;
	                		ROB[b].type = RS[a].Name;
	                		ROB[b].Busy = "yes";
	                		ROB[b].Ready = false;
	                		ROB[b].Instruction = instruction;
	                		ROB[b].Destination = Integer.valueOf(subu.rd,2);
	                		ROB[b].pInstruction = subu.returnbuffer();
	                		
	                		RegisterStat[Integer.valueOf(subu.rd,2)].Reorder = ROBid;
	                		RegisterStat[Integer.valueOf(subu.rd,2)].Busy = "yes";
	                		subu.RSnumber = a;         		
	                	}
	                	
	                	else if (function.equals(and.function)==true){
	                		and.Divide(instruction);
	                		RS[a].Op = and.Op;
	                		RS[a].Name = and.Name;
	                		if (RegisterStat[Integer.valueOf(and.rt,2)].Busy=="yes"){
	                			int h = 0;		
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(and.rt,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vj = ROB[h].Value;
	                				RS[a].Qj = 0;
	                			}
	                			else {
	                				RS[a].Qj = RegisterStat[Integer.valueOf(and.rt,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vj = RegisterStat[Integer.valueOf(and.rt,2)].Values;
	                			RS[a].Qj = 0;
	                		}
	                		
	                		if (RegisterStat[Integer.valueOf(and.rs,2)].Busy=="yes"){
	                			int h = 0;		
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(and.rs,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vk = ROB[h].Value;
	                				RS[a].Qk = 0;
	                			}
	                			else {
	                				RS[a].Qk = RegisterStat[Integer.valueOf(and.rs,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vk = RegisterStat[Integer.valueOf(and.rs,2)].Values;
	                			RS[a].Qk = 0;
	                		}
	                		
	                		RS[a].Busy = "yes";
	                		RS[a].Destination = ROBid;
	                		RS[a].type = and;	
	                		RS[a].Exe = false;
	                		RS[a].PC = address-4;
	                		RS[a].Instruction = instruction;
	                		RS[a].pInstruction = and.returnbuffer();
	                		
	                		ROB[b].ID = ROBid;
	                		ROB[b].type = RS[a].Name;
	                		ROB[b].Busy = "yes";
	                		ROB[b].Ready = false;
	                		ROB[b].Instruction = instruction;
	                		ROB[b].Destination = Integer.valueOf(and.rd,2);
	                		ROB[b].pInstruction = and.returnbuffer();
	                		
	                		RegisterStat[Integer.valueOf(and.rd,2)].Reorder = ROBid;
	                		RegisterStat[Integer.valueOf(and.rd,2)].Busy = "yes";
	                		and.RSnumber = a;         		
	                	}
	                	else if (function.equals(or.function)==true){
	                		or.Divide(instruction);
	                		RS[a].Op = or.Op;
	                		RS[a].Name = or.Name;
	                		if (RegisterStat[Integer.valueOf(or.rt,2)].Busy=="yes"){
	                			int h = 0;	
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(or.rt,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vj = ROB[h].Value;
	                				RS[a].Qj = 0;
	                			}
	                			else {
	                				RS[a].Qj = RegisterStat[Integer.valueOf(or.rt,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vj = RegisterStat[Integer.valueOf(or.rt,2)].Values;
	                			RS[a].Qj = 0;
	                		}
	                		
	                		if (RegisterStat[Integer.valueOf(or.rs,2)].Busy=="yes"){
	                			int h = 0;		
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(or.rs,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vk = ROB[h].Value;
	                				RS[a].Qk = 0;
	                			}
	                			else {
	                				RS[a].Qk = RegisterStat[Integer.valueOf(or.rs,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vk = RegisterStat[Integer.valueOf(or.rs,2)].Values;
	                			RS[a].Qk = 0;
	                		}
	                		
	                		RS[a].Busy = "yes";
	                		RS[a].Destination = ROBid;
	                		RS[a].type = or;	
	                		RS[a].Exe = false;
	                		RS[a].PC = address-4;
	                		RS[a].Instruction = instruction;
	                		RS[a].pInstruction = or.returnbuffer();
	                		
	                		ROB[b].ID = ROBid;
	                		ROB[b].type = RS[a].Name;
	                		ROB[b].Busy = "yes";
	                		ROB[b].Ready = false;
	                		ROB[b].Instruction = instruction;
	                		ROB[b].Destination = Integer.valueOf(or.rd,2);
	                		ROB[b].pInstruction = or.returnbuffer();
	                		
	                		RegisterStat[Integer.valueOf(or.rd,2)].Reorder = ROBid;
	                		RegisterStat[Integer.valueOf(or.rd,2)].Busy = "yes";
	                		or.RSnumber = a;         		
	                	}
	                	else if (function.equals(xor.function)==true){
	                		xor.Divide(instruction);
	                		RS[a].Op = xor.Op;
	                		RS[a].Name = xor.Name;
	                		if (RegisterStat[Integer.valueOf(xor.rt,2)].Busy=="yes"){
	                			int h = 0;		
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(xor.rt,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vj = ROB[h].Value;
	                				RS[a].Qj = 0;
	                			}
	                			else {
	                				RS[a].Qj = RegisterStat[Integer.valueOf(xor.rt,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vj = RegisterStat[Integer.valueOf(xor.rt,2)].Values;
	                			RS[a].Qj = 0;
	                		}
	                		
	                		if (RegisterStat[Integer.valueOf(xor.rs,2)].Busy=="yes"){
	                			int h = 0;		
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(xor.rs,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vk = ROB[h].Value;
	                				RS[a].Qk = 0;
	                			}
	                			else {
	                				RS[a].Qk = RegisterStat[Integer.valueOf(xor.rs,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vk = RegisterStat[Integer.valueOf(xor.rs,2)].Values;
	                			RS[a].Qk = 0;
	                		}
	                		
	                		RS[a].Busy = "yes";
	                		RS[a].Destination = ROBid;
	                		RS[a].type = xor;	
	                		RS[a].Exe = false;
	                		RS[a].PC = address-4;
	                		RS[a].Instruction = instruction;
	                		RS[a].pInstruction = xor.returnbuffer();
	                		
	                		ROB[b].ID = ROBid;
	                		ROB[b].type = RS[a].Name;
	                		ROB[b].Busy = "yes";
	                		ROB[b].Ready = false;
	                		ROB[b].Instruction = instruction;
	                		ROB[b].Destination = Integer.valueOf(xor.rd,2);
	                		ROB[b].pInstruction = xor.returnbuffer();
	                		
	                		RegisterStat[Integer.valueOf(xor.rd,2)].Reorder = ROBid;
	                		RegisterStat[Integer.valueOf(xor.rd,2)].Busy = "yes";
	                		xor.RSnumber = a;         		
	                	}
	                  	else if (function.equals(nor.function)==true){
	                		nor.Divide(instruction);
	                		RS[a].Op = nor.Op;
	                		RS[a].Name = nor.Name;
	                		if (RegisterStat[Integer.valueOf(nor.rt,2)].Busy=="yes"){
	                			int h = 0;		
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(nor.rt,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vj = ROB[h].Value;
	                				RS[a].Qj = 0;
	                			}
	                			else {
	                				RS[a].Qj = RegisterStat[Integer.valueOf(nor.rt,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vj = RegisterStat[Integer.valueOf(nor.rt,2)].Values;
	                			RS[a].Qj = 0;
	                		}
	                		
	                		if (RegisterStat[Integer.valueOf(nor.rs,2)].Busy=="yes"){
	                			int h = 0;		
	                			for (int q = 0; q < 10; q++){
	                				if (ROB[q].ID == RegisterStat[Integer.valueOf(nor.rs,2)].Reorder){
	                					h = q;
	                				}
	                			}
	                			if (ROB[h].Ready){
	                				RS[a].Vk = ROB[h].Value;
	                				RS[a].Qk = 0;
	                			}
	                			else {
	                				RS[a].Qk = RegisterStat[Integer.valueOf(nor.rs,2)].Reorder;
	                			}
	                		}
	                		else {
	                			RS[a].Vk = RegisterStat[Integer.valueOf(nor.rs,2)].Values;
	                			RS[a].Qk = 0;
	                		}
	                		
	                		RS[a].Busy = "yes";
	                		RS[a].Destination = ROBid;
	                		RS[a].type = nor;	
	                		RS[a].Exe = false;
	                		RS[a].PC = address-4;
	                		RS[a].Instruction = instruction;
	                		RS[a].pInstruction = nor.returnbuffer();
	                		
	                		ROB[b].ID = ROBid;
	                		ROB[b].type = RS[a].Name;
	                		ROB[b].Busy = "yes";
	                		ROB[b].Ready = false;
	                		ROB[b].Instruction = instruction;
	                		ROB[b].Destination = Integer.valueOf(nor.rd,2);
	                		ROB[b].pInstruction = nor.returnbuffer();
	                		
	                		RegisterStat[Integer.valueOf(nor.rd,2)].Reorder = ROBid;
	                		RegisterStat[Integer.valueOf(nor.rd,2)].Busy = "yes";
	                		nor.RSnumber = a;         		
	                	}
		      }
	                	
		}
		else{
			//System.out.println("ROB or RS is full");
		}
					
		int j = 0;
		for (int i = 0; i <= a; i++){
			if ((RS[i].Qj==0)&&(RS[i].Qk==0)&&(RS[i].Exe==false)){
				rtype[j] = RS[i].type;
				j = j+1;
				RS[i].Exe = true;
			}
		}
		return rtype;	
	}//ID
	
	

//------------------------------EX------------------------	
	private static Result[] EX(Object[] type, int PC) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException{
		Result[] r = new Result[10];
		for (int i = 0; i < 10; i++){
			r[i]  = new Result();
		}		
		
		for (int i = 0, j = 0; (i < type.length)&&(type[i] != null); i++){
			
			Class<?> clazz = type[i].getClass();
			Field field1 = type[i].getClass().getDeclaredField("instruction");	
			String instruction = field1.get(type[i]).toString();
			Field field2 = type[i].getClass().getDeclaredField("Name");	
			String name = field2.get(type[i]).toString();
			int RSnumber = 0;
			
			for (int k = 0; k < 10; k++){
				if (instruction == RS[k].Instruction){
					RSnumber = k;
				}
			}
			
			if(name.equals("BREAK") ||name.equals("NOP")){
				r[j].type = name;
				r[j].instruction = instruction;
			}
			else if (name.equals("SW")){
				int s1 = RS[RSnumber].Vj;		//base
				Method method = type[i].getClass().getDeclaredMethod("Execute",int.class);
				r[j].result =  (int) method.invoke(type[i],s1);			//SW
				r[j].type = name;
				r[j].instruction = instruction;
			}
			else if(name.equals("LW")){
				int s1 = RS[RSnumber].Vj;
				int s2 = RS[RSnumber].Vk; 
				
				Method method = type[i].getClass().getDeclaredMethod("Execute", int.class, int.class);
				int address = (int) method.invoke(type[i],s1,s2);	
				int result = Integer.valueOf(Memory[(address-600)/4]) ;
				r[j].result =  result;			
				r[j].type = name;
				r[j].instruction = instruction;		
			}
			//Branch
			else if(name.equals("BGEZ")||(name.equals("BLTZ"))||(name.equals("BEQ"))||(name.equals("BNE"))||(name.equals("BGTZ"))||(name.equals("BLEZ"))||(name.equals("J"))){
				int s1 = RS[RSnumber].Vj;
				int s2 = RS[RSnumber].Vk;
				int pc = RS[RSnumber].PC;
				Method method = type[i].getClass().getDeclaredMethod("Execute", int.class, int.class, int.class);
				int result = (int) method.invoke(type[i],s1,s2,pc);		
				r[j].type = name;
				r[j].instruction = instruction;
				
				int takenornot = 0;	//untaken  
				if(result == pc){
					takenornot = 0;		//untaken
				}
				else{
					takenornot = 1;		//taken
				}
					
				
				for (int e = 0; (e < 10) && (BTB[e][0] != 0); e++){

					if (BTB[e][0] == (RS[RSnumber].PC)){	
						
						if (BTB[e][2] == 0){	
							if (takenornot == 0){		
								r[j].Address = 0;	
							}
							else {		
								BTB[e][1] = result;
								BTB[e][2] = 1;
								r[j].Address = result;
								
								for (int m = RSnumber+1; m<10; m++){
									RS[m] = new ReservationStation();
								}
								int ROBnumber = 0;
								for (int n = 0; n<10; n++){
									if (RS[RSnumber].Destination == ROB[n].ID){
										ROBnumber = n; 
									}
								}
								for (int m = ROBnumber+1; m<10; m++){
									ROB[m] = new ReoderBuffer();
								}
								
							}
						}
						else{		
							if (takenornot == 1){	
								r[j].Address = 0;	
							}
							else{
								BTB[e][1] = result;
								BTB[e][2] = 0;
								r[j].Address = result;
								
								for (int m = RSnumber+1; m<10; m++){
									RS[m] = new ReservationStation();
								}
								int ROBnumber = 0;
								for (int n = 0; n<10; n++){
									if (RS[RSnumber].Destination == ROB[n].ID){
										ROBnumber = n; 
									}
								}
								for (int m = ROBnumber+1; m<10; m++){
									ROB[m] = new ReoderBuffer();
								}
								
							}
							
						}
					}
				}
			}
			
			else{
				int s1 = RS[RSnumber].Vj;
				int s2 = RS[RSnumber].Vk;
				Method method = type[i].getClass().getDeclaredMethod("Execute", int.class, int.class);//加入参数
				int result = (int) method.invoke(type[i],s1,s2);
				r[j].result =  result;			
				r[j].type = name;
				r[j].instruction = instruction;	
			}	
			j = j + 1;
			RS[RSnumber].Exe = true;
		}
		return r;	
	}
	
	
//------------------------------CBD------------------------	
	private static boolean CDB (Result[] r){			
		int a = 0;
		int b = 0;
		boolean mark = false;
			
		if (r[0].type != null){
			for (int i = 0; (i < r.length)&&(r[i].type != null); i++){	
				for (int j = 0; RS[j].Instruction != null; j++){
					if(r[i].instruction == RS[j].Instruction){
						a = j;
					}
				}
				for (int k = 0; (k<10)&&(ROB[k].Instruction!=null); k++){
					if(r[i].instruction == ROB[k].Instruction){
						b = k;
					}
				}
				if((r[i].type).equals("BREAK") ||(r[i].type).equals("NOP")){
					RS[a].Busy = "no";
					ROB[b].Ready = true;
				}
				else if((r[i].type).equals("SW")){
					RS[a].Busy = "no";
					ROB[b].Ready = true;
					ROB[b].Value = RS[a].Vk;	
					ROB[b].Address = r[i].result;  
				}
				else{
					RS[a].Busy = "no";
					ROB[b].Value = Integer.valueOf(r[i].result);
					ROB[b].Ready = true;
					for (int p = 0; p < 10; p++){
						if (RS[p].Qj == RS[a].Destination){
							RS[p].Vj = ROB[b].Value;
							RS[p].Qj = 0;
						}
						if (RS[p].Qk == RS[a].Destination){
							RS[p].Vk = ROB[b].Value;
							RS[p].Qk = 0;
						}
					}
				}
	
			}
			mark = true;
		}
		else {
			mark = false;
		}
		for (int p =0; p<10; p++){
			r[p] = new Result();
		}
		return mark;
	}
	
	
//------------------------------COMMIT------------------------
private static int Commit(boolean mark){		
	int back = 0;
	int mPC = 0;
	int n = 0;
	
	for (int i = 0; i < 10; i++){
		if(ROB[0].Instruction == RS[i].Instruction){
			n = i;
		}
	}
	
	//删除指令(RS/ROB)
	if((ROB[0].Ready == true)){
		if ((ROB[0].type).equals("BREAK")){
			back = 1;
			
		}
		else if(ROB[0].type=="SW"){
			int address = (ROB[0].Address-600)/4;
			Memory[address] = String.valueOf(ROB[0].Value);
			back = 0;
		}
		else {
			int j = ROB[0].Destination;
			RegisterStat[j].Values = ROB[0].Value;
			RegisterStat[j].Reorder = 0;
			RegisterStat[j].Busy = "no";
			back = 0;
		}
		
		//删除指令
		int i = 0;
		for(i = 0; i < 9; i++){
			RS[i] = RS[i+1];
		}
		RS[i] = new ReservationStation();
		int j = 0;
		for(j = 0; j < 9; j++){
			ROB[j] = ROB[j+1];	
		}
		ROB[j] = new ReoderBuffer();
	}
	return back;
}
	
	
	
	public static void main(String[] args) throws FileNotFoundException, EOFException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException{	
		
		String input = args[0];
		String output = args[1];
		String strattrace = args[2];
		String endtrace = args[3];
		
		String[] instructionset = PR(input);
		int PC = 600;		//(pc-600)/4
		int newPC = 0;
		int back = 0;
		boolean m = false;
		boolean t = false;
		Object[] o = new Object[10];
		Result[] r = new Result[10];
		for (int i = 0; i < 10; i++){
			r[i]  = new Result();
		}	
		
		String i = "";
		int clock = 1;
		
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(output));  

	
		do{	
			String buffer = "";
			if (clock == 1){
				for (int p = 0; p < 10; p++){
				RS[p]  = new ReservationStation();
				ROB[p] = new ReoderBuffer();
				}
				for (int p = 0; p < 32; p++){
					RegisterStat[p]  = new RegisterStatus();
				}
			}


			if (t){
				back = Commit(t);
			}
			if (r[0].instruction != null){
				m = CDB(r);
			}
			 
			if (o[0] != null){
				r = EX(o, PC);
			}
			if (i != "" ){
				o = ID(i,PC,clock);
			}
			int ad = 0;
			for (int a = 0; a<10; a++){
				if (r[a].Address !=0){
					ad = r[a].Address;
				}
			}
			if(ad == 0){
				i = instructionset[(PC-600)/4];
				newPC = IF(instructionset[(PC-600)/4], PC);	
			}
			else{
				i = instructionset[(ad-600)/4];
				PC = ad;
				newPC = IF(instructionset[(PC-600)/4], PC);		
			}
			
			if ((Integer.valueOf(strattrace) == 0) && (Integer.valueOf(endtrace) == 0)){	//no-tracing
				System.out.println("CLOCK<"+ clock +">");
				buffer = "CLOCK<"+ clock +">\n";
				dos.writeBytes(buffer); 
				
				System.out.println("IQ:");
				System.out.println(i);
				buffer = "IQ:\n";
				dos.writeBytes(buffer); 
				buffer = i + "\n";
				dos.writeBytes(buffer); 

				System.out.println("RS is :" );
				buffer = "RS is :\n";
				dos.writeBytes(buffer); 

				for (int x = 0; (x < 10)&&(RS[x].Name!=null); x ++){
					System.out.println(RS[x].pInstruction);
					buffer = RS[x].pInstruction + "\n";
					dos.writeBytes(buffer); 
				}
				
				System.out.println("ROB is :" );
				buffer = "ROB is :\n";
				dos.writeBytes(buffer); 
				for (int x = 0; (x < 10)&&(ROB[x].pInstruction!=null) ; x ++){
					buffer = ROB[x].pInstruction + "\n";
					dos.writeBytes(buffer); 
					System.out.println(ROB[x].Busy + " " + ROB[x].Ready + " "+ ROB[x].Destination + " " +ROB[x].Value + " " + ROB[x].type);
				}
				
				buffer = "Registers is :\n";
				dos.writeBytes(buffer); 
				System.out.println("Registers is :" );
				for (int x = 0; x < RegisterStat.length; x ++){
					System.out.print(RegisterStat[x].Reorder + " ");
				}
				System.out.println();
				
				
				for (int x = 0; x < RegisterStat.length; x ++){
					System.out.print(RegisterStat[x].Values + " ");
					buffer = RegisterStat[x].Values + " ";
					dos.writeBytes(buffer); 
				}
				buffer = "\n";
				dos.writeBytes(buffer); 
				System.out.println();
				
				
				System.out.println("BTB is :" );
				buffer = "BTB is :\n";
				dos.writeBytes(buffer); 
				
				for (int x = 0; (x < 10) && (BTB[x][0] != 0 ); x ++){
					System.out.println("<" + BTB[x][0] +"," + BTB[x][1] +"," + BTB[x][2] + ">");
					buffer = "<" + BTB[x][0] +"," + BTB[x][1] +"," + BTB[x][2] + ">\n";
					dos.writeBytes(buffer);
				}
				
				
				System.out.println("Memory is :" );
				buffer = "Memory is :\n";
				dos.writeBytes(buffer);
				for (int x = 29; (x < 50) ; x ++){
					System.out.print(Memory[x] +",");
					buffer = Memory[x] +",";
					dos.writeBytes(buffer);
				}
				
				buffer = "\n";
				dos.writeBytes(buffer);
				buffer = "\n";
				dos.writeBytes(buffer);
				
				System.out.println();
				System.out.println("PC is :" + PC);
				System.out.println();
			}
			else{		//tracing
				if ((clock >= Integer.valueOf(strattrace)) && (clock <= Integer.valueOf(endtrace))){
					System.out.println("CLOCK<"+ clock +">");
					buffer = "CLOCK<"+ clock +">\n";
					dos.writeBytes(buffer); 
					
					System.out.println("IQ:");
					System.out.println(i);
					buffer = "IQ:\n";
					dos.writeBytes(buffer); 
					buffer = i + "\n";
					dos.writeBytes(buffer); 

					System.out.println("RS is :" );
					buffer = "RS is :\n";
					dos.writeBytes(buffer); 

					for (int x = 0; (x < 10)&&(RS[x].Name!=null); x ++){
						System.out.println(RS[x].pInstruction);
						buffer = RS[x].pInstruction + "\n";
						dos.writeBytes(buffer); 
					}
					
					
					System.out.println("ROB is :" );
					buffer = "ROB is :\n";
					dos.writeBytes(buffer); 
					for (int x = 0; (x < 10)&&(ROB[x].Instruction!=null) ; x ++){
						buffer = ROB[x].pInstruction + "\n";
						dos.writeBytes(buffer); 
						System.out.println(ROB[x].Busy + " " + ROB[x].Ready + " "+ ROB[x].Destination + " " +ROB[x].Value + " " + ROB[x].type);
					}
					System.out.println("Registers is :" );
					for (int x = 0; x < RegisterStat.length; x ++){
						System.out.print(RegisterStat[x].Reorder + " ");
					}
					System.out.println();
					
					
					for (int x = 0; x < RegisterStat.length; x ++){
						System.out.print(RegisterStat[x].Values + " ");
						buffer = RegisterStat[x].Values + " ";
						dos.writeBytes(buffer); 
					}
					buffer = "\n";
					dos.writeBytes(buffer); 
					System.out.println();
					
					
					System.out.println("BTB is :" );
					buffer = "BTB is :\n";
					dos.writeBytes(buffer); 
					
					for (int x = 0; (x < 10) && (BTB[x][0] != 0 ); x ++){
						System.out.println("<" + BTB[x][0] +"," + BTB[x][1] +"," + BTB[x][2] + ">");
						buffer = "<" + BTB[x][0] +"," + BTB[x][1] +"," + BTB[x][2] + ">\n";
						dos.writeBytes(buffer);
					}
					
					
					System.out.println("Memory is :" );
					buffer = "Memory is :\n";
					dos.writeBytes(buffer);
					for (int x = 29; (x < 50) ; x ++){
						System.out.print(Memory[x] +",");
						buffer = Memory[x] +",";
						dos.writeBytes(buffer);
					}
					
					buffer = "\n";
					dos.writeBytes(buffer);
					buffer = "\n";
					dos.writeBytes(buffer);
					
					System.out.println("PC is :" + PC);
					System.out.println();
					
				}
			}	
			if (ROB[0].Instruction != null){
				t = true;
			}				
			PC = newPC;
			clock = clock + 1;
			ROBid = ROBid +1;
			
		}while((PC <= 716)&&(i != "")&&(clock<50) && (back ==0 ));//&&(RS.length>0)&&(ROB.length>0));
		
	}//main	

}






//-----------------------------------------------

class ReoderBuffer{
	String type;
	String Busy;
	boolean Ready;
	String Instruction;
	int Destination;
	int Source;
	int Value;
	int Address;
	int ID;
	String pInstruction;
}

class ReservationStation{
	String Name;
	String Busy;
	String Op;
	int Vj;
	int Vk;
	int Qj;
	int Qk;
	int Destination;
	int Address;
	Object type;
	boolean Exe;
	String Instruction;
	int PC;
	String pInstruction;
}

class RegisterStatus{
	String Busy;
	int Reorder;
	int Values;
}

class Result{
	String Destination;
	int result;
	String type;
	int RSnumber;
	String instruction;
	int Address;
}

//meet negative number
class Binary2Integer{
	public int B2I(String s){
		s = s.replaceAll("0", "2");
		s = s.replaceAll("1", "0");
		s = s.replaceAll("2", "1");
		int num = Integer.valueOf(s,2) + 1;
		return (-1*num);
	}
}

//make a category
//-----------------NOP-----------------
class NOP{
	String Name = "NOP";
	String instruction = "";
	String b1 = "";
	String b2 = "";
	public void Divide(String s){
		instruction  = s;
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name);
		b2 = Name;
	}
	public String returnbuffer()
	{
		String buff = Name;		
		return buff;
	}
}
//-----------------BREAK-----------------
class BREAK{
	String Name = "BREAK";
	String instruction = "";
	String b1 = "";
	String b2 = "";
	public void Divide(String s){
		instruction  = s;
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name);
		b2 = Name;
	}
	public String returnbuffer()
	{
		String buff = Name;		
		return buff;
	}
}



//instructions
class SW{
	String Op = "101011";
	String Name = "SW";
	String instruction = "";
	String rs = ""; 
	String rt = ""; 
	String offset = ""; 
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;

	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		rt = instruction.substring(11,16);
		offset = instruction.substring(16,32);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rt,2).toString() + ", " + Integer.valueOf(offset,2).toString() + "(R" + Integer.valueOf(rs,2).toString() +")");
		b2 = Name + " " + "R" + Integer.valueOf(rt,2).toString() + ", " + Integer.valueOf(offset,2).toString() + "(R" + Integer.valueOf(rs,2).toString() +")";
	}
	
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rt,2).toString() + ", " + Integer.valueOf(offset,2).toString() + "(R" + Integer.valueOf(rs,2).toString() +")";
		return buff;
	}
	
	public int Execute(int s1){
		int address = Integer.valueOf(offset,2) + s1;
		return address;
	}
}


class LW{
	String Op = "100011";
	String Name = "LW";
	String instruction = "";
	String rs = ""; 
	String rd = ""; 
	String offset = ""; 
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		rd = instruction.substring(11,16);
		offset = instruction.substring(16,32);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rd).toString() + ", " + Integer.valueOf(offset,2).toString() + "(R" + Integer.valueOf(rs,2).toString() +")");
		b2 = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", " + Integer.valueOf(offset,2).toString() + "(R" + Integer.valueOf(rs,2).toString() +")";
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", " + Integer.valueOf(offset,2).toString() + "(R" + Integer.valueOf(rs,2).toString() +")";
		return buff;
	}
	public int Execute(int s1, int s2){
		//System.out.println("LW偏移值" + Integer.valueOf(offset,2));
		int address = Integer.valueOf(offset,2) + s1;
		return address;
	}
}

class BEQ{
	String Op = "000100";
	String Name = "BEQ";
	String instruction = "";
	String rs = ""; 
	String rt = ""; 
	String offset = ""; 
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		rt = instruction.substring(11,16);
		offset = instruction.substring(16,32);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rs,2).toString() + ", " + "R" + Integer.valueOf(rt,2).toString() +", #"+ Integer.valueOf(offset,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rs,2).toString() + ", " + "R" + Integer.valueOf(rt,2).toString() +", #"+ Integer.valueOf(offset,2).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rs,2).toString() + ", " + "R" + Integer.valueOf(rt,2).toString() +", #"+ Integer.valueOf(offset,2).toString();
		
		return buff;
	}
	public int Execute(int s1, int s2, int s3){	
		int address = 0;
		if(s1 == s2){
			address = Integer.valueOf(offset,2)*4 + s3 +4;
		}
		else{
			address = s3;
		}
		
		return address;
	}
}

class BNE{
	String Op = "000101";
	String Name = "BNE";
	String instruction = "";
	String rs = ""; 
	String rt = ""; 
	String offset = ""; 
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		rt = instruction.substring(11,16);
		offset = instruction.substring(16,32);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rs,2).toString() + ", " + "R" + Integer.valueOf(rt,2).toString() +", "+ Integer.valueOf(offset,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rs,2).toString() + ", " + "R" + Integer.valueOf(rt,2).toString() +", "+ Integer.valueOf(offset,2).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rs,2).toString() + ", " + "R" + Integer.valueOf(rt,2).toString() +", "+ Integer.valueOf(offset,2).toString();
;		
		return buff;
	}
	public int Execute(int s1, int s2){ 
		int address = 0;
		if(s1 != s2){
			address = Integer.valueOf(offset,2);
		}		
		return address;
	}
}

class BGTZ{
	String Op = "000111";
	String Name = "BGTZ";
	String instruction = "";
	String rs = ""; 
	String offset = ""; 
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		offset = instruction.substring(16,32);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(offset,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(offset,2).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(offset,2).toString();
	
		return buff;
	}
	public int Execute(int s1, int s2){
		int address = 0;
		if(s1 > 0){
			address = Integer.valueOf(offset,2);
		}		
		return address;
	}	
}

class BLEZ{
	String Op = "000110";
	String Name = "BLEZ";
	String instruction = "";
	String rs = ""; 
	String offset = ""; 
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		offset = instruction.substring(16,32);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(offset,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(offset,2).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(offset,2).toString();
		return buff;
	}
	public int Execute(int s1, int s2){
		int address = 0;
		if(s1 <= 0){
			address = Integer.valueOf(offset,2);
		}		
		return address;
	}
}

class ADDI{
	String Op = "001000";
	String Name = "ADDI";
	String instruction = "";
	String rs = ""; 
	String rd = ""; 
	String immediate = ""; 
	int num;
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		rd = instruction.substring(11,16);
		immediate = instruction.substring(16,32);
		
		if (immediate.substring(0,1).equals("1")) {		//immediate负数		
			Binary2Integer x = new Binary2Integer();
			num = x.B2I(immediate);
		}
		else {
			num = Integer.valueOf(immediate,2);
		}
	}	
	
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);

	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", " + "R" + Integer.valueOf(rs,2).toString() +", #"+ Integer.valueOf(num).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", " + "R" + Integer.valueOf(rs,2).toString() +", #"+ Integer.valueOf(num).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", " + "R" + Integer.valueOf(rs,2).toString() +", #"+ Integer.valueOf(num).toString();
		return buff;
	}
	
	public int Execute(int s1, int s2){
		int result = s1 + num;
		return result;
	}
	
}

class ADDIU{
	String Op = "001001";
	String Name = "ADDI";
	String instruction = "";
	String rs = ""; //b-d
	String rd = ""; //b-d
	String immediate = ""; //b-d
	String b1 = "";
	String b2 = "";
	int num = 0;
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		rd = instruction.substring(11,16);
		immediate = instruction.substring(16,32);
		num = Integer.valueOf(immediate,2);
	}
		
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", " + "R" + Integer.valueOf(rs,2).toString() +", #"+ Integer.valueOf(immediate,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", " + "R" + Integer.valueOf(rs,2).toString() +", #"+ Integer.valueOf(immediate,2).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", " + "R" + Integer.valueOf(rs,2).toString() +", #"+ Integer.valueOf(immediate,2).toString();		
		return buff;
	}
	public int Execute(int s1, int s2){
		int result = s1 + num;
		return result;
	}
}

class SLTI{
	String Op = "001010";
	String Name = "SLTI";
	String instruction = "";
	String rs = ""; 
	String rd = ""; 
	String immediate = ""; 
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		rd = instruction.substring(11,16);
		immediate = instruction.substring(16,32);
	}		
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", " + "R" + Integer.valueOf(rs,2).toString() +", "+ Integer.valueOf(immediate,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", " + "R" + Integer.valueOf(rs,2).toString() +", "+ Integer.valueOf(immediate,2).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", " + "R" + Integer.valueOf(rs,2).toString() +", "+ Integer.valueOf(immediate,2).toString();
		
		return buff;
	}
	public int Execute(int s1, int s2){
		int result = 0;
		if(s1 < Integer.valueOf(immediate,2)){
			result = 1;
		}	
		else{
			result = 0;
		}
		return result;
	}

}

class BGEZ{
	String Op = "000001";
	String Name = "BGEZ";
	String instruction = "";
	String rs = ""; 
	String offset = ""; 
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		offset = instruction.substring(16,32);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(offset,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(offset,2).toString();
	}
	public String returnbuffer()
	{
		String buff =  Name + " " + "R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(offset,2).toString();
		return buff;
	}
	public int Execute(int s1, int s2){
		int address = 0;
		if(s1 >= 0){
			address = Integer.valueOf(offset,2);
		}		
		return address;
	}
}

class BLTZ{
	String Op = "000001";
	String Name = "BLTZ";
	String instruction = "";
	String rs = ""; 
	String offset = ""; 
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		offset = instruction.substring(16,32);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(offset,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(offset,2).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(offset,2).toString();
		return buff;
	}
	public int Execute(int s1, int s2){
		int address = 0;
		if(s1 < 0){
			address = Integer.valueOf(offset,2);
		}		
		return address;
	}
}



//-----------------J-----------------
class J{
	String Op = "000010";
	String Name = "J";
	String instruction = "";
	String index = "";
	int address = 0;
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		index = instruction.substring(6,32);
		address = Integer.valueOf(index,2) * 4;
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " #" + Integer.valueOf(address).toString());
		b2 = Name + " #" + Integer.valueOf(address).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " #" + Integer.valueOf(address).toString();
		
		return buff;
	}
	//-------------------------------------------执行之问题？？
	public int Execute(int s1, int s2, int s3){
		return address;
	}
}
//-----------------R-----------------
class SLT{
	String Op = "000000";
	String Name = "SLT";
	String instruction = "";
	String rs = ""; 
	String rt = ""; 
	String rd = ""; 
	String function = "101010";
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		rt = instruction.substring(11,16);
		rd = instruction.substring(16,21);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString();
		return buff;
	}
	public int Execute(int s1, int s2){
		int result = 0;
		if(s2 < s1){
			result = 1;
		}	
		else{
			result = 0;
		}
		return result;
	}
	
}

class SLTU{
	String Op = "000000";
	String Name = "SLTU";
	String instruction = "";
	String rs = ""; 
	String rt = ""; 
	String rd = ""; 
	String function = "101011";
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		rt = instruction.substring(11,16);
		rd = instruction.substring(16,21);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString();
		return buff;
	}
	public int Execute(int s1, int s2){
		int result = 0;
		if(s2 < s1){
			result = 1;
		}	
		else{
			result = 0;
		}
		return result;
	}
}

class SLL{
	String Op = "000000";
	String Name = "SLL";
	String instruction = "";
	String sa = ""; 
	String rs = ""; 
	String rd = ""; 
	String function = "000000";
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(11,16);
		rd = instruction.substring(16,21);
		sa = instruction.substring(21,26);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(sa,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(sa,2).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(sa,2).toString();
		return buff;
	}
	public int Execute(int s1, int s2){
		int result = s1 << Integer.valueOf(sa);
		return result;
	}
}

class SRL{
	String Op = "000000";
	String Name = "SRL";
	String instruction = "";
	String sa = ""; 
	String rs = ""; 
	String rd = ""; 
	String function = "000010";
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(11,16);
		rd = instruction.substring(16,21);
		sa = instruction.substring(21,26);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(sa,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(sa,2).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(sa,2).toString();
		return buff;
	}
	public int Execute(int s1, int s2){
		int result = s1 >> Integer.valueOf(sa);
		return result;
	}
}


class SRA{
	String Op = "000000";
	String Name = "SRA";
	String instruction = "";
	String sa = ""; 
	String rs = ""; 
	String rd = ""; 
	String function = "000011";
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(11,16);
		rd = instruction.substring(16,21);
		sa = instruction.substring(21,26);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(sa,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(sa,2).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", " + Integer.valueOf(sa,2).toString();
		return buff;
	}
	public int Execute(int s1, int s2){
		int result = s1 >> Integer.valueOf(sa);
		return result;
	}
}

class SUB{					//---------------负数问题
	String Op = "000000";
	String Name = "SUB";
	String instruction = "";
	String rs = ""; 
	String rt = ""; 
	String rd = ""; 
	String function = "100010";
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		rt = instruction.substring(11,16);
		rd = instruction.substring(16,21);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString();
		return buff;
	}
	public int Execute(int s1, int s2){
		int result = s2 - s1;
		return result;
	}
}

class SUBU{
	String Op = "000000";
	String Name = "SUBU";
	String instruction = "";
	String rs = ""; 
	String rt = ""; 
	String rd = ""; 
	String function = "100011";
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		rt = instruction.substring(11,16);
		rd = instruction.substring(16,21);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString();
		return buff;
	}
	public int Execute(int s1, int s2){
		int result = s2 - s1;
		return result;
	}
}

class ADD{				
	String Op = "000000";
	String Name = "ADD";
	String instruction = "";
	String rs = ""; 
	String rt = ""; 
	String rd = ""; 
	String function = "100000";
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		rt = instruction.substring(11,16);
		rd = instruction.substring(16,21);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString();	
		return buff;
	}
	public int Execute(int s1, int s2){
		int result = s2 + s1;
		return result;
	}
}

class ADDU{
	String Op = "000000";
	String Name = "ADDU";
	String instruction = "";
	String rs = ""; 
	String rt = ""; 
	String rd = ""; 
	String function = "100001";
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		rt = instruction.substring(11,16);
		rd = instruction.substring(16,21);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString();
		return buff;
	}
	public int Execute(int s1, int s2){
		int result = s2 + s1;
		return result;
	}
}

class AND{
	String Op = "000000";
	String Name = "AND";
	String instruction = "";
	String rs = ""; 
	String rt = ""; 
	String rd = ""; 
	String function = "100100";
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		rt = instruction.substring(11,16);
		rd = instruction.substring(16,21);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString();
		return buff;
	}
	public int Execute(int s1, int s2){
		int result = s2 & s1;
		return result;
	}
}

class OR{
	String Op = "000000";
	String Name = "OR";
	String instruction = "";
	String rs = ""; 
	String rt = ""; 
	String rd = ""; 
	String function = "100101";
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		rt = instruction.substring(11,16);
		rd = instruction.substring(16,21);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString();
		return buff;
	}
	public int Execute(int s1, int s2){
		int result = s2 | s1;
		return result;
	}
}

class XOR{
	String Op = "000000";
	String Name = "XOR";
	String instruction = "";
	String rs = ""; 
	String rt = ""; 
	String rd = ""; 
	String function = "100110";
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		rt = instruction.substring(11,16);
		rd = instruction.substring(16,21);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString();
		return buff;
	}
	public int Execute(int s1, int s2){
		int result = s2 ^ s1;
		return result;
	}
}

class NOR{
	String Op = "000000";
	String Name = "NOR";
	String instruction = "";
	String rs = ""; 
	String rt = ""; 
	String rd = ""; 
	String function = "100111";
	String b1 = "";
	String b2 = "";
	int RSnumber = 0;
	public void Divide(String s){
		instruction  = s;
		rs = instruction.substring(6,11);
		rt = instruction.substring(11,16);
		rd = instruction.substring(16,21);
	}
	public void PrintBinary(){
		System.out.print(instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32));
		b1 = instruction.substring(0,6) + " " + instruction.substring(6,11) + " " + instruction.substring(11,16) + " " + instruction.substring(16,21) + " " + instruction.substring(21,26) + " " + instruction.substring(26,32);
	}
	public void PrintInstruction(){
		System.out.println(Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString());
		b2 = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString();
	}
	public String returnbuffer()
	{
		String buff = Name + " " + "R" + Integer.valueOf(rd,2).toString() + ", R" + Integer.valueOf(rs,2).toString() + ", R" + Integer.valueOf(rt,2).toString();
		return buff;
	}
//-------------------------------------------------
	public int Execute(int s1, int s2){
		int result = 0;
		return result;
	}
}




















