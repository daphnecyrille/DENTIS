/* "Report an Issue" (objective 4c): opened from the user menu on every page.
   Sends the subject, description and current page to /report-issue, and lists the user's recent reports. */
(function () {
    var css = document.createElement('style');
    css.textContent =
        '.ri-back{position:fixed;inset:0;background:rgba(15,25,40,.45);z-index:10000;display:none;align-items:center;justify-content:center;padding:16px}' +
        '.ri-back.show{display:flex}' +
        '.ri-box{background:#fff;border-radius:12px;width:100%;max-width:480px;max-height:90vh;overflow:auto;box-shadow:0 12px 40px rgba(0,0,0,.25);font-family:Poppins,sans-serif}' +
        '.ri-head{display:flex;align-items:center;justify-content:space-between;background:#2d4a6b;color:#fff;padding:14px 20px;border-radius:12px 12px 0 0}' +
        '.ri-head h3{margin:0;font-size:17px;font-weight:600}' +
        '.ri-x{background:none;border:none;color:#fff;font-size:24px;line-height:1;cursor:pointer;padding:0 4px}' +
        '.ri-body{padding:18px 20px;display:flex;flex-direction:column;gap:10px}' +
        '.ri-body label{font-size:13px;font-weight:600;color:#333}' +
        '.ri-body input,.ri-body textarea{width:100%;box-sizing:border-box;padding:9px 11px;border:1px solid #ccd5df;border-radius:6px;font:14px Poppins,sans-serif}' +
        '.ri-body textarea{min-height:110px;resize:vertical}' +
        '.ri-page{font-size:12px;color:#7a8796}' +
        '.ri-actions{display:flex;justify-content:flex-end;gap:8px;margin-top:4px}' +
        '.ri-btn{border:none;border-radius:6px;padding:9px 18px;font:600 14px Poppins,sans-serif;cursor:pointer}' +
        '.ri-send{background:#2d4a6b;color:#fff}.ri-send:disabled{opacity:.6}' +
        '.ri-cancel{background:#eef1f5;color:#333}' +
        '.ri-msg{font-size:13px;font-weight:600}.ri-msg.ok{color:#2f855a}.ri-msg.err{color:#c0392b}' +
        '.ri-mine{border-top:1px solid #eef1f5;padding:12px 20px 18px}' +
        '.ri-mine h4{margin:0 0 8px;font-size:13px;color:#6b7a8c;font-weight:600}' +
        '.ri-item{padding:8px 0;border-bottom:1px solid #f2f4f7;font-size:13px}.ri-item:last-child{border-bottom:none}' +
        '.ri-item-top{display:flex;justify-content:space-between;gap:10px}' +
        '.ri-st{font-size:11px;font-weight:700;padding:2px 8px;border-radius:10px;white-space:nowrap}' +
        '.ri-st.OPEN{background:#fdf3f2;color:#c0392b}.ri-st.IN_PROGRESS{background:#fdf8ee;color:#b7791f}.ri-st.RESOLVED{background:#f1f9f4;color:#2f855a}' +
        '.ri-note{margin-top:4px;color:#555;background:#f6f8fb;border-radius:6px;padding:6px 8px;white-space:pre-wrap}' +
        '.ri-date{color:#8a97a8;font-size:11px}';
    document.head.appendChild(css);

    var back = document.createElement('div');
    back.className = 'ri-back';
    back.innerHTML =
        '<div class="ri-box" role="dialog" aria-modal="true" aria-labelledby="riTitle">' +
        '<div class="ri-head"><h3 id="riTitle">Report an Issue</h3><button type="button" class="ri-x" aria-label="Close">&times;</button></div>' +
        '<form class="ri-body">' +
        '<label for="riSubject">What went wrong?</label>' +
        '<input id="riSubject" name="subject" maxlength="150"  required>' +
        '<label for="riDesc">Details</label>' +
        '<textarea id="riDesc" name="description" maxlength="4000"></textarea>' +
        '<div class="ri-page"></div>' +
        '<div class="ri-msg"></div>' +
        '<div class="ri-actions"><button type="button" class="ri-btn ri-cancel">Cancel</button><button type="submit" class="ri-btn ri-send">Send Report</button></div>' +
        '</form>' +
        '<div class="ri-mine" style="display:none"><h4>Your recent reports</h4><div class="ri-list"></div></div>' +
        '</div>';
    document.body.appendChild(back);

    var form = back.querySelector('form'), msg = back.querySelector('.ri-msg'), send = back.querySelector('.ri-send');
    var mine = back.querySelector('.ri-mine'), list = back.querySelector('.ri-list');
    var page = '';

    function esc(t) { var d = document.createElement('div'); d.textContent = t || ''; return d.innerHTML; }
    function loadMine() {
        fetch('/report-issue/mine', { credentials: 'same-origin' }).then(function (r) { return r.ok ? r.json() : []; }).then(function (rows) {
            if (!rows || !rows.length) { mine.style.display = 'none'; return; }
            list.innerHTML = rows.map(function (r) {
                return '<div class="ri-item"><div class="ri-item-top"><span>' + esc(r.subject) + '</span><span class="ri-st ' + esc(r.status) + '">' + esc(r.label) + '</span></div>' +
                    '<div class="ri-date">' + esc(r.date) + '</div>' + (r.note ? '<div class="ri-note">' + esc(r.note) + '</div>' : '') + '</div>';
            }).join('');
            mine.style.display = '';
        }).catch(function () {});
    }
    function open() {
        page = location.pathname + location.search;
        back.querySelector('.ri-page').textContent = 'Page: ' + page;
        msg.textContent = ''; msg.className = 'ri-msg';
        send.disabled = false;
        back.classList.add('show');
        loadMine();
        setTimeout(function () { back.querySelector('#riSubject').focus(); }, 50);
    }
    function close() { back.classList.remove('show'); }

    back.querySelector('.ri-x').addEventListener('click', close);
    back.querySelector('.ri-cancel').addEventListener('click', close);
    back.addEventListener('click', function (e) { if (e.target === back) close(); });
    document.addEventListener('keydown', function (e) { if (e.key === 'Escape' && back.classList.contains('show')) close(); });

    form.addEventListener('submit', function (e) {
        e.preventDefault();
        var fd = new FormData(form);
        fd.append('page', page);
        send.disabled = true;
        msg.textContent = 'Sending…'; msg.className = 'ri-msg';
        fetch('/report-issue', { method: 'POST', body: fd, credentials: 'same-origin' })
            .then(function (r) { return r.json().then(function (j) { if (!r.ok) throw new Error(j.error || 'Could not send'); return j; }); })
            .then(function () {
                form.reset();
                msg.textContent = '✓ Sent. The system administrator will look into it.'; msg.className = 'ri-msg ok';
                send.disabled = false;
                loadMine();
            })
            .catch(function (err) { msg.textContent = err.message + '. Please try again.'; msg.className = 'ri-msg err'; send.disabled = false; });
    });

    document.addEventListener('click', function (e) {
        var link = e.target.closest('[data-report-issue]');
        if (!link) return;
        e.preventDefault();
        e.stopPropagation();
        document.querySelectorAll('.user-dropdown.show').forEach(function (d) { d.classList.remove('show'); });
        open();
    }, true);
})();
