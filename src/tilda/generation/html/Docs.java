/* ===========================================================================
 * Copyright (C) 2015 CapsicoHealth Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tilda.generation.html;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;

import com.sun.istack.internal.NotNull;

import tilda.enums.ColumnMode;
import tilda.enums.FrameworkSourcedType;
import tilda.generation.GeneratorSession;
import tilda.generation.java8.Helper;
import tilda.generation.java8.JavaJDBCType;
import tilda.parsing.ParserSession;
import tilda.parsing.parts.Column;
import tilda.parsing.parts.ColumnValue;
import tilda.parsing.parts.ForeignKey;
import tilda.parsing.parts.Formula;
import tilda.parsing.parts.Object;
import tilda.parsing.parts.Value;
import tilda.parsing.parts.View;
import tilda.parsing.parts.ViewColumn;
import tilda.utils.SystemValues;
import tilda.utils.TextUtil;

public class Docs
  {
	
	public static void DataClassDocs(PrintWriter Out, GeneratorSession G, Object O)
    throws Exception
      {
        if ( O.getShortName().equalsIgnoreCase("DATAMART2.SCREENING"))
          System.out.println("xxx");
        
        View view = O._ParentSchema.getView(O._Name);
        
        Out.println("<DIV>");
        Out.println("<DIV id='" + O._Name + "_DIV' class='tables'>");
        Out.println("<H1>" + O._Name + "&nbsp;&nbsp;&nbsp;&nbsp;<SUP style=\"font-size: 60%;\"><A href=\"#\">top</A></SUP></H1>");
        Out.println("</DIV>");
        Out.println("The generated " + Helper.getCodeGenLanguage() + "/" + G.getSql().getName() + " Tilda data class <B>Data_" + O._Name + "</B> is mapped to the table <B>" + O.getShortName() + "</B>." + SystemValues.NEWLINE
        + "<UL>" + SystemValues.NEWLINE);
        switch (O._LC)
          {
            case NORMAL:
              Out.println("<LI>The Object has normal <B>read/write</B> capabilities.</LI>");
              break;
            case READONLY:
              Out.println("<LI>The Object is <B>ReadOnly</B>.</LI>");
              break;
            case WORM:
              Out.println("<LI>The Object is <B>WORM</B> (Write Once Read Many).</LI>");
              break;
            default:
              throw new Exception("Unknown Object lifecycle value '" + O._LC + "' when generating class docs");
          }

        if (O._OCC == true)
          Out.println("<LI>The Object is OCC-enabled. Default created/lastUpdated/deleted columns will be automatically generated.</LI>");
        else
          Out.println("<LI>That Object is not OCC-Enabled. No record lifecycle columns (created/updated/deleted) have been generated.</LI>");

        Out.println("</UL>");

        Out.println(
        "<B>Description</B>: " + O._Description + "<BR>" + SystemValues.NEWLINE
        + "<BR>" + SystemValues.NEWLINE
        + "It contains the following columns:<BR>" + SystemValues.NEWLINE
        + " <TABLE border=\"0px\" cellpadding=\"3px\" cellspacing=\"0px\">" + SystemValues.NEWLINE
        + "   <TR><TH>&nbsp;</TH><TH align=\"right\">Name&nbsp;&nbsp;</TH><TH align=\"left\">Type</TH><!--TH align=\"left\">Column</TH--><TH align=\"left\">Type</TH><TH align=\"left\">Nullable</TH><TH align=\"left\">Mode</TH><TH align=\"left\">Invariant</TH><TH align=\"left\">Protect</TH><TH align=\"left\">Description</TH></TR>" + SystemValues.NEWLINE);
       
        int i = 1;
        for (Column C : O._Columns)
          {
            if (C == null)
              continue;
            Out.println(
            "  <TR valign=\"top\" bgcolor=\"" + (i % 2 == 0 ? "#FFFFFF" : "#DFECF8") + "\">"
            + "<TD>" + i + "&nbsp;&nbsp;</TD>"
            );
            
            if(C.getSingleColFK() != null || (view != null && C._SameAsObj != null) || (view != null && view._Pivot != null && view._Pivot.hasValue(C.getName())) ) {
            	Out.println("<TD onclick=\"onModalShowClicked('"+O._Name+"-"+C.getName()+"')\" align=\"right\"><B id='"+O._Name+"-"+C.getName()+"_DIV' class='columns dotted_underline cursor_pointer'>" + C.getName() + "</B>&nbsp;&nbsp;</TD>");
            } else {
            	Out.println("<TD align=\"right\"><B id='"+O._Name+"-"+C.getName()+"_DIV' class='columns'>" + C.getName() + "</B>&nbsp;&nbsp;</TD>");
            }
            
        	
            
            Out.println(
            "<TD>" + JavaJDBCType.getFieldType(C) + (C.isList() == true ? " List<>" : C.isSet() == true ? " Set<>" : "") + "&nbsp;&nbsp;</TD>"
            // + "<TD><B>" + C.getName() + "</B>&nbsp;&nbsp;</TD>"
            + "<TD>" + G.getSql().getColumnType(C) + "&nbsp;&nbsp;</TD>"
            + "<TD align=\"center\">" + (C._Nullable == true ? "&#x2611;" : "") + "&nbsp;&nbsp;</TD>"
            + "<TD align=\"center\">" + (C._Mode == ColumnMode.NORMAL ? "" : C._Mode) + "&nbsp;&nbsp;</TD>"
            + "<TD align=\"center\">" + (C._Invariant == false ? "" : "&#x2611;") + "&nbsp;&nbsp;</TD>"
            + "<TD align=\"center\">" + (C._Protect == null ? "" : C._Protect) + "&nbsp;&nbsp;</TD>"
            + "<TD>" + C._Description + "</TD>"
            + "</TR>");
            
            if (C._MapperDef != null)
              {
                Out.println("  <TR bgcolor=\"" + (i % 2 == 0 ? "#FFFFFF" : "#DFECF8") + "\"><TD></TD><TD></TD><TD colspan=\"10\" align=\"center\">");
                Out.println("This column is automatically generated against the Mapper " + C._SameAsObj.getFullName() + ".");
                Out.println("</TD></TR>");
              }
            if (C._Values != null)
              {
                Out.println("  <TR bgcolor=\"" + (i % 2 == 0 ? "#FFFFFF" : "#DFECF8") + "\"><TD></TD><TD></TD><TD colspan=\"10\" align=\"center\">");
                docFieldValues(Out, C);
                Out.println("</TD></TR>");
              }
            
            ++i;
            
          }

        Out.println("</TABLE>");
               
        for(Column C : O._Columns) {
          Out.println("<DIV id='"+O._Name+"-"+C.getName()+"_MODAL' class='modal'>");
          Out.println("<DIV class='modal-content'>");
          Out.println("<SPAN onclick=\"onModalCloseClicked('"+O._Name+"-"+C.getName()+"_MODAL')\" class='close'>&times;</SPAN>");
          // Out.println("<DIV><CENTER><H2>Dependencies for Column "+C.getShortName()+"</H2></CENTER></DIV>");
          Out.println("<DIV><CENTER><H2>Column Dependencies</H2></CENTER></DIV>");

          Out.println("<table> ");
          Out.println("  <tr> ");
          Out.println("    <th align='left' width=\"200em\">Schema</th> ");
          Out.println("    <th align='left' width=\"400em\">Table/View</th> ");
          Out.println("    <th align='left' >Column/Formula</th> ");
          Out.println("  </tr> ");
          
          Out.println("<tr><td></td></tr><tr><td></td></tr>");
          PrintColumnHierarchy(Out, O, C, false, 1);
          
          Out.println("</table>");
          Out.println("</DIV></DIV>");
        }       
        
        Out.println("</DIV>");
      }
	
	private static void PrintColumnHierarchy(PrintWriter Out, Object O, Column C, boolean skipPrintColumn, int level) {
		if (O != null && C != null) {
			View view = O._ParentSchema.getView(O._Name);
			if(!skipPrintColumn) {
				if(C.getSingleColFK() != null || (view != null && C._SameAsObj != null) || (view != null && view._Pivot != null && view._Pivot.hasValue(C.getName())) )
					PrintColumn(Out, C, level, false, "");
				else
					PrintColumn(Out, C, level, true, "");
			}
			
			
			if(view != null && view._Pivot != null && view._Pivot.hasValue(C.getName())) {
				// Follow Pivot
				PrintColumn(Out, view._Pivot._VC._SameAsObj, level + 1, false, " = '"+C.getName()+"'");
				PrintColumnHierarchy(Out, view._Pivot._VC._SameAsObj._ParentObject, view._Pivot._VC._SameAsObj, true, ++level);
				
			} else if( view != null && C._SameAsObj!= null) {
				// Follow SameAs
				PrintColumnHierarchy(Out, C._SameAsObj._ParentObject, C._SameAsObj, false, ++level);
				
			} else if(C.getSingleColFK() != null && C.getSingleColFK()._PrimaryKey != null) {
				// Follow FK
				int tempLevel = ++level;
				for(Column C2 : C.getSingleColFK()._PrimaryKey._ColumnObjs) {
					PrintColumnHierarchy(Out, C2._ParentObject, C2, false, tempLevel);
				}
									
			}
		}
	}	

	private static void PrintColumnHierarchy(PrintWriter Out, View V, ViewColumn VC, boolean skipPrintColumn, int level) {
		if (V != null && VC != null) {
			if(!skipPrintColumn) {
				if( (V != null && VC._SameAsObj != null) || (V != null && V._Pivot != null && V._Pivot.hasValue(VC.getName())) )
					PrintColumn(Out, VC, level, false, "");
				else
					PrintColumn(Out, VC, level, true, "");
			}
			
			if (V._Pivot != null && V._Pivot.hasValue(VC.getName())) {
				// Follow Pivot
				PrintColumn(Out, V._Pivot._VC._SameAsObj, level + 1, false, " = '"+VC.getName()+"'");
				PrintColumnHierarchy(Out, V._Pivot._VC._SameAsObj._ParentObject, V._Pivot._VC._SameAsObj, true, ++level);
				
			} else if (VC._SameAsObj != null) {
				// Follow SameAs
				PrintColumnHierarchy(Out, VC._SameAsObj._ParentObject, VC._SameAsObj, false, ++level);
				
			}
		}
	}

	private static void PrintColumn(PrintWriter Out, Column C, int level, boolean isLast, @NotNull String valueToAppend) {
		String indentedBody = "";
		for(int i = 2; i < level ; i++) indentedBody += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

		String schemaDocFileName = "TILDA___Docs."+C._ParentObject._ParentSchema._Name+".html";
		String tableName = schemaDocFileName+"#"+C._ParentObject._Name+"_DIV";
		String columnName = schemaDocFileName+"#"+C._ParentObject._Name+"-"+C.getName()+"_DIV";
		Out.println("<tr>");
		if (level < 2)
			Out.println("<td><a href='"+schemaDocFileName+"'>"+C._ParentObject._ParentSchema._Name+"</a></td>");
		else
			Out.println("<td>"+indentedBody+"&#9492;&#9472;<a href='"+schemaDocFileName+"'>"+C._ParentObject._ParentSchema._Name+"</a></td>");
		
		Out.println("<td><a href='"+tableName+"'>"+C._ParentObject._Name+"</a></td>");
		
		if (level < 2)
			Out.println("<td><a href='"+columnName+"'>"+C.getName()+"</a>"+valueToAppend+" -- "+C._TypeStr+"</td>");
		else
			Out.println("<td><a href='"+columnName+"'>"+C.getName()+"</a>"+valueToAppend+"</td>");;
		Out.println("</tr>");
	}
	
	private static void PrintColumn(PrintWriter Out, ViewColumn VC, int level, boolean isLast, @NotNull String valueToAppend) {
		String indentedBody = "";
		for(int i = 2; i < level ; i++) indentedBody += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		
		String schemaDocFileName = "TILDA___Docs."+VC._ParentView._ParentSchema._Name+".html";
		String tableName = schemaDocFileName+"#"+VC._ParentView._Name+"_DIV";
		String columnName = schemaDocFileName+"#"+VC._ParentView._Name+"-"+VC.getName()+"_DIV";

		Out.println("<tr>");
		if (level < 2)
			Out.println("<td><a href='"+schemaDocFileName+"'>"+VC._ParentView._ParentSchema._Name+"</a></td>");
		else
			Out.println("<td>"+indentedBody+"&#9492;&#9472;<a href='"+schemaDocFileName+"'>"+VC._ParentView._ParentSchema._Name+"</a></td>");

		Out.println("<td><a href='"+tableName+"'>"+VC._ParentView._Name+"</a></td>");
		
		if (level < 2)
			Out.println("<td><a href='"+columnName+"'>"+VC.getName()+"</a>"+valueToAppend+" -- "+VC._SameAsObj._TypeStr+"</td>");
		else
			Out.println("<td><a href='"+columnName+"'>"+VC.getName()+"</a>"+valueToAppend+"</td>");
		Out.println("</tr>");
	}
	
	private static void PrintFormula(PrintWriter Out, Formula F, int level) {
		String indentedBody = "";
		for(int i = 2; i < level ; i++) indentedBody += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		
		String schemaDocFileName = "TILDA___Docs."+ F.getParentView()._ParentSchema._Name+".html";
		String tableName = schemaDocFileName+"#"+F.getParentView()._Name+"_DIV";
		String columnName = schemaDocFileName+"#"+F.getParentView()._Name+"-"+F._Name+"_DIV";
		
		Out.println("<tr>");
		if (level < 2) // Prints symbol for hierarchy
			Out.println("<td><a href='"+schemaDocFileName+"'>"+F.getParentView()._ParentSchema._Name+"</a></td>");
		else
			Out.println("<td>"+indentedBody+"&#9492;&#9472;<a href='"+schemaDocFileName+"'>"+F.getParentView()._ParentSchema._Name+"</a></td>");

		Out.println("<td><a href='"+tableName+"'>"+F.getParentView()._Name+"</a></td>");
		
		if (level < 2) // Prints Root Node Data type
			Out.println("<td><a href='"+columnName+"'>"+F._Name+"</a> -- "+F._TypeStr+"</td>");
		else
			Out.println("<td><a href='"+columnName+"'>"+F._Name+"</a></td>");
		Out.println("</tr>");
	}

		
    private static void docFieldValues(PrintWriter Out, Column C)
      {
        Out.println("<TABLE border=\"0px\" cellpadding=\"2px\" cellspacing=\"0px\">"
        + "   <TR align=\"left\"><TH>&nbsp;</TH><TH align=\"right\">Name&nbsp;&nbsp;</TH><TH>Value&nbsp;&nbsp;</TH><TH>Label&nbsp;&nbsp;</TH><TH>Default&nbsp;&nbsp;</TH><TH>Groupings&nbsp;&nbsp;</TH><TH>Description</TH></TR>");
        int i = 0;
        for (ColumnValue V : C._Values)
          {
            if (V == null)
              continue;
            Out.println("  <TR bgcolor=\"" + (i % 2 == 0 ? "#FFFFFF" : "#FFF2CC") + "\">"
            + "<TD>" + i + "&nbsp;&nbsp;</TD>"
            + "<TD align=\"right\"><B>" + V._Name + "</B>&nbsp;&nbsp;</TD>"
            + "<TD>" + V._Value + "&nbsp;&nbsp;</TD>"
            + "<TD>" + V._Label + "&nbsp;&nbsp;</TD>"
            + "<TD>" + V._Default + "&nbsp;&nbsp;</TD>"
            + "<TD>" + TextUtil.Print(V._Groupings) + "&nbsp;&nbsp;</TD>"
            // + "<TD>" + V._Raw + "</TD>"
            + "<TD>" + V._Description + "</TD>"
            + "</TR>");
            ++i;
          }
        Out.println("</TABLE>");
      }


    public static String CleanForHTML(String[] Str)
      {
        return String.join("\n", Str); // .replaceAll("(?i)<\\s*br\\s*>\\s*<(/?\\s*[^>]+)\\s*>", "<$1>");
      }

    public static String cleanClause(String Str)
      {
        return Str.replaceAll("(?i)\\b([_a-z][a-z0-9_]*\\.)*([_a-z][a-z0-9_]*\\.[_a-z][a-z0-9_]*\\.[_a-z][a-z0-9_])", "$2");
      }

    public static void RealizedDataMartTableDocs(PrintWriter Out, ParserSession PS, View V)
    throws Exception
      {
        String TName = V._Name.substring(0, V._Name.length() - (V._Pivot != null ? "PivotView" : "View").length()) + "Realized";
        Out.println("<DIV>");
        Out.println("<DIV id='" + TName + "_DIV' class='tables'>");
        Out.println("<H2>" + TName + "&nbsp;&nbsp;&nbsp;&nbsp;<SUP style=\"font-size: 60%;\"><A href=\"#\">top</A></SUP></H1>");
        Out.println("</DIV>");
        Out.println(
        "The generated DataMart table based on the View <B>" + V._Name + "</B>." + SystemValues.NEWLINE
        + "<BR><BR>"
        + "<STYLE>"
        + ".RowedTable > TBODY > TR > TD {"
        + "  vertical-align: top;"
        + "  border: 1px solid black;"
        + " }"
        + ".RowedTable > TBODY >TR > TD:nth-child(1) {"
        + "  padding-right: 5px;"
        + "  text-align: right;"
        + " }"
        + "UL {"
        + "   margin-top: 0px;"
        + " }"
        + "</STYLE>");

        Out.println("<H3>Inclusions/Exclusions Criteria</H3>");
        DoewSubWhereDetails(Out, V);

        Set<String> ObjectNames = new HashSet<String>();
        boolean First = true;
        for (ViewColumn VC : V._ViewColumns)
          {
            if (VC == null || VC._SameAsObj == null)
             continue;
            Object O = VC._SameAsObj._ParentObject;
            if (ObjectNames.add(O.getShortName()) == false)
              continue;
            if (O._FST == FrameworkSourcedType.VIEW)
              {
                if (First == true)
                  {
                    Out.println("In addition, this view depends on other views:");
                    Out.println("<BLOCKQUOTE><TABLE class=\"RowedTable\" border=\"0px\" cellspacing=\"0px\" cellpadding=\"2px\">");
                    First = false;
                  }
                View V2 = PS.getView(O._ParentSchema._Package, O._ParentSchema._Name, O._Name);
                if (V2 == null)
                  throw new Exception("Cannot find View " + O._ParentSchema._Package + "." + O._ParentSchema._Name + "." + O._Name);
                Out.println("<TR><TD>" + V2.getShortName() + "</TD><TD>");
                DoewSubWhereDetails(Out, V2);
                Out.println("</TD></TR>");
              }
          }
        Out.println("</TABLE></BLOCKQUOTE><BR><BR></BLOCKQUOTE>");

        Out.println("<H3>The following terms/formulas are defined:</H3>"
        + "<BLOCKQUOTE>\n");
      
        if (V._Formulas != null)
          for (Formula F : V._Formulas)
            {
              StringBuffer Str = new StringBuffer();
              SortedSet<String> ColumnMatches = new TreeSet<String>();
              Matcher M = F._ViewColumnsRegEx.matcher(String.join("\n", F._FormulaStrs));
              while (M.find() == true)
                {
                  String s = M.group(1);
                  ViewColumn VC = V.getViewColumn(s);
                  if (VC != null)
                    {
                	  M.appendReplacement(Str, "<B style=\"color:#00AA00;\">" + s + "</B>");
                      ColumnMatches.add(s);
                    }
                }
              M.appendTail(Str);

              SortedSet<String> FormulaMatches = new TreeSet<String>();
              M = F._FormulasRegEx.matcher(Str.toString());
              Str.setLength(0);
              while (M.find() == true)
                {
                  String s = M.group(1);
                  for (Formula F2 : V._Formulas)
                    if (s.equals(F2._Name) == true)
                      {
                        M.appendReplacement(Str, "<B style=\"color:#0000AA;\">" + s + "</B>");
                        FormulaMatches.add(s);
                        break;
                      }
                }
              M.appendTail(Str);

              String FormulaStr = Str.toString();

              // Start Table
              Out.println("<TABLE class=\"RowedTable\" border=\"0px\" cellspacing=\"0px\" cellpadding=\"2px\" width=\"75%\">");
              
              // Start Rows
              Out.println("<TR style=\"height: 40px;\"><TD style=\"border: 0px !Important;\" colspan=\"2\">&nbsp;</TD></TR>");
              
              Out.println("<TR bgcolor=\"DFECF8\">");
              if ( !FormulaMatches.isEmpty() || !ColumnMatches.isEmpty() ) {
            	  Out.println("<TD style=\"text-align:left !important;\" colspan=\"2\"><B>Term</B> <B onclick=\"onModalShowClicked('"+TName+"-"+F._Name+"')\" id='"+TName+"-"+F._Name+"_DIV' class='formula dotted_underline cursor_pointer'>" + F._Name + "</B>" + (TextUtil.isNullOrEmpty(F._Id) == true ? "" : (" &nbsp;&nbsp;&nbsp; (#" + F._Id + ")")) + "</TD>");
              } else {
            	  Out.println("<TD style=\"text-align:left !important;\" colspan=\"2\"><B id='"+TName+"-"+F._Name+"_DIV' class='formula'>Term " + F._Name + "</B>" + (TextUtil.isNullOrEmpty(F._Id) == true ? "" : (" &nbsp;&nbsp;&nbsp; (#" + F._Id + ")")) + "</TD>");
              }
              Out.println("</TR>");
              
              Out.println("<TR><TD><B>Title</B></TD><TD>" + F._Title + "</TD></TR>"
              + "<TR><TD><B>Description</B></TD><TD>" + CleanForHTML(F._Description) + "</TD></TR>"
              + "<TR><TD><B>Formula</B></TD><TD><PRE style=\"padding-top: 3px;\">" + FormulaStr + "</PRE></TD><TR>");
              if (F._Values != null && F._Values.length > 0)
                {
                  Out.println("<TR valign=\"top\"><TD><B>Values</B></TD><TD><TABLE border=\"0px\">");
                  for (Value Val : F._Values)
                    Out.println("<TR><TD>" + Val._Value + "</TD><TD>" + Val._Description + "</TD><TR>");
                  Out.println("</TABLE></TD></TR>");
                }
              Out.println("<TR valign=\"top\"><TD><B>Referenced Columns</B></TD><TD>");
              if (ColumnMatches.isEmpty() == true)
                Out.println("None");
              else
                { 
                  Out.println("<TABLE border=\"0px\">");
                  for (String ColName : ColumnMatches)
                	  Out.println("<TR><TD valign=\"top\" align=\"right\"><B style=\"color:#00AA00;\">" + ColName + "</B>:</TD><TD>" + V.getColumn(ColName)._Description + "</TD></TR>");
                  Out.println("</TABLE>");
                }
              Out.println("</TD></TR>");
              Out.println("<TR valign=\"top\"><TD><B>Referenced Terms</B></TD><TD>");
              if (FormulaMatches.isEmpty() == true)
                Out.println("None");
              else
                {
                  Out.println("<TABLE border=\"0px\">");
                  for (String FormulaName : FormulaMatches)
                    Out.println("<TR><TD valign=\"top\" align=\"right\"><B style=\"color:#0000AA;\">" + FormulaName + "</B>:</TD><TD>" + CleanForHTML(V.getFormula(FormulaName)._Description) + "</TD></TR>");
                  Out.println("</TABLE>");
                }
              Out.println("</TD></TR>");
              
              // End Table
              Out.println("</TABLE>");
              
              if (!ColumnMatches.isEmpty() || !FormulaMatches.isEmpty() ) {
                  Out.println("<DIV id='"+TName+"-"+F._Name+"_MODAL' class='modal'>");
                  Out.println("<DIV class='modal-content'>");
                  Out.println("<SPAN onclick=\"onModalCloseClicked('"+TName+"-"+F._Name+"_MODAL')\" class='close'>&times;</SPAN>");
                  // Out.println("<DIV><CENTER><H2>Dependencies for Formula "+F.getShortName()+"</H2></CENTER></DIV>");
                  Out.println("<DIV><CENTER><H2>Formula Dependencies</H2></CENTER></DIV>");

                  Out.println("<table> ");
                  Out.println("  <tr> ");
                  Out.println("    <th align='left' width=\"200em\">Schema</th> ");
                  Out.println("    <th align='left' width=\"400em\">Table/View</th> ");
                  Out.println("    <th align='left' >Column/Formula</th> ");
                  Out.println("  </tr> ");

                  Out.println("<tr><td></td></tr><tr><td></td></tr>");
                  PrintFormula(Out, F, 1);
                  
                  // formula dependencies
                  for( String formulaName : FormulaMatches ) {
                	  Formula formula = V.getFormula(formulaName);
                	  Out.println("<tr><td></td></tr><tr><td></td></tr>");
                	  PrintFormula(Out, formula, 2);
                  }
                  
                  // Column dependencies
                  for( String columnName : ColumnMatches ) {
                	  ViewColumn VC = V.getViewColumn(columnName);
                	  
                	  Out.println("<tr><td></td></tr><tr><td></td></tr>");
                	  PrintColumnHierarchy(Out, VC._ParentView, VC, false, 2);
                  }

                  Out.println("</table>");
                  Out.println("</DIV></DIV>");
              }
              
            }
        Out.println("</BLOCKQUOTE>");
      }


    private static void DoewSubWhereDetails(PrintWriter Out, View V)
      {
        Out.println(V._Description);
        if (V._SubWhereX != null)
          Out.println("<BLOCKQUOTE>That view is filtered: "
          + "<BLOCKQUOTE><PRE>" + cleanClause(String.join("\n", V._SubWhereX._Clause)) + "</PRE>"
          + String.join("\n", V._SubWhereX._Description)
          + "</BLOCKQUOTE>");
        if (V._Pivot != null)
          {
            Out.println("The pivot was done explicitly on the following values for " + V._Pivot._VC._SameAsObj.getName()
            + "<BLOCKQUOTE><PRE><TABLE border=\"0px\">");
            for (Value Val : V._Pivot._Values)
              Out.println("<TR><TD>" + Val._Value + "&nbsp;&nbsp;&nbsp;</TD><TD>" + Val._Description + "</TD></TR>");
            Out.println("</TABLE></PRE><BLOCKQUOTE>");
          }
      }
  }
