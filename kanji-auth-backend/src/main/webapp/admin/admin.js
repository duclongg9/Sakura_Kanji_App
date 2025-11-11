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
  jlptLevels: [],
  levels: [],
  selectedJlptLevelId: "",
  editingLevelId: null,
  kanjiList: [],
  selectedLevelIdForKanji: "",
  editingKanjiId: null,
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
  jlptFilter: document.querySelector("#jlpt-filter"),
  levelsTbody: document.querySelector("#levels-tbody"),
  levelRowTemplate: document.querySelector("#level-row-template"),
  levelForm: document.querySelector("#level-form"),
  levelFormTitle: document.querySelector("#level-form-title"),
  levelIdInput: document.querySelector("#level-id"),
  levelNameInput: document.querySelector("#level-name"),
  levelJlptSelect: document.querySelector("#level-jlpt-select"),
  levelDescriptionInput: document.querySelector("#level-description"),
  levelAccessTierSelect: document.querySelector("#level-access-tier"),
  levelActiveInput: document.querySelector("#level-active"),
  levelCancelButton: document.querySelector("#level-cancel"),
  kanjiLevelSelect: document.querySelector("#kanji-level-select"),
  reloadKanjiButton: document.querySelector("#reload-kanji"),
  kanjiTbody: document.querySelector("#kanji-tbody"),
  kanjiRowTemplate: document.querySelector("#kanji-row-template"),
  kanjiForm: document.querySelector("#kanji-form"),
  kanjiFormTitle: document.querySelector("#kanji-form-title"),
  kanjiIdInput: document.querySelector("#kanji-id"),
  kanjiCharacterInput: document.querySelector("#kanji-character"),
  kanjiHanVietInput: document.querySelector("#kanji-hanviet"),
  kanjiOnInput: document.querySelector("#kanji-on"),
  kanjiKunInput: document.querySelector("#kanji-kun"),
  kanjiDescriptionInput: document.querySelector("#kanji-description"),
  kanjiLevelInput: document.querySelector("#kanji-level-input"),
  kanjiCancelButton: document.querySelector("#kanji-cancel"),
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
    showToast(normalizeErrorMessage(error), "error");
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
 * Tải danh sách cấp độ JLPT và cập nhật các bộ chọn liên quan.
 */
async function loadJlptLevels() {
  try {
    const response = await fetch("../api/jlpt-levels");
    if (!response.ok) {
      throw new Error(`Không thể tải danh sách JLPT (${response.status})`);
    }
    const data = await response.json();
    state.jlptLevels = Array.isArray(data) ? data : [];
    if (!state.selectedJlptLevelId && state.jlptLevels.length > 0) {
      state.selectedJlptLevelId = String(state.jlptLevels[0].id);
    }
    renderJlptSelectors();
    resetLevelForm();
    if (state.selectedJlptLevelId) {
      await loadLevels();
    }
  } catch (error) {
    console.error(error);
    showToast(normalizeErrorMessage(error), "error");
    renderJlptSelectors();
  }
}

/**
 * Render các thẻ select dành cho JLPT.
 */
function renderJlptSelectors() {
  if (!elements.jlptFilter) {
    return;
  }
  const hasJlpt = state.jlptLevels.length > 0;
  const optionHtml = state.jlptLevels
    .map((level) => `<option value="${level.id}">${escapeHtml(level.nameLevel ?? `JLPT #${level.id}`)}</option>`)
    .join("");

  if (hasJlpt) {
    elements.jlptFilter.innerHTML = optionHtml;
    elements.levelJlptSelect.innerHTML = optionHtml;
    const currentValue = state.selectedJlptLevelId && optionHtml ? state.selectedJlptLevelId : String(state.jlptLevels[0].id);
    state.selectedJlptLevelId = currentValue;
    elements.jlptFilter.value = currentValue;
    elements.levelJlptSelect.value = currentValue;
  } else {
    elements.jlptFilter.innerHTML = `<option value="">Chưa có dữ liệu JLPT</option>`;
    elements.levelJlptSelect.innerHTML = `<option value="">Chưa có dữ liệu JLPT</option>`;
    state.selectedJlptLevelId = "";
  }
}

/**
 * Tải danh sách level tương ứng JLPT đang chọn.
 */
async function loadLevels() {
  if (!state.selectedJlptLevelId) {
    elements.levelsTbody.innerHTML = `<tr class="placeholder"><td colspan="5">Chưa chọn JLPT.</td></tr>`;
    updateKanjiLevelSelectors();
    return;
  }
  try {
    elements.levelsTbody.innerHTML = `<tr class="placeholder"><td colspan="5">Đang tải level...</td></tr>`;
    const url = new URL("../api/levels", window.location.href);
    url.searchParams.set("jlptId", state.selectedJlptLevelId);
    url.searchParams.set("includeInactive", "true");
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Không thể tải level (${response.status})`);
    }
    const data = await response.json();
    state.levels = Array.isArray(data) ? data : [];
    renderLevelsTable();
    updateKanjiLevelSelectors();
  } catch (error) {
    console.error(error);
    elements.levelsTbody.innerHTML = `<tr class="placeholder"><td colspan="5">${escapeHtml(error.message)}</td></tr>`;
    state.levels = [];
    updateKanjiLevelSelectors();
  }
}

/**
 * Hiển thị danh sách level cùng các nút thao tác.
 */
function renderLevelsTable() {
  if (!elements.levelRowTemplate) {
    return;
  }
  if (!state.levels.length) {
    elements.levelsTbody.innerHTML = `<tr class="placeholder"><td colspan="5">Chưa có level cho JLPT này.</td></tr>`;
    return;
  }
  const fragment = document.createDocumentFragment();
  state.levels.forEach((level) => {
    const row = elements.levelRowTemplate.content.firstElementChild.cloneNode(true);
    row.dataset.levelId = String(level.id);
    row.querySelector(".level-name").textContent = level.name ?? "(Không tên)";
    row.querySelector(".level-jlpt").textContent = getJlptName(level.jlptLevelId);
    row.querySelector(".level-tier").textContent = level.accessTier ?? "FREE";
    const pill = row.querySelector(".status-pill");
    if (level.active) {
      pill.textContent = "Đang hiển thị";
      pill.classList.remove("inactive");
    } else {
      pill.textContent = "Đã ẩn";
      pill.classList.add("inactive");
    }

    const [editButton, deleteButton] = row.querySelectorAll(".table-action");
    if (editButton) {
      editButton.addEventListener("click", () => populateLevelForm(level));
    }
    if (deleteButton) {
      deleteButton.addEventListener("click", () => deleteLevel(level));
    }
    fragment.appendChild(row);
  });
  elements.levelsTbody.innerHTML = "";
  elements.levelsTbody.appendChild(fragment);
}

/**
 * Điền dữ liệu level vào form để chỉnh sửa.
 */
function populateLevelForm(level) {
  state.editingLevelId = level.id;
  elements.levelFormTitle.textContent = `Chỉnh sửa level #${level.id}`;
  elements.levelIdInput.value = String(level.id);
  elements.levelNameInput.value = level.name ?? "";
  elements.levelJlptSelect.value = String(level.jlptLevelId);
  elements.levelDescriptionInput.value = level.description ?? "";
  elements.levelAccessTierSelect.value = level.accessTier ?? "FREE";
  elements.levelActiveInput.checked = Boolean(level.active);
}

/**
 * Khôi phục form level về trạng thái thêm mới.
 */
function resetLevelForm() {
  state.editingLevelId = null;
  elements.levelFormTitle.textContent = "Thêm level mới";
  elements.levelIdInput.value = "";
  elements.levelNameInput.value = "";
  const defaultJlpt = state.selectedJlptLevelId || (state.jlptLevels[0] ? String(state.jlptLevels[0].id) : "");
  elements.levelJlptSelect.value = defaultJlpt;
  elements.levelDescriptionInput.value = "";
  elements.levelAccessTierSelect.value = "FREE";
  elements.levelActiveInput.checked = true;
}

/**
 * Gửi yêu cầu tạo mới hoặc cập nhật level.
 */
async function handleLevelSubmit(event) {
  event.preventDefault();
  try {
    const payload = {
      name: elements.levelNameInput.value.trim(),
      jlptLevelId: Number(elements.levelJlptSelect.value),
      description: elements.levelDescriptionInput.value.trim() || null,
      accessTier: elements.levelAccessTierSelect.value,
      active: elements.levelActiveInput.checked,
    };

    const isEdit = Boolean(state.editingLevelId);
    const url = "../api/levels";
    const headers = {
      "Content-Type": "application/json",
      ...buildAuthHeader(),
    };
    let response;
    if (isEdit) {
      payload.id = Number(state.editingLevelId);
      response = await fetch(url, {
        method: "PUT",
        headers,
        body: JSON.stringify(payload),
      });
    } else {
      response = await fetch(url, {
        method: "POST",
        headers,
        body: JSON.stringify(payload),
      });
    }

    if (!response.ok) {
      const data = await safeReadJson(response);
      const message = extractErrorMessage(data, isEdit ? "Cập nhật level thất bại" : "Tạo level thất bại");
      throw new Error(message);
    }

    showToast(isEdit ? "Đã cập nhật level thành công" : "Đã tạo level mới", "success");
    await loadLevels();
    resetLevelForm();
  } catch (error) {
    console.error(error);
    showToast(normalizeErrorMessage(error), "error");
  }
}

/**
 * Xóa level được chọn khỏi hệ thống.
 */
async function deleteLevel(level) {
  if (!window.confirm(`Xóa level \"${level.name ?? level.id}\"?`)) {
    return;
  }
  try {
    const response = await fetch(`../api/levels?id=${level.id}`, {
      method: "DELETE",
      headers: {
        ...buildAuthHeader(),
      },
    });
    if (!response.ok) {
      const data = await safeReadJson(response);
      const message = extractErrorMessage(data, "Xóa level thất bại");
      throw new Error(message);
    }
    showToast("Đã xóa level", "success");
    if (Number(state.selectedLevelIdForKanji) === Number(level.id)) {
      state.selectedLevelIdForKanji = "";
    }
    await loadLevels();
    resetLevelForm();
  } catch (error) {
    console.error(error);
    showToast(normalizeErrorMessage(error), "error");
  }
}

/**
 * Cập nhật danh sách level cho phần quản lý Kanji.
 */
function updateKanjiLevelSelectors() {
  const optionHtml = state.levels
    .map((level) => `<option value="${level.id}">${escapeHtml(level.name ?? `Level #${level.id}`)}</option>`)
    .join("");

  if (state.levels.length > 0) {
    elements.kanjiLevelSelect.innerHTML = `<option value="">-- Chọn level --</option>${optionHtml}`;
    elements.kanjiLevelInput.innerHTML = optionHtml;
    if (!state.selectedLevelIdForKanji || !state.levels.some((level) => Number(level.id) === Number(state.selectedLevelIdForKanji))) {
      state.selectedLevelIdForKanji = String(state.levels[0].id);
    }
    elements.kanjiLevelSelect.value = state.selectedLevelIdForKanji;
    elements.kanjiLevelInput.value = state.selectedLevelIdForKanji;
    resetKanjiForm();
    loadKanjiForSelectedLevel();
  } else {
    elements.kanjiLevelSelect.innerHTML = `<option value="">Chưa có level để hiển thị</option>`;
    elements.kanjiLevelInput.innerHTML = `<option value="">Chưa có level</option>`;
    elements.kanjiLevelInput.value = "";
    state.selectedLevelIdForKanji = "";
    resetKanjiForm();
    elements.kanjiTbody.innerHTML = `<tr class="placeholder"><td colspan="5">Hãy tạo level trước khi thêm Kanji.</td></tr>`;
  }
}

/**
 * Tải danh sách Kanji theo level đang chọn.
 */
async function loadKanjiForSelectedLevel() {
  if (!state.selectedLevelIdForKanji) {
    elements.kanjiTbody.innerHTML = `<tr class="placeholder"><td colspan="5">Chọn level để xem Kanji.</td></tr>`;
    return;
  }
  try {
    elements.kanjiTbody.innerHTML = `<tr class="placeholder"><td colspan="5">Đang tải Kanji...</td></tr>`;
    const url = new URL("../api/kanji", window.location.href);
    url.searchParams.set("levelId", state.selectedLevelIdForKanji);
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Không thể tải Kanji (${response.status})`);
    }
    const data = await response.json();
    state.kanjiList = Array.isArray(data) ? data : [];
    renderKanjiTable();
  } catch (error) {
    console.error(error);
    elements.kanjiTbody.innerHTML = `<tr class="placeholder"><td colspan="5">${escapeHtml(error.message)}</td></tr>`;
    state.kanjiList = [];
  }
}

/**
 * Hiển thị bảng Kanji với các hành động chỉnh sửa.
 */
function renderKanjiTable() {
  if (!elements.kanjiRowTemplate) {
    return;
  }
  if (!state.kanjiList.length) {
    elements.kanjiTbody.innerHTML = `<tr class="placeholder"><td colspan="5">Level này chưa có Kanji.</td></tr>`;
    return;
  }
  const fragment = document.createDocumentFragment();
  state.kanjiList.forEach((kanji) => {
    const row = elements.kanjiRowTemplate.content.firstElementChild.cloneNode(true);
    row.dataset.kanjiId = String(kanji.id);
    row.querySelector(".kanji-character").textContent = kanji.character ?? "?";
    row.querySelector(".kanji-hanviet").textContent = kanji.hanViet ?? "-";
    row.querySelector(".kanji-on").textContent = kanji.onReading ?? "-";
    row.querySelector(".kanji-kun").textContent = kanji.kunReading ?? "-";
    const [editButton, deleteButton] = row.querySelectorAll(".table-action");
    if (editButton) {
      editButton.addEventListener("click", () => populateKanjiForm(kanji));
    }
    if (deleteButton) {
      deleteButton.addEventListener("click", () => deleteKanji(kanji));
    }
    fragment.appendChild(row);
  });
  elements.kanjiTbody.innerHTML = "";
  elements.kanjiTbody.appendChild(fragment);
}

/**
 * Đổ dữ liệu Kanji lên form để chỉnh sửa.
 */
function populateKanjiForm(kanji) {
  state.editingKanjiId = kanji.id;
  elements.kanjiFormTitle.textContent = `Chỉnh sửa Kanji #${kanji.id}`;
  elements.kanjiIdInput.value = String(kanji.id);
  elements.kanjiCharacterInput.value = kanji.character ?? "";
  elements.kanjiHanVietInput.value = kanji.hanViet ?? "";
  elements.kanjiOnInput.value = kanji.onReading ?? "";
  elements.kanjiKunInput.value = kanji.kunReading ?? "";
  elements.kanjiDescriptionInput.value = kanji.description ?? "";
  if (kanji.levelId) {
    elements.kanjiLevelInput.value = String(kanji.levelId);
    elements.kanjiLevelSelect.value = String(kanji.levelId);
    state.selectedLevelIdForKanji = String(kanji.levelId);
  }
}

/**
 * Khôi phục form Kanji về trạng thái mặc định.
 */
function resetKanjiForm() {
  state.editingKanjiId = null;
  elements.kanjiFormTitle.textContent = "Thêm Kanji mới";
  elements.kanjiIdInput.value = "";
  elements.kanjiCharacterInput.value = "";
  elements.kanjiHanVietInput.value = "";
  elements.kanjiOnInput.value = "";
  elements.kanjiKunInput.value = "";
  elements.kanjiDescriptionInput.value = "";
  if (state.selectedLevelIdForKanji) {
    elements.kanjiLevelInput.value = state.selectedLevelIdForKanji;
  }
}

/**
 * Gửi yêu cầu tạo mới hoặc cập nhật Kanji.
 */
async function handleKanjiSubmit(event) {
  event.preventDefault();
  try {
    if (!elements.kanjiLevelInput.value) {
      throw new Error("Hãy chọn level cho Kanji");
    }
    const payload = {
      character: elements.kanjiCharacterInput.value.trim(),
      hanViet: valueOrNull(elements.kanjiHanVietInput.value),
      onReading: valueOrNull(elements.kanjiOnInput.value),
      kunReading: valueOrNull(elements.kanjiKunInput.value),
      description: valueOrNull(elements.kanjiDescriptionInput.value),
      levelId: Number(elements.kanjiLevelInput.value),
    };
    const headers = {
      "Content-Type": "application/json",
      ...buildAuthHeader(),
    };
    const isEdit = Boolean(state.editingKanjiId);
    let response;
    if (isEdit) {
      payload.id = Number(state.editingKanjiId);
      response = await fetch("../api/kanji", {
        method: "PUT",
        headers,
        body: JSON.stringify(payload),
      });
    } else {
      response = await fetch("../api/kanji", {
        method: "POST",
        headers,
        body: JSON.stringify(payload),
      });
    }
    if (!response.ok) {
      const data = await safeReadJson(response);
      const message = extractErrorMessage(data, isEdit ? "Cập nhật Kanji thất bại" : "Tạo Kanji thất bại");
      throw new Error(message);
    }
    showToast(isEdit ? "Đã cập nhật Kanji" : "Đã thêm Kanji", "success");
    await loadKanjiForSelectedLevel();
    resetKanjiForm();
  } catch (error) {
    console.error(error);
    showToast(normalizeErrorMessage(error), "error");
  }
}

/**
 * Xóa Kanji theo id.
 */
async function deleteKanji(kanji) {
  if (!window.confirm(`Xóa Kanji ${kanji.character ?? kanji.id}?`)) {
    return;
  }
  try {
    const response = await fetch(`../api/kanji?id=${kanji.id}`, {
      method: "DELETE",
      headers: {
        ...buildAuthHeader(),
      },
    });
    if (!response.ok) {
      const data = await safeReadJson(response);
      const message = extractErrorMessage(data, "Xóa Kanji thất bại");
      throw new Error(message);
    }
    showToast("Đã xóa Kanji", "success");
    await loadKanjiForSelectedLevel();
    resetKanjiForm();
  } catch (error) {
    console.error(error);
    showToast(normalizeErrorMessage(error), "error");
  }
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
 * Trả về tên JLPT tương ứng id.
 */
function getJlptName(jlptId) {
  const level = state.jlptLevels.find((item) => Number(item.id) === Number(jlptId));
  return level ? level.nameLevel ?? `JLPT #${level.id}` : `JLPT #${jlptId}`;
}

/**
 * Chuyển chuỗi rỗng thành null.
 */
function valueOrNull(value) {
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
}

/**
 * Đọc thông điệp lỗi từ payload trả về của API.
 */
function extractErrorMessage(payload, fallback) {
  if (!payload) {
    return fallback;
  }
  if (typeof payload.message === "string" && payload.message.trim()) {
    return payload.message;
  }
  if (typeof payload.error === "string" && payload.error.trim()) {
    return payload.error;
  }
  if (payload.details && typeof payload.details === "object") {
    const firstKey = Object.keys(payload.details)[0];
    if (firstKey) {
      const detail = payload.details[firstKey];
      if (typeof detail === "string") {
        return detail;
      }
      if (Array.isArray(detail) && detail.length > 0) {
        return detail[0];
      }
    }
  }
  return fallback;
}

/**
 * Đồng nhất thông điệp lỗi cho người dùng.
 */
function normalizeErrorMessage(error, fallback = "Có lỗi xảy ra") {
  if (!error) {
    return fallback;
  }
  const message = typeof error === "string" ? error : error.message;
  if (message === "Admin ID is missing") {
    return "Vui lòng đăng nhập quản trị trước khi thao tác.";
  }
  return message || fallback;
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

  elements.jlptFilter.addEventListener("change", (event) => {
    state.selectedJlptLevelId = event.target.value;
    resetLevelForm();
    loadLevels();
  });

  elements.levelForm.addEventListener("submit", handleLevelSubmit);
  elements.levelCancelButton.addEventListener("click", () => {
    resetLevelForm();
  });

  elements.kanjiLevelSelect.addEventListener("change", (event) => {
    state.selectedLevelIdForKanji = event.target.value;
    if (event.target.value) {
      elements.kanjiLevelInput.value = event.target.value;
    }
    resetKanjiForm();
    loadKanjiForSelectedLevel();
  });

  elements.reloadKanjiButton.addEventListener("click", () => {
    loadKanjiForSelectedLevel();
  });

  elements.kanjiForm.addEventListener("submit", handleKanjiSubmit);
  elements.kanjiCancelButton.addEventListener("click", () => {
    resetKanjiForm();
  });
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
loadJlptLevels();
resetLevelForm();
resetKanjiForm();

if (!state.adminId) {
  elements.sessionDialog.showModal();
}
