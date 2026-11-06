/* Date and time fields: clicking anywhere in the box opens the picker (not only the small calendar icon). */
(function () {
    function open(e) {
        var el = e.target;
        if (!el.matches || !el.matches('input[type="date"], input[type="time"], input[type="datetime-local"], input[type="month"]')) return;
        if (el.readOnly || el.disabled || typeof el.showPicker !== 'function') return;
        try { el.showPicker(); } catch (err) { /* some browsers only allow it on a direct click */ }
    }
    document.addEventListener('click', open, true);
})();
