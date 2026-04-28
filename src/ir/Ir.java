/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import mips.MipsGenerator;

public class Ir {
	private IrCommand head = null;
	private IrCommandList tail = null;
	private final Map<String, Map<String, Integer>> paramIndexByFunction = new HashMap<>();
	private final Set<String> globalVars = new HashSet<>();
	private boolean registerAllocationFailed = false;

	/******************/
	/* Add Ir command */
	/******************/
	public void AddIrCommand(IrCommand cmd) {
		if ((head == null) && (tail == null)) {
			this.head = cmd;
		} else if ((head != null) && (tail == null)) {
			this.tail = new IrCommandList(cmd, null);
		} else {
			IrCommandList it = tail;
			while ((it != null) && (it.tail != null)) {
				it = it.tail;
			}
			it.tail = new IrCommandList(cmd, null);
		}
	}

	public List<IrCommand> getCommandList() {
		List<IrCommand> cmds = new ArrayList<>();
		if (head != null) {
			cmds.add(head);
		}
		IrCommandList it = tail;
		while (it != null) {
			cmds.add(it.head);
			it = it.tail;
		}
		return cmds;
	}

	public void registerParam(String functionName, String name, int index) {
		Map<String, Integer> fnParams = paramIndexByFunction.get(functionName);
		if (fnParams == null) {
			fnParams = new HashMap<>();
			paramIndexByFunction.put(functionName, fnParams);
		}
		fnParams.put(name, index);
	}

	public boolean isParam(String functionName, String name) {
		Map<String, Integer> fnParams = paramIndexByFunction.get(functionName);
		return fnParams != null && fnParams.containsKey(name);
	}

	public int getParamIndex(String functionName, String name) {
		Map<String, Integer> fnParams = paramIndexByFunction.get(functionName);
		Integer idx = (fnParams == null) ? null : fnParams.get(name);
		if (idx == null) {
			throw new RuntimeException("Not a function parameter: " + functionName + ":" + name);
		}
		return idx;
	}

	public void registerGlobal(String varName) {
		globalVars.add(varName);
	}

	public boolean isGlobal(String varName) {
		return globalVars.contains(varName);
	}

	public void mipsMe() {
		if (registerAllocationFailed) {
			throw new RuntimeException("Register Allocation Failed");
		}
		MipsGenerator mg = MipsGenerator.getInstance();

		Cfg cfg = Cfg.buildFromIr();
		Map<Integer, Integer> allocation = RegisterAllocator.allocate(cfg);
		if (allocation == null) {
			throw new RuntimeException("Register Allocation Failed");
		}
		mg.setTempRegisterAllocation(allocation);

		List<IrCommand> cmds = getCommandList();
		Map<String, Set<String>> localVarsByFunction = new HashMap<>();
		String scanFunction = null;
		for (IrCommand c : cmds) {
			if (c instanceof IrCommandLabel && ((IrCommandLabel) c).isFunctionEntry) {
				scanFunction = ((IrCommandLabel) c).labelName;
				localVarsByFunction.put(scanFunction, new HashSet<>());
				continue;
			}
			if (scanFunction == null) continue;
			if (c instanceof IrCommandLoad) {
				IrCommandLoad l = (IrCommandLoad) c;
				if (!isParam(scanFunction, l.varName) && !isGlobal(l.uniqueVarId)) {
					localVarsByFunction.get(scanFunction).add(l.uniqueVarId);
				}
			} else if (c instanceof IrCommandStore) {
				IrCommandStore s = (IrCommandStore) c;
				if (!isParam(scanFunction, s.varName) && !isGlobal(s.uniqueVarId)) {
					localVarsByFunction.get(scanFunction).add(s.uniqueVarId);
				}
			}
		}
		for (Map.Entry<String, Set<String>> e : localVarsByFunction.entrySet()) {
			mg.setFunctionLocals(e.getKey(), e.getValue());
		}
		int firstFunctionIdx = cmds.size();
		for (int i = 0; i < cmds.size(); i++) {
			IrCommand c = cmds.get(i);
			if (c instanceof IrCommandLabel && ((IrCommandLabel) c).isFunctionEntry) {
				firstFunctionIdx = i;
				break;
			}
		}

		List<IrCommand> globalInitCommands = new ArrayList<>();
		for (int i = 0; i < firstFunctionIdx; i++) {
			IrCommand c = cmds.get(i);
			if (!(c instanceof IrCommandLabel)) {
				globalInitCommands.add(c);
			}
		}

		mg.emit(".text\n");
		mg.emit("j main\n");

		String currentFunction = null;

		for (int i = firstFunctionIdx; i < cmds.size(); i++) {
			IrCommand c = cmds.get(i);
			if (c instanceof IrCommandLabel) {
				IrCommandLabel lbl = (IrCommandLabel) c;
				if (lbl.isFunctionEntry) {
					if (currentFunction != null) {
						mg.endFunction(currentFunction);
					}
					currentFunction = lbl.labelName;
					mg.startFunction(currentFunction);
					if ("main".equals(currentFunction)) {
						for (IrCommand initCmd : globalInitCommands) {
							emitNonLabelCommand(initCmd, mg, currentFunction);
						}
					}
					continue;
				}
				mg.label(lbl.labelName);
				continue;
			}
			emitNonLabelCommand(c, mg, currentFunction);
		}

		if (currentFunction != null) {
			mg.endFunction(currentFunction);
		}
	}

	private void emitNonLabelCommand(IrCommand c, MipsGenerator mg, String currentFunction) {
		if (c instanceof IRcommandConstInt) {
			IRcommandConstInt k = (IRcommandConstInt) c;
			mg.li(k.t, k.value);
		} else if (c instanceof IrCommandConstString) {
			IrCommandConstString s = (IrCommandConstString) c;
			mg.loadStringConst(s.dst, s.value);
		} else if (c instanceof IrCommandAllocate) {
			IrCommandAllocate a = (IrCommandAllocate) c;
			registerGlobal(a.varName);
			mg.allocate(a.varName);
		} else if (c instanceof IrCommandLoad) {
			IrCommandLoad l = (IrCommandLoad) c;
			if (currentFunction != null && isParam(currentFunction, l.varName)) {
				mg.loadParam(l.dst, getParamIndex(currentFunction, l.varName));
			} else if (currentFunction != null && !isGlobal(l.uniqueVarId)) {
				mg.loadLocal(l.dst, l.uniqueVarId, currentFunction);
			} else {
				mg.load(l.dst, l.uniqueVarId);
			}
		} else if (c instanceof IrCommandStore) {
			IrCommandStore s = (IrCommandStore) c;
			if (currentFunction != null && isParam(currentFunction, s.varName)) {
				mg.storeParam(getParamIndex(currentFunction, s.varName), s.src);
			} else if (currentFunction != null && !isGlobal(s.uniqueVarId)) {
				mg.storeLocal(s.uniqueVarId, s.src, currentFunction);
			} else {
				mg.store(s.uniqueVarId, s.src);
			}
		} else if (c instanceof IrCommandBinopAddIntegers) {
			IrCommandBinopAddIntegers b = (IrCommandBinopAddIntegers) c;
			mg.add(b.dst, b.t1, b.t2);
		} else if (c instanceof IrCommandBinopSubIntegers) {
			IrCommandBinopSubIntegers b = (IrCommandBinopSubIntegers) c;
			mg.sub(b.dst, b.t1, b.t2);
		} else if (c instanceof IrCommandBinopMulIntegers) {
			IrCommandBinopMulIntegers b = (IrCommandBinopMulIntegers) c;
			mg.mul(b.dst, b.t1, b.t2);
		} else if (c instanceof IrCommandBinopDivIntegers) {
			IrCommandBinopDivIntegers b = (IrCommandBinopDivIntegers) c;
			mg.div(b.dst, b.t1, b.t2);
		} else if (c instanceof IrCommandBinopLtIntegers) {
			IrCommandBinopLtIntegers b = (IrCommandBinopLtIntegers) c;
			mg.slt(b.dst, b.t1, b.t2);
		} else if (c instanceof IrCommandBinopEqIntegers) {
			IrCommandBinopEqIntegers b = (IrCommandBinopEqIntegers) c;
			mg.seq(b.dst, b.t1, b.t2);
		} else if (c instanceof IrCommandBinopEqStrings) {
			IrCommandBinopEqStrings b = (IrCommandBinopEqStrings) c;
			mg.stringEquals(b.dst, b.t1, b.t2);
		} else if (c instanceof IrCommandJumpIfEqToZero) {
			IrCommandJumpIfEqToZero j = (IrCommandJumpIfEqToZero) c;
			mg.beqz(j.t, j.labelName);
		} else if (c instanceof IrCommandJumpLabel) {
			IrCommandJumpLabel j = (IrCommandJumpLabel) c;
			mg.jump(j.labelName);
		} else if (c instanceof IrCommandPrintInt) {
			IrCommandPrintInt p = (IrCommandPrintInt) c;
			mg.printInt(p.t);
		} else if (c instanceof IrCommandPrintString) {
			IrCommandPrintString p = (IrCommandPrintString) c;
			mg.printString(p.t);
		} else if (c instanceof IrCommandStringConcat) {
			IrCommandStringConcat sc = (IrCommandStringConcat) c;
			mg.stringConcat(sc.dst, sc.left, sc.right);
		} else if (c instanceof IrCommandCall) {
			IrCommandCall call = (IrCommandCall) c;
			mg.call(call.funcName, call.args, call.dst);
		} else if (c instanceof IrCommandNewArray) {
			IrCommandNewArray a = (IrCommandNewArray) c;
			mg.newArray(a.dst, a.size);
		} else if (c instanceof IrCommandArrayLoad) {
			IrCommandArrayLoad a = (IrCommandArrayLoad) c;
			mg.arrayLoad(a.dst, a.array, a.index);
		} else if (c instanceof IrCommandArrayStore) {
			IrCommandArrayStore a = (IrCommandArrayStore) c;
			mg.arrayStore(a.array, a.index, a.value);
		} else if (c instanceof IrCommandNewClass) {
			IrCommandNewClass n = (IrCommandNewClass) c;
			mg.newClass(n.dst, n.fieldCount, n.classId);
		} else if (c instanceof IrCommandFieldLoad) {
			IrCommandFieldLoad f = (IrCommandFieldLoad) c;
			mg.fieldLoad(f.dst, f.object, f.fieldOffsetWords);
		} else if (c instanceof IrCommandFieldStore) {
			IrCommandFieldStore f = (IrCommandFieldStore) c;
			mg.fieldStore(f.object, f.value, f.fieldOffsetWords);
		} else if (c instanceof IrCommandFieldStoreImm) {
			IrCommandFieldStoreImm f = (IrCommandFieldStoreImm) c;
			mg.fieldStoreImm(f.object, f.immValue, f.fieldOffsetWords);
		} else if (c instanceof IrCommandReturn) {
			IrCommandReturn ret = (IrCommandReturn) c;
			if (currentFunction != null) {
				mg.functionReturn(currentFunction, ret.value);
			}
		}
	}

	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static Ir instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	protected Ir() {
	}

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static Ir getInstance() {
		if (instance == null) {
			/*******************************/
			/* [0] The instance itself ... */
			/*******************************/
			instance = new Ir();
		}
		return instance;
	}

	/* Accessors for external analysis (CFG builder) */
	public IrCommand getHeadCommand() {
		return this.head;
	}

	public IrCommandList getTailList() {
		return this.tail;
	}
}
