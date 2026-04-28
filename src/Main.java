import java.io.*;
import java_cup.runtime.Symbol;
import ast.*;
import ir.*;
import mips.*;
import types.Type;
import types.TypeError;

public class Main
{
	private static void writeSingleLine(String outputFileName, String text)
	{
		try (PrintWriter w = new PrintWriter(outputFileName)) {
			w.print(text);
		}
		catch (Exception ignored) {}
	}

	private static int extractFirstInt(String s, int fallback)
	{
		if (s == null) return fallback;
		StringBuilder digits = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (Character.isDigit(c)) {
				digits.append(c);
			} else if (digits.length() > 0) {
				break;
			}
		}
		if (digits.length() == 0) return fallback;
		try {
			return Integer.parseInt(digits.toString());
		} catch (Exception e) {
			return fallback;
		}
	}

	static public void main(String argv[])
	{
		Lexer l;
		Parser p;
		Symbol s;
		AstProgram ast;
		FileReader fileReader;
		if (argv.length < 2) {
			return;
		}
		String inputFileName = argv[0];
		String outputFileName = argv[1];

		try
		{
			/********************************/
			/* [1] Initialize a file reader */
			/********************************/
			fileReader = new FileReader(inputFileName);

			/******************************/
			/* [3] Initialize a new lexer */
			/******************************/
			l = new Lexer(fileReader);

			/*******************************/
			/* [4] Initialize a new parser */
			/*******************************/
			p = new Parser(l, outputFileName);

			/***********************************/
			/* [5] 3 ... 2 ... 1 ... Parse !!! */
			/***********************************/
			ast = (AstProgram) p.parse().value;

			/*************************/
			/* [6] Print the AST ... */
			/*************************/
			// Disabled for performance on large tests.

			/**************************/
			/* [7] Semant the AST ... */
			/**************************/
			Type semantResult = ast.semantMe();
			if (semantResult == null) {
				writeSingleLine(outputFileName, "ERROR(0)");
				return;
			}
			if (semantResult.isError()) {
				int line = (semantResult instanceof TypeError) ? ((TypeError) semantResult).line : 0;
				writeSingleLine(outputFileName, String.format("ERROR(%d)", line+1));
				return;
			}

			/**********************/
			/* [8] Ir the AST ... */
			/**********************/
			ast.irMe();

			/***********************/
			/* [9] MIPS the Ir ... */
			/***********************/
			MipsGenerator.setOutputFile(outputFileName);
			Ir.getInstance().mipsMe();

			/**************************************/
			/* [10] Finalize AST GRAPHIZ DOT file */
			/**************************************/
			AstGraphviz.getInstance().finalizeFile();

			/***************************/
			/* [11] Finalize MIPS file */
			/***************************/
			MipsGenerator.getInstance().finalizeFile();
		}
		catch (SecurityException e)
		{
			// System.exit() from parser's lexical error path; file already contains "ERROR"
			return;
		}
		catch (RuntimeException e)
		{
			String msg = e.getMessage();
			if (msg != null && msg.contains("Register Allocation Failed")) {
				writeSingleLine(outputFileName, "Register Allocation Failed");
				return;
			}
			if (msg != null && msg.contains("Parse error at line")) {
				int line = extractFirstInt(msg, 0);
				writeSingleLine(outputFileName, String.format("ERROR(%d)", line));
				return;
			}
			writeSingleLine(outputFileName, "ERROR");
		}

		catch (Exception e)
		{
			writeSingleLine(outputFileName, "ERROR");
		}
	}
}