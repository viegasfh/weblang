
/* Bug fixes and improvements:

1999:

145. 6/8  Improved the HTML parser so that <script> elements that do not contain
          script code in comments (eg <!-- code here -->) does not throw the parser
          off. The parser will skip directly to the end of the script section,
          as defined by the "</script>" tag. Everything between <script ..> and 
          </script> is regarded as a single text segment (no interpretation at all is
          performed, even if it contains markup, or is a comment itself.)
          (Thanks Tom Szymanski)
144. 6/1  Added support for printing recursive (object) data structures.
          (Thanks Pete Halverson)
143. 5/18 Fixed missing ARGS. (thanks Tom Szymanski)
142. 5/18 Fixed webl.util.(Macro(BufReader.skipTill to work correctly at EOF. 
          (thanks Dave Barrett)
141. 5/4  Added tag equal/not equal comparison. (thanks Dave Barrett)
140. 5/4  Fixed argument extraction in Files_PostURL. (thanks Alex Safonov)
139. 5/3  Fixed scanning of \\uXXXX sequences in Scanner. (thanks Jeffrey Hsu)

          --- Public release 3.0g on 4/29
          
138. 4/29 Added "expandentities" option to GetURL, PostURL etc, to expand the
          character entities (eg "&quot;") in an HTML (only) page. Only the character
          entities in parsed character data are expanded (i.e. only between tags, not 
          in attributes). Use this option carefully, especially only if the resulting
          page is *not* to be displayed in future.
137. 4/29 Added DeleteField(o: object, field): nil to delete a field from an object.
136. 4/28 Fixed SplitQuery operation on empty query values eg x=&y= and so on.
          (Thanks Andrew Comas)
135. 4/21 Removed a security hole in WebServer that allows tricks with filenames
          to potentially access any file on your machine. The canonical file name 
          requested should match *exactly* the root server directory name prefix 
          (it is case sensitive).
          Tip: Because of this case sensitivity write on MS Windows 
            
                WebServer_Start("C:\\serverroot", 80); 
                
          instead of
          
                WebServer_Start("c:\\serverroot", 80);
          
          If you get access denied messages from the WebServer, switch on debugging
          messages with -D and monitor the console of the web server for the explanation.
134. 4/21 WebServer now allows export of arbitrary named scripts, not only those
          starting with /bin. The default behavior is to first look for a script
          matching the path, and if none is found, look for a file with the same
          name. This also allows scripts to override files with the same name. (thanks steveg)
133. 4/21 -D switch now prints a message for each request to the WebServer. (thanks steveg)
134. 4/21 Fixed WebServer returning nil instead of 404 message. (thanks steveg)
133. 4/20 Major re-organization regarding embedding of WebL inside other applications,
          causing the interfaces to classes WebL and Machine to change. 
          See CallWebL.java and CallWebL2.java for examples of how things are used now. 
          The motivation for these changes involves plumbing support for embedding WebL 
          code in HTML markup etc.
132. 4/20 Fixed ReadLn at EOF. (thanks Stephan Hartman)
131. 4/16 First exception code for First([]). (thanks David Barrett)
130. 4/15 Fixed Url.webl to handle list arguments. (thanks Alex Safonov)

          --- Public release 3.0f on 4/13
          
129. 4/8  Changed Print, PrintLn, Error, ErrorLn to work atomically. Instead of printing
          the arguments one by one, a temporary buffer is created, the arguments printed into
          it, and the buffer emitted in one action. This reduces lock contention at the cost
          of an extra buffer being allocated and then thrown away. (thanks zalman stern)
128. 4/6  Added a new Exception section to Quick Ref part of the WebL user manual,
          which lists the exceptions throw by statements, operators, and built-in functions.
127. 4/5  Renamed exception type names to make them a little more consistent. 
126. 3/20 Added NOBR tag to the HTML 3.2 DTD to cut down on the number of HTML parsing
          errors. Of course, this deviates from the official HTML 3.2 spec.
125. 3/20 Added support for multiple cookie databases (see module Cookies in the user manual
          for explanation). (thanks Joel Rieke).
124. 3/30 Added Files_Size(filename:string):int built-in.
123. 3/25 The JDK does NOT allow multiple same named headers to be send to the server.
          WebL has the code to do it, but the JDK blocks all of our attempts to do this. Only
          the last header of the list will be sent to the server.
          So please ignore the corresponding description in bug 122. (thanks Thomas Szymanski)
122. 3/18 Changed default program encoding from UTF8 to the platform convention. 
          (Thanks Lars Rasmusson)
121. 3/17 Fixed Java_New and Java_Class so that missing indirectly used classes are detected
          correctly. (thanks Joel Rieke)
120. 3/17 Made HTML character entities case sensitive when using ExpandCharEntities, by fixing 
          webl.dtd.DTDParse. (thanks Jonas Holmberg)
119. 3/17 Close Reader when finished in XML, HTML, and plain text parser. (thanks Nicolas Esposito)
118. 3/11 Change in AutoStreamReader to close input on java.net.SocketException. This allows
          communicating with Microsoft HTTPS servers that close the socket incorrectly after a page
          is downloaded.
117. 3/10 Fixed parsing of cookies when loading from file. (thanks Dave Jones)

          --- Public release of WebL3.0e on 2/5
          
116. 2/5  Fixed distance calculation  between WebL data types and Java data types in 
          module java.
115. 2/5  Fixed bug introduced by improvement #89.
114. 2/4  Fixed defineClass in webl.builtins.NativeFun not to use the deprecated version
          of the method.
113. 2/4  Changed importDTD in webl.dtd.Catalog to use Readers instead of streams.
112. 2/4  Replace PrintStream with PrintWriter in webl.page.net.Net.
111. 2/4  Switched to OROMatcher version that is compatible with JDK 1.2/Java 2.
110. 2/4  Files_LoadStringFromFile now takes an optional second string argument that
          specifies the charset encoding to use (e.g UTF8, UTF16, and so on).
109. 2/1  Fixed Files_LoadStringFromFile to work with international character sets.
108. 2/1  Corrected return value of Type(o:j-array) and Type(a:j-array).
107. 1/22 Added "noncompliantPOSTredirect" flag to GetURL & PostURL. 
          See user manual for details. (thanks Steve Glassman)
106. 1/12 Disallowed expressions of the form "abc" + 100. (thanks Keith Sibson)
105. 1/7  Moved Cell class from PieceSet.java to Cell.java. (thanks Robin Cottiss)
104. 1/6  Added support for multiple headers (in & out) and parameter (in) fix in WebServer module.
103. 1/5  Fix in module servlet where multiply occur paramaters from a request of the form
            
                http://xxx/yyy?a=1&a=2&a=3
              
          is mapped into a list. i.e. req.param.a = ["1", "2", "3"]. (thanks Raphael Anumba)
102. 1/5  HTTP headers do not map cleanly into WebL objects, since the same header (name)
          can be repeated several times, and WebL objects require unique field names.
          This fix introduces a work around that uses lists of header values.
          
          In the new scheme, should a web server return N headers with the same name, e.g.

              HeaderA: X
              HeaderA: Y   (etc)
              
          the header value of the return page object will be a list with N
          values, e.g.
          
              page.HeaderA = [ "X", "Y" ]
              
          In the case of only one header returned, a string is returned as usual.
          
      (Note: See bug 123 for an addendum to the following paragraph:)
          In a similar vein, submitting multiple (of the same) headers with GetURL 
          is now possible:
          
              GetURL(url, nil, [. HeaderA = [ "X", "Y" ] .]
          
101. 1/5  Correctly process multiple set-cookie headers. (thanks B Ling)
100. 1/5  Associated mimetype "application/xml" with builtin xml parser.

1998:

99. 12/16 Fixed module Words to use the Para function to break a page into words.
98. 12/16 Removed insertspace argument of the Text builtin. See the description of 
          the Para function in the markup algebra section of the WebL user
          manual for an alternative to extracting the text of a page while taking
          word breaks into account. (also see fix #86)
97. 12/16 Introduced NewPieceSet(p: page): pieceset to create an empty pieceset
          associated with page p. (thanks Zalman Stern)
96. 12/15 Introduced Para builtin for breaking pages/pieces into paragraphs.
          (See "Searching functions" in the markup algebra chapter of the
          user manual.)
95. 12/15 NewPieceSet({}) now throws an "EmptySet" exception. (thanks Zalman Stern)
94. 12/15 Further improvements in Cookie class to handle case with multiple '='. 
          (thanks john goalby)
93. 12/14 Added a demonstration class called CallWebL.java that illustrates how
          to invoke the WebL interpreter from a Java program.
92. 12/14 Minor re-org in WebL.java and Machine.java. 
91. 12/14 Minor improvement in Cookie class to handle case with multiple '='.
90. 12/14 Added PCData(p:(page|piece)): pieceset as a standard builtin. This builtin
          returns the "parsed character data" of a given page/piece context. The function
          is similar to Text(p) except that it returns multiple pieces for each contiguous
          stretch of text (deliniated by tags).
89. 12/14 Added Seq(p: piece): pieceset support.
88. 12/14 Fixes Pat(p: piece, s: string) to not over-run regular searches beyond
          the end tag of p. (thanks Zalman Stern)
87. 12/10 Added the possibility of accessing Java objects directly with module
          Java. See user manual chapter modules for more details.
86. 11/20 Added Text(x, insertspaces:boolean): string builtin that will insert
          a space in the result string wherever tags occur in x. In case of HTML, 
          inline elements like font, i, b, tt, em, etc will not be replaced with
          spaces. (thanks monika)
85. 11/16 Improved error checking of argument to Farm.Perform(task). "Task" 
          must be a function application (method calls are not allowed). (thanks pkumar)
84. 11/16 Updated WebL version number. (thanks felciano)
83. 11/16 Fixed argument count of Str_EqualsIgnoreCase. (thanks pkumar)

          --- Public release 3.0d on 11/13
          
82. 11/13 Fixed memory leak in Farms. This leak caused DemoCrawler.webl to use too much space.
          The problem was introduced by improvement #53. (thanks pkumar)
81. 11/10 Added Trap(x) builtin to catch any exception objects generated while evaluating
          x. In addition, the exception object is augmented with a "trace" field that contains
          a complete description of the exception, where it occured, and the stack trace. This
          information is useful for logging unexpected exceptions. (thanks barrett)
80. 11/10 Added ToList and ToSet builtin functions. Use Size(ToList(o:object)) to count
          how many fields an object has.
79. 11/10 "return;" is now equivalent to "return nil". (thanks farshad)
78. 11/10 Added URL to debugging output while parsing web pages. (thanks farshad)
77. 11/2  Fixed memory leak where threads were not being collected. (thanks pkumar)
76. 10/30 Added "fixhtml" option to GetURL, PostURL. Setting this flag to true (default
          is false), will cause the HTML parser to pay more attention to valid element
          nestings, and attempt to rewrite elements (when possible) to ensure they obey
          the HTML spec. Note that switching on this option may result in unintuitive HTML
          and the resulting page can be far removed from the original.
75. 10/27 Fixed Files_LoadStringFromFile to close file after reading it. (thanks barrett)
75. 10/21 Fixed Rest([]) so that it returns the empty list [].
74. 10/20 Added "emptyparagraphs" option to GetURL, PostURL. Setting this flag to true (default
          is false), will cause HTML <p> tags to be treated as empty tags (like <br>). This
          is a useful to take care of HTML authors that are using <p> tags as if they are breaks,
          without regard to where the HTML spec says they are allowed.
73. 10/20 Added ^ and \ as valid path characters to URLs. (see weblx.url.URLSplitter)
72. 10/20 Modified cookie handling so that it does not throw a null exception when
          confronted with an illegal url.
71. 10/16 Added RealArg to webl.lang.AbstractFunExpr. (thanks barrett)
70. 10/9  Fixed null pointer exception when loading a non-existing class in the
          Native builtin. (thanks barrett)
          
          --- Public release 3.0c on 10/5
          
69. 10/5  Improved error reporting by the WebL language parser.
68. 10/5  Fixed example scripts that broke because of site changes.
67. 10/5  Added builtins to "select" elements from lists, sets, and piece-sets:
            Select(s: set, f: fun): set
            Select(l: list, f: fun): list
            Select(p: pieceset, f: fun): pieceset
          Function f should take a single argument and return true or false depending if
          the argument should be included in the result or not.
          Examples:  Select([1, 2, 3, 4], fun(s) s mod 2 = 0 end)
                     Select(Elem(P, "a"), fun(a) Str_StartsWith(a.href, "http://xxx") end)
                     
66. 10/2  Added Java servlet support. See weblx.servlet.Servlet.java for docu.
          Note that the servlet support is still very much untested and not
          extensively documented (nothing in the manual so far). Use at your
          own risk !
65. 10/2  Minor changes in output logging to make servlets report syntax errors.
64. 9/30  Added support for reloading a module should it have been modified since it
          was last loaded. (See webl.lang.Machine)
63. 9/29  Corrected temporary file writting Browser_ShowPage. Should eventually be 
          replaced with Files.createTempFile when JDK1.2 is released.
62. 9/29  Added MacShowPageFun contibuted by Steve March. (thanks steve)
61. 9/29  Fixed printing of HTML element attr with no value.
60. 9/29  Small improvement in HTML parser to skip over bogus attribute values. (thanks barrett)
59. 9/29  Fixed problem with Pat returning empty groups as "null". (thanks barrett)
58. 9/29  I now close the input stream of the webl program in WebL.java. (thanks Brent Iverson)
57. 9/29  Changed Files_GetURL, Files_PostURL, GetURL, PostURL, HeadURL, so as to also take
          a string value as param. This allows the programmer take complete control over what 
          parameters are posted etc.
56. 9/28  Added a field ordering to objects. Fields are ordered in the sequence they are defined.
          This should reduce some problems encountered with HTTP form parameter submission.
          (thanks barrett, rmahoney)
55. 9/28  Added automatic HTTP cookie support. WebL will keep track of cookies for all sites
          for the current runtime session. For each run the cookie database is empty initially 
          and fills up as cookies are set. To load and store cookie databases I added a Cookies
          module. Note that in the current implementation cookies are never expired. It is 
          possible to explicitly override cookies by setting the "Cookie" header field of
          GetURL, PostURL, etc.

          --- Public release 3.0b on 9/24
          
54. 9/21  Added an autoredirect options flag for GetURL, and PostURL. When the flag is set to
          true, HTTP auto-redirects would be performed (which will cause GetURL and PostURL to 
          throw an exception, for example 302 "Moved Temporarily"). When omitted, the flag
          defaults to true.
          Also added option support to Files_GetURL and Files_PostURL.
53. 9/18  Added a simple stack trace facility that reports all the callsites of function
          and method invocations when an exception occurs. Also lists all the variables
          in the context of the exception, including their relative stack offsets, types, 
          and values.
52. 9/18  Modified webl.util.AutoStreamReader.read to skip over badly encoded byte streams
          (thanks barrett)
51. 9/15  Renamed shell.dll to weblwin32.dll. Added DDE support to Browser module to "talk"
          to a running Netscape web browser (Windows only) (No support for MS IE at this moment).
          See the module section of the user manual for more details.
50. 9/14  After several messages from WebL programmers that fell into the trap, I have decided
          to make a small language change to pre-empt further problems. Ending a statement
          sequence with a semicolon WILL NOT implicitly append a "nil" statement at the end
          of the basic block any more.
49. 9/10  Fixed the (!)directlycontain and (!)directlyinside markup operators to work
          as defined by the formal definition in the user manual. The previous implementations
          worked incorrectly when some relatively rare deep nestings occurred. (thanks tomr)
          (This change will cause the existing test harness to fail, at least until I 
          update it to reflect the correct semantics.)
48. 8/13  Changed webl.util.FileLocator to check for absolute filenames too (thanks jinyu).
47. 8/3   Added Files_AppendToFile function to append a string to the end of a file.

          --- Public release 3.0 
 
46. 7/16  Fixed webl.lang.Scanner, BufReader and MacroBufReader to work correctly with \n, \r and \r\n 
          line terminators (thanks kistler).
45. 7/15  Change in webl.util.FileLocator so that new search directories can be appended later.
44. 7/14  Fix in Startup.webl to use the correct UNIX shell command.
43. 7/9   Added standard "Sign" function.
42. 7/8   Fix in WebLThread that solves a problem with Timeout.
41. 7/7   Fix in Url_Resolve (thanks jinyu).
40. 7/7   Fix in Net.java to handle FTP over an HTTP proxy (thanks jinyu).
39. 7/1   Fix in HeadURL to work with FTP protocol too (thanks jinyu).
38. 7/1   Added ReadLn functions to read input from the console (thanks jinyu).
37. 6/29  Changed the formals of GetURL and PostURL to replace the mimeoverride argument
          with an option object instead (much easier to extend later). The user manual
          describes the fields of the options object in the chapter about "Pages".
          In addition to overriding the mimetype, charset and dtd, it is also possible
          to control URL resolution with the "resolveurls" flag. (thanks jinyu)
          Also removed ContentHandler support for page downloading, because this support
          was just causing a lot of messy code.
36. 6/29  Added Url_Resolve function.
35. 6/29  Convert HTTP response headers to lowercase (thanks jinyu).
34. 6/26  Added line number and module name to the fields of an exception object (thanks jdean).
33. 6/23  Added builtin ExpandCharEntities which expands the HTML character entities in a string.
32. 6/23  Bug fix in Delete builtin.
31. 6/22  Improved Exec and Call. Introduced undocumented ExecArgs, ExecCall, and ExpandVariables
          builtin functions to implement these functions (see Startup.webl).
30. 6/22  Allow access to ARGS and PROPS in Startup.webl
29. 6/19  Added Call and Exec builtin functions (see user manual)
28. 6/16  Added the Url module for manipulating URL objects (see user manual).
27. 6/16  Added Str_Split and Str_LastIndexOf.
26. 6/9   Sped up BufReader/MacroBufReader skipTill by using a local buffer
          to prevent the string append sync penalty.
25. 6/5   Added a global PROPS object that has as fields the Java system properties.
24  6/5   Fix -P option to pause even when a script throws an exception.
23. 6/5   Added HeadURL(...): page function to retrieve the headers of a page
          with the HTTP HEAD request (thanks jinyu)
22. 6/5   Changed weblx.files.Mkdir to create all directories on the path
          (e.g. as in Java's File.Mkdirs)
21. 6/4   Added new functions to module File (thanks jinyu)
            List, Mkdir, IsFile, IsDir and Delete.
20. 6/4   Bug fix in StringExpr. (thanks jdean & jinyu)
19. 6/2   Added a return statement to prematurely return a value from a function
          or a method. (thanks jdean)
18. 6/2   Changed NativeFun so that classes can be loaded from the current
          WebL file search path. (thanks bse)
17. 6/2   Added Seq function for searching sequences of tags in a page.
          (See Searching Functions in Chpater 4 of the WebL manual fordetails)
16. 6/1   Mimetype override for PostURL, GetURL etc.
          is now of the form 'xxx/xxx;charset=xxx;dtd="yyy"', where
          dtd overrides the DTD specified in the HTML page itself. yyy must be the
          official name of the DTD, as defined in the Catalog, i.e.
            "-//W3C//DTD HTML 4.0//EN"   -> HTML 4.0
            "-//W3C//DTD HTML 3.2//EN"   -> HTML 3.2
            "-//IETF//DTD HTML//EN"      -> HTML 2.0
            
15. 6/1   Made the HTML parser more liberal regarding strange characters inside attribute
          values.
14. 6/1   Allowed Sort(L, Str_Compare).
13. 6/1   The ORO regular expression package sometimes returns the same position
          more that once. Fixed Page.getPattern to take this into account.
          
12. 6/1   Replaced the version tree of SetExpr with a more efficient version.

11. 5/27  Transfered changes to BufReader to MacroBufReader.
          Modified Read(), SkipTill() and added readLine();
          
10. 5/27  Added a hashCode function to ListExpr.

9. 5/27  Replaced SetExpr with a more efficient implementation that does not involve copying
         the set each time.
8. 5/26  Replaced ListExpr with a more efficient implementation based on lazy evaluation.
         See "Amortization, Lazy evaluation, and Persistence: Lists with Catenation via
         Lazy Linking", Chris Okasaki, 36th Annual Symposium on Foundations of computer
         science.
         
         The algorithmic costs are as follows (for lists X and Y with lengths x and y):
         
            concatenation of two arbitrary lists with +   O(1)
            First(X: list): any                           O(1)
            Rest(X: list): list                           O(1)
            PrintLn/Print of X                            O(x)
            X[index]                                      O(x)
            Select(List, from, to)                        O(x)
            
         The O(x) costs are only paid once for a specific list X, afterwards the cost is
         O(1) or O(to-from). This cost is needed to "flatten out" the list into an array
         from the internal pre-order tree used to store the list.
         
7. 5/22  WebL exception objects raised during a network operation now contains an header
         field that lists the header files returned by the web server.
         (thanks bse)
6. 5/22  Corrected weblx.files.SaveToFileFun to work correctly on Sanjay's java runtime.
5. 5/22  Improved parsing of (incorrect) var x := 1.
        (thanks tomr)
4. 5/22  Fixed parsing of semicolon before 'else', 'elsif'.
3. 5/22  Added readLine method to webl.util.BufReader.
         (Thanks bse)
2. 5/22  Added -P, -C, -L options to webl commands. Added logging to file capability to
         webl.util.Log.
         (thanks bse)
1. 5/22  Parent(element) failed with a null pointer exception when element has no parent.
         (thanks bse)
      
0. 5/22  Start of log.

*/

public class BugList {
}