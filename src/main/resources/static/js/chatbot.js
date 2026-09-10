/* ==========================================================================
   chatbot.js
   ========================================================================== */

const Chatbot = {
    history: [],
    open: false,

    init() {
        this.injectStyles();
        this.injectMarkup();
        this.bindEvents();
    },

    injectStyles() {
        const css = `
            #chatbot-toggle {
                position: fixed; right: 24px; bottom: 24px; z-index: 999;
                width: 56px; height: 56px; border-radius: 50%;
                background: var(--accent); color: var(--accent-ink);
                border: none; box-shadow: var(--shadow-2);
                font-size: 24px; cursor: pointer;
                display: flex; align-items: center; justify-content: center;
            }
            #chatbot-panel {
                position: fixed; right: 24px; bottom: 92px; z-index: 999;
                width: 340px; max-width: calc(100vw - 48px); height: 440px;
                background: var(--surface); border: 1px solid var(--border);
                border-radius: var(--radius-lg); box-shadow: var(--shadow-2);
                display: none; flex-direction: column; overflow: hidden;
                font-family: var(--font-body);
            }
            #chatbot-panel.open { display: flex; }
            #chatbot-header {
                padding: 14px 16px; background: var(--surface-2);
                border-bottom: 1px solid var(--border-soft);
                color: var(--text); font-weight: 600; font-size: 14px;
                display: flex; align-items: center; justify-content: space-between;
            }
            #chatbot-header span.sub { display:block; font-weight:400; font-size:11px; color: var(--text-faint); }
            #chatbot-close { background: none; border: none; color: var(--text-muted); cursor: pointer; font-size: 18px; }
            #chatbot-messages {
                flex: 1; overflow-y: auto; padding: 12px 14px;
                display: flex; flex-direction: column; gap: 8px;
            }
            .cb-msg { max-width: 85%; padding: 8px 12px; border-radius: var(--radius); font-size: 13px; line-height: 1.45; }
            .cb-msg.user { align-self: flex-end; background: var(--accent); color: var(--accent-ink); }
            .cb-msg.bot { align-self: flex-start; background: var(--surface-3); color: var(--text); }
            .cb-msg.bot.error { background: var(--danger-bg); color: var(--danger); }
            #chatbot-input-row { display: flex; gap: 8px; padding: 10px; border-top: 1px solid var(--border-soft); }
            #chatbot-input {
                flex: 1; background: var(--surface-2); border: 1px solid var(--border);
                border-radius: var(--radius-sm); color: var(--text); padding: 8px 10px; font-size: 13px;
            }
            #chatbot-send {
                background: var(--accent); color: var(--accent-ink); border: none;
                border-radius: var(--radius-sm); padding: 0 14px; font-weight: 600; cursor: pointer;
            }
            #chatbot-send:disabled { opacity: .5; cursor: default; }
        `;
        $("<style>").text(css).appendTo("head");
    },

    injectMarkup() {
        $("body").append(`
            <button id="chatbot-toggle" title="Ask Readora Assistant">&#128172;</button>
            <div id="chatbot-panel">
                <div id="chatbot-header">
                    <div>Readora Assistant<span class="sub">Ask about books, categories, prices</span></div>
                    <button id="chatbot-close">&times;</button>
                </div>
                <div id="chatbot-messages"></div>
                <div id="chatbot-input-row">
                    <input id="chatbot-input" type="text" placeholder="Ask for a recommendation..." />
                    <button id="chatbot-send">Send</button>
                </div>
            </div>
        `);
        this.appendMessage("bot", "Hi! I'm the Readora shopping assistant. Ask me for a book recommendation, or about a category or price range.");
    },

    bindEvents() {
        $("#chatbot-toggle").on("click", () => this.togglePanel());
        $("#chatbot-close").on("click", () => this.togglePanel(false));
        $("#chatbot-send").on("click", () => this.send());
        $("#chatbot-input").on("keydown", (e) => {
            if (e.key === "Enter") this.send();
        });
    },

    togglePanel(force) {
        this.open = force !== undefined ? force : !this.open;
        $("#chatbot-panel").toggleClass("open", this.open);
        if (this.open) $("#chatbot-input").focus();
    },

    appendMessage(role, text, isError) {
        const cls = role === "user" ? "user" : "bot" + (isError ? " error" : "");
        $("#chatbot-messages").append(`<div class="cb-msg ${cls}"></div>`).children().last().text(text);
        const $box = $("#chatbot-messages");
        $box.scrollTop($box[0].scrollHeight);
    },

    send() {
        const $input = $("#chatbot-input");
        const message = $input.val().trim();
        if (!message) return;

        this.appendMessage("user", message);
        $input.val("");
        $("#chatbot-send").prop("disabled", true);

        api.post("/chat", { message: message, history: this.history })
            .done((res) => {
                const reply = (res.body && res.body.reply) || "Sorry, I didn't get a reply.";
                this.appendMessage("bot", reply);
                this.history.push({ role: "user", content: message });
                this.history.push({ role: "assistant", content: reply });
                //
                if (this.history.length > 20) this.history = this.history.slice(-20);
            })
            .fail((xhr) => {
                const msg = (xhr.responseJSON && xhr.responseJSON.message) || "";
                const friendly = msg === "AI_NOT_CONFIGURED"
                    ? "The assistant isn't set up yet - the admin needs to add an AI API key."
                    : "Sorry, something went wrong reaching the assistant. Please try again.";
                this.appendMessage("bot", friendly, true);
            })
            .always(() => $("#chatbot-send").prop("disabled", false));
    }
};

$(document).ready(() => Chatbot.init());