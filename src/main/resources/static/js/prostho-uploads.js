/* Prosthodontics booklet (clinician form): nicer upload cells and live row status.
   - every upload shows a thumbnail (PDFs as a "PDF" tile); click opens it full size
   - Upload / Replace button; a newly chosen file previews right away ("New, press Save to keep it", with Cancel)
   - Remove marks a saved file for removal on Save (Undo until then)
   - each row shows a check when it has a date and a photo, N/A greys the row */
(function () {
    var VIEW_ONLY = window.PROSTHO_VIEW_ONLY === true;
    var table = document.querySelector('.upload-table');
    if (!table) return;

    function isPdf(url) { return /\.pdf($|\?)/i.test(url || ''); }
    function thumb(url, isNewPdf) {
        var a = document.createElement('a');
        a.className = 'thumb';
        a.target = '_blank';
        a.href = url;
        a.title = 'Open full size';
        if (isNewPdf || isPdf(url)) {
            /* first page of the PDF, drawn by the browser's own PDF viewer */
            a.classList.add('thumb-pdf');
            var fr = document.createElement('iframe');
            fr.src = url + '#toolbar=0&navpanes=0&scrollbar=0&view=Fit';
            fr.tabIndex = -1; fr.setAttribute('aria-hidden', 'true');
            a.appendChild(fr);
            var tag = document.createElement('span'); tag.className = 'thumb-tag'; tag.textContent = 'PDF';
            a.appendChild(tag);
        }
        else { var img = document.createElement('img'); img.src = url; img.alt = 'Uploaded file'; a.appendChild(img); }
        return a;
    }
    function btn(text, cls) {
        var b = document.createElement('button');
        b.type = 'button'; b.className = 'upl-btn ' + (cls || ''); b.textContent = text;
        return b;
    }

    table.querySelectorAll('input.upload-input[type="file"]').forEach(function (input) {
        var field = input.name.replace(/File$/, '');
        /* the saved preview sits right before the input (a div holding <a><img>) */
        var prev = input.previousElementSibling;
        var savedLink = prev && prev.querySelector ? prev.querySelector('a[href]') : null;
        var savedUrl = savedLink ? savedLink.getAttribute('href') : null;

        var box = document.createElement('div');
        box.className = 'upl';
        var shown = document.createElement('div');
        shown.className = 'upl-shown';
        var note = document.createElement('div');
        note.className = 'upl-note';
        var actions = document.createElement('div');
        actions.className = 'upl-actions';
        box.appendChild(shown); box.appendChild(actions); box.appendChild(note);

        if (prev && savedLink) prev.remove();
        input.parentNode.insertBefore(box, input);
        input.classList.add('upl-input');
        actions.appendChild(input);

        var removeFlag = null, newUrl = null;
        var upBtn = btn(savedUrl ? 'Replace' : 'Upload', 'primary');
        var rmBtn = btn('Remove', 'danger');
        var cancelBtn = btn('Cancel', '');
        var undoBtn = btn('Undo', '');

        function render() {
            shown.innerHTML = ''; note.textContent = ''; note.className = 'upl-note';
            var file = input.files && input.files[0];
            if (newUrl) { URL.revokeObjectURL(newUrl); newUrl = null; }
            if (file) {
                newUrl = URL.createObjectURL(file);
                var t = thumb(newUrl, file.type === 'application/pdf');
                t.classList.add('is-new');
                shown.appendChild(t);
                note.textContent = 'New file: ' + file.name + ' (press Save to keep it)';
                note.classList.add('pending');
            } else if (savedUrl) {
                var s = thumb(savedUrl);
                if (removeFlag) { s.classList.add('is-removed'); note.textContent = 'Will be removed when you save.'; note.classList.add('removed'); }
                shown.appendChild(s);
            } else {
                var empty = document.createElement('span');
                empty.className = 'muted'; empty.textContent = 'No file yet';
                shown.appendChild(empty);
            }
            if (VIEW_ONLY) { actions.style.display = 'none'; return; }
            [upBtn, rmBtn, cancelBtn, undoBtn].forEach(function (b) { if (b.parentNode) b.remove(); });
            if (file) { actions.appendChild(cancelBtn); }
            else if (removeFlag) { actions.appendChild(undoBtn); actions.appendChild(upBtn); upBtn.textContent = 'Upload'; }
            else { upBtn.textContent = savedUrl ? 'Replace' : 'Upload'; actions.appendChild(upBtn); if (savedUrl) actions.appendChild(rmBtn); }
            updateRow(input.closest('tr'));
        }
        upBtn.addEventListener('click', function () { input.click(); });
        cancelBtn.addEventListener('click', function () { input.value = ''; render(); });
        rmBtn.addEventListener('click', function () {
            removeFlag = document.createElement('input');
            removeFlag.type = 'hidden'; removeFlag.name = 'removeFiles'; removeFlag.value = field;
            box.appendChild(removeFlag);
            render();
        });
        undoBtn.addEventListener('click', function () { if (removeFlag) removeFlag.remove(); removeFlag = null; render(); });
        input.addEventListener('change', render);
        box._hasFile = function () { return !!((input.files && input.files[0]) || (savedUrl && !removeFlag)); };
        box._isSignature = /Signature$/.test(field);
        render();
    });

    /* ── Row status: check when the row has a date (and its main photo, if the row takes one); N/A greys it ── */
    function updateRow(tr) {
        if (!tr) return;
        var tick = tr.querySelector('.tick');
        if (!tick) return;
        var na = tr.querySelector('.na-label input[type="checkbox"]');
        var date = tr.querySelector('input[type="date"]');
        var main = Array.prototype.filter.call(tr.querySelectorAll('.upl'), function (b) { return !b._isSignature; })[0];
        var isNa = !!(na && na.checked);
        var done = !isNa && date && date.value && (!main || main._hasFile());
        tr.classList.toggle('row-na', isNa);
        tr.classList.toggle('row-done', !!done);
        tr.classList.toggle('row-missing', !isNa && !done);
        tick.className = 'tick ' + (isNa ? 'na' : done ? 'done' : 'todo');
        tick.textContent = isNa ? 'N/A' : done ? '✓' : '';
    }
    table.querySelectorAll('tbody > tr').forEach(function (tr) {
        var first = tr.querySelector('td');
        if (!first) return;
        var tick = document.createElement('span');
        tick.className = 'tick todo';
        first.classList.add('step-cell');
        first.insertBefore(tick, first.firstChild);
        tr.addEventListener('input', function () { updateRow(tr); });
        tr.addEventListener('change', function () { updateRow(tr); });
        updateRow(tr);
    });

    /* ── Saving: chosen files go straight to storage first (no size limit from the web host),
          then the form is sent with just each file's address in "uploadedFiles" ── */
    var form = table.closest('form');
    var m = form && (form.getAttribute('action') || '').match(/prosthodontics-clinician\/(\d+)/);
    if (form && m && !VIEW_ONLY) {
        var chartId = m[1];
        var status = document.createElement('div');
        status.className = 'upl-status';
        status.style.cssText = 'display:none;position:fixed;left:50%;bottom:24px;transform:translateX(-50%);z-index:9999;' +
            'background:#2d4a6b;color:#fff;padding:12px 20px;border-radius:8px;font:600 14px Poppins,sans-serif;box-shadow:0 4px 16px rgba(0,0,0,.25)';
        document.body.appendChild(status);
        function say(text, isError) {
            status.textContent = text;
            status.style.background = isError ? '#c0392b' : '#2d4a6b';
            status.style.display = 'block';
        }

        form.addEventListener('submit', function (e) {
            if (form._uploadsDone) return;
            var picked = Array.prototype.filter.call(
                table.querySelectorAll('input.upload-input[type="file"]'), function (i) { return i.files && i.files[0]; });
            if (!picked.length) return;
            e.preventDefault();
            var submitter = e.submitter || null;
            var buttons = form.querySelectorAll('button, input[type="submit"]');
            buttons.forEach(function (b) { b.disabled = true; });

            var done = 0;
            (function next(k) {
                if (k >= picked.length) {
                    form._uploadsDone = true;
                    buttons.forEach(function (b) { b.disabled = false; });
                    say('Saving…');
                    if (submitter && submitter.name) {
                        var h = document.createElement('input');
                        h.type = 'hidden'; h.name = submitter.name; h.value = submitter.value;
                        form.appendChild(h);
                    }
                    HTMLFormElement.prototype.submit.call(form);
                    return;
                }
                var input = picked[k], file = input.files[0];
                var field = input.name.replace(/File$/, '');
                say('Uploading ' + (k + 1) + ' of ' + picked.length + ': ' + file.name);
                var fd = new FormData();
                fd.append('chartId', chartId); fd.append('field', field); fd.append('name', file.name);
                fetch('/attachments/sign', { method: 'POST', body: fd, credentials: 'same-origin' })
                    .then(function (r) { return r.json().then(function (j) { if (!r.ok) throw new Error(j.error || 'Upload refused'); return j; }); })
                    .then(function (j) {
                        return fetch(j.uploadUrl, {
                            method: 'PUT', body: file, credentials: j.uploadUrl.charAt(0) === '/' ? 'same-origin' : 'omit',
                            headers: { 'Content-Type': file.type || 'application/octet-stream' }
                        }).then(function (r) {
                            if (!r.ok) throw new Error('Upload failed (' + r.status + ')');
                            var h = document.createElement('input');
                            h.type = 'hidden'; h.name = 'uploadedFiles'; h.value = field + '|' + j.path;
                            form.appendChild(h);
                            input.value = '';          /* the file itself is not sent with the form */
                            done++;
                            next(k + 1);
                        });
                    })
                    .catch(function (err) {
                        buttons.forEach(function (b) { b.disabled = false; });
                        say('Could not upload ' + file.name + ': ' + err.message + '. Nothing was saved; try again.', true);
                        setTimeout(function () { status.style.display = 'none'; }, 7000);
                    });
            })(0);
        });
    }
    table.classList.add('booklet-table');
})();
