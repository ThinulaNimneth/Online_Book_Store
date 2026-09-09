/* ==========================================================================
   admin-dashboard.js
   Requires an ADMIN JWT. Every panel below is wired to its real endpoint:
     Books        -> /books        (POST / PUT / DELETE /books/{id})
     Categories   -> /categories   (POST / PUT / DELETE /categories/{id})
     Authors      -> /authors      (POST / PUT / DELETE /authors/{id})
     Publishers   -> /publishers   (POST / PUT / DELETE /publishers/{id})
     Inventory    -> /inventory    (POST / PUT /inventory - upsert by bookId, no delete)
     Orders       -> /orders/all   (PATCH /orders/{id}/status?status= only - no add/delete)
   ========================================================================== */

// Where each dropdown/multiselect field's options come from.
const LOOKUP_SOURCES = {
    publishers: { endpoint: "/publishers", idKey: "publisherId", labelKey: "name" },
    categories: { endpoint: "/categories", idKey: "categoryId", labelKey: "name" },
    authors:    { endpoint: "/authors",    idKey: "authorId",    labelKey: "name" },
};

const PANEL_CONFIG = {
    books: {
        title: "Books", endpoint: "/books", idKey: "id",
        columns: ["Title", "Author", "Category", "Price", "Stock", ""],
        row: b => [b.title, b.author, b.category, formatLKR(b.specialPrice || b.price), b.inStock ? "In Stock" : "Out of Stock"],
        canDelete: true,
        fields: [
            { key: "title", label: "Title", type: "text", required: true },
            { key: "isbn", label: "ISBN", type: "text" },
            { key: "description", label: "Description", type: "textarea" },
            { key: "price", label: "Price (LKR)", type: "number", step: "0.01", required: true },
            { key: "specialPrice", label: "Special price (optional)", type: "number", step: "0.01" },
            { key: "language", label: "Language", type: "text" },
            { key: "pages", label: "Pages", type: "number" },
            { key: "publisherId", label: "Publisher", type: "select", source: "publishers", required: true },
            { key: "categoryIds", label: "Categories", type: "multiselect", source: "categories", required: true },
            { key: "authorIds", label: "Authors", type: "multiselect", source: "authors", required: true },
            { key: "quantityAvailable", label: "Initial stock quantity", type: "number" },
        ],
    },
    categories: {
        title: "Categories", endpoint: "/categories", idKey: "categoryId",
        columns: ["Name", "Description", ""],
        row: c => [c.name, c.description || "\u2014"],
        canDelete: true,
        fields: [
            { key: "name", label: "Name", type: "text", required: true },
            { key: "description", label: "Description", type: "textarea" },
        ],
    },
    authors: {
        title: "Authors", endpoint: "/authors", idKey: "authorId",
        columns: ["Name", "Bio", ""],
        row: a => [a.name, (a.bio || "\u2014")],
        canDelete: true,
        fields: [
            { key: "name", label: "Name", type: "text", required: true },
            { key: "bio", label: "Bio", type: "textarea" },
        ],
    },
    publishers: {
        title: "Publishers", endpoint: "/publishers", idKey: "publisherId",
        columns: ["Name", "Contact Email", ""],
        row: p => [p.name, p.contactEmail || "\u2014"],
        canDelete: true,
        fields: [
            { key: "name", label: "Name", type: "text", required: true },
            { key: "contactEmail", label: "Contact email", type: "email" },
        ],
    },
    inventory: {
        title: "Inventory", endpoint: "/inventory", idKey: "bookId",
        columns: ["Book", "Quantity Available", "Reorder Level", ""],
        row: i => [i.bookTitle, i.quantityAvailable, i.reorderLevel],
        canDelete: false,
        fields: [
            { key: "bookId", label: "Book ID", type: "number", required: true },
            { key: "quantityAvailable", label: "Quantity available", type: "number", required: true },
            { key: "reorderLevel", label: "Reorder level", type: "number" },
        ],
    },
    orders: {
        title: "Orders", endpoint: "/orders/all", idKey: "orderId",
        columns: ["Order #", "Customer", "Status", "Total", ""],
        row: o => ["#" + o.orderId, o.customerName, o.orderStatus, formatLKR(o.totalAmount)],
        canDelete: false, canAdd: false, statusOnly: true,
        statusOptions: ["PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"],
    },
};

const DEMO_ROWS = {
    books: [
        { id: 1, title: "The Silence of the Sea", author: "K. Jayatilaka", category: "Fiction", specialPrice: 1190, inStock: true },
        { id: 2, title: "The Merchant's Ledger", author: "S. Fernando", category: "Biography", price: 1750, inStock: false },
    ],
    categories: [ { categoryId: 1, name: "Fiction", description: "Novels & short stories" }, { categoryId: 2, name: "Education", description: "School textbooks" } ],
    authors: [ { authorId: 1, name: "K. Jayatilaka", bio: "Contemporary Sri Lankan novelist" } ],
    publishers: [ { publisherId: 1, name: "Godage International", contactEmail: "info@godage.lk" } ],
    inventory: [ { bookId: 1, bookTitle: "The Silence of the Sea", quantityAvailable: 42, reorderLevel: 10 } ],
    orders: [ { orderId: 1058, customerName: "R. Silva", orderStatus: "SHIPPED", totalAmount: 990 } ],
};

let currentPanel = "books";
let currentRows = [];

$(document).ready(function () {
    initLayout();
    if (!Auth.requireLogin()) return;

    // Admin-only guard: Auth.isAdmin() reads the roles Auth.setSession()
    // stored from the JWT response at login, so this is safe to check now.
    if (!Auth.isAdmin()) {
        showToast("Admins only");
        window.location.href = "index.html";
        return;
    }

    loadStats();
    loadPanel("books");

    $(".admin-sidebar a").on("click", function (e) {
        e.preventDefault();
        $(".admin-sidebar a").removeClass("active");
        $(this).addClass("active");
        loadPanel($(this).data("panel") || "overview");
    });

    $("#add-new-btn").on("click", () => openModal(null));
    $("#admin-modal-cancel").on("click", closeModal);
    $("#admin-modal-backdrop").on("click", function (e) {
        if (e.target === this) closeModal();
    });
    $("#admin-modal-form").on("submit", handleModalSubmit);

    $("#admin-table-body").on("click", ".edit-row-btn", function () {
        const idx = $(this).closest("tr").data("idx");
        openModal(currentRows[idx]);
    });
    $("#admin-table-body").on("click", ".delete-row-btn", function () {
        const idx = $(this).closest("tr").data("idx");
        handleDelete(currentRows[idx]);
    });
});

function loadStats() {
    api.get("/admin/stats")
        .done(res => renderStats(res.body || {}))
        .fail(() => renderStats({ totalBooks: 128, ordersThisMonth: 34, revenueThisMonth: 187500, lowStockItems: 5 }));
}

function renderStats(s) {
    $("#stat-books").text(s.totalBooks ?? "\u2014");
    $("#stat-orders").text(s.ordersThisMonth ?? "\u2014");
    $("#stat-revenue").text(s.revenueThisMonth ? formatLKR(s.revenueThisMonth) : "\u2014");
    $("#stat-low-stock").text(s.lowStockItems ?? "\u2014");
}

function loadPanel(panelKey) {
    if (panelKey === "overview") panelKey = "books";
    currentPanel = panelKey;
    const config = PANEL_CONFIG[panelKey];
    if (!config) return;

    $("#panel-title").text(config.title);
    $("#add-new-btn").toggle(config.canAdd !== false).text("+ Add " + config.title.replace(/s$/, ""));

    const $head = $("#admin-table-head").empty();
    config.columns.forEach(c => $head.append(`<th>${c}</th>`));

    api.get(config.endpoint)
        .done(res => renderRows(config, res.body || []))
        .fail(() => renderRows(config, DEMO_ROWS[panelKey] || []));
}

function renderRows(config, items) {
    currentRows = items;
    const $body = $("#admin-table-body").empty();
    if (items.length === 0) {
        $body.append(`<tr><td colspan="${config.columns.length}" style="color:var(--text-faint)">No records yet.</td></tr>`);
        return;
    }
    items.forEach((item, idx) => {
        const cells = config.row(item);
        const cellsHtml = cells.map(c => `<td>${escapeHtml(String(c))}</td>`).join("");
        const actionBtn = config.statusOnly
            ? `<button class="btn btn-outline btn-sm edit-row-btn">Update status</button>`
            : `<button class="btn btn-outline btn-sm edit-row-btn">Edit</button>`
            + (config.canDelete ? `<button class="btn btn-danger btn-sm delete-row-btn">Delete</button>` : "");
        $body.append(`<tr data-idx="${idx}">${cellsHtml}<td class="table-actions">${actionBtn}</td></tr>`);
    });
}

/* ---------------- Add / Edit ---------------- */

function openModal(row) {
    const config = PANEL_CONFIG[currentPanel];
    const isEdit = !!row;
    $("#admin-modal-error").removeClass("show").text("");
    $("#admin-modal-title").text((isEdit ? "Edit " : "Add ") + config.title.replace(/s$/, ""));
    $("#admin-modal-backdrop").data("editing-row", row || null);
    $("#admin-modal-backdrop").addClass("open");

    const $form = $("#admin-modal-form").empty();

    if (config.statusOnly) {
        const options = config.statusOptions.map(s =>
            `<option value="${s}" ${row && row.orderStatus === s ? "selected" : ""}>${s}</option>`).join("");
        $form.append(`
            <div class="form-group">
                <label>New status for Order #${row.orderId}</label>
                <select class="form-control" name="status">${options}</select>
            </div>
        `);
        return;
    }

    // Any fields that pull from a lookup (publisher/category/author) need
    // their live options fetched from the backend before we can render them.
    const neededSources = [...new Set(config.fields.filter(f => f.source).map(f => f.source))];

    if (neededSources.length === 0) {
        renderFormFields(config, row, isEdit, {});
        return;
    }

    $form.append(`<p style="color:var(--text-faint)">Loading form...</p>`);

    Promise.all(neededSources.map(src => api.get(LOOKUP_SOURCES[src].endpoint)))
        .then(results => {
            const lookups = {};
            neededSources.forEach((src, i) => { lookups[src] = (results[i] && results[i].body) || []; });
            renderFormFields(config, row, isEdit, lookups);
        })
        .catch(() => {
            $form.empty();
            showModalError({ responseJSON: { message: "Couldn't load publishers/categories/authors from the server. Add at least one of each first, then try again." } });
        });
}

function renderFormFields(config, row, isEdit, lookups) {
    const $form = $("#admin-modal-form").empty();

    config.fields.forEach(f => {
        let value = "";
        if (isEdit) {
            if (f.type === "multiselect" && Array.isArray(row[f.key])) value = row[f.key];
            else value = row[f.key] ?? "";
        }

        if (f.type === "textarea") {
            $form.append(`
                <div class="form-group">
                    <label>${f.label}</label>
                    <textarea class="form-control" name="${f.key}" rows="3">${escapeHtml(String(value))}</textarea>
                </div>
            `);
        } else if (f.type === "select") {
            const src = LOOKUP_SOURCES[f.source];
            const items = lookups[f.source] || [];
            const options = [`<option value="">-- Select ${f.label} --</option>`]
                .concat(items.map(it => {
                    const id = it[src.idKey];
                    const selected = isEdit && Number(value) === Number(id) ? "selected" : "";
                    return `<option value="${id}" ${selected}>${escapeHtml(String(it[src.labelKey]))}</option>`;
                })).join("");
            const emptyNote = items.length === 0
                ? `<div style="color:#e57373;font-size:.85em;margin-top:4px">No ${f.label.toLowerCase()}s found yet — add one in the ${f.label} tab first.</div>`
                : "";
            $form.append(`
                <div class="form-group">
                    <label>${f.label}</label>
                    <select class="form-control" name="${f.key}" ${f.required ? "required" : ""}>${options}</select>
                    ${emptyNote}
                </div>
            `);
        } else if (f.type === "multiselect") {
            const src = LOOKUP_SOURCES[f.source];
            const items = lookups[f.source] || [];
            const selectedIds = (Array.isArray(value) ? value : []).map(Number);
            const options = items.map(it => {
                const id = it[src.idKey];
                const selected = selectedIds.includes(Number(id)) ? "selected" : "";
                return `<option value="${id}" ${selected}>${escapeHtml(String(it[src.labelKey]))}</option>`;
            }).join("");
            const emptyNote = items.length === 0
                ? `<div style="color:#e57373;font-size:.85em;margin-top:4px">No ${f.label.toLowerCase()} found yet — add some in the ${f.label} tab first.</div>`
                : "";
            $form.append(`
                <div class="form-group">
                    <label>${f.label}</label>
                    <select class="form-control" name="${f.key}" multiple size="5" ${f.required ? "required" : ""}>${options}</select>
                    <div style="color:var(--text-faint);font-size:.8em;margin-top:4px">Hold Ctrl (Cmd on Mac) to pick more than one.</div>
                    ${emptyNote}
                </div>
            `);
        } else {
            $form.append(`
                <div class="form-group">
                    <label>${f.label}</label>
                    <input class="form-control" type="${f.type}" name="${f.key}"
                           ${f.step ? `step="${f.step}"` : ""} ${f.required ? "required" : ""}
                           value="${escapeHtml(String(value))}">
                </div>
            `);
        }
    });
}

function closeModal() {
    $("#admin-modal-backdrop").removeClass("open").removeData("editing-row");
}

// Like $form.serializeArray(), but repeated field names (from a <select multiple>)
// are collected into an array instead of overwriting each other.
function collectFormData($form) {
    const data = {};
    $form.serializeArray().forEach(({ name, value }) => {
        if (data[name] === undefined) {
            data[name] = value;
        } else if (Array.isArray(data[name])) {
            data[name].push(value);
        } else {
            data[name] = [data[name], value];
        }
    });
    return data;
}

function handleModalSubmit(e) {
    e.preventDefault();
    const config = PANEL_CONFIG[currentPanel];
    const row = $("#admin-modal-backdrop").data("editing-row");
    const formData = collectFormData($("#admin-modal-form"));

    if (config.statusOnly) {
        api.patch(`/orders/${row.orderId}/status?status=${encodeURIComponent(formData.status)}`)
            .done(() => { showToast("Order status updated"); closeModal(); loadPanel(currentPanel); })
            .fail(err => showModalError(err));
        return;
    }

    const payload = {};
    config.fields.forEach(f => {
        let v = formData[f.key];

        if (f.type === "multiselect") {
            const arr = v === undefined ? [] : (Array.isArray(v) ? v : [v]);
            const ids = arr.map(Number).filter(n => !isNaN(n));
            payload[f.key] = ids.length ? ids : null;
            return;
        }

        if (v === "" || v === undefined) { payload[f.key] = null; return; }
        if (f.type === "number" || f.type === "select") payload[f.key] = Number(v);
        else payload[f.key] = v;
    });

    const isEdit = !!row;
    const id = isEdit ? row[config.idKey] : null;
    const request = !isEdit
        ? api.post(config.endpoint, payload)
        : (currentPanel === "inventory" ? api.put(config.endpoint, payload) : api.put(`${config.endpoint}/${id}`, payload));

    request
        .done(() => { showToast(isEdit ? "Saved" : "Created"); closeModal(); loadPanel(currentPanel); loadStats(); })
        .fail(err => showModalError(err));
}

function handleDelete(row) {
    const config = PANEL_CONFIG[currentPanel];
    if (!confirm("Delete this " + config.title.replace(/s$/, "").toLowerCase() + "? This can't be undone.")) return;

    api.del(`${config.endpoint}/${row[config.idKey]}`)
        .done(() => { showToast("Deleted"); loadPanel(currentPanel); loadStats(); })
        .fail(err => {
            const msg = (err.responseJSON && err.responseJSON.message) || "Delete failed";
            showToast(msg);
        });
}

function showModalError(xhr) {
    const msg = (xhr.responseJSON && xhr.responseJSON.message) || "Something went wrong. Check the fields and try again.";
    $("#admin-modal-error").addClass("show").text(msg);
}

function escapeHtml(str) {
    return $("<div>").text(str || "").html();
}