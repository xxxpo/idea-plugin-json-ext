package com.xxxlin.json;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.xxxlin.json.JsonElementTypes.*;

%%

%{
  public JsonFlexLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class JsonFlexLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=\R
WHITE_SPACE=\s+

LINE_COMMENT1="//".*
LINE_COMMENT2="#".*
BLOCK_COMMENT="/"\*([^*]|\*+[^*/])*(\*+"/")?
DOUBLE_QUOTED_STRING=\"([^\\\"\r\n]|\\[^\r\n])*\"?
SINGLE_QUOTED_STRING='([^\\'\r\n]|\\[^\r\n])*'?
NUMBER=(-?(0|[1-9][0-9]*)(\.[0-9]+)?([eE][+-]?[0-9]*)?)|Infinity|-Infinity|NaN
IDENTIFIER=[[:jletterdigit:]~!()*\-."/"@\^<>=]+

%%
<YYINITIAL> {
  {WHITE_SPACE}               { return WHITE_SPACE; }

  "{"                         { return L_CURLY; }
  "}"                         { return R_CURLY; }
  "["                         { return L_BRACKET; }
  "]"                         { return R_BRACKET; }
  ","                         { return COMMA; }
  ":"                         { return COLON; }
  "true"                      { return TRUE; }
  "false"                     { return FALSE; }
  "null"                      { return NULL; }

  {LINE_COMMENT1}              { return LINE_COMMENT1; }
  {LINE_COMMENT2}              { return LINE_COMMENT2; }
  {BLOCK_COMMENT}             { return BLOCK_COMMENT; }
  {DOUBLE_QUOTED_STRING}      { return DOUBLE_QUOTED_STRING; }
  {SINGLE_QUOTED_STRING}      { return SINGLE_QUOTED_STRING; }
  {NUMBER}                    { return NUMBER; }
  {IDENTIFIER}                { return IDENTIFIER; }

}

[^] { return BAD_CHARACTER; }
