package app.dao;

import app.model.AdminMember;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO phục vụ các báo cáo quản trị hội viên.
 */
public class AdminMembershipDAO extends BaseDAO {

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Lấy danh sách hội viên theo bộ lọc.
     *
     * @param filter "pending", "vip", "free" hoặc rỗng.
     * @return danh sách hội viên.
     * @throws SQLException nếu truy vấn thất bại.
     */
    public List<AdminMember> listMembers(String filter) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT u.id, u.userName, u.email, u.imgUrl, u.accountTier, u.vipExpiresAt, "
                        + "r.request_id, r.status, r.note, r.createdAt "
                        + "FROM `User` u "
                        + "LEFT JOIN AccountUpgradeRequest r ON r.user_id = u.id AND r.status = 'PENDING' "
                        + "WHERE u.roleId <> 1 ");
        if ("pending".equalsIgnoreCase(filter)) {
            sql.append("AND r.request_id IS NOT NULL ");
        } else if ("vip".equalsIgnoreCase(filter)) {
            sql.append("AND (u.accountTier = 'VIP' OR u.roleId = 3) ");
        } else if ("free".equalsIgnoreCase(filter)) {
            sql.append("AND u.accountTier = 'FREE' ");
        }
        sql.append("ORDER BY r.request_id IS NULL, u.accountTier DESC, u.userName ASC");

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            try (ResultSet rs = ps.executeQuery()) {
                List<AdminMember> result = new ArrayList<>();
                while (rs.next()) {
                    AdminMember member = new AdminMember();
                    member.setId(rs.getLong("id"));
                    member.setUserName(rs.getString("userName"));
                    member.setEmail(rs.getString("email"));
                    member.setAvatarUrl(rs.getString("imgUrl"));
                    member.setAccountTier(rs.getString("accountTier"));
                    member.setVipExpiresAt(toLocalDateTime(rs.getTimestamp("vipExpiresAt")));
                    member.setRequestId(rs.getObject("request_id") == null ? null : rs.getLong("request_id"));
                    member.setRequestStatus(rs.getString("status"));
                    member.setRequestNote(rs.getString("note"));
                    member.setRequestCreatedAt(toLocalDateTime(rs.getTimestamp("createdAt")));
                    result.add(member);
                }
                return result;
            }
        }
    }
}
