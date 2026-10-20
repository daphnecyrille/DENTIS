/* Side panel: opens when the pointer rests on the ☰ menu button or on the panel itself, and closes again
   when the pointer leaves both. Clicking ☰ keeps it open (click again to close). */
(function () {
    var menu = document.querySelector('.menu-icon');
    var sidebar = document.querySelector('.sidebar');
    if (!menu || !sidebar) return;
    var pinned = false, timer = null;

    function open()  { clearTimeout(timer); sidebar.classList.add('expanded'); }
    function closeSoon() {
        if (pinned) return;
        clearTimeout(timer);
        timer = setTimeout(function () { sidebar.classList.remove('expanded'); }, 250);
    }

    menu.addEventListener('mouseenter', open);
    menu.addEventListener('mouseleave', closeSoon);
    sidebar.addEventListener('mouseenter', open);
    sidebar.addEventListener('mouseleave', closeSoon);

    /* replaces the page's own toggle so a hover-then-click doesn't close the panel */
    menu.onclick = function (e) {
        if (e) e.stopPropagation();
        pinned = !pinned;
        if (pinned) open(); else sidebar.classList.remove('expanded');
    };
})();
