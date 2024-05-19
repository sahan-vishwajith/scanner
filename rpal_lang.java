import csem.*;
import parser.AST;
import parser.Parser;
import scanner.*;

import java.io.IOException;

public class rpal_lang {
    public static void main(String[] args) throws Exception {
        // Check if a file name was provided as a command-line argument
        String fileName = null;
        if (args.length == 1) {
            fileName = args[0];
        }

        // If no file name was provided, print the usage message and return
        if (fileName == null) {
            System.out.println("Input format: java rpal20 <filename>");
            return;
        }

        // Initialize the AST (Abstract Syntax Tree) variable
        AST ast = null;

        try {
            // Create a scanner to read the input file
            Scanner scanner = new Scanner(fileName);

            // Create a parser to build the AST from the scanned input
            Parser parser = new Parser(scanner);
            ast = parser.buildAST();
        } catch (IOException e) {
            // Handle any I/O exceptions that may occur during scanning or parsing
            e.printStackTrace();
        }

        // Standardize the AST
        ast.standardize();

        // Create a CSE (Concurrent State Evaluation) machine and evaluate the program
        CSEMachine csem = new CSEMachine(ast);
        csem.evaluateProgram();

        // Print a newline at the end of the program output
        System.out.println();
    }
}
