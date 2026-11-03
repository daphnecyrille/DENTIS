/* Draw-on-screen signature pad (finger, Apple Pencil or mouse).
   DentisSignaturePad.create(container, { hint, onChange(hasInk) }) -> { isEmpty(), toDataURL(), clear() } */
(function () {
    function create(container, opts) {
        opts = opts || {};
        container.innerHTML =
            '<div class="sigpad">' +
            '<span class="sigpad-line"></span><span class="sigpad-x">\u2715</span>' +
            '<span class="sigpad-hint">' + (opts.hint || 'Sign here') + '</span>' +
            '<canvas></canvas></div>';
        var canvas = container.querySelector('canvas');
        var hint = container.querySelector('.sigpad-hint');
        var ctx = canvas.getContext('2d');
        var drawing = false, last = null, ink = false;

        function setup() {
            var r = canvas.getBoundingClientRect(), d = window.devicePixelRatio || 1;
            if (!r.width) return;
            canvas.width = r.width * d; canvas.height = r.height * d;
            ctx.setTransform(d, 0, 0, d, 0, 0);
            ctx.lineWidth = 2.4; ctx.lineCap = 'round'; ctx.lineJoin = 'round'; ctx.strokeStyle = '#1b2a3a';
        }
        setup();
        function point(e) { var r = canvas.getBoundingClientRect(); return { x: e.clientX - r.left, y: e.clientY - r.top }; }

        canvas.addEventListener('pointerdown', function (e) {
            if (!canvas.width) setup();
            drawing = true; last = point(e);
            canvas.setPointerCapture(e.pointerId);
            hint.style.display = 'none';
            e.preventDefault();
        });
        canvas.addEventListener('pointermove', function (e) {
            if (!drawing) return;
            var p = point(e);
            ctx.beginPath(); ctx.moveTo(last.x, last.y); ctx.lineTo(p.x, p.y); ctx.stroke();
            last = p;
            if (!ink) { ink = true; if (opts.onChange) opts.onChange(true); }
            e.preventDefault();
        });
        ['pointerup', 'pointercancel', 'pointerleave'].forEach(function (t) {
            canvas.addEventListener(t, function () { drawing = false; });
        });

        return {
            isEmpty: function () { return !ink; },
            toDataURL: function () { return canvas.toDataURL('image/png'); },
            clear: function () {
                ctx.clearRect(0, 0, canvas.width, canvas.height);
                ink = false; hint.style.display = '';
                if (opts.onChange) opts.onChange(false);
            }
        };
    }
    window.DentisSignaturePad = { create: create };
})();
