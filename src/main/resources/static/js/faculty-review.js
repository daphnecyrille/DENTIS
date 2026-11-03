/* Faculty review on the form itself: Approve (optionally with a drawn signature) and Revise (with a note). */
(function () {
    var pad = null, signing = false;
    var approveModal = document.getElementById('approveModal');
    var reviseModal = document.getElementById('reviseModal');
    if (!approveModal || !reviseModal) return;
    var submit = document.getElementById('approveSubmit');
    var padBox = document.getElementById('approveSignPad');
    var reason = document.getElementById('revisionReasonInput');

    window.openApproveModal = function (btn) {
        signing = btn.getAttribute('data-sign') === 'true';
        document.getElementById('approveTitle').textContent = btn.getAttribute('data-title') || 'Approve this chart?';
        document.getElementById('approveSignText').textContent = btn.getAttribute('data-sign-text') ||
            'Sign below to approve. Your signature is placed in the Faculty Signature box of this chart.';
        document.getElementById('approveForm').action = btn.getAttribute('data-action');
        document.getElementById('approveSignInput').value = '';
        document.getElementById('approvePlainText').style.display = signing ? 'none' : '';
        document.getElementById('approveSignText').style.display = signing ? '' : 'none';
        document.getElementById('approveSignClear').style.display = signing ? '' : 'none';
        padBox.style.display = signing ? '' : 'none';
        submit.textContent = signing ? 'Approve & sign' : 'Approve';
        submit.disabled = signing;
        approveModal.style.display = 'flex';
        if (signing) {
            pad = window.DentisSignaturePad.create(padBox, {
                hint: 'Sign here to approve',
                onChange: function (ink) { submit.disabled = !ink; }
            });
        }
    };
    window.closeApproveModal = function () { approveModal.style.display = 'none'; };
    document.getElementById('approveSignClear').onclick = function () { if (pad) pad.clear(); };
    document.getElementById('approveForm').addEventListener('submit', function (e) {
        if (!signing) return;
        if (!pad || pad.isEmpty()) { e.preventDefault(); return; }
        document.getElementById('approveSignInput').value = pad.toDataURL();
    });

    window.openReviseModal = function (btn) {
        document.getElementById('reviseForm').action = btn.getAttribute('data-action');
        reason.value = '';
        reason.style.borderColor = '#ccc';
        reviseModal.style.display = 'flex';
        reason.focus();
    };
    window.closeReviseModal = function () { reviseModal.style.display = 'none'; };
    document.getElementById('reviseForm').addEventListener('submit', function (e) {
        if (!reason.value.trim()) { e.preventDefault(); reason.style.borderColor = '#c00'; reason.focus(); }
    });
    reason.addEventListener('input', function () { reason.style.borderColor = '#ccc'; });

    [approveModal, reviseModal].forEach(function (m) {
        m.addEventListener('click', function (e) { if (e.target === m) m.style.display = 'none'; });
    });
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') { approveModal.style.display = 'none'; reviseModal.style.display = 'none'; }
    });
})();
