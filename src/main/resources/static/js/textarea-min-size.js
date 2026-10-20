/* Text boxes can be dragged bigger but never smaller than their starting size.
   The starting size is taken the first time the user presses on a box (so boxes that start hidden,
   e.g. in a tab, or that are added later, work too). */
(function () {
    function lockMin(t) {
        if (t.dataset.minLocked || !t.offsetHeight) return;
        t.style.minHeight = t.offsetHeight + 'px';
        if (t.offsetWidth && getComputedStyle(t).resize !== 'vertical') t.style.minWidth = t.offsetWidth + 'px';
        t.dataset.minLocked = '1';
    }
    document.addEventListener('pointerdown', function (e) {
        if (e.target && e.target.tagName === 'TEXTAREA') lockMin(e.target);
    }, true);
    function lockVisible() { document.querySelectorAll('textarea').forEach(lockMin); }
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', lockVisible);
    else lockVisible();
})();
