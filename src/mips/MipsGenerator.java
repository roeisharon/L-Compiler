/***********/
/* PACKAGE */
/***********/
package mips;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import temp.*;
import types.TypeClass;

public class MipsGenerator
{
	private static final int WORD_SIZE=4;
	private static final String[] TEMP_REGS = {
		"$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7", "$t8", "$t9"
	};
	/***********************/
	/* The file writer ... */
	/***********************/
	private PrintWriter fileWriter;
	private static String outputPath = null;
	private final Map<String, Integer> dataVars = new HashMap<>();
	private final Set<String> emittedVars = new HashSet<>();
	private final Map<String, Map<String, Integer>> localOffsetsByFunction = new HashMap<>();
	private final Map<Integer, String> tempRegBySerial = new HashMap<>();
	private String currentFunction = null;
	private int stringCounter = 0;
	private int labelCounter = 0;
	private static final int FRAME_HEADER_SIZE = 40; // s0-s7 (32) + ra (4) + fp (4)
	private static final int INT_MAX = 32767;
	private static final int INT_MIN = -32768;

	/***********************/
	/* The file writer ... */
	/***********************/
	public void finalizeFile()
	{
		fileWriter.close();
	}

	private String regOf(Temp t) {
		String mapped = tempRegBySerial.get(t.getSerialNumber());
		if (mapped != null) {
			return mapped;
		}
		throw new RuntimeException("Register Allocation Failed");
	}

	public void setTempRegisterAllocation(Map<Integer, Integer> allocation) {
		tempRegBySerial.clear();
		for (Map.Entry<Integer, Integer> e : allocation.entrySet()) {
			int color = e.getValue();
			if (color < 0 || color >= TEMP_REGS.length) {
				throw new RuntimeException("Register Allocation Failed");
			}
			tempRegBySerial.put(e.getKey(), TEMP_REGS[color]);
		}
	}

	public void emit(String text) {
		fileWriter.print(text);
	}

	private String sanitizeLabel(String raw) {
		return raw.replace('#', '_').replace('-', '_');
	}

	private void ensureDataVar(String varName) {
		String safe = sanitizeLabel(varName);
		if (emittedVars.contains(safe)) return;
		emittedVars.add(safe);
		emit(".data\n");
		emit("global_" + safe + ": .word 0\n");
		emit(".text\n");
	}

	public void printInt(Temp t)
	{
		String r = regOf(t);
		fileWriter.format("\tmove $a0,%s\n",r);
		fileWriter.format("\tli $v0,1\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tli $a0,32\n");
		fileWriter.format("\tli $v0,11\n");
		fileWriter.format("\tsyscall\n");
	}

	public void printString(Temp t) {
		String r = regOf(t);
		fileWriter.format("\tmove $a0,%s\n", r);
		fileWriter.format("\tli $v0,4\n");
		fileWriter.format("\tsyscall\n");
	}

	public void loadStringConst(Temp dst, String value) {
		String label = "str_const_" + (stringCounter++);
		emit(".data\n");
		emit(label + ": .asciiz " + value + "\n");
		emit(".text\n");
		emit("\tla " + regOf(dst) + ", " + label + "\n");
	}

	public void stringConcat(Temp dst, Temp left, Temp right) {
		String rDst = regOf(dst);
		String rLeft = regOf(left);
		String rRight = regOf(right);
		int lbl = labelCounter++;

		emit("\tbeq " + rLeft + ",$zero,__invalid_ptr\n");
		emit("\tbeq " + rRight + ",$zero,__invalid_ptr\n");

		// len(left) -> $a2
		emit("\tmove $a1," + rLeft + "\n");
		emit("\tli $a2,0\n");
		emit("__str_len_l_loop_" + lbl + ":\n");
		emit("\tlb $a3,0($a1)\n");
		emit("\tbeq $a3,$zero,__str_len_l_end_" + lbl + "\n");
		emit("\taddiu $a2,$a2,1\n");
		emit("\taddiu $a1,$a1,1\n");
		emit("\tj __str_len_l_loop_" + lbl + "\n");
		emit("__str_len_l_end_" + lbl + ":\n");

		// len(right) -> $t0
		emit("\tmove $a1," + rRight + "\n");
		emit("\tli $k0,0\n");
		emit("__str_len_r_loop_" + lbl + ":\n");
		emit("\tlb $a3,0($a1)\n");
		emit("\tbeq $a3,$zero,__str_len_r_end_" + lbl + "\n");
		emit("\taddiu $k0,$k0,1\n");
		emit("\taddiu $a1,$a1,1\n");
		emit("\tj __str_len_r_loop_" + lbl + "\n");
		emit("__str_len_r_end_" + lbl + ":\n");

		// allocate len(left)+len(right)+1 bytes
		emit("\taddu $a0,$a2,$k0\n");
		emit("\taddiu $a0,$a0,1\n");
		emit("\tli $v0,9\n");
		emit("\tsyscall\n");
		emit("\tmove " + rDst + ",$v0\n");

		// copy left into dst cursor ($a2)
		emit("\tmove $a1," + rLeft + "\n");
		emit("\tmove $a2," + rDst + "\n");
		emit("__str_copy_l_loop_" + lbl + ":\n");
		emit("\tlb $a3,0($a1)\n");
		emit("\tbeq $a3,$zero,__str_copy_l_end_" + lbl + "\n");
		emit("\tsb $a3,0($a2)\n");
		emit("\taddiu $a1,$a1,1\n");
		emit("\taddiu $a2,$a2,1\n");
		emit("\tj __str_copy_l_loop_" + lbl + "\n");
		emit("__str_copy_l_end_" + lbl + ":\n");

		// append right
		emit("\tmove $a1," + rRight + "\n");
		emit("__str_copy_r_loop_" + lbl + ":\n");
		emit("\tlb $a3,0($a1)\n");
		emit("\tbeq $a3,$zero,__str_copy_r_end_" + lbl + "\n");
		emit("\tsb $a3,0($a2)\n");
		emit("\taddiu $a1,$a1,1\n");
		emit("\taddiu $a2,$a2,1\n");
		emit("\tj __str_copy_r_loop_" + lbl + "\n");
		emit("__str_copy_r_end_" + lbl + ":\n");

		// null terminator
		emit("\tsb $zero,0($a2)\n");
	}

	public void stringEquals(Temp dst, Temp left, Temp right) {
		String rDst = regOf(dst);
		String rLeft = regOf(left);
		String rRight = regOf(right);
		int lbl = labelCounter++;

		emit("\tbeq " + rLeft + ",$zero,__invalid_ptr\n");
		emit("\tbeq " + rRight + ",$zero,__invalid_ptr\n");
		emit("\tmove $a1," + rLeft + "\n");
		emit("\tmove $a2," + rRight + "\n");
		emit("__str_eq_loop_" + lbl + ":\n");
		emit("\tlb $a3,0($a1)\n");
		emit("\tlb $v1,0($a2)\n");
		emit("\tbne $a3,$v1,__str_eq_false_" + lbl + "\n");
		emit("\tbeq $a3,$zero,__str_eq_true_" + lbl + "\n");
		emit("\taddiu $a1,$a1,1\n");
		emit("\taddiu $a2,$a2,1\n");
		emit("\tj __str_eq_loop_" + lbl + "\n");
		emit("__str_eq_false_" + lbl + ":\n");
		emit("\tli " + rDst + ",0\n");
		emit("\tj __str_eq_end_" + lbl + "\n");
		emit("__str_eq_true_" + lbl + ":\n");
		emit("\tli " + rDst + ",1\n");
		emit("__str_eq_end_" + lbl + ":\n");
	}
//	public Temp addressLocalVar(int serialLocalVarNum)
//	{
//		Temp t  = TempFactory.getInstance().getFreshTemp();
//		int idx = t.getSerialNumber();
//
//		fileWriter.format("\taddi Temp_%d,$fp,%d\n",idx,-serialLocalVarNum*WORD_SIZE);
//
//		return t;
//	}
	public void allocate(String varName)
	{
		ensureDataVar(varName);
	}
	public void load(Temp dst, String varName)
	{
		String safe = sanitizeLabel(varName);
		ensureDataVar(safe);
		fileWriter.format("\tlw %s,global_%s\n",regOf(dst),safe);
	}
	public void store(String varName, Temp src)
	{
		String safe = sanitizeLabel(varName);
		ensureDataVar(safe);
		fileWriter.format("\tsw %s,global_%s\n",regOf(src),safe);
	}

	public void loadParam(Temp dst, int paramIndex) {
		int offset = FRAME_HEADER_SIZE + (paramIndex * 4);
		fileWriter.format("\tlw %s,%d($fp)\n", regOf(dst), offset);
	}

	public void storeParam(int paramIndex, Temp src) {
		int offset = FRAME_HEADER_SIZE + (paramIndex * 4);
		fileWriter.format("\tsw %s,%d($fp)\n", regOf(src), offset);
	}

	public void setFunctionLocals(String functionName, Set<String> locals) {
		Map<String, Integer> offsets = new HashMap<>();
		int slot = 1;
		for (String name : locals) {
			offsets.put(name, -4 * slot);
			slot++;
		}
		localOffsetsByFunction.put(functionName, offsets);
	}

	private int getLocalOffset(String functionName, String varName) {
		Map<String, Integer> offsets = localOffsetsByFunction.get(functionName);
		if (offsets == null || !offsets.containsKey(varName)) {
			throw new RuntimeException("Unknown local var " + functionName + ":" + varName);
		}
		return offsets.get(varName);
	}

	public void loadLocal(Temp dst, String varName, String functionName) {
		int off = getLocalOffset(functionName, varName);
		fileWriter.format("\tlw %s,%d($fp)\n", regOf(dst), off);
	}

	public void storeLocal(String varName, Temp src, String functionName) {
		int off = getLocalOffset(functionName, varName);
		fileWriter.format("\tsw %s,%d($fp)\n", regOf(src), off);
	}
	public void li(Temp t, int value)
	{
		int clamped = Math.max(INT_MIN, Math.min(INT_MAX, value));
		fileWriter.format("\tli %s,%d\n",regOf(t),clamped);
	}

	private void clampToInt16(String reg) {
		int id = labelCounter++;
		emit("\tli $a3," + INT_MAX + "\n");
		emit("\tble " + reg + ",$a3,__clamp_low_check_" + id + "\n");
		emit("\tmove " + reg + ",$a3\n");
		emit("\tj __clamp_end_" + id + "\n");
		emit("__clamp_low_check_" + id + ":\n");
		emit("\tli $a3," + INT_MIN + "\n");
		emit("\tbge " + reg + ",$a3,__clamp_end_" + id + "\n");
		emit("\tmove " + reg + ",$a3\n");
		emit("__clamp_end_" + id + ":\n");
	}

	public void add(Temp dst, Temp oprnd1, Temp oprnd2)
	{
		String rDst = regOf(dst);
		fileWriter.format("\tadd %s,%s,%s\n",rDst,regOf(oprnd1),regOf(oprnd2));
		clampToInt16(rDst);
	}

	public void sub(Temp dst, Temp oprnd1, Temp oprnd2)
	{
		String rDst = regOf(dst);
		fileWriter.format("\tsub %s,%s,%s\n",rDst,regOf(oprnd1),regOf(oprnd2));
		clampToInt16(rDst);
	}
	public void mul(Temp dst, Temp oprnd1, Temp oprnd2)
	{
		String rDst = regOf(dst);
		fileWriter.format("\tmul %s,%s,%s\n",rDst,regOf(oprnd1),regOf(oprnd2));
		clampToInt16(rDst);
	}

	public void div(Temp dst, Temp oprnd1, Temp oprnd2)
	{
		String rDst = regOf(dst);
		fileWriter.format("\tbeq %s,$zero,__div_by_zero\n", regOf(oprnd2));
		fileWriter.format("\tdiv %s,%s\n",regOf(oprnd1),regOf(oprnd2));
		fileWriter.format("\tmflo %s\n",rDst);
		clampToInt16(rDst);
	}

	public void newArray(Temp dst, Temp size)
	{
		String rSize = regOf(size);
		String rDst = regOf(dst);

		emit("\tmove $s0," + rSize + "\n");
		emit("\taddiu $a0," + rSize + ",1\n");
		emit("\tsll $a0,$a0,2\n");
		emit("\tli $v0,9\n");
		emit("\tsyscall\n");
		emit("\tmove " + rDst + ",$v0\n");
		emit("\tsw $s0,0(" + rDst + ")\n");
	}

	public void newClass(Temp dst, int fieldCount, int classId)
	{
		String rDst = regOf(dst);
		int bytes = Math.max(0, fieldCount) * 4;
		emit("\tli $a0," + (bytes + 4) + "\n");
		emit("\tli $v0,9\n");
		emit("\tsyscall\n");
		emit("\tmove " + rDst + ",$v0\n");
		emit("\tli $a3," + classId + "\n");
		emit("\tsw $a3,0(" + rDst + ")\n");
		emit("\taddiu " + rDst + "," + rDst + ",4\n");
		emit("\tmove $v1," + rDst + "\n");
		emit("\tli $a3," + bytes + "\n");
		emit("__newclass_zero_loop_" + labelCounter + ":\n");
		emit("\tbeq $a3,$zero,__newclass_zero_end_" + labelCounter + "\n");
		emit("\tsw $zero,0($v1)\n");
		emit("\taddiu $v1,$v1,4\n");
		emit("\taddiu $a3,$a3,-4\n");
		emit("\tj __newclass_zero_loop_" + labelCounter + "\n");
		emit("__newclass_zero_end_" + labelCounter + ":\n");
		labelCounter++;
	}

	public void fieldLoad(Temp dst, Temp object, int fieldOffsetWords)
	{
		String rDst = regOf(dst);
		String rObj = regOf(object);
		int off = fieldOffsetWords * 4;
		emit("\tbeq " + rObj + ",$zero,__invalid_ptr\n");
		emit("\tlw " + rDst + "," + off + "(" + rObj + ")\n");
	}

	public void fieldStore(Temp object, Temp value, int fieldOffsetWords)
	{
		String rObj = regOf(object);
		String rVal = regOf(value);
		int off = fieldOffsetWords * 4;
		emit("\tbeq " + rObj + ",$zero,__invalid_ptr\n");
		emit("\tsw " + rVal + "," + off + "(" + rObj + ")\n");
	}

	public void fieldStoreImm(Temp object, int immValue, int fieldOffsetWords)
	{
		String rObj = regOf(object);
		int off = fieldOffsetWords * 4;
		int clamped = Math.max(INT_MIN, Math.min(INT_MAX, immValue));
		emit("\tbeq " + rObj + ",$zero,__invalid_ptr\n");
		emit("\tli $a3," + clamped + "\n");
		emit("\tsw $a3," + off + "(" + rObj + ")\n");
	}

	public void arrayLoad(Temp dst, Temp array, Temp index)
	{
		String rDst = regOf(dst);
		String rArr = regOf(array);
		String rIdx = regOf(index);

		emit("\tbeq " + rArr + ",$zero,__invalid_ptr\n");
		emit("\tbltz " + rIdx + ",__access_violation\n");
		emit("\tlw $a3,0(" + rArr + ")\n");
		emit("\tbge " + rIdx + ",$a3,__access_violation\n");
		emit("\taddiu $v1," + rIdx + ",1\n");
		emit("\tsll $v1,$v1,2\n");
		emit("\taddu $v1," + rArr + ",$v1\n");
		emit("\tlw " + rDst + ",0($v1)\n");
	}

	public void arrayStore(Temp array, Temp index, Temp value)
	{
		String rArr = regOf(array);
		String rIdx = regOf(index);
		String rVal = regOf(value);

		emit("\tbeq " + rArr + ",$zero,__invalid_ptr\n");
		emit("\tbltz " + rIdx + ",__access_violation\n");
		emit("\tlw $a3,0(" + rArr + ")\n");
		emit("\tbge " + rIdx + ",$a3,__access_violation\n");
		emit("\taddiu $v1," + rIdx + ",1\n");
		emit("\tsll $v1,$v1,2\n");
		emit("\taddu $v1," + rArr + ",$v1\n");
		emit("\tsw " + rVal + ",0($v1)\n");
	}

	public void slt(Temp dst, Temp oprnd1, Temp oprnd2)
	{
		fileWriter.format("\tslt %s,%s,%s\n",regOf(dst),regOf(oprnd1),regOf(oprnd2));
	}

	public void seq(Temp dst, Temp oprnd1, Temp oprnd2)
	{
		fileWriter.format("\tseq %s,%s,%s\n",regOf(dst),regOf(oprnd1),regOf(oprnd2));
	}
	public void label(String inlabel)
	{
		fileWriter.format("%s:\n",inlabel);
	}	

	public void startFunction(String functionName) {
		currentFunction = functionName;
		Map<String, Integer> offsets = localOffsetsByFunction.get(functionName);
		int localBytes = (offsets == null) ? 0 : offsets.size() * 4;
		label(functionName);
		emit("\taddiu $sp,$sp,-" + FRAME_HEADER_SIZE + "\n");
		emit("\tsw $s0,0($sp)\n");
		emit("\tsw $s1,4($sp)\n");
		emit("\tsw $s2,8($sp)\n");
		emit("\tsw $s3,12($sp)\n");
		emit("\tsw $s4,16($sp)\n");
		emit("\tsw $s5,20($sp)\n");
		emit("\tsw $s6,24($sp)\n");
		emit("\tsw $s7,28($sp)\n");
		emit("\tsw $ra,32($sp)\n");
		emit("\tsw $fp,36($sp)\n");
		emit("\tmove $fp,$sp\n");
		if (localBytes > 0) {
			emit("\taddiu $sp,$sp,-" + localBytes + "\n");
		}
	}

	public void endFunction(String functionName) {
		label(functionName + "_epilogue");
		emit("\tmove $sp,$fp\n");
		emit("\tlw $s0,0($sp)\n");
		emit("\tlw $s1,4($sp)\n");
		emit("\tlw $s2,8($sp)\n");
		emit("\tlw $s3,12($sp)\n");
		emit("\tlw $s4,16($sp)\n");
		emit("\tlw $s5,20($sp)\n");
		emit("\tlw $s6,24($sp)\n");
		emit("\tlw $s7,28($sp)\n");
		emit("\tlw $ra,32($sp)\n");
		emit("\tlw $fp,36($sp)\n");
		emit("\taddiu $sp,$sp," + FRAME_HEADER_SIZE + "\n");
		if ("main".equals(functionName)) {
			emit("\tli $v0,10\n");
			emit("\tsyscall\n");
		} else {
			emit("\tjr $ra\n");
		}
		currentFunction = null;
	}

	public void call(String funcName, ir.TempList args, Temp dst) {
		// Preserve all temp-mapped registers across calls (caller-save for our temp model)
		for (String r : TEMP_REGS) {
			emit("\taddiu $sp,$sp,-4\n");
			emit("\tsw " + r + ",0($sp)\n");
		}

		int count = 0;
		int size = 0;
		for (ir.TempList it = args; it != null; it = it.tail) size++;
		Temp[] arr = new Temp[size];
		for (ir.TempList it = args; it != null; it = it.tail) arr[count++] = it.head;
		for (int i = size - 1; i >= 0; i--) {
			emit("\taddiu $sp,$sp,-4\n");
			emit("\tsw " + regOf(arr[i]) + ",0($sp)\n");
		}
		if (funcName != null && funcName.startsWith("__virtual__:")) {
			String[] parts = funcName.split(":", 3);
			String baseClass = (parts.length > 1) ? parts[1] : "";
			String methodName = (parts.length > 2) ? parts[2] : "";
			if (size <= 0 || arr[0] == null) {
				emit("\tj __invalid_ptr\n");
			} else {
				String rRecv = regOf(arr[0]);
				int vid = labelCounter++;
				emit("\tbeq " + rRecv + ",$zero,__invalid_ptr\n");
				emit("\tlw $a3,-4(" + rRecv + ")\n");

				List<String[]> targets = new ArrayList<>();
				for (TypeClass c : TypeClass.getAllClasses()) {
					if (!c.isDescendantOf(baseClass)) continue;
					String owner = c.findMethodOwnerClassName(methodName);
					if (owner == null) continue;
					targets.add(new String[] { Integer.toString(c.classId), owner + "_" + methodName });
				}

				for (int i = 0; i < targets.size(); i++) {
					emit("\tli $a2," + targets.get(i)[0] + "\n");
					emit("\tbeq $a3,$a2,__virt_call_" + vid + "_" + i + "\n");
				}

				emit("\tj __invalid_ptr\n");
				for (int i = 0; i < targets.size(); i++) {
					emit("__virt_call_" + vid + "_" + i + ":\n");
					emit("\tjal " + targets.get(i)[1] + "\n");
					emit("\tj __virt_call_end_" + vid + "\n");
				}
				emit("__virt_call_end_" + vid + ":\n");
			}
		} else {
			emit("\tjal " + funcName + "\n");
		}
		if (size > 0) {
			emit("\taddiu $sp,$sp," + (size * 4) + "\n");
		}

		// Restore preserved registers
		for (int i = TEMP_REGS.length - 1; i >= 0; i--) {
			emit("\tlw " + TEMP_REGS[i] + ",0($sp)\n");
			emit("\taddiu $sp,$sp,4\n");
		}

		if (dst != null) {
			emit("\tmove " + regOf(dst) + ",$v0\n");
		}
	}

	public void functionReturn(String functionName, Temp value) {
		if (value != null) {
			emit("\tmove $v0," + regOf(value) + "\n");
		}
		emit("\tj " + functionName + "_epilogue\n");
	}

	public void jump(String inlabel)
	{
		fileWriter.format("\tj %s\n",inlabel);
	}	
	public void blt(Temp oprnd1, Temp oprnd2, String label)
	{
		int i1 =oprnd1.getSerialNumber();
		int i2 =oprnd2.getSerialNumber();
		
		fileWriter.format("\tblt Temp_%d,Temp_%d,%s\n",i1,i2,label);				
	}
	public void bge(Temp oprnd1, Temp oprnd2, String label)
	{
		int i1 =oprnd1.getSerialNumber();
		int i2 =oprnd2.getSerialNumber();
		
		fileWriter.format("\tbge Temp_%d,Temp_%d,%s\n",i1,i2,label);				
	}
	public void bne(Temp oprnd1, Temp oprnd2, String label)
	{
		int i1 =oprnd1.getSerialNumber();
		int i2 =oprnd2.getSerialNumber();
		
		fileWriter.format("\tbne Temp_%d,Temp_%d,%s\n",i1,i2,label);				
	}
	public void beq(Temp oprnd1, Temp oprnd2, String label)
	{
		int i1 =oprnd1.getSerialNumber();
		int i2 =oprnd2.getSerialNumber();
		
		fileWriter.format("\tbeq Temp_%d,Temp_%d,%s\n",i1,i2,label);				
	}
	public void beqz(Temp oprnd1, String label)
	{
		fileWriter.format("\tbeq %s,$zero,%s\n",regOf(oprnd1),label);				
	}
	
	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static MipsGenerator instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	protected MipsGenerator() {}

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static MipsGenerator getInstance()
	{
		if (instance == null)
		{
			/*******************************/
			/* [0] The instance itself ... */
			/*******************************/
			instance = new MipsGenerator();

			try
			{
				/*********************************************************************************/
				/* [1] Open the MIPS text file and write data section with error message strings */
				/*********************************************************************************/
				String target = (outputPath != null) ? outputPath : "./output/MIPS.txt";

				/***************************************/
				/* [2] Open MIPS text file for writing */
				/***************************************/
				instance.fileWriter = new PrintWriter(target);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			/*****************************************************/
			/* [3] Print data section with error message strings */
			/*****************************************************/
			instance.fileWriter.print(".data\n");
			instance.fileWriter.print("string_access_violation: .asciiz \"Access Violation\"\n");
			instance.fileWriter.print("string_illegal_div_by_0: .asciiz \"Illegal Division By Zero\"\n");
			instance.fileWriter.print("string_invalid_ptr_dref: .asciiz \"Invalid Pointer Dereference\"\n");
			instance.fileWriter.print(".text\n");
			instance.fileWriter.print("__access_violation:\n");
			instance.fileWriter.print("\tla $a0,string_access_violation\n");
			instance.fileWriter.print("\tli $v0,4\n");
			instance.fileWriter.print("\tsyscall\n");
			instance.fileWriter.print("\tli $v0,10\n");
			instance.fileWriter.print("\tsyscall\n");
			instance.fileWriter.print("__invalid_ptr:\n");
			instance.fileWriter.print("\tla $a0,string_invalid_ptr_dref\n");
			instance.fileWriter.print("\tli $v0,4\n");
			instance.fileWriter.print("\tsyscall\n");
			instance.fileWriter.print("\tli $v0,10\n");
			instance.fileWriter.print("\tsyscall\n");
			instance.fileWriter.print("__div_by_zero:\n");
			instance.fileWriter.print("\tla $a0,string_illegal_div_by_0\n");
			instance.fileWriter.print("\tli $v0,4\n");
			instance.fileWriter.print("\tsyscall\n");
			instance.fileWriter.print("\tli $v0,10\n");
			instance.fileWriter.print("\tsyscall\n");
		}
		return instance;
	}

	public static void setOutputFile(String path) {
		outputPath = path;
	}
}
