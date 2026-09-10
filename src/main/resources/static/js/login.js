/* ==========================================================================
   login.js
   ========================================================================== */

$(document).ready(function () {
    initLayout();

    $("#show-password").on("change", function () {
        $("#password").attr("type", this.checked ? "text" : "password");
    });

    $("#facebook-login-btn, #google-login-btn").on("click", function () {
        showToast("Social login isn't part of the core coursework spec - TODO if you want it as a bonus.");
    });

    $("#forgot-password-link").on("click", function (e) {
        e.preventDefault();
        showToast("Password reset flow not implemented yet - TODO.");
    });

    $("#login-form").on("submit", function (e) {
        e.preventDefault();
        $("#login-error").removeClass("show");

        const email = $("#email").val().trim();
        const password = $("#password").val();
        const rememberMe = $("#remember-me").is(":checked");
        const $btn = $("#login-submit").prop("disabled", true).text("Signing in\u2026");

        api.post("/auth/login", { email, password })
            .done(function (res) {
                const body = res.body || {};
                Auth.setSession(body.token, {
                    userId: body.userId, fullName: body.fullName, roles: body.roles || ["USER"]
                });
                // "Remember Me"
                // localStorage in Auth.setSession so the token clears on tab close.
                if (!rememberMe) {
                    console.log("Remember Me unchecked - session persistence TODO");
                }
                showToast("Welcome back, " + (body.fullName || "reader") + "!");
                const params = new URLSearchParams(window.location.search);
                window.location.href = params.get("redirect") || "index.html";
            })
            .fail(function (xhr) {
                const msg = (xhr.responseJSON && xhr.responseJSON.message) || "Could not reach the server - is the backend running?";
                $("#login-error").text(msg).addClass("show");
            })
            .always(function () {
                $btn.prop("disabled", false).text("Sign In");
            });
    });
});
