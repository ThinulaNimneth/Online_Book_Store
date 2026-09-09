/* ==========================================================================
   api.js - shared across every page.
   Central place for the API base URL, JWT storage, a thin $.ajax wrapper
   that attaches the Authorization header, and a small toast helper.
   ========================================================================== */

const API_BASE = "http://localhost:8080/api/v1";

const Auth = {
    TOKEN_KEY: "potha_token",
    USER_KEY: "potha_user",

    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    },
    setSession(token, user) {
        localStorage.setItem(this.TOKEN_KEY, token);
        localStorage.setItem(this.USER_KEY, JSON.stringify(user || {}));
    },
    getUser() {
        try { return JSON.parse(localStorage.getItem(this.USER_KEY)) || null; }
        catch (e) { return null; }
    },
    isLoggedIn() {
        return !!this.getToken();
    },
    isAdmin() {
        const u = this.getUser();
        return !!(u && u.roles && u.roles.includes("ADMIN"));
    },
    logout() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
        window.location.href = "login.html";
    },
    requireLogin() {
        if (!this.isLoggedIn()) {
            window.location.href = "login.html?redirect=" + encodeURIComponent(window.location.pathname);
            return false;
        }
        return true;
    }
};

/**
 * endpoint
 */
const api = {
    request(method, path, data) {
        return $.ajax({
            url: API_BASE + path,
            method: method,
            contentType: "application/json",
            data: data ? JSON.stringify(data) : undefined,
            headers: Auth.getToken() ? { Authorization: "Bearer " + Auth.getToken() } : {}
        });
    },
    get(path)        { return this.request("GET", path); },
    post(path, data) { return this.request("POST", path, data); },
    put(path, data)  { return this.request("PUT", path, data); },
    patch(path, data){ return this.request("PATCH", path, data); },
    del(path)         { return this.request("DELETE", path); }
};

function showToast(message) {
    let $t = $("#toast");
    if ($t.length === 0) {
        $t = $('<div id="toast"></div>').appendTo("body");
    }
    $t.text(message).addClass("show");
    clearTimeout(window.__toastTimer);
    window.__toastTimer = setTimeout(() => $t.removeClass("show"), 2600);
}

function formatLKR(amount) {
    const n = Number(amount || 0);
    return "Rs. " + n.toLocaleString("en-LK", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function starString(rating) {
    const r = Math.round(Number(rating) || 0);
    return "&#9733;".repeat(r).concat("&#9734;".repeat(5 - r));
}
