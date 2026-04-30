package payroll;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

// ══════════════════════════════════════════════════════════
//  DBConnection
// ══════════════════════════════════════════════════════════
class DBConnection {
    private static final String URL  = "jdbc:mysql://localhost:3306/payroll_db";
    private static final String USER = "root";
    private static final String PASS = "root";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASS);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver not found", e);
            }
        }
        return connection;
    }
    public static void close() {
        try { if (connection != null) connection.close(); }
        catch (SQLException ignored) {}
    }
}

class Employee {
    private int empId;
    private String empCode, fullName, email, phone;
    private int deptId;
    private String designation, status, bankAccount, panNumber;
    private Date joinDate;
    private double basicSalary, hraPercent, daPercent, pfPercent, taxBracket;

    // Getters & Setters
    public int getEmpId()           { return empId; }
    public String getEmpCode()      { return empCode; }
    public String getFullName()     { return fullName; }
    public String getEmail()        { return email; }
    public String getPhone()        { return phone; }
    public int getDeptId()          { return deptId; }
    public String getDesignation()  { return designation; }
    public String getStatus()       { return status; }
    public String getBankAccount()  { return bankAccount; }
    public String getPanNumber()    { return panNumber; }
    public Date getJoinDate()       { return joinDate; }
    public double getBasicSalary()  { return basicSalary; }
    public double getHraPercent()   { return hraPercent; }
    public double getDaPercent()    { return daPercent; }
    public double getPfPercent()    { return pfPercent; }
    public double getTaxBracket()   { return taxBracket; }

    public void setEmpId(int v)          { empId = v; }
    public void setEmpCode(String v)     { empCode = v; }
    public void setFullName(String v)    { fullName = v; }
    public void setEmail(String v)       { email = v; }
    public void setPhone(String v)       { phone = v; }
    public void setDeptId(int v)         { deptId = v; }
    public void setDesignation(String v) { designation = v; }
    public void setStatus(String v)      { status = v; }
    public void setBankAccount(String v) { bankAccount = v; }
    public void setPanNumber(String v)   { panNumber = v; }
    public void setJoinDate(Date v)      { joinDate = v; }
    public void setBasicSalary(double v) { basicSalary = v; }
    public void setHraPercent(double v)  { hraPercent = v; }
    public void setDaPercent(double v)   { daPercent = v; }
    public void setPfPercent(double v)   { pfPercent = v; }
    public void setTaxBracket(double v)  { taxBracket = v; }

    @Override
    public String toString() {
        return String.format("%-6d %-10s %-25s %-30s %-20s ₹%.0f",
            empId, empCode, fullName, email, designation, basicSalary);
    }
}

class PayrollSlip {
    public int payrollId, empId, payMonth, payYear, absentDays;
    public double basicSalary, hra, da, otherAllowance, grossSalary;
    public double pfDeduction, taxDeduction, otherDeduction, absentDeduction, netSalary;
    public String status;
}


class EmployeeDAO {

    public int addEmployee(Employee e) throws SQLException {
        String sql = """
            INSERT INTO employees
            (emp_code,full_name,email,phone,dept_id,designation,join_date,
             basic_salary,hra_percent,da_percent,pf_percent,tax_bracket,
             bank_account,pan_number)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getEmpCode());
            ps.setString(2, e.getFullName());
            ps.setString(3, e.getEmail());
            ps.setString(4, e.getPhone());
            ps.setInt(5, e.getDeptId());
            ps.setString(6, e.getDesignation());
            ps.setDate(7, new java.sql.Date(e.getJoinDate().getTime()));
            ps.setDouble(8, e.getBasicSalary());
            ps.setDouble(9, e.getHraPercent());
            ps.setDouble(10, e.getDaPercent());
            ps.setDouble(11, e.getPfPercent());
            ps.setDouble(12, e.getTaxBracket());
            ps.setString(13, e.getBankAccount());
            ps.setString(14, e.getPanNumber());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    public Employee getById(int id) throws SQLException {
        String sql = "SELECT * FROM employees WHERE emp_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public List<Employee> getAllActive() throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE status='ACTIVE' ORDER BY full_name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public boolean updateSalary(int empId, double newSalary) throws SQLException {
        String sql = "UPDATE employees SET basic_salary=? WHERE emp_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, newSalary); ps.setInt(2, empId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int empId, String status) throws SQLException {
        String sql = "UPDATE employees SET status=? WHERE emp_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status); ps.setInt(2, empId);
            return ps.executeUpdate() > 0;
        }
    }

    private Employee mapRow(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setEmpId(rs.getInt("emp_id"));
        e.setEmpCode(rs.getString("emp_code"));
        e.setFullName(rs.getString("full_name"));
        e.setEmail(rs.getString("email"));
        e.setPhone(rs.getString("phone"));
        e.setDeptId(rs.getInt("dept_id"));
        e.setDesignation(rs.getString("designation"));
        e.setJoinDate(rs.getDate("join_date"));
        e.setBasicSalary(rs.getDouble("basic_salary"));
        e.setHraPercent(rs.getDouble("hra_percent"));
        e.setDaPercent(rs.getDouble("da_percent"));
        e.setPfPercent(rs.getDouble("pf_percent"));
        e.setTaxBracket(rs.getDouble("tax_bracket"));
        e.setStatus(rs.getString("status"));
        e.setBankAccount(rs.getString("bank_account"));
        e.setPanNumber(rs.getString("pan_number"));
        return e;
    }
}

class LeaveDAO {

    public int applyLeave(int empId, int leaveTypeId,
                          String from, String to, String reason) throws SQLException {
        long days = ChronoUnit.DAYS.between(LocalDate.parse(from), LocalDate.parse(to)) + 1;

        // Check leave balance
        int used = getLeavesUsed(empId, leaveTypeId, LocalDate.parse(from).getYear());
        int max  = getMaxLeaves(leaveTypeId);
        if (used + days > max) {
            System.out.printf("❌ Insufficient leave balance. Used: %d, Max: %d%n", used, max);
            return -1;
        }

        String sql = """
            INSERT INTO leave_requests
            (emp_id, leave_type_id, from_date, to_date, total_days, reason)
            VALUES (?,?,?,?,?,?)
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, empId); ps.setInt(2, leaveTypeId);
            ps.setDate(3, Date.valueOf(from));
            ps.setDate(4, Date.valueOf(to));
            ps.setLong(5, days);
            ps.setString(6, reason);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    public boolean approveRejectLeave(int leaveId, String status, int approvedBy) throws SQLException {
        String sql = "UPDATE leave_requests SET status=?, approved_by=? WHERE leave_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status); ps.setInt(2, approvedBy); ps.setInt(3, leaveId);
            return ps.executeUpdate() > 0;
        }
    }

    public void showLeaveBalance(int empId) throws SQLException {
        String sql = "SELECT lt.leave_name, lt.max_per_year, " +
                     "COALESCE(SUM(CASE WHEN lr.status='APPROVED' THEN lr.total_days ELSE 0 END),0) AS used " +
                     "FROM leave_types lt " +
                     "LEFT JOIN leave_requests lr ON lt.leave_type_id=lr.leave_type_id AND lr.emp_id=? " +
                     "AND YEAR(lr.from_date)=YEAR(CURDATE()) " +
                     "GROUP BY lt.leave_type_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            System.out.printf("%-20s %-10s %-10s %-10s%n","Leave Type","Total","Used","Remaining");
            System.out.println("-".repeat(52));
            while (rs.next()) {
                int max  = rs.getInt("max_per_year");
                int used = rs.getInt("used");
                System.out.printf("%-20s %-10d %-10d %-10d%n",
                    rs.getString("leave_name"), max, used, max - used);
            }
        }
    }

    public void showPendingLeaves() throws SQLException {
        String sql = """
            SELECT lr.leave_id, e.full_name, lt.leave_name,
                   lr.from_date, lr.to_date, lr.total_days, lr.reason, lr.status
            FROM leave_requests lr
            JOIN employees e ON lr.emp_id = e.emp_id
            JOIN leave_types lt ON lr.leave_type_id = lt.leave_type_id
            WHERE lr.status = 'PENDING' ORDER BY lr.applied_on
        """;
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.printf("%-6s %-20s %-15s %-12s %-12s %-5s%n",
                "ID","Employee","Leave Type","From","To","Days");
            System.out.println("-".repeat(72));
            while (rs.next()) {
                System.out.printf("%-6d %-20s %-15s %-12s %-12s %-5d%n",
                    rs.getInt("leave_id"), rs.getString("full_name"),
                    rs.getString("leave_name"), rs.getDate("from_date"),
                    rs.getDate("to_date"), rs.getInt("total_days"));
            }
        }
    }

    private int getLeavesUsed(int empId, int leaveTypeId, int year) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_days),0) FROM leave_requests " +
                     "WHERE emp_id=? AND leave_type_id=? AND status='APPROVED' AND YEAR(from_date)=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, empId); ps.setInt(2, leaveTypeId); ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private int getMaxLeaves(int leaveTypeId) throws SQLException {
        String sql = "SELECT max_per_year FROM leave_types WHERE leave_type_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, leaveTypeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}

class PayrollDAO {

    public int generatePayroll(int empId, int month, int year) throws SQLException {
        // Check if already generated
        String check = "SELECT payroll_id FROM payroll WHERE emp_id=? AND pay_month=? AND pay_year=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(check)) {
            ps.setInt(1, empId); ps.setInt(2, month); ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("⚠️  Payroll already generated for this month.");
                return rs.getInt(1);
            }
        }

        EmployeeDAO empDao = new EmployeeDAO();
        Employee emp = empDao.getById(empId);
        if (emp == null) throw new SQLException("Employee not found.");

        // Count absent days in that month
        String absSql = """
            SELECT COUNT(*) FROM attendance
            WHERE emp_id=? AND status='ABSENT'
            AND MONTH(att_date)=? AND YEAR(att_date)=?
        """;
        int absentDays = 0;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(absSql)) {
            ps.setInt(1, empId); ps.setInt(2, month); ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) absentDays = rs.getInt(1);
        }

        // Salary calculation
        double basic       = emp.getBasicSalary();
        double hra         = basic * emp.getHraPercent() / 100;
        double da          = basic * emp.getDaPercent() / 100;
        double gross       = basic + hra + da;

        // Absent deduction (per working day — assuming 26 working days/month)
        double perDay      = basic / 26.0;
        double absentDeduct= perDay * absentDays;

        // Deductions
        double pf          = basic * emp.getPfPercent() / 100;
        double taxBase     = (gross - pf) * 12; // annualized
        double tax         = (taxBase * emp.getTaxBracket() / 100) / 12;

        double net         = gross - pf - tax - absentDeduct;

        String sql = """
            INSERT INTO payroll
            (emp_id, pay_month, pay_year, basic_salary, hra, da,
             gross_salary, pf_deduction, tax_deduction, absent_days,
             absent_deduction, net_salary)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, empId);    ps.setInt(2, month);   ps.setInt(3, year);
            ps.setDouble(4, basic); ps.setDouble(5, hra);  ps.setDouble(6, da);
            ps.setDouble(7, gross); ps.setDouble(8, pf);   ps.setDouble(9, tax);
            ps.setInt(10, absentDays);
            ps.setDouble(11, absentDeduct);
            ps.setDouble(12, net);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    public PayrollSlip getPayrollSlip(int empId, int month, int year) throws SQLException {
        String sql = "SELECT * FROM payroll WHERE emp_id=? AND pay_month=? AND pay_year=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, empId); ps.setInt(2, month); ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PayrollSlip slip = new PayrollSlip();
                slip.payrollId = rs.getInt("payroll_id");
                slip.empId     = rs.getInt("emp_id");
                slip.payMonth  = rs.getInt("pay_month");
                slip.payYear   = rs.getInt("pay_year");
                slip.basicSalary   = rs.getDouble("basic_salary");
                slip.hra           = rs.getDouble("hra");
                slip.da            = rs.getDouble("da");
                slip.otherAllowance= rs.getDouble("other_allowance");
                slip.grossSalary   = rs.getDouble("gross_salary");
                slip.pfDeduction   = rs.getDouble("pf_deduction");
                slip.taxDeduction  = rs.getDouble("tax_deduction");
                slip.otherDeduction= rs.getDouble("other_deduction");
                slip.absentDays    = rs.getInt("absent_days");
                slip.absentDeduction= rs.getDouble("absent_deduction");
                slip.netSalary     = rs.getDouble("net_salary");
                slip.status        = rs.getString("status");
                return slip;
            }
        }
        return null;
    }

    public boolean markPaid(int payrollId) throws SQLException {
        String sql = "UPDATE payroll SET status='PAID' WHERE payroll_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, payrollId);
            return ps.executeUpdate() > 0;
        }
    }

    public void generateBulkPayroll(int month, int year) throws SQLException {
        EmployeeDAO empDao = new EmployeeDAO();
        List<Employee> emps = empDao.getAllActive();
        int success = 0;
        for (Employee emp : emps) {
            try {
                generatePayroll(emp.getEmpId(), month, year);
                success++;
            } catch (Exception e) {
                System.err.println("Failed for " + emp.getFullName() + ": " + e.getMessage());
            }
        }
        System.out.println("✅ Bulk payroll generated for " + success + "/" + emps.size() + " employees.");
    }
}

class PerformanceDAO {

    public int addReview(int empId, String period, double rating,
                         String remarks, double bonusPercent, int reviewedBy) throws SQLException {
        String sql = """
            INSERT INTO performance_reviews
            (emp_id, review_period, rating, remarks, bonus_percent, reviewed_by, review_date)
            VALUES (?,?,?,?,?,?,CURDATE())
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, empId);       ps.setString(2, period);
            ps.setDouble(3, rating);   ps.setString(4, remarks);
            ps.setDouble(5, bonusPercent); ps.setInt(6, reviewedBy);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    public void showEmployeeReviews(int empId) throws SQLException {
        String sql = """
            SELECT review_period, rating, bonus_percent, remarks, review_date
            FROM performance_reviews WHERE emp_id=? ORDER BY review_date DESC
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            System.out.printf("%-10s %-8s %-10s %-15s %s%n",
                "Period","Rating","Bonus%","Date","Remarks");
            System.out.println("-".repeat(70));
            while (rs.next()) {
                System.out.printf("%-10s %-8.1f %-10.1f %-15s %s%n",
                    rs.getString("review_period"), rs.getDouble("rating"),
                    rs.getDouble("bonus_percent"), rs.getDate("review_date"),
                    rs.getString("remarks"));
            }
        }
    }
}

public class PayrollMain {

    static Scanner sc          = new Scanner(System.in);
    static EmployeeDAO empDao  = new EmployeeDAO();
    static LeaveDAO leaveDao   = new LeaveDAO();
    static PayrollDAO payDao   = new PayrollDAO();
    static PerformanceDAO perfDao = new PerformanceDAO();

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   EMPLOYEE PAYROLL & HR MANAGEMENT   ║");
        System.out.println("╚══════════════════════════════════════╝");

        boolean run = true;
        while (run) {
            printMenu();
            int ch = readInt("Choice: ");
            switch (ch) {
                case 1  -> manageEmployees();
                case 2  -> manageLeaves();
                case 3  -> managePayroll();
                case 4  -> managePerformance();
                case 5  -> markAttendance();
                case 0  -> { run = false; DBConnection.close(); }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    static void printMenu() {
        System.out.println("\n┌────────────────────────────┐");
        System.out.println("│      PAYROLL MENU          │");
        System.out.println("├────────────────────────────┤");
        System.out.println("│ 1. Employee Management     │");
        System.out.println("│ 2. Leave Management        │");
        System.out.println("│ 3. Payroll Processing      │");
        System.out.println("│ 4. Performance Review      │");
        System.out.println("│ 5. Mark Attendance         │");
        System.out.println("│ 0. Exit                    │");
        System.out.println("└────────────────────────────┘");
    }

    static void manageEmployees() {
        System.out.println("\n1.List All  2.Add  3.Update Salary  4.Change Status");
        int c = readInt("Choice: ");
        try {
            if (c == 1) {
                System.out.printf("%-6s %-10s %-25s %-30s %-20s %-10s%n",
                    "ID","Code","Name","Email","Designation","Basic(₹)");
                System.out.println("-".repeat(100));
                empDao.getAllActive().forEach(System.out::println);
            } else if (c == 2) {
                Employee e = new Employee();
                e.setEmpCode(readString("Employee Code: "));
                e.setFullName(readString("Full Name: "));
                e.setEmail(readString("Email: "));
                e.setPhone(readString("Phone: "));
                e.setDeptId(readInt("Dept ID: "));
                e.setDesignation(readString("Designation: "));
                e.setJoinDate(Date.valueOf(readString("Join Date (YYYY-MM-DD): ")));
                e.setBasicSalary(readDouble("Basic Salary: "));
                e.setHraPercent(readDouble("HRA % (default 40): "));
                e.setDaPercent(readDouble("DA % (default 20): "));
                e.setPfPercent(readDouble("PF % (default 12): "));
                e.setTaxBracket(readDouble("Tax % (e.g. 10/20/30): "));
                e.setBankAccount(readString("Bank Account: "));
                e.setPanNumber(readString("PAN Number: "));
                int id = empDao.addEmployee(e);
                System.out.println("✅ Employee added with ID: " + id);
            } else if (c == 3) {
                int id     = readInt("Employee ID: ");
                double sal = readDouble("New Basic Salary: ");
                empDao.updateSalary(id, sal);
                System.out.println("✅ Salary updated.");
            } else if (c == 4) {
                int id = readInt("Employee ID: ");
                System.out.println("Status: ACTIVE / INACTIVE / RESIGNED / TERMINATED");
                String st = readString("New Status: ").toUpperCase();
                empDao.updateStatus(id, st);
                System.out.println("✅ Status updated.");
            }
        } catch (SQLException e) { System.err.println("DB Error: " + e.getMessage()); }
    }

    static void manageLeaves() {
        System.out.println("\n1.Apply Leave  2.Approve/Reject  3.View Balance  4.Pending Requests");
        int c = readInt("Choice: ");
        try {
            if (c == 1) {
                int empId = readInt("Employee ID: ");
                System.out.println("Leave Types: 1=Casual 2=Sick 3=Earned 4=Maternity 5=Paternity");
                int type  = readInt("Leave Type ID: ");
                String from = readString("From (YYYY-MM-DD): ");
                String to   = readString("To   (YYYY-MM-DD): ");
                String reason = readString("Reason: ");
                int id = leaveDao.applyLeave(empId, type, from, to, reason);
                if (id > 0) System.out.println("✅ Leave applied. Request ID: " + id);
            } else if (c == 2) {
                leaveDao.showPendingLeaves();
                int leaveId = readInt("Leave Request ID: ");
                System.out.println("Action: APPROVED / REJECTED");
                String status = readString("Status: ").toUpperCase();
                int by = readInt("Approver Employee ID: ");
                leaveDao.approveRejectLeave(leaveId, status, by);
                System.out.println("✅ Leave " + status.toLowerCase() + ".");
            } else if (c == 3) {
                int empId = readInt("Employee ID: ");
                leaveDao.showLeaveBalance(empId);
            } else if (c == 4) {
                leaveDao.showPendingLeaves();
            }
        } catch (SQLException e) { System.err.println("DB Error: " + e.getMessage()); }
    }

    static void managePayroll() {
        System.out.println("\n1.Generate (Single)  2.Generate (Bulk)  3.View Slip  4.Mark Paid");
        int c = readInt("Choice: ");
        try {
            if (c == 1) {
                int empId = readInt("Employee ID: ");
                int month = readInt("Month (1-12): ");
                int year  = readInt("Year: ");
                int id    = payDao.generatePayroll(empId, month, year);
                System.out.println("✅ Payroll generated. ID: " + id);
                printPaySlip(empId, month, year);
            } else if (c == 2) {
                int month = readInt("Month: ");
                int year  = readInt("Year: ");
                payDao.generateBulkPayroll(month, year);
            } else if (c == 3) {
                int empId = readInt("Employee ID: ");
                int month = readInt("Month: ");
                int year  = readInt("Year: ");
                printPaySlip(empId, month, year);
            } else if (c == 4) {
                int payId = readInt("Payroll ID: ");
                payDao.markPaid(payId);
                System.out.println("✅ Marked as PAID.");
            }
        } catch (SQLException e) { System.err.println("DB Error: " + e.getMessage()); }
    }

    static void printPaySlip(int empId, int month, int year) throws SQLException {
        Employee emp   = empDao.getById(empId);
        PayrollSlip sl = payDao.getPayrollSlip(empId, month, year);
        if (sl == null || emp == null) { System.out.println("Payroll not found."); return; }
        String[] months = {"","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║               PAY SLIP                       ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.printf("║ Name      : %-33s║%n", emp.getFullName());
        System.out.printf("║ Emp Code  : %-33s║%n", emp.getEmpCode());
        System.out.printf("║ Period    : %-33s║%n", months[month] + " " + year);
        System.out.println("╠═══════════════════════╦══════════════════════╣");
        System.out.println("║ EARNINGS              ║ DEDUCTIONS           ║");
        System.out.println("╠═══════════════════════╬══════════════════════╣");
        System.out.printf("║ Basic     : ₹%-9.2f ║ PF        : ₹%-7.2f║%n", sl.basicSalary, sl.pfDeduction);
        System.out.printf("║ HRA       : ₹%-9.2f ║ Tax (TDS) : ₹%-7.2f║%n", sl.hra, sl.taxDeduction);
        System.out.printf("║ DA        : ₹%-9.2f ║ Absent(-) : ₹%-7.2f║%n", sl.da, sl.absentDeduction);
        System.out.printf("║ Other     : ₹%-9.2f ║ Other     : ₹%-7.2f║%n", sl.otherAllowance, sl.otherDeduction);
        System.out.println("╠═══════════════════════╬══════════════════════╣");
        System.out.printf("║ GROSS     : ₹%-9.2f ║ ABSENT DAYS: %-7d║%n", sl.grossSalary, sl.absentDays);
        System.out.println("╠═══════════════════════╩══════════════════════╣");
        System.out.printf("║ NET SALARY        :  ₹%-22.2f║%n", sl.netSalary);
        System.out.printf("║ Status            :  %-22s║%n", sl.status);
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    static void managePerformance() {
        System.out.println("\n1.Add Review  2.View Reviews");
        int c = readInt("Choice: ");
        try {
            if (c == 1) {
                int empId       = readInt("Employee ID: ");
                String period   = readString("Review Period (e.g. Q1-2025): ");
                double rating   = readDouble("Rating (1.0-5.0): ");
                String remarks  = readString("Remarks: ");
                double bonus    = readDouble("Bonus % (0 if none): ");
                int by          = readInt("Reviewer Employee ID: ");
                int id = perfDao.addReview(empId, period, rating, remarks, bonus, by);
                System.out.println("✅ Review added. ID: " + id);
            } else if (c == 2) {
                int empId = readInt("Employee ID: ");
                perfDao.showEmployeeReviews(empId);
            }
        } catch (SQLException e) { System.err.println("DB Error: " + e.getMessage()); }
    }

    static void markAttendance() {
        System.out.println("\n--- MARK ATTENDANCE ---");
        try {
            int empId   = readInt("Employee ID: ");
            String date = readString("Date (YYYY-MM-DD, Enter for today): ");
            if (date.isEmpty()) date = LocalDate.now().toString();
            System.out.println("Status: PRESENT / ABSENT / HALF_DAY / HOLIDAY / LEAVE");
            String status  = readString("Status: ").toUpperCase();
            String checkIn  = status.equals("PRESENT") ? readString("Check-in (HH:MM:SS): ") : null;
            String checkOut = status.equals("PRESENT") ? readString("Check-out (HH:MM:SS): ") : null;

            String sql = """
                INSERT INTO attendance (emp_id, att_date, status, check_in, check_out)
                VALUES (?,?,?,?,?)
                ON DUPLICATE KEY UPDATE status=VALUES(status),
                check_in=VALUES(check_in), check_out=VALUES(check_out)
            """;
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, empId);
                ps.setDate(2, Date.valueOf(date));
                ps.setString(3, status);
                ps.setString(4, checkIn);
                ps.setString(5, checkOut);
                ps.executeUpdate();
                System.out.println("✅ Attendance marked.");
            }
        } catch (SQLException e) { System.err.println("DB Error: " + e.getMessage()); }
    }

    static int readInt(String p)       { System.out.print(p); while(!sc.hasNextInt()) sc.next(); int v=sc.nextInt(); sc.nextLine(); return v; }
    static double readDouble(String p) { System.out.print(p); while(!sc.hasNextDouble()) sc.next(); double v=sc.nextDouble(); sc.nextLine(); return v; }
    static String readString(String p) { System.out.print(p); return sc.nextLine().trim(); }
}
