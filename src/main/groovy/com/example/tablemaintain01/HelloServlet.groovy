package com.example.tablemaintain01

import com.opencsv.CSVReader
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.naming.Context
import javax.naming.InitialContext
import javax.servlet.http.*
import javax.servlet.annotation.*
import javax.sql.DataSource
import java.sql.Connection

@WebServlet(name = "helloServlet", value = "/hello-servlet")
class HelloServlet extends HttpServlet {
    private String message

    void init() {
        message = "Hello World!"
    }

    void doGet(HttpServletRequest request, HttpServletResponse response) {

        Logger _logger = LoggerFactory.getLogger(HelloServlet.class)

        ArrayList<GroovyRowResult> StockInfo
        try {
            Context webContainer = new InitialContext()
            DataSource dbSource = (DataSource) webContainer.lookup("java:/comp/env/jdbc/h2mem")
            Connection dbConn =dbSource.getConnection()
            Sql sqlObject = new Sql(dbConn)


            def sqlCmdCreateTable =
                    """
                        CREATE TABLE IF NOT EXISTS "StockInfo"(
                        "id" IDENTITY NOT NULL PRIMARY KEY,
                        "stockId" CHAR(5),
                        "stockDate" CHAR(10),
                        "stockOpen" NUMBER,
                        "stockHigh" NUMBER,
                        "stockLow" NUMBER,
                        "stockClose" NUMBER,
                        "stockAdjClose" NUMBER,
                        "stockVol" NUMBER,
                        "created" date,
                        "createBy" CHAR(10),
                        "updated" date,
                        "updateBy" CHAR(10),
                        "deleteFlag" BOOLEAN
                        );
                    """

            sqlObject.execute("""DROP TABLE IF EXISTS "StockInfo";""")
            sqlObject.execute(sqlCmdCreateTable)
            sqlObject.execute("""CREATE UNIQUE INDEX ON "StockInfo"("stockId", "stockDate");""")

            CSVReader csvReader =
                    new CSVReader( new InputStreamReader(getClass().getClassLoader().getResourceAsStream("abc.csv"), "UTF-8"))

            //CSVReader csvReader =
            //        new CSVReader( new InputStreamReader(new FileInputStream(csvPath), "UTF-8"))
            List<String[]> list = new ArrayList<>()
            list = csvReader.readAll()
            list.remove(0)
            csvReader.close()

            String stockId, date
            float open, high, low, close, adj, vol

            for(line in list) {
                stockId = "IBM"
                date = line[0]
                open = Double.parseDouble(line[1])
                high = Double.parseDouble(line[2])
                low = Double.parseDouble(line[3])
                close = Double.parseDouble(line[4])
                adj = Double.parseDouble(line[5])
                vol = Double.parseDouble(line[6])

                GString sqlCmdInsert = GString.EMPTY +
                """
                    INSERT INTO "StockInfo"
                    ("stockId", "stockDate", "stockOpen", "stockHigh", "stockLow", "stockClose", "stockAdjClose", "stockVol")
                    VALUES(${stockId}, ${date}, ${open}, ${high}, ${low}, ${close}, ${adj}, ${vol});
                """

                sqlObject.executeInsert(sqlCmdInsert)
            }

            GString sqlCmdSelect = GString.EMPTY +
                    """SELECT * FROM "StockInfo";"""

            ArrayList<GroovyRowResult> groovyRowResultArrayList = sqlObject.rows(sqlCmdSelect)
            StockInfo = groovyRowResultArrayList

        } catch (Exception ex) {
            ex.printStackTrace( )
        } finally {

        }

        response.setContentType("text/html")

        // Hello
        PrintWriter out = response.getWriter()
        out.println("<html>")
        out.println("<head>")
        out.println("<style>table, th, td {  border: 1px solid black;}</style>")
        out.println("</head>")
        out.println("<body>")
        out.println("<h1>" + message + "</h1>")
        out.println("<table>")
        out.println("<thead>")
        out.println("<tr>" +
                "<td>ID</td><td>stock ID</td><td>Date</td><td>Open</td>" +
                "<td>High</td><td>Low</td><td>Close</td>" +
                "<td>Adj Close</td><td>Volume</td>" +
                "<td>created</td><td>createBy</td><td>updated</td><td>updateBy</td>" +
                "</tr>")
        out.println("</thead>")
        out.println("<tbody>")
        for (row in StockInfo) {
            GString tr1 = GString.EMPTY +"<tr>"
            GString tr2 = GString.EMPTY +"<td>${row.id}</td><td>${row.stockId}</td><td>${row.stockDate}</td><td>${row.stockOpen}"
            GString tr3 = GString.EMPTY +"</td><td>${row.stockHigh}</td><td>${row.stockLow}</td><td>${row.stockClose}</td>"
            GString tr4 = GString.EMPTY +"<td>${row.stockAdjCLose}</td><td>${String.format("%f", row.stockVol)}</td>"
            GString tr5 = GString.EMPTY +"<td>${row.created}</td><td>${row.createBy}</td><td>${row.updated}</td><td>${row.updateBy}</td>"
            GString tr6 = GString.EMPTY +"</tr>"
            GString tr = tr1 + tr2 + tr3 + tr4 + tr5 + tr6
            out.println(tr)
        }
        out.println("</tbody>")
        out.println("</table>")
        out.println("</body></html>");
    }

    void destroy() {
    }
}