package ast;

import java.io.File;
import java.io.PrintWriter;

public class AstGraphviz
{
	/***********************/
	/* The file writer ... */
	/***********************/
	private PrintWriter fileWriter;
	
	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static AstGraphviz instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	private AstGraphviz() {}

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static AstGraphviz getInstance()
	{
		if (instance == null)
    {
        instance = new AstGraphviz();

        try
        {
            File outDir = new File("output");
            outDir.mkdirs(); // ← creates output/ in *current working dir*

            instance.fileWriter =
                new PrintWriter(new File(outDir, "AST_IN_GRAPHVIZ_DOT_FORMAT.txt"));

            instance.fileWriter.println("digraph");
            instance.fileWriter.println("{");
            instance.fileWriter.println("graph [ordering = \"out\"]");
        }
        catch (Exception e)
        {
            // Graphviz is optional – never crash SEMANT
            instance.fileWriter = null;
        }
    }
    return instance;
	}

	/***********************************/
	/* Log node in graphviz dot format */
	/***********************************/
	public void logNode(int sn, String name)
{
    if (fileWriter == null) return;
    fileWriter.format("v%d [label=\"%s\"];\n", sn, name);
}

	/***********************************/
	/* Log edge in graphviz dot format */
	/***********************************/
	public void logEdge(
		int fatherNodeSerialNumber,
		int sonNodeSerialNumber)
	{
		if (fileWriter == null) return;
		fileWriter.format(
			"v%d -> v%d;\n",
			fatherNodeSerialNumber,
			sonNodeSerialNumber);
	}
	
	/******************************/
	/* Finalize graphviz dot file */
	/******************************/
	public void finalizeFile()
	{
		if (fileWriter == null) return;
		fileWriter.print("}\n");
		fileWriter.close();
	}
}
