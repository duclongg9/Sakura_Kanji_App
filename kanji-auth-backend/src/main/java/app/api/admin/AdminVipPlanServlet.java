package app.api.admin;

import app.integration.momo.MomoUpgradePlan;
import java.io.IOException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * API liệt kê các gói VIP đang mở bán cho quản trị viên.
 */
@WebServlet(name = "AdminVipPlanServlet", urlPatterns = "/api/admin/vip-plans")
public class AdminVipPlanServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        JSONArray array = new JSONArray();
        for (MomoUpgradePlan plan : MomoUpgradePlan.values()) {
            JSONObject obj = new JSONObject()
                    .put("code", plan.getCode())
                    .put("description", plan.getDescription())
                    .put("amount", plan.getAmount())
                    .put("durationMonths", plan.getDuration().toTotalMonths());
            array.put(obj);
        }
        resp.getWriter().print(array.toString());
    }
}
