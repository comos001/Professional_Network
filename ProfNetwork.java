/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 * Kiyomi Sugita - 862097829
 * Victor Omosor - 862075888
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class ProfNetwork {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of ProfNetwork
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public ProfNetwork (String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end ProfNetwork

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
	 if(outputHeader){
	    for(int i = 1; i <= numCol; i++){
		System.out.print(rsmd.getColumnName(i) + "\t");
	    }
	    System.out.println();
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
          List<String> record = new ArrayList<String>();
         for (int i=1; i<=numCol; ++i)
            record.add(rs.getString (i));
         result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            ProfNetwork.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      ProfNetwork esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the ProfNetwork object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new ProfNetwork (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            String password = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
		System.out.println("\n---------");
                System.out.println("MAIN MENU");
                System.out.println("---------");
		System.out.println("0. Search");
                System.out.println("1. Friends");
                System.out.println("2. Profile");
                System.out.println("3. Messages");
		System.out.println("4. Friend Request");
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
		   case 0: Search(esql, authorisedUser); break;
		   case 1: FriendList(esql, authorisedUser, authorisedUser); break;
		   case 2: boolean profile_menu = true;
                           while(profile_menu) {
                              System.out.println("Profile");
                              System.out.println("--------");
                              System.out.println("1. View Profile");
                              System.out.println("2. Update Profile");
                              System.out.println("3. Exit to Main Menu ");
                              switch(readChoice()){
                                 case 1: ViewProfile(esql, authorisedUser); break;
                                 case 2: boolean update_menu = true;
                                          while(update_menu) {
                                             System.out.println("Choose Field to Update");
                                             System.out.println("------------------------");
                                             System.out.println("1. Password");
                                             System.out.println("2. Educational Details");
                                             System.out.println("3. Work Experience");
                                             System.out.println("4. Exit to Profile Menu");
                                             switch(readChoice()){
                                                case 1: UpdatePass(esql, authorisedUser); break;
                                                case 2: boolean edit_edu = true;
                                                         while(edit_edu) {
                                                            System.out.println("Education - Choose Option");
                                                            System.out.println("-----------------------------");
                                                            System.out.println("1. Add New Educational Details");
                                                            System.out.println("2. Update Existing Educational Details");
                                                            System.out.println("3. Delete Existing Educational Details");
                                                            System.out.println("4. View Current Educational Details");
                                                            System.out.println("5. Exit to Update menu");
                                                            switch(readChoice()){
                                                                     case 1: AddEdu(esql, authorisedUser); break;
                                                                     case 2: UpdateEdu(esql, authorisedUser); break;
                                                                     case 3: DeleteEdu(esql, authorisedUser); break;
                                                                     case 4: ViewEdu(esql, authorisedUser); break;
                                                                     case 5: edit_edu = false; break;
                                                            }
                                                         } break;
                                                case 3: boolean edit_exp = true;
                                                         while(edit_exp) {
                                                            System.out.println("Work Experience - Choose Option");
                                                            System.out.println("------------------------");
                                                            System.out.println("1. Add New Work Experience");
                                                            System.out.println("2. Update Existing Work Experience");
                                                            System.out.println("3. Delete Existing Work Experience");
                                                            System.out.println("4. View Current Work Experience");
                                                            System.out.println("5. Exit to Update menu");
                                                            switch(readChoice()){
                                                                     case 1: AddExp(esql, authorisedUser); break;
                                                                     case 2: UpdateExp(esql, authorisedUser); break;
                                                                     case 3: DeleteExp(esql, authorisedUser); break;
                                                                     case 4: ViewExp(esql, authorisedUser); break;
                                                                     case 5: edit_exp = false; break;
                                                            }
                                                         } break;
                                                case 4: update_menu = false; break;
                                             }
                                          } break; 
                                 case 3: profile_menu = false; break;
                              }
                           } break;
                   case 3: boolean message_menu = true;
                           while(message_menu) {
                              System.out.println("Messages - Choose Option");
                              System.out.println("------------------------");
                              System.out.println("1. View Messages");
                              System.out.println("2. Send New Message");
                              System.out.println("3. Exit to Main Menu");
                              switch(readChoice()){
                                       case 1: ViewMessages(esql, authorisedUser); break;
                                       case 2: SendMessages(esql, authorisedUser); break;
                                       case 3: message_menu = false; break;
                              }
                           } break;
                   case 4: boolean request_menu = true;
                           while(request_menu) {
                              System.out.println("Friend Request - Choose Option");
                              System.out.println("------------------------");
                              System.out.println("1. Send Friend Request");
                              System.out.println("2. View Incoming Request");
                              System.out.println("3. Exit to Main Menu");
                              switch(readChoice()){
                                       case 1: SearchandSendRequest(esql, authorisedUser);break;
                                       case 2: ActionRequest(esql, authorisedUser); break;
                                       case 3: request_menu = false; break;
                              }
                           } break;
                   case 9: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("\nPlease make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
   public static void CreateUser(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user email: ");
         String email = in.readLine();

	 //Creating empty contact\block lists for a user
	 String query = String.format("INSERT INTO USR (userId, password, email) VALUES ('%s','%s','%s')", login, password, email);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USR WHERE userId = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

//Rest of the functions definition go in here

   //SEARCH SPACE
   public static void Search(ProfNetwork esql, String authorizedUser) {
      try{
         System.out.println("Enter Username to search: ");
         String search_name = in.readLine();
         String query = String.format("Select * from usr where userId = '%s'", search_name);
         int isValid = esql.executeQuery(query);
         if (isValid > 0){
            query = String.format("Select C.connectionid From USR U, CONNECTION_USR C Where U.userId = C.userId and C.userId = '%s' and C.connectionId = '%s' and C.status = 'Accept'", authorizedUser, search_name);
            int isFriend = esql.executeQuery(query);
            int userNum;

            if (isFriend == 0){
               System.out.println("\nProfile");
               System.out.println("--------- ");
               query = String.format("Select U.name From USR U Where U.userid = '%s'", search_name);
               userNum = esql.executeQueryAndPrintResult(query);
               System.out.println("---------");
               System.out.println("Education");
               System.out.println("---------");
               query = String.format("Select instituitionname, major, degree, startdate, enddate From educational_details E Where E.userId = '%s'", search_name);
               userNum = esql.executeQueryAndPrintResult(query);
               System.out.println("---------------");
               System.out.println("Work Experience");
               System.out.println("---------------");
               query = String.format("Select company, role, location, startDate, endDate From work_expr W Where W.userId = '%s'", search_name);
               userNum = esql.executeQueryAndPrintResult(query);
            }
            else {
               System.out.println("\nProfile");
               System.out.println("--------- ");
               query = String.format("Select name, dateOfBirth From USR U Where U.userid = '%s'", search_name);
               userNum = esql.executeQueryAndPrintResult(query);
               System.out.println("---------");
               System.out.println("Education");
               System.out.println("---------");
               query = String.format("Select instituitionname, major, degree, startdate, enddate From educational_details E Where E.userId = '%s'", search_name);
               userNum = esql.executeQueryAndPrintResult(query);
               System.out.println("---------------");
               System.out.println("Work Experience");
               System.out.println("---------------");
               query = String.format("Select company, role, location, startDate, endDate From work_expr W Where W.userId = '%s'", search_name);
               userNum = esql.executeQueryAndPrintResult(query);
            }
         }
         else {
            System.out.println("User account does not exist");
         }
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }
   }



   //FRIEND LIST
   public static void FriendList(ProfNetwork esql, String originalUser, String authorizedUser) {
      try{
         String query = String.format("Select connectionId from connection_usr where userId = '%s' and status = 'Accept' union Select userId from connection_usr where connectionId = '%s' and status = 'Accept'", authorizedUser, authorizedUser);
         int userNum = esql.executeQueryAndPrintResult(query);
         int empty_check = esql.executeQuery(query);
         if (empty_check == 0){
                System.out.println("No friends");
         }
         else{
            System.out.println("Select Profile to View: ");
            String friend_prof = in.readLine();
            query = String.format("Select connectionId from connection_usr where userId = '%s' and status = 'Accept' union Select userId from connection_usr where connectionId = '%s' and status = 'Accept'", friend_prof, friend_prof);
            int isFriend = esql.executeQuery(query);
            if (isFriend == 0){
               System.out.println("Entry not in Friends List!");
            }
            else {
               ViewProfile(esql, friend_prof);
               boolean friend_menu = true;
               while(friend_menu){
                  System.out.println("Options");
                  System.out.println("------------------");
                  System.out.println("1. View Friends");
                  System.out.println("2. Send Connection Request (Invalid over Connection level 3)");
                  System.out.println("3. Return");
                  switch(readChoice()){
                     case 1: FriendList(esql, originalUser, friend_prof); break;
                     case 2: SendRequest(esql, originalUser, friend_prof); break;
                     case 3: friend_menu = false; break;
                  }
               }
            }
         }
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }
   }

   //PROFILE

   //viewProfile

   public static void ViewProfile(ProfNetwork esql, String authorizedUser) {
      try{
         String query = String.format("Select userId, name, email, dateOfBirth From USR U Where U.userid = '%s'", authorizedUser);
         int userNum = esql.executeQueryAndPrintResult(query);
         query = String.format("Select instituitionname, major, degree, startdate, enddate From educational_details E Where E.userId = '%s'", authorizedUser);
         userNum = esql.executeQueryAndPrintResult(query);
         query = String.format("Select company, role, location, startDate, endDate From work_expr W Where W.userId = '%s'", authorizedUser);
         userNum = esql.executeQueryAndPrintResult(query);
         }
      catch(Exception e) {
         System.err.println (e.getMessage ());
      }
   }

   //updateProfile
   
   //Password
   public static void UpdatePass(ProfNetwork esql, String authorizedUser) {
      try{
         System.out.println("Enter old password: ");
         String oldPass = in.readLine();
         System.out.println("Enter new password: ");
         String newPass = in.readLine();
         String query = String.format("Update USR Set password = '%s' Where userId = '%s' and password = '%s'", newPass, authorizedUser, oldPass);

         esql.executeUpdate(query);
      }catch(Exception e) {
         System.err.println (e.getMessage());
      }
   }

   
   //EDUCATION
   
   //Add Educational Details
   public static void AddEdu(ProfNetwork esql, String authorizedUser) {
         try{
            System.out.println("Enter Institution Name: ");
            String iname = in.readLine();
            System.out.println("Enter Major: ");
            String major = in.readLine();
            System.out.println("Enter Degree: ");
            String degree = in.readLine();
            System.out.println("Enter StartDate(YYYY-MM-DD): ");
            String sdate = in.readLine();
            System.out.println("Enter EndDate(YYYY-MM-DD): ");
            String edate = in.readLine();
	    System.out.println("\n");
            String query = String.format("INSERT INTO educational_details (userId, instituitionName, major, degree, startdate, enddate) VALUES ('%s', '%s', '%s', '%s', '%s', '%s')", authorizedUser, iname, major, degree, sdate, edate);
            esql.executeUpdate(query);
	    String query1 = String.format("SELECT * FROM educational_details E where E.userid = '%s'", authorizedUser);
	    int userNum = esql.executeQueryAndPrintResult(query1);	    
         }catch(Exception e) {
            System.err.println (e.getMessage());
         }
      }

   //Update Educational Details
   public static void UpdateEdu(ProfNetwork esql, String authorizedUser) {
         try{
            String query1 = String.format("SELECT ROW_NUMBER() OVER(ORDER BY startdate) AS row_num, instituitionname, major, degree, startdate, enddate FROM educational_details E WHERE E.userId = '%s'", authorizedUser);
            int userNum = esql.executeQueryAndPrintResult(query1);

            System.out.println("\n"); 
            System.out.println("Choose which Row to edit: ");
            String rownum = in.readLine();
            String editquery;
            boolean edit_menu = true;
	    while(edit_menu){
	    System.out.println("\n");
            System.out.println("------------------------------");
            System.out.println("Choose which attribute to edit: ");
	    System.out.println("------------------------------");
            System.out.println("1. Institution Name");
            System.out.println("2. Major");
            System.out.println("3. Degree");
            System.out.println("4. Start Date");
            System.out.println("5. End Date");
            System.out.println("6. Exit to Education menu");
           
               switch(readChoice()){
 		  case 1:
                     System.out.println("Enter New Institution: ");
                     String iname = in.readLine();
                     editquery = String.format("WITH E1 as (Select userId, instituitionname, major, degree, startdate, enddate FROM(select ROW_NUMBER() OVER(ORDER BY startdate) AS row_num, userId, instituitionname, major, degree, startdate, enddate FROM educational_details AS E2 WHERE E2.userId = '%s') AS derived WHERE row_num = '%s') UPDATE educational_details SET instituitionName = '%s' FROM E1 WHERE educational_details.userId = E1.userId and educational_details.instituitionName = E1.instituitionName and educational_details.major = E1.major and educational_details.degree = E1.degree and educational_details.startdate = E1.startdate and educational_details.enddate = E1.endDate", authorizedUser, rownum, iname);
                     esql.executeUpdate(editquery);
                  break;
                  case 2:
                     System.out.println("Enter New Major: ");
                     String major = in.readLine();
                     editquery = String.format("WITH E1 as (Select userId, instituitionname, major, degree, startdate, enddate FROM(select ROW_NUMBER() OVER(ORDER BY startdate) AS row_num, userId, instituitionname, major, degree, startdate, enddate FROM educational_details AS E2 WHERE E2.userId = '%s') AS derived WHERE row_num = '%s') UPDATE educational_details SET major = '%s' FROM E1 WHERE educational_details.userId = E1.userId and educational_details.instituitionName = E1.instituitionName and educational_details.major = E1.major and educational_details.degree = E1.degree and educational_details.startdate = E1.startdate and educational_details.enddate = E1.endDate", authorizedUser, rownum, major);
                     esql.executeUpdate(editquery);
                  break;
                  case 3:
                     System.out.println("Enter New Degree: ");
                     String degree = in.readLine();
                     editquery = String.format("WITH E1 as (Select userId, instituitionname, major, degree, startdate, enddate FROM(select ROW_NUMBER() OVER(ORDER BY startdate) AS row_num, userId, instituitionname, major, degree, startdate, enddate FROM educational_details AS E2 WHERE E2.userId = '%s') AS derived WHERE row_num = '%s') UPDATE educational_details SET degree = '%s' FROM E1 WHERE educational_details.userId = E1.userId and educational_details.instituitionName = E1.instituitionName and educational_details.major = E1.major and educational_details.degree = E1.degree and educational_details.startdate = E1.startdate and educational_details.enddate = E1.endDate", authorizedUser, rownum, degree);
                     esql.executeUpdate(editquery);
                  break;
                  case 4:
                     System.out.println("Enter New Start Date(YYYY-MM-DD): ");
                     String sdate = in.readLine();
                     editquery = String.format("WITH E1 as (Select userId, instituitionname, major, degree, startdate, enddate FROM(select ROW_NUMBER() OVER(ORDER BY startdate) AS row_num, userId, instituitionname, major, degree, startdate, enddate FROM educational_details AS E2 WHERE E2.userId = '%s') AS derived WHERE row_num = '%s') UPDATE educational_details SET startdate = '%s' FROM E1 WHERE educational_details.userId = E1.userId and educational_details.instituitionName = E1.instituitionName and educational_details.major = E1.major and educational_details.degree = E1.degree and educational_details.startdate = E1.startdate and educational_details.enddate = E1.endDate", authorizedUser, rownum, sdate);
                     esql.executeUpdate(editquery);
                  break;
                  case 5:
                     System.out.println("Enter New End Date(YYYY-MM-DD): ");
                     String edate = in.readLine();
                     editquery = String.format("WITH E1 as (Select userId, instituitionname, major, degree, startdate, enddate FROM(select ROW_NUMBER() OVER(ORDER BY startdate) AS row_num, userId, instituitionname, major, degree, startdate, enddate FROM educational_details AS E2 WHERE E2.userId = '%s') AS derived WHERE row_num = '%s') UPDATE educational_details SET enddate = '%s' FROM E1 WHERE educational_details.userId = E1.userId and educational_details.instituitionName = E1.instituitionName and educational_details.major = E1.major and educational_details.degree = E1.degree and educational_details.startdate = E1.startdate and educational_details.enddate = E1.endDate", authorizedUser, rownum, edate);
                     esql.executeUpdate(editquery);
                  break;
		  case 6: edit_menu = false;
                  break;
               }
            }
            
         }catch(Exception e) {
            System.err.println (e.getMessage());
         }
      }
 

    //Delete Educational Details  
   public static void DeleteEdu(ProfNetwork esql, String authorizedUser) {
      try{
         String query1 = String.format("SELECT ROW_NUMBER() OVER(ORDER BY startdate) AS row_num, instituitionname, major, degree, startdate, enddate FROM educational_details E WHERE E.userId = '%s'", authorizedUser);
         int userNum = esql.executeQueryAndPrintResult(query1);
         System.out.println("Choose which Row to delete: ");
         String rownum = in.readLine();
         String editquery = String.format("WITH E1 as (Select userId, instituitionname, major, degree, startdate, enddate FROM(select ROW_NUMBER() OVER(ORDER BY startdate) AS row_num, userId, instituitionname, major, degree, startdate, enddate FROM educational_details AS E2 WHERE E2.userId = '%s') AS derived WHERE row_num = '%s') DELETE FROM educational_details USING E1 WHERE educational_details.userId = E1.userId and educational_details.instituitionName = E1.instituitionName and educational_details.major = E1.major and educational_details.degree = E1.degree and educational_details.startdate = E1.startdate and educational_details.enddate = E1.endDate", authorizedUser, rownum);
         esql.executeUpdate(editquery);
      }catch(Exception e) {
         System.err.println (e.getMessage());
      }
   }


    //View Educational Details 
   public static void ViewEdu(ProfNetwork esql, String authorizedUser) {
      try{
         String query = String.format("SELECT ROW_NUMBER() OVER(ORDER BY startdate) AS row_num, instituitionname, major, degree, startdate, enddate FROM educational_details E WHERE E.userId = '%s'", authorizedUser);
         int usernum = esql.executeQueryAndPrintResult(query);
      }catch(Exception e) {
         System.err.println (e.getMessage());
      }
   }


//WORK EXPERIENCE 
    
    //Add work experience 
    public static void AddExp(ProfNetwork esql, String authorizedUser) {
      try{
         System.out.println("Enter Company Name: ");
         String company = in.readLine();
         System.out.println("Enter Role: ");
         String role = in.readLine();
         System.out.println("Enter Location: ");
         String location = in.readLine();
         System.out.println("Enter StartDate(YYYY-MM-DD): ");
         String sdate = in.readLine();
         System.out.println("Enter EndDate(YYYY-MM-DD): ");
         String edate = in.readLine();
         String query = String.format("INSERT INTO work_expr (userId, company, role, location, startDate, endDate) VALUES ('%s', '%s', '%s', '%s', '%s', '%s')", authorizedUser, company, role, location, sdate, edate);
         esql.executeUpdate(query);
	 String query1 = String.format("SELECT * FROM work_expr W where W.userid = '%s'", authorizedUser);
         int userNum = esql.executeQueryAndPrintResult(query1);
      }catch(Exception e) {
         System.err.println (e.getMessage());
      }
   } 
 
   //Update work experience
   public static void UpdateExp(ProfNetwork esql, String authorizedUser) {
      try{
         System.out.println("\n");
         String query1 = String.format("SELECT ROW_NUMBER() OVER(ORDER BY startDate) AS row_num, company, role, location, startDate, endDate FROM work_expr E WHERE E.userId = '%s'", authorizedUser);
         int userNum = esql.executeQueryAndPrintResult(query1);
	 System.out.println("\n");
         System.out.println("Choose which Row to edit: ");
         String rownum = in.readLine();
         String editquery;
         boolean edit_menu = true;
         while(edit_menu){
         System.out.println("\n");
         System.out.println("------------------------------");
         System.out.println("Choose which attribute to edit: ");
         System.out.println("------------------------------");
         System.out.println("1. Company Name");
         System.out.println("2. Role");
         System.out.println("3. Location");
         System.out.println("4. Start Date");
         System.out.println("5. End Date");
         System.out.println("6. Exit to Work Experience menu");

            switch(readChoice()){
               case 1:
                  System.out.println("Enter New Company: ");
                  String company = in.readLine();
                  editquery = String.format("WITH E1 as (Select userId, company, role, location, startDate, endDate FROM(select ROW_NUMBER() OVER(ORDER BY startDate) AS row_num, userId, company, role, location, startDate, endDate FROM work_expr AS E2 WHERE E2.userId = '%s') AS derived WHERE row_num = '%s') UPDATE work_expr SET company = '%s' FROM E1 WHERE work_expr.userId = E1.userId and work_expr.company = E1.company and work_expr.role = E1.role and work_expr.location = E1.location and work_expr.startDate = E1.startDate and work_expr.endDate = E1.endDate", authorizedUser, rownum, company);
                  esql.executeUpdate(editquery);
               break;
               case 2:
                  System.out.println("Enter New Role: ");
                  String role = in.readLine();
                  editquery = String.format("WITH E1 as (Select userId, company, role, location, startDate, endDate FROM(select ROW_NUMBER() OVER(ORDER BY startDate) AS row_num, userId, company, role, location, startDate, endDate FROM work_expr AS E2 WHERE E2.userId = '%s') AS derived WHERE row_num = '%s') UPDATE work_expr SET role = '%s' FROM E1 WHERE work_expr.userId = E1.userId and work_expr.company = E1.company and work_expr.role = E1.role and work_expr.location = E1.location and work_expr.startDate = E1.startDate and work_expr.endDate = E1.endDate", authorizedUser, rownum, role);
                  esql.executeUpdate(editquery);
               break;
               case 3:
                  System.out.println("Enter New Location: ");
                  String location = in.readLine();
                  editquery = String.format("WITH E1 as (Select userId, company, role, location, startDate, endDate FROM(select ROW_NUMBER() OVER(ORDER BY startDate) AS row_num, userId, company, role, location, startDate, endDate FROM work_expr AS E2 WHERE E2.userId = '%s') AS derived WHERE row_num = '%s') UPDATE work_expr SET location = '%s' FROM E1 WHERE work_expr.userId = E1.userId and work_expr.company = E1.company and work_expr.role = E1.role and work_expr.location = E1.location and work_expr.startDate = E1.startDate and work_expr.endDate = E1.endDate", authorizedUser, rownum, location);
                  esql.executeUpdate(editquery);
               break;
               case 4:
                  System.out.println("Enter New Start Date(YYYY-MM-DD): ");
                  String sdate = in.readLine();
                  editquery = String.format("WITH E1 as (Select userId, company, role, location, startDate, endDate FROM(select ROW_NUMBER() OVER(ORDER BY startDate) AS row_num, userId, company, role, location, startDate, endDate FROM work_expr AS E2 WHERE E2.userId = '%s') AS derived WHERE row_num = '%s') UPDATE work_expr SET startDate = '%s' FROM E1 WHERE work_expr.userId = E1.userId and work_expr.company = E1.company and work_expr.role = E1.role and work_expr.location = E1.location and work_expr.startDate = E1.startDate and work_expr.endDate = E1.endDate", authorizedUser, rownum, sdate);
                  esql.executeUpdate(editquery);
               break;
               case 5:
                  System.out.println("Enter New End Date(YYYY-MM-DD): ");
                  String edate = in.readLine();
                  editquery = String.format("WITH E1 as (Select userId, company, role, location, startDate, endDate FROM(select ROW_NUMBER() OVER(ORDER BY startDate) AS row_num, userId, company, role, location, startDate, endDate FROM work_expr AS E2 WHERE E2.userId = '%s') AS derived WHERE row_num = '%s') UPDATE work_expr SET endDate = '%s' FROM E1 WHERE work_expr.userId = E1.userId and work_expr.company = E1.company and work_expr.role = E1.role and work_expr.location = E1.location and work_expr.startDate = E1.startDate and work_expr.endDate = E1.endDate", authorizedUser, rownum, edate);
                  esql.executeUpdate(editquery);
               break;
               case 6: edit_menu = false;
               break;
            }
         }

      }catch(Exception e) {
         System.err.println (e.getMessage());
      }
   }
   

   //Delete work experience
   public static void DeleteExp(ProfNetwork esql, String authorizedUser) {
      try{
         String query1 = String.format("SELECT ROW_NUMBER() OVER(ORDER BY startDate) AS row_num, company, role, location, startDate, endDate FROM work_expr E WHERE E.userId = '%s'", authorizedUser);
         int userNum = esql.executeQueryAndPrintResult(query1);
         System.out.println("Choose which Row to delete: ");
         String rownum = in.readLine();
         String editquery = String.format("WITH E1 as (Select userId, company, role, location, startDate, endDate FROM(select ROW_NUMBER() OVER(ORDER BY startDate) AS row_num, userId, company, role, location, startDate, endDate FROM work_expr AS E2 WHERE E2.userId = '%s') AS derived WHERE row_num = '%s') DELETE FROM work_expr USING E1 WHERE work_expr.userId = E1.userId and work_expr.company = E1.company and work_expr.role = E1.role and work_expr.location = E1.location and work_expr.startDate = E1.startDate and work_expr.endDate = E1.endDate", authorizedUser, rownum);
         esql.executeUpdate(editquery);
      }catch(Exception e) {
         System.err.println (e.getMessage());
      }
   }
    

   //View work experience
   public static void ViewExp(ProfNetwork esql, String authorizedUser) {
      try{
         String query = String.format("SELECT ROW_NUMBER() OVER(ORDER BY startDate) AS row_num, company, role, location, startDate, endDate FROM work_expr E WHERE E.userId = '%s'", authorizedUser);
         int usernum = esql.executeQueryAndPrintResult(query);
      }catch(Exception e) {
         System.err.println (e.getMessage());
      }
   }





   //end of update and profile menu
   public static void NewMessage(ProfNetwork esql){
      try{
         System.out.println("In the works");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }

   //REQUEST MENU
   //send request
   //search and send
   
   public static void SearchandSendRequest(ProfNetwork esql, String authorizedUser){
      try{
         System.out.println("\nEnter Username to Request: ");
         String username = in.readLine();
         SendRequest(esql, authorizedUser, username);
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }

   public static void SendRequest(ProfNetwork esql, String authorizedUser, String receiverId){
      try{
         String dropViews = "drop view if exists A, U, W, M, N";
         esql.executeUpdate(dropViews);
         String create_A = String.format("create view A as Select connectionId from CONNECTION_USR where userId = '%s' and status = 'Accept' union Select userId from CONNECTION_USR where connectionId = '%s' and status = 'Accept'", authorizedUser, authorizedUser);
         esql.executeUpdate(create_A);
         String create_U = String.format("create view U as Select C.userId from CONNECTION_USR C, A where C.connectionId = A.connectionId and C.status = 'Accept' and  C.connectionId <> '%s' and C.userId <> '%s'", authorizedUser, authorizedUser);
         esql.executeUpdate(create_U);
         String create_W = String.format("create view W as Select C.userId from CONNECTION_USR C, U where C.connectionId = U.userId and C.status = 'Accept' and  C.connectionId <> '%s'", authorizedUser);
         esql.executeUpdate(create_W);
         String create_M = "create view M as Select * from A union Select * from U";
         esql.executeUpdate(create_M);
         String create_N = "create view N as Select * from M union select * from W";
         esql.executeUpdate(create_N);
         String query = String.format("Select * from N where connectionId = '%s'", receiverId); 
         int userNum = esql.executeQuery(query);
         if (userNum == 1){
            String send_request = String.format("Insert into connection_usr Values('%s', '%s', 'Request')", authorizedUser, receiverId);
            esql.executeUpdate(send_request);
            send_request = String.format("\nRequest to '%s' sent!", receiverId);
            System.out.println(send_request);
         }
         else {
            System.out.println("User is beyond connection level");
         }
        esql.executeUpdate(dropViews);
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }   
   //accept or reject request
   public static void ActionRequest(ProfNetwork esql, String authorizedUser){
      try{
         System.out.println("-----------------------------");
         String query1 = String.format("select ROW_NUMBER() OVER(ORDER BY userId) AS row_num, userId, status FROM connection_usr AS E2 WHERE E2.connectionId = '%s' and E2.status = 'Request'", authorizedUser);
         int userNum = esql.executeQueryAndPrintResult(query1);
	 userNum = esql.executeQuery(query1);

         if (userNum != 0){
            boolean retry = true;
            do {
	       if (userNum == 0){break;} 
               System.out.println("\nChoose Username to accept or reject(Type 'back' return to Friend Request Menu): ");
               String sender = in.readLine();
	       String validkey = String.format("select ROW_NUMBER() OVER(ORDER BY userId) AS row_num, userId, status FROM connection_usr AS E2 WHERE E2.userId = '%s' and E2.status = 'Request'", sender);
               int isValid = esql.executeQuery(validkey);

               if (isValid > 0){
		  System.out.println("\nOptions");
                  System.out.println("---------");
                  System.out.println("1. Accept");
                  System.out.println("2. Reject");
                  switch(readChoice()){
                        case 1: query1 = String.format("Update Connection_Usr Set status = 'Accept' Where connectionId = '%s' and userId = '%s' and status = 'Request'", authorizedUser, sender);
                              esql.executeUpdate(query1);
			      System.out.println("Friend Accepted!");
                              break;
                        case 2: query1 = String.format("Update Connection_Usr Set status = 'Reject' Where connectionId = '%s' and userId = '%s' and status = 'Request'", authorizedUser, sender);
                              esql.executeUpdate(query1);
                              break;
                  }
		  System.out.println("-----------------------------");
         	  query1 = String.format("select ROW_NUMBER() OVER(ORDER BY userId) AS row_num, userId, status FROM connection_usr AS E2 WHERE E2.connectionId = '%s' and E2.status = 'Request'", authorizedUser);
                  userNum = esql.executeQueryAndPrintResult(query1);	  
               }
               else if(sender.equalsIgnoreCase("back")){
                  retry = false; 
               }
               else{
                  System.out.println("Invalid Input please try again");
               }  
            }
            while(retry);
	    System.out.println("You have no outstanding requests");
         }
         else {
            System.out.println("You have no outstanding requests"); 
         }

      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }
  

  //Messages
   public static void ViewMessages(ProfNetwork esql, String authorizedUser){
      try{
         String query;
         int userNum;
         boolean send_receive = true;
         while(send_receive){
            System.out.println("\n1. View Outgoing Messages");
            System.out.println("2. View Unread Messages");
            System.out.println("3. View Read Messages");
            System.out.println("4. Return to Message Menu");
	    switch(readChoice()){
	       case 1: query = String.format("Select msgId, receiverId, contents, sendTime from message where senderId = '%s' and deleteStatus <> 1 and status <> 'Draft' intersect Select msgId, receiverId, contents, sendTime from message where senderId = '%s' and deleteStatus <> 3 and status <> 'Draft' Order by sendTime", authorizedUser, authorizedUser);
                       userNum = esql.executeQueryAndPrintResult(query); 
                       if (userNum != 0){
                           System.out.println("1. Delete Message");
			   System.out.println("2. Return");
                           String input = in.readLine();
                           if (input.equals("1")){
                              System.out.println("Input Message ID to delete: ");
                              input = in.readLine();
                              query = String.format("Update message set deleteStatus = 1 where msgId = '%s' and deleteStatus = 0 and senderId = '%s'", input, authorizedUser);
                              esql.executeUpdate(query);
                              query = String.format("Update message set deleteStatus = 3 where msgId = '%s' and deleteStatus = 2 and senderId = '%s'", input, authorizedUser);
                              esql.executeUpdate(query);
                              System.out.println("Deletion Successful!");
                           }
                           else {
                              System.out.println("Invalid Option (loop later)");
                           }
                       }
                       else{
                        System.out.println("No Outgoing messages");
                       }
                        break;
	       case 2: query = String.format("Select msgId, senderId, sendTime from message where receiverId = '%s' and deleteStatus <> 2 and status = 'Delivered' intersect Select msgId, senderId, sendTime from message where receiverId = '%s' and deleteStatus <> 3 and status = 'Delivered' Order by sendTime", authorizedUser, authorizedUser);
                       userNum = esql.executeQueryAndPrintResult(query); 
                       if (userNum != 0){
                           System.out.println("\n1. View Message");
                           System.out.println("2. Delete Message");
                           System.out.println("3. Return");
			   String input = in.readLine();
                           if (input.equals("1")){
                              System.out.println("Input Message ID to open: ");
                              input = in.readLine();
                              query = String.format("Select contents from message where msgId = '%s' and receiverId = '%s' and deleteStatus <> 2 and status = 'Delivered' intersect Select contents from message where msgId = '%s' and receiverId = '%s' and deleteStatus <> 3 and status = 'Delivered'", input, authorizedUser, input, authorizedUser);
                              userNum = esql.executeQueryAndPrintResult(query);
                              query = String.format("Update message set status = 'Read' where msgId = '%s' and receiverId = '%s'", input, authorizedUser);
                              esql.executeUpdate(query);
                           }
                           else if (input.equals("2")){
                              System.out.println("Input Message ID to delete: ");
                              input = in.readLine();
                              query = String.format("Update message set deleteStatus = 2 where msgId = '%s' and deleteStatus = 0 and receiverId = '%s'", input, authorizedUser);
                              esql.executeUpdate(query);
                              query = String.format("Update message set deleteStatus = 3 where msgId = '%s' and deleteStatus = 1 and receiverId = '%s'", input, authorizedUser);
                              esql.executeUpdate(query);
			      System.out.println("Deletion Successful!");
                           }
                           else {
                              System.out.println("Invalid Option (loop later)");
                           }
                       }
                        else {
                           System.out.println("No unread messages");
                        }                         
                       break;
	       case 3: query = String.format("Select msgId, senderId, contents, sendTime from message where receiverId = '%s' and deleteStatus <> 2 and status = 'Read' intersect Select msgId, senderId, contents, sendTime from message where receiverId = '%s' and deleteStatus <> 3 and status = 'Read' Order by sendTime", authorizedUser, authorizedUser);
                       userNum = esql.executeQueryAndPrintResult(query);
                       if (userNum != 0){
                           System.out.println("1. Delete Message\n");
			   System.out.println("2. Return");
                           String input = in.readLine();
                           if (input.equals("1")){
                              System.out.println("Input Message ID to delete: ");
                              input = in.readLine();
                              query = String.format("Update message set deleteStatus = 2 where msgId = '%s' and deleteStatus = 0 and receiverId = '%s'", input, authorizedUser);
                              esql.executeUpdate(query);
                              query = String.format("Update message set deleteStatus = 3 where msgId = '%s' and deleteStatus = 1 and receiverId = '%s'", input, authorizedUser);
                              esql.executeUpdate(query);
                              System.out.println("Deletion Successful!");
                           }
                           else {
                              System.out.println("Invalid Option (loop later)");
                           }
                       }
                       else{
                        System.out.println("No Read messages");
                       }
                        break;
               case 4: send_receive = false; break;
            }
         }
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }

   public static void SendMessages(ProfNetwork esql, String authorizedUser){
      try{  
         boolean send_messages = true;
         while (send_messages){
            System.out.println("1. Compose New Message");
            System.out.println("2. View Drafts");
            System.out.println("3. Return");
            switch(readChoice()){
               case 1: System.out.println("Input receiver userId: ");
                       String input = in.readLine();
                       String query = String.format("Select * from usr where userId = '%s'", input);
                       int isValid = esql.executeQuery(query);
                       if (isValid > 0){
                           System.out.println("\nWrite New Message:\n");
                           String message = in.readLine();
                           if (!message.isEmpty()){
                              System.out.println("1. Send Message");
                              System.out.println("2. Send to Drafts");
                              System.out.println("press any other key to exit");
                              String menu_input = in.readLine();
                              if (menu_input.equals("1")){
                                 query = "Select COUNT(*) from Message";
                                 List<List<String>> curr_size = esql.executeQueryAndReturnResult(query);
				 int max = Integer.parseInt(curr_size.get(0).get(0));
                                 System.out.println("current size is:" + max);
                                 query = String.format("Insert into Message Values('%s', '%s', '%s', '%s', current_timestamp, 0, 'Sent')", max + 2, authorizedUser, input, message);
                                 esql.executeUpdate(query);
                                 System.out.println("Message successfully sent!");
                              }
                              else if (menu_input.equals("2")){
				 query = "Select COUNT(*) from Message";
                                 List<List<String>> curr_size = esql.executeQueryAndReturnResult(query);
                                 int max = Integer.parseInt(curr_size.get(0).get(0));
                                 query = String.format("Insert into Message Values('%s', '%s', '%s', '%s', current_timestamp, 0, 'Draft')", max + 2, authorizedUser, input, message);
                                 esql.executeUpdate(query);
                                 System.out.println("Successfully drafted!");
                              }
                           }
                           else {
                              System.out.println("Invalid Message");
                           }
                        }
                        else{
                           System.out.println("Invalid Username");
                        }
                        break; 

               case 2:  query = String.format("Select msgId, receiverId, contents, sendTime from message where senderId = '%s' and deleteStatus <> 1 and status = 'Draft'", authorizedUser);
                        int userNum = esql.executeQueryAndPrintResult(query);        
                        if (userNum != 0){
                           System.out.println("\n1. Send Draft");
                           System.out.println("2. Delete Draft");
			   System.out.println("3. Return\n");
                           input = in.readLine();
                           if (input.equals("1")){
                              System.out.println("Input Message ID to Send: ");
                              input = in.readLine();
                              query = String.format("Update message set status = 'Sent' where msgId = '%s' and senderId = '%s'", input, authorizedUser);
                              esql.executeUpdate(query);
                           }  
                           else if (input.equals("2")){
                              System.out.println("Input Message ID to delete: ");
                              input = in.readLine();
                              query = String.format("Update message set deleteStatus = 1 where msgId = '%s' and deleteStatus = 0 and senderId = '%s'", input, authorizedUser);
                              esql.executeUpdate(query);
                              System.out.println("Deletion Successful!");
                           }  
                           else {
				break;
                           }  
                       }   
                        else {
                           System.out.println("No drafted messages");
                        }  
                       break;
		case 3: send_messages = false; break;
            }          
         }  
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }
}//end ProfNetwork
