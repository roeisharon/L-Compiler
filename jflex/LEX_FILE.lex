/***************************/
/* FILE NAME: LEX_FILE.lex */
/***************************/

/*************/
/* USER CODE */
/*************/
import java_cup.runtime.*;

/******************************/
/* DOLAR DOLAR - DON'T TOUCH! */
/******************************/

%%

/************************************/
/* OPTIONS AND DECLARATIONS SECTION */
/************************************/
   
/*****************************************************/ 
/* Lexer is the name of the class JFlex will create. */
/* The code will be written to the file Lexer.java.  */
/*****************************************************/ 
%class Lexer

/********************************************************************/
/* The current line number can be accessed with the variable yyline */
/* and the current column number with the variable yycolumn.        */
/********************************************************************/
%line
%column

/*******************************************************************************/
/* Note that this has to be the EXACT same name of the class the CUP generates */
/*******************************************************************************/
%cupsym TokenNames

/******************************************************************/
/* CUP compatibility mode interfaces with a CUP generated parser. */
/******************************************************************/
%cup
%state COMMENT2_S

/****************/
/* DECLARATIONS */
/****************/
/*****************************************************************************/   
/* Code between %{ and %}, both of which must be at the beginning of a line, */
/* will be copied verbatim (letter to letter) into the Lexer class code.     */
/* Here you declare member variables and functions that are used inside the  */
/* scanner actions.                                                          */  
/*****************************************************************************/   
%{
	/*********************************************************************************/
	/* Create a new java_cup.runtime.Symbol with information about the current token */
	/*********************************************************************************/
	private Symbol symbol(int type)               {return new Symbol(type, yyline, yycolumn);}
	private Symbol symbol(int type, Object value) {return new Symbol(type, yyline, yycolumn, value);}

	/*******************************************/
	/* Enable line number extraction from main */
	/*******************************************/
	public int getLine() { return yyline + 1; } 

	/**********************************************/
	/* Enable token position extraction from main */
	/**********************************************/
	public int getTokenStartPosition() { return yycolumn + 1; } 
%}

/***********************/
/* MACRO DECALARATIONS */
/***********************/
DIGIT                = [0-9]
LETTER               = [A-Za-z]
ID				     = {LETTER}({LETTER} | {DIGIT})*
LINE_TERMINATOR      = \r\n|\n|\r
WHITE_SPACES 	     = ({LINE_TERMINATOR} | [ \t])+


/* ===========
   Punctuation
   =========== */
LPAREN               = \(
RPAREN               = \)
LBRACK               = \[
RBRACK               = \]
LBRACE               = \{
RBRACE               = \}
COMMA                = ,
DOT                  = \.
SEMICOLON            = ;
ASSIGN               = :=
EQ                   = =
LT                   = <
GT                   = >

/* ==========
   Operators
   ========== */
PLUS                 = \+
MINUS                = \-
TIMES                = \*
DIVIDE               = \/

/* =========
   Keywords
   ========= */
NIL                  = nil
TYPE_INT             = int
TYPE_VOID            = void
TYPE_STRING          = string
ARRAY                = array
CLASS                = class
EXTENDS              = extends
RETURN               = return
WHILE                = while
IF                   = if
NEW                  = new
ELSE				 = else

/* =========
   Literals
   ========= */
   
INT 			= 0						 // Matches the number 0 
				| [1-9]{DIGIT}{0,3}      // Matches numbers from 1 to 9999
    			| [1-2]{DIGIT}{4}         // Matches numbers from 10000 to 19999 or 20000 to 29999
    			| 3[0-1]{DIGIT}{3}        // Matches numbers from 30000 to 31999
    			| 32[0-6]{DIGIT}{2}       // Matches numbers from 32000 to 32699
    			| 327[0-5]{DIGIT}         // Matches numbers from 32700 to 32759
    			| 3276[0-7]        // Matches numbers from 32760 to 32767
    			
STRING               = \"{LETTER}*\"

/* =========
   Comments
   ========= */

COMMENT_CHARS = [A-Za-z0-9\(\)\{\}\[\]!?+\-*/;\. \t]


COMMENT_INVALID_CHARS = [^{COMMENT_CHARS}\r\n]

TYPE_1_COMMENT = \/\/{COMMENT_CHARS}*({LINE_TERMINATOR})

TYPE_1_INVALID = \/\/{COMMENT_CHARS}*{COMMENT_INVALID_CHARS}.*({LINE_TERMINATOR})


TYPE_2_OPEN = \/\*

COMMENT1 = {TYPE_1_COMMENT} 
INVALID_COMMENT1 = {TYPE_1_INVALID} 

NUMBER= {DIGIT}+

/******************************/
/* DOLAR DOLAR - DON'T TOUCH! */
/******************************/

%%

/************************************************************/
/* LEXER matches regular expressions to actions (Java code) */
/************************************************************/

/**************************************************************/
/* YYINITIAL is the state at which the lexer begins scanning. */
/* So these regular expressions will only be matched if the   */
/* scanner is in the start state YYINITIAL.                   */
/**************************************************************/

<YYINITIAL> {

{LPAREN}           { return symbol(TokenNames.LPAREN); }
{RPAREN}           { return symbol(TokenNames.RPAREN); }
{LBRACK}           { return symbol(TokenNames.LBRACK); }
{RBRACK}           { return symbol(TokenNames.RBRACK); }
{LBRACE}           { return symbol(TokenNames.LBRACE); }
{RBRACE}           { return symbol(TokenNames.RBRACE); }
{NIL}              { return symbol(TokenNames.NIL); }
{PLUS}             { return symbol(TokenNames.PLUS); }
{MINUS}            { return symbol(TokenNames.MINUS); }
{TIMES}            { return symbol(TokenNames.TIMES); }
{DIVIDE}           { return symbol(TokenNames.DIVIDE); }
{COMMA}            { return symbol(TokenNames.COMMA); }
{DOT}              { return symbol(TokenNames.DOT); }
{SEMICOLON}        { return symbol(TokenNames.SEMICOLON); }
{TYPE_INT}         { return symbol(TokenNames.TYPE_INT); }
{TYPE_VOID}        { return symbol(TokenNames.TYPE_VOID); }
{ASSIGN}           { return symbol(TokenNames.ASSIGN); }
{EQ}               { return symbol(TokenNames.EQ); }
{LT}               { return symbol(TokenNames.LT); }
{GT}               { return symbol(TokenNames.GT); }
{ARRAY}            { return symbol(TokenNames.ARRAY); }
{CLASS}            { return symbol(TokenNames.CLASS); }
{EXTENDS}          { return symbol(TokenNames.EXTENDS); }
{RETURN}           { return symbol(TokenNames.RETURN); }
{WHILE}            { return symbol(TokenNames.WHILE); }
{ELSE}             {return symbol(TokenNames.ELSE);}
{IF}               { return symbol(TokenNames.IF); }
{NEW}              { return symbol(TokenNames.NEW); }
{TYPE_STRING}      { return symbol(TokenNames.TYPE_STRING); }
{INT}              { return symbol(TokenNames.INT, Integer.valueOf(yytext())); }
{ID}               { return symbol(TokenNames.ID, new String(yytext())); }
{STRING}           { return symbol(TokenNames.STRING, new String(yytext())); }
{WHITE_SPACES}     { /* just skip what was found, do nothing */ }
{INVALID_COMMENT1}  { return symbol(TokenNames.ERROR); }
{COMMENT1}          { /* just skip what was found, do nothing */ }
{TYPE_2_OPEN}      {yybegin(COMMENT2_S);}
{NUMBER}           { return symbol(TokenNames.ERROR); }
.				   { return symbol(TokenNames.ERROR); }
<<EOF>>			   { return symbol(TokenNames.EOF); }
}

<COMMENT2_S>{
    [A-Za-z0-9\s\(\)\{\}\[\]\?!+\-*/\.;]   { /* skip legal chars */ }
    "*"                                     { /* stay in comment, check next char manually */ }
    "*" "/"                                    { yybegin(YYINITIAL); /* comment closed */ }
    \n                                      { /* handle newlines */ }
	.                                       { return symbol(TokenNames.ERROR); }
	<<EOF>>			   						{ return symbol(TokenNames.ERROR); }
}