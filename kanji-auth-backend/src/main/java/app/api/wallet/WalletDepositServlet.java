package app.api.wallet;

import java.io.IOException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 * Endpoint tạo giao dịch nạp tiền và trả về QR code.
 */
@WebServlet(name = "WalletDepositServlet", urlPatterns = "/api/wallet/deposits")
public class WalletDepositServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_GONE);
        resp.getWriter().print(new JSONObject()
                .put("error", "Deprecated")
                .put("message", "Tính năng nạp ví đã bị tắt. Vui lòng thanh toán MoMo để nâng cấp VIP.").toString());
    }

}
