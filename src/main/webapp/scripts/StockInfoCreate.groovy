import com.google.gson.Gson
 import com.google.gson.GsonBuilder
 import groovy.json.JsonOutput
 import groovy.json.JsonSlurper
 import groovy.sql.GroovyRowResult
 import groovy.sql.Sql
 import org.slf4j.Logger
 import org.slf4j.LoggerFactory
 
 import javax.naming.Context
 import javax.naming.InitialContext
 import javax.sql.DataSource
 import java.sql.Connection
 import java.text.SimpleDateFormat
 
 def get_params() {
     def paramFields = request.getParameterValues("request")
     def resultFields = [soetekId: "999", status: -1, error: "_"]
 
     if (paramFields) {
         def _fields = new JsonSlurper().parseText(paramFields[0])
 
         try {
             _fields.entrySet().forEach(entry -> {
                 resultFields.put(entry.key, entry.value)
             })
 
             resultFields.status = 0//W3InfoLib.checkLockerId(resultFields.soetekId, session.getId())
             resultFields.error = ""
         } catch (Exception err) {
             resultFields.status = -1
             resultFields.error = "參數錯誤:" + err.toString() + " ${_fields}"
         }
     }
     return resultFields
 }
 
 def process(paramFields, db, tableName, tableFields, logger) {
     def resultFields = [result: "", status: 1, error: ""]
     //資安檢查
     if (0==1) {
         resultFields.result = ""
         resultFields.status = 1
         resultFields.error = "權限不足"
         return resultFields
     }
     //欄位檢查
     if (0==1) {
         resultFields.result = ""
         resultFields.status = 1
         resultFields.error = "輸入錯誤"
         return resultFields
     }
 
     //資料庫操作
     SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd")
     SimpleDateFormat sdfDatetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
     Date now = new Date()
     Context webContainer
     DataSource dbSource
     Connection dbConn
     Sql sqlObject
     ArrayList<GroovyRowResult> sqlResult = new ArrayList()
 
     //修改MAP，使MAP元素與table欄位的對應
     paramFields.created = now
     paramFields.createBy = paramFields.ui
     paramFields.updated = null
     paramFields.updateBy = null
     paramFields.deleteFlag = null
 
 
 
 
     def sqlCmd =
     """
     INSERT INTO "$tableName"
     ( "${tableFields.join("\", \"")}")
     VALUES(:${tableFields.join(", :")});
     """
     logger.info("SQL Cmd:" + getClass().getName() + sqlCmd)
     try {
         webContainer = new InitialContext()
         dbSource = (DataSource) webContainer.lookup(db)
         dbConn =dbSource.getConnection()
         sqlObject = new Sql(dbConn)
 
         //存入資料庫
         sqlResult = sqlObject.executeInsert(sqlCmd, paramFields)
         def newId = sqlResult[0][0].toString().toInteger().intValue()
         sqlResult = new ArrayList()
 
         //抓取新建立的資料
         sqlCmd =
         """
         select * from "$tableName" where "id" = :id
         """
         sqlResult = sqlObject.rows(sqlCmd, [id:newId])
 
     } catch (Exception ex) {
         ex.printStackTrace( )
         resultFields.result = ""
         resultFields.status = 1
         resultFields.error = ex.getMessage()
         return resultFields
     } finally {
     }
 
     Gson gson = new GsonBuilder().serializeNulls().create()
 
     resultFields.result = gson.toJson(sqlResult)
     resultFields.status = 0
     resultFields.error = ""
     return resultFields
 }
 
 def output(resultFields) {
     response.contentType = 'application/json'
 
     json."response" {
         "status" resultFields.status
         "result" resultFields.result
         "error" resultFields.error
     }
 }
 
 
 if (!session) {
     session = request.getSession(true);
 }
 
 
 //##變動 db / tableName / tableFields / insertFields##
 def db = "java:/comp/env/jdbc/h2mem"
 def tableName = "StockInfo"
 def tableFields = ['id', 'stockId', 'stockDate', 'stockOpen', 'stockHigh', 'stockLow', 'stockClose', 'stockAdjClose', 'stockVol', 'created', 'createBy', 'updated', 'updateBy', 'deleteFlag']
 
 //只須去除id
 //remove id 欄位, 因為id欄位是自動生成
 def insertFields = ['stockId', 'stockDate', 'stockOpen', 'stockHigh', 'stockLow', 'stockClose', 'stockAdjClose', 'stockVol', 'created', 'createBy', 'updated', 'updateBy', 'deleteFlag']
 def logger = LoggerFactory.getLogger(tableName)
 def paramFields = get_params()
 def resultFields = process(paramFields, db, tableName, insertFields, logger)
 
 output(resultFields)