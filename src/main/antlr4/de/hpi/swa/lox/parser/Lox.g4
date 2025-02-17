grammar Lox;

// Lox Grammar adapted from https://craftinginterpreters.com/appendix-i.html

@parser::header {
// DO NOT MODIFY - generated from Lox.g4
}

@lexer::header {
// DO NOT MODIFY - generated from Lox.g4
}

program: declaration* EOF;

declaration: varDecl | statement | funDecl;

statement:
	exprStmt
	| forStmt
	| ifStmt
	| whileStmt
	| printStmt
	| block
	| breakStmt
	| continueStmt
	| postfixConditionStmt
	| forOfStmt
	| forInStmt
	| returnStmt;

returnStmt: 'return' expression? ';';
forOfStmt:
	'for' '(' 'var' each = IDENTIFIER 'of' arr = expression ')' body = statement;
forInStmt:
	'for' '(' 'var' each = IDENTIFIER 'in' arr = expression ')' body = statement;
postfixConditionStmt:
	then = statementBody keyword = ('if' | 'unless') '(' condition = expression ')' ';';
statementBody: expression | 'print' exp = expression;

forStmt:
	'for' '(' (varDecl | exprStmt | ';') condition = expression? ';' increment = expression? ')'
		body = statement;
ifStmt:
	'if' '(' condition = expression ')' then = statement (
		'else' alt = statement
	)?;
whileStmt:
	'while' '(' condition = expression ')' body = statement;

varDecl:
	'var' IDENTIFIER ('=' expression)? ';'
	| IDENTIFIER ':=' expression ';';
funDecl: 'fun' function ';'?;

exprStmt: expression ';';
printStmt: 'print' expression ';';
breakStmt: 'break' ';';
continueStmt: 'continue' ';';

block: '{' declaration* '}';

expression: assignment;

assignment: IDENTIFIER '=' assignment | arrAssignment;
arrAssignment:
	left = variableExpr '[' index = expression ']' '=' right = assignment
	| other = logic_or;

logic_or: logic_and ( 'or' logic_and)*;
logic_and: equality ( 'and' equality)*;
equality: comparison ( ( '!=' | '==') comparison)*;
comparison: term ( ( '>' | '>=' | '<' | '<=') term)*;
term: factor ( ( '-' | '+') factor)*;
factor: unary ( ( '/' | '*' | '%') unary)*;

unary: ( '!' | '-') unary | call;
call: primary callArguments*;
callArguments: '(' arguments? ')';
arguments: expression ( ',' expression)*;
function: IDENTIFIER '(' parameters? ')' block;
parameters: IDENTIFIER ( ',' IDENTIFIER)*;

primary:
	number
	| string
	| true
	| false
	| nil
	| variableExpr
	| '(' expression ')'
	| array
	| arrayExpr;
arrayExpr: left = variableExpr '[' index = expression ']';
array: '[' (expression (',' expression)*)? ']';

variableExpr: IDENTIFIER;

string: STRING;
nil: 'nil';
true: 'true';
false: 'false';
number: NUMBER;

IF: 'if';
UNLESS: 'unless';
NUMBER: DIGIT+ ( '.' DIGIT+)?;
STRING: '"' (~["\\])* '"';
IDENTIFIER: ALPHA ( ALPHA | DIGIT)*;
fragment ALPHA: [a-zA-Z_];
fragment DIGIT: [0-9];

// more...
WS: [ \t\r\n]+ -> skip;
LINE_COMMENT: '//' ~[\r\n]* -> skip;
MULTI_LINE_COMMENT: '/*' .*? '*/' -> skip;
// Local Variables: eval: (add-hook 'after-save-hook (lambda () (if (fboundp 'lsp-workspace-root)
// (if-let ((workspace (car (gethash (lsp-workspace-root) (lsp-session-folder->servers
// (lsp-session)))))) (with-lsp-workspace workspace (lsp-notify "workspace/didChangeWatchedFiles"
// `((changes . [((type . ,(alist-get 'changed lsp--file-change-type)) (uri . ,(lsp--path-to-uri
// buffer-file-name)))]))))))) nil t) End: