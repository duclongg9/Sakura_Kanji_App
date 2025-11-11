/**
 * Điều phối logic trang quản trị Sakura Kanji.
 *
 * File sử dụng module ES6 để tránh ô nhiễm phạm vi toàn cục.
 */
const state = {
  adminId: null,
  members: [],
  selectedMemberId: null,
  filter: "",
  search: "",
};

const elements = {
  sessionLabel: document.querySelector("#admin-session-label"),
  sessionDialog: document.querySelector("#session-dialog"),
  adminIdInput: document.querySelector("#admin-id-input"),
  openSessionButton: document.querySelector("#open-session-dialog"),
  filterSelect: document.querySelector("#member-filter"),
  searchInput: document.querySelector("#search-input"),
  tableBody: document.querySelector("#members-tbody"),
  rowTemplate: document.querySelector("#member-row-template"),
  detailsPanel: document.querySelector("#member-details"),
  vipPlanList: document.querySelector("#vip-plan-list"),
  vipPlanTemplate: document.querySelector("#vip-plan-template"),
};

/**
 * Đọc adminId đã lưu (nếu có) và cập nhật nhãn hiển thị.
 */
function initializeSession() {
  const cachedId = window.localStorage.getItem("sakura-admin-id");
  if (cachedId) {
    state.adminId = cachedId;
    elements.adminIdInput.value = cachedId;
  }
  updateSessionLabel();
}

/**
 * Cập nhật nội dung nhãn phiên đăng nhập.
 */
function updateSessionLabel() {
  if (state.adminId) {
    elements.sessionLabel.textContent = `Đang dùng ID #${state.adminId}`;
    elements.sessionLabel.classList.remove("danger");
  } else {
    elements.sessionLabel.textContent = "Chưa đăng nhập";
    elements.sessionLabel.classList.add("danger");
  }
}

/**
 * Trả về header Authorization cho các yêu cầu cần xác thực.
 */
function buildAuthHeader() {
  if (!state.adminId) {
    throw new Error("Admin ID is missing");
  }
  return {
    Authorization: `Bearer demo-${state.adminId}`,
  };
}

/**
 * Gửi yêu cầu tải danh sách hội viên.
 */
async function loadMembers() {
  const url = new URL("../api/admin/members", window.location.href);
  if (state.filter) {
    url.searchParams.set("filter", state.filter);
  }

  try {
    elements.tableBody.innerHTML = `<tr class="placeholder"><td colspan="5">Đang tải dữ liệu...</td></tr>`;
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Không thể tải hội viên: ${response.status}`);
    }
    const data = await response.json();
    state.members = Array.isArray(data) ? data : [];
    renderMemberTable();
  } catch (error) {
    console.error(error);
    elements.tableBody.innerHTML = `<tr class="placeholder"><td colspan="5">${error.message}</td></tr>`;
  }
}

/**
 * Hiển thị danh sách hội viên theo bộ lọc tìm kiếm hiện tại.
 */
function renderMemberTable() {
  const tbody = document.createDocumentFragment();
  const normalizedSearch = state.search.trim().toLowerCase();

  const filteredMembers = state.members.filter((member) => {
    if (!normalizedSearch) {
      return true;
    }
    const haystack = `${member.userName ?? ""} ${member.email ?? ""}`.toLowerCase();
    return haystack.includes(normalizedSearch);
  });

  if (filteredMembers.length === 0) {
    elements.tableBody.innerHTML = `<tr class="placeholder"><td colspan="5">Không tìm thấy hội viên phù hợp.</td></tr>`;
    state.selectedMemberId = null;
    renderMemberDetails(null);
    return;
  }

  const stillSelected = filteredMembers.some((member) => Number(member.id) === Number(state.selectedMemberId));
  if (!stillSelected) {
    state.selectedMemberId = null;
    renderMemberDetails(null);
  }

  filteredMembers.forEach((member) => {
    const row = elements.rowTemplate.content.firstElementChild.cloneNode(true);
    row.dataset.memberId = String(member.id);
    row.querySelector(".avatar").src = member.avatarUrl || "https://ui-avatars.com/api/?name=?";
    row.querySelector(".member-name").textContent = member.userName ?? "(Chưa cập nhật)";
    row.querySelector(".member-id").textContent = `ID: ${member.id}`;
    row.querySelector(".member-email").textContent = member.email ?? "-";
    row.querySelector(".member-tier").textContent = member.accountTier ?? "-";
    row.querySelector(".member-expiry").textContent = formatDate(member.vipExpiresAt);
    row.querySelector(".member-request").textContent = formatRequestStatus(member);
    if (Number(state.selectedMemberId) === Number(member.id)) {
      row.classList.add("selected");
    }
    row.addEventListener("click", () => {
      state.selectedMemberId = member.id;
      renderMemberTable();
      renderMemberDetails(member);
    });
    tbody.appendChild(row);
  });

  elements.tableBody.innerHTML = "";
  elements.tableBody.appendChild(tbody);
}

/**
 * Hiển thị chi tiết hội viên ở panel bên phải.
 */
function renderMemberDetails(member) {
  if (!member) {
    elements.detailsPanel.innerHTML = `<div class="placeholder">Chưa chọn hội viên nào.</div>`;
    return;
  }

  const detailHtml = `
    <div class="member-meta">
      <strong>${escapeHtml(member.userName ?? "(Chưa cập nhật)")}</strong>
      <div class="tags">
        <span class="tag">ID #${member.id}</span>
        <span class="tag">${escapeHtml(member.email ?? "Không có email")}</span>
        <span class="tag">Hạng: ${escapeHtml(member.accountTier ?? "Không rõ")}</span>
      </div>
    </div>
    <div class="detail-grid">
      <dl class="detail-card">
        <dt>Ngày hết hạn VIP</dt>
        <dd>${formatDate(member.vipExpiresAt, "Chưa kích hoạt VIP")}</dd>
      </dl>
      <dl class="detail-card">
        <dt>Trạng thái yêu cầu</dt>
        <dd>${escapeHtml(formatRequestStatus(member))}</dd>
      </dl>
    </div>
    ${renderPendingRequest(member)}
  `;

  elements.detailsPanel.innerHTML = detailHtml;
  const approveButton = elements.detailsPanel.querySelector("#approve-request");
  if (approveButton) {
    approveButton.addEventListener("click", () => approveRequest(member));
  }
}

/**
 * Trả về HTML mô tả khối yêu cầu nâng cấp (nếu có).
 */
function renderPendingRequest(member) {
  if (!member.hasPendingRequest) {
    return "";
  }
  const requestDate = formatDate(member.requestCreatedAt, "Không rõ thời gian");
  const note = member.requestNote ? escapeHtml(member.requestNote) : "Không có ghi chú";
  return `
    <div class="pending-request">
      <header>
        <h3>Yêu cầu nâng cấp đang chờ</h3>
        <time>Gửi lúc: ${requestDate}</time>
      </header>
      <p>${note}</p>
      <button id="approve-request" class="primary" type="button">Phê duyệt yêu cầu</button>
    </div>
  `;
}

/**
 * Gửi yêu cầu phê duyệt nâng cấp VIP.
 */
async function approveRequest(member) {
  if (!member.requestId) {
    showToast("Không tìm thấy mã yêu cầu để phê duyệt", "error");
    return;
  }
  try {
    const response = await fetch("../api/admin/upgrade-requests/approve", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...buildAuthHeader(),
      },
      body: JSON.stringify({ requestId: member.requestId }),
    });

    if (!response.ok) {
      const payload = await safeReadJson(response);
      const message = payload?.message || payload?.error || `Phê duyệt thất bại (${response.status})`;
      throw new Error(message);
    }

    showToast("Đã phê duyệt yêu cầu thành công", "success");
    await loadMembers();
    const refreshed = state.members.find((m) => Number(m.id) === Number(member.id));
    renderMemberDetails(refreshed);
  } catch (error) {
    console.error(error);
    showToast(error.message, "error");
  }
}

/**
 * Tải danh sách gói VIP.
 */
async function loadVipPlans() {
  try {
    const response = await fetch("../api/admin/vip-plans");
    if (!response.ok) {
      throw new Error(`Không thể tải gói VIP (${response.status})`);
    }
    const data = await response.json();
    renderVipPlans(Array.isArray(data) ? data : []);
  } catch (error) {
    console.error(error);
    elements.vipPlanList.innerHTML = `<li class="placeholder">${error.message}</li>`;
  }
}

/**
 * Hiển thị danh sách gói VIP ra giao diện.
 */
function renderVipPlans(plans) {
  if (plans.length === 0) {
    elements.vipPlanList.innerHTML = `<li class="placeholder">Không có gói VIP nào.</li>`;
    return;
  }

  const fragment = document.createDocumentFragment();
  plans.forEach((plan) => {
    const li = elements.vipPlanTemplate.content.firstElementChild.cloneNode(true);
    li.querySelector("h3").textContent = plan.description ?? "Gói VIP";
    li.querySelector(".plan-description").textContent = `Phí ${formatCurrency(plan.amount)} cho ${plan.durationMonths} tháng.`;
    li.querySelector(".plan-code").textContent = plan.code ?? "-";
    li.querySelector(".plan-amount").textContent = formatCurrency(plan.amount);
    li.querySelector(".plan-duration").textContent = `${plan.durationMonths} tháng`;
    fragment.appendChild(li);
  });

  elements.vipPlanList.innerHTML = "";
  elements.vipPlanList.appendChild(fragment);
}

/**
 * Hiển thị thông báo dạng toast phía trên màn hình.
 */
function showToast(message, type = "info") {
  let container = document.querySelector(".toast-container");
  if (!container) {
    container = document.createElement("div");
    container.className = "toast-container";
    document.body.appendChild(container);
  }

  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  container.appendChild(toast);

  setTimeout(() => {
    toast.classList.add("visible");
  }, 20);

  setTimeout(() => {
    toast.classList.remove("visible");
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

/**
 * Đảm bảo dữ liệu JSON được đọc an toàn.
 */
async function safeReadJson(response) {
  try {
    return await response.json();
  } catch (error) {
    return null;
  }
}

/**
 * Chuẩn hóa ngày ở định dạng dễ đọc.
 */
function formatDate(value, fallback = "-") {
  if (!value || value === "null") {
    return fallback;
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return fallback;
  }
  return date.toLocaleString("vi-VN", {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

/**
 * Trả về chuỗi mô tả trạng thái yêu cầu nâng cấp.
 */
function formatRequestStatus(member) {
  if (!member.hasPendingRequest) {
    return "Không có yêu cầu";
  }
  return `${member.requestStatus ?? "PENDING"}`;
}

/**
 * Hiển thị số tiền theo định dạng VND.
 */
function formatCurrency(amount) {
  if (typeof amount !== "number") {
    return "-";
  }
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(amount);
}

/**
 * Escape ký tự HTML trong chuỗi bất kỳ.
 */
function escapeHtml(text) {
  return String(text)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

/**
 * Cài đặt mọi xử lý sự kiện cần thiết cho giao diện.
 */
function registerEventHandlers() {
  elements.openSessionButton.addEventListener("click", () => {
    elements.sessionDialog.showModal();
  });

  elements.sessionDialog.addEventListener("close", () => {
    if (elements.sessionDialog.returnValue === "confirm") {
      const value = elements.adminIdInput.value.trim();
      if (value) {
        state.adminId = value;
        window.localStorage.setItem("sakura-admin-id", value);
      }
    }
    updateSessionLabel();
  });

  elements.filterSelect.addEventListener("change", (event) => {
    state.filter = event.target.value;
    loadMembers();
  });

  elements.searchInput.addEventListener("input", debounce((event) => {
    state.search = event.target.value;
    renderMemberTable();
  }, 200));
}

/**
 * Tạo hàm debounce để giảm số lần render.
 */
function debounce(callback, delay = 300) {
  let timer = null;
  return (...args) => {
    if (timer) {
      clearTimeout(timer);
    }
    timer = setTimeout(() => callback(...args), delay);
  };
}

// Khởi chạy khi DOM sẵn sàng
initializeSession();
registerEventHandlers();
loadMembers();
loadVipPlans();

if (!state.adminId) {
  elements.sessionDialog.showModal();
}
