package app.api.admin;

import app.api.support.JsonRequestHelper;
import app.dao.AdminMembershipDAO;
import app.model.AdminMember;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * API trả về danh sách hội viên và yêu cầu nâng cấp cho trang quản trị.
 */
@WebServlet(name = "AdminMembershipServlet", urlPatterns = "/api/admin/members")
public class AdminMembershipServlet extends HttpServlet {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String filter = req.getParameter("filter");
        AdminMembershipDAO dao = new AdminMembershipDAO();
        try {
            List<AdminMember> members = dao.listMembers(filter);
            JSONArray array = new JSONArray();
            for (AdminMember member : members) {
                array.put(toJson(member));
            }
            resp.getWriter().print(array.toString());
        } catch (SQLException ex) {
            JsonRequestHelper.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DatabaseError", "Database error occurred");
        }
    }

    private JSONObject toJson(AdminMember member) {
        JSONObject json = new JSONObject()
                .put("id", member.getId())
                .put("userName", member.getUserName())
                .put("email", member.getEmail())
                .put("avatarUrl", member.getAvatarUrl())
                .put("accountTier", member.getAccountTier());
        if (member.getVipExpiresAt() != null) {
            json.put("vipExpiresAt", ISO_FORMATTER.format(member.getVipExpiresAt()));
        } else {
            json.put("vipExpiresAt", JSONObject.NULL);
        }
        boolean hasPending = member.getRequestId() != null;
        json.put("hasPendingRequest", hasPending);
        if (hasPending) {
            json.put("requestStatus", member.getRequestStatus());
            json.put("requestNote", member.getRequestNote());
            if (member.getRequestCreatedAt() != null) {
                json.put("requestCreatedAt", ISO_FORMATTER.format(member.getRequestCreatedAt()));
            } else {
                json.put("requestCreatedAt", JSONObject.NULL);
            }
        } else {
            json.put("requestStatus", JSONObject.NULL);
            json.put("requestNote", JSONObject.NULL);
            json.put("requestCreatedAt", JSONObject.NULL);
        }
        return json;
    }
}
